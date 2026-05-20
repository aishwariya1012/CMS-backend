package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private int           userId;
    private String        fullName;
    private String        email;
    private String        address;
    private LocalDate     dateOfBirth;
    private int           age;
    private String        aadhaarNumber;
    private String        panNumber;
    private String        phoneNumber;
    private String        profilePicture;
    private String        role;
    private LocalDateTime createdAt;
}
