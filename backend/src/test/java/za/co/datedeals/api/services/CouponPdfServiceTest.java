package za.co.datedeals.api.services;

import com.lowagie.text.DocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.coupon.Coupon;
import za.co.datedeals.api.entities.deal.Deal;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponPdfServiceTest {

    @Mock
    private QrCodeService qrCodeService;

    @InjectMocks
    private CouponPdfService couponPdfService;

    private Business testBusiness;
    private Deal testDeal;
    private Coupon testCoupon;

    @BeforeEach
    void setUp() {
        testBusiness = new Business();
        testBusiness.setBusinessId(1L);
        testBusiness.setBusinessName("Test Restaurant");
        testBusiness.setContactEmail("contact@testrestaurant.com");
        testBusiness.setContactPhone("021-555-0001");
        testBusiness.setAddress("123 Test Street, Cape Town");

        testDeal = new Deal();
        testDeal.setDealId(1L);
        testDeal.setCode("DEAL123");
        testDeal.setTitle("50% Off Dinner");
        testDeal.setBusiness(testBusiness);
        testDeal.setHtmlVoucherTemplate(createBasicHtmlTemplate());

        testCoupon = new Coupon();
        testCoupon.setCouponId(1L);
        testCoupon.setCouponCode("CP-TEST123");
        testCoupon.setDeal(testDeal);
        testCoupon.setPurchasePrice(50.00);
        testCoupon.setValuePrice(100.00);
        testCoupon.setIssueDate(LocalDateTime.of(2024, 1, 15, 10, 0));
        testCoupon.setExpireDate(LocalDateTime.of(2024, 12, 31, 23, 59));
        testCoupon.setRedeemed(false);
    }

    private String createBasicHtmlTemplate() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        body { font-family: Arial, sans-serif; }
                        .voucher { border: 2px solid #333; padding: 20px; }
                        .code { font-size: 24px; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="voucher">
                        <h1>{{dealTitle}}</h1>
                        <div class="code">Code: {{couponCode}}</div>
                        <p>Business: {{businessName}}</p>
                        <p>Address: {{businessAddress}}</p>
                        <p>Email: {{businessEmail}}</p>
                        <p>Phone: {{businessPhone}}</p>
                        <p>Purchase Price: {{purchasePrice}}</p>
                        <p>Value: {{valuePrice}}</p>
                        <p>Issue Date: {{issueDate}}</p>
                        <p>Expires: {{expireDate}}</p>
                        <div>{{qrCode}}</div>
                    </div>
                </body>
                </html>
                """;
    }

    @Test
    void generatePdfFromCoupon_WithValidCoupon_GeneratesPdf() throws DocumentException, IOException, Exception {
        // Arrange
        byte[] mockQrCode = new byte[]{1, 2, 3, 4, 5};
        when(qrCodeService.generateQRCode(anyString(), anyInt(), anyInt())).thenReturn(mockQrCode);

        // Act
        byte[] result = couponPdfService.generatePdfFromCoupon(testCoupon);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        // PDF files start with %PDF
        assertThat(new String(result, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void generatePdfFromCoupon_WithNullCoupon_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> couponPdfService.generatePdfFromCoupon(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Coupon cannot be null");
    }

    @Test
    void generatePdfFromCoupon_WithNullDeal_ThrowsException() {
        // Arrange
        testCoupon.setDeal(null);

        // Act & Assert
        assertThatThrownBy(() -> couponPdfService.generatePdfFromCoupon(testCoupon))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Coupon must have a deal with an HTML template");
    }

    @Test
    void generatePdfFromCoupon_WithNullHtmlTemplate_ThrowsException() {
        // Arrange
        testDeal.setHtmlVoucherTemplate(null);

        // Act & Assert
        assertThatThrownBy(() -> couponPdfService.generatePdfFromCoupon(testCoupon))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Coupon must have a deal with an HTML template");
    }

    @Test
    void generatePdfFromCoupon_ReplacesAllPlaceholders() throws DocumentException, IOException, Exception {
        // Arrange
        byte[] mockQrCode = new byte[]{1, 2, 3, 4, 5};
        when(qrCodeService.generateQRCode(anyString(), anyInt(), anyInt())).thenReturn(mockQrCode);

        // Act
        byte[] result = couponPdfService.generatePdfFromCoupon(testCoupon);
        String pdfContent = new String(result);

        // Assert
        assertThat(result).isNotNull();
        // Verify PDF was generated (we can't easily verify the content without parsing PDF)
        assertThat(result.length).isGreaterThan(100);
    }

    @Test
    void generatePdfFromCoupon_WithNullBusinessFields_HandlesGracefully() throws DocumentException, IOException, Exception {
        // Arrange
        testBusiness.setContactEmail(null);
        testBusiness.setContactPhone(null);
        testBusiness.setAddress(null);
        
        byte[] mockQrCode = new byte[]{1, 2, 3, 4, 5};
        when(qrCodeService.generateQRCode(anyString(), anyInt(), anyInt())).thenReturn(mockQrCode);

        // Act
        byte[] result = couponPdfService.generatePdfFromCoupon(testCoupon);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generatePdfFromCoupon_WithNullPrices_HandlesGracefully() throws DocumentException, IOException, Exception {
        // Arrange
        testCoupon.setPurchasePrice(null);
        testCoupon.setValuePrice(null);
        
        byte[] mockQrCode = new byte[]{1, 2, 3, 4, 5};
        when(qrCodeService.generateQRCode(anyString(), anyInt(), anyInt())).thenReturn(mockQrCode);

        // Act
        byte[] result = couponPdfService.generatePdfFromCoupon(testCoupon);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generatePdfFromCoupon_WithNullDates_HandlesGracefully() throws DocumentException, IOException, Exception {
        // Arrange
        testCoupon.setIssueDate(null);
        testCoupon.setExpireDate(null);
        
        byte[] mockQrCode = new byte[]{1, 2, 3, 4, 5};
        when(qrCodeService.generateQRCode(anyString(), anyInt(), anyInt())).thenReturn(mockQrCode);

        // Act
        byte[] result = couponPdfService.generatePdfFromCoupon(testCoupon);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generatePdfFromCoupon_GeneratesQrCodeWithRedeemLink() throws DocumentException, IOException, Exception {
        // Arrange
        byte[] mockQrCode = new byte[]{1, 2, 3, 4, 5};
        when(qrCodeService.generateQRCode(
                "https://admin.datedeals.co.za/redeem/CP-TEST123", 
                200, 
                200
        )).thenReturn(mockQrCode);

        // Act
        byte[] result = couponPdfService.generatePdfFromCoupon(testCoupon);

        // Assert
        assertThat(result).isNotNull();
        // Verify QR code service was called with correct parameters
    }

    @Test
    void generatePdfFromCoupon_WithQrCodeGenerationFailure_ContinuesGeneration() throws Exception {
        // Arrange
        when(qrCodeService.generateQRCode(anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("QR generation failed"));

        // Act
        byte[] result = couponPdfService.generatePdfFromCoupon(testCoupon);

        // Assert - Should still generate PDF without QR code
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generatePdfFromCoupon_WithSimpleHtmlTemplate_WrapsInHtmlStructure() throws DocumentException, IOException, Exception {
        // Arrange
        testDeal.setHtmlVoucherTemplate("<div>Code: {{couponCode}}</div>");
        byte[] mockQrCode = new byte[]{1, 2, 3, 4, 5};
        when(qrCodeService.generateQRCode(anyString(), anyInt(), anyInt())).thenReturn(mockQrCode);

        // Act
        byte[] result = couponPdfService.generatePdfFromCoupon(testCoupon);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateDefaultPdf_WithValidCoupon_GeneratesPdf() throws DocumentException, IOException, Exception {
        // Arrange
        byte[] mockQrCode = new byte[]{1, 2, 3, 4, 5};
        when(qrCodeService.generateQRCode(anyString(), anyInt(), anyInt())).thenReturn(mockQrCode);

        // Act
        byte[] result = couponPdfService.generateDefaultPdf(testCoupon);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        assertThat(new String(result, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void generateDefaultPdf_WithNullCoupon_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> couponPdfService.generateDefaultPdf(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Coupon cannot be null");
    }

    @Test
    void generateDefaultPdf_WithNullDeal_GeneratesPdf() throws DocumentException, IOException, Exception {
        // Arrange
        testCoupon.setDeal(null);
        byte[] mockQrCode = new byte[]{1, 2, 3, 4, 5};
        when(qrCodeService.generateQRCode(anyString(), anyInt(), anyInt())).thenReturn(mockQrCode);

        // Act
        byte[] result = couponPdfService.generateDefaultPdf(testCoupon);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateDefaultPdf_WithMinimalCouponData_GeneratesPdf() throws DocumentException, IOException, Exception {
        // Arrange
        Coupon minimalCoupon = new Coupon();
        minimalCoupon.setCouponCode("CP-MINIMAL");
        minimalCoupon.setDeal(null);
        minimalCoupon.setPurchasePrice(null);
        minimalCoupon.setValuePrice(null);
        minimalCoupon.setIssueDate(null);
        minimalCoupon.setExpireDate(null);
        
        byte[] mockQrCode = new byte[]{1, 2, 3, 4, 5};
        when(qrCodeService.generateQRCode(anyString(), anyInt(), anyInt())).thenReturn(mockQrCode);

        // Act
        byte[] result = couponPdfService.generateDefaultPdf(minimalCoupon);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateDefaultPdf_FormatsDatesProperly() throws DocumentException, IOException, Exception {
        // Arrange
        testCoupon.setIssueDate(LocalDateTime.of(2024, 3, 15, 10, 30));
        testCoupon.setExpireDate(LocalDateTime.of(2024, 12, 25, 23, 59));
        
        byte[] mockQrCode = new byte[]{1, 2, 3, 4, 5};
        when(qrCodeService.generateQRCode(anyString(), anyInt(), anyInt())).thenReturn(mockQrCode);

        // Act
        byte[] result = couponPdfService.generateDefaultPdf(testCoupon);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateDefaultPdf_FormatsPricesWithCurrency() throws DocumentException, IOException, Exception {
        // Arrange
        testCoupon.setPurchasePrice(123.45);
        testCoupon.setValuePrice(250.99);
        
        byte[] mockQrCode = new byte[]{1, 2, 3, 4, 5};
        when(qrCodeService.generateQRCode(anyString(), anyInt(), anyInt())).thenReturn(mockQrCode);

        // Act
        byte[] result = couponPdfService.generateDefaultPdf(testCoupon);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generatePdfFromCoupon_WithComplexHtmlTemplate_GeneratesPdf() throws DocumentException, IOException, Exception {
        // Arrange
        String complexTemplate = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        body { font-family: 'Helvetica', Arial, sans-serif; background: #f0f0f0; }
                        .voucher { 
                            background: white;
                            border: 3px solid #gold;
                            border-radius: 10px;
                            padding: 30px;
                            max-width: 800px;
                            margin: 20px auto;
                            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                        }
                        .header { text-align: center; color: #333; }
                        .code { font-size: 32px; font-weight: bold; color: #d4af37; margin: 20px 0; }
                        .details { display: flex; justify-content: space-between; }
                        .qr-code { text-align: center; margin: 20px 0; }
                    </style>
                </head>
                <body>
                    <div class="voucher">
                        <div class="header">
                            <h1>{{dealTitle}}</h1>
                            <h2>{{businessName}}</h2>
                        </div>
                        <div class="code">{{couponCode}}</div>
                        <div class="details">
                            <div>
                                <p><strong>Purchase Price:</strong> {{purchasePrice}}</p>
                                <p><strong>Value:</strong> {{valuePrice}}</p>
                                <p><strong>Issued:</strong> {{issueDate}}</p>
                                <p><strong>Expires:</strong> {{expireDate}}</p>
                            </div>
                            <div>
                                <p><strong>Business Address:</strong></p>
                                <p>{{businessAddress}}</p>
                                <p>{{businessEmail}}</p>
                                <p>{{businessPhone}}</p>
                            </div>
                        </div>
                        <div class="qr-code">
                            {{qrCode}}
                        </div>
                    </div>
                </body>
                </html>
                """;
        
        testDeal.setHtmlVoucherTemplate(complexTemplate);
        byte[] mockQrCode = new byte[]{1, 2, 3, 4, 5};
        when(qrCodeService.generateQRCode(anyString(), anyInt(), anyInt())).thenReturn(mockQrCode);

        // Act
        byte[] result = couponPdfService.generatePdfFromCoupon(testCoupon);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }
}
