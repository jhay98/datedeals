package za.co.datedeals.api.utils;

import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.coupon.Coupon;
import za.co.datedeals.api.entities.deal.Deal;
import za.co.datedeals.api.entities.user.User;
import za.co.datedeals.api.dtos.LoginRequestDto;
import za.co.datedeals.api.dtos.DealRequestDto;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Utility class for building test data objects
 */
public class TestDataBuilder {

    public static User createTestUser() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword123");
        user.setRole(User.UserRole.BUSINESS);
        user.setEnabled(true);
        return user;
    }

    public static User createTestAdmin() {
        User admin = new User();
        admin.setUserId(2L);
        admin.setUsername("admin");
        admin.setPassword("encodedAdminPass");
        admin.setRole(User.UserRole.ADMIN);
        admin.setEnabled(true);
        return admin;
    }

    public static Business createTestBusiness() {
        Business business = new Business();
        business.setBusinessId(1L);
        business.setBusinessName("Test Restaurant");
        business.setContactEmail("contact@testrestaurant.com");
        business.setContactPhone("0123456789");
        business.setAddress("123 Test Street, Test City");
        business.setDescription("A test restaurant for testing purposes");
        business.setDeals(new ArrayList<>());
        return business;
    }

    public static Business createTestBusinessWithoutId() {
        Business business = new Business();
        business.setBusinessName("New Restaurant");
        business.setContactEmail("new@restaurant.com");
        business.setContactPhone("0987654321");
        business.setAddress("456 New Street, New City");
        business.setDescription("A new restaurant");
        return business;
    }

    public static Deal createTestDeal(Business business) {
        Deal deal = new Deal();
        deal.setDealId(1L);
        deal.setCode("DEAL2024");
        deal.setTitle("50% Off Dinner");
        deal.setHtmlVoucherTemplate("<html><body>50% Off</body></html>");
        deal.setExpiryDate(LocalDateTime.now().plusMonths(3));
        deal.setLifetimeDays(30);
        deal.setCommissionPercentage(10.0);
        deal.setBusiness(business);
        return deal;
    }

    public static DealRequestDto createTestDealRequestDto(Long businessId) {
        DealRequestDto dto = new DealRequestDto();
        dto.setBusinessId(businessId);
        dto.setCode("NEWDEAL");
        dto.setTitle("New Deal Title");
        dto.setHtmlVoucherTemplate("<html><body>New Deal</body></html>");
        dto.setExpiryDate(LocalDateTime.now().plusMonths(6));
        dto.setLifetimeDays(60);
        dto.setCommissionPercentage(15.0);
        return dto;
    }

    public static Coupon createTestCoupon(Deal deal) {
        Coupon coupon = new Coupon();
        coupon.setCouponId(1L);
        coupon.setCouponCode("COUPON123");
        coupon.setPurchasePrice(50.0);
        coupon.setValuePrice(100.0);
        coupon.setIssueDate(LocalDateTime.now().minusDays(5));
        coupon.setExpireDate(LocalDateTime.now().plusMonths(1));
        coupon.setRedeemed(false);
        coupon.setDeal(deal);
        return coupon;
    }

    public static Coupon createExpiredCoupon(Deal deal) {
        Coupon coupon = new Coupon();
        coupon.setCouponId(2L);
        coupon.setCouponCode("EXPIRED123");
        coupon.setPurchasePrice(50.0);
        coupon.setValuePrice(100.0);
        coupon.setIssueDate(LocalDateTime.now().minusMonths(3));
        coupon.setExpireDate(LocalDateTime.now().minusDays(1));
        coupon.setRedeemed(false);
        coupon.setDeal(deal);
        return coupon;
    }

    public static Coupon createRedeemedCoupon(Deal deal) {
        Coupon coupon = new Coupon();
        coupon.setCouponId(3L);
        coupon.setCouponCode("REDEEMED123");
        coupon.setPurchasePrice(50.0);
        coupon.setValuePrice(100.0);
        coupon.setIssueDate(LocalDateTime.now().minusMonths(2));
        coupon.setExpireDate(LocalDateTime.now().plusMonths(1));
        coupon.setRedeemed(true);
        coupon.setRedeemDate(LocalDateTime.now().minusDays(10));
        coupon.setDeal(deal);
        return coupon;
    }

    public static LoginRequestDto createLoginRequest() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setUsername("testuser");
        dto.setPassword("password123");
        return dto;
    }

    public static User createUserWithBusiness(Business business) {
        User user = createTestUser();
        user.setBusiness(business);
        return user;
    }
}
