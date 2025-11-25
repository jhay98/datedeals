package za.co.datedeals.api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import za.co.datedeals.api.entities.coupon.Coupon;
import za.co.datedeals.api.entities.coupon.CouponRepository;
import za.co.datedeals.api.entities.deal.Deal;
import za.co.datedeals.api.entities.deal.DealRepository;
import za.co.datedeals.api.entities.user.User;
import za.co.datedeals.api.entities.user.UserRepository;

@Service
public class AuthorizationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private DealRepository dealRepository;

    public boolean canAccessBusiness(Long businessId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Admin can access any business
        if (user.getRole() == User.UserRole.ADMIN) {
            return true;
        }

        // Business user can only access their own business
        if (user.getRole() == User.UserRole.BUSINESS && user.getBusiness() != null) {
            return user.getBusiness().getBusinessId().equals(businessId);
        }

        return false;
    }

    public boolean canAccessCoupon(Long couponId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Admin can access any coupon
        if (user.getRole() == User.UserRole.ADMIN) {
            return true;
        }

        // Business user can only access coupons for their business
        if (user.getRole() == User.UserRole.BUSINESS && user.getBusiness() != null) {
            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new RuntimeException("Coupon not found"));
            return coupon.getDeal() != null && 
                   coupon.getDeal().getBusiness() != null && 
                   coupon.getDeal().getBusiness().getBusinessId().equals(user.getBusiness().getBusinessId());
        }

        return false;
    }

    public boolean canAccessDeal(Long dealId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Admin can access any deal
        if (user.getRole() == User.UserRole.ADMIN) {
            return true;
        }

        // Business user can only access deals for their business
        if (user.getRole() == User.UserRole.BUSINESS && user.getBusiness() != null) {
            Deal deal = dealRepository.findById(dealId)
                    .orElseThrow(() -> new RuntimeException("Deal not found"));
            return deal.getBusiness().getBusinessId().equals(user.getBusiness().getBusinessId());
        }

        return false;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
