import ast, json

source = open('semantic_matcher.py', 'r', encoding='utf-8').read()
tree = ast.parse(source)

base_aliases = {}
updates = []

for node in ast.walk(tree):
    # find initial SKILL_ALIASES assignment
    if isinstance(node, ast.Assign):
        for target in node.targets:
            if getattr(target, 'id', None) == 'SKILL_ALIASES':
                try:
                    base_aliases = ast.literal_eval(node.value)
                except Exception:
                    base_aliases = {}
    # find SKILL_ALIASES.update({...}) calls
    if isinstance(node, ast.Expr) and isinstance(node.value, ast.Call):
        call = node.value
        func = call.func
        # pattern: SKILL_ALIASES.update(dict)
        if isinstance(func, ast.Attribute) and getattr(func.value, 'id', None) == 'SKILL_ALIASES' and func.attr == 'update':
            if call.args:
                try:
                    updates.append(ast.literal_eval(call.args[0]))
                except Exception:
                    pass

# merge base and updates
final = dict(base_aliases)
for u in updates:
    final.update(u)

print(json.dumps(final, indent=2))
