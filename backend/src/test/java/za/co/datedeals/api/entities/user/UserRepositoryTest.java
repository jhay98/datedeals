package za.co.datedeals.api.entities.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.utils.TestDataBuilder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Business testBusiness;

    @BeforeEach
    void setUp() {
        testBusiness = TestDataBuilder.createTestBusiness();
        testBusiness.setBusinessId(null); // Let JPA generate ID
        testBusiness = entityManager.persistAndFlush(testBusiness);

        testUser = TestDataBuilder.createTestUser();
        testUser.setUserId(null); // Let JPA generate ID
        testUser.setBusiness(testBusiness);
    }

    @Test
    void findByUsername_WithExistingUsername_ReturnsUser() {
        // Arrange
        testUser = entityManager.persistAndFlush(testUser);

        // Act
        Optional<User> found = userRepository.findByUsername("testuser");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getUserId()).isEqualTo(testUser.getUserId());
    }

    @Test
    void findByUsername_WithNonExistentUsername_ReturnsEmpty() {
        // Act
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void existsByUsername_WithExistingUsername_ReturnsTrue() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        boolean exists = userRepository.existsByUsername("testuser");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_WithNonExistentUsername_ReturnsFalse() {
        // Act
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void save_WithValidUser_PersistsUser() {
        // Act
        User saved = userRepository.save(testUser);
        entityManager.flush();

        // Assert
        assertThat(saved.getUserId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("testuser");
        
        User found = entityManager.find(User.class, saved.getUserId());
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("testuser");
    }

    @Test
    void findById_WithExistingId_ReturnsUser() {
        // Arrange
        testUser = entityManager.persistAndFlush(testUser);

        // Act
        Optional<User> found = userRepository.findById(testUser.getUserId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void deleteById_WithExistingId_DeletesUser() {
        // Arrange
        testUser = entityManager.persistAndFlush(testUser);
        Long userId = testUser.getUserId();

        // Act
        userRepository.deleteById(userId);
        entityManager.flush();

        // Assert
        User found = entityManager.find(User.class, userId);
        assertThat(found).isNull();
    }

    @Test
    void findByUsername_WithUserHavingBusiness_ReturnsUserWithBusiness() {
        // Arrange
        testUser = entityManager.persistAndFlush(testUser);

        // Act
        Optional<User> found = userRepository.findByUsername("testuser");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getBusiness()).isNotNull();
        assertThat(found.get().getBusiness().getBusinessId()).isEqualTo(testBusiness.getBusinessId());
    }
}
