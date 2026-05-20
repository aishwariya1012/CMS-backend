package com.example.demo.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String address;

    private LocalDate dateOfBirth;

    private int age;

    @Column(unique = true)
    private String aadhaarNumber;

    @Column(unique = true)
    private String panNumber;

    private String phoneNumber;

    private String profilePicture;

    @Column(nullable = false)
    @Builder.Default
    private String role = "CITIZEN";

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "reportedByUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Incident> incidents;

    @OneToMany(mappedBy = "actorId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.dateOfBirth != null) {
            this.age = Period.between(this.dateOfBirth, LocalDate.now()).getYears();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (this.dateOfBirth != null) {
            this.age = Period.between(this.dateOfBirth, LocalDate.now()).getYears();
        }
    }
}
