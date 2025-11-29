package za.co.datedeals.api.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.datedeals.api.dtos.DealRequestDto;
import za.co.datedeals.api.dtos.PageResponse;
import za.co.datedeals.api.entities.deal.Deal;
import za.co.datedeals.api.security.AuthorizationService;
import za.co.datedeals.api.services.DealService;

import java.util.List;

@RestController
@RequestMapping("/deal")
public class DealController {

    private static final Logger logger = LoggerFactory.getLogger(DealController.class);

    private final DealService dealService;

    @Autowired
    private AuthorizationService authorizationService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS')")
    public ResponseEntity<Deal> addDeal(@RequestBody DealRequestDto dealRequestDto) {
        try {
            if (!authorizationService.canAccessBusiness(dealRequestDto.getBusinessId())) {
                return ResponseEntity.status(403).build();
            }
            Deal deal = dealService.createDeal(dealRequestDto);
            return ResponseEntity.ok(deal);
        } catch (Exception e) {
            logger.error("Error creating deal", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Deal>> getAllDeals() {
        try {
            List<Deal> deals = dealService.getAllDeals();
            return ResponseEntity.ok(deals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS')")
    public ResponseEntity<Deal> getDealById(@PathVariable Long id) {
        try {
            Deal deal = dealService.getDealById(id);
            if (!authorizationService.canAccessDeal(id)) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.ok(deal);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS')")
    public ResponseEntity<List<Deal>> getDealsByBusinessId(@PathVariable Long businessId) {
        try {
            if (!authorizationService.canAccessBusiness(businessId)) {
                return ResponseEntity.status(403).build();
            }
            List<Deal> deals = dealService.getDealsByBusinessId(businessId);
            return ResponseEntity.ok(deals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/business/{businessId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS')")
    public ResponseEntity<PageResponse<Deal>> getDealsByBusinessIdPaginated(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dealId") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        try {
            if (!authorizationService.canAccessBusiness(businessId)) {
                return ResponseEntity.status(403).build();
            }
            Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<Deal> dealPage = dealService.getDealsByBusinessIdPaginated(businessId, pageable);
            
            PageResponse<Deal> response = new PageResponse<>(
                    dealPage.getContent(),
                    dealPage.getNumber(),
                    dealPage.getSize(),
                    dealPage.getTotalElements(),
                    dealPage.getTotalPages(),
                    dealPage.isLast(),
                    dealPage.isFirst()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting paginated deals", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS')")
    public ResponseEntity<Deal> updateDeal(@PathVariable Long id, @RequestBody DealRequestDto dealRequestDto) {
        try {
            if (!authorizationService.canAccessDeal(id)) {
                return ResponseEntity.status(403).build();
            }
            Deal updatedDeal = dealService.updateDeal(id, dealRequestDto);
            return ResponseEntity.ok(updatedDeal);
        } catch (RuntimeException e) {
            logger.error("Error updating deal", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS')")
    public ResponseEntity<Void> deleteDeal(@PathVariable Long id) {
        try {
            if (!authorizationService.canAccessDeal(id)) {
                return ResponseEntity.status(403).build();
            }
            dealService.deleteDeal(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
