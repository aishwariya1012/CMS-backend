package com.example.demo.Controller;

import com.example.demo.dto.*;
import com.example.demo.Model.Document;
import com.example.demo.Repository.DocumentRepository;
import com.example.demo.Service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/citizen")
@RequiredArgsConstructor
@Tag(name = "Citizen", description = "All citizen operations — requires CITIZEN role JWT")
@SecurityRequirement(name = "bearerAuth")
public class CitizenController {

    private final IncidentService     incidentService;
    private final UserService         userService;
    private final NotificationService notificationService;
    private final FileStorageService  fileStorageService;
    private final DocumentRepository  documentRepository;

   

    @Operation(summary = "Get my profile")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication auth) {
        int userId = (int) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", userService.getProfile(userId)));
    }

    @Operation(summary = "Update my profile")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            Authentication auth, @Valid @RequestBody UpdateProfileRequest req) {
        int userId = (int) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Profile updated", userService.updateProfile(userId, req)));
    }

    @Operation(summary = "Upload profile picture")
    @PostMapping(value = "/profile/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> uploadPicture(
            Authentication auth, @RequestParam("file") MultipartFile file) {
        int userId = (int) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Picture updated", userService.uploadProfilePicture(userId, file)));
    }

    @Operation(summary = "Change password")
    @PutMapping("/profile/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication auth, @Valid @RequestBody ChangePasswordRequest req) {
        int userId = (int) auth.getPrincipal();
        return ResponseEntity.ok(userService.changePassword(userId, req));
    }

    // incidents

    @Operation(summary = "File a new incident")
    @PostMapping(value = "/incidents")
    public ResponseEntity<ApiResponse<IncidentResponse>> fileIncident(
            Authentication auth,
            @RequestBody @Valid IncidentRequest request) {

        int userId = (int) auth.getPrincipal();
        log.info("Citizen {} filing incident type={}", userId, request.getIncidentType());
        IncidentResponse response = incidentService.fileIncident(request, userId, null);
        return ResponseEntity.status(201)
                .body(ApiResponse.success("Incident filed successfully", response));
    }

    @PostMapping(value = "/incidents/{uniqueCode}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadGraffitiImage(
            Authentication auth,
            @PathVariable String uniqueCode,
            @RequestParam("image") MultipartFile image) {

        int userId = (int) auth.getPrincipal();
        return ResponseEntity.ok(
            ApiResponse.success("Image uploaded", incidentService.uploadGraffitiImage(uniqueCode, userId, image))
        );
    }

    @Operation(summary = "Get all my incidents")
    @GetMapping("/incidents")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getMyIncidents(Authentication auth) {
        int userId = (int) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Incidents fetched", incidentService.getMyIncidents(userId)));
    }

    @Operation(summary = "Get incident details by unique code (e.g. CMS-2026-LARC-001)")
    @GetMapping("/incidents/{uniqueCode}")
    public ResponseEntity<ApiResponse<IncidentResponse>> getIncidentDetail(
            Authentication auth, @PathVariable String uniqueCode) {
        int userId = (int) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Incident fetched",
                incidentService.getIncidentByCode(uniqueCode, userId, "CITIZEN")));
    }

    //pdf dl

    @Operation(summary = "Download incident report PDF")
    @GetMapping("/incidents/{uniqueCode}/download")
    public ResponseEntity<Resource> downloadReport(Authentication auth, @PathVariable String uniqueCode) {
        int userId = (int) auth.getPrincipal();
        IncidentResponse inc = incidentService.getIncidentByCode(uniqueCode, userId, "CITIZEN");
        List<Document> docs = documentRepository
                .findByIncident_IncidentIdAndDocumentType(inc.getIncidentId(), "INCIDENT_REPORT");
        if (docs.isEmpty()) return ResponseEntity.notFound().build();
        Resource resource = fileStorageService.loadFileAsResource(docs.get(docs.size() - 1).getFilePath());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + uniqueCode + "-report.pdf\"")
                .body(resource);
    }

    @Operation(summary = "Download status card PDF (available after VERIFIED)")
    @GetMapping("/incidents/{uniqueCode}/status-card")
    public ResponseEntity<Resource> downloadStatusCard(Authentication auth, @PathVariable String uniqueCode) {
        int userId = (int) auth.getPrincipal();
        IncidentResponse inc = incidentService.getIncidentByCode(uniqueCode, userId, "CITIZEN");
        List<Document> docs = documentRepository
                .findByIncident_IncidentIdAndDocumentType(inc.getIncidentId(), "STATUS_CARD");
        if (docs.isEmpty()) return ResponseEntity.notFound().build();
        Resource resource = fileStorageService.loadFileAsResource(docs.get(docs.size() - 1).getFilePath());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + uniqueCode + "-status.pdf\"")
                .body(resource);
    }

    // notification

    @Operation(summary = "Get my notifications")
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<?>> getNotifications(Authentication auth) {
        int userId = (int) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched",
                notificationService.getNotificationsForActor(userId, "CITIZEN")));
    }
}
