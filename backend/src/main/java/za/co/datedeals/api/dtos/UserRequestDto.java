package za.co.datedeals.api.dtos;

import lombok.Data;
import za.co.datedeals.api.entities.user.User;

@Data
public class UserRequestDto {
    private String username;
    private String password;
    private User.UserRole role;
    private Long businessId;
    private Boolean enabled = true;
}
