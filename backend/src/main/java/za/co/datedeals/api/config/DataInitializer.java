package za.co.datedeals.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.business.BusinessRepository;
import za.co.datedeals.api.entities.user.User;
import za.co.datedeals.api.entities.user.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

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

        // Create test business if not exists
        if (!businessRepository.existsByBusinessName("Test Restaurant")) {
            Business business = new Business();
            business.setBusinessName("Test Restaurant");
            business.setContactEmail("test@restaurant.com");
            business.setContactPhone("0123456789");
            business.setAddress("123 Test Street, Cape Town");
            business.setDescription("A test restaurant for development");
            business = businessRepository.save(business);

            // Create business user
            if (!userRepository.existsByUsername("testbusiness")) {
                User businessUser = new User();
                businessUser.setUsername("testbusiness");
                businessUser.setPassword(passwordEncoder.encode("business123"));
                businessUser.setRole(User.UserRole.BUSINESS);
                businessUser.setBusiness(business);
                businessUser.setEnabled(true);
                userRepository.save(businessUser);
                System.out.println("Business user created: username=testbusiness, password=business123");
            }
        }
    }
}
