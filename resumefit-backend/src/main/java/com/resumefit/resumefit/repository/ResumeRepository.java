package com.resumefit.resumefit.repository;



import com.resumefit.resumefit.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
	List<Resume> findByUser_IdOrderByUploadedAtDesc(Long userId);

	org.springframework.data.domain.Page<Resume> findByUser_Id(Long userId, org.springframework.data.domain.Pageable pageable);
}