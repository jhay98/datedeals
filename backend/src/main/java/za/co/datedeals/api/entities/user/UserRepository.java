package za.co.datedeals.api.entities.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    List<User> findByBusiness_BusinessId(Long businessId);
    
    Page<User> findByBusiness_BusinessId(Long businessId, Pageable pageable);
}
