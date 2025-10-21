package com.bayer.healthgoal.mapper;

import com.bayer.healthgoal.api.model.HealthGoal;
import com.bayer.healthgoal.entity.HealthGoalEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface HealthGoalMapper {

    @Mapping(target = "userId", source = "user.id")
    HealthGoal toDto(HealthGoalEntity entity);
}