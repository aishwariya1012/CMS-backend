package com.example.demo.Controller;

import com.example.demo.dto.*;
import com.example.demo.Service.IncidentService;
import com.example.demo.Service.NotificationService;
import com.example.demo.Service.OfficerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/officer")
@RequiredArgsConstructor
@Tag(name = "Officer", description = "All officer operations — requires OFFICER role JWT")
@SecurityRequirement(name = "bearerAuth")
public class OfficerController {

    private final OfficerService      officerService;
    private final IncidentService     incidentService;
    private final NotificationService notificationService;


    @Operation(summary = "Get my officer profile")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<OfficerResponse>> getProfile(Authentication auth) {
        int officerId = (int) auth.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success("Profile fetched", officerService.getProfile(officerId)));
    }

    @Operation(summary = "Update my profile (name, phone)")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<OfficerResponse>> updateProfile(
            Authentication auth,
            @Valid @RequestBody UpdateProfileRequest req) {
        int officerId = (int) auth.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated", officerService.updateProfile(officerId, req)));
    }

    @Operation(summary = "Upload profile picture")
    @PostMapping(value = "/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<OfficerResponse>> uploadPicture(
            Authentication auth,
            @RequestParam("file") MultipartFile file) {
        int officerId = (int) auth.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success("Profile picture updated",
                        officerService.uploadProfilePicture(officerId, file)));
    }

    @Operation(summary = "Change my password")
    @PutMapping("/profile/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication auth,
            @Valid @RequestBody ChangePasswordRequest req) {
        int officerId = (int) auth.getPrincipal();
        return ResponseEntity.ok(officerService.changePassword(officerId, req));
    }


    @Operation(summary = "View all cases assigned to me")
    @GetMapping("/cases")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getMyCases(Authentication auth) {
        int officerId = (int) auth.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success("Cases fetched", officerService.getMyCases(officerId)));
    }

    @Operation(summary = "View details of a specific assigned case by unique code")
    @GetMapping("/cases/{uniqueCode}")
    public ResponseEntity<ApiResponse<IncidentResponse>> getCaseDetail(
            Authentication auth,
            @PathVariable String uniqueCode) {
        int officerId = (int) auth.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success("Case details fetched",
                        incidentService.getIncidentByCode(uniqueCode, officerId, "OFFICER")));
    }

    @Operation(summary = "Close a case (changes status to CLOSED, goes to SH for verification)")
    @PutMapping("/cases/{incidentId}/close")
    public ResponseEntity<ApiResponse<String>> closeCase(
            Authentication auth,
            @PathVariable int incidentId) {
        int officerId = (int) auth.getPrincipal();
        log.info("Officer {} closing incidentId={}", officerId, incidentId);
        return ResponseEntity.ok(officerService.closeCase(incidentId, officerId));
    }


    @Operation(summary = "Get my notifications")
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<?>> getNotifications(Authentication auth) {
        int officerId = (int) auth.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success("Notifications fetched",
                        notificationService.getNotificationsForActor(officerId, "OFFICER")));
    }
}
