const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');
const os = require('os');

// Configuração dos testes
const steps = [
    {
        name: 'Backend - Testes Unitários',
        command: process.platform === 'win32' ? 'gradlew.bat' : './gradlew',
        args: ['unitTest'],
        cwd: 'backend'
    },
    {
        name: 'Backend - Testes de Integração',
        command: process.platform === 'win32' ? 'gradlew.bat' : './gradlew',
        args: ['integrationTest'],
        cwd: 'backend'
    },
    {
        name: 'Frontend - Testes Unitários',
        command: 'npm',
        args: ['run', 'test:unit'],
        cwd: 'frontend'
    },
    {
        name: 'E2E - Playwright',
        command: 'npx',
        args: ['playwright', 'test'],
        cwd: '.'
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
        /Downloading Chromium/,
        /Downloading Firefox/,
        /Downloading Webkit/,
        /^\|.*\| \d+% of .* MiB/, // Barras de progresso
        /^\s*$/ // Linhas vazias
    ];

    let lines = text.split('\n');
    lines = lines.filter(line => {
        const trimmed = line.trim();
        if (filters.some(f => f.test(trimmed))) return false;
        // Filtro específico para os pontos do vitest/jest
        if (/^[\.·]+$/.test(trimmed)) return false;
        return true;
    });

    return lines.join('\n');
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
            resolve({
                ...step,
                status,
                code,
                duration,
                output
            });
        });

        child.on('error', (err) => {
            console.error(`Erro ao executar ${step.name}:`, err);
            resolve({
                ...step,
                status: '❌ Erro de Execução',
                code: -1,
                duration: 0,
                output: err.message
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
    md += `| Teste | Status | Duração (s) |\n`;
    md += `| :--- | :---: | :---: |\n`;
    
    let allPassed = true;
    results.forEach(r => {
        md += `| ${r.name} | ${r.status} | ${r.duration}s |\n`;
        if (r.code !== 0) allPassed = false;
    });

    md += `\n**Status Geral:** ${allPassed ? '🟢 APROVADO' : '🔴 REPROVADO'}\n\n`;

    md += `## Detalhes da Execução\n\n`;

    results.forEach(r => {
        md += `### ${r.name}\n\n`;
        md += '- **Comando:** `' + r.command + ' ' + r.args.join(' ') + '`\n';
        md += '- **Diretório:** `' + r.cwd + '`\n';
        md += `- **Status:** ${r.status}\n\n`;
        
        md += '<details>\n<summary>Ver Logs de Saída</summary>\n\n';
        md += '```text\n';

        let cleanOutput = cleanLog(r.output);

        // Limita o log para não quebrar o markdown se for gigante
        // Aumentei o limite para 50k para capturar mais contexto em erros
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
