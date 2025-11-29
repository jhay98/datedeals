package za.co.datedeals.api.entities.deal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
    List<Deal> findByBusiness_BusinessId(Long businessId);
    
    Page<Deal> findByBusiness_BusinessId(Long businessId, Pageable pageable);
    
    Optional<Deal> findByCode(String code);
    
    boolean existsByCode(String code);
}