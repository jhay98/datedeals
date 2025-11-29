package za.co.datedeals.api.services;

import com.lowagie.text.DocumentException;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;
import za.co.datedeals.api.entities.coupon.Coupon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class CouponPdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    /**
     * Generates a PDF from a coupon's HTML template
     * 
     * @param coupon The coupon to generate PDF for
     * @return byte array containing the PDF
     * @throws DocumentException if PDF generation fails
     * @throws IOException if there's an I/O error
     */
    public byte[] generatePdfFromCoupon(Coupon coupon) throws DocumentException, IOException {
        if (coupon == null) {
            throw new IllegalArgumentException("Coupon cannot be null");
        }

        if (coupon.getDeal() == null || coupon.getDeal().getHtmlVoucherTemplate() == null) {
            throw new IllegalArgumentException("Coupon must have a deal with an HTML template");
        }

        String htmlContent = processHtmlTemplate(coupon);
        return generatePdfFromHtml(htmlContent);
    }

    /**
     * Processes the HTML template by replacing placeholders with actual coupon data
     * 
     * @param coupon The coupon containing data to replace in the template
     * @return Processed HTML string with placeholders replaced
     */
    private String processHtmlTemplate(Coupon coupon) {
        String html = coupon.getDeal().getHtmlVoucherTemplate();

        // Replace common placeholders
        html = html.replace("{{couponCode}}", coupon.getCouponCode() != null ? coupon.getCouponCode() : "");
        html = html.replace("{{dealTitle}}", coupon.getDeal().getTitle() != null ? coupon.getDeal().getTitle() : "");
        html = html.replace("{{businessName}}", 
                coupon.getDeal().getBusiness() != null && coupon.getDeal().getBusiness().getBusinessName() != null 
                ? coupon.getDeal().getBusiness().getBusinessName() : "");
        html = html.replace("{{businessAddress}}", 
                coupon.getDeal().getBusiness() != null && coupon.getDeal().getBusiness().getAddress() != null 
                ? coupon.getDeal().getBusiness().getAddress() : "");
        html = html.replace("{{businessEmail}}", 
                coupon.getDeal().getBusiness() != null && coupon.getDeal().getBusiness().getContactEmail() != null 
                ? coupon.getDeal().getBusiness().getContactEmail() : "");
        html = html.replace("{{businessPhone}}", 
                coupon.getDeal().getBusiness() != null && coupon.getDeal().getBusiness().getContactPhone() != null 
                ? coupon.getDeal().getBusiness().getContactPhone() : "");
        
        // Format prices
        html = html.replace("{{purchasePrice}}", 
                coupon.getPurchasePrice() != null ? String.format("R%.2f", coupon.getPurchasePrice()) : "");
        html = html.replace("{{valuePrice}}", 
                coupon.getValuePrice() != null ? String.format("R%.2f", coupon.getValuePrice()) : "");
        
        // Format dates
        html = html.replace("{{issueDate}}", 
                coupon.getIssueDate() != null ? coupon.getIssueDate().format(DATE_FORMATTER) : "");
        html = html.replace("{{expireDate}}", 
                coupon.getExpireDate() != null ? coupon.getExpireDate().format(DATE_FORMATTER) : "");
        html = html.replace("{{redeemDate}}", 
                coupon.getRedeemDate() != null ? coupon.getRedeemDate().format(DATE_TIME_FORMATTER) : "");
        
        // Redeem status
        html = html.replace("{{redeemed}}", 
                coupon.getRedeemed() != null && coupon.getRedeemed() ? "REDEEMED" : "ACTIVE");

        // Ensure proper HTML structure
        if (!html.trim().startsWith("<!DOCTYPE") && !html.trim().startsWith("<html")) {
            html = wrapInHtmlStructure(html);
        }

        return html;
    }

    /**
     * Wraps content in proper HTML structure if not already present
     * 
     * @param content The content to wrap
     * @return Complete HTML document
     */
    private String wrapInHtmlStructure(String content) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            margin: 20px;
                        }
                        .coupon-container {
                            border: 2px solid #333;
                            padding: 20px;
                            max-width: 600px;
                        }
                    </style>
                </head>
                <body>
                    %s
                </body>
                </html>
                """.formatted(content);
    }

    /**
     * Generates PDF from HTML string using Flying Saucer and OpenPDF
     * 
     * @param html The HTML content to convert to PDF
     * @return byte array containing the PDF
     * @throws DocumentException if PDF generation fails
     * @throws IOException if there's an I/O error
     */
    private byte[] generatePdfFromHtml(String html) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            renderer.finishPDF();
            
            return outputStream.toByteArray();
        } finally {
            outputStream.close();
        }
    }

    /**
     * Generates a PDF with default styling if no template exists
     * 
     * @param coupon The coupon to generate PDF for
     * @return byte array containing the PDF
     * @throws DocumentException if PDF generation fails
     * @throws IOException if there's an I/O error
     */
    public byte[] generateDefaultPdf(Coupon coupon) throws DocumentException, IOException {
        if (coupon == null) {
            throw new IllegalArgumentException("Coupon cannot be null");
        }

        String htmlTemplate = createDefaultHtmlTemplate();
        
        // Create a temporary coupon-like object with the default template
        // to leverage the processHtmlTemplate method
        String originalTemplate = coupon.getDeal().getHtmlVoucherTemplate();
        coupon.getDeal().setHtmlVoucherTemplate(htmlTemplate);
        String processedHtml = processHtmlTemplate(coupon);
        coupon.getDeal().setHtmlVoucherTemplate(originalTemplate);
        
        return generatePdfFromHtml(processedHtml);
    }

    /**
     * Creates a default HTML template for coupons without custom templates
     * Uses placeholders that will be processed by processHtmlTemplate
     * 
     * @return Default HTML template string with placeholders
     */
    private String createDefaultHtmlTemplate() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            margin: 0;
                            padding: 20px;
                            background-color: #f5f5f5;
                        }
                        .coupon {
                            background: white;
                            border: 3px dashed #333;
                            border-radius: 10px;
                            padding: 30px;
                            max-width: 600px;
                            margin: 0 auto;
                            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                        }
                        .header {
                            text-align: center;
                            border-bottom: 2px solid #333;
                            padding-bottom: 20px;
                            margin-bottom: 20px;
                        }
                        .business-name {
                            font-size: 28px;
                            font-weight: bold;
                            color: #333;
                            margin-bottom: 10px;
                        }
                        .deal-title {
                            font-size: 20px;
                            color: #666;
                        }
                        .coupon-code {
                            text-align: center;
                            font-size: 36px;
                            font-weight: bold;
                            letter-spacing: 4px;
                            background: #f0f0f0;
                            padding: 20px;
                            border-radius: 5px;
                            margin: 20px 0;
                        }
                        .details {
                            margin: 20px 0;
                        }
                        .detail-row {
                            display: flex;
                            justify-content: space-between;
                            padding: 10px 0;
                            border-bottom: 1px solid #eee;
                        }
                        .label {
                            font-weight: bold;
                            color: #666;
                        }
                        .value {
                            color: #333;
                        }
                        .status {
                            text-align: center;
                            font-size: 24px;
                            font-weight: bold;
                            padding: 15px;
                            margin-top: 20px;
                            border-radius: 5px;
                        }
                        .status.active {
                            background: #4CAF50;
                            color: white;
                        }
                        .status.redeemed {
                            background: #f44336;
                            color: white;
                        }
                    </style>
                </head>
                <body>
                    <div class="coupon">
                        <div class="header">
                            <div class="business-name">{{businessName}}</div>
                            <div class="deal-title">{{dealTitle}}</div>
                        </div>
                        
                        <div class="coupon-code">{{couponCode}}</div>
                        
                        <div class="details">
                            <div class="detail-row">
                                <span class="label">Purchase Price:</span>
                                <span class="value">{{purchasePrice}}</span>
                            </div>
                            <div class="detail-row">
                                <span class="label">Value:</span>
                                <span class="value">{{valuePrice}}</span>
                            </div>
                            <div class="detail-row">
                                <span class="label">Issue Date:</span>
                                <span class="value">{{issueDate}}</span>
                            </div>
                            <div class="detail-row">
                                <span class="label">Expiry Date:</span>
                                <span class="value">{{expireDate}}</span>
                            </div>
                            <div class="detail-row">
                                <span class="label">Address:</span>
                                <span class="value">{{businessAddress}}</span>
                            </div>
                            <div class="detail-row">
                                <span class="label">Email:</span>
                                <span class="value">{{businessEmail}}</span>
                            </div>
                            <div class="detail-row">
                                <span class="label">Phone:</span>
                                <span class="value">{{businessPhone}}</span>
                            </div>
                        </div>
                        
                        <div class="status {{redeemed}}">{{redeemed}}</div>
                    </div>
                </body>
                </html>
                """;
    }
}
