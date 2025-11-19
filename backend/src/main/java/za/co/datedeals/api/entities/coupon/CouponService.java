package za.co.datedeals.api.entities.coupon;

import za.co.datedeals.api.dtos.CouponResponseDto;

import java.util.List;

public interface CouponService {
    List<CouponResponseDto> getCouponsByBusinessId(Long businessId);
    
    CouponResponseDto redeemCoupon(Long couponId);
    
    Coupon getCouponById(Long couponId);
}