package za.co.datedeals.api.services;

import com.lowagie.text.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.datedeals.api.dtos.LineItemDto;
import za.co.datedeals.api.dtos.ShopifyOrderWebhookDto;
import za.co.datedeals.api.entities.coupon.Coupon;
import za.co.datedeals.api.entities.coupon.CouponRepository;
import za.co.datedeals.api.entities.deal.Deal;
import za.co.datedeals.api.entities.deal.DealRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ShopifyService {
    private static final Logger logger = LoggerFactory.getLogger(ShopifyService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private CouponPdfService couponPdfService;

    @Autowired
    private MailService mailService;

    @Transactional
    public List<Coupon> createCouponsFromOrder(ShopifyOrderWebhookDto orderDto) {
        List<Coupon> createdCoupons = new ArrayList<>();

        logger.info("Processing Shopify order {} with {} line items", 
                    orderDto.getId(), 
                    orderDto.getLineItems() != null ? orderDto.getLineItems().size() : 0);

        if (orderDto.getLineItems() == null || orderDto.getLineItems().isEmpty()) {
            logger.warn("No line items found in order {}", orderDto.getId());
            return createdCoupons;
        }

        for (LineItemDto lineItem : orderDto.getLineItems()) {
            // Look up deal by SKU (which maps to deal.code)
            Deal deal = null;
            if (lineItem.getSku() != null && !lineItem.getSku().trim().isEmpty()) {
                deal = dealRepository.findByCode(lineItem.getSku()).orElse(null);
                if (deal == null) {
                    logger.warn("Deal not found for SKU: {}. Creating coupon without deal link.", lineItem.getSku());
                }
            } else {
                logger.info("Line item {} has no SKU. Creating coupon without deal link.", lineItem.getId());
            }

            // Create coupons based on quantity
            int quantity = lineItem.getQuantity() != null ? lineItem.getQuantity() : 1;
            Double purchasePrice = parsePriceToDouble(lineItem.getPrice());

            for (int i = 0; i < quantity; i++) {
                Coupon coupon = new Coupon();
                coupon.setCouponCode(generateUniqueCouponCode());
                coupon.setPurchasePrice(purchasePrice);
                coupon.setIssueDate(LocalDateTime.now());
                coupon.setDeal(deal);
                coupon.setRedeemed(false);

                // Set expiry date if deal exists and has expiry information
                if (deal != null) {
                    if (deal.getExpiryDate() != null) {
                        coupon.setExpireDate(deal.getExpiryDate());
                    } else if (deal.getLifetimeDays() != null) {
                        coupon.setExpireDate(LocalDateTime.now().plusDays(deal.getLifetimeDays()));
                    }
                }

                Coupon savedCoupon = couponRepository.save(coupon);
                createdCoupons.add(savedCoupon);
                
                logger.info("Created coupon {} for line item {} (SKU: {})", 
                           savedCoupon.getCouponCode(), 
                           lineItem.getId(), 
                           lineItem.getSku());
            }
        }

        logger.info("Successfully created {} coupons for order {}", createdCoupons.size(), orderDto.getId());
        
        // Send email with coupon PDFs
        if (!createdCoupons.isEmpty()) {
            sendCouponEmailAsync(orderDto, createdCoupons);
        }
        
        return createdCoupons;
    }

    /**
     * Asynchronously generates PDFs and sends email with coupons to customer
     */
    private void sendCouponEmailAsync(ShopifyOrderWebhookDto orderDto, List<Coupon> coupons) {
        try {
            // Determine recipient email
            String recipientEmail = determineRecipientEmail(orderDto);
            if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
                logger.warn("No valid email found for order {}. Skipping email.", orderDto.getId());
                return;
            }

            // Determine recipient name
            String recipientName = determineRecipientName(orderDto);
            
            logger.info("Generating {} PDFs for order {}", coupons.size(), orderDto.getId());
            
            // Generate PDFs for each coupon
            List<byte[]> pdfAttachments = new ArrayList<>();
            List<String> attachmentNames = new ArrayList<>();
            
            for (Coupon coupon : coupons) {
                try {
                    byte[] pdfData;
                    if (coupon.getDeal() != null && coupon.getDeal().getHtmlVoucherTemplate() != null) {
                        pdfData = couponPdfService.generatePdfFromCoupon(coupon);
                    } else {
                        pdfData = couponPdfService.generateDefaultPdf(coupon);
                    }
                    
                    pdfAttachments.add(pdfData);
                    String fileName = String.format("coupon_%s.pdf", coupon.getCouponCode());
                    attachmentNames.add(fileName);
                    
                    logger.info("Generated PDF for coupon {}", coupon.getCouponCode());
                } catch (DocumentException | IOException e) {
                    logger.error("Failed to generate PDF for coupon {}: {}", 
                               coupon.getCouponCode(), e.getMessage(), e);
                }
            }
            
            if (pdfAttachments.isEmpty()) {
                logger.warn("No PDFs generated for order {}. Email will not be sent.", orderDto.getId());
                return;
            }
            
            // Build email content
            String subject = "Your DateDeals Coupons - Order #" + orderDto.getId();
            String body = buildCouponEmailBody(recipientName, coupons, orderDto);
            
            // Send email with PDF attachments
            mailService.sendMailWithMultiplePdfAttachments(
                    recipientEmail,
                    recipientName,
                    subject,
                    body,
                    pdfAttachments,
                    attachmentNames
            );
            
            logger.info("Queued email with {} coupon PDFs to {} for order {}", 
                       pdfAttachments.size(), recipientEmail, orderDto.getId());
            
        } catch (Exception e) {
            logger.error("Failed to send coupon email for order {}: {}", 
                        orderDto.getId(), e.getMessage(), e);
        }
    }

    /**
     * Determines the recipient email from order data
     */
    private String determineRecipientEmail(ShopifyOrderWebhookDto orderDto) {
        // Priority: contact_email > email > customer.email
        if (orderDto.getContactEmail() != null && !orderDto.getContactEmail().trim().isEmpty()) {
            return orderDto.getContactEmail();
        }
        if (orderDto.getEmail() != null && !orderDto.getEmail().trim().isEmpty()) {
            return orderDto.getEmail();
        }
        if (orderDto.getCustomer() != null && orderDto.getCustomer().getEmail() != null) {
            return orderDto.getCustomer().getEmail();
        }
        return null;
    }

    /**
     * Determines the recipient name from order data
     */
    private String determineRecipientName(ShopifyOrderWebhookDto orderDto) {
        if (orderDto.getCustomer() != null) {
            String firstName = orderDto.getCustomer().getFirstName();
            String lastName = orderDto.getCustomer().getLastName();
            
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            } else if (firstName != null) {
                return firstName;
            } else if (lastName != null) {
                return lastName;
            }
        }
        return "Valued Customer";
    }

    /**
     * Builds HTML email body for coupon delivery
     */
    private String buildCouponEmailBody(String recipientName, List<Coupon> coupons, ShopifyOrderWebhookDto orderDto) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>")
            .append("<html><head><meta charset='UTF-8'/><style>")
            .append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }")
            .append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }")
            .append(".header { background: #4CAF50; color: white; padding: 20px; text-align: center; }")
            .append(".content { padding: 20px; background: #f9f9f9; }")
            .append(".coupon-list { margin: 20px 0; }")
            .append(".coupon-item { background: white; padding: 15px; margin: 10px 0; border-left: 4px solid #4CAF50; }")
            .append(".coupon-code { font-size: 18px; font-weight: bold; color: #4CAF50; }")
            .append(".footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }")
            .append("</style></head><body>");
        
        html.append("<div class='container'>")
            .append("<div class='header'><h1>Your DateDeals Coupons</h1></div>")
            .append("<div class='content'>");
        
        html.append("<p>Dear ").append(recipientName).append(",</p>")
            .append("<p>Thank you for your purchase! Your coupons are ready to use.</p>")
            .append("<p><strong>Order ID:</strong> #").append(orderDto.getId()).append("</p>");
        
        html.append("<div class='coupon-list'><h3>Your Coupons:</h3>");
        
        for (Coupon coupon : coupons) {
            html.append("<div class='coupon-item'>")
                .append("<div class='coupon-code'>").append(coupon.getCouponCode()).append("</div>");
            
            if (coupon.getDeal() != null) {
                html.append("<div><strong>Deal:</strong> ").append(coupon.getDeal().getTitle()).append("</div>");
                
                if (coupon.getDeal().getBusiness() != null) {
                    html.append("<div><strong>Business:</strong> ")
                        .append(coupon.getDeal().getBusiness().getBusinessName())
                        .append("</div>");
                }
            }
            
            if (coupon.getExpireDate() != null) {
                html.append("<div><strong>Expires:</strong> ")
                    .append(coupon.getExpireDate().format(DATE_FORMATTER))
                    .append("</div>");
            }
            
            html.append("</div>");
        }
        
        html.append("</div>"); // Close coupon-list
        
        html.append("<p>Your coupon PDFs are attached to this email. You can present these at the business location to redeem your deal.</p>")
            .append("<p><strong>Important:</strong> Please keep these coupons safe and present them when redeeming.</p>")
            .append("</div>"); // Close content
        
        html.append("<div class='footer'>")
            .append("<p>Thank you for choosing DateDeals!</p>")
            .append("<p>If you have any questions, please contact us.</p>")
            .append("</div>")
            .append("</div></body></html>"); // Close container
        
        return html.toString();
    }

    private String generateUniqueCouponCode() {
        String code;
        do {
            code = "CP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        } while (couponRepository.findByCouponCode(code).isPresent());
        return code;
    }

    private Double parsePriceToDouble(String price) {
        if (price == null || price.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(price);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse price: {}", price);
            return null;
        }
    }
}
