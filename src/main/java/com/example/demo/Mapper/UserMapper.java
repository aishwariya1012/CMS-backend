package com.example.demo.Mapper;


import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.Model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;


@Component
public class UserMapper {

    public User toEntity(RegisterRequest req, String encodedPassword) {
        int age = 0;
        if (req.getDateOfBirth() != null) {
            age = Period.between(req.getDateOfBirth(), LocalDate.now()).getYears();
        }
        return User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .passwordHash(encodedPassword)
                .address(req.getAddress())
                .dateOfBirth(req.getDateOfBirth())
                .age(age)
                .aadhaarNumber(req.getAadhaarNumber())
                .panNumber(req.getPanNumber())
                .phoneNumber(req.getPhoneNumber())
                .role("CITIZEN")
                .build();
    }

    
    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .age(user.getAge())
                .aadhaarNumber(user.getAadhaarNumber())
                .panNumber(user.getPanNumber())
                .phoneNumber(user.getPhoneNumber())
                .profilePicture(user.getProfilePicture())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
