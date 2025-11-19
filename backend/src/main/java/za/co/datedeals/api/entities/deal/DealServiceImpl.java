package za.co.datedeals.api.entities.deal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.co.datedeals.api.dtos.DealRequestDto;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.business.BusinessRepository;

import java.util.List;

@Service
public class DealServiceImpl implements DealService {

    private final DealRepository dealRepository;

    @Autowired
    private BusinessRepository businessRepository;

    public DealServiceImpl(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    @Override
    public Deal createDeal(DealRequestDto dealRequestDto) {
        Business business = businessRepository.findById(dealRequestDto.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found"));

        Deal deal = new Deal();
        deal.setCode(dealRequestDto.getCode() != null ? dealRequestDto.getCode() : generateCode());
        deal.setTitle(dealRequestDto.getTitle());
        deal.setHtmlVoucherTemplate(dealRequestDto.getHtmlVoucherTemplate());
        deal.setExpiryDate(dealRequestDto.getExpiryDate());
        deal.setLifetimeDays(dealRequestDto.getLifetimeDays());
        deal.setCommissionPercentage(dealRequestDto.getCommissionPercentage());
        deal.setBusiness(business);

        return dealRepository.save(deal);
    }

    @Override
    public Deal getDealById(Long id) {
        return dealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
    }

    @Override
    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    @Override
    public List<Deal> getDealsByBusinessId(Long businessId) {
        return dealRepository.findByBusiness_BusinessId(businessId);
    }

    private String generateCode() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

}