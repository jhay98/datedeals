package za.co.datedeals.api.entities.deal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deals")
public class DealController {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PostMapping
    public ResponseEntity<Deal> addDeal(@RequestBody Deal deal) {
        Deal created = dealService.createDeal(deal);
        return ResponseEntity.status(201).body(created);
    }

}