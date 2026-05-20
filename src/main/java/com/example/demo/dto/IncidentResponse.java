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
public class IncidentResponse {
    private int           incidentId;
    private String        uniqueCode;
    private String        incidentType;
    private String        status;
    private String        description;
    private LocalDateTime reportedAt;
    private LocalDateTime updatedAt;

    // Citizen
    private int    reportedByUserId;
    private String reportedByUserName;

    // Officer (null until assigned)
    private Integer assignedOfficerId;
    private String  assignedOfficerName;
    private String  assignedOfficerBadge;

    // Detail
    private String propertyDescription;
    private Double valueEstimate;
    private String location;
    private String imagePath;
    private String lostLocation;
    private String lostDate;
    private String damagedPropertyType;
    private Double damageEstimate;
}
