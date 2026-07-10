package com.resumefit.resumefit.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ResumeHistoryDTO {

    private Long resumeId;
    private String fileName;
    private LocalDateTime uploadedAt;
    private Integer score;
    private List<String> missingSkills;
    private Long analysisResultId;

    public ResumeHistoryDTO() {
    }

    public ResumeHistoryDTO(Long resumeId, String fileName, LocalDateTime uploadedAt, Integer score, List<String> missingSkills, Long analysisResultId) {
        this.resumeId = resumeId;
        this.fileName = fileName;
        this.uploadedAt = uploadedAt;
        this.score = score;
        this.missingSkills = missingSkills;
        this.analysisResultId = analysisResultId;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public Long getAnalysisResultId() {
        return analysisResultId;
    }

    public void setAnalysisResultId(Long analysisResultId) {
        this.analysisResultId = analysisResultId;
    }
}
