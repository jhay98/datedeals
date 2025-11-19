package za.co.datedeals.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.datedeals.api.dtos.BusinessResponseDto;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.services.BusinessService;

import java.util.List;

@RestController
@RequestMapping("/business")
@CrossOrigin(origins = "http://localhost:4200")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

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
}
