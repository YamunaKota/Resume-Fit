package com.resumefit.resumefit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.resumefit.resumefit.dto.ATSRequestDTO;
import com.resumefit.resumefit.dto.ATSResponseDTO;
import com.resumefit.resumefit.dto.ResumeRequestDTO;
import com.resumefit.resumefit.dto.SkillsResponseDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIService {

        private final RestTemplate restTemplate;

    // --------------------------------
    // RESUME SKILL EXTRACTION
    // --------------------------------
    public List<String> extractResumeSkills(String resumeText) {

        String url = "http://localhost:8000/analyze-resume";

        ResumeRequestDTO request = new ResumeRequestDTO();

        request.setResume_text(resumeText);

        SkillsResponseDTO response =
                restTemplate.postForObject(
                        url,
                        request,
                        SkillsResponseDTO.class
                );

        return response.getSkills();
    }

    // --------------------------------
    // JD SKILL EXTRACTION
    // --------------------------------
    public List<String> extractJDSkills(String jdText) {

        String url = "http://localhost:8000/analyze-jd";

        Map<String, String> request = new HashMap<>();

        request.put("job_description", jdText);

        SkillsResponseDTO response =
                restTemplate.postForObject(
                        url,
                        request,
                        SkillsResponseDTO.class
                );

        return response.getSkills();
    }

    // --------------------------------
    // ATS SCORE SIMPLE
    // --------------------------------
    public ATSResponseDTO calculateATS(
            List<String> resumeSkills,
            List<String> jdSkills
    ) {

        String url = "http://localhost:8000/ats-score";

        ATSRequestDTO request = new ATSRequestDTO();

        request.setResumeSkills(resumeSkills);
        request.setJdSkills(jdSkills);

        ATSResponseDTO response =
                restTemplate.postForObject(
                        url,
                        request,
                        ATSResponseDTO.class
                );

        return response;
    }

    // --------------------------------
    // ATS SCORE SEMANTIC
    // --------------------------------

    public ATSResponseDTO semanticATS(
        List<String> resumeSkills,
        List<String> jdSkills
) {

    String url =
            "http://localhost:8000/semantic-ats-score";

    ATSRequestDTO request = new ATSRequestDTO();

    request.setResumeSkills(resumeSkills);
    request.setJdSkills(jdSkills);

    ATSResponseDTO response =
            restTemplate.postForObject(
                    url,
                    request,
                    ATSResponseDTO.class
            );

    return response;
}
}
