package com.campusfit.inbound.service;

import com.campusfit.inbound.entity.InboundLine;
import com.campusfit.inbound.entity.PutawayTask;
import com.campusfit.inbound.repository.InboundLineRepository;
import com.campusfit.inbound.repository.PutawayTaskRepository;
import com.campusfit.shared.exception.BusinessException;
import com.campusfit.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PutawayService {

    private final PutawayTaskRepository putawayTaskRepository;
    private final InboundLineRepository lineRepository;

    public PutawayService(PutawayTaskRepository putawayTaskRepository,
                          InboundLineRepository lineRepository) {
        this.putawayTaskRepository = putawayTaskRepository;
        this.lineRepository = lineRepository;
    }

    @Transactional
    public List<PutawayTask> generateTasks(Long receiptId) {
        // Idempotent: return existing tasks if already generated
        List<PutawayTask> existing = putawayTaskRepository.findByReceiptId(receiptId);
        if (!existing.isEmpty()) {
            return existing;
        }

        List<InboundLine> lines = lineRepository.findByReceiptId(receiptId);
        List<PutawayTask> tasks = new ArrayList<>();

        for (InboundLine line : lines) {
            if (line.getInspectionResult() == InboundLine.InspectionResult.PASS) {
                PutawayTask task = PutawayTask.builder()
                        .receiptId(receiptId)
                        .lineId(line.getId())
                        .suggestedLocation("ZONE-A-" + line.getItemCode())
                        .status(PutawayTask.TaskStatus.PENDING)
                        .build();
                tasks.add(task);
            }
        }

        return putawayTaskRepository.saveAll(tasks);
    }

    @Transactional
    public PutawayTask completeTask(Long receiptId, Long taskId, Long userId, String actualLocation) {
        PutawayTask task = putawayTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("PutawayTask", taskId));

        if (!task.getReceiptId().equals(receiptId)) {
            throw new BusinessException("Task does not belong to the specified receipt");
        }

        if (task.getStatus() != PutawayTask.TaskStatus.PENDING) {
            throw new BusinessException("Task is not in PENDING status");
        }

        task.setStatus(PutawayTask.TaskStatus.COMPLETED);
        task.setCompletedBy(userId);
        task.setCompletedAt(LocalDateTime.now());
        task.setActualLocation(actualLocation != null ? actualLocation : task.getSuggestedLocation());

        return putawayTaskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<PutawayTask> getByReceiptId(Long receiptId) {
        return putawayTaskRepository.findByReceiptId(receiptId);
    }
}
