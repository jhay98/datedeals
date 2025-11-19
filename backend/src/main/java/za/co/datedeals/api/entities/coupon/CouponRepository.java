package za.co.datedeals.api.entities.coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.datedeals.api.entities.business.Business;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findByBusiness_BusinessId(Long businessId);
    
    List<Coupon> findByBusinessAndRedeemedFalse(Business business);
    
    List<Coupon> findByBusiness_BusinessIdAndRedeemedFalse(Long businessId);
    
    Optional<Coupon> findByCouponCode(String couponCode);
}