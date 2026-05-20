package com.example.demo.Repository;


import com.example.demo.Model.StationHead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StationHeadRepository extends JpaRepository<StationHead, Integer> {
    Optional<StationHead> findByEmail(String email);
    boolean existsByEmail(String email);
}
