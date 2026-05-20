package com.example.demo.Repository;

import com.example.demo.Model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {
    List<Assignment> findByIncident_IncidentId(int incidentId);
    List<Assignment> findByOfficer_OfficerId(int officerId);
}
