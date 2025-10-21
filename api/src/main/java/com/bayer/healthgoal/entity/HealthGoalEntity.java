package com.bayer.healthgoal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "health_goals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HealthGoalEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;   // each goal belongs to one user

    private String title;
    private String description;
    private Integer target;
    private String unit;
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status { ACTIVE, COMPLETED, CANCELLED }
}

