package za.co.datedeals.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import za.co.datedeals.api.dtos.UserRequestDto;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.business.BusinessRepository;
import za.co.datedeals.api.entities.user.User;
import za.co.datedeals.api.entities.user.UserRepository;
import za.co.datedeals.api.utils.TestDataBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Business testBusiness;

    @BeforeEach
    void setUp() {
        testBusiness = TestDataBuilder.createTestBusiness();
        testUser = TestDataBuilder.createTestUser();
    }

    @Test
    void createUser_WithValidData_ReturnsCreatedUser() {
        // Arrange
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setUsername("newuser");
        userRequest.setPassword("rawPassword");
        userRequest.setRole(User.UserRole.BUSINESS);
        userRequest.setBusinessId(1L);
        userRequest.setEnabled(true);

        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("encodedPassword");
        newUser.setRole(User.UserRole.BUSINESS);
        newUser.setBusiness(testBusiness);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(businessRepository.findById(1L)).thenReturn(Optional.of(testBusiness));
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        User result = userService.createUser(userRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("rawPassword");
        verify(businessRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithDuplicateUsername_ThrowsException() {
        // Arrange
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setUsername("existinguser");
        userRequest.setPassword("password");
        userRequest.setRole(User.UserRole.BUSINESS);

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(userRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Username already exists");

        verify(userRepository).existsByUsername("existinguser");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_WithExistingId_ReturnsUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(1L);
        assertThat(result.get().getUsername()).isEqualTo(testUser.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_WithNonExistentId_ReturnsEmpty() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserById(999L);

        // Assert
        assertThat(result).isEmpty();
        verify(userRepository).findById(999L);
    }

    @Test
    void getUserByUsername_WithExistingUsername_ReturnsUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserByUsername("testuser");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getUserByUsername_WithNonExistentUsername_ReturnsEmpty() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserByUsername("nonexistent");

        // Assert
        assertThat(result).isEmpty();
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void getAllUsers_ReturnsAllUsers() {
        // Arrange
        User user2 = TestDataBuilder.createTestAdmin();
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(testUser, user2);
        verify(userRepository).findAll();
    }

    @Test
    void updateUser_WithValidData_ReturnsUpdatedUser() {
        // Arrange
        User updateDetails = new User();
        updateDetails.setUsername("updateduser");
        updateDetails.setPassword("newPassword");
        updateDetails.setRole(User.UserRole.ADMIN);
        updateDetails.setEnabled(false);
        updateDetails.setBusiness(testBusiness);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(1L, updateDetails);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_WithNullPassword_DoesNotEncodePassword() {
        // Arrange
        User updateDetails = new User();
        updateDetails.setUsername("updateduser");
        updateDetails.setPassword(null);
        updateDetails.setRole(User.UserRole.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(1L, updateDetails);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_WithEmptyPassword_DoesNotEncodePassword() {
        // Arrange
        User updateDetails = new User();
        updateDetails.setUsername("updateduser");
        updateDetails.setPassword("");
        updateDetails.setRole(User.UserRole.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(1L, updateDetails);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).findById(1L);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_WithNonExistentId_ThrowsException() {
        // Arrange
        User updateDetails = new User();
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(999L, updateDetails))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_WithExistingId_DeletesUser() {
        // Arrange
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).deleteById(1L);
    }
}
