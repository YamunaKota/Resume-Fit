import re


def clean_text(text: str) -> str:
    """
    Cleans extracted resume text while preserving structure.
    """

    # Normalize line endings
    text = text.replace("\r\n", "\n").replace("\r", "\n")

    # Remove spaces/tabs before newline
    text = re.sub(r"[ \t]+\n", "\n", text)

    # Collapse multiple spaces into one
    text = re.sub(r"[ \t]{2,}", " ", text)

    # Fix spaces before punctuation
    text = re.sub(r"\s+:", ":", text)

    # Collapse 3 or more blank lines into 2
    text = re.sub(r"\n{3,}", "\n\n", text)

    return text.strip()