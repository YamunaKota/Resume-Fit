from fastapi import FastAPI
from pydantic import BaseModel

from resume_parser import extract_skills_from_resume
from jd_parser import extract_skills_from_jd
from ats_matcher import calculate_ats_score
from semantic_matcher import semantic_ats_score

app = FastAPI()


# -----------------------------
# JD ANALYZER
# -----------------------------
@app.post("/analyze-jd")
def analyze_jd(data: dict):

    jd_text = data.get("job_description", "")

    print("JD TEXT:", jd_text)

    skills = extract_skills_from_jd(jd_text)

    print("EXTRACTED JD SKILLS:", skills)

    return {
        "skills": skills
    }


# -----------------------------
# RESUME ANALYZER
# -----------------------------
@app.post("/analyze-resume")
def analyze_resume(data: dict):

    resume_text = data["resume_text"]

    skills = extract_skills_from_resume(resume_text)

    return {
        "skills": skills
    }


# -----------------------------
# ATS REQUEST MODEL
# -----------------------------
class ATSRequest(BaseModel):
    resumeSkills: list[str]
    jdSkills: list[str]


# -----------------------------
# ATS SCORE ENDPOINT
# -----------------------------
@app.post("/ats-score")
def ats_score(data: ATSRequest):

    result = calculate_ats_score(
        data.resumeSkills,
        data.jdSkills
    )

    return result



@app.post("/semantic-ats-score")
def semantic_score(data: ATSRequest):

    result = semantic_ats_score(
        data.resumeSkills,
        data.jdSkills
    )

    return result