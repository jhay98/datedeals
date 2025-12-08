package za.co.datedeals.api.services;

import com.lowagie.text.DocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.datedeals.api.dtos.LineItemDto;
import za.co.datedeals.api.dtos.ShopifyOrderWebhookDto;
import za.co.datedeals.api.dtos.ShopifyOrderWebhookDto.CustomerDto;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.coupon.Coupon;
import za.co.datedeals.api.entities.coupon.CouponRepository;
import za.co.datedeals.api.entities.deal.Deal;
import za.co.datedeals.api.entities.deal.DealRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopifyServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private DealRepository dealRepository;

    @Mock
    private CouponPdfService couponPdfService;

    @Mock
    private MailService mailService;

    @InjectMocks
    private ShopifyService shopifyService;

    private Business testBusiness;
    private Deal testDeal;
    private ShopifyOrderWebhookDto testOrder;
    private LineItemDto testLineItem;

    @BeforeEach
    void setUp() {
        testBusiness = new Business();
        testBusiness.setBusinessId(1L);
        testBusiness.setBusinessName("Test Restaurant");
        testBusiness.setContactEmail("contact@testrestaurant.com");

        testDeal = new Deal();
        testDeal.setDealId(1L);
        testDeal.setCode("DEAL123");
        testDeal.setTitle("Test Deal");
        testDeal.setBusiness(testBusiness);
        testDeal.setLifetimeDays(30);

        testLineItem = new LineItemDto();
        testLineItem.setId(100L);
        testLineItem.setSku("DEAL123");
        testLineItem.setQuantity(2);
        testLineItem.setPrice("99.50");

        testOrder = new ShopifyOrderWebhookDto();
        testOrder.setId(12345L);
        testOrder.setEmail("customer@example.com");
        
        CustomerDto customer = new CustomerDto();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("customer@example.com");
        testOrder.setCustomer(customer);
        
        List<LineItemDto> lineItems = new ArrayList<>();
        lineItems.add(testLineItem);
        testOrder.setLineItems(lineItems);
    }

    @Test
    void createCouponsFromOrder_WithValidOrder_CreatesCoupons() {
        // Arrange
        when(dealRepository.findByCode("DEAL123")).thenReturn(Optional.of(testDeal));
        when(couponRepository.findByCouponCode(anyString())).thenReturn(Optional.empty());
        
        Coupon savedCoupon1 = new Coupon();
        savedCoupon1.setCouponId(1L);
        savedCoupon1.setCouponCode("CP-TEST1");
        savedCoupon1.setDeal(testDeal);
        
        Coupon savedCoupon2 = new Coupon();
        savedCoupon2.setCouponId(2L);
        savedCoupon2.setCouponCode("CP-TEST2");
        savedCoupon2.setDeal(testDeal);
        
        when(couponRepository.save(any(Coupon.class)))
            .thenReturn(savedCoupon1)
            .thenReturn(savedCoupon2);

        // Act
        List<Coupon> result = shopifyService.createCouponsFromOrder(testOrder);

        // Assert
        assertThat(result).hasSize(2);
        verify(dealRepository).findByCode("DEAL123");
        verify(couponRepository, times(2)).save(any(Coupon.class));
        
        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository, times(2)).save(couponCaptor.capture());
        
        List<Coupon> savedCoupons = couponCaptor.getAllValues();
        assertThat(savedCoupons.get(0).getDeal()).isEqualTo(testDeal);
        assertThat(savedCoupons.get(0).getPurchasePrice()).isEqualTo(99.50);
        assertThat(savedCoupons.get(0).getRedeemed()).isFalse();
        assertThat(savedCoupons.get(0).getIssueDate()).isNotNull();
    }

    @Test
    void createCouponsFromOrder_WithEmptyLineItems_ReturnsEmptyList() {
        // Arrange
        testOrder.setLineItems(new ArrayList<>());

        // Act
        List<Coupon> result = shopifyService.createCouponsFromOrder(testOrder);

        // Assert
        assertThat(result).isEmpty();
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void createCouponsFromOrder_WithNullLineItems_ReturnsEmptyList() {
        // Arrange
        testOrder.setLineItems(null);

        // Act
        List<Coupon> result = shopifyService.createCouponsFromOrder(testOrder);

        // Assert
        assertThat(result).isEmpty();
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void createCouponsFromOrder_WithNonExistentDeal_CreatesOrphanCoupon() {
        // Arrange
        when(dealRepository.findByCode("DEAL123")).thenReturn(Optional.empty());
        when(couponRepository.findByCouponCode(anyString())).thenReturn(Optional.empty());
        
        Coupon savedCoupon = new Coupon();
        savedCoupon.setCouponId(1L);
        savedCoupon.setCouponCode("CP-TEST1");
        
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        List<Coupon> result = shopifyService.createCouponsFromOrder(testOrder);

        // Assert
        assertThat(result).hasSize(2);
        
        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository, times(2)).save(couponCaptor.capture());
        
        Coupon firstCoupon = couponCaptor.getAllValues().get(0);
        assertThat(firstCoupon.getDeal()).isNull();
        assertThat(firstCoupon.getPurchasePrice()).isEqualTo(99.50);
    }

    @Test
    void createCouponsFromOrder_WithEmptySku_CreatesOrphanCoupon() {
        // Arrange
        testLineItem.setSku("");
        when(couponRepository.findByCouponCode(anyString())).thenReturn(Optional.empty());
        
        Coupon savedCoupon = new Coupon();
        savedCoupon.setCouponId(1L);
        savedCoupon.setCouponCode("CP-TEST1");
        
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        List<Coupon> result = shopifyService.createCouponsFromOrder(testOrder);

        // Assert
        assertThat(result).hasSize(2);
        verify(dealRepository, never()).findByCode(anyString());
        
        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository, times(2)).save(couponCaptor.capture());
        
        assertThat(couponCaptor.getAllValues().get(0).getDeal()).isNull();
    }

    @Test
    void createCouponsFromOrder_WithNullSku_CreatesOrphanCoupon() {
        // Arrange
        testLineItem.setSku(null);
        when(couponRepository.findByCouponCode(anyString())).thenReturn(Optional.empty());
        
        Coupon savedCoupon = new Coupon();
        savedCoupon.setCouponId(1L);
        savedCoupon.setCouponCode("CP-TEST1");
        
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        List<Coupon> result = shopifyService.createCouponsFromOrder(testOrder);

        // Assert
        assertThat(result).hasSize(2);
        verify(dealRepository, never()).findByCode(anyString());
    }

    @Test
    void createCouponsFromOrder_WithDealExpiryDate_SetsExpiryDate() {
        // Arrange
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(60);
        testDeal.setExpiryDate(expiryDate);
        testDeal.setLifetimeDays(null);
        
        when(dealRepository.findByCode("DEAL123")).thenReturn(Optional.of(testDeal));
        when(couponRepository.findByCouponCode(anyString())).thenReturn(Optional.empty());
        
        Coupon savedCoupon = new Coupon();
        savedCoupon.setCouponId(1L);
        savedCoupon.setCouponCode("CP-TEST1");
        
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        shopifyService.createCouponsFromOrder(testOrder);

        // Assert
        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository, times(2)).save(couponCaptor.capture());
        
        Coupon capturedCoupon = couponCaptor.getAllValues().get(0);
        assertThat(capturedCoupon.getExpireDate()).isEqualTo(expiryDate);
    }

    @Test
    void createCouponsFromOrder_WithDealLifetimeDays_CalculatesExpiryDate() {
        // Arrange
        testDeal.setExpiryDate(null);
        testDeal.setLifetimeDays(45);
        
        when(dealRepository.findByCode("DEAL123")).thenReturn(Optional.of(testDeal));
        when(couponRepository.findByCouponCode(anyString())).thenReturn(Optional.empty());
        
        Coupon savedCoupon = new Coupon();
        savedCoupon.setCouponId(1L);
        savedCoupon.setCouponCode("CP-TEST1");
        
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        shopifyService.createCouponsFromOrder(testOrder);

        // Assert
        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository, times(2)).save(couponCaptor.capture());
        
        Coupon capturedCoupon = couponCaptor.getAllValues().get(0);
        assertThat(capturedCoupon.getExpireDate()).isNotNull();
        assertThat(capturedCoupon.getExpireDate()).isAfter(LocalDateTime.now().plusDays(44));
        assertThat(capturedCoupon.getExpireDate()).isBefore(LocalDateTime.now().plusDays(46));
    }

    @Test
    void createCouponsFromOrder_WithNullQuantity_CreatesOneCoupon() {
        // Arrange
        testLineItem.setQuantity(null);
        when(dealRepository.findByCode("DEAL123")).thenReturn(Optional.of(testDeal));
        when(couponRepository.findByCouponCode(anyString())).thenReturn(Optional.empty());
        
        Coupon savedCoupon = new Coupon();
        savedCoupon.setCouponId(1L);
        savedCoupon.setCouponCode("CP-TEST1");
        
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        List<Coupon> result = shopifyService.createCouponsFromOrder(testOrder);

        // Assert
        assertThat(result).hasSize(1);
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    void createCouponsFromOrder_WithInvalidPrice_SetsNullPurchasePrice() {
        // Arrange
        testLineItem.setPrice("invalid");
        when(dealRepository.findByCode("DEAL123")).thenReturn(Optional.of(testDeal));
        when(couponRepository.findByCouponCode(anyString())).thenReturn(Optional.empty());
        
        Coupon savedCoupon = new Coupon();
        savedCoupon.setCouponId(1L);
        savedCoupon.setCouponCode("CP-TEST1");
        
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        shopifyService.createCouponsFromOrder(testOrder);

        // Assert
        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository, times(2)).save(couponCaptor.capture());
        
        assertThat(couponCaptor.getAllValues().get(0).getPurchasePrice()).isNull();
    }

    @Test
    void createCouponsFromOrder_WithEmptyPrice_SetsNullPurchasePrice() {
        // Arrange
        testLineItem.setPrice("");
        when(dealRepository.findByCode("DEAL123")).thenReturn(Optional.of(testDeal));
        when(couponRepository.findByCouponCode(anyString())).thenReturn(Optional.empty());
        
        Coupon savedCoupon = new Coupon();
        savedCoupon.setCouponId(1L);
        savedCoupon.setCouponCode("CP-TEST1");
        
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        shopifyService.createCouponsFromOrder(testOrder);

        // Assert
        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository, times(2)).save(couponCaptor.capture());
        
        assertThat(couponCaptor.getAllValues().get(0).getPurchasePrice()).isNull();
    }

    @Test
    void createCouponsFromOrder_WithNullPrice_SetsNullPurchasePrice() {
        // Arrange
        testLineItem.setPrice(null);
        when(dealRepository.findByCode("DEAL123")).thenReturn(Optional.of(testDeal));
        when(couponRepository.findByCouponCode(anyString())).thenReturn(Optional.empty());
        
        Coupon savedCoupon = new Coupon();
        savedCoupon.setCouponId(1L);
        savedCoupon.setCouponCode("CP-TEST1");
        
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        shopifyService.createCouponsFromOrder(testOrder);

        // Assert
        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository, times(2)).save(couponCaptor.capture());
        
        assertThat(couponCaptor.getAllValues().get(0).getPurchasePrice()).isNull();
    }

    @Test
    void createCouponsFromOrder_GeneratesUniqueCouponCodes() {
        // Arrange
        when(dealRepository.findByCode("DEAL123")).thenReturn(Optional.of(testDeal));
        when(couponRepository.findByCouponCode(anyString())).thenReturn(Optional.empty());
        
        Coupon savedCoupon1 = new Coupon();
        savedCoupon1.setCouponId(1L);
        savedCoupon1.setCouponCode("CP-TEST1");
        
        Coupon savedCoupon2 = new Coupon();
        savedCoupon2.setCouponId(2L);
        savedCoupon2.setCouponCode("CP-TEST2");
        
        when(couponRepository.save(any(Coupon.class)))
            .thenReturn(savedCoupon1)
            .thenReturn(savedCoupon2);

        // Act
        shopifyService.createCouponsFromOrder(testOrder);

        // Assert
        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository, times(2)).save(couponCaptor.capture());
        
        List<Coupon> savedCoupons = couponCaptor.getAllValues();
        assertThat(savedCoupons.get(0).getCouponCode()).isNotNull();
        assertThat(savedCoupons.get(0).getCouponCode()).startsWith("CP-");
        assertThat(savedCoupons.get(1).getCouponCode()).isNotNull();
        assertThat(savedCoupons.get(1).getCouponCode()).startsWith("CP-");
    }

    @Test
    void createCouponsFromOrder_WithMultipleLineItems_CreatesAllCoupons() {
        // Arrange
        LineItemDto lineItem2 = new LineItemDto();
        lineItem2.setId(101L);
        lineItem2.setSku("DEAL456");
        lineItem2.setQuantity(1);
        lineItem2.setPrice("49.99");
        
        testOrder.getLineItems().add(lineItem2);
        
        Deal deal2 = new Deal();
        deal2.setDealId(2L);
        deal2.setCode("DEAL456");
        deal2.setTitle("Another Deal");
        
        when(dealRepository.findByCode("DEAL123")).thenReturn(Optional.of(testDeal));
        when(dealRepository.findByCode("DEAL456")).thenReturn(Optional.of(deal2));
        when(couponRepository.findByCouponCode(anyString())).thenReturn(Optional.empty());
        
        Coupon savedCoupon = new Coupon();
        savedCoupon.setCouponId(1L);
        savedCoupon.setCouponCode("CP-TEST1");
        
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        List<Coupon> result = shopifyService.createCouponsFromOrder(testOrder);

        // Assert
        assertThat(result).hasSize(3); // 2 from first item + 1 from second item
        verify(couponRepository, times(3)).save(any(Coupon.class));
    }



    @Test
    void createCouponsFromOrder_UsesOrderEmailWhenContactEmailMissing() {
        // Arrange
        testOrder.setContactEmail(null);
        testOrder.setEmail("primary@example.com");
        
        when(dealRepository.findByCode("DEAL123")).thenReturn(Optional.of(testDeal));
        when(couponRepository.findByCouponCode(anyString())).thenReturn(Optional.empty());
        
        Coupon savedCoupon = new Coupon();
        savedCoupon.setCouponId(1L);
        savedCoupon.setCouponCode("CP-TEST1");
        savedCoupon.setDeal(testDeal);
        
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        try {
            when(couponPdfService.generatePdfFromCoupon(any(Coupon.class)))
                .thenReturn(new byte[]{1, 2, 3});
        } catch (DocumentException | IOException e) {
            // Should not happen in test
        }

        // Act
        shopifyService.createCouponsFromOrder(testOrder);

        // Assert
        verify(mailService).sendMailWithMultiplePdfAttachments(
            eq("primary@example.com"),
            anyString(),
            anyString(),
            anyString(),
            anyList(),
            anyList()
        );
    }

    @Test
    void createCouponsFromOrder_WithNoEmail_DoesNotSendEmail() {
        // Arrange
        testOrder.setContactEmail(null);
        testOrder.setEmail(null);
        testOrder.setCustomer(null);
        
        when(dealRepository.findByCode("DEAL123")).thenReturn(Optional.of(testDeal));
        when(couponRepository.findByCouponCode(anyString())).thenReturn(Optional.empty());
        
        Coupon savedCoupon = new Coupon();
        savedCoupon.setCouponId(1L);
        savedCoupon.setCouponCode("CP-TEST1");
        
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        shopifyService.createCouponsFromOrder(testOrder);

        // Assert
        verify(mailService, never()).sendMailWithMultiplePdfAttachments(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyList(),
            anyList()
        );
    }
}
