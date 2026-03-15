import re
import os
import subprocess

def get_old_fixtures():
    # Use git grep to find all auth fixtures in HEAD^
    try:
        grep_output = subprocess.check_output(['git', 'grep', '-E', 'autenticadoComo\w+', 'HEAD^', 'e2e/']).decode('utf-8')
    except subprocess.CalledProcessError:
        return {}
    
    files_to_fix = {}
    for line in grep_output.split('\n'):
        if not line: continue
        # HEAD^:e2e/cdu-01.spec.ts: ... autenticadoComoAdmin ...
        parts = line.split(':', 2)
        if len(parts) < 3: continue
        file_path = parts[1]
        content = parts[2]
        
        if file_path not in files_to_fix:
            files_to_fix[file_path] = set()
            
        found = re.findall(r'autenticadoComo\w+', content)
        for f in found:
            files_to_fix[file_path].add(f)
            
    return files_to_fix

def fix_file_v2(file_path, fixtures_to_check):
    if not os.path.exists(file_path):
        return
        
    print(f"Processing {file_path}")
    with open(file_path, 'r') as f:
        content = f.read()
    
    # 1. Fix {page: _autenticadoComoAdmin} -> {page, _autenticadoComoAdmin}
    content = re.sub(r'\{(\s*\w+\s*):\s*(_autenticadoComo\w+\s*)\}', r'{\1, \2}', content)

    # 2. Add missing fixtures to test functions
    # We'll look for test('desc', async ({args}) => {
    # and if the desc or surrounding lines suggest it needs a fixture from the list
    
    # Let's try a different approach:
    # For each fixture in fixtures_to_check, if it's NOT in the file (with underscore), 
    # we need to find where it should be.
    # But that's hard.
    
    # Let's go back to the diff approach but better.
    diff = subprocess.check_output(['git', 'diff', 'HEAD^', file_path]).decode('utf-8')
    
    # Find removed fixtures and their context
    # Use a sliding window or just find the line before/after
    lines = diff.split('\n')
    for i, line in enumerate(lines):
        if line.startswith('-') and not line.startswith('---'):
            match = re.search(r'autenticadoComo\w+', line)
            if match:
                # This line was removed or changed. 
                # Find the corresponding line in the current file.
                # Usually it's close to the same line number or near the same test description.
                
                # Look for test description in previous lines
                test_desc = None
                for j in range(i, max(-1, i-20), -1):
                    m = re.search(r"test\(\s*['\"](.*?)['\"]", lines[j])
                    if m:
                        test_desc = m.group(1)
                        break
                
                if test_desc:
                    fixture = match.group(0)
                    print(f"  Found fixture {fixture} for test '{test_desc}'")
                    # Escaped desc for regex
                    desc_escaped = re.escape(test_desc)
                    # Pattern to find the test and its arguments (multiline)
                    pattern = rf"(test\(\s*['\"]{desc_escaped}['\"].*?async\s*\()\{{(.*?)\}}(\s*\)\s*=>)"
                    
                    def add_fixture(m):
                        args = m.group(2)
                        if "_" + fixture not in args:
                            print(f"    Adding _{fixture} to args")
                            if args.strip():
                                # Handle multiline args nicely
                                if '\n' in args:
                                    # Find last arg and append
                                    return f"{m.group(1)}{{{args}, _{fixture}}}{m.group(3)}"
                                else:
                                    return f"{m.group(1)}{{{args}, _{fixture}}}{m.group(3)}"
                            else:
                                return f"{m.group(1)}{{{'_' + fixture}}}{m.group(3)}"
                        return m.group(0)
                    
                    content = re.sub(pattern, add_fixture, content, flags=re.DOTALL)
                else:
                    # Maybe it's a beforeEach or a fixture extension
                    # (handled manually for now or with simple regex)
                    fixture = match.group(0)
                    if 'beforeEach' in line:
                        pattern = r"(test\.beforeEach\(async\s*\()\{{(.*?)\}}(\s*\)\s*=>)"
                        def add_to_beforeEach(m):
                            args = m.group(2)
                            if "_" + fixture not in args:
                                return f"{m.group(1)}{{{args}, _{fixture}}}{m.group(3)}"
                            return m.group(0)
                        content = re.sub(pattern, add_to_beforeEach, content, flags=re.DOTALL)

    # Final cleanup of double commas or weird spacing
    content = re.sub(r',\s*,', ',', content)
    content = re.sub(r'\{\s*,', '{', content)
    
    # Fix the {page: _autenticadoComoAdmin} again just in case
    content = re.sub(r'\{(\s*\w+\s*):\s*(_autenticadoComo\w+\s*)\}', r'{\1, \2}', content)

    with open(file_path, 'w') as f:
        f.write(content)

# Process all spec files and the fixture file
files = [f for f in os.listdir('e2e') if f.endswith('.spec.ts')]
files = [os.path.join('e2e', f) for f in files]
files.append('e2e/fixtures/processo-fixtures.ts')

all_fixtures = get_old_fixtures()
for f_path in files:
    if f_path in all_fixtures or any(f_path.endswith(k) for k in all_fixtures.keys()):
        fix_file_v2(f_path, all_fixtures.get(f_path, set()))
    else:
        # Even if not in all_fixtures (grep might have missed some if they weren't on the same line)
        # we still try to fix the {page: _fixture} syntax
        fix_file_v2(f_path, set())

