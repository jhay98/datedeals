package za.co.datedeals.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.datedeals.api.dtos.BusinessResponseDto;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.business.BusinessRepository;
import za.co.datedeals.api.utils.TestDataBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private BusinessService businessService;

    private Business testBusiness;
    private Business newBusiness;

    @BeforeEach
    void setUp() {
        testBusiness = TestDataBuilder.createTestBusiness();
        newBusiness = TestDataBuilder.createTestBusinessWithoutId();
    }

    @Test
    void createBusiness_WithValidData_ReturnsCreatedBusiness() {
        // Arrange
        when(businessRepository.existsByBusinessName(newBusiness.getBusinessName())).thenReturn(false);
        when(businessRepository.save(any(Business.class))).thenReturn(newBusiness);

        // Act
        Business result = businessService.createBusiness(newBusiness);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getBusinessName()).isEqualTo(newBusiness.getBusinessName());
        verify(businessRepository).existsByBusinessName(newBusiness.getBusinessName());
        verify(businessRepository).save(newBusiness);
    }

    @Test
    void createBusiness_WithDuplicateName_ThrowsException() {
        // Arrange
        when(businessRepository.existsByBusinessName(newBusiness.getBusinessName())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> businessService.createBusiness(newBusiness))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Business name already exists");

        verify(businessRepository).existsByBusinessName(newBusiness.getBusinessName());
        verify(businessRepository, never()).save(any());
    }

    @Test
    void getBusinessById_WithExistingId_ReturnsBusiness() {
        // Arrange
        when(businessRepository.findById(1L)).thenReturn(Optional.of(testBusiness));

        // Act
        Business result = businessService.getBusinessById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getBusinessId()).isEqualTo(1L);
        assertThat(result.getBusinessName()).isEqualTo(testBusiness.getBusinessName());
        verify(businessRepository).findById(1L);
    }

    @Test
    void getBusinessById_WithNonExistentId_ThrowsException() {
        // Arrange
        when(businessRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> businessService.getBusinessById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Business not found");

        verify(businessRepository).findById(999L);
    }

    @Test
    void getAllBusinesses_ReturnsAllBusinesses() {
        // Arrange
        Business business2 = TestDataBuilder.createTestBusinessWithoutId();
        business2.setBusinessId(2L);
        when(businessRepository.findAll()).thenReturn(Arrays.asList(testBusiness, business2));

        // Act
        List<BusinessResponseDto> result = businessService.getAllBusinesses();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBusinessId()).isEqualTo(testBusiness.getBusinessId());
        assertThat(result.get(0).getBusinessName()).isEqualTo(testBusiness.getBusinessName());
        assertThat(result.get(1).getBusinessId()).isEqualTo(business2.getBusinessId());
        verify(businessRepository).findAll();
    }

    @Test
    void updateBusiness_WithValidData_ReturnsUpdatedBusiness() {
        // Arrange
        Business updatedDetails = new Business();
        updatedDetails.setBusinessName("Updated Restaurant");
        updatedDetails.setContactEmail("updated@email.com");
        updatedDetails.setContactPhone("1111111111");
        updatedDetails.setAddress("New Address");
        updatedDetails.setDescription("New Description");

        when(businessRepository.findById(1L)).thenReturn(Optional.of(testBusiness));
        when(businessRepository.save(any(Business.class))).thenReturn(testBusiness);

        // Act
        Business result = businessService.updateBusiness(1L, updatedDetails);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getBusinessName()).isEqualTo("Updated Restaurant");
        assertThat(result.getContactEmail()).isEqualTo("updated@email.com");
        verify(businessRepository).findById(1L);
        verify(businessRepository).save(testBusiness);
    }

    @Test
    void updateBusiness_WithNonExistentId_ThrowsException() {
        // Arrange
        when(businessRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> businessService.updateBusiness(999L, newBusiness))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Business not found");

        verify(businessRepository).findById(999L);
        verify(businessRepository, never()).save(any());
    }

    @Test
    void deleteBusiness_WithExistingId_DeletesBusiness() {
        // Arrange
        doNothing().when(businessRepository).deleteById(1L);

        // Act
        businessService.deleteBusiness(1L);

        // Assert
        verify(businessRepository).deleteById(1L);
    }
}
