package za.co.datedeals.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import za.co.datedeals.api.dtos.BusinessResponseDto;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.security.JwtAuthenticationFilter;
import za.co.datedeals.api.security.JwtTokenProvider;
import za.co.datedeals.api.services.BusinessService;
import za.co.datedeals.api.utils.TestDataBuilder;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(BusinessController.class)
@AutoConfigureMockMvc(addFilters = false)
class BusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BusinessService businessService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Business testBusiness;
    private BusinessResponseDto businessResponseDto;

    @BeforeEach
    void setUp() {
        testBusiness = TestDataBuilder.createTestBusiness();
        businessResponseDto = new BusinessResponseDto(
                testBusiness.getBusinessId(),
                testBusiness.getBusinessName(),
                testBusiness.getContactEmail(),
                testBusiness.getContactPhone(),
                testBusiness.getAddress(),
                testBusiness.getDescription()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBusiness_WithValidData_ReturnsOk() throws Exception {
        // Arrange
        when(businessService.createBusiness(any(Business.class))).thenReturn(testBusiness);

        // Act & Assert
        mockMvc.perform(post("/business")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBusiness)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessId").value(testBusiness.getBusinessId()))
                .andExpect(jsonPath("$.businessName").value(testBusiness.getBusinessName()));
    }



    @Test
    @WithMockUser(roles = "ADMIN")
    void createBusiness_WithDuplicateName_ReturnsBadRequest() throws Exception {
        // Arrange
        when(businessService.createBusiness(any(Business.class)))
                .thenThrow(new RuntimeException("Business name already exists"));

        // Act & Assert
        mockMvc.perform(post("/business")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBusiness)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllBusinesses_ReturnsListOfBusinesses() throws Exception {
        // Arrange
        List<BusinessResponseDto> businesses = Arrays.asList(businessResponseDto);
        when(businessService.getAllBusinesses()).thenReturn(businesses);

        // Act & Assert
        mockMvc.perform(get("/business/all")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].businessId").value(testBusiness.getBusinessId()))
                .andExpect(jsonPath("$[0].businessName").value(testBusiness.getBusinessName()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBusinessById_WithExistingId_ReturnsOk() throws Exception {
        // Arrange
        when(businessService.getBusinessById(1L)).thenReturn(testBusiness);

        // Act & Assert
        mockMvc.perform(get("/business/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessId").value(testBusiness.getBusinessId()))
                .andExpect(jsonPath("$.businessName").value(testBusiness.getBusinessName()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBusinessById_WithNonExistentId_ReturnsNotFound() throws Exception {
        // Arrange
        when(businessService.getBusinessById(999L))
                .thenThrow(new RuntimeException("Business not found"));

        // Act & Assert
        mockMvc.perform(get("/business/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateBusiness_WithValidData_ReturnsOk() throws Exception {
        // Arrange
        when(businessService.updateBusiness(eq(1L), any(Business.class))).thenReturn(testBusiness);

        // Act & Assert
        mockMvc.perform(put("/business/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBusiness)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessId").value(testBusiness.getBusinessId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateBusiness_WithNonExistentId_ReturnsBadRequest() throws Exception {
        // Arrange
        when(businessService.updateBusiness(eq(999L), any(Business.class)))
                .thenThrow(new RuntimeException("Business not found"));

        // Act & Assert
        mockMvc.perform(put("/business/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBusiness)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteBusiness_WithExistingId_ReturnsOk() throws Exception {
        // Arrange
        doNothing().when(businessService).deleteBusiness(1L);

        // Act & Assert
        mockMvc.perform(delete("/business/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
