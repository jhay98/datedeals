package za.co.datedeals.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import za.co.datedeals.api.dtos.BusinessResponseDto;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.business.BusinessRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusinessService {

    @Autowired
    private BusinessRepository businessRepository;

    public Business createBusiness(Business business) {
        if (businessRepository.existsByBusinessName(business.getBusinessName())) {
            throw new RuntimeException("Business name already exists");
        }
        return businessRepository.save(business);
    }

    public Business getBusinessById(Long id) {
        return businessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));
    }

    public List<BusinessResponseDto> getAllBusinesses() {
        return businessRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<BusinessResponseDto> getAllBusinessesPaginated(Pageable pageable) {
        return businessRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public Business updateBusiness(Long id, Business businessDetails) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        business.setBusinessName(businessDetails.getBusinessName());
        business.setContactEmail(businessDetails.getContactEmail());
        business.setContactPhone(businessDetails.getContactPhone());
        business.setAddress(businessDetails.getAddress());
        business.setDescription(businessDetails.getDescription());

        return businessRepository.save(business);
    }

    public void deleteBusiness(Long id) {
        businessRepository.deleteById(id);
    }

    private BusinessResponseDto convertToDto(Business business) {
        return new BusinessResponseDto(
                business.getBusinessId(),
                business.getBusinessName(),
                business.getContactEmail(),
                business.getContactPhone(),
                business.getAddress(),
                business.getDescription()
        );
    }
}
