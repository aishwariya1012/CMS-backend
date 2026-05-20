package com.example.demo.Service;


import com.example.demo.dto.IncidentRequest;
import com.example.demo.dto.IncidentResponse;
import com.example.demo.Exception.BadRequestException;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.Exception.UnauthorizedException;
import com.example.demo.Mapper.DocumentMapper;
import com.example.demo.Mapper.IncidentDetailMapper;
import com.example.demo.Mapper.IncidentMapper;
import com.example.demo.Model.*;
import com.example.demo.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository       incidentRepository;
    private final IncidentDetailRepository incidentDetailRepository;
    private final DocumentRepository       documentRepository;
    private final UserRepository           userRepository;
    private final NotificationService      notificationService;
    private final PdfService               pdfService;
    private final FileStorageService       fileStorageService;
    private final IncidentMapper           incidentMapper;
    private final DocumentMapper           documentMapper;
    private final IncidentDetailMapper     incidentDetailMapper;


    @Transactional
    public IncidentResponse fileIncident(IncidentRequest req, int userId,
                                         MultipartFile graffitiImage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateIncidentType(req.getIncidentType());

        String code = generateUniqueCode(req.getIncidentType());
        Incident incident = incidentMapper.toEntity(req, user, code);
        Incident savedIncident = incidentRepository.save(incident);

        IncidentDetail detail = incidentDetailMapper.toEntity(req, savedIncident);

        if ("GRAFFITI".equalsIgnoreCase(req.getIncidentType()) && graffitiImage != null) {
            String imgPath = fileStorageService.storeFile(graffitiImage, "graffiti-" + code);
            detail.setImagePath(imgPath);
        }

        IncidentDetail savedDetail = incidentDetailRepository.save(detail);

        String pdfPath = pdfService.generateIncidentReport(savedIncident, savedDetail);

        documentRepository.save(documentMapper.toEntity(savedIncident, pdfPath, "INCIDENT_REPORT"));

        notificationService.send(
                userId, "CITIZEN", savedIncident.getIncidentId(),
                "INCIDENT_CREATED",
                "Your incident has been filed successfully. Unique Code: " + code
                + ". Your report is available at: " + pdfPath,
                user.getEmail()
        );

        log.info("Incident filed — code={}, userId={}", code, userId);
        return incidentMapper.toResponse(savedIncident, savedDetail);
    }

    public List<IncidentResponse> getMyIncidents(int userId) {
        return incidentRepository.findByReportedByUser_UserId(userId)
                .stream().map(i -> incidentMapper.toResponse(i, i.getIncidentDetail()))
                .collect(Collectors.toList());
    }

    @Transactional
    public String uploadGraffitiImage(String uniqueCode, int userId, MultipartFile image) {
        Incident incident = incidentRepository.findByUniqueCode(uniqueCode)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + uniqueCode));

        if (incident.getReportedByUser().getUserId() != userId)
            throw new UnauthorizedException("You can only upload images for your own incidents");

        String path = fileStorageService.storeFile(image, "graffiti-" + uniqueCode);

        
        IncidentDetail detail = incident.getIncidentDetail();
        if (detail != null) {
            detail.setImagePath(path);
            incidentDetailRepository.save(detail);
        }

        return path;
    }
    
    public IncidentResponse getIncidentByCode(String uniqueCode, int actorId, String role) {
        Incident incident = incidentRepository.findByUniqueCode(uniqueCode)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + uniqueCode));

        if ("CITIZEN".equals(role) && incident.getReportedByUser().getUserId() != actorId)
            throw new UnauthorizedException("You can only view your own incidents");

        if ("OFFICER".equals(role)) {
            if (incident.getAssignedOfficer() == null
                    || incident.getAssignedOfficer().getOfficerId() != actorId)
                throw new UnauthorizedException("This incident is not assigned to you");
        }

        return incidentMapper.toResponse(incident, incident.getIncidentDetail());
    }

    public List<IncidentResponse> getAllIncidents() {
        return incidentRepository.findAll()
                .stream().map(i -> incidentMapper.toResponse(i, i.getIncidentDetail()))
                .collect(Collectors.toList());
    }

    public List<IncidentResponse> getOfficerIncidents(int officerId) {
        return incidentRepository.findByAssignedOfficer_OfficerId(officerId)
                .stream().map(i -> incidentMapper.toResponse(i, i.getIncidentDetail()))
                .collect(Collectors.toList());
    }


    private void validateIncidentType(String type) {
        if (type == null) throw new BadRequestException("Incident type is required");
        switch (type.toUpperCase()) {
            case "LOST_PROPERTY", "PETIT_LARCENY", "CRIMINAL_MISCHIEF", "GRAFFITI" -> {}
            default -> throw new BadRequestException(
                "Invalid incident type. Allowed: LOST_PROPERTY, PETIT_LARCENY, CRIMINAL_MISCHIEF, GRAFFITI");
        }
    }

    private String generateUniqueCode(String type) {
        String year   = String.valueOf(java.time.Year.now().getValue());
        String abbrev = getTypeAbbrev(type);
        long count    = incidentRepository.countByIncidentType(type.toUpperCase()) + 1;
        return String.format("CMS-%s-%s-%03d", year, abbrev, count);
    }

    private String getTypeAbbrev(String type) {
        return switch (type.toUpperCase()) {
            case "LOST_PROPERTY"     -> "LOST";
            case "PETIT_LARCENY"     -> "LARC";
            case "CRIMINAL_MISCHIEF" -> "MISC";
            case "GRAFFITI"          -> "GRAF";
            default                  -> "INC";
        };
    }
}
