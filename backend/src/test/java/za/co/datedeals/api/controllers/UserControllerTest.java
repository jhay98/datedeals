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
import za.co.datedeals.api.dtos.UserRequestDto;
import za.co.datedeals.api.entities.user.User;
import za.co.datedeals.api.security.JwtAuthenticationFilter;
import za.co.datedeals.api.security.JwtTokenProvider;
import za.co.datedeals.api.services.UserService;
import za.co.datedeals.api.utils.TestDataBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User testUser;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createTestUser();
        testAdmin = TestDataBuilder.createTestAdmin();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_WithAdminRole_ReturnsOk() throws Exception {
        // Arrange
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setUsername(testUser.getUsername());
        userRequest.setPassword("password");
        userRequest.setRole(testUser.getRole());
        userRequest.setEnabled(testUser.getEnabled());
        
        when(userService.createUser(any(UserRequestDto.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getUserId()))
                .andExpect(jsonPath("$.username").value(testUser.getUsername()));
    }



    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_WithDuplicateUsername_ReturnsBadRequest() throws Exception {
        // Arrange
        UserRequestDto userRequest = new UserRequestDto();
        userRequest.setUsername(testUser.getUsername());
        userRequest.setPassword("password");
        userRequest.setRole(testUser.getRole());
        
        when(userService.createUser(any(UserRequestDto.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        // Act & Assert
        mockMvc.perform(post("/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ReturnsListOfUsers() throws Exception {
        // Arrange
        List<User> users = Arrays.asList(testUser, testAdmin);
        when(userService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/user/all")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(testUser.getUserId()))
                .andExpect(jsonPath("$[1].userId").value(testAdmin.getUserId()));
    }



    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WithExistingId_ReturnsOk() throws Exception {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/user/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getUserId()))
                .andExpect(jsonPath("$.username").value(testUser.getUsername()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WithNonExistentId_ReturnsNotFound() throws Exception {
        // Arrange
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/user/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WithValidData_ReturnsOk() throws Exception {
        // Arrange
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(put("/user/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getUserId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WithNonExistentId_ReturnsBadRequest() throws Exception {
        // Arrange
        when(userService.updateUser(eq(999L), any(User.class)))
                .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        mockMvc.perform(put("/user/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isBadRequest());
    }



    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_WithExistingId_ReturnsOk() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/user/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }


}
