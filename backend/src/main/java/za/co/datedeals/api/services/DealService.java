package za.co.datedeals.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import za.co.datedeals.api.dtos.DealRequestDto;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.business.BusinessRepository;
import za.co.datedeals.api.entities.deal.Deal;
import za.co.datedeals.api.entities.deal.DealRepository;

import java.util.List;

@Service
public class DealService {

    private final DealRepository dealRepository;

    @Autowired
    private BusinessRepository businessRepository;

    public DealService(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

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

    public Deal getDealById(Long id) {
        return dealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deal not found"));
    }

    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    public List<Deal> getDealsByBusinessId(Long businessId) {
        return dealRepository.findByBusiness_BusinessId(businessId);
    }

    public Page<Deal> getDealsByBusinessIdPaginated(Long businessId, Pageable pageable) {
        return dealRepository.findByBusiness_BusinessId(businessId, pageable);
    }

    public Deal updateDeal(Long id, DealRequestDto dealRequestDto) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deal not found"));

        if (dealRequestDto.getBusinessId() != null) {
            Business business = businessRepository.findById(dealRequestDto.getBusinessId())
                    .orElseThrow(() -> new RuntimeException("Business not found"));
            deal.setBusiness(business);
        }

        if (dealRequestDto.getCode() != null) {
            deal.setCode(dealRequestDto.getCode());
        }
        if (dealRequestDto.getTitle() != null) {
            deal.setTitle(dealRequestDto.getTitle());
        }
        if (dealRequestDto.getHtmlVoucherTemplate() != null) {
            deal.setHtmlVoucherTemplate(dealRequestDto.getHtmlVoucherTemplate());
        }
        if (dealRequestDto.getExpiryDate() != null) {
            deal.setExpiryDate(dealRequestDto.getExpiryDate());
        }
        if (dealRequestDto.getLifetimeDays() != null) {
            deal.setLifetimeDays(dealRequestDto.getLifetimeDays());
        }
        if (dealRequestDto.getCommissionPercentage() != null) {
            deal.setCommissionPercentage(dealRequestDto.getCommissionPercentage());
        }

        return dealRepository.save(deal);
    }

    public void deleteDeal(Long id) {
        dealRepository.deleteById(id);
    }

    private String generateCode() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
