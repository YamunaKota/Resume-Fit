# ResumeFit AI Service - Detailed Technical Guide

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [1. skills.py - The Skill Dictionary](#1-skillspy---the-skill-dictionary)
3. [2. resume_parser.py - Extracting Skills from Resume](#2-resume_parserpy---extracting-skills-from-resume)
4. [3. jd_parser.py - Extracting Skills from Job Description](#3-jd_parserpy---extracting-skills-from-job-description)
5. [4. ats_matcher.py - Boolean/Exact Matching](#4-ats_matcherpy---booleanexact-matching)
6. [5. semantic_matcher.py - Embedding-Based Matching](#5-semantic_matcherpy---embedding-based-matching)
7. [6. main.py - FastAPI Server & Endpoints](#6-mainpy---fastapi-server--endpoints)
8. [Flow Diagrams](#flow-diagrams)

---

## Project Overview

The ResumeFit AI Service is a **skill-matching engine** that:
- Extracts skills from resumes and job descriptions
- Compares them using two algorithms:
  - **ATS Matcher**: Exact keyword matching (Boolean)
  - **Semantic Matcher**: Embedding-based similarity (Neural Network)
- Exposes REST endpoints for the backend to call

**Data Flow:**
```
Resume Text / JD Text
    ↓
Parse & Extract Skills
    ↓
Compare (ATS vs Semantic)
    ↓
Return Score + Matched/Missing Skills
```

---

## 1. skills.py - The Skill Dictionary

### What It Does
Maintains a **curated list of recognized technical skills**. This is the "vocabulary" that the system knows about.

### The Code
```python
SKILLS = [
    "React", "Angular", "Java", "Spring Boot", "AWS", "Docker",
    "Kubernetes", "Python", "Machine Learning", "ML", "Artificial Intelligence", "AI",
    "Deep Learning", "DL", "NLP", "SQL", "MongoDB", "PostgreSQL"
]

WEB_SKILLS = [
    "HTML", "CSS", "JavaScript", "JS", "Node.js", "React.js"
]

SKILLS = SKILLS + WEB_SKILLS  # Merge both lists
```

### How It Works
- It's just a **list of skill names** the system recognizes
- If a skill appears in a resume/JD, it can be detected
- If a skill is NOT in this list, it will be **ignored**

### Example
```
SKILLS = ["Python", "Java", "React", "Docker"]

Resume mentions: "Python, JavaScript, React"
Extracted: ["Python", "React"]  ← JavaScript is ignored (not in SKILLS)
```

### Key Insight
- **Limitation**: If you have a skill like "Kubernetes" but write "k8s" in resume, it won't match
- **Solution**: Use alias mapping (see semantic_matcher.py)

---

## 2. resume_parser.py - Extracting Skills from Resume

### What It Does
Scans a resume text and finds all **skill keywords** by comparing against the `SKILLS` list.

### The Code Breakdown

```python
import re
import spacy
from skills import SKILLS

nlp = spacy.load("en_core_web_sm")  # Load a small NLP model

def extract_skills_from_resume(resume_text):
    found_skills = []
    
    # Step 1: Clean the text
    cleaned_text = re.sub(r'[^a-zA-Z0-9+#.\s]', ' ', resume_text)
    # Remove special chars except +, #, . (common in tech skills)
    # Example: "C++" stays as "C++", but "C+++" becomes "C++"
    
    cleaned_text = cleaned_text.lower()
    # Convert to lowercase for matching
    
    # Step 2: Process with spaCy (not really used here, but kept for future)
    doc = nlp(cleaned_text)
    text = doc.text
    
    # Step 3: Search for each skill
    for skill in SKILLS:
        pattern = r'\b' + re.escape(skill.lower()) + r'\b'
        # \b = word boundary (ensures exact word match)
        
        if re.search(pattern, text):
            found_skills.append(skill)
    
    return found_skills
```

### Step-by-Step Example

**Input Resume:**
```
Skills:
- Python (5 years)
- JavaScript, React
- AWS & Docker
- C++ for embedded systems
```

**Step 1: Clean & Lowercase**
```
"skills python 5 years javascript react aws docker c for embedded systems"
```

**Step 2: Search**
```
Does it contain "\bpython\b" (word-boundary)?     YES → Add "Python"
Does it contain "\breact\b" (word-boundary)?      YES → Add "React"
Does it contain "\baws\b" (word-boundary)?        YES → Add "AWS"
Does it contain "\bdocker\b" (word-boundary)?     YES → Add "Docker"
Does it contain "\bc\+\+\b" (word-boundary)?      NO  → Not added
```

**Output:**
```python
["Python", "React", "AWS", "Docker"]
```

### Key Concepts Explained

#### What is `\b` (Word Boundary)?
Ensures we match complete words, not partial matches.

```
Resume mentions: "Reactive" (a pattern library)
Without \b: Would match "React" ✗ (FALSE POSITIVE)
With \b: Doesn't match "React" ✓ (CORRECT - it's "Reactive", not "React")
```

#### Why Clean Text First?
```
Resume has: "C++"
Without cleaning: "C++" stays (good for some skills)
Special chars can break regex, so we remove them first
```

### Limitations
- ❌ Doesn't catch synonyms: "JS" won't match "JavaScript"
- ❌ Doesn't understand context: "Python development" recognized, but "knew Python 10 years ago" also recognized equally
- ❌ Miss partial matches: "sql" won't match "SQL"
- ✅ Fast and deterministic

---

## 3. jd_parser.py - Extracting Skills from Job Description

### What It Does
Same as `resume_parser.py` but for job descriptions. **Identical logic.**

### The Code
```python
import re
from skills import SKILLS

def extract_skills_from_jd(job_description):
    found_skills = []
    job_description = job_description.lower()
    
    for skill in SKILLS:
        pattern = r'\b' + re.escape(skill.lower()) + r'\b'
        if re.search(pattern, job_description):
            found_skills.append(skill)
    
    return found_skills
```

### Example

**Job Description:**
```
We are looking for a Senior Developer with:
- 5+ years Python experience
- Strong React.js skills
- AWS deployment knowledge
- Docker and Kubernetes
- SQL or NoSQL (MongoDB preferred)
```

**Extracted Skills:**
```python
["Python", "React", "AWS", "Docker", "Kubernetes", "MongoDB", "SQL"]
```

Note: "React.js" becomes "React" after lowercase and regex matching.

---

## 4. ats_matcher.py - Boolean/Exact Matching

### What It Does
Compares resume skills and JD skills using **simple set intersection** (Boolean logic).
- Checks if resume skill **exactly matches** any JD skill
- Calculates match percentage

### The Code Breakdown

```python
def calculate_ats_score(resume_skills, jd_skills):
    # Convert both to sets of lowercase strings
    resume_set = set(skill.lower() for skill in resume_skills)
    jd_set = set(skill.lower() for skill in jd_skills)
    
    # Find common skills (intersection)
    matched_skills = resume_set.intersection(jd_set)
    
    if len(jd_set) == 0:
        return {"Score": 0, "matchedSkills": [], "missingSkills": []}
    
    # Calculate percentage
    score = (len(matched_skills) / len(jd_set)) * 100
    
    # Find missing skills (what's in JD but not resume)
    missing_skills = jd_set - matched_skills
    
    return {
        "Score": round(score),
        "matchedSkills": list(matched_skills),
        "missingSkills": list(missing_skills)
    }
```

### Example Walkthrough

**Resume Skills:**
```
["Python", "Docker", "AWS", "React"]
```

**JD Skills:**
```
["Python", "React", "Kubernetes", "SQL", "Docker"]
```

**Step 1: Convert to Sets (lowercase)**
```
resume_set = {"python", "docker", "aws", "react"}
jd_set = {"python", "react", "kubernetes", "sql", "docker"}
```

**Step 2: Find Intersection**
```
matched_skills = resume_set ∩ jd_set
              = {"python", "react", "docker"}
```

**Step 3: Calculate Score**
```
score = (3 matched / 5 required) × 100 = 60%
```

**Step 4: Find Missing Skills**
```
missing_skills = jd_set - matched_skills
              = {"kubernetes", "sql"}
```

**Output:**
```json
{
  "Score": 60,
  "matchedSkills": ["python", "react", "docker"],
  "missingSkills": ["kubernetes", "sql"]
}
```

### How It Works Conceptually
Think of it like **checkbox matching**:
- JD requires: Python ☑, React ☑, Kubernetes ☐, SQL ☐, Docker ☑
- Resume has: Python ☑, React ☑, Kubernetes ☐, SQL ☐, Docker ☑
- Score = 3/5 = 60%

### Limitations
- ❌ "Python" and "python3" are treated as different (even though they're the same)
- ❌ "Node.js" and "NodeJS" won't match
- ❌ No fuzzy matching: typos break matching
- ✅ Very fast (simple set math)

---

## 5. semantic_matcher.py - Embedding-Based Matching

### What It Does
Uses **AI embeddings** to find semantic similarity between skills, even if they're worded differently.

### The Model: Sentence-Transformers (`all-MiniLM-L6-v2`)

This is a pre-trained neural network that converts text into **numerical vectors** (embeddings).

```python
from sentence_transformers import SentenceTransformer, util

model = SentenceTransformer('all-MiniLM-L6-v2')
# A small, fast model (384-dimensional embeddings)
# Pre-trained on millions of sentence pairs to understand semantic similarity
```

### Concept: What Are Embeddings?

**Embeddings** = converting words/sentences into **vectors of numbers** that capture meaning.

**Example:**
```
"machine learning" → [0.21, -0.45, 0.87, ..., 0.12]  (384 numbers)
"ml"               → [0.20, -0.44, 0.86, ..., 0.11]   (very similar!)
"python"           → [0.01, 0.99, -0.22, ..., 0.45]   (different!)
```

**Key Insight**: Similar meanings → similar vectors.

---

### Concept: Cosine Similarity

**What is Cosine Similarity?**

It measures the **angle between two vectors**. If vectors point in the same direction, they're similar.

**Formula:**
```
Similarity = (A · B) / (||A|| × ||B||)
           = (dot product) / (magnitude A × magnitude B)

Range: -1 to 1 (typically 0 to 1 for text)
- 1.0 = identical direction (very similar)
- 0.5 = 60° angle (somewhat similar)
- 0.0 = 90° angle (completely different)
```

**Visual Example:**
```
           Vector A (Python)
           /
          /60°
         /         → Cosine similarity ≈ 0.5 (somewhat similar)
        /
Vector B (Coding)

vs.

           Vector A (Machine Learning)
           |
           |  5°
           | /     → Cosine similarity ≈ 0.996 (very similar)
           |/
Vector B (Deep Learning)
```

---

### Skill Aliases: Normalizing Abbreviations

Before comparing, the code normalizes common abbreviations:

```python
SKILL_ALIASES = {
    "ml": "machine learning",
    "ai": "artificial intelligence",
    "js": "javascript",
    "node": "node.js",
    "py": "python",
    "k8s": "kubernetes",
    "c#": "c#",
    "dl": "deep learning",
    "nlp": "natural language processing"
}

def normalize_skill(skill):
    return SKILL_ALIASES.get(skill.lower().strip(), skill.lower().strip())
```

**Example:**
```
normalize_skill("ml")  → "machine learning"
normalize_skill("ML")  → "machine learning"  (lowercase + alias)
normalize_skill("Python") → "python"  (not in alias, but lowercased)
```

---

### Step-by-Step Matching Example

**Resume Skills (raw):**
```
["python", "ml", "node"]
```

**JD Skills (raw):**
```
["machine learning", "nodejs", "Python"]
```

**Step 1: Normalize Skills**
```
Resume (normalized):
  "python"      → normalize_skill("python") = "python"
  "ml"          → normalize_skill("ml") = "machine learning"
  "node"        → normalize_skill("node") = "node.js"

Result: ["python", "machine learning", "node.js"]

JD (normalized):
  "machine learning" → "machine learning"
  "nodejs"           → "nodejs"  (not in alias, stays as is)
  "python"           → "python"

Result: ["machine learning", "nodejs", "python"]
```

**Step 2: Generate Embeddings**
```
model.encode(["python", "machine learning", "node.js"])
→ [
    [0.12, -0.34, 0.78, ..., 0.21],  # python embedding (384 dims)
    [0.11, -0.32, 0.79, ..., 0.20],  # machine learning embedding
    [0.05, 0.22, -0.15, ..., 0.45]   # node.js embedding
  ]

model.encode(["machine learning", "nodejs", "python"])
→ [
    [0.11, -0.32, 0.79, ..., 0.20],  # machine learning
    [0.06, 0.21, -0.14, ..., 0.44],  # nodejs (different from "node.js")
    [0.12, -0.34, 0.78, ..., 0.21]   # python
  ]
```

**Step 3: Compute Pairwise Cosine Similarities**
```
For each JD skill, find if ANY resume skill matches:

JD Skill 1: "machine learning"
  vs Resume skill 1 ("python"):           cosine_sim = 0.45 ✗ (< 0.7 threshold)
  vs Resume skill 2 ("machine learning"): cosine_sim = 0.98 ✓ (>= 0.7 threshold) → MATCH!
  (stop searching after first match)

JD Skill 2: "nodejs"
  vs Resume skill 1 ("python"):           cosine_sim = 0.22 ✗
  vs Resume skill 2 ("machine learning"): cosine_sim = 0.19 ✗
  vs Resume skill 3 ("node.js"):          cosine_sim = 0.92 ✓ → MATCH!

JD Skill 3: "python"
  vs Resume skill 1 ("python"):           cosine_sim = 0.99 ✓ → MATCH!
```

**Step 4: Calculate Score**
```
Matched: ["machine learning", "nodejs", "python"]
Total JD skills: 3

Score = (3 / 3) × 100 = 100%
```

**Output:**
```json
{
  "score": 100,
  "matchedSkills": ["machine learning", "nodejs", "python"],
  "missingSkills": []
}
```

---

### The Code in Full Context

```python
def semantic_ats_score(resume_skills, jd_skills):
    # Edge case: no JD skills
    if not jd_skills:
        return {
            "score": 0,
            "matchedSkills": [],
            "missingSkills": [],
            "message": "No JD skills found"
        }

    # Normalize skills
    normalized_resume = [normalize_skill(s) for s in resume_skills]
    normalized_jd = [normalize_skill(s) for s in jd_skills]

    # Generate embeddings (convert text → vectors)
    resume_embeddings = model.encode(normalized_resume, convert_to_tensor=True)
    jd_embeddings = model.encode(normalized_jd, convert_to_tensor=True)

    matched_skills = []

    # For each JD skill, find if it matches a resume skill
    for i, jd_emb in enumerate(jd_embeddings):
        for j, res_emb in enumerate(resume_embeddings):
            # Compute cosine similarity
            similarity = util.cos_sim(jd_emb, res_emb).item()

            if similarity >= 0.7:  # Threshold: 70% similar
                matched_skills.append(normalized_jd[i])
                break  # Stop after first match

    # Remove duplicate matches
    matched_skills = list(set(matched_skills))

    # Calculate score
    score = (len(matched_skills) / len(jd_skills)) * 100

    # Find missing skills
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
```

### Key Threshold: 0.7 (70% Similarity)

```
similarity >= 0.7 → considered a MATCH
similarity < 0.7  → NOT considered a match
```

**Why 0.7?**
- Tuned experimentally to balance false positives vs false negatives
- 0.7 = ~45° angle between vectors (similar but not identical)
- Can be adjusted: increase for stricter matching, decrease for lenient matching

---

### Advantages vs ATS Matcher

| Feature | ATS Matcher | Semantic Matcher |
|---------|-------------|------------------|
| Speed | ⚡ Very fast | 🐢 Slower (neural network) |
| Synonyms | ❌ "ML" ≠ "Machine Learning" | ✅ "ML" ≈ "Machine Learning" |
| Typos | ❌ "Pythno" ≠ "Python" | ✅ "Pythno" ≈ "Python" (somewhat) |
| Accuracy | ⭐⭐ Basic | ⭐⭐⭐⭐ Good |

---

## 6. main.py - FastAPI Server & Endpoints

### What It Does
Exposes HTTP endpoints that the backend calls to:
1. Extract skills from resume
2. Extract skills from JD
3. Calculate ATS score
4. Calculate semantic ATS score

### The Code

```python
from fastapi import FastAPI
from pydantic import BaseModel
from resume_parser import extract_skills_from_resume
from jd_parser import extract_skills_from_jd
from ats_matcher import calculate_ats_score
from semantic_matcher import semantic_ats_score

app = FastAPI()

# Endpoint 1: Extract skills from a resume
@app.post("/analyze-resume")
def analyze_resume(data: dict):
    resume_text = data["resume_text"]
    skills = extract_skills_from_resume(resume_text)
    return {"skills": skills}

# Endpoint 2: Extract skills from a job description
@app.post("/analyze-jd")
def analyze_jd(data: dict):
    jd_text = data.get("job_description", "")
    skills = extract_skills_from_jd(jd_text)
    return {"skills": skills}

# Request model for matching endpoints
class ATSRequest(BaseModel):
    resumeSkills: list[str]
    jdSkills: list[str]

//Without ATSRequest

Suppose frontend sends:

{
  "resumeSkills": ["python", "java"],
  "jdSkills": ["python", "sql"]
}

Without a model, FastAPI does not clearly know:

what fields to expect
whether values are lists
whether data is valid

You would manually do everything.

Example:

@app.post("/ats-score")
def ats_score(data: dict):

    resume = data["resumeSkills"]
    jd = data["jdSkills"]

Problems here:

no validation
can crash if keys missing
wrong data types possible
messy code
With ATSRequest
class ATSRequest(BaseModel):
    resumeSkills: list[str]
    jdSkills: list[str]

Now FastAPI already knows:

Field	Expected Type
resumeSkills	list of strings
jdSkills	list of strings

So if user sends wrong data:

{
  "resumeSkills": "python"
}

FastAPI automatically gives error:

{
  "detail": "resumeSkills must be a list"
}

You don't write validation manually.

# Endpoint 3: Calculate ATS (Boolean) score
@app.post("/ats-score")
def ats_score(data: ATSRequest):
    result = calculate_ats_score(data.resumeSkills, data.jdSkills)
    return result

# Endpoint 4: Calculate semantic ATS score
@app.post("/semantic-ats-score")
def semantic_score(data: ATSRequest):
    result = semantic_ats_score(data.resumeSkills, data.jdSkills)
    return result
```

### How Backend Calls These Endpoints

The Java Spring backend calls these endpoints like this:

```
POST http://localhost:8000/analyze-resume
Body: { "resume_text": "I have Python, React, AWS..." }
Response: { "skills": ["Python", "React", "AWS"] }

POST http://localhost:8000/semantic-ats-score
Body: { "resumeSkills": ["Python", "React"], "jdSkills": ["python", "ml", "react"] }
Response: { "score": 100, "matchedSkills": [...], "missingSkills": [...] }
```

---

## Flow Diagrams

### Overall Flow

```
┌──────────────────┐
│  Resume Upload   │
└────────┬─────────┘
         │
         v
┌──────────────────┐
│ analyze_resume   │  (POST /analyze-resume)
│  resume_parser   │
└────────┬─────────┘
         │
         v
┌──────────────────┐
│  Extract Skills  │
│  ["Python",      │
│   "React", ...]  │
└────────┬─────────┘
         │
         │         ┌──────────────────┐
         │         │  Job Desc Upload │
         │         └────────┬─────────┘
         │                  │
         │                  v
         │         ┌──────────────────┐
         │         │ analyze_jd       │  (POST /analyze-jd)
         │         │  jd_parser       │
         │         └────────┬─────────┘
         │                  │
         │                  v
         │         ┌──────────────────┐
         │         │  Extract Skills  │
         │         │  ["Machine       │
         │         │   Learning", ...]│
         │         └────────┬─────────┘
         │                  │
         └──────┬───────────┘
                │
                v
    ┌───────────────────────┐
    │   Compare Skills      │
    │   ars_matcher OR      │
    │   semantic_matcher    │
    └───────────┬───────────┘
                │
                v
    ┌───────────────────────┐
    │   Score + Results     │
    │   Return to Backend   │
    └───────────────────────┘
```

### ATS Matching Flow

```
Resume: ["python", "react", "docker"]
JD:     ["python", "react", "nodejs", "sql"]
         │
         v
      Set Intersection
         │
         v
Matched: ["python", "react"]  (2 matches)
Missing: ["nodejs", "sql"]     (2 missing)
         │
         v
Score = (2 / 4) × 100 = 50%
```

### Semantic Matching Flow

```
Resume: ["ml", "node"]
JD:     ["machine learning", "nodejs"]
         │
         v
   Normalize (via aliases)
         │
         v
Resume: ["machine learning", "node.js"]
JD:     ["machine learning", "nodejs"]
         │
         v
   Generate Embeddings
         │
         v
Resume embeddings: [vec1, vec2]
JD embeddings:     [vec3, vec4]
         │
         v
Compute Cosine Similarity
         │
         v
vec3 (ML) vs vec1 (ML):        similarity = 0.98 ✓ MATCH
vec4 (nodejs) vs vec2 (node):  similarity = 0.92 ✓ MATCH
         │
         v
Matched: 2/2 = 100%
```

---

## Quick Reference: When to Use Each Matcher

| Use Case | ATS Matcher | Semantic Matcher |
|----------|-------------|------------------|
| Exact keyword match needed | ✅ Perfect | ❌ Overkill |
| Handle abbreviations | ❌ No | ✅ Yes |
| Typos/fuzzy match | ❌ No | ✅ Yes |
| Performance critical | ✅ Yes | ❌ Slow |
| Learning similarity concept | ❌ Too simple | ✅ Good learning |

---

## Summary

| File | Purpose | Input | Output | Algorithm |
|------|---------|-------|--------|-----------|
| `skills.py` | Vocabulary | None | List of skills | Manual curation |
| `resume_parser.py` | Parse resume | Resume text | Extracted skills | Regex + word boundary |
| `jd_parser.py` | Parse JD | JD text | Extracted skills | Regex + word boundary |
| `ats_matcher.py` | Boolean match | 2 skill lists | Score + matched/missing | Set intersection |
| `semantic_matcher.py` | Smart match | 2 skill lists | Score + matched/missing | Embeddings + cosine similarity |
| `main.py` | API server | HTTP requests | JSON responses | FastAPI framework |

