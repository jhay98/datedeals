package za.co.datedeals.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import za.co.datedeals.api.entities.user.User;
import za.co.datedeals.api.entities.user.UserRepository;
import za.co.datedeals.api.utils.TestDataBuilder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;
    private User testAdmin;
    private User disabledUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createTestUser();
        testUser.setRole(User.UserRole.BUSINESS);
        testUser.setEnabled(true);

        testAdmin = TestDataBuilder.createTestAdmin();
        testAdmin.setRole(User.UserRole.ADMIN);
        testAdmin.setEnabled(true);

        disabledUser = TestDataBuilder.createTestUser();
        disabledUser.setUserId(3L);
        disabledUser.setUsername("disableduser");
        disabledUser.setEnabled(false);
    }

    @Test
    void loadUserByUsername_WithExistingBusinessUser_ReturnsUserDetails() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo(testUser.getPassword());
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_BUSINESS");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void loadUserByUsername_WithExistingAdminUser_ReturnsUserDetailsWithAdminRole() {
        // Arrange
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_WithDisabledUser_ReturnsDisabledUserDetails() {
        // Arrange
        when(userRepository.findByUsername("disableduser")).thenReturn(Optional.of(disabledUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("disableduser");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("disableduser");
        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_WithNonExistentUser_ThrowsUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with username: nonexistent");
    }

    @Test
    void loadUserByUsername_AddsRolePrefixToAuthority() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertThat(userDetails.getAuthorities())
                .hasSize(1)
                .allMatch(auth -> auth.getAuthority().startsWith("ROLE_"));
    }
}
