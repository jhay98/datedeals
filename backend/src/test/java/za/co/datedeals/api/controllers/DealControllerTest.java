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
import za.co.datedeals.api.dtos.DealRequestDto;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.deal.Deal;
import za.co.datedeals.api.security.AuthorizationService;
import za.co.datedeals.api.security.JwtAuthenticationFilter;
import za.co.datedeals.api.security.JwtTokenProvider;
import za.co.datedeals.api.services.DealService;
import za.co.datedeals.api.utils.TestDataBuilder;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(DealController.class)
@AutoConfigureMockMvc(addFilters = false)
class DealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DealService dealService;

    @MockBean
    private AuthorizationService authorizationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Business testBusiness;
    private Deal testDeal;
    private DealRequestDto dealRequestDto;

    @BeforeEach
    void setUp() {
        testBusiness = TestDataBuilder.createTestBusiness();
        testDeal = TestDataBuilder.createTestDeal(testBusiness);
        dealRequestDto = TestDataBuilder.createTestDealRequestDto(testBusiness.getBusinessId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addDeal_WithAdminRole_ReturnsOk() throws Exception {
        // Arrange
        when(authorizationService.canAccessBusiness(testBusiness.getBusinessId())).thenReturn(true);
        when(dealService.createDeal(any(DealRequestDto.class))).thenReturn(testDeal);

        // Act & Assert
        mockMvc.perform(post("/deal")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dealRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dealId").value(testDeal.getDealId()))
                .andExpect(jsonPath("$.title").value(testDeal.getTitle()));
    }

    @Test
    @WithMockUser(roles = "BUSINESS")
    void addDeal_WithBusinessRole_ReturnsOk() throws Exception {
        // Arrange
        when(authorizationService.canAccessBusiness(testBusiness.getBusinessId())).thenReturn(true);
        when(dealService.createDeal(any(DealRequestDto.class))).thenReturn(testDeal);

        // Act & Assert
        mockMvc.perform(post("/deal")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dealRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "BUSINESS")
    void addDeal_WithUnauthorizedBusiness_ReturnsForbidden() throws Exception {
        // Arrange
        when(authorizationService.canAccessBusiness(testBusiness.getBusinessId())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/deal")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dealRequestDto)))
                .andExpect(status().isForbidden());

        verify(dealService, never()).createDeal(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllDeals_WithAdminRole_ReturnsAllDeals() throws Exception {
        // Arrange
        List<Deal> deals = Arrays.asList(testDeal);
        when(dealService.getAllDeals()).thenReturn(deals);

        // Act & Assert
        mockMvc.perform(get("/deal/all")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dealId").value(testDeal.getDealId()));
    }



    @Test
    @WithMockUser(roles = "ADMIN")
    void getDealById_WithExistingId_ReturnsOk() throws Exception {
        // Arrange
        when(dealService.getDealById(1L)).thenReturn(testDeal);
        when(authorizationService.canAccessDeal(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/deal/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dealId").value(testDeal.getDealId()));
    }

    @Test
    @WithMockUser(roles = "BUSINESS")
    void getDealById_WithUnauthorizedAccess_ReturnsForbidden() throws Exception {
        // Arrange
        when(dealService.getDealById(1L)).thenReturn(testDeal);
        when(authorizationService.canAccessDeal(1L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/deal/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDealById_WithNonExistentId_ReturnsNotFound() throws Exception {
        // Arrange
        when(dealService.getDealById(999L)).thenThrow(new RuntimeException("Deal not found"));

        // Act & Assert
        mockMvc.perform(get("/deal/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "BUSINESS")
    void getDealsByBusinessId_WithAuthorizedAccess_ReturnsDeals() throws Exception {
        // Arrange
        when(authorizationService.canAccessBusiness(testBusiness.getBusinessId())).thenReturn(true);
        when(dealService.getDealsByBusinessId(testBusiness.getBusinessId()))
                .thenReturn(Arrays.asList(testDeal));

        // Act & Assert
        mockMvc.perform(get("/deal/business/" + testBusiness.getBusinessId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dealId").value(testDeal.getDealId()));
    }

    @Test
    @WithMockUser(roles = "BUSINESS")
    void getDealsByBusinessId_WithUnauthorizedAccess_ReturnsForbidden() throws Exception {
        // Arrange
        when(authorizationService.canAccessBusiness(testBusiness.getBusinessId())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/deal/business/" + testBusiness.getBusinessId())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDeal_WithValidData_ReturnsOk() throws Exception {
        // Arrange
        when(authorizationService.canAccessDeal(1L)).thenReturn(true);
        when(dealService.updateDeal(eq(1L), any(DealRequestDto.class))).thenReturn(testDeal);

        // Act & Assert
        mockMvc.perform(put("/deal/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dealRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dealId").value(testDeal.getDealId()));
    }

    @Test
    @WithMockUser(roles = "BUSINESS")
    void updateDeal_WithUnauthorizedAccess_ReturnsForbidden() throws Exception {
        // Arrange
        when(authorizationService.canAccessDeal(1L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(put("/deal/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dealRequestDto)))
                .andExpect(status().isForbidden());

        verify(dealService, never()).updateDeal(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDeal_WithExistingId_ReturnsOk() throws Exception {
        // Arrange
        when(authorizationService.canAccessDeal(1L)).thenReturn(true);
        doNothing().when(dealService).deleteDeal(1L);

        // Act & Assert
        mockMvc.perform(delete("/deal/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
