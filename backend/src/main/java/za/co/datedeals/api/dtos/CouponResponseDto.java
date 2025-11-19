package za.co.datedeals.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponseDto {
    private Long couponId;
    private String couponCode;
    private Double purchasePrice;
    private Double valuePrice;
    private LocalDateTime issueDate;
    private LocalDateTime redeemDate;
    private LocalDateTime expireDate;
    private Boolean redeemed;
    private String dealTitle;
    private Long dealId;
    private String businessName;
    private Long businessId;
}
