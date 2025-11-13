package za.co.datedeals.api.entities.coupon;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Data
public class Coupon {
    @Id
    @SequenceGenerator(
            name = "coupon_seq",
            sequenceName = "customer_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "coupon_seq"
    )
    private Long couponId;
    private String couponCode;
    private Double purchasePrice;
    private Double valuePrice;
    private LocalDateTime issueDate;
    private LocalDateTime redeemDate;
    private LocalDateTime expireDate;

}