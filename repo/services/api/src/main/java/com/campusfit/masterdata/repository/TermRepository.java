package com.campusfit.masterdata.repository;

import com.campusfit.masterdata.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TermRepository extends JpaRepository<Term, Long> {
    Optional<Term> findByCode(String code);
    boolean existsByCode(String code);
}
