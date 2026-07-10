package com.resumefit.resumefit.service;

import com.resumefit.resumefit.dto.ATSAnalyzeRequestDTO;
import com.resumefit.resumefit.dto.ATSResponseDTO;
import com.resumefit.resumefit.entity.AnalysisResult;
import com.resumefit.resumefit.entity.Resume;
import com.resumefit.resumefit.entity.User;
import com.resumefit.resumefit.repository.AnalysisResultRepository;
import com.resumefit.resumefit.repository.ResumeRepository;
import com.resumefit.resumefit.repository.UserRepository;
import com.resumefit.resumefit.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeAnalysisService {

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final AIService aiService;
    private final JwtUtil jwtUtil;

    @Transactional
    public ATSResponseDTO analyzeAndStore(ATSAnalyzeRequestDTO request, String authorization) {
        String resumeText = request.getResumeText();
        String jdText = request.getJdText();

        if (!StringUtils.hasText(resumeText) || !StringUtils.hasText(jdText)) {
            throw new IllegalArgumentException("Both resumeText and jdText are required.");
        }

        User user = resolveUser(request, authorization);

        if (StringUtils.hasText(request.getUserName())) {
            user.setName(request.getUserName().trim());
        }

        if (StringUtils.hasText(request.getUserEmail())) {
            user.setEmail(request.getUserEmail().trim());
        }

        user = userRepository.save(user);

        Resume resume = new Resume(user, resolveFileName(request.getFileName()), resumeText.trim());
        resume = resumeRepository.save(resume);

        List<String> resumeSkills = aiService.extractResumeSkills(resumeText);
        List<String> jdSkills = aiService.extractJDSkills(jdText);
        ATSResponseDTO atsResponse = aiService.semanticATS(resumeSkills, jdSkills);

        AnalysisResult analysisResult = new AnalysisResult();
        analysisResult.setResume(resume);
        analysisResult.setAtsScore((double) atsResponse.getScore());

        List<String> missingSkills = atsResponse.getMissingSkills();
        analysisResult.setMissingSkills(missingSkills == null ? "[]" : missingSkills.toString());
        analysisResult.setSuggestions(
                missingSkills == null || missingSkills.isEmpty()
                        ? "No major gaps detected from the extracted resume and job description text."
                        : "Focus on: " + String.join(", ", missingSkills)
        );

        analysisResult = analysisResultRepository.save(analysisResult);

        resume.setAnalysisResult(analysisResult);

        atsResponse.setUserId(user.getId());
        atsResponse.setResumeId(resume.getId());
        atsResponse.setAnalysisResultId(analysisResult.getId());
        atsResponse.setFileName(resume.getFileName());

        return atsResponse;
    }

    public java.util.List<com.resumefit.resumefit.dto.ResumeHistoryDTO> getUserHistory(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header with Bearer token is required");
        }

        String raw = authorization.replace("Bearer ", "").trim();
        String email = extractEmailFromToken(raw);

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        var resumes = resumeRepository.findByUser_IdOrderByUploadedAtDesc(user.getId());

        java.util.List<com.resumefit.resumefit.dto.ResumeHistoryDTO> out = new java.util.ArrayList<>();

        for (var r : resumes) {
            var opt = analysisResultRepository.findByResume_Id(r.getId());
            Integer score = null;
            java.util.List<String> missing = null;
            Long analysisId = null;
            if (opt.isPresent()) {
                var ar = opt.get();
                score = ar.getAtsScore() == null ? null : ar.getAtsScore().intValue();
                // stored as string (e.g. [skill1, skill2])
                if (ar.getMissingSkills() != null) {
                    String s = ar.getMissingSkills().trim();
                    s = s.replaceAll("^\\[|\\]$", "");
                    if (s.isEmpty()) {
                        missing = java.util.List.of();
                    } else {
                        missing = java.util.Arrays.asList(s.split(",\\s*"));
                    }
                }
                analysisId = ar.getId();
            }

            out.add(new com.resumefit.resumefit.dto.ResumeHistoryDTO(
                    r.getId(), r.getFileName(), r.getUploadedAt(), score, missing, analysisId
            ));
        }

        return out;
    }

    public org.springframework.data.domain.Page<com.resumefit.resumefit.dto.ResumeHistoryDTO> getUserHistoryPaged(String authorization, int page, int size) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header with Bearer token is required");
        }

        String raw = authorization.replace("Bearer ", "").trim();
        String email = extractEmailFromToken(raw);

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        var pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("uploadedAt").descending());

        var pageResumes = resumeRepository.findByUser_Id(user.getId(), pageable);

        var dtoList = pageResumes.map(r -> {
            var opt = analysisResultRepository.findByResume_Id(r.getId());
            Integer score = null;
            java.util.List<String> missing = null;
            Long analysisId = null;
            if (opt.isPresent()) {
                var ar = opt.get();
                score = ar.getAtsScore() == null ? null : ar.getAtsScore().intValue();
                if (ar.getMissingSkills() != null) {
                    String s = ar.getMissingSkills().trim();
                    s = s.replaceAll("^\\[|\\]$", "");
                    if (s.isEmpty()) {
                        missing = java.util.List.of();
                    } else {
                        missing = java.util.Arrays.asList(s.split(",\\s*"));
                    }
                }
                analysisId = ar.getId();
            }

            return new com.resumefit.resumefit.dto.ResumeHistoryDTO(
                    r.getId(), r.getFileName(), r.getUploadedAt(), score, missing, analysisId
            );
        });

        return dtoList;
    }

    private User resolveUser(ATSAnalyzeRequestDTO request, String authorization) {
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            String rawToken = authorization.replace("Bearer ", "").trim();
            String email = extractEmailFromToken(rawToken);

            if (StringUtils.hasText(email)) {
                return userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
            }
        }

        if (request.getUserId() != null) {
            return userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found for id: " + request.getUserId()));
        }

        if (StringUtils.hasText(request.getUserEmail())) {
            return userRepository.findByEmail(request.getUserEmail().trim())
                    .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + request.getUserEmail()));
        }

        throw new IllegalArgumentException("User identity is required to save resume analysis data.");
    }

    private String resolveFileName(String fileName) {
        if (StringUtils.hasText(fileName)) {
            return StringUtils.cleanPath(fileName.trim());
        }

        return "resume.pdf";
    }

    private String extractEmailFromToken(String token) {
        return jwtUtil.extractEmail(token);
    }
}