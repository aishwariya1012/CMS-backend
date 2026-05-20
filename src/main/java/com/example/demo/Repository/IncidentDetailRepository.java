package com.example.demo.Repository;

import com.example.demo.Model.IncidentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IncidentDetailRepository extends JpaRepository<IncidentDetail, Integer> {
    Optional<IncidentDetail> findByIncident_IncidentId(int incidentId);
}