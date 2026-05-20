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
public class AssignmentResponse {
    private int           assignmentId;
    private LocalDateTime assignedAt;
    private String        incidentCode;
    private String        incidentType;
    private String        officerName;
    private String        officerBadge;
    private String        assignedByHeadName;
}
