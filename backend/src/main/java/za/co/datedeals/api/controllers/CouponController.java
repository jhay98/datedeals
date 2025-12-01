package za.co.datedeals.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.datedeals.api.dtos.CouponResponseDto;
import za.co.datedeals.api.dtos.PageResponse;
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

    @GetMapping("/business/{businessId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS')")
    public ResponseEntity<PageResponse<CouponResponseDto>> getCouponsByBusinessPaginated(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "couponId") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        try {
            if (!authorizationService.canAccessBusiness(businessId)) {
                return ResponseEntity.status(403).build();
            }
            Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<CouponResponseDto> couponPage = couponService.getCouponsByBusinessIdPaginated(businessId, pageable);
            
            PageResponse<CouponResponseDto> response = new PageResponse<>(
                    couponPage.getContent(),
                    couponPage.getNumber(),
                    couponPage.getSize(),
                    couponPage.getTotalElements(),
                    couponPage.getTotalPages(),
                    couponPage.isLast(),
                    couponPage.isFirst()
            );
            
            return ResponseEntity.ok(response);
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

    @GetMapping("/code/{couponCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS')")
    public ResponseEntity<CouponResponseDto> getCouponByCode(@PathVariable String couponCode) {
        try {
            if (!authorizationService.canAccessCouponByCode(couponCode)) {
                return ResponseEntity.status(403).build();
            }
            CouponResponseDto coupon = couponService.getCouponByCode(couponCode);
            return ResponseEntity.ok(coupon);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        }
    }

    @PostMapping("/code/{couponCode}/redeem")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS')")
    public ResponseEntity<CouponResponseDto> redeemCouponByCode(@PathVariable String couponCode) {
        try {
            if (!authorizationService.canAccessCouponByCode(couponCode)) {
                return ResponseEntity.status(403).build();
            }
            CouponResponseDto coupon = couponService.redeemCouponByCode(couponCode);
            return ResponseEntity.ok(coupon);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
