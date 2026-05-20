package com.example.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficerResponse {
    private int           officerId;
    private String        fullName;
    private String        email;
    private String        badgeNumber;
    private String        phoneNumber;
    private String        profilePicture;
    private int           activeCaseCount;
    private String        role;
    private LocalDateTime createdAt;
}

