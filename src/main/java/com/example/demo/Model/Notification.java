package com.example.demo.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int notificationId;

    // Polymorphic: stores ID of User / Officer / StationHead
    @Column(nullable = false)
    private int actorId;
//actorid and role together act as unique identifier
    @Column(nullable = false)
    private String actorRole;   // CITIZEN | OFFICER | STATION_HEAD

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id")
    private Incident incident;  // nullable for login/register alerts

    // ACCOUNT_CREATED | LOGIN_ALERT | INCIDENT_CREATED |
    // OFFICER_ASSIGNED | CASE_CLOSED_PENDING | CASE_VERIFIED
    @Column(nullable = false)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime sentAt;

    @Builder.Default
    private boolean emailSent = false;

    @PrePersist
    public void prePersist() {
        this.sentAt = LocalDateTime.now();
    }
}
