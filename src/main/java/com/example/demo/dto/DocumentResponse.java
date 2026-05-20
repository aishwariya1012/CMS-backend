package com.example.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private int           documentId;
    private String        documentType;
    private String        filePath;
    private LocalDateTime generatedAt;
    private String        incidentCode;
}