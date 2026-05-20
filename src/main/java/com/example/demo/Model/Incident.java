package com.example.demo.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int incidentId;

    @Column(unique = true, nullable = false)
    private String uniqueCode;

    @Column(nullable = false)
    private String incidentType;

    @Column(nullable = false)
    @Builder.Default
    private String status = "INITIATED";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    private LocalDateTime reportedAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_user_id", nullable = false)
    private User reportedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_id")
    private Officer assignedOfficer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_head_id")
    private StationHead verifiedByHead;

    @OneToOne(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private IncidentDetail incidentDetail;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Assignment> assignments;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Document> documents;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications;

    @PrePersist
    public void prePersist() {
        this.reportedAt = LocalDateTime.now();
        this.updatedAt  = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
