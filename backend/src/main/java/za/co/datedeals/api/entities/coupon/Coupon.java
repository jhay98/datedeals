package za.co.datedeals.api.entities.coupon;
import jakarta.persistence.*;
import lombok.Data;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.deal.Deal;

import java.time.LocalDateTime;


@Entity
@Data
public class Coupon {
    @Id
    @SequenceGenerator(
            name = "coupon_seq",
            sequenceName = "coupon_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "coupon_seq"
    )
    private Long couponId;
    
    @Column(nullable = false, unique = true)
    private String couponCode;
    
    private Double purchasePrice;
    private Double valuePrice;
    private LocalDateTime issueDate;
    private LocalDateTime redeemDate;
    private LocalDateTime expireDate;
    
    @ManyToOne
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;
    
    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
    
    private Boolean redeemed = false;

}