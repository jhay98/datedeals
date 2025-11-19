package za.co.datedeals.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import za.co.datedeals.api.dtos.LoginRequestDto;
import za.co.datedeals.api.dtos.LoginResponseDto;
import za.co.datedeals.api.entities.user.User;
import za.co.datedeals.api.entities.user.UserRepository;
import za.co.datedeals.api.security.JwtTokenProvider;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    public LoginResponseDto login(LoginRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long businessId = user.getBusiness() != null ? user.getBusiness().getBusinessId() : null;
        String businessName = user.getBusiness() != null ? user.getBusiness().getBusinessName() : null;

        return new LoginResponseDto(token, user.getRole().name(), businessId, businessName);
    }
}
