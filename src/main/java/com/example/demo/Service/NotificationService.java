package com.example.demo.Service;

import com.example.demo.dto.NotificationResponse;
import java.util.List;
import com.example.demo.Model.Incident;
import com.example.demo.Model.Notification;
import com.example.demo.Mapper.NotificationMapper;
import com.example.demo.Repository.IncidentRepository;
import com.example.demo.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final IncidentRepository incidentRepository;
    private final NotificationMapper notificationMapper;
    private final JavaMailSender mailSender;

    
    public Notification send(int actorId, String actorRole,
                             Integer incidentId, String type,
                             String message, String toEmail) {

        Incident incident = null;
        if (incidentId != null) {
            incident = incidentRepository.findById(incidentId).orElse(null);
        }

        Notification notification = notificationMapper.toEntity(actorId, actorRole, incident, type, message);

        Notification saved = notificationRepository.save(notification);

        
        if (toEmail != null && !toEmail.isBlank()) {
            sendEmailAsync(toEmail, "CMS Notification — " + type, message);
            saved.setEmailSent(true);
            notificationRepository.save(saved);
        }

        log.info("Notification saved — type={}, actorId={}, role={}", type, actorId, actorRole);
        return saved;
    }

   
    public Notification send(int actorId, String actorRole,
                             Integer incidentId, String type, String message) {
        return send(actorId, actorRole, incidentId, type, message, null);
    }

    @Async
    protected void sendEmailAsync(String to, String subject, String body) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    public List<NotificationResponse> getNotificationsForActor(int actorId, String actorRole) {
        return notificationRepository
                .findByActorIdAndActorRoleOrderBySentAtDesc(actorId, actorRole)
                .stream()
                .map(notificationMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }
}
