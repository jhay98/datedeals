package za.co.datedeals.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import za.co.datedeals.api.dtos.CouponResponseDto;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.coupon.Coupon;
import za.co.datedeals.api.entities.deal.Deal;
import za.co.datedeals.api.security.AuthorizationService;
import za.co.datedeals.api.security.JwtAuthenticationFilter;
import za.co.datedeals.api.security.JwtTokenProvider;
import za.co.datedeals.api.services.CouponService;
import za.co.datedeals.api.utils.TestDataBuilder;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(CouponController.class)
@AutoConfigureMockMvc(addFilters = false)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CouponService couponService;

    @MockBean
    private AuthorizationService authorizationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Business testBusiness;
    private Deal testDeal;
    private Coupon testCoupon;
    private CouponResponseDto couponResponseDto;

    @BeforeEach
    void setUp() {
        testBusiness = TestDataBuilder.createTestBusiness();
        testDeal = TestDataBuilder.createTestDeal(testBusiness);
        testCoupon = TestDataBuilder.createTestCoupon(testDeal);
        
        couponResponseDto = new CouponResponseDto(
                testCoupon.getCouponId(),
                testCoupon.getCouponCode(),
                testCoupon.getPurchasePrice(),
                testCoupon.getValuePrice(),
                testCoupon.getIssueDate(),
                testCoupon.getRedeemDate(),
                testCoupon.getExpireDate(),
                testCoupon.getRedeemed(),
                testDeal.getTitle(),
                testDeal.getDealId(),
                testBusiness.getBusinessName(),
                testBusiness.getBusinessId()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCouponsByBusiness_WithAuthorizedAccess_ReturnsCoupons() throws Exception {
        // Arrange
        when(authorizationService.canAccessBusiness(testBusiness.getBusinessId())).thenReturn(true);
        when(couponService.getCouponsByBusinessId(testBusiness.getBusinessId()))
                .thenReturn(Arrays.asList(couponResponseDto));

        // Act & Assert
        mockMvc.perform(get("/coupon/business/" + testBusiness.getBusinessId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].couponId").value(testCoupon.getCouponId()))
                .andExpect(jsonPath("$[0].businessName").value(testBusiness.getBusinessName()));
    }

    @Test
    @WithMockUser(roles = "BUSINESS")
    void getCouponsByBusiness_WithUnauthorizedAccess_ReturnsForbidden() throws Exception {
        // Arrange
        when(authorizationService.canAccessBusiness(testBusiness.getBusinessId())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/coupon/business/" + testBusiness.getBusinessId())
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(couponService, never()).getCouponsByBusinessId(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCouponsByDeal_WithAuthorizedAccess_ReturnsCoupons() throws Exception {
        // Arrange
        when(authorizationService.canAccessDeal(testDeal.getDealId())).thenReturn(true);
        when(couponService.getCouponsByDealId(testDeal.getDealId()))
                .thenReturn(Arrays.asList(couponResponseDto));

        // Act & Assert
        mockMvc.perform(get("/coupon/deal/" + testDeal.getDealId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dealId").value(testDeal.getDealId()));
    }

    @Test
    @WithMockUser(roles = "BUSINESS")
    void getCouponsByDeal_WithUnauthorizedAccess_ReturnsForbidden() throws Exception {
        // Arrange
        when(authorizationService.canAccessDeal(testDeal.getDealId())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/coupon/deal/" + testDeal.getDealId())
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(couponService, never()).getCouponsByDealId(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void redeemCoupon_WithValidCoupon_ReturnsRedeemedCoupon() throws Exception {
        // Arrange
        when(authorizationService.canAccessCoupon(testCoupon.getCouponId())).thenReturn(true);
        when(couponService.redeemCoupon(testCoupon.getCouponId())).thenReturn(couponResponseDto);

        // Act & Assert
        mockMvc.perform(post("/coupon/" + testCoupon.getCouponId() + "/redeem")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponId").value(testCoupon.getCouponId()));
    }

    @Test
    @WithMockUser(roles = "BUSINESS")
    void redeemCoupon_WithUnauthorizedAccess_ReturnsForbidden() throws Exception {
        // Arrange
        when(authorizationService.canAccessCoupon(testCoupon.getCouponId())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/coupon/" + testCoupon.getCouponId() + "/redeem")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(couponService, never()).redeemCoupon(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void redeemCoupon_WithAlreadyRedeemedCoupon_ReturnsBadRequest() throws Exception {
        // Arrange
        when(authorizationService.canAccessCoupon(testCoupon.getCouponId())).thenReturn(true);
        when(couponService.redeemCoupon(testCoupon.getCouponId()))
                .thenThrow(new RuntimeException("Coupon has already been redeemed"));

        // Act & Assert
        mockMvc.perform(post("/coupon/" + testCoupon.getCouponId() + "/redeem")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void redeemCoupon_WithExpiredCoupon_ReturnsBadRequest() throws Exception {
        // Arrange
        when(authorizationService.canAccessCoupon(testCoupon.getCouponId())).thenReturn(true);
        when(couponService.redeemCoupon(testCoupon.getCouponId()))
                .thenThrow(new RuntimeException("Coupon has expired"));

        // Act & Assert
        mockMvc.perform(post("/coupon/" + testCoupon.getCouponId() + "/redeem")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
