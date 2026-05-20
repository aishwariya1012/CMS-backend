package com.example.demo.Repository;

import com.example.demo.Model.Officer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfficerRepository extends JpaRepository<Officer, Integer> {
    Optional<Officer> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByBadgeNumber(String badgeNumber);
    List<Officer> findAllByOrderByActiveCaseCountAsc();
}