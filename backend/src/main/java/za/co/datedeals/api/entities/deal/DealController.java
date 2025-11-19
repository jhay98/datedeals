package za.co.datedeals.api.entities.deal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.co.datedeals.api.dtos.DealRequestDto;

@RestController
@RequestMapping("/deal")
@CrossOrigin(origins = "http://localhost:4200")
public class DealController {

    private static final Logger logger = LoggerFactory.getLogger(DealController.class);

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Deal> addDeal(@RequestBody DealRequestDto dealRequestDto) {
        try {
            Deal deal = dealService.createDeal(dealRequestDto);
            return ResponseEntity.ok(deal);
        } catch (Exception e) {
            logger.error("Error creating deal", e);
            return ResponseEntity.badRequest().build();
        }
    }
}