package com.example.demo.Controller;

import com.example.demo.dto.*;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/station")
@RequiredArgsConstructor
@Tag(name = "Station Head", description = "All station head operations — requires STATION_HEAD role JWT")
@SecurityRequirement(name = "bearerAuth")
public class StationHeadController {

    private final StationHeadService  stationHeadService;
    private final IncidentService     incidentService;
    private final NotificationService notificationService;
    private final UserRepository      userRepository;


    @Operation(summary = "Get my profile")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication auth) {
        int headId = (int) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Profile fetched",
                stationHeadService.getProfile(headId)));
    }

    @Operation(summary = "Update my profile")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            Authentication auth,
            @Valid @RequestBody UpdateProfileRequest req) {
        int headId = (int) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                stationHeadService.updateProfile(headId, req)));
    }

    @Operation(summary = "Change password")
    @PutMapping("/profile/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication auth,
            @Valid @RequestBody ChangePasswordRequest req) {
        int headId = (int) auth.getPrincipal();
        return ResponseEntity.ok(stationHeadService.changePassword(headId, req));
    }


    @Operation(summary = "Add a new officer")
    @PostMapping("/officers")
    public ResponseEntity<ApiResponse<OfficerResponse>> addOfficer(
            @Valid @RequestBody OfficerRequest request) {
        log.info("Station Head adding officer: {}", request.getEmail());
        OfficerResponse response = stationHeadService.addOfficer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Officer added successfully", response));
    }

    @Operation(summary = "Remove an officer by ID")
    @DeleteMapping("/officers/{officerId}")
    public ResponseEntity<ApiResponse<Void>> removeOfficer(@PathVariable int officerId) {
        log.info("Station Head removing officerId={}", officerId);
        stationHeadService.removeOfficer(officerId);
        return ResponseEntity.ok(ApiResponse.success("Officer removed successfully"));
    }

    @Operation(summary = "Get all officers sorted by least active cases")
    @GetMapping("/officers")
    public ResponseEntity<ApiResponse<List<OfficerResponse>>> getAllOfficers() {
        return ResponseEntity.ok(
                ApiResponse.success("Officers fetched", stationHeadService.getAllOfficers()));
    }

    @Operation(summary = "Get a specific officer's details")
    @GetMapping("/officers/{officerId}")
    public ResponseEntity<ApiResponse<OfficerResponse>> getOfficer(
            @PathVariable int officerId) {
        return ResponseEntity.ok(
                ApiResponse.success("Officer fetched", stationHeadService.getOfficer(officerId)));
    }

    @Operation(summary = "Get all cases assigned to a specific officer")
    @GetMapping("/officers/{officerId}/cases")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getOfficerCases(
            @PathVariable int officerId) {
        return ResponseEntity.ok(
                ApiResponse.success("Officer cases fetched",
                        incidentService.getOfficerIncidents(officerId)));
    }


    @Operation(summary = "View all incidents in the system")
    @GetMapping("/incidents")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getAllIncidents() {
        return ResponseEntity.ok(
                ApiResponse.success("All incidents fetched",
                        incidentService.getAllIncidents()));
    }

    @Operation(summary = "Get details of a specific incident by unique code")
    @GetMapping("/incidents/{uniqueCode}")
    public ResponseEntity<ApiResponse<IncidentResponse>> getIncidentDetail(
            Authentication auth,
            @PathVariable String uniqueCode) {
        int headId = (int) auth.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success("Incident fetched",
                        incidentService.getIncidentByCode(uniqueCode, headId, "STATION_HEAD")));
    }

    @Operation(summary = "Assign an officer to an incident")
    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<String>> assignOfficer(
            Authentication auth,
            @Valid @RequestBody AssignRequest request) {
        int headId = (int) auth.getPrincipal();
        log.info("SH {} assigning officerId={} to incidentId={}",
                headId, request.getOfficerId(), request.getIncidentId());
        return ResponseEntity.ok(stationHeadService.assignOfficer(request, headId));
    }

    @Operation(summary = "Verify a closed case (changes status to VERIFIED)")
    @PutMapping("/incidents/{incidentId}/verify")
    public ResponseEntity<ApiResponse<String>> verifyCase(
            Authentication auth,
            @PathVariable int incidentId) {
        int headId = (int) auth.getPrincipal();
        log.info("SH {} verifying incidentId={}", headId, incidentId);
        return ResponseEntity.ok(stationHeadService.verifyCase(incidentId, headId));
    }


    @Operation(summary = "View all registered citizens")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(
                ApiResponse.success("Users fetched",
                        stationHeadService.getAllUsers(userRepository)));
    }


    @Operation(summary = "Get my notifications")
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<?>> getNotifications(Authentication auth) {
        int headId = (int) auth.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success("Notifications fetched",
                        notificationService.getNotificationsForActor(headId, "STATION_HEAD")));
    }
}

