package com.example.demo.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "station_heads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationHead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int headId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(unique = true)
    private String badgeNumber;

    private String phoneNumber;

    private String profilePicture;

    @Column(nullable = false)
    @Builder.Default
    private String role = "STATION_HEAD";

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
