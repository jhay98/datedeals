package za.co.datedeals.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import za.co.datedeals.api.dtos.CouponResponseDto;
import za.co.datedeals.api.entities.coupon.Coupon;
import za.co.datedeals.api.entities.coupon.CouponRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    public List<CouponResponseDto> getCouponsByBusinessId(Long businessId) {
        return couponRepository.findByDeal_Business_BusinessId(businessId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<CouponResponseDto> getCouponsByBusinessIdPaginated(Long businessId, Pageable pageable) {
        return couponRepository.findByDeal_Business_BusinessId(businessId, pageable)
                .map(this::convertToDto);
    }

    public List<CouponResponseDto> getCouponsByDealId(Long dealId) {
        return couponRepository.findByDeal_DealId(dealId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

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
                coupon.getDeal() != null && coupon.getDeal().getBusiness() != null ? coupon.getDeal().getBusiness().getBusinessName() : null,
                coupon.getDeal() != null && coupon.getDeal().getBusiness() != null ? coupon.getDeal().getBusiness().getBusinessId() : null
        );
    }
}
