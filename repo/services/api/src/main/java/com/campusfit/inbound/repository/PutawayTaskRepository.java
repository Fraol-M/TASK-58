package com.campusfit.inbound.repository;

import com.campusfit.inbound.entity.PutawayTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PutawayTaskRepository extends JpaRepository<PutawayTask, Long> {

    List<PutawayTask> findByReceiptId(Long receiptId);

    List<PutawayTask> findByReceiptIdAndStatus(Long receiptId, PutawayTask.TaskStatus status);
}
