package com.example.demo.Mapper;


import com.example.demo.dto.OfficerRequest;
import com.example.demo.dto.OfficerResponse;
import com.example.demo.Model.Officer;
import org.springframework.stereotype.Component;


@Component
public class OfficerMapper {

  
    public Officer toEntity(OfficerRequest req, String encodedPassword) {
        return Officer.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .passwordHash(encodedPassword)
                .badgeNumber(req.getBadgeNumber())
                .phoneNumber(req.getPhoneNumber())
                .activeCaseCount(0)
                .role("OFFICER")
                .build();
    }

   
    public OfficerResponse toResponse(Officer officer) {
        if (officer == null) return null;
        return OfficerResponse.builder()
                .officerId(officer.getOfficerId())
                .fullName(officer.getFullName())
                .email(officer.getEmail())
                .badgeNumber(officer.getBadgeNumber())
                .phoneNumber(officer.getPhoneNumber())
                .profilePicture(officer.getProfilePicture())
                .activeCaseCount(officer.getActiveCaseCount())
                .role(officer.getRole())
                .createdAt(officer.getCreatedAt())
                .build();
    }
}
