import os
import re

def fix_file(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # 1. Fix imports: add .js to relative imports if missing
    # Match: from './something' or from "../something"
    content = re.sub(r"from\s+'(\.\.?/[^']+)('|\s+)", r"from '\1.js\2", content)
    content = re.sub(r'from\s+"(\.\.?/[^"]+)("|\s+)', r'from "\1.js\2', content)
    
    # Fix double .js.js if it happened
    content = content.replace('.js.js', '.js')

    # 2. Add 'import type { Page }' if not present and 'page' is used
    if 'page' in content and 'import type { Page }' not in content:
        content = "import type { Page } from '@playwright/test';\n" + content

    # 3. Add types to test functions
    # Patterns to match: test('...', async ({...}) => { or test.beforeEach(async ({...}) => {
    # We want to match the whole async line and replace it with correct typed version
    # Regex for async parameter list: async\s*\(\s*\{([^}]+)\}\s*\)\s*=>
    
    def add_types(match):
        prefix = match.group(1) # e.g. "test('...', "
        params_str = match.group(2) # e.g. "page, autenticadoComoAdmin"
        
        # If it already has a colon outside of the parameter list (after the closing brace), it's already typed
        # Actually, simpler: if it has ': {', it's probably already typed in the way we want.
        if ': {' in match.group(0):
            return match.group(0)
            
        params = [p.strip() for p in params_str.split(',')]
        type_annotations = []
        for p in params:
            if p == 'page':
                type_annotations.append('page: Page')
            elif p == 'request':
                type_annotations.append('request: any')
            elif p.startswith('autenticadoComo'):
                type_annotations.append(f'{p}: void')
            elif p == 'cleanupAutomatico':
                type_annotations.append('cleanupAutomatico: any')
            else:
                type_annotations.append(f'{p}: any')
        
        return f"{prefix}async ({{{', '.join(params)}}}: {{{', '.join(type_annotations)}}}) =>"

    # Match test(..., async ({...}) =>
    content = re.sub(r"(test(?:\.\w+)?\([^,]+,\s+)async\s*\(\s*\{([^}]+)\}\s*\)\s*=>", add_types, content)
    
    # Also match test.beforeEach(async ({...}) =>
    content = re.sub(r"(test\.(?:before|after)(?:Each|All)\()async\s*\(\s*\{([^}]+)\}\s*\)\s*=>", add_types, content)

    # 4. Fix parseInt to Number.parseInt
    content = content.replace('parseInt(', 'Number.parseInt(')

    with open(file_path, 'w') as f:
        f.write(content)

e2e_dir = '/Users/leonardo/sgc/e2e'
for filename in os.listdir(e2e_dir):
    if filename.endswith('.spec.ts'):
        # We can re-run on everything, the script checks if already typed
        fix_file(os.path.join(e2e_dir, filename))
