package za.co.datedeals.api.entities.deal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.coupon.Coupon;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@JsonIgnoreProperties({"coupons"})
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
    
    @Column(nullable = false, unique = true)
    private String code;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String htmlVoucherTemplate;
    
    private LocalDateTime expiryDate;
    private Integer lifetimeDays;
    private Double commissionPercentage;
    
    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
    
    @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL)
    private List<Coupon> coupons;
}