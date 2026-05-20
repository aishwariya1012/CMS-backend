package com.example.demo.Mapper;

import com.example.demo.dto.UserResponse;
import com.example.demo.Model.StationHead;
import org.springframework.stereotype.Component;


@Component
public class StationHeadMapper {

    
    public UserResponse toResponse(StationHead head) {
        if (head == null) return null;
        return UserResponse.builder()
                .userId(head.getHeadId())
                .fullName(head.getFullName())
                .email(head.getEmail())
                .phoneNumber(head.getPhoneNumber())
                .profilePicture(head.getProfilePicture())
                .role(head.getRole())
                .createdAt(head.getCreatedAt())
                .build();
    }
}
