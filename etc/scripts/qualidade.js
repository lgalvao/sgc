import { spawn } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';

// Configuração dos testes
const steps = [
    {
        name: 'Backend - Testes Unitários',
        command: process.platform === 'win32' ? 'gradlew.bat' : './gradlew',
        args: ['unitTest'],
        cwd: 'backend',
        type: 'gradle',
        reportDir: 'backend/build/test-results/unitTest'
    },
    {
        name: 'Backend - Testes de Integração',
        command: process.platform === 'win32' ? 'gradlew.bat' : './gradlew',
        args: ['integrationTest'],
        cwd: 'backend',
        type: 'gradle',
        reportDir: 'backend/build/test-results/integrationTest'
    },
    {
        name: 'Frontend - Testes Unitários',
        command: 'npm',
        args: ['run', 'test:unit'],
        cwd: 'frontend',
        type: 'vitest'
    },
    {
        name: 'E2E - Playwright',
        command: 'npx',
        args: ['playwright', 'test'],
        cwd: '.',
        type: 'playwright'
    }
];

const reportFile = 'relatorio-testes.md';

function cleanLog(output) {
    // Remove cores ANSI
    let text = output.replace(/\u001b\[.*?m/g, '');

    // Filtros de linhas indesejadas
    const filters = [
        /Not implemented: HTMLCanvasElement/,
        /^> Task :.* UP-TO-DATE/,
        /^> Task :.* NO-SOURCE/,
        /^> Task :.* SUCCESS/,
        /Downloading Chromium/,
        /Downloading Firefox/,
        /Downloading Webkit/,
        /^\|.*\| \d+% of .* MiB/, // Barras de progresso
        /^\s*$/, // Linhas vazias
        /^Calculating task graph/,
        /^Reusing configuration cache/,
        /^Configuration cache entry/,
        /^BUILD SUCCESSFUL/,
        /^npm warn/,
        /^> sgc@/,
        /^> vitest/,
        /^Results: SUCCESS/
    ];

    let lines = text.split('\n');
    lines = lines.filter(line => {
        const trimmed = line.trim();
        if (filters.some(f => f.test(trimmed))) return false;
        // Filtro específico para os pontos do vitest/jest
        return !/^[\.·]+$/.test(trimmed);

    });

    return lines.join('\n');
}

function parseGradleStats(step) {
    let stats = { passed: 0, failed: 0, skipped: 0, total: 0 };

    if (!step.reportDir || !fs.existsSync(step.reportDir)) {
        return stats;
    }

    const files = fs.readdirSync(step.reportDir).filter(f => f.endsWith('.xml'));

    files.forEach(file => {
        const content = fs.readFileSync(path.join(step.reportDir, file), 'utf-8');
        // Simple regex to find attributes in <testsuite>
        const testsMatch = content.match(/tests="(\d+)"/);
        const failuresMatch = content.match(/failures="(\d+)"/);
        const skippedMatch = content.match(/skipped="(\d+)"/);
        const errorsMatch = content.match(/errors="(\d+)"/);

        if (testsMatch) {
            const tests = parseInt(testsMatch[1], 10);
            const failures = failuresMatch ? parseInt(failuresMatch[1], 10) : 0;
            const errors = errorsMatch ? parseInt(errorsMatch[1], 10) : 0;
            const skipped = skippedMatch ? parseInt(skippedMatch[1], 10) : 0;

            stats.total += tests;
            stats.failed += failures + errors;
            stats.skipped += skipped;
            stats.passed += (tests - failures - errors - skipped);
        }
    });

    return stats;
}

function parseVitestStats(output) {
    let stats = { passed: 0, failed: 0, skipped: 0, total: 0 };

    // Pattern: Tests  1101 passed (1101)
    const passedMatch = output.match(/Tests\s+(\d+)\s+passed\s+\((\d+)\)/);
    if (passedMatch) {
        stats.passed = parseInt(passedMatch[1], 10);
        stats.total = parseInt(passedMatch[2], 10); // Or match[1]
        return stats;
    }

    // Pattern: Tests  X passed | Y failed | Z skipped | W total
    // Note: Vitest output might vary, capturing generic numbers
    const testsLine = output.match(/Tests\s+(.*)/);
    if (testsLine) {
        const line = testsLine[1];

        const p = line.match(/(\d+)\s+passed/);
        if (p) stats.passed = parseInt(p[1], 10);

        const f = line.match(/(\d+)\s+failed/);
        if (f) stats.failed = parseInt(f[1], 10);

        const s = line.match(/(\d+)\s+skipped/);
        if (s) stats.skipped = parseInt(s[1], 10);

        const t = line.match(/(\d+)\s+total/);
        if (t) stats.total = parseInt(t[1], 10);
        else stats.total = stats.passed + stats.failed + stats.skipped;
    }

    return stats;
}

function parsePlaywrightStats(output) {
    let stats = { passed: 0, failed: 0, skipped: 0, total: 0 };

    // Matches lines like: "  12 passed (20s)" or "  10 failed"
    // Playwright prints summaries at the end usually

    const passedMatch = output.match(/(\d+)\s+passed/);
    if (passedMatch) stats.passed = parseInt(passedMatch[1], 10);

    const failedMatch = output.match(/(\d+)\s+failed/);
    if (failedMatch) stats.failed = parseInt(failedMatch[1], 10);

    const skippedMatch = output.match(/(\d+)\s+skipped/);
    if (skippedMatch) stats.skipped = parseInt(skippedMatch[1], 10);

    // Playwright might say "169 did not run", which we can count as skipped or just ignore?
    // Let's count "did not run" as skipped for simplicity if skipped is 0
    const didNotRunMatch = output.match(/(\d+)\s+did not run/);
    if (didNotRunMatch) stats.skipped += parseInt(didNotRunMatch[1], 10);

    stats.total = stats.passed + stats.failed + stats.skipped;
    return stats;
}

function getStats(step, output) {
    try {
        if (step.type === 'gradle') {
            return parseGradleStats(step);
        } else if (step.type === 'vitest') {
            return parseVitestStats(output);
        } else if (step.type === 'playwright') {
            return parsePlaywrightStats(output);
        }
    } catch (e) {
        console.error(`Erro ao processar estatísticas de ${step.name}:`, e);
    }
    return { passed: '-', failed: '-', skipped: '-', total: '-' };
}

async function runCommand(step) {
    return new Promise((resolve) => {
        console.log(`\n🚀 Iniciando: ${step.name}...`);
        const startTime = Date.now();
        
        const cmd = process.platform === 'win32' ? `${step.command}` : step.command;
        
        // No Windows, npm/npx são .cmd
        let finalCmd = cmd;
        if (process.platform === 'win32' && (cmd === 'npm' || cmd === 'npx')) {
            finalCmd = `${cmd}.cmd`;
        }

        const child = spawn(finalCmd, step.args, {
            cwd: path.resolve(process.cwd(), step.cwd),
            shell: true,
            stdio: ['ignore', 'pipe', 'pipe']
        });

        let output = '';

        child.stdout.on('data', (data) => {
            const txt = data.toString();
            process.stdout.write(txt); // Mostra no console em tempo real
            output += txt;
        });

        child.stderr.on('data', (data) => {
            const txt = data.toString();
            process.stderr.write(txt);
            output += txt;
        });

        child.on('close', (code) => {
            const duration = ((Date.now() - startTime) / 1000).toFixed(2);
            const status = code === 0 ? '✅ Sucesso' : '❌ Falha';
            console.log(`\n🏁 Finalizado: ${step.name} (${status}) - ${duration}s`);

            // Collect stats
            const stats = getStats(step, output);

            resolve({
                ...step,
                status,
                code,
                duration,
                output,
                stats
            });
        });

        child.on('error', (err) => {
            console.error(`Erro ao executar ${step.name}:`, err);
            resolve({
                ...step,
                status: '❌ Erro de Execução',
                code: -1,
                duration: 0,
                output: err.message,
                stats: { passed: '-', failed: '-', skipped: '-', total: '-' }
            });
        });
    });
}

function generateReport(results) {
    const date = new Date().toLocaleString('pt-BR');
    
    let md = `# Relatório de Testes Automatizados\n\n`;
    md += `**Data:** ${date}\n`;
    md += `**Sistema:** ${os.type()} ${os.release()}\n\n`;

    md += `## Resumo Executivo\n\n`;

    // Status Table
    md += `| Teste | Status | Duração (s) |\n`;
    md += `| :--- | :---: | :---: |\n`;
    
    let allPassed = true;
    results.forEach(r => {
        md += `| ${r.name} | ${r.status} | ${r.duration}s |\n`;
        if (r.code !== 0) allPassed = false;
    });
    md += `\n`;

    // Statistics Table
    md += `### Estatísticas Detalhadas\n\n`;
    md += `| Teste | Total | Passou | Falhou | Ignorado |\n`;
    md += `| :--- | :---: | :---: | :---: | :---: |\n`;

    results.forEach(r => {
        const s = r.stats;
        md += `| ${r.name} | ${s.total} | ${s.passed} | ${s.failed} | ${s.skipped} |\n`;
    });

    md += `\n**Status Geral:** ${allPassed ? '🟢 APROVADO' : '🔴 REPROVADO'}\n\n`;

    md += `## Detalhes da Execução\n\n`;

    results.forEach(r => {
        md += `### ${r.name}\n\n`;
        md += '- **Comando:** `' + r.command + ' ' + r.args.join(' ') + '`\n';
        md += '- **Diretório:** `' + r.cwd + '`\n';
        md += `- **Status:** ${r.status}\n`;

        const s = r.stats;
        md += `- **Resultados:** ${s.total} testes, ${s.passed} aprovados, ${s.failed} falhas\n\n`;
        
        md += '<details>\n<summary>Ver Logs de Saída</summary>\n\n';
        md += '```text\n';

        let cleanOutput = cleanLog(r.output);

        // Limita o log para não quebrar o markdown se for gigante
        if (cleanOutput.length > 50000) {
            cleanOutput = `... (Log truncado - mostrando últimos 50k caracteres) ...\n${cleanOutput.slice(-50000)}`;
        }
        md += cleanOutput;
        md += '\n```\n\n';
        md += '</details>\n\n---\n\n';
    });

    fs.writeFileSync(reportFile, md, 'utf-8');
    console.log(`\n📄 Relatório gerado em: ${path.resolve(reportFile)}`);
}

async function main() {
    const results = [];
    
    console.log('Iniciando bateria de testes...\n');

    for (const step of steps) {
        const result = await runCommand(step);
        results.push(result);
    }

    generateReport(results);
}

main();
