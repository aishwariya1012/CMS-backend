package com.example.demo.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "officers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Officer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int officerId;

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
    private int activeCaseCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private String role = "OFFICER";

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "officer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Assignment> assignments;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
