package com.campusfit.fitness.service;

import com.campusfit.fitness.dto.GoalResponse;
import com.campusfit.fitness.entity.Milestone;
import com.campusfit.fitness.repository.MilestoneRepository;
import com.campusfit.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;

    public MilestoneService(MilestoneRepository milestoneRepository) {
        this.milestoneRepository = milestoneRepository;
    }

    @Transactional(readOnly = true)
    public List<GoalResponse.MilestoneResponse> getByGoalId(Long goalId) {
        return milestoneRepository.findByGoalIdOrderBySeq(goalId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public GoalResponse.MilestoneResponse markAchieved(Long milestoneId) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", milestoneId));

        milestone.setAchievedDate(LocalDate.now());
        Milestone saved = milestoneRepository.save(milestone);
        return toResponse(saved);
    }

    private GoalResponse.MilestoneResponse toResponse(Milestone m) {
        return GoalResponse.MilestoneResponse.builder()
                .id(m.getId())
                .description(m.getDescription())
                .targetValue(m.getTargetValue())
                .achievedDate(m.getAchievedDate())
                .seq(m.getSeq())
                .build();
    }
}
