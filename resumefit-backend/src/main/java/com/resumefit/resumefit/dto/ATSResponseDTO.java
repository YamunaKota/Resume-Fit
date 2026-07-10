package com.resumefit.resumefit.dto;

import java.util.List;

public class ATSResponseDTO {

    private int score;
    private List<String> missingSkills;
    private Long userId;
    private Long resumeId;
    private Long analysisResultId;
    private String fileName;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public Long getAnalysisResultId() {
        return analysisResultId;
    }

    public void setAnalysisResultId(Long analysisResultId) {
        this.analysisResultId = analysisResultId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}