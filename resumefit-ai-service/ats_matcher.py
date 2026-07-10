def calculate_ats_score(resume_skills, jd_skills):

    # convert to lowercase
    print("Resume Skills:", resume_skills)
    print("JD Skills:", jd_skills)
    resume_set = set(skill.lower() for skill in resume_skills)
    jd_set = set(skill.lower() for skill in jd_skills)

    matched_skills = resume_set.intersection(jd_set)

    if len(jd_set) == 0:
        return {
            "Score": 0,
            "matchedSkills": [],
            "missingSkills": []
        }

    score = (len(matched_skills) / len(jd_set)) * 100

    missing_skills = jd_set - matched_skills

    return {
        "Score": round(score),
        "matchedSkills": list(matched_skills),
        "missingSkills": list(missing_skills)
    }


