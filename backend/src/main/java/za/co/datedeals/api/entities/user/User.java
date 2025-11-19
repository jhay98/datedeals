package za.co.datedeals.api.entities.user;

import jakarta.persistence.*;
import lombok.Data;
import za.co.datedeals.api.entities.business.Business;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @SequenceGenerator(
            name = "user_seq",
            sequenceName = "user_seq",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_seq"
    )
    private Long userId;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    @ManyToOne
    @JoinColumn(name = "business_id")
    private Business business;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    public enum UserRole {
        ADMIN,
        BUSINESS
    }
}
