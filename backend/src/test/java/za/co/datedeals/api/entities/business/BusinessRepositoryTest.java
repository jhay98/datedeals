package za.co.datedeals.api.entities.business;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import za.co.datedeals.api.utils.TestDataBuilder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class BusinessRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BusinessRepository businessRepository;

    private Business testBusiness;

    @BeforeEach
    void setUp() {
        testBusiness = TestDataBuilder.createTestBusiness();
        testBusiness.setBusinessId(null); // Let JPA generate ID
    }

    @Test
    void findByBusinessName_WithExistingName_ReturnsBusiness() {
        // Arrange
        testBusiness = entityManager.persistAndFlush(testBusiness);

        // Act
        Optional<Business> found = businessRepository.findByBusinessName("Test Restaurant");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getBusinessName()).isEqualTo("Test Restaurant");
        assertThat(found.get().getBusinessId()).isEqualTo(testBusiness.getBusinessId());
    }

    @Test
    void findByBusinessName_WithNonExistentName_ReturnsEmpty() {
        // Act
        Optional<Business> found = businessRepository.findByBusinessName("Nonexistent");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void existsByBusinessName_WithExistingName_ReturnsTrue() {
        // Arrange
        entityManager.persistAndFlush(testBusiness);

        // Act
        boolean exists = businessRepository.existsByBusinessName("Test Restaurant");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByBusinessName_WithNonExistentName_ReturnsFalse() {
        // Act
        boolean exists = businessRepository.existsByBusinessName("Nonexistent");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void save_WithValidBusiness_PersistsBusiness() {
        // Act
        Business saved = businessRepository.save(testBusiness);
        entityManager.flush();

        // Assert
        assertThat(saved.getBusinessId()).isNotNull();
        assertThat(saved.getBusinessName()).isEqualTo("Test Restaurant");
        
        Business found = entityManager.find(Business.class, saved.getBusinessId());
        assertThat(found).isNotNull();
        assertThat(found.getContactEmail()).isEqualTo("contact@testrestaurant.com");
    }

    @Test
    void findAll_ReturnsAllBusinesses() {
        // Arrange
        Business business2 = TestDataBuilder.createTestBusinessWithoutId();
        entityManager.persist(testBusiness);
        entityManager.persist(business2);
        entityManager.flush();

        // Act
        var businesses = businessRepository.findAll();

        // Assert
        assertThat(businesses).hasSize(2);
        assertThat(businesses)
                .extracting(Business::getBusinessName)
                .containsExactlyInAnyOrder("Test Restaurant", "New Restaurant");
    }

    @Test
    void update_WithExistingBusiness_UpdatesFields() {
        // Arrange
        testBusiness = entityManager.persistAndFlush(testBusiness);
        
        // Act
        testBusiness.setContactEmail("newemail@test.com");
        testBusiness.setContactPhone("9999999999");
        Business updated = businessRepository.save(testBusiness);
        entityManager.flush();

        // Assert
        Business found = entityManager.find(Business.class, updated.getBusinessId());
        assertThat(found.getContactEmail()).isEqualTo("newemail@test.com");
        assertThat(found.getContactPhone()).isEqualTo("9999999999");
        assertThat(found.getBusinessName()).isEqualTo("Test Restaurant");
    }

    @Test
    void deleteById_WithExistingId_DeletesBusiness() {
        // Arrange
        testBusiness = entityManager.persistAndFlush(testBusiness);
        Long businessId = testBusiness.getBusinessId();

        // Act
        businessRepository.deleteById(businessId);
        entityManager.flush();

        // Assert
        Business found = entityManager.find(Business.class, businessId);
        assertThat(found).isNull();
    }
}
