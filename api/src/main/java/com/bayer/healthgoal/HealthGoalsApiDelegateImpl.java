package com.bayer.healthgoal;

import com.bayer.healthgoal.api.HealthGoalsApiDelegate;
import com.bayer.healthgoal.api.model.CreateHealthGoalRequest;
import com.bayer.healthgoal.api.model.HealthGoal;
import com.bayer.healthgoal.api.model.UpdateHealthGoalRequest;
import com.bayer.healthgoal.entity.HealthGoalEntity;
import com.bayer.healthgoal.entity.UserEntity;
import com.bayer.healthgoal.mapper.HealthGoalMapper;
import com.bayer.healthgoal.repository.HealthGoalRepository;
import com.bayer.healthgoal.repository.UserRepository;
import com.bayer.healthgoal.utlity.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class HealthGoalsApiDelegateImpl implements HealthGoalsApiDelegate {

    private final HealthGoalRepository healthGoalRepository;
    private final UserRepository userRepository;
    private final HealthGoalMapper healthGoalMapper;

    @Override
    public ResponseEntity<HealthGoal> createHealthGoal(
            String xApiKey,
            CreateHealthGoalRequest request,
            String xCorrelationId,
            String xRequestId) {

        log.info("createHealthGoal -> CorrelationId={} RequestId={} Payload={}",
                xCorrelationId, xRequestId, request);

        if (request.getUserId() == null || request.getTitle() == null) {
            log.error("valedation failed: userId or title missing");
            return ResponseEntity.badRequest().build();
        }

        UUID userId = UUID.fromString(request.getUserId());
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found for id= " + userId));

        HealthGoalEntity entity = HealthGoalEntity.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .target(request.getTarget())
                .unit(request.getUnit())
                .startDate(Utility.toLocalDate(request.getStartDate()))
                .endDate(Utility.toLocalDate(request.getEndDate()))
                .status(HealthGoalEntity.Status.ACTIVE)
                .build();

        HealthGoalEntity saved = healthGoalRepository.save(entity);
        log.info("health goal created successfully with ID={}", saved.getId());

        return ResponseEntity.status(201).body(healthGoalMapper.toDto(saved));
    }

    @Override
    public ResponseEntity<List<HealthGoal>> listHealthGoals(
            String xApiKey, String xCorrelationId, String xRequestId) {

        log.info("correlationId={} RequestId={}", xCorrelationId, xRequestId);

        List<HealthGoal> goals = healthGoalRepository.findAll().stream()
                .map(healthGoalMapper::toDto)
                .collect(Collectors.toList());

        log.info("retrieved {} health goals", goals.size());
        return ResponseEntity.ok(goals);
    }

    @Override
    public ResponseEntity<HealthGoal> getHealthGoalById(
            UUID id, String xApiKey, String xCorrelationId, String xRequestId) {

        log.info("id={} correlationId={} requestId={}", id, xCorrelationId, xRequestId);

        return healthGoalRepository.findById(id)
                .map(goal -> {
                    log.info("found health goal id={}", id);
                    return ResponseEntity.ok(healthGoalMapper.toDto(goal));
                })
                .orElseGet(() -> {
                    log.warn("health goal not found for id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @Override
    public ResponseEntity<HealthGoal> updateHealthGoal(
            UUID id,
            String xApiKey,
            UpdateHealthGoalRequest request,
            String xCorrelationId,
            String xRequestId) {

        log.info("id={} correlationId={} requestId={}", id, xCorrelationId, xRequestId);

        return healthGoalRepository.findById(id)
                .map(existing -> {
                    if (request.getTitle() != null) existing.setTitle(request.getTitle());
                    if (request.getDescription() != null) existing.setDescription(request.getDescription());
                    if (request.getTarget() != null) existing.setTarget(request.getTarget());
                    if (request.getUnit() != null) existing.setUnit(request.getUnit());
                    if (request.getStartDate() != null) existing.setStartDate(Utility.toLocalDate(request.getStartDate()));
                    if (request.getEndDate() != null) existing.setEndDate(Utility.toLocalDate(request.getEndDate()));
                    if (request.getStatus() != null) {
                        existing.setStatus(HealthGoalEntity.Status.valueOf(request.getStatus().getValue()));
                    }

                    HealthGoalEntity updated = healthGoalRepository.save(existing);
                    log.info("updated health goal id={}", updated.getId());
                    return ResponseEntity.ok(healthGoalMapper.toDto(updated));
                })
                .orElseGet(() -> {
                    log.warn("health goal not found for update id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @Override
    public ResponseEntity<Void> deleteHealthGoal(
            UUID id, String xApiKey, String xCorrelationId, String xRequestId) {

        log.info("id={} correlationId={} requestId={}", id, xCorrelationId, xRequestId);

        if (!healthGoalRepository.existsById(id)) {
            log.warn("health goal not found for deletion id={}", id);
            return ResponseEntity.notFound().build();
        }

        healthGoalRepository.deleteById(id);
        log.info("deleted health goal id={}", id);
        return ResponseEntity.noContent().build();
    }
}
