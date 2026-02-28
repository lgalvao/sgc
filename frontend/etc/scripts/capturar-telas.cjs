/* eslint-disable */
const {spawn} = require('child_process');
const path = require('path');
const fs = require('fs');

const CATEGORIES = {
    'seguranca': "01 - Autenticação",
    'painel': "02 - Painel Principal",
    'processo': "03 - Fluxo de Processo",
    'subprocesso': "04 - Subprocesso e Atividades",
    'mapa': "05 - Mapa de Competências",
    'navegacao': "06 - Navegação e Menus",
    'estados': "07 - Estados e Situações",
    'responsividade': "08 - Responsividade",
    'all': ""
};

const ARGS = process.argv.slice(2);
const HEADED = ARGS.includes('--headed');
const DEBUG = ARGS.includes('--debug');
const UI = ARGS.includes('--ui');
const HELP = ARGS.includes('--help') || ARGS.includes('-h');

let category = ARGS.find(arg => !arg.startsWith('-')) || 'all';

if (HELP) {
    console.log(`
Uso: node scripts/frontend/capturar-telas.js [categoria] [opções]

Categorias disponíveis:
  ${Object.keys(CATEGORIES).join('\n  ')}

Opções:
  --headed    Executar com navegador visível
  --debug     Ativar modo debug
  --ui        Abrir interface UI do Playwright
  --help, -h  Exibir esta ajuda
`);
    process.exit(0);
}

if (!CATEGORIES.hasOwnProperty(category)) {
    console.error(`Erro: Categoria inválida '${category}'. Use --help para ver as opções.`);
    process.exit(1);
}

const pattern = CATEGORIES[category];
const screenshotsDir = path.join(__dirname, '../../../screenshots');

// Limpar screenshots se for 'all' (comportamento do script original capturar-telas.sh)
if (category === 'all') {
    if (fs.existsSync(screenshotsDir)) {
        console.log('Limpando screenshots antigas...');
        fs.rmSync(screenshotsDir, {recursive: true, force: true});
    }
}
if (!fs.existsSync(screenshotsDir)) {
    fs.mkdirSync(screenshotsDir, {recursive: true});
}

console.log(`
=== Captura de Telas: ${category.toUpperCase()} ===\n`);

const cmdArgs = ['playwright', 'test', 'e2e/captura-telas.spec.ts'];

if (pattern) {
    cmdArgs.push('--grep', pattern);
}

if (HEADED) cmdArgs.push('--headed');
if (DEBUG) cmdArgs.push('--debug');
if (UI) cmdArgs.push('--ui');

// Run via npx (cross-platform)
const npx = process.platform === 'win32' ? 'npx.cmd' : 'npx';

console.log(`Executando: ${npx} ${cmdArgs.join(' ')}`);

const child = spawn(npx, cmdArgs, {
    stdio: 'inherit',
    cwd: path.join(__dirname, '../../../') // Root of project
});

child.on('close', (code) => {
    if (code === 0) {
        console.log(`
✓ Captura concluída com sucesso!`);
        console.log(`Screenshots salvas em: ${screenshotsDir}`);
    } else {
        console.error(`
⚠ Captura falhou ou foi interrompida (código ${code})`);
        process.exit(code);
    }
});
