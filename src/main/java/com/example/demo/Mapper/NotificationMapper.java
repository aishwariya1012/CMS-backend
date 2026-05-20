package com.example.demo.Mapper;
import com.example.demo.dto.NotificationResponse;
import com.example.demo.Model.Incident;
import com.example.demo.Model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    
    public Notification toEntity(int actorId, String actorRole,
                                  Incident incident, String type, String message) {
        return Notification.builder()
                .actorId(actorId)
                .actorRole(actorRole)
                .incident(incident)
                .type(type)
                .message(message)
                .emailSent(false)
                .build();
    }

    
    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) return null;
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getType())
                .message(notification.getMessage())
                .actorRole(notification.getActorRole())
                .sentAt(notification.getSentAt())
                .emailSent(notification.isEmailSent())
                .incidentCode(notification.getIncident() != null
                        ? notification.getIncident().getUniqueCode() : null)
                .build();
    }
}