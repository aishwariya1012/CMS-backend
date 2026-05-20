package com.example.demo.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IncidentRequest {

    @NotBlank(message = "Incident type is required")
    private String incidentType;   // LOST_PROPERTY | PETIT_LARCENY | CRIMINAL_MISCHIEF | GRAFFITI

    @NotBlank(message = "Description is required")
    private String description;

    // Common detail fields
    private String propertyDescription;
    private Double valueEstimate;
    private String location;

    // LOST_PROPERTY
    private String lostLocation;
    private String lostDate;

    // CRIMINAL_MISCHIEF
    private String damagedPropertyType;
    private Double damageEstimate;

    // GRAFFITI — image uploaded separately as multipart
    private String imagePath;
}
