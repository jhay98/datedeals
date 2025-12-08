package za.co.datedeals.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();

        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();
    }

    @Test
    void doFilterInternal_WithValidToken_SetsAuthentication() throws ServletException, IOException {
        // Arrange
        String token = "valid-jwt-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token)).thenReturn("testuser");
        when(customUserDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
        assertThat(authentication.getName()).isEqualTo("testuser");
        assertThat(authentication.getAuthorities())
                .hasSize(1)
                .anySatisfy(auth -> assertThat(auth.getAuthority()).isEqualTo("ROLE_ADMIN"));

        verify(filterChain).doFilter(request, response);
        verify(tokenProvider).validateToken(token);
        verify(tokenProvider).getUsernameFromToken(token);
        verify(customUserDetailsService).loadUserByUsername("testuser");
    }

    @Test
    void doFilterInternal_WithoutToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange - No Authorization header

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
        verify(tokenProvider, never()).validateToken(any());
        verify(customUserDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void doFilterInternal_WithInvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        String token = "invalid-jwt-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenProvider.validateToken(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
        verify(tokenProvider).validateToken(token);
        verify(tokenProvider, never()).getUsernameFromToken(any());
        verify(customUserDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void doFilterInternal_WithMalformedAuthHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "InvalidFormat token");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
        verify(tokenProvider, never()).validateToken(any());
    }

    @Test
    void doFilterInternal_WithEmptyBearerToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
        verify(tokenProvider, never()).validateToken(any());
    }

    @Test
    void doFilterInternal_WithOnlyBearerKeyword_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithEmptyAuthHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
        verify(tokenProvider, never()).validateToken(any());
    }

    @Test
    void doFilterInternal_WithLowercaseBearer_DoesNotExtractToken() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "bearer valid-token");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
        verify(tokenProvider, never()).validateToken(any());
    }

    @Test
    void doFilterInternal_WhenTokenProviderThrowsException_ContinuesFilterChain() throws ServletException, IOException {
        // Arrange
        String token = "error-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenProvider.validateToken(token)).thenThrow(new RuntimeException("Token validation error"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenUserDetailsServiceThrowsException_ContinuesFilterChain() throws ServletException, IOException {
        // Arrange
        String token = "valid-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token)).thenReturn("testuser");
        when(customUserDetailsService.loadUserByUsername("testuser"))
                .thenThrow(new RuntimeException("User not found"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithValidTokenForDifferentUser_LoadsCorrectUser() throws ServletException, IOException {
        // Arrange
        String token = "valid-token-user2";
        request.addHeader("Authorization", "Bearer " + token);

        UserDetails user2Details = User.builder()
                .username("user2")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token)).thenReturn("user2");
        when(customUserDetailsService.loadUserByUsername("user2")).thenReturn(user2Details);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("user2");
        assertThat(authentication.getAuthorities())
                .hasSize(1)
                .anySatisfy(auth -> assertThat(auth.getAuthority()).isEqualTo("ROLE_USER"));

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithTokenContainingSpecialCharacters_ProcessesCorrectly() throws ServletException, IOException {
        // Arrange
        String token = "token-with-special.chars_123";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token)).thenReturn("testuser");
        when(customUserDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithVeryLongToken_ProcessesCorrectly() throws ServletException, IOException {
        // Arrange
        String longToken = "a".repeat(500);
        request.addHeader("Authorization", "Bearer " + longToken);

        when(tokenProvider.validateToken(longToken)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(longToken)).thenReturn("testuser");
        when(customUserDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_AlwaysCallsFilterChain_EvenOnError() throws ServletException, IOException {
        // Arrange
        String token = "error-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(tokenProvider.validateToken(token)).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - Filter chain should always be called
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithMultipleAuthHeaders_UsesFirst() throws ServletException, IOException {
        // Arrange
        String token1 = "first-token";
        String token2 = "second-token";
        request.addHeader("Authorization", "Bearer " + token1);
        request.addHeader("Authorization", "Bearer " + token2);

        when(tokenProvider.validateToken(token1)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token1)).thenReturn("testuser");
        when(customUserDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider).validateToken(token1);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithWhitespaceInToken_TrimsAndProcesses() throws ServletException, IOException {
        // Arrange
        String token = "valid-token";
        request.addHeader("Authorization", "Bearer " + token + "  ");

        when(tokenProvider.validateToken(token + "  ")).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token + "  ")).thenReturn("testuser");
        when(customUserDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }
}
