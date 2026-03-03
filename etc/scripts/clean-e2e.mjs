import fs from 'fs';
import path from 'path';

const E2E_DIR = './e2e';

// Fixtures que são apenas para autenticação automática (podem ser removidas se não usadas)
const AUTH_FIXTURES = [
    'autenticadoComoAdmin',
    'autenticadoComoGestor',
    'autenticadoComoGestorCoord21',
    'autenticadoComoGestorCoord22',
    'autenticadoComoChefeSecao111',
    'autenticadoComoChefeSecao211',
    'autenticadoComoChefeSecao212',
    'autenticadoComoChefeSecao221',
    'autenticadoComoChefeSecao121',
    'autenticadoComoChefeAssessoria11',
    'autenticadoComoChefeAssessoria12',
    'autenticadoComoChefeAssessoria21',
    'autenticadoComoChefeAssessoria22',
    'autenticadoComoServidor'
];

function cleanFile(filePath) {
    let content = fs.readFileSync(filePath, 'utf8');
    let original = content;

    // 1. Substituir not.toBeVisible() por toBeHidden()
    content = content.replace(/\.not\.toBeVisible\(\)/g, '.toBeHidden()');

    // 2. Limpar assinaturas de teste: async ({page, autenticadoComoAdmin}: { page: Page, ... }) =>
    // Procura por padrões de desestruturação em assinaturas de teste
    content = content.replace(/async\s*\(\s*\{([^}]+)\}\s*(:\s*\{[^}]+\})?\s*\)\s*=>/g, (match, p1, p2) => {
        let args = p1.split(',').map(s => s.trim());
        
        // Remove fixtures de auth
        let cleanedArgs = args.filter(arg => !AUTH_FIXTURES.includes(arg));
        
        // Se mudou algo, reconstrói a assinatura sem a tipagem inline redundante
        if (cleanedArgs.length !== args.length || p2) {
             return `async ({${cleanedArgs.join(', ')}}) =>`;
        }
        return match;
    });

    // 3. Remover imports de tipo não usados (Page, useProcessoCleanup)
    if (!content.includes(': Page') && !content.includes('Page,')) {
        content = content.replace(/import\s+type\s*\{\s*Page\s*\}\s*from\s*['"]@playwright\/test['"];?\r?\n?/g, '');
    }
    if (!content.includes('ReturnType<typeof useProcessoCleanup>')) {
        content = content.replace(/import\s+type\s*\{\s*useProcessoCleanup\s*\}\s*from\s*['"].*hooks\/hooks-limpeza.js['"];?\r?\n?/g, '');
    }

    if (content !== original) {
        fs.writeFileSync(filePath, content);
        console.log(`Cleaned: ${filePath}`);
    }
}

const files = fs.readdirSync(E2E_DIR).filter(f => f.startsWith('cdu-') && f.endsWith('.spec.ts'));
files.forEach(f => cleanFile(path.join(E2E_DIR, f)));
