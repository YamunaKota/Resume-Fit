package com.resumefit.resumefit.dto;



import java.util.List;

public class ATSRequestDTO {

    private List<String> resumeSkills;
    private List<String> jdSkills;

    public List<String> getResumeSkills() {
        return resumeSkills;
    }

    public void setResumeSkills(List<String> resumeSkills) {
        this.resumeSkills = resumeSkills;
    }

    public List<String> getJdSkills() {
        return jdSkills;
    }

    public void setJdSkills(List<String> jdSkills) {
        this.jdSkills = jdSkills;
    }
}
