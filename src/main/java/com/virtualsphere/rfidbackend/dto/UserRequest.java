package com.virtualsphere.rfidbackend.dto;

import com.virtualsphere.rfidbackend.model.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Used for both create and update. On create, username + password are required;
 * on update, only the fields you want to change need to be set (null = unchanged).
 */
@Data
public class UserRequest {
    @NotBlank
    private String username;

    private String password;
    private String fullName;
    private Role role;
    private String location;
    private Boolean active;
}
