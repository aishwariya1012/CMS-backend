package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRequest {

    @NotNull(message = "Incident ID is required")
    private Integer incidentId;

    @NotNull(message = "Officer ID is required")
    private Integer officerId;
}