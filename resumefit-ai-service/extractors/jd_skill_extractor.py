# import re
# from skills import SKILLS


# def extract_skills_from_jd(job_description):

#     found_skills = []

#     job_description = job_description.lower()

#     for skill in SKILLS:

#         pattern = r'\b' + re.escape(skill.lower()) + r'\b'

#     if re.search(pattern, job_description):
#         found_skills.append(skill)
#     return found_skills

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