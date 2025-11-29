package za.co.datedeals.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.datedeals.api.dtos.BusinessResponseDto;
import za.co.datedeals.api.dtos.PageResponse;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.services.BusinessService;

import java.util.List;

@RestController
@RequestMapping("/business")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Business> createBusiness(@RequestBody Business business) {
        try {
            Business createdBusiness = businessService.createBusiness(business);
            return ResponseEntity.ok(createdBusiness);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BusinessResponseDto>> getAllBusinesses() {
        try {
            List<BusinessResponseDto> businesses = businessService.getAllBusinesses();
            return ResponseEntity.ok(businesses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<BusinessResponseDto>> getAllBusinessesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "businessId") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<BusinessResponseDto> businessPage = businessService.getAllBusinessesPaginated(pageable);
            
            PageResponse<BusinessResponseDto> response = new PageResponse<>(
                    businessPage.getContent(),
                    businessPage.getNumber(),
                    businessPage.getSize(),
                    businessPage.getTotalElements(),
                    businessPage.getTotalPages(),
                    businessPage.isLast(),
                    businessPage.isFirst()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Business> getBusinessById(@PathVariable Long id) {
        try {
            Business business = businessService.getBusinessById(id);
            return ResponseEntity.ok(business);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Business> updateBusiness(@PathVariable Long id, @RequestBody Business businessDetails) {
        try {
            Business updatedBusiness = businessService.updateBusiness(id, businessDetails);
            return ResponseEntity.ok(updatedBusiness);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBusiness(@PathVariable Long id) {
        try {
            businessService.deleteBusiness(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
