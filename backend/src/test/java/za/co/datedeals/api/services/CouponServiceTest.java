package za.co.datedeals.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.datedeals.api.dtos.CouponResponseDto;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.coupon.Coupon;
import za.co.datedeals.api.entities.coupon.CouponRepository;
import za.co.datedeals.api.entities.deal.Deal;
import za.co.datedeals.api.utils.TestDataBuilder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    private Business testBusiness;
    private Deal testDeal;
    private Coupon testCoupon;

    @BeforeEach
    void setUp() {
        testBusiness = TestDataBuilder.createTestBusiness();
        testDeal = TestDataBuilder.createTestDeal(testBusiness);
        testCoupon = TestDataBuilder.createTestCoupon(testDeal);
    }

    @Test
    void getCouponsByBusinessId_ReturnsCouponsForBusiness() {
        // Arrange
        Coupon coupon2 = TestDataBuilder.createTestCoupon(testDeal);
        coupon2.setCouponId(2L);
        when(couponRepository.findByDeal_Business_BusinessId(testBusiness.getBusinessId()))
                .thenReturn(Arrays.asList(testCoupon, coupon2));

        // Act
        List<CouponResponseDto> result = couponService.getCouponsByBusinessId(testBusiness.getBusinessId());

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCouponId()).isEqualTo(testCoupon.getCouponId());
        assertThat(result.get(0).getBusinessName()).isEqualTo(testBusiness.getBusinessName());
        verify(couponRepository).findByDeal_Business_BusinessId(testBusiness.getBusinessId());
    }

    @Test
    void getCouponsByDealId_ReturnsCouponsForDeal() {
        // Arrange
        Coupon coupon2 = TestDataBuilder.createTestCoupon(testDeal);
        coupon2.setCouponId(2L);
        when(couponRepository.findByDeal_DealId(testDeal.getDealId()))
                .thenReturn(Arrays.asList(testCoupon, coupon2));

        // Act
        List<CouponResponseDto> result = couponService.getCouponsByDealId(testDeal.getDealId());

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDealId()).isEqualTo(testDeal.getDealId());
        verify(couponRepository).findByDeal_DealId(testDeal.getDealId());
    }

    @Test
    void redeemCoupon_WithValidCoupon_RedeemsCoupon() {
        // Arrange
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CouponResponseDto result = couponService.redeemCoupon(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCouponId()).isEqualTo(testCoupon.getCouponId());
        assertThat(testCoupon.getRedeemed()).isTrue();
        assertThat(testCoupon.getRedeemDate()).isNotNull();
        assertThat(testCoupon.getRedeemDate()).isBeforeOrEqualTo(LocalDateTime.now());
        verify(couponRepository).findById(1L);
        verify(couponRepository).save(testCoupon);
    }

    @Test
    void redeemCoupon_WithAlreadyRedeemedCoupon_ThrowsException() {
        // Arrange
        Coupon redeemedCoupon = TestDataBuilder.createRedeemedCoupon(testDeal);
        when(couponRepository.findById(redeemedCoupon.getCouponId()))
                .thenReturn(Optional.of(redeemedCoupon));

        // Act & Assert
        assertThatThrownBy(() -> couponService.redeemCoupon(redeemedCoupon.getCouponId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Coupon has already been redeemed");

        verify(couponRepository).findById(redeemedCoupon.getCouponId());
        verify(couponRepository, never()).save(any());
    }

    @Test
    void redeemCoupon_WithExpiredCoupon_ThrowsException() {
        // Arrange
        Coupon expiredCoupon = TestDataBuilder.createExpiredCoupon(testDeal);
        when(couponRepository.findById(expiredCoupon.getCouponId()))
                .thenReturn(Optional.of(expiredCoupon));

        // Act & Assert
        assertThatThrownBy(() -> couponService.redeemCoupon(expiredCoupon.getCouponId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Coupon has expired");

        verify(couponRepository).findById(expiredCoupon.getCouponId());
        verify(couponRepository, never()).save(any());
    }

    @Test
    void redeemCoupon_WithNonExistentCoupon_ThrowsException() {
        // Arrange
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> couponService.redeemCoupon(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Coupon not found");

        verify(couponRepository).findById(999L);
        verify(couponRepository, never()).save(any());
    }

    @Test
    void getCouponById_WithExistingId_ReturnsCoupon() {
        // Arrange
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));

        // Act
        Coupon result = couponService.getCouponById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCouponId()).isEqualTo(1L);
        assertThat(result.getCouponCode()).isEqualTo(testCoupon.getCouponCode());
        verify(couponRepository).findById(1L);
    }

    @Test
    void getCouponById_WithNonExistentId_ThrowsException() {
        // Arrange
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> couponService.getCouponById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Coupon not found");

        verify(couponRepository).findById(999L);
    }
}
