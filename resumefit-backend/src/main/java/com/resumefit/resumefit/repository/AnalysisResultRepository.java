package com.resumefit.resumefit.repository;



import com.resumefit.resumefit.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisResultRepository
        extends JpaRepository<AnalysisResult, Long> {

        Optional<AnalysisResult> findByResume_Id(Long resumeId);
}
