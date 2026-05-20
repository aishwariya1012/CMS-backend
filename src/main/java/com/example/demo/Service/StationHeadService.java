package com.example.demo.Service;


import com.example.demo.dto.*;
import com.example.demo.Exception.BadRequestException;
import com.example.demo.Exception.DuplicateResourceException;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.Mapper.AssignmentMapper;
import com.example.demo.Mapper.DocumentMapper;
import com.example.demo.Mapper.OfficerMapper;
import com.example.demo.Mapper.StationHeadMapper;
import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.*;
import com.example.demo.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StationHeadService {

    private final OfficerRepository     officerRepository;
    private final IncidentRepository    incidentRepository;
    private final AssignmentRepository  assignmentRepository;
    private final DocumentRepository    documentRepository;
    private final StationHeadRepository stationHeadRepository;
    private final NotificationService   notificationService;
    private final PdfService            pdfService;
    private final PasswordEncoder       passwordEncoder;
    private final OfficerMapper         officerMapper;
    private final UserMapper            userMapper;
    private final AssignmentMapper      assignmentMapper;
    private final DocumentMapper        documentMapper;
    private final StationHeadMapper     stationHeadMapper;

    // ── GET OWN PROFILE ──────────────────────────────────────────────

    public UserResponse getProfile(int headId) {
        StationHead head = stationHeadRepository.findById(headId)
                .orElseThrow(() -> new ResourceNotFoundException("Station head not found"));
        return stationHeadMapper.toResponse(head);
    }

    // ── UPDATE OWN PROFILE ───────────────────────────────────────────

    @Transactional
    public UserResponse updateProfile(int headId, com.example.demo.dto.UpdateProfileRequest req) {
        StationHead head = stationHeadRepository.findById(headId)
                .orElseThrow(() -> new ResourceNotFoundException("Station head not found"));
        if (req.getFullName()    != null) head.setFullName(req.getFullName());
        if (req.getPhoneNumber() != null) head.setPhoneNumber(req.getPhoneNumber());
        return stationHeadMapper.toResponse(stationHeadRepository.save(head));
    }

    // ── CHANGE PASSWORD ──────────────────────────────────────────────

    @Transactional
    public ApiResponse<Void> changePassword(int headId, com.example.demo.dto.ChangePasswordRequest req) {
        StationHead head = stationHeadRepository.findById(headId)
                .orElseThrow(() -> new ResourceNotFoundException("Station head not found"));
        if (!passwordEncoder.matches(req.getOldPassword(), head.getPasswordHash()))
            throw new BadRequestException("Old password is incorrect");
        head.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        stationHeadRepository.save(head);
        return ApiResponse.success("Password changed successfully");
    }

    // ── ADD OFFICER ──────────────────────────────────────────────────

    @Transactional
    public OfficerResponse addOfficer(OfficerRequest req) {
        if (officerRepository.existsByEmail(req.getEmail()))
            throw new DuplicateResourceException("Officer email already exists");
        if (officerRepository.existsByBadgeNumber(req.getBadgeNumber()))
            throw new DuplicateResourceException("Badge number already exists");

        Officer officer = officerMapper.toEntity(req, passwordEncoder.encode(req.getPassword()));
        Officer saved   = officerRepository.save(officer);
        log.info("Officer added — officerId={}", saved.getOfficerId());

        notificationService.send(saved.getOfficerId(), "OFFICER", null,
                "ACCOUNT_CREATED",
                "Welcome to CMS! Your officer account has been created. Badge: " + saved.getBadgeNumber(),
                saved.getEmail());

        return officerMapper.toResponse(saved);
    }

    // ── REMOVE OFFICER ───────────────────────────────────────────────

    @Transactional
    public void removeOfficer(int officerId) {
        Officer officer = officerRepository.findById(officerId)
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found with ID: " + officerId));

        if (officer.getActiveCaseCount() > 0)
            throw new BadRequestException("Cannot remove officer with active cases. Reassign cases first.");

        officerRepository.delete(officer);
        log.info("Officer removed — officerId={}", officerId);
    }

    // ── GET ALL OFFICERS ─────────────────────────────────────────────

    public List<OfficerResponse> getAllOfficers() {
        return officerRepository.findAllByOrderByActiveCaseCountAsc()
                .stream().map(officerMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ── GET OFFICER DETAILS ───────────────────────────────────────────

    public OfficerResponse getOfficer(int officerId) {
        Officer officer = officerRepository.findById(officerId)
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found with ID: " + officerId));
        return officerMapper.toResponse(officer);
    }

    // ── ASSIGN OFFICER TO INCIDENT ───────────────────────────────────

    @Transactional
    public ApiResponse<String> assignOfficer(AssignRequest req, int headId) {
        Incident incident = incidentRepository.findById(req.getIncidentId())
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + req.getIncidentId()));

        if ("ACTIVE".equals(incident.getStatus()) ||
                "CLOSED".equals(incident.getStatus()) ||
                "VERIFIED".equals(incident.getStatus())) {
                throw new BadRequestException(
                    "Cannot reassign. Incident is already " + incident.getStatus()
                );
            }
        Officer officer = officerRepository.findById(req.getOfficerId())
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found: " + req.getOfficerId()));

        StationHead head = stationHeadRepository.findById(headId)
                .orElseThrow(() -> new ResourceNotFoundException("Station head not found"));

        // Create assignment record via AssignmentMapper
        assignmentRepository.save(assignmentMapper.toEntity(incident, officer, head));

        // Update incident
        incident.setAssignedOfficer(officer);
        incident.setStatus("ACTIVE");
        incidentRepository.save(incident);

        // Update officer case count
        officer.setActiveCaseCount(officer.getActiveCaseCount() + 1);
        officerRepository.save(officer);

        // Notify citizen
        notificationService.send(
                incident.getReportedByUser().getUserId(), "CITIZEN",
                incident.getIncidentId(), "OFFICER_ASSIGNED",
                "An officer has been assigned to your case " + incident.getUniqueCode()
                + ". Officer: " + officer.getFullName() + " (Badge: " + officer.getBadgeNumber() + ")",
                incident.getReportedByUser().getEmail()
        );

        log.info("Officer assigned — incidentId={}, officerId={}, headId={}",
                req.getIncidentId(), req.getOfficerId(), headId);

        return ApiResponse.success("Officer assigned successfully",
                "Officer " + officer.getFullName() + " assigned to " + incident.getUniqueCode());
    }

    // ── VERIFY CASE ───────────────────────────────────────────────────

    @Transactional
    public ApiResponse<String> verifyCase(int incidentId, int headId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + incidentId));

        if (!"CLOSED".equals(incident.getStatus()))
            throw new BadRequestException("Only CLOSED cases can be verified. Current status: " + incident.getStatus());

        StationHead head = stationHeadRepository.findById(headId)
                .orElseThrow(() -> new ResourceNotFoundException("Station head not found"));

        // Update incident
        incident.setStatus("VERIFIED");
        incident.setVerifiedByHead(head);
        incidentRepository.save(incident);

        // Decrement officer case count
        if (incident.getAssignedOfficer() != null) {
            Officer officer = incident.getAssignedOfficer();
            officer.setActiveCaseCount(Math.max(0, officer.getActiveCaseCount() - 1));
            officerRepository.save(officer);
        }

        // Generate status card PDF and save via DocumentMapper
        String pdfPath = pdfService.generateStatusCard(incident);
        documentRepository.save(documentMapper.toEntity(incident, pdfPath, "STATUS_CARD"));

        // Notify citizen
        notificationService.send(
                incident.getReportedByUser().getUserId(), "CITIZEN",
                incident.getIncidentId(), "CASE_VERIFIED",
                "Your case " + incident.getUniqueCode() + " has been verified and closed. "
                + "Download your status card from: " + pdfPath,
                incident.getReportedByUser().getEmail()
        );

        log.info("Case verified — incidentId={}, headId={}", incidentId, headId);
        return ApiResponse.success("Case verified successfully",
                "Case " + incident.getUniqueCode() + " is now VERIFIED");
    }

    // ── GET ALL USERS (Station Head view) ────────────────────────────

    public List<UserResponse> getAllUsers(com.example.demo.Repository.UserRepository userRepository) {
        return userRepository.findAll()
                .stream().map(userMapper::toResponse)
                .collect(Collectors.toList());
    }
}
