from sentence_transformers import SentenceTransformer, util

model = SentenceTransformer('all-MiniLM-L6-v2')

SKILL_ALIASES = {
    "ml": "machine learning",
    "ai": "artificial intelligence",
    "js": "javascript",
    "javascript": "javascript",
    "ts": "typescript",
    "typescript": "typescript",
    "node": "node.js",
    "nodejs": "node.js",
    "react": "react",
    "react.js": "react",
    "reactjs": "react",
    "py": "python",
    "python3": "python",
    "html": "html",
    "css": "css",
    "sql": "sql",
    "db": "sql",
    "postgres": "postgresql",
    "postgresql": "postgresql",
    "mongo": "mongodb",
    "docker": "docker",
    "k8s": "kubernetes",
    "kubectl": "kubernetes",
    "spring": "spring boot",
    "springboot": "spring boot",
    "java script": "javascript",
    "html5": "html",
    "css3": "css",
    "reactjs": "react",
    "c#": "c#",
    "csharp": "c#",
    "dl": "deep learning",
    "nlp": "natural language processing"
}


def normalize_skill(skill):
    return SKILL_ALIASES.get(skill.lower().strip(), skill.lower().strip())


def semantic_ats_score(resume_skills, jd_skills):

    print("Resume Skills:", resume_skills)
    print("JD Skills:", jd_skills)

    if not jd_skills:
        return {
            "score": 0,
            "matchedSkills": [],
            "missingSkills": [],
            "message": "No JD skills found"
        }

    # -----------------------------
    # Normalize skills
    # -----------------------------
    normalized_resume = [normalize_skill(s) for s in resume_skills]
    normalized_jd = [normalize_skill(s) for s in jd_skills]

    # -----------------------------
    # Embeddings
    # -----------------------------
    resume_embeddings = model.encode(normalized_resume, convert_to_tensor=True)
    jd_embeddings = model.encode(normalized_jd, convert_to_tensor=True)

    matched_skills = []

    # -----------------------------
    # Matching
    # -----------------------------
    for i, jd_emb in enumerate(jd_embeddings):
        for j, res_emb in enumerate(resume_embeddings):

            similarity = util.cos_sim(jd_emb, res_emb).item()

            print(
                f"{normalized_jd[i]} ↔ {normalized_resume[j]} = {similarity}"
            )

            if similarity >= 0.7:
                matched_skills.append(normalized_jd[i])
                break

    # -----------------------------
    # Remove duplicates
    # -----------------------------
    matched_skills = list(set(matched_skills))

    # -----------------------------
    # Score
    # -----------------------------
    score = (len(matched_skills) / len(jd_skills)) * 100

    # -----------------------------
    # Missing skills
    # -----------------------------
    matched_set = set(matched_skills)

    missing_skills = [
        jd_skills[i]
        for i in range(len(jd_skills))
        if normalize_skill(jd_skills[i]) not in matched_set
    ]

    return {
        "score": round(score),
        "matchedSkills": matched_skills,
        "missingSkills": missing_skills
    }