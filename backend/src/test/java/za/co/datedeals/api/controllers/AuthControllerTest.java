package za.co.datedeals.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import za.co.datedeals.api.dtos.LoginRequestDto;
import za.co.datedeals.api.dtos.LoginResponseDto;
import za.co.datedeals.api.entities.user.User;
import za.co.datedeals.api.security.JwtAuthenticationFilter;
import za.co.datedeals.api.security.JwtTokenProvider;
import za.co.datedeals.api.services.AuthService;
import za.co.datedeals.api.utils.TestDataBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private LoginRequestDto loginRequest;
    private LoginResponseDto loginResponse;

    @BeforeEach
    void setUp() {
        loginRequest = TestDataBuilder.createLoginRequest();
        loginResponse = new LoginResponseDto(
                "test.jwt.token",
                User.UserRole.BUSINESS.name(),
                1L,
                "Test Restaurant"
        );
    }

    @Test
    @WithMockUser
    void login_WithValidCredentials_ReturnsOkWithToken() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequestDto.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test.jwt.token"))
                .andExpect(jsonPath("$.role").value("BUSINESS"))
                .andExpect(jsonPath("$.businessId").value(1))
                .andExpect(jsonPath("$.businessName").value("Test Restaurant"));
    }

    @Test
    @WithMockUser
    void login_WithInvalidCredentials_ReturnsBadRequest() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }


}
