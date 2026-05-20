package com.example.demo.Mapper;
import com.example.demo.dto.AssignmentResponse;
import com.example.demo.Model.Assignment;
import com.example.demo.Model.Incident;
import com.example.demo.Model.Officer;
import com.example.demo.Model.StationHead;
import org.springframework.stereotype.Component;

@Component
public class AssignmentMapper {


    public Assignment toEntity(Incident incident, Officer officer, StationHead assignedByHead) {
        return Assignment.builder()
                .incident(incident)
                .officer(officer)
                .assignedByHead(assignedByHead)
                .build();
    }

    
    public AssignmentResponse toResponse(Assignment assignment) {
        if (assignment == null) return null;
        return AssignmentResponse.builder()
                .assignmentId(assignment.getAssignmentId())
                .assignedAt(assignment.getAssignedAt())
                .incidentCode(assignment.getIncident() != null
                        ? assignment.getIncident().getUniqueCode() : null)
                .incidentType(assignment.getIncident() != null
                        ? assignment.getIncident().getIncidentType() : null)
                .officerName(assignment.getOfficer() != null
                        ? assignment.getOfficer().getFullName() : null)
                .officerBadge(assignment.getOfficer() != null
                        ? assignment.getOfficer().getBadgeNumber() : null)
                .assignedByHeadName(assignment.getAssignedByHead() != null
                        ? assignment.getAssignedByHead().getFullName() : null)
                .build();
    }
}