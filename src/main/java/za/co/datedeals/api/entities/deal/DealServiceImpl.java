package za.co.datedeals.api.entities.deal;

import org.springframework.stereotype.Service;

@Service
public class DealServiceImpl implements DealService {

    private final DealRepository dealRepository;

    public DealServiceImpl(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    @Override
    public Deal createDeal(Deal deal) {
        deal.setCode(generateCode());
        return dealRepository.save(deal);
    }

    private String generateCode() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

}