import re

SECTION_HEADERS = {
    "summary": [
        "summary",
        "professional summary",
        "profile",
        "objective"
    ],

    "skills": [
        "skills",
        "technical skills",
        "core skills",
        "technologies"
    ],

    "projects": [
        "projects",
        "academic projects",
        "personal projects"
    ],

    "experience": [
        "experience",
        "work experience",
        "professional experience",
        "employment history"
    ],

    "education": [
        "education",
        "academic background",
        "qualifications"
    ]
}


def detect_sections(text: str) -> dict:
    """
    Splits a resume into logical sections.

    Returns:
    {
        "summary": "...",
        "skills": "...",
        "projects": "...",
        "experience": "...",
        "education": "..."
    }
    """

    sections = {
        "summary": "",
        "skills": "",
        "projects": "",
        "experience": "",
        "education": ""
    }

    current_section = None

    lines = text.split("\n")

    for line in lines:

        line = line.strip()

        if not line:
            continue

        normalized = re.sub(r"\s+", " ", line.lower())

        found_header = False

        # Check if current line is a section heading
        for section, headings in SECTION_HEADERS.items():

            if normalized in headings:
                current_section = section
                found_header = True
                break

        if found_header:
            continue

        # Store content under current section
        if current_section:
            sections[current_section] += line + "\n"

    # Remove extra newline characters
    for key in sections:
        sections[key] = sections[key].strip()

    return sections