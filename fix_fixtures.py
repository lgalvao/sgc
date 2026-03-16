import re
import os
import subprocess

def get_old_fixtures():
    # Get the diff of e2e directory from HEAD^
    diff = subprocess.check_output(['git', 'diff', 'HEAD^', 'e2e/']).decode('utf-8')
    
    files_to_fix = {}
    current_file = None
    
    lines = diff.split('\n')
    for line in lines:
        if line.startswith('diff --git'):
            # diff --git a/e2e/cdu-01.spec.ts b/e2e/cdu-01.spec.ts
            current_file = line.split(' ')[-1].replace('b/', '')
            if current_file not in files_to_fix:
                files_to_fix[current_file] = []
        
        if line.startswith('-') and not line.startswith('---'):
            # Removed line
            # Check if it's a test line with a fixture
            # test('...', async ({page, autenticadoComoAdmin}) => {
            match = re.search(r'test\((.*?), async\s*\(\{(.*?)\}\)\s*=>', line)
            if match:
                test_desc = match.group(1).strip()
                args = match.group(2).strip()
                
                # Find all auth fixtures in args
                auth_fixtures = re.findall(r'autenticadoComo\w+', args)
                if auth_fixtures:
                    files_to_fix[current_file].append({
                        'test_desc': test_desc,
                        'fixtures': auth_fixtures,
                        'original_args': args
                    })
            
            # Also check for fixtures in extend (e.g. processoFixture)
            # processoFixture: async ({page, autenticadoComoAdmin}, use, testInfo) => {
            match_fixture = re.search(r'(\w+Fixture):\s*async\s*\(\{(.*?)\},', line)
            if match_fixture:
                fixture_name = match_fixture.group(1)
                args = match_fixture.group(2).strip()
                auth_fixtures = re.findall(r'autenticadoComo\w+', args)
                if auth_fixtures:
                    files_to_fix[current_file].append({
                        'fixture_name': fixture_name,
                        'fixtures': auth_fixtures,
                        'original_args': args
                    })
                    
    return files_to_fix

def fix_file(file_path, fixes):
    if not os.path.exists(file_path):
        return
        
    with open(file_path, 'r') as f:
        content = f.read()
    
    for fix in fixes:
        fixtures_str = ", ".join(["_" + f for f in fix['fixtures']])
        
        if 'test_desc' in fix:
            # Escape test_desc for regex
            desc_escaped = re.escape(fix['test_desc'])
            
            # Try to find the test in current content
            # Pattern: test(desc_escaped, async ({args}) =>
            # Current args might have page or browser but missing the fixture
            pattern = rf'(test\({desc_escaped},\s*async\s*\()\{{(.*?)\}}(\)\s*=>)'
            
            def replace_test(match):
                current_args = match.group(2).strip()
                # Check for the broken case: {page: _autenticadoComoAdmin}
                if ': _autenticadoComo' in current_args:
                    # Fix it: {page, _autenticadoComoAdmin}
                    # We assume it was {page: _fixture}
                    parts = current_args.split(':')
                    page_name = parts[0].strip()
                    fixture_name = parts[1].strip()
                    return f"{match.group(1)}{{{page_name}, {fixture_name}}}{match.group(3)}"
                
                # Normal case: check if fixture already there with underscore
                all_present = True
                for f in fix['fixtures']:
                    if "_" + f not in current_args:
                        all_present = False
                        break
                
                if not all_present:
                    # Add missing fixtures
                    new_args = current_args
                    for f in fix['fixtures']:
                        if "_" + f not in new_args:
                            if new_args:
                                new_args += ", _" + f
                            else:
                                new_args = "_" + f
                    return f"{match.group(1)}{{{new_args}}}{match.group(3)}"
                
                return match.group(0) # No change needed or already fixed

            content = re.sub(pattern, replace_test, content)
        
        elif 'fixture_name' in fix:
            pattern = rf'({fix["fixture_name"]}:\s*async\s*\()\{{(.*?)\}},'
            
            def replace_fixture(match):
                current_args = match.group(2).strip()
                new_args = current_args
                for f in fix['fixtures']:
                    if "_" + f not in new_args:
                        if new_args:
                            new_args += ", _" + f
                        else:
                            new_args = "_" + f
                return f"{match.group(1)}{{{new_args}}},"
            
            content = re.sub(pattern, replace_fixture, content)

    # Extra pass for any {page: _autenticadoComoAdmin} that might have been missed
    content = re.sub(r'async\s*\(\{(page|pageCoord22|browser|request):\s*(_autenticadoComo\w+)\}\)', r'async ({\1, \2})', content)

    with open(file_path, 'w') as f:
        f.write(content)

fixes = get_old_fixtures()
for file_path, file_fixes in fixes.items():
    print(f"Fixing {file_path}")
    fix_file(file_path, file_fixes)

