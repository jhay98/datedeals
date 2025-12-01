package za.co.datedeals.api.services;

import com.lowagie.text.DocumentException;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;
import za.co.datedeals.api.entities.coupon.Coupon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class CouponPdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    private static final int QR_CODE_SIZE = 200;
    
    private final QrCodeService qrCodeService;
    
    public CouponPdfService(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

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
        return processHtmlTemplate(coupon, html);
    }

    /**
     * Processes the provided HTML template by replacing placeholders with actual coupon data
     * 
     * @param coupon The coupon containing data to replace in the template
     * @param html The HTML template to process
     * @return Processed HTML string with placeholders replaced
     */
    private String processHtmlTemplate(Coupon coupon, String html) {

        // Replace common placeholders
        html = html.replace("{{couponCode}}", coupon.getCouponCode() != null ? coupon.getCouponCode() : "");
        html = html.replace("{{dealTitle}}", 
                coupon.getDeal() != null && coupon.getDeal().getTitle() != null ? coupon.getDeal().getTitle() : "");
        html = html.replace("{{businessName}}", 
                coupon.getDeal() != null && coupon.getDeal().getBusiness() != null && coupon.getDeal().getBusiness().getBusinessName() != null 
                ? coupon.getDeal().getBusiness().getBusinessName() : "");
        html = html.replace("{{businessAddress}}", 
                coupon.getDeal() != null && coupon.getDeal().getBusiness() != null && coupon.getDeal().getBusiness().getAddress() != null 
                ? coupon.getDeal().getBusiness().getAddress() : "");
        html = html.replace("{{businessEmail}}", 
                coupon.getDeal() != null && coupon.getDeal().getBusiness() != null && coupon.getDeal().getBusiness().getContactEmail() != null 
                ? coupon.getDeal().getBusiness().getContactEmail() : "");
        html = html.replace("{{businessPhone}}", 
                coupon.getDeal() != null && coupon.getDeal().getBusiness() != null && coupon.getDeal().getBusiness().getContactPhone() != null 
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
        
        // Generate QR code with redeem link
        String redeemUrl = "https://admin.datedeals.co.za/redeem/" + coupon.getCouponCode();
        html = html.replace("{{qrCode}}", generateQrCodeDataUrl(redeemUrl));

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
        String processedHtml = processHtmlTemplate(coupon, htmlTemplate);
        
        return generatePdfFromHtml(processedHtml);
    }

    /**
     * Creates a default HTML template for coupons without custom templates
     * Loads the template from the resources folder
     * 
     * @return Default HTML template string with placeholders
     * @throws IOException if the template file cannot be read
     */
    private String createDefaultHtmlTemplate() throws IOException {
        try (InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("templates/coupon-default.html")) {
            
            if (inputStream == null) {
                throw new IOException("Template file not found: templates/coupon-default.html");
            }
            
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    /**
     * Generates a QR code and returns it as a base64 data URL for embedding in HTML
     * 
     * @param text The text to encode in the QR code
     * @return Base64 data URL string for the QR code image
     */
    private String generateQrCodeDataUrl(String text) {
        try {
            if (text == null || text.isEmpty()) {
                return "";
            }
            
            byte[] qrCodeBytes = qrCodeService.generateQRCode(text, QR_CODE_SIZE, QR_CODE_SIZE);
            String base64Image = Base64.getEncoder().encodeToString(qrCodeBytes);
            return "data:image/png;base64," + base64Image;
        } catch (Exception e) {
            // Log error and return empty string to avoid breaking PDF generation
            System.err.println("Failed to generate QR code: " + e.getMessage());
            return "";
        }
    }
}
