package za.co.datedeals.api.entities.coupon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.datedeals.api.dtos.CouponResponseDto;

import java.util.List;

@RestController
@RequestMapping("/coupon")
@CrossOrigin(origins = "http://localhost:4200")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS')")
    public ResponseEntity<List<CouponResponseDto>> getCouponsByBusiness(@PathVariable Long businessId) {
        try {
            List<CouponResponseDto> coupons = couponService.getCouponsByBusinessId(businessId);
            return ResponseEntity.ok(coupons);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{couponId}/redeem")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS')")
    public ResponseEntity<CouponResponseDto> redeemCoupon(@PathVariable Long couponId) {
        try {
            CouponResponseDto coupon = couponService.redeemCoupon(couponId);
            return ResponseEntity.ok(coupon);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}