import re
import spacy
from skills import SKILLS

# Load spaCy model
nlp = spacy.load("en_core_web_sm")


def extract_skills_from_resume(resume_text):

    found_skills = []

    # Clean text
    cleaned_text = re.sub(r'[^a-zA-Z0-9+#.\s]', ' ', resume_text)

    cleaned_text = cleaned_text.lower()

    # Process with spaCy
    doc = nlp(cleaned_text)

    text = doc.text

    for skill in SKILLS:

        pattern = r'\b' + re.escape(skill.lower()) + r'\b'

        if re.search(pattern, text):
            found_skills.append(skill)

    return found_skills