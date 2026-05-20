package com.example.demo.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "incident_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int detailId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    private String propertyDescription;

    private Double valueEstimate;

    private String location;

    private String imagePath;

    // LOST_PROPERTY specific
    private String lostLocation;
    private String lostDate;

    // CRIMINAL_MISCHIEF specific
    private String damagedPropertyType;
    private Double damageEstimate;
}
