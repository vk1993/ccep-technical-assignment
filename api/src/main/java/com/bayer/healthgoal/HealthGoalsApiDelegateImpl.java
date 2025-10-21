package com.bayer.healthgoal;

import com.bayer.healthgoal.api.HealthGoalsApiDelegate;
import com.bayer.healthgoal.api.model.CreateHealthGoalRequest;
import com.bayer.healthgoal.api.model.HealthGoal;
import com.bayer.healthgoal.api.model.UpdateHealthGoalRequest;
import com.bayer.healthgoal.entity.HealthGoalEntity;
import com.bayer.healthgoal.entity.UserEntity;
import com.bayer.healthgoal.exceptions.InvalidRequestException;
import com.bayer.healthgoal.exceptions.ResourceNotFoundException;
import com.bayer.healthgoal.exceptions.UserNotFoundException;
import com.bayer.healthgoal.mapper.HealthGoalMapper;
import com.bayer.healthgoal.repository.HealthGoalRepository;
import com.bayer.healthgoal.repository.UserRepository;
import com.bayer.healthgoal.utlity.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

        if (request.getUserId() == null || request.getTitle() == null) {
            throw new InvalidRequestException("userId and title are required fields");
        }

        UUID userId = UUID.fromString(request.getUserId());
        // we shouldn't use the provided user, we should pull the user details form JWT token,
        // for now just i've verified user from payload user.
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("user not found for id= " + userId));

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

        return ResponseEntity.status(HttpStatus.CREATED).body(healthGoalMapper.toDto(saved));
    }

    @Override
    public ResponseEntity<List<HealthGoal>> listHealthGoals(
            String xApiKey, String xCorrelationId, String xRequestId) {

        List<HealthGoal> goals = healthGoalRepository.findAll().stream()
                .map(healthGoalMapper::toDto)
                .collect(Collectors.toList());

        log.info("retrieved {} health goals", goals.size());
        return ResponseEntity.ok(goals);
    }

    @Override
    public ResponseEntity<HealthGoal> getHealthGoalById(
            UUID id, String xApiKey, String xCorrelationId, String xRequestId) {

        HealthGoalEntity entity = healthGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HealthGoal not found: " + id));

        return ResponseEntity.ok(healthGoalMapper.toDto(entity));
    }

    @Override
    public ResponseEntity<HealthGoal> updateHealthGoal(
            UUID id,
            String xApiKey,
            UpdateHealthGoalRequest request,
            String xCorrelationId,
            String xRequestId) {

        log.info("Updating HealthGoal | id={} | correlationId={} | requestId={}", id, xCorrelationId, xRequestId);

        if (request == null) {
            throw new InvalidRequestException("Request body cannot be null");
        }

        HealthGoalEntity entity = healthGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health goal not found for id = " + id));

        Optional.ofNullable(request.getTitle())
                .ifPresent(entity::setTitle);
        Optional.ofNullable(request.getDescription())
                .ifPresent(entity::setDescription);
        Optional.ofNullable(request.getTarget())
                .ifPresent(entity::setTarget);
        Optional.ofNullable(request.getUnit()).
                ifPresent(entity::setUnit);
        Optional.ofNullable(request.getStartDate())
                .ifPresent(date -> entity.setStartDate(Utility.toLocalDate(date)));
        Optional.ofNullable(request.getEndDate())
                .ifPresent(date -> entity.setEndDate(Utility.toLocalDate(date)));

        if (request.getStatus() != null) {
            try {
                entity.setStatus(HealthGoalEntity.Status.valueOf(request.getStatus().getValue()));
            } catch (IllegalArgumentException e) {
                throw new InvalidRequestException("Invalid status value: " + request.getStatus().getValue());
            }
        }

        HealthGoalEntity updated = healthGoalRepository.save(entity);
        log.info("Updated HealthGoal successfully | id={} | correlationId={}", updated.getId(), xCorrelationId);

        return ResponseEntity.ok(healthGoalMapper.toDto(updated));
    }

    @Override
    public ResponseEntity<Void> deleteHealthGoal(
            UUID id, String xApiKey, String xCorrelationId, String xRequestId) {

        if (!healthGoalRepository.existsById(id)) {
            log.warn("health goal not found for deletion id={}", id);
            throw new ResourceNotFoundException("Health goal not found for id = " + id);
        }

        healthGoalRepository.deleteById(id);
        log.info("deleted health goal id={}", id);
        return ResponseEntity.noContent().build();
    }
}
