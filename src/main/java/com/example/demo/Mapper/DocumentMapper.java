package com.example.demo.Mapper;

import com.example.demo.dto.DocumentResponse;
import com.example.demo.Model.Document;
import com.example.demo.Model.Incident;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

  
    public Document toEntity(Incident incident, String filePath, String documentType) {
        return Document.builder()
                .incident(incident)
                .filePath(filePath)
                .documentType(documentType)
                .build();
    }

   
    public DocumentResponse toResponse(Document document) {
        if (document == null) return null;
        return DocumentResponse.builder()
                .documentId(document.getDocumentId())
                .documentType(document.getDocumentType())
                .filePath(document.getFilePath())
                .generatedAt(document.getGeneratedAt())
                .incidentCode(document.getIncident() != null
                        ? document.getIncident().getUniqueCode() : null)
                .build();
    }
}