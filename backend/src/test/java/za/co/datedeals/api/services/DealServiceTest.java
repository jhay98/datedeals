package za.co.datedeals.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.datedeals.api.dtos.DealRequestDto;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.business.BusinessRepository;
import za.co.datedeals.api.entities.deal.Deal;
import za.co.datedeals.api.entities.deal.DealRepository;
import za.co.datedeals.api.utils.TestDataBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private BusinessRepository businessRepository;

    private DealService dealService;

    private Business testBusiness;
    private Deal testDeal;
    private DealRequestDto dealRequestDto;

    @BeforeEach
    void setUp() {
        dealService = new DealService(dealRepository);
        // Manually inject businessRepository using reflection
        try {
            java.lang.reflect.Field field = DealService.class.getDeclaredField("businessRepository");
            field.setAccessible(true);
            field.set(dealService, businessRepository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        testBusiness = TestDataBuilder.createTestBusiness();
        testDeal = TestDataBuilder.createTestDeal(testBusiness);
        dealRequestDto = TestDataBuilder.createTestDealRequestDto(testBusiness.getBusinessId());
    }

    @Test
    void createDeal_WithValidData_ReturnsCreatedDeal() {
        // Arrange
        when(businessRepository.findById(testBusiness.getBusinessId()))
                .thenReturn(Optional.of(testBusiness));
        when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);

        // Act
        Deal result = dealService.createDeal(dealRequestDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(testDeal.getTitle());
        assertThat(result.getBusiness()).isEqualTo(testBusiness);
        verify(businessRepository).findById(testBusiness.getBusinessId());
        verify(dealRepository).save(any(Deal.class));
    }

    @Test
    void createDeal_WithoutCode_GeneratesCode() {
        // Arrange
        dealRequestDto.setCode(null);
        when(businessRepository.findById(testBusiness.getBusinessId()))
                .thenReturn(Optional.of(testBusiness));
        when(dealRepository.save(any(Deal.class))).thenAnswer(invocation -> {
            Deal savedDeal = invocation.getArgument(0);
            assertThat(savedDeal.getCode()).isNotNull();
            assertThat(savedDeal.getCode()).hasSize(8);
            return savedDeal;
        });

        // Act
        Deal result = dealService.createDeal(dealRequestDto);

        // Assert
        verify(dealRepository).save(any(Deal.class));
    }

    @Test
    void createDeal_WithNonExistentBusiness_ThrowsException() {
        // Arrange
        when(businessRepository.findById(testBusiness.getBusinessId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> dealService.createDeal(dealRequestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Business not found");

        verify(businessRepository).findById(testBusiness.getBusinessId());
        verify(dealRepository, never()).save(any());
    }

    @Test
    void getDealById_WithExistingId_ReturnsDeal() {
        // Arrange
        when(dealRepository.findById(1L)).thenReturn(Optional.of(testDeal));

        // Act
        Deal result = dealService.getDealById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDealId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo(testDeal.getTitle());
        verify(dealRepository).findById(1L);
    }

    @Test
    void getDealById_WithNonExistentId_ThrowsException() {
        // Arrange
        when(dealRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> dealService.getDealById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Deal not found");

        verify(dealRepository).findById(999L);
    }

    @Test
    void getAllDeals_ReturnsAllDeals() {
        // Arrange
        Deal deal2 = TestDataBuilder.createTestDeal(testBusiness);
        deal2.setDealId(2L);
        when(dealRepository.findAll()).thenReturn(Arrays.asList(testDeal, deal2));

        // Act
        List<Deal> result = dealService.getAllDeals();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(testDeal, deal2);
        verify(dealRepository).findAll();
    }

    @Test
    void getDealsByBusinessId_ReturnsBusinessDeals() {
        // Arrange
        Deal deal2 = TestDataBuilder.createTestDeal(testBusiness);
        deal2.setDealId(2L);
        when(dealRepository.findByBusiness_BusinessId(testBusiness.getBusinessId()))
                .thenReturn(Arrays.asList(testDeal, deal2));

        // Act
        List<Deal> result = dealService.getDealsByBusinessId(testBusiness.getBusinessId());

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(deal -> deal.getBusiness().equals(testBusiness));
        verify(dealRepository).findByBusiness_BusinessId(testBusiness.getBusinessId());
    }

    @Test
    void updateDeal_WithValidData_ReturnsUpdatedDeal() {
        // Arrange
        DealRequestDto updateDto = TestDataBuilder.createTestDealRequestDto(testBusiness.getBusinessId());
        updateDto.setTitle("Updated Title");
        updateDto.setCommissionPercentage(20.0);

        when(dealRepository.findById(1L)).thenReturn(Optional.of(testDeal));
        when(businessRepository.findById(testBusiness.getBusinessId()))
                .thenReturn(Optional.of(testBusiness));
        when(dealRepository.save(any(Deal.class))).thenReturn(testDeal);

        // Act
        Deal result = dealService.updateDeal(1L, updateDto);

        // Assert
        assertThat(result).isNotNull();
        verify(dealRepository).findById(1L);
        verify(dealRepository).save(testDeal);
    }

    @Test
    void updateDeal_WithNonExistentId_ThrowsException() {
        // Arrange
        when(dealRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> dealService.updateDeal(999L, dealRequestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Deal not found");

        verify(dealRepository).findById(999L);
        verify(dealRepository, never()).save(any());
    }

    @Test
    void updateDeal_WithNonExistentBusiness_ThrowsException() {
        // Arrange
        dealRequestDto.setBusinessId(999L);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(testDeal));
        when(businessRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> dealService.updateDeal(1L, dealRequestDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Business not found");

        verify(dealRepository).findById(1L);
        verify(businessRepository).findById(999L);
        verify(dealRepository, never()).save(any());
    }

    @Test
    void deleteDeal_WithExistingId_DeletesDeal() {
        // Arrange
        doNothing().when(dealRepository).deleteById(1L);

        // Act
        dealService.deleteDeal(1L);

        // Assert
        verify(dealRepository).deleteById(1L);
    }
}
