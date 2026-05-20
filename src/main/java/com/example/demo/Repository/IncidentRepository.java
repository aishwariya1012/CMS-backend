package com.example.demo.Repository;

import com.example.demo.Model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Integer> {
    List<Incident> findByReportedByUser_UserId(int userId);
    List<Incident> findByAssignedOfficer_OfficerId(int officerId);
    Optional<Incident> findByUniqueCode(String uniqueCode);
    List<Incident> findByStatus(String status);
    long countByIncidentType(String incidentType);
}
