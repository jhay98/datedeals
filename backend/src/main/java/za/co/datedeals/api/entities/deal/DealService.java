package za.co.datedeals.api.entities.deal;

import za.co.datedeals.api.dtos.DealRequestDto;

import java.util.List;

public interface DealService {
    Deal createDeal(DealRequestDto dealRequestDto);
    
    Deal getDealById(Long id);
    
    List<Deal> getAllDeals();
    
    List<Deal> getDealsByBusinessId(Long businessId);
}