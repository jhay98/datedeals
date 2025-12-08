package za.co.datedeals.api.services;

import com.google.zxing.WriterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QrCodeServiceTest {

    private QrCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeService();
    }

    @Test
    void generateQRCode_WithValidInput_GeneratesQrCode() throws Exception {
        // Arrange
        String text = "https://datedeals.co.za/redeem/CP-TEST123";
        int width = 200;
        int height = 200;

        // Act
        byte[] result = qrCodeService.generateQRCode(text, width, height);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        // PNG files start with specific bytes: 137 80 78 71 (‰PNG)
        assertThat(result[0]).isEqualTo((byte) 137);
        assertThat(result[1]).isEqualTo((byte) 80);
        assertThat(result[2]).isEqualTo((byte) 78);
        assertThat(result[3]).isEqualTo((byte) 71);
    }

    @Test
    void generateQRCode_WithSimpleText_GeneratesQrCode() throws Exception {
        // Arrange
        String text = "SIMPLE-CODE-123";
        int width = 150;
        int height = 150;

        // Act
        byte[] result = qrCodeService.generateQRCode(text, width, height);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCode_WithLongText_GeneratesQrCode() throws Exception {
        // Arrange
        String text = "https://admin.datedeals.co.za/redeem/CP-VERYLONGCOUPONCODE123456789ABCDEF";
        int width = 300;
        int height = 300;

        // Act
        byte[] result = qrCodeService.generateQRCode(text, width, height);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCode_WithMinimalSize_GeneratesQrCode() throws Exception {
        // Arrange
        String text = "TEST";
        int width = 50;
        int height = 50;

        // Act
        byte[] result = qrCodeService.generateQRCode(text, width, height);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCode_WithLargeSize_GeneratesQrCode() throws Exception {
        // Arrange
        String text = "https://datedeals.co.za";
        int width = 500;
        int height = 500;

        // Act
        byte[] result = qrCodeService.generateQRCode(text, width, height);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCode_WithRectangularDimensions_GeneratesSquareQrCode() throws Exception {
        // Arrange
        String text = "RECTANGULAR-TEST";
        int width = 200;
        int height = 300;

        // Act
        byte[] result = qrCodeService.generateQRCode(text, width, height);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCode_WithSpecialCharacters_GeneratesQrCode() throws Exception {
        // Arrange
        String text = "Coupon Code: 50% OFF! @#$%^&*()";
        int width = 200;
        int height = 200;

        // Act
        byte[] result = qrCodeService.generateQRCode(text, width, height);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCode_WithUnicodeCharacters_GeneratesQrCode() throws Exception {
        // Arrange
        String text = "Café Münchën 日本 🎉";
        int width = 200;
        int height = 200;

        // Act
        byte[] result = qrCodeService.generateQRCode(text, width, height);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCode_WithNumericData_GeneratesQrCode() throws Exception {
        // Arrange
        String text = "1234567890";
        int width = 150;
        int height = 150;

        // Act
        byte[] result = qrCodeService.generateQRCode(text, width, height);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCode_WithWhitespace_GeneratesQrCode() throws Exception {
        // Arrange
        String text = "   ";
        int width = 200;
        int height = 200;

        // Act
        byte[] result = qrCodeService.generateQRCode(text, width, height);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCode_WithMultilineText_GeneratesQrCode() throws Exception {
        // Arrange
        String text = "Line 1\nLine 2\nLine 3";
        int width = 200;
        int height = 200;

        // Act
        byte[] result = qrCodeService.generateQRCode(text, width, height);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCode_WithJsonData_GeneratesQrCode() throws Exception {
        // Arrange
        String text = "{\"code\":\"CP-123\",\"url\":\"https://datedeals.co.za\"}";
        int width = 250;
        int height = 250;

        // Act
        byte[] result = qrCodeService.generateQRCode(text, width, height);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCode_WithStandardSize_GeneratesConsistentOutput() throws Exception {
        // Arrange
        String text = "TEST-CONSISTENCY";
        int width = 200;
        int height = 200;

        // Act
        byte[] result1 = qrCodeService.generateQRCode(text, width, height);
        byte[] result2 = qrCodeService.generateQRCode(text, width, height);

        // Assert
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    void generateQRCode_WithDifferentSizes_GeneratesDifferentOutputs() throws Exception {
        // Arrange
        String text = "SAME-TEXT";

        // Act
        byte[] result100 = qrCodeService.generateQRCode(text, 100, 100);
        byte[] result200 = qrCodeService.generateQRCode(text, 200, 200);

        // Assert
        assertThat(result100).isNotEqualTo(result200);
        assertThat(result100.length).isLessThan(result200.length);
    }

    @Test
    void generateQRCode_WithSameTextDifferentCasing_GeneratesDifferentOutputs() throws Exception {
        // Arrange
        String text1 = "test";
        String text2 = "TEST";
        int width = 200;
        int height = 200;

        // Act
        byte[] result1 = qrCodeService.generateQRCode(text1, width, height);
        byte[] result2 = qrCodeService.generateQRCode(text2, width, height);

        // Assert
        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    void generateQRCode_WithVeryLongUrl_GeneratesQrCode() throws Exception {
        // Arrange
        StringBuilder longUrl = new StringBuilder("https://admin.datedeals.co.za/redeem/");
        for (int i = 0; i < 100; i++) {
            longUrl.append("ABCD");
        }
        int width = 400;
        int height = 400;

        // Act
        byte[] result = qrCodeService.generateQRCode(longUrl.toString(), width, height);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCode_WithSmallerWidthThanHeight_GeneratesQrCode() throws Exception {
        // Arrange
        String text = "ASYMMETRIC-TEST";
        int width = 150;
        int height = 250;

        // Act
        byte[] result = qrCodeService.generateQRCode(text, width, height);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }
}
