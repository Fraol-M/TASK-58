package com.campusfit.masterdata.repository;

import com.campusfit.masterdata.entity.MergeOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MergeOperationRepository extends JpaRepository<MergeOperation, Long> {
}
