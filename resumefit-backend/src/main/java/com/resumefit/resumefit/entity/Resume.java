package com.resumefit.resumefit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @OneToOne(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private AnalysisResult analysisResult;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "resume_text", columnDefinition = "TEXT")
    private String resumeText;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    // Constructor
    public Resume() {
    }

    public Resume(User user, String fileName, String resumeText) {
        this.user = user;
        this.fileName = fileName;
        this.resumeText = resumeText;
        this.uploadedAt = LocalDateTime.now();
    }

    // Automatically sets upload time
    @PrePersist
    protected void onUpload() {
        uploadedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public AnalysisResult getAnalysisResult() {
        return analysisResult;
    }

    public void setAnalysisResult(AnalysisResult analysisResult) {
        this.analysisResult = analysisResult;
        if (analysisResult != null && analysisResult.getResume() != this) {
            analysisResult.setResume(this);
        }
    }
}
