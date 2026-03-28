#!/usr/bin/env node
const fs = require('node:fs');
const path = require('node:path');
const {execFileSync} = require('node:child_process');
const {ROOT_DIR} = require('./lib/cobertura-base.cjs');

function runNodeScript(scriptName, args = [], options = {}) {
    return execFileSync('node', [path.join('backend/etc/scripts', scriptName), ...args], {
        cwd: ROOT_DIR,
        encoding: 'utf-8',
        stdio: options.capture ? ['inherit', 'pipe', 'pipe'] : 'inherit'
    });
}

function printHeader(title) {
    console.log(`📍 ${title}`);
    console.log('─────────────────────────────────────────');
}

function printPreviewFromFile(filePath, lines = 50) {
    if (!fs.existsSync(filePath)) {
        return;
    }
    const content = fs.readFileSync(filePath, 'utf-8').split(/\r?\n/).slice(0, lines).join('\n');
    console.log(content);
}

function main() {
    if (process.argv.includes('--help') || process.argv.includes('-h')) {
        console.log(`Uso: node backend/etc/scripts/cobertura-100.cjs

Executa a jornada completa de diagnostico de cobertura:
1. testa e gera JaCoCo
2. analisa cobertura
3. gera lacunas
4. gera plano de cobertura
5. analisa classes sem testes
6. prioriza backlog de testes`);
        process.exit(0);
    }

    console.log('🎯 === JORNADA PARA 100% DE COBERTURA ===\n');

    printHeader('Etapa 1: Executar testes e gerar relatorio JaCoCo');
    execFileSync(process.platform === 'win32' ? 'gradlew.bat' : './gradlew', [':backend:test', ':backend:jacocoTestReport'], {
        cwd: ROOT_DIR,
        stdio: 'inherit'
    });
    console.log('');

    printHeader('Etapa 2: Analisar cobertura atual (visao detalhada)');
    const coberturaDetalhada = runNodeScript('analisar-cobertura.cjs', [], {capture: true});
    fs.writeFileSync(path.join(ROOT_DIR, 'cobertura-detalhada.txt'), coberturaDetalhada, 'utf-8');
    console.log(coberturaDetalhada.split(/\r?\n/).slice(0, 50).join('\n'));
    console.log('');

    printHeader('Etapa 3: Identificar lacunas de cobertura');
    const lacunas = runNodeScript('super-cobertura.cjs', [], {capture: true});
    console.log(lacunas.split(/\r?\n/).slice(0, 100).join('\n'));
    console.log('');

    printHeader('Etapa 4: Gerar plano de acao para 100%');
    runNodeScript('gerar-plano-cobertura.cjs');
    console.log('');

    printHeader('Etapa 5: Analisar arquivos sem testes unitarios');
    runNodeScript('analyze_tests.cjs', ['--dir', 'backend', '--output', 'analise-testes.md', '--output-json', 'analise-testes.json']);
    console.log('');

    printHeader('Etapa 6: Priorizar criacao de testes');
    const caminhoJson = path.join(ROOT_DIR, 'analise-testes.json');
    if (fs.existsSync(caminhoJson)) {
        runNodeScript('prioritize_tests.cjs', ['--input', 'analise-testes.json', '--output', 'priorizacao-testes.md']);
        printPreviewFromFile(path.join(ROOT_DIR, 'priorizacao-testes.md'));
    } else {
        console.log('⚠️  Arquivo analise-testes.json nao encontrado, pulando priorizacao');
    }
    console.log('');

    console.log('✅ === ANALISE COMPLETA ===\n');
    console.log('Arquivos gerados:');
    console.log('  📄 plano-100-cobertura.md       - Plano detalhado com todas as lacunas');
    console.log('  📄 cobertura-detalhada.txt      - Analise detalhada com tabelas');
    console.log('  📄 cobertura_lacunas.json       - Dados estruturados das lacunas');
    console.log('  📄 analise-testes.md            - Analise de arquivos sem testes');
    console.log('  📄 analise-testes.json          - Dados estruturados da analise');
    console.log('  📄 priorizacao-testes.md        - Testes priorizados por importancia\n');
    console.log('Proximos passos:');
    console.log('  1. Revisar plano-100-cobertura.md');
    console.log('  2. Comecar pelos testes P1 (criticos) em priorizacao-testes.md');
    console.log("  3. Usar 'node backend/etc/scripts/gerar-stub-teste.cjs <Classe>'");
    console.log('     para gerar esqueletos de testes');
    console.log('  4. Implementar os testes');
    console.log('  5. Rodar este script novamente para verificar progresso\n');

    const lacunasPath = path.join(ROOT_DIR, 'cobertura_lacunas.json');
    if (fs.existsSync(lacunasPath)) {
        const dados = JSON.parse(fs.readFileSync(lacunasPath, 'utf-8'));
        console.log(`🎯 Meta: De ${dados.globalLineCoverage || '?%'} -> 100%`);
    }
}

try {
    main();
} catch (error) {
    console.error(`Erro na jornada de cobertura: ${error.message}`);
    process.exit(1);
}
