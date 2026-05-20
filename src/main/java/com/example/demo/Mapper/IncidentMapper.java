package com.example.demo.Mapper;

import com.example.demo.dto.IncidentRequest;
import com.example.demo.dto.IncidentResponse;
import com.example.demo.Model.Incident;
import com.example.demo.Model.IncidentDetail;
import com.example.demo.Model.User;
import org.springframework.stereotype.Component;


@Component
public class IncidentMapper {

   
    public Incident toEntity(IncidentRequest req, User reportedByUser, String uniqueCode) {
        return Incident.builder()
                .uniqueCode(uniqueCode)
                .incidentType(req.getIncidentType().toUpperCase())
                .status("INITIATED")
                .description(req.getDescription())
                .reportedByUser(reportedByUser)
                .build();
    }

    
    public IncidentDetail toDetailEntity(IncidentRequest req, Incident savedIncident) {
        return IncidentDetail.builder()
                .incident(savedIncident)
                .propertyDescription(req.getPropertyDescription())
                .valueEstimate(req.getValueEstimate())
                .location(req.getLocation())
                .lostLocation(req.getLostLocation())
                .lostDate(req.getLostDate())
                .damagedPropertyType(req.getDamagedPropertyType())
                .damageEstimate(req.getDamageEstimate())
                .build();
    }

  
    public IncidentResponse toResponse(Incident incident, IncidentDetail detail) {
        if (incident == null) return null;

        IncidentResponse.IncidentResponseBuilder builder = IncidentResponse.builder()
                .incidentId(incident.getIncidentId())
                .uniqueCode(incident.getUniqueCode())
                .incidentType(incident.getIncidentType())
                .status(incident.getStatus())
                .description(incident.getDescription())
                .reportedAt(incident.getReportedAt())
                .updatedAt(incident.getUpdatedAt());

        if (incident.getReportedByUser() != null) {
            builder.reportedByUserId(incident.getReportedByUser().getUserId());
            builder.reportedByUserName(incident.getReportedByUser().getFullName());
        }

        if (incident.getAssignedOfficer() != null) {
            builder.assignedOfficerId(incident.getAssignedOfficer().getOfficerId());
            builder.assignedOfficerName(incident.getAssignedOfficer().getFullName());
            builder.assignedOfficerBadge(incident.getAssignedOfficer().getBadgeNumber());
        }

        if (detail != null) {
            builder.propertyDescription(detail.getPropertyDescription());
            builder.valueEstimate(detail.getValueEstimate());
            builder.location(detail.getLocation());
            builder.imagePath(detail.getImagePath());
            builder.lostLocation(detail.getLostLocation());
            builder.lostDate(detail.getLostDate());
            builder.damagedPropertyType(detail.getDamagedPropertyType());
            builder.damageEstimate(detail.getDamageEstimate());
        }

        return builder.build();
    }
}