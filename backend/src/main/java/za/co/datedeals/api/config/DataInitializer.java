package za.co.datedeals.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.business.BusinessRepository;
import za.co.datedeals.api.entities.coupon.Coupon;
import za.co.datedeals.api.entities.coupon.CouponRepository;
import za.co.datedeals.api.entities.deal.Deal;
import za.co.datedeals.api.entities.deal.DealRepository;
import za.co.datedeals.api.entities.user.User;
import za.co.datedeals.api.entities.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.UserRole.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println("Admin user created: username=admin, password=admin123");
        }

        // Create multiple businesses with deals and coupons
        List<Business> businesses = new ArrayList<>();
        
        // Business 1: Wild X Adventures
        if (!businessRepository.existsByBusinessName("Wild X Adventures")) {
            Business wildX = new Business();
            wildX.setBusinessName("Wild X Adventures");
            wildX.setContactEmail("info@wildx.co.za");
            wildX.setContactPhone("021-555-1001");
            wildX.setAddress("Atlantis Dunes, Cape Town");
            wildX.setDescription("Experience the thrill of quad biking through the stunning Atlantis Dunes");
            wildX = businessRepository.save(wildX);
            businesses.add(wildX);

            // Create business user
            if (!userRepository.existsByUsername("wildx")) {
                User businessUser = new User();
                businessUser.setUsername("wildx");
                businessUser.setPassword(passwordEncoder.encode("wildx123"));
                businessUser.setRole(User.UserRole.BUSINESS);
                businessUser.setBusiness(wildX);
                businessUser.setEnabled(true);
                userRepository.save(businessUser);
                System.out.println("Business user created: username=wildx, password=wildx123");
            }

            // Create deals for Wild X
            Deal quadDeal = new Deal();
            quadDeal.setCode("QUAD2024");
            quadDeal.setTitle("Quad Biking Experience in Atlantis Dunes - For Two");
            quadDeal.setHtmlVoucherTemplate("<h1>Quad Biking Adventure</h1><p>Enjoy an exciting quad biking experience for 2 people</p>");
            quadDeal.setExpiryDate(LocalDateTime.now().plusMonths(6));
            quadDeal.setLifetimeDays(90);
            quadDeal.setCommissionPercentage(15.0);
            quadDeal.setBusiness(wildX);
            quadDeal = dealRepository.save(quadDeal);

            // Create coupons for this deal
            createCoupons(quadDeal, 5, 299.0, 599.0);
        }

        // Business 2: Ocean Breeze Restaurant
        if (!businessRepository.existsByBusinessName("Ocean Breeze Restaurant")) {
            Business oceanBreeze = new Business();
            oceanBreeze.setBusinessName("Ocean Breeze Restaurant");
            oceanBreeze.setContactEmail("reservations@oceanbreeze.co.za");
            oceanBreeze.setContactPhone("021-555-2002");
            oceanBreeze.setAddress("15 Beach Road, Camps Bay, Cape Town");
            oceanBreeze.setDescription("Fine dining with spectacular ocean views");
            oceanBreeze = businessRepository.save(oceanBreeze);
            businesses.add(oceanBreeze);

            if (!userRepository.existsByUsername("oceanbreeze")) {
                User businessUser = new User();
                businessUser.setUsername("oceanbreeze");
                businessUser.setPassword(passwordEncoder.encode("ocean123"));
                businessUser.setRole(User.UserRole.BUSINESS);
                businessUser.setBusiness(oceanBreeze);
                businessUser.setEnabled(true);
                userRepository.save(businessUser);
                System.out.println("Business user created: username=oceanbreeze, password=ocean123");
            }

            // Create deals
            Deal dinnerDeal = new Deal();
            dinnerDeal.setCode("DINNER2024");
            dinnerDeal.setTitle("3-Course Dinner for Two with Wine Pairing");
            dinnerDeal.setHtmlVoucherTemplate("<h1>Ocean Breeze Dinner</h1><p>Romantic 3-course dinner for 2 with wine pairing</p>");
            dinnerDeal.setExpiryDate(LocalDateTime.now().plusMonths(3));
            dinnerDeal.setLifetimeDays(60);
            dinnerDeal.setCommissionPercentage(20.0);
            dinnerDeal.setBusiness(oceanBreeze);
            dinnerDeal = dealRepository.save(dinnerDeal);
            createCoupons(dinnerDeal, 8, 450.0, 950.0);

            Deal brunchDeal = new Deal();
            brunchDeal.setCode("BRUNCH2024");
            brunchDeal.setTitle("Sunday Brunch Buffet for Two");
            brunchDeal.setHtmlVoucherTemplate("<h1>Sunday Brunch</h1><p>All-you-can-eat brunch buffet for 2</p>");
            brunchDeal.setExpiryDate(LocalDateTime.now().plusMonths(2));
            brunchDeal.setLifetimeDays(30);
            brunchDeal.setCommissionPercentage(18.0);
            brunchDeal.setBusiness(oceanBreeze);
            brunchDeal = dealRepository.save(brunchDeal);
            createCoupons(brunchDeal, 6, 180.0, 380.0);
        }

        // Business 3: Serenity Spa
        if (!businessRepository.existsByBusinessName("Serenity Spa")) {
            Business spa = new Business();
            spa.setBusinessName("Serenity Spa");
            spa.setContactEmail("bookings@serenityspa.co.za");
            spa.setContactPhone("021-555-3003");
            spa.setAddress("88 Kloof Street, Gardens, Cape Town");
            spa.setDescription("Luxury spa treatments in a tranquil setting");
            spa = businessRepository.save(spa);
            businesses.add(spa);

            if (!userRepository.existsByUsername("serenityspa")) {
                User businessUser = new User();
                businessUser.setUsername("serenityspa");
                businessUser.setPassword(passwordEncoder.encode("spa123"));
                businessUser.setRole(User.UserRole.BUSINESS);
                businessUser.setBusiness(spa);
                businessUser.setEnabled(true);
                userRepository.save(businessUser);
                System.out.println("Business user created: username=serenityspa, password=spa123");
            }

            Deal coupleMassage = new Deal();
            coupleMassage.setCode("MASSAGE2024");
            coupleMassage.setTitle("Couples Massage Package - 90 Minutes");
            coupleMassage.setHtmlVoucherTemplate("<h1>Couples Massage</h1><p>Relaxing 90-minute massage for 2 people</p>");
            coupleMassage.setExpiryDate(LocalDateTime.now().plusMonths(4));
            coupleMassage.setLifetimeDays(60);
            coupleMassage.setCommissionPercentage(22.0);
            coupleMassage.setBusiness(spa);
            coupleMassage = dealRepository.save(coupleMassage);
            createCoupons(coupleMassage, 7, 550.0, 1200.0);

            Deal dayPackage = new Deal();
            dayPackage.setCode("SPADAY2024");
            dayPackage.setTitle("Full Day Spa Package with Lunch");
            dayPackage.setHtmlVoucherTemplate("<h1>Spa Day Package</h1><p>Full day spa experience with treatments and lunch</p>");
            dayPackage.setExpiryDate(LocalDateTime.now().plusMonths(5));
            dayPackage.setLifetimeDays(90);
            dayPackage.setCommissionPercentage(25.0);
            dayPackage.setBusiness(spa);
            dayPackage = dealRepository.save(dayPackage);
            createCoupons(dayPackage, 4, 890.0, 1800.0);
        }

        // Business 4: Adventure Skydiving
        if (!businessRepository.existsByBusinessName("Adventure Skydiving")) {
            Business skydiving = new Business();
            skydiving.setBusinessName("Adventure Skydiving");
            skydiving.setContactEmail("jump@adventureskydiving.co.za");
            skydiving.setContactPhone("021-555-4004");
            skydiving.setAddress("Cape Town International Airport, Cape Town");
            skydiving.setDescription("Experience the ultimate adrenaline rush with tandem skydiving");
            skydiving = businessRepository.save(skydiving);
            businesses.add(skydiving);

            if (!userRepository.existsByUsername("skydiving")) {
                User businessUser = new User();
                businessUser.setUsername("skydiving");
                businessUser.setPassword(passwordEncoder.encode("jump123"));
                businessUser.setRole(User.UserRole.BUSINESS);
                businessUser.setBusiness(skydiving);
                businessUser.setEnabled(true);
                userRepository.save(businessUser);
                System.out.println("Business user created: username=skydiving, password=jump123");
            }

            Deal tandemJump = new Deal();
            tandemJump.setCode("SKYDIVE2024");
            tandemJump.setTitle("Tandem Skydive Experience with Video Package");
            tandemJump.setHtmlVoucherTemplate("<h1>Skydiving Adventure</h1><p>Tandem skydive from 10,000 feet with video</p>");
            tandemJump.setExpiryDate(LocalDateTime.now().plusMonths(12));
            tandemJump.setLifetimeDays(180);
            tandemJump.setCommissionPercentage(10.0);
            tandemJump.setBusiness(skydiving);
            tandemJump = dealRepository.save(tandemJump);
            createCoupons(tandemJump, 3, 1200.0, 2500.0);
        }

        // Business 5: Wine Valley Tours
        if (!businessRepository.existsByBusinessName("Wine Valley Tours")) {
            Business wineTours = new Business();
            wineTours.setBusinessName("Wine Valley Tours");
            wineTours.setContactEmail("tours@winevalley.co.za");
            wineTours.setContactPhone("021-555-5005");
            wineTours.setAddress("Stellenbosch, Western Cape");
            wineTours.setDescription("Guided wine tasting tours through the beautiful Cape Winelands");
            wineTours = businessRepository.save(wineTours);
            businesses.add(wineTours);

            if (!userRepository.existsByUsername("winetours")) {
                User businessUser = new User();
                businessUser.setUsername("winetours");
                businessUser.setPassword(passwordEncoder.encode("wine123"));
                businessUser.setRole(User.UserRole.BUSINESS);
                businessUser.setBusiness(wineTours);
                businessUser.setEnabled(true);
                userRepository.save(businessUser);
                System.out.println("Business user created: username=winetours, password=wine123");
            }

            Deal fullDayTour = new Deal();
            fullDayTour.setCode("WINETOUR2024");
            fullDayTour.setTitle("Full Day Wine Tasting Tour for Two");
            fullDayTour.setHtmlVoucherTemplate("<h1>Wine Valley Tour</h1><p>Visit 4 wine estates with tastings and lunch included</p>");
            fullDayTour.setExpiryDate(LocalDateTime.now().plusMonths(6));
            fullDayTour.setLifetimeDays(90);
            fullDayTour.setCommissionPercentage(15.0);
            fullDayTour.setBusiness(wineTours);
            fullDayTour = dealRepository.save(fullDayTour);
            createCoupons(fullDayTour, 10, 650.0, 1400.0);
        }

        // Create some standalone coupons without deals (from Shopify orders with missing SKUs)
        createStandaloneCoupons(5);

        System.out.println("=== Data Initialization Complete ===");
        System.out.println("Total Businesses: " + businessRepository.count());
        System.out.println("Total Deals: " + dealRepository.count());
        System.out.println("Total Coupons: " + couponRepository.count());
        System.out.println("Total Users: " + userRepository.count());
    }

    private void createCoupons(Deal deal, int count, Double purchasePrice, Double valuePrice) {
        for (int i = 0; i < count; i++) {
            Coupon coupon = new Coupon();
            coupon.setCouponCode(generateCouponCode());
            coupon.setPurchasePrice(purchasePrice);
            coupon.setValuePrice(valuePrice);
            coupon.setIssueDate(LocalDateTime.now().minusDays((long) (Math.random() * 30)));
            coupon.setDeal(deal);
            coupon.setRedeemed(i % 4 == 0); // Make 25% redeemed
            
            if (coupon.getRedeemed()) {
                coupon.setRedeemDate(LocalDateTime.now().minusDays((long) (Math.random() * 10)));
            }
            
            if (deal.getExpiryDate() != null) {
                coupon.setExpireDate(deal.getExpiryDate());
            } else if (deal.getLifetimeDays() != null) {
                coupon.setExpireDate(coupon.getIssueDate().plusDays(deal.getLifetimeDays()));
            }
            
            couponRepository.save(coupon);
        }
    }

    private void createStandaloneCoupons(int count) {
        for (int i = 0; i < count; i++) {
            Coupon coupon = new Coupon();
            coupon.setCouponCode(generateCouponCode());
            coupon.setPurchasePrice(100.0 + (Math.random() * 500));
            coupon.setIssueDate(LocalDateTime.now().minusDays((long) (Math.random() * 20)));
            coupon.setDeal(null); // No deal linked
            coupon.setRedeemed(false);
            couponRepository.save(coupon);
        }
    }

    private String generateCouponCode() {
        return "CP-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
