package za.co.datedeals.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.datedeals.api.dtos.CouponResponseDto;
import za.co.datedeals.api.security.AuthorizationService;
import za.co.datedeals.api.services.CouponService;

import java.util.List;

@RestController
@RequestMapping("/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS')")
    public ResponseEntity<List<CouponResponseDto>> getCouponsByBusiness(@PathVariable Long businessId) {
        try {
            if (!authorizationService.canAccessBusiness(businessId)) {
                return ResponseEntity.status(403).build();
            }
            List<CouponResponseDto> coupons = couponService.getCouponsByBusinessId(businessId);
            return ResponseEntity.ok(coupons);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/deal/{dealId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS')")
    public ResponseEntity<List<CouponResponseDto>> getCouponsByDeal(@PathVariable Long dealId) {
        try {
            if (!authorizationService.canAccessDeal(dealId)) {
                return ResponseEntity.status(403).build();
            }
            List<CouponResponseDto> coupons = couponService.getCouponsByDealId(dealId);
            return ResponseEntity.ok(coupons);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{couponId}/redeem")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS')")
    public ResponseEntity<CouponResponseDto> redeemCoupon(@PathVariable Long couponId) {
        try {
            if (!authorizationService.canAccessCoupon(couponId)) {
                return ResponseEntity.status(403).build();
            }
            CouponResponseDto coupon = couponService.redeemCoupon(couponId);
            return ResponseEntity.ok(coupon);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
