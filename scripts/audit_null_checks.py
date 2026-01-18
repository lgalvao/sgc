import os
import re

SOURCE_DIR = 'backend/src/main/java/sgc'
AUDIT_FILE = 'null-checks-audit.txt'
ANALYSIS_FILE = 'null-checks-analysis.md'

def scan_files():
    results = {}

    for root, dirs, files in os.walk(SOURCE_DIR):
        for file in files:
            if file.endswith('.java'):
                path = os.path.join(root, file)
                try:
                    with open(path, 'r', encoding='utf-8') as f:
                        lines = f.readlines()
                except Exception as e:
                    print(f"Error reading {path}: {e}")
                    continue

                file_results = []
                for i, line in enumerate(lines):
                    if '!= null' in line or '== null' in line:
                        stripped = line.strip()
                        # Ignore comments
                        if stripped.startswith('//') or stripped.startswith('*'):
                            continue

                        category = classify(lines, i, stripped)
                        file_results.append({
                            'line': i + 1,
                            'content': stripped,
                            'category': category
                        })

                if file_results:
                    results[path] = file_results
    return results

def classify(lines, index, content):
    # Simple heuristics

    # Check for @Nullable in the method signature or field declaration nearby
    # This is a very rough check looking backwards for the annotation
    context_range = 20
    start = max(0, index - context_range)
    context = "".join(lines[start:index+1])

    if "@Nullable" in context:
        return "MAYBE_LEGIT"

    # If it's a DTO (in a dto package), it might be checking incoming data
    # But usually DTOs should use Bean Validation annotations instead of manual checks

    return "POTENTIALLY_REDUNDANT"

def generate_report(results):
    with open(AUDIT_FILE, 'w', encoding='utf-8') as f:
        for path, items in results.items():
            f.write(f"File: {path}\n")
            for item in items:
                f.write(f"  L{item['line']} [{item['category']}]: {item['content']}\n")
            f.write("\n")

    with open(ANALYSIS_FILE, 'w', encoding='utf-8') as f:
        f.write("# Null Checks Analysis\n\n")
        f.write("| Class | Total Checks | Potentially Redundant |\n")
        f.write("|-------|--------------|-----------------------|\n")

        # Sort by total checks
        sorted_files = sorted(results.items(), key=lambda x: len(x[1]), reverse=True)

        for path, items in sorted_files:
            filename = os.path.basename(path)
            total = len(items)
            redundant = len([x for x in items if x['category'] == 'POTENTIALLY_REDUNDANT'])
            f.write(f"| {filename} | {total} | {redundant} |\n")

if __name__ == '__main__':
    print("Scanning files for null checks...")
    data = scan_files()
    print(f"Found null checks in {len(data)} files.")
    generate_report(data)
    print(f"Reports generated: {AUDIT_FILE}, {ANALYSIS_FILE}")
