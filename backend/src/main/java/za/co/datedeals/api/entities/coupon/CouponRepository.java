package za.co.datedeals.api.entities.coupon;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findByDeal_Business_BusinessId(Long businessId);
    
    Page<Coupon> findByDeal_Business_BusinessId(Long businessId, Pageable pageable);
    
    List<Coupon> findByDeal_Business_BusinessIdAndRedeemedFalse(Long businessId);
    
    List<Coupon> findByDeal_DealId(Long dealId);
    
    Optional<Coupon> findByCouponCode(String couponCode);
}