import re
import spacy

from data.skills import SKILLS
from parser.cleaner import clean_text
from parser.section_detector import detect_sections

# Load spaCy model once
nlp = spacy.load("en_core_web_sm")


def extract_skills_from_resume(resume_text):

    found_skills = []

    # Step 1: Clean resume
    cleaned_text = clean_text(resume_text)

    # Step 2: Detect sections
    sections = detect_sections(cleaned_text)

    # Step 3: Prefer Skills section
    skills_text = sections.get("skills", "")

    # Fallback if no Skills section exists
    if not skills_text:
        skills_text = cleaned_text

    skills_text = skills_text.lower()

    # Step 4: Process with spaCy
    doc = nlp(skills_text)

    text = doc.text

    # Step 5: Extract skills
    for skill in SKILLS:

        pattern = r"\b" + re.escape(skill.lower()) + r"\b"

        if re.search(pattern, text):
            found_skills.append(skill)

    return found_skills