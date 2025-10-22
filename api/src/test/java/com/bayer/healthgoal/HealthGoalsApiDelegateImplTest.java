package com.bayer.healthgoal;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthGoalsApiDelegateImplTest {

    @Mock
    private HealthGoalRepository healthGoalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HealthGoalMapper healthGoalMapper;

    @InjectMocks
    private HealthGoalsApiDelegateImpl delegate;
    private UUID userId;
    private UserEntity userEntity;
    private HealthGoalEntity goalEntity;
    private HealthGoal goalDto;


    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();

        userEntity = UserEntity.builder()
                .id(userId)
                .username("visal")
                .email("visal@zohomail.in")
                .build();

        goalEntity = HealthGoalEntity.builder()
                .id(UUID.randomUUID())
                .user(userEntity)
                .title("Lose Weight")
                .description("Target to lose 5 kg in 3 months")
                .target(5)
                .unit("kg")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .status(HealthGoalEntity.Status.ACTIVE)
                .build();

        goalDto = new HealthGoal()
                .id(goalEntity.getId())
                .userId(userEntity.getId().toString())
                .title(goalEntity.getTitle())
                .description(goalEntity.getDescription())
                .target(goalEntity.getTarget())
                .unit(goalEntity.getUnit());
    }

    @Test
    void testCreateHealthGoal_Success() {
        CreateHealthGoalRequest request = new CreateHealthGoalRequest()
                .userId(userId.toString())
                .title("Lose Weight")
                .description("Target to lose 5 kg in 3 months")
                .target(5)
                .unit("kg");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(healthGoalRepository.save(any())).thenReturn(goalEntity);
        when(healthGoalMapper.toDto(goalEntity)).thenReturn(goalDto);

        ResponseEntity<HealthGoal> response = delegate.createHealthGoal("api_key", request, "corr", "req");

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Lose Weight", response.getBody().getTitle());
        verify(healthGoalRepository).save(any(HealthGoalEntity.class));
    }

    @Test
    void testCreateHealthGoal_InvalidInput_ThrowsException() {
        CreateHealthGoalRequest request = new CreateHealthGoalRequest();
        assertThrows(InvalidRequestException.class, () ->
                delegate.createHealthGoal("api_key", request, "corr", "req"));
    }

    @Test
    void testCreateHealthGoal_UserNotFound_ThrowsException() {
        CreateHealthGoalRequest request = new CreateHealthGoalRequest()
                .userId(userId.toString())
                .title("Lose Weight");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                delegate.createHealthGoal("api_key", request, "corr", "req"));
    }

    @Test
    void testListHealthGoals_ReturnsList() {
        when(healthGoalRepository.findAll()).thenReturn(List.of(goalEntity));
        when(healthGoalMapper.toDto(goalEntity)).thenReturn(goalDto);

        ResponseEntity<List<HealthGoal>> response = delegate.listHealthGoals("api_key", "corr", "req");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetHealthGoalById_Success() {
        when(healthGoalRepository.findById(goalEntity.getId())).thenReturn(Optional.of(goalEntity));
        when(healthGoalMapper.toDto(goalEntity)).thenReturn(goalDto);

        ResponseEntity<HealthGoal> response = delegate.getHealthGoalById(goalEntity.getId(), "api", "corr", "req");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Lose Weight", response.getBody().getTitle());
    }

    @Test
    void testGetHealthGoalById_NotFound_ThrowsException() {
        UUID id = UUID.randomUUID();
        when(healthGoalRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () ->
                delegate.getHealthGoalById(id, "api", "corr", "req"));
    }

    @Test
    void testUpdateHealthGoal_Success() {
        UUID goalId = goalEntity.getId();
        UpdateHealthGoalRequest request = new UpdateHealthGoalRequest()
                .title("Updated Title")
                .description("Updated Description");

        when(healthGoalRepository.findById(goalId)).thenReturn(Optional.of(goalEntity));
        when(healthGoalRepository.save(any())).thenReturn(goalEntity);
        when(healthGoalMapper.toDto(goalEntity)).thenReturn(goalDto);

        ResponseEntity<HealthGoal> response = delegate.updateHealthGoal(goalId, "api", request, "corr", "req");

        assertEquals(200, response.getStatusCodeValue());
        verify(healthGoalRepository).save(any(HealthGoalEntity.class));
    }

    @Test
    void testUpdateHealthGoal_NotFound_ThrowsException() {
        UUID id = UUID.randomUUID();
        UpdateHealthGoalRequest request = new UpdateHealthGoalRequest().title("Test");

        when(healthGoalRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                delegate.updateHealthGoal(id, "api", request, "corr", "req"));
    }

    @Test
    void testDeleteHealthGoal_Success() {
        when(healthGoalRepository.existsById(goalEntity.getId())).thenReturn(true);

        ResponseEntity<Void> response = delegate.deleteHealthGoal(goalEntity.getId(), "api", "corr", "req");

        assertEquals(204, response.getStatusCodeValue());
        verify(healthGoalRepository).deleteById(goalEntity.getId());
    }

    @Test
    void testDeleteHealthGoal_NotFound_ThrowsException() {
        when(healthGoalRepository.existsById(goalEntity.getId())).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () ->
                delegate.deleteHealthGoal(goalEntity.getId(), "api", "corr", "req"));
    }


}