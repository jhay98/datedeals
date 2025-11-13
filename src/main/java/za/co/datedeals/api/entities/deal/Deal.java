package za.co.datedeals.api.entities.deal;

import jakarta.persistence.*;
import lombok.Data;
import za.co.datedeals.api.entities.coupon.Coupon;

import java.time.LocalDateTime;

@Entity
@Data
public class Deal {
    @Id
    @SequenceGenerator(
            name = "deal_seq",
            sequenceName = "deal_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "deal_seq"
    )
    private Long dealId;
    private String code;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String htmlVoucherTemplate;
    private LocalDateTime expiryDate;
    private Integer lifetimeDays;
    private Double commissionPercentage;
    
    @ManyToOne()
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;
}