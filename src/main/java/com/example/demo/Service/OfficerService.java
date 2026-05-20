package com.example.demo.Service;


import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.IncidentResponse;
import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.dto.OfficerResponse;
import com.example.demo.Exception.BadRequestException;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.Exception.UnauthorizedException;
import com.example.demo.Mapper.OfficerMapper;
import com.example.demo.Mapper.IncidentMapper;
import com.example.demo.Model.Incident;
import com.example.demo.Model.Officer;
import com.example.demo.Repository.IncidentRepository;
import com.example.demo.Repository.OfficerRepository;
import com.example.demo.Repository.StationHeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfficerService {

    private final OfficerRepository     officerRepository;
    private final IncidentRepository    incidentRepository;
    private final StationHeadRepository stationHeadRepository;
    private final NotificationService   notificationService;
    private final PasswordEncoder       passwordEncoder;
    private final FileStorageService    fileStorageService;
    private final IncidentService       incidentService;
    private final OfficerMapper         officerMapper;

    private final IncidentMapper        incidentMapper;

    // ── VIEW ASSIGNED CASES ──────────────────────────────────────────

    public List<IncidentResponse> getMyCases(int officerId) {
        return incidentRepository.findByAssignedOfficer_OfficerId(officerId)
                .stream().map(i -> incidentMapper.toResponse(i, i.getIncidentDetail()))
                .collect(Collectors.toList());
    }

    // ── CLOSE CASE ────────────────────────────────────────────────────

    @Transactional
    public ApiResponse<String> closeCase(int incidentId, int officerId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentId));

        if (incident.getAssignedOfficer() == null
                || incident.getAssignedOfficer().getOfficerId() != officerId)
            throw new UnauthorizedException("This incident is not assigned to you");

        if (!"ACTIVE".equals(incident.getStatus()))
            throw new BadRequestException("Only ACTIVE cases can be closed. Current: " + incident.getStatus());

        incident.setStatus("CLOSED");
        incidentRepository.save(incident);

        // Notify station head
        stationHeadRepository.findAll().stream().findFirst().ifPresent(head ->
            notificationService.send(
                    head.getHeadId(), "STATION_HEAD",
                    incident.getIncidentId(), "CASE_CLOSED_PENDING",
                    "Officer " + incident.getAssignedOfficer().getFullName()
                    + " has closed case " + incident.getUniqueCode() + ". Please verify.",
                    head.getEmail()
            )
        );

        log.info("Case closed — incidentId={}, officerId={}", incidentId, officerId);
        return ApiResponse.success("Case marked as closed. Sent to Station Head for verification.",
                incident.getUniqueCode());
    }

    // ── GET PROFILE ──────────────────────────────────────────────────

    public OfficerResponse getProfile(int officerId) {
        Officer officer = officerRepository.findById(officerId)
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found"));
        return officerMapper.toResponse(officer);
    }

    @Transactional
    public OfficerResponse updateProfile(int officerId, UpdateProfileRequest req) {
        Officer officer = officerRepository.findById(officerId)
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found"));
        if (req.getFullName()    != null) officer.setFullName(req.getFullName());
        if (req.getPhoneNumber() != null) officer.setPhoneNumber(req.getPhoneNumber());
        return officerMapper.toResponse(officerRepository.save(officer));
    }

    @Transactional
    public OfficerResponse uploadProfilePicture(int officerId, MultipartFile file) {
        Officer officer = officerRepository.findById(officerId)
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found"));
        String path = fileStorageService.storeFile(file, "officer-" + officerId);
        officer.setProfilePicture(path);
        return officerMapper.toResponse(officerRepository.save(officer));
    }

    @Transactional
    public ApiResponse<Void> changePassword(int officerId, ChangePasswordRequest req) {
        Officer officer = officerRepository.findById(officerId)
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found"));
        if (!passwordEncoder.matches(req.getOldPassword(), officer.getPasswordHash()))
            throw new BadRequestException("Old password is incorrect");
        officer.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        officerRepository.save(officer);
        return ApiResponse.success("Password changed successfully");
    }
}
