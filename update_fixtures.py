import re
import os
import sys

files = [
    'e2e/cdu-02.spec.ts', 'e2e/cdu-03.spec.ts', 'e2e/cdu-04.spec.ts', 'e2e/cdu-05.spec.ts',
    'e2e/cdu-06.spec.ts', 'e2e/cdu-07.spec.ts', 'e2e/cdu-08.spec.ts', 'e2e/cdu-09.spec.ts',
    'e2e/cdu-10.spec.ts', 'e2e/cdu-11.spec.ts', 'e2e/cdu-12.spec.ts', 'e2e/cdu-13.spec.ts',
    'e2e/cdu-14.spec.ts', 'e2e/cdu-15.spec.ts', 'e2e/cdu-16.spec.ts', 'e2e/cdu-17.spec.ts',
    'e2e/cdu-18.spec.ts', 'e2e/cdu-19.spec.ts', 'e2e/cdu-20.spec.ts', 'e2e/cdu-21.spec.ts',
    'e2e/cdu-22.spec.ts', 'e2e/cdu-23.spec.ts', 'e2e/cdu-24.spec.ts', 'e2e/cdu-25.spec.ts',
    'e2e/cdu-26.spec.ts', 'e2e/cdu-27.spec.ts', 'e2e/cdu-28.spec.ts', 'e2e/cdu-29.spec.ts',
    'e2e/cdu-30.spec.ts', 'e2e/cdu-31.spec.ts', 'e2e/cdu-32.spec.ts', 'e2e/cdu-33.spec.ts',
    'e2e/cdu-34.spec.ts', 'e2e/cdu-35.spec.ts', 'e2e/cdu-36.spec.ts'
]

# Regex to match async ({ ... }) =>
# We want to capture the content inside the braces
# test(..., async ({ ... }) =>
pattern = re.compile(r'(async\s*\(\s*\{)([^}]*)(\}\s*\)\s*=>)')

def process_file(file_path):
    if not os.path.exists(file_path):
        print(f"File not found: {file_path}")
        return

    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    def replace_func(match):
        prefix = match.group(1)
        args_str = match.group(2)
        suffix = match.group(3)

        # Remove existing _resetAutomatico or resetAutomatico
        # Also remove potential leading/trailing commas and whitespace
        # We'll split by comma, strip, filter out resetAutomatico, and then join back.
        
        args = [a.strip() for a in args_str.split(',') if a.strip()]
        
        # Check if resetAutomatico or _resetAutomatico is already there
        filtered_args = []
        for arg in args:
            if arg == 'resetAutomatico' or arg == '_resetAutomatico':
                continue
            filtered_args.append(arg)
        
        # Reconstruct with _resetAutomatico at the beginning
        new_args = ['_resetAutomatico'] + filtered_args
        
        # If there were newlines or specific formatting, this might change it a bit,
        # but for E2E tests it should be fine.
        # To preserve some formatting, let's check if the original had newlines.
        if '\n' in args_str:
            # Try to keep indentation if possible
            indent = ""
            for line in args_str.split('\n'):
                if line.strip():
                    # get leading whitespace
                    indent = line[:len(line) - len(line.lstrip())]
                    if indent: break
            
            if not indent: indent = "    "
            
            new_args_str = "\n" + indent + (",\n" + indent).join(new_args) + "\n"
        else:
            new_args_str = ", ".join(new_args)

        return f"{prefix}{new_args_str}{suffix}"

    new_content = pattern.sub(replace_func, content)

    if new_content != content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated: {file_path}")
    else:
        print(f"No changes: {file_path}")

for file in files:
    process_file(file)
