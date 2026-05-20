package com.example.demo.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    @Builder.Default
    private String documentType = "INCIDENT_REPORT";  // INCIDENT_REPORT | STATUS_CARD

    private LocalDateTime generatedAt;

    @PrePersist
    public void prePersist() {
        this.generatedAt = LocalDateTime.now();
    }
}
