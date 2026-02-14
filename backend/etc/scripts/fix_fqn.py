import os
import re

TARGET_DIR_TEST = "src/test/java"
TARGET_DIR_MAIN = "src/main/java"
TARGET_DIRS = [TARGET_DIR_TEST, TARGET_DIR_MAIN]
PACKAGE_PREFIX = "package "
IMPORT_PREFIX = "import "
STATIC_PREFIX = "static "

# Regex explanation:
# Group 1: String literals (double quotes) -> ignore
# Group 2: FQN -> process
# FQN: packet part (lower+number+underscore) followed by optional subpackages
# THEN a Class part (Uppercase start)
MATCH_PTRN = re.compile(r'("[^"]*")|(\b([a-z]\w*(?:\.[a-z]\w*)+)\.([A-Z]\w*)\b)')

def should_ignore_fqn(package_part, class_part):
    if class_part == "Assertions":
        return True
    return package_part == "java.lang"

def parse_imports(lines):
    current_package = None
    existing_imports = {} # SimpleName -> FullKey
    import_lines_indices = []
    
    for i, line in enumerate(lines):
        stripped = line.strip()
        
        if stripped.startswith(PACKAGE_PREFIX):
            clean = stripped.split("//")[0].replace(PACKAGE_PREFIX, "").replace(";", "").strip()
            current_package = clean
            
        elif stripped.startswith(IMPORT_PREFIX):
            import_lines_indices.append(i)
            clean = stripped.split("//")[0].replace(IMPORT_PREFIX, "").replace(";", "").strip()
            
            if clean.startswith(STATIC_PREFIX):
                clean = clean.replace(STATIC_PREFIX, "").strip()
            
            # Simple assumption for java imports
            parts = clean.split(".")
            simple_name = parts[-1]
            
            if simple_name != "*":
                existing_imports[simple_name] = clean
                
    return current_package, existing_imports, import_lines_indices

def get_insert_position(lines, import_lines_indices):
    if import_lines_indices:
        return import_lines_indices[-1] + 1
    
    # If no imports, look for package
    for i, line in enumerate(lines):
        if line.strip().startswith(PACKAGE_PREFIX):
            return i + 1
            
    return 0

def check_collision(cls, fqn, new_imports_to_add):
    for imp in new_imports_to_add:
        imp_parts = imp.split(".")
        imp_cls = imp_parts[-1]
        if imp_cls == cls and imp != fqn:
            return True
    return False

def determine_replacement(match, current_package, existing_imports, new_imports_to_add):
    # Group 1: String literal
    if match.group(1):
        return match.group(1), False
    
    # Group 2: FQN
    full_match = match.group(2)
    pkg = match.group(3)
    cls = match.group(4)
    fqn = f"{pkg}.{cls}"
    
    # Logic to decide if we should replace
    var = False
    
    if should_ignore_fqn(pkg, cls):
         should_replace = True
    elif current_package and pkg == current_package:
         should_replace = True
    elif cls in existing_imports:
         if existing_imports[cls] == fqn:
             should_replace = True
         else:
             # Collision with existing import
             should_replace = False
    else:
        # Potential new import
        if check_collision(cls, fqn, new_imports_to_add):
            should_replace = False
        else:
             new_imports_to_add.add(fqn)
             should_replace = True
             
    if should_replace:
        return cls, True
    return full_match, False

def scan_lines(lines, current_package, existing_imports):
    new_imports_to_add = set()
    modified_lines = []
    has_modifications = False
    
    for line in lines:
        stripped = line.strip()
        # Skip processing for packages and imports
        if stripped.startswith(PACKAGE_PREFIX) or stripped.startswith(IMPORT_PREFIX):
            modified_lines.append(line)
            continue
            
        if stripped.startswith("//") or stripped.startswith("*"):
             modified_lines.append(line)
             continue

        def replace_match_callback(match):
            replacement, changed = determine_replacement(match, current_package, existing_imports, new_imports_to_add)
            nonlocal has_modifications
            if changed:
                has_modifications = True
            return replacement

        new_line = MATCH_PTRN.sub(replace_match_callback, line)
        modified_lines.append(new_line)
        
    return modified_lines, new_imports_to_add, has_modifications

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    current_package, existing_imports, import_lines_indices = parse_imports(lines)
    
    modified_lines, new_imports_to_add, has_modifications = scan_lines(lines, current_package, existing_imports)

    if not has_modifications and not new_imports_to_add:
        return

    # Rewrite file
    insert_pos = get_insert_position(lines, import_lines_indices)
    sorted_new = sorted(new_imports_to_add)
    final_output = []
    
    if insert_pos == 0:
        for imp in sorted_new:
            final_output.append(f"{IMPORT_PREFIX}{imp};\n")

    for idx, line in enumerate(modified_lines):
        final_output.append(line)
        if idx == insert_pos - 1:
             for imp in sorted_new:
                 final_output.append(f"{IMPORT_PREFIX}{imp};\n")

    with open(filepath, 'w', encoding='utf-8') as f:
        f.writelines(final_output)
    print(f"Updated: {filepath} ({len(new_imports_to_add)} new imports)")

def find_backend_root():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    current = script_dir
    while current != "/":
        if os.path.exists(os.path.join(current, "src")):
            return current
        parent = os.path.dirname(current)
        if parent == current:
            break
        current = parent
    return "."

def main():
    print("Scanning project for FQNs...")
    
    backend_root = find_backend_root()
    print(f"Resolved backend root: {backend_root}")

    target_dirs = [
        os.path.join(backend_root, TARGET_DIR_TEST),
        os.path.join(backend_root, TARGET_DIR_MAIN)
    ]

    total_files_analyzed = 0

    for target_dir in target_dirs:
        if not os.path.exists(target_dir):
            print(f"Directory not found: {target_dir}")
            continue
            
        print(f"Processing directory: {target_dir}")
        for root, _, files in os.walk(target_dir):
            for file in files:
                if file.endswith(".java"):
                    total_files_analyzed += 1
                    full_path = os.path.join(root, file)
                    process_file(full_path)
    
    print(f"Total files analyzed: {total_files_analyzed}")

if __name__ == "__main__":
    main()
