package za.co.datedeals.api.entities.coupon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.co.datedeals.api.dtos.CouponResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Override
    public List<CouponResponseDto> getCouponsByBusinessId(Long businessId) {
        return couponRepository.findByBusiness_BusinessId(businessId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CouponResponseDto redeemCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (coupon.getRedeemed()) {
            throw new RuntimeException("Coupon has already been redeemed");
        }

        if (coupon.getExpireDate() != null && coupon.getExpireDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Coupon has expired");
        }

        coupon.setRedeemed(true);
        coupon.setRedeemDate(LocalDateTime.now());
        coupon = couponRepository.save(coupon);

        return convertToDto(coupon);
    }

    @Override
    public Coupon getCouponById(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
    }

    private CouponResponseDto convertToDto(Coupon coupon) {
        return new CouponResponseDto(
                coupon.getCouponId(),
                coupon.getCouponCode(),
                coupon.getPurchasePrice(),
                coupon.getValuePrice(),
                coupon.getIssueDate(),
                coupon.getRedeemDate(),
                coupon.getExpireDate(),
                coupon.getRedeemed(),
                coupon.getDeal() != null ? coupon.getDeal().getTitle() : null,
                coupon.getDeal() != null ? coupon.getDeal().getDealId() : null,
                coupon.getBusiness() != null ? coupon.getBusiness().getBusinessName() : null,
                coupon.getBusiness() != null ? coupon.getBusiness().getBusinessId() : null
        );
    }
}