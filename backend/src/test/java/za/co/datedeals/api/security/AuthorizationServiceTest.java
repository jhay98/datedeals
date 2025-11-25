package za.co.datedeals.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.coupon.Coupon;
import za.co.datedeals.api.entities.coupon.CouponRepository;
import za.co.datedeals.api.entities.deal.Deal;
import za.co.datedeals.api.entities.deal.DealRepository;
import za.co.datedeals.api.entities.user.User;
import za.co.datedeals.api.entities.user.UserRepository;
import za.co.datedeals.api.utils.TestDataBuilder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private DealRepository dealRepository;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthorizationService authorizationService;

    private User adminUser;
    private User businessUser;
    private Business testBusiness;
    private Business otherBusiness;
    private Deal testDeal;
    private Coupon testCoupon;

    @BeforeEach
    void setUp() {
        testBusiness = TestDataBuilder.createTestBusiness();
        otherBusiness = TestDataBuilder.createTestBusiness();
        otherBusiness.setBusinessId(2L);
        otherBusiness.setBusinessName("Other Business");

        adminUser = TestDataBuilder.createTestAdmin();
        adminUser.setRole(User.UserRole.ADMIN);

        businessUser = TestDataBuilder.createTestUser();
        businessUser.setRole(User.UserRole.BUSINESS);
        businessUser.setBusiness(testBusiness);

        testDeal = TestDataBuilder.createTestDeal(testBusiness);
        testCoupon = TestDataBuilder.createTestCoupon(testDeal);

        SecurityContextHolder.setContext(securityContext);
    }

    // canAccessBusiness Tests

    @Test
    void canAccessBusiness_WithAdminUser_ReturnsTrue() {
        // Arrange
        setupAuthentication("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // Act
        boolean result = authorizationService.canAccessBusiness(testBusiness.getBusinessId());

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void canAccessBusiness_WithBusinessUserOwnBusiness_ReturnsTrue() {
        // Arrange
        setupAuthentication("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(businessUser));

        // Act
        boolean result = authorizationService.canAccessBusiness(testBusiness.getBusinessId());

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void canAccessBusiness_WithBusinessUserOtherBusiness_ReturnsFalse() {
        // Arrange
        setupAuthentication("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(businessUser));

        // Act
        boolean result = authorizationService.canAccessBusiness(otherBusiness.getBusinessId());

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void canAccessBusiness_WithBusinessUserNoBusiness_ReturnsFalse() {
        // Arrange
        setupAuthentication("testuser");
        businessUser.setBusiness(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(businessUser));

        // Act
        boolean result = authorizationService.canAccessBusiness(testBusiness.getBusinessId());

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void canAccessBusiness_WithNoAuthentication_ReturnsFalse() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        boolean result = authorizationService.canAccessBusiness(testBusiness.getBusinessId());

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void canAccessBusiness_WithNonExistentUser_ThrowsException() {
        // Arrange
        setupAuthentication("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authorizationService.canAccessBusiness(testBusiness.getBusinessId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    // canAccessDeal Tests

    @Test
    void canAccessDeal_WithAdminUser_ReturnsTrue() {
        // Arrange
        setupAuthentication("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // Act
        boolean result = authorizationService.canAccessDeal(testDeal.getDealId());

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void canAccessDeal_WithBusinessUserOwnDeal_ReturnsTrue() {
        // Arrange
        setupAuthentication("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(businessUser));
        when(dealRepository.findById(testDeal.getDealId())).thenReturn(Optional.of(testDeal));

        // Act
        boolean result = authorizationService.canAccessDeal(testDeal.getDealId());

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void canAccessDeal_WithBusinessUserOtherBusinessDeal_ReturnsFalse() {
        // Arrange
        setupAuthentication("testuser");
        Deal otherDeal = TestDataBuilder.createTestDeal(otherBusiness);
        otherDeal.setDealId(2L);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(businessUser));
        when(dealRepository.findById(otherDeal.getDealId())).thenReturn(Optional.of(otherDeal));

        // Act
        boolean result = authorizationService.canAccessDeal(otherDeal.getDealId());

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void canAccessDeal_WithNonExistentDeal_ThrowsException() {
        // Arrange
        setupAuthentication("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(businessUser));
        when(dealRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authorizationService.canAccessDeal(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Deal not found");
    }

    // canAccessCoupon Tests

    @Test
    void canAccessCoupon_WithAdminUser_ReturnsTrue() {
        // Arrange
        setupAuthentication("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // Act
        boolean result = authorizationService.canAccessCoupon(testCoupon.getCouponId());

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void canAccessCoupon_WithBusinessUserOwnCoupon_ReturnsTrue() {
        // Arrange
        setupAuthentication("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(businessUser));
        when(couponRepository.findById(testCoupon.getCouponId())).thenReturn(Optional.of(testCoupon));

        // Act
        boolean result = authorizationService.canAccessCoupon(testCoupon.getCouponId());

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void canAccessCoupon_WithBusinessUserOtherBusinessCoupon_ReturnsFalse() {
        // Arrange
        setupAuthentication("testuser");
        Deal otherDeal = TestDataBuilder.createTestDeal(otherBusiness);
        Coupon otherCoupon = TestDataBuilder.createTestCoupon(otherDeal);
        otherCoupon.setCouponId(2L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(businessUser));
        when(couponRepository.findById(otherCoupon.getCouponId())).thenReturn(Optional.of(otherCoupon));

        // Act
        boolean result = authorizationService.canAccessCoupon(otherCoupon.getCouponId());

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void canAccessCoupon_WithNonExistentCoupon_ThrowsException() {
        // Arrange
        setupAuthentication("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(businessUser));
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authorizationService.canAccessCoupon(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Coupon not found");
    }

    private void setupAuthentication(String username) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
    }
}
