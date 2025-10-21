package com.bayer.healthgoal.repository;

import com.bayer.healthgoal.entity.HealthGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HealthGoalRepository extends JpaRepository<HealthGoalEntity, UUID> {
}
