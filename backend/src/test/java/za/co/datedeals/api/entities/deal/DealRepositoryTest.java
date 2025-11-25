package za.co.datedeals.api.entities.deal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.utils.TestDataBuilder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class DealRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DealRepository dealRepository;

    private Business testBusiness;
    private Deal testDeal;

    @BeforeEach
    void setUp() {
        testBusiness = TestDataBuilder.createTestBusiness();
        testBusiness.setBusinessId(null);
        testBusiness = entityManager.persistAndFlush(testBusiness);

        testDeal = TestDataBuilder.createTestDeal(testBusiness);
        testDeal.setDealId(null);
    }

    @Test
    void findByBusiness_BusinessId_WithExistingBusinessId_ReturnsDeals() {
        // Arrange
        Deal deal2 = TestDataBuilder.createTestDeal(testBusiness);
        deal2.setDealId(null);
        deal2.setCode("DEAL2025");
        deal2.setTitle("Another Deal");

        entityManager.persist(testDeal);
        entityManager.persist(deal2);
        entityManager.flush();

        // Act
        List<Deal> deals = dealRepository.findByBusiness_BusinessId(testBusiness.getBusinessId());

        // Assert
        assertThat(deals).hasSize(2);
        assertThat(deals)
                .extracting(Deal::getTitle)
                .containsExactlyInAnyOrder("50% Off Dinner", "Another Deal");
    }

    @Test
    void findByBusiness_BusinessId_WithNonExistentBusinessId_ReturnsEmptyList() {
        // Act
        List<Deal> deals = dealRepository.findByBusiness_BusinessId(999L);

        // Assert
        assertThat(deals).isEmpty();
    }

    @Test
    void findByCode_WithExistingCode_ReturnsDeal() {
        // Arrange
        testDeal = entityManager.persistAndFlush(testDeal);

        // Act
        Optional<Deal> found = dealRepository.findByCode("DEAL2024");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("DEAL2024");
        assertThat(found.get().getTitle()).isEqualTo("50% Off Dinner");
    }

    @Test
    void findByCode_WithNonExistentCode_ReturnsEmpty() {
        // Act
        Optional<Deal> found = dealRepository.findByCode("NONEXISTENT");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void existsByCode_WithExistingCode_ReturnsTrue() {
        // Arrange
        entityManager.persistAndFlush(testDeal);

        // Act
        boolean exists = dealRepository.existsByCode("DEAL2024");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByCode_WithNonExistentCode_ReturnsFalse() {
        // Act
        boolean exists = dealRepository.existsByCode("NONEXISTENT");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void save_WithValidDeal_PersistsDeal() {
        // Act
        Deal saved = dealRepository.save(testDeal);
        entityManager.flush();

        // Assert
        assertThat(saved.getDealId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("DEAL2024");
        
        Deal found = entityManager.find(Deal.class, saved.getDealId());
        assertThat(found).isNotNull();
        assertThat(found.getBusiness()).isNotNull();
        assertThat(found.getBusiness().getBusinessId()).isEqualTo(testBusiness.getBusinessId());
    }

    @Test
    void findByBusiness_BusinessId_WithMultipleBusinesses_ReturnsOnlyMatchingDeals() {
        // Arrange
        Business otherBusiness = TestDataBuilder.createTestBusinessWithoutId();
        otherBusiness = entityManager.persistAndFlush(otherBusiness);

        Deal otherDeal = TestDataBuilder.createTestDeal(otherBusiness);
        otherDeal.setDealId(null);
        otherDeal.setCode("OTHER2024");
        otherDeal.setTitle("Other Business Deal");

        entityManager.persist(testDeal);
        entityManager.persist(otherDeal);
        entityManager.flush();

        // Act
        List<Deal> deals = dealRepository.findByBusiness_BusinessId(testBusiness.getBusinessId());

        // Assert
        assertThat(deals).hasSize(1);
        assertThat(deals.get(0).getTitle()).isEqualTo("50% Off Dinner");
        assertThat(deals.get(0).getBusiness().getBusinessId()).isEqualTo(testBusiness.getBusinessId());
    }

    @Test
    void update_WithExistingDeal_UpdatesFields() {
        // Arrange
        testDeal = entityManager.persistAndFlush(testDeal);
        
        // Act
        testDeal.setTitle("Updated Title");
        testDeal.setCommissionPercentage(25.0);
        Deal updated = dealRepository.save(testDeal);
        entityManager.flush();

        // Assert
        Deal found = entityManager.find(Deal.class, updated.getDealId());
        assertThat(found.getTitle()).isEqualTo("Updated Title");
        assertThat(found.getCommissionPercentage()).isEqualTo(25.0);
        assertThat(found.getCode()).isEqualTo("DEAL2024");
    }

    @Test
    void deleteById_WithExistingId_DeletesDeal() {
        // Arrange
        testDeal = entityManager.persistAndFlush(testDeal);
        Long dealId = testDeal.getDealId();

        // Act
        dealRepository.deleteById(dealId);
        entityManager.flush();

        // Assert
        Deal found = entityManager.find(Deal.class, dealId);
        assertThat(found).isNull();
    }
}
