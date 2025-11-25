package za.co.datedeals.api.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import za.co.datedeals.api.dtos.LoginRequestDto;
import za.co.datedeals.api.dtos.LoginResponseDto;
import za.co.datedeals.api.entities.business.Business;
import za.co.datedeals.api.entities.user.User;
import za.co.datedeals.api.entities.user.UserRepository;
import za.co.datedeals.api.security.JwtTokenProvider;
import za.co.datedeals.api.utils.TestDataBuilder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private LoginRequestDto loginRequest;
    private User testUser;
    private Business testBusiness;

    @BeforeEach
    void setUp() {
        loginRequest = TestDataBuilder.createLoginRequest();
        testBusiness = TestDataBuilder.createTestBusiness();
        testUser = TestDataBuilder.createUserWithBusiness(testBusiness);
    }

    @Test
    void login_WithValidCredentials_ReturnsLoginResponse() {
        // Arrange
        String expectedToken = "test.jwt.token";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn(expectedToken);
        when(userRepository.findByUsername(loginRequest.getUsername()))
                .thenReturn(Optional.of(testUser));

        // Act
        LoginResponseDto response = authService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(expectedToken);
        assertThat(response.getRole()).isEqualTo(User.UserRole.BUSINESS.name());
        assertThat(response.getBusinessId()).isEqualTo(testBusiness.getBusinessId());
        assertThat(response.getBusinessName()).isEqualTo(testBusiness.getBusinessName());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(authentication);
        verify(userRepository).findByUsername(loginRequest.getUsername());
    }

    @Test
    void login_WithUserWithoutBusiness_ReturnsLoginResponseWithNullBusiness() {
        // Arrange
        String expectedToken = "test.jwt.token";
        User userWithoutBusiness = TestDataBuilder.createTestUser();
        userWithoutBusiness.setBusiness(null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn(expectedToken);
        when(userRepository.findByUsername(loginRequest.getUsername()))
                .thenReturn(Optional.of(userWithoutBusiness));

        // Act
        LoginResponseDto response = authService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(expectedToken);
        assertThat(response.getBusinessId()).isNull();
        assertThat(response.getBusinessName()).isNull();
    }

    @Test
    void login_WithNonExistentUser_ThrowsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("token");
        when(userRepository.findByUsername(loginRequest.getUsername()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername(loginRequest.getUsername());
    }

    @Test
    void login_WithInvalidCredentials_ThrowsAuthenticationException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Bad credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider, never()).generateToken(any());
        verify(userRepository, never()).findByUsername(anyString());
    }
}
