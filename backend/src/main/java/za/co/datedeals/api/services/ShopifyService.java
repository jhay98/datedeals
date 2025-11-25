package za.co.datedeals.api.services;

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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ShopifyService {
    private static final Logger logger = LoggerFactory.getLogger(ShopifyService.class);

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private DealRepository dealRepository;

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
        return createdCoupons;
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
