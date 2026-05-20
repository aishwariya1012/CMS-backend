package com.example.demo.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String address;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;
}

