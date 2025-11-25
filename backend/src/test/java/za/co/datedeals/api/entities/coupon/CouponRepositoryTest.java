package za.co.datedeals.api.entities.coupon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.deal.Deal;
import za.co.datedeals.api.utils.TestDataBuilder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class CouponRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CouponRepository couponRepository;

    private Business testBusiness;
    private Deal testDeal;
    private Coupon testCoupon;

    @BeforeEach
    void setUp() {
        testBusiness = TestDataBuilder.createTestBusiness();
        testBusiness.setBusinessId(null);
        testBusiness = entityManager.persistAndFlush(testBusiness);

        testDeal = TestDataBuilder.createTestDeal(testBusiness);
        testDeal.setDealId(null);
        testDeal = entityManager.persistAndFlush(testDeal);

        testCoupon = TestDataBuilder.createTestCoupon(testDeal);
        testCoupon.setCouponId(null);
    }

    @Test
    void findByDeal_Business_BusinessId_WithExistingBusinessId_ReturnsCoupons() {
        // Arrange
        Coupon coupon2 = TestDataBuilder.createTestCoupon(testDeal);
        coupon2.setCouponId(null);
        coupon2.setCouponCode("COUPON456");

        entityManager.persist(testCoupon);
        entityManager.persist(coupon2);
        entityManager.flush();

        // Act
        List<Coupon> coupons = couponRepository.findByDeal_Business_BusinessId(testBusiness.getBusinessId());

        // Assert
        assertThat(coupons).hasSize(2);
        assertThat(coupons)
                .extracting(Coupon::getCouponCode)
                .containsExactlyInAnyOrder("COUPON123", "COUPON456");
    }

    @Test
    void findByDeal_Business_BusinessIdAndRedeemedFalse_ReturnsOnlyUnredeemedCoupons() {
        // Arrange
        Coupon redeemedCoupon = TestDataBuilder.createRedeemedCoupon(testDeal);
        redeemedCoupon.setCouponId(null);

        entityManager.persist(testCoupon);
        entityManager.persist(redeemedCoupon);
        entityManager.flush();

        // Act
        List<Coupon> coupons = couponRepository.findByDeal_Business_BusinessIdAndRedeemedFalse(
                testBusiness.getBusinessId());

        // Assert
        assertThat(coupons).hasSize(1);
        assertThat(coupons.get(0).getCouponCode()).isEqualTo("COUPON123");
        assertThat(coupons.get(0).getRedeemed()).isFalse();
    }

    @Test
    void findByDeal_DealId_WithExistingDealId_ReturnsCoupons() {
        // Arrange
        entityManager.persistAndFlush(testCoupon);

        // Act
        List<Coupon> coupons = couponRepository.findByDeal_DealId(testDeal.getDealId());

        // Assert
        assertThat(coupons).hasSize(1);
        assertThat(coupons.get(0).getCouponCode()).isEqualTo("COUPON123");
    }

    @Test
    void findByDeal_DealId_WithNonExistentDealId_ReturnsEmptyList() {
        // Act
        List<Coupon> coupons = couponRepository.findByDeal_DealId(999L);

        // Assert
        assertThat(coupons).isEmpty();
    }

    @Test
    void findByCouponCode_WithExistingCode_ReturnsCoupon() {
        // Arrange
        testCoupon = entityManager.persistAndFlush(testCoupon);

        // Act
        Optional<Coupon> found = couponRepository.findByCouponCode("COUPON123");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getCouponCode()).isEqualTo("COUPON123");
        assertThat(found.get().getPurchasePrice()).isEqualTo(50.0);
    }

    @Test
    void findByCouponCode_WithNonExistentCode_ReturnsEmpty() {
        // Act
        Optional<Coupon> found = couponRepository.findByCouponCode("NONEXISTENT");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void save_WithValidCoupon_PersistsCoupon() {
        // Act
        Coupon saved = couponRepository.save(testCoupon);
        entityManager.flush();

        // Assert
        assertThat(saved.getCouponId()).isNotNull();
        assertThat(saved.getCouponCode()).isEqualTo("COUPON123");
        
        Coupon found = entityManager.find(Coupon.class, saved.getCouponId());
        assertThat(found).isNotNull();
        assertThat(found.getDeal()).isNotNull();
        assertThat(found.getDeal().getDealId()).isEqualTo(testDeal.getDealId());
    }

    @Test
    void findByDeal_Business_BusinessId_WithMultipleDealsSameBusiness_ReturnsAllCoupons() {
        // Arrange
        Deal deal2 = TestDataBuilder.createTestDeal(testBusiness);
        deal2.setDealId(null);
        deal2.setCode("DEAL2025");
        deal2 = entityManager.persistAndFlush(deal2);

        Coupon couponFromDeal2 = TestDataBuilder.createTestCoupon(deal2);
        couponFromDeal2.setCouponId(null);
        couponFromDeal2.setCouponCode("COUPON789");

        entityManager.persist(testCoupon);
        entityManager.persist(couponFromDeal2);
        entityManager.flush();

        // Act
        List<Coupon> coupons = couponRepository.findByDeal_Business_BusinessId(testBusiness.getBusinessId());

        // Assert
        assertThat(coupons).hasSize(2);
        assertThat(coupons).extracting(Coupon::getCouponCode)
                .containsExactlyInAnyOrder("COUPON123", "COUPON789");
    }

    @Test
    void update_WithExistingCoupon_UpdatesFields() {
        // Arrange
        testCoupon = entityManager.persistAndFlush(testCoupon);
        
        // Act
        testCoupon.setRedeemed(true);
        testCoupon.setRedeemDate(java.time.LocalDateTime.now());
        Coupon updated = couponRepository.save(testCoupon);
        entityManager.flush();

        // Assert
        Coupon found = entityManager.find(Coupon.class, updated.getCouponId());
        assertThat(found.getRedeemed()).isTrue();
        assertThat(found.getRedeemDate()).isNotNull();
        assertThat(found.getCouponCode()).isEqualTo("COUPON123");
    }

    @Test
    void deleteById_WithExistingId_DeletesCoupon() {
        // Arrange
        testCoupon = entityManager.persistAndFlush(testCoupon);
        Long couponId = testCoupon.getCouponId();

        // Act
        couponRepository.deleteById(couponId);
        entityManager.flush();

        // Assert
        Coupon found = entityManager.find(Coupon.class, couponId);
        assertThat(found).isNull();
    }
}
