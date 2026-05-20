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
public class NotificationResponse {
    private int           notificationId;
    private String        type;
    private String        message;
    private String        actorRole;
    private LocalDateTime sentAt;
    private boolean       emailSent;
    private String        incidentCode;
}