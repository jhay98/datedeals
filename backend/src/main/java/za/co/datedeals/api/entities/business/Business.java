package za.co.datedeals.api.entities.business;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import za.co.datedeals.api.entities.coupon.Coupon;
import za.co.datedeals.api.entities.deal.Deal;

import java.util.List;

@Entity
@Data
@JsonIgnoreProperties({"deals", "coupons"})
public class Business {
    @Id
    @SequenceGenerator(
            name = "business_seq",
            sequenceName = "business_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "business_seq"
    )
    private Long businessId;
    
    @Column(nullable = false, unique = true)
    private String businessName;
    
    private String contactEmail;
    private String contactPhone;
    private String address;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    private List<Deal> deals;
    
    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    private List<Coupon> coupons;
}
