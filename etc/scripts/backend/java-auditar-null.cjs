#!/usr/bin/env node
const fs = require('node:fs');
const path = require('node:path');
const {exibirAjudaComando} = require('./lib/cli-ajuda.cjs');

const SOURCE_DIR = path.join(__dirname, '../../../backend/src/main/java/sgc');
const AUDIT_FILE = path.join(__dirname, '../../../null-checks-audit.txt');
const ANALYSIS_FILE = path.join(__dirname, '../../../null-checks-analysis.md');

function getAllFiles(dirPath, arrayOfFiles = []) {
    const files = fs.readdirSync(dirPath, {withFileTypes: true});

    files.forEach(file => {
        const fullPath = path.join(dirPath, file.name);
        if (file.isDirectory()) {
            getAllFiles(fullPath, arrayOfFiles);
        } else if (file.name.endsWith('.java')) {
            arrayOfFiles.push(fullPath);
        }
    });

    return arrayOfFiles;
}

function classify(lines, index) {
    const contextRange = 20;
    const start = Math.max(0, index - contextRange);
    const contextLines = lines.slice(start, index + 1).join('\n');
    return contextLines.includes('@Nullable') ? 'MAYBE_LEGIT' : 'POTENTIALLY_REDUNDANT';
}

function scanFiles() {
    const results = {};
    const files = getAllFiles(SOURCE_DIR);

    files.forEach(file => {
        try {
            const content = fs.readFileSync(file, 'utf-8');
            const lines = content.split(/\r?\n/);
            const fileResults = [];

            lines.forEach((line, index) => {
                if (!line.includes('!= null') && !line.includes('== null')) {
                    return;
                }

                const stripped = line.trim();
                if (stripped.startsWith('//') || stripped.startsWith('*')) {
                    return;
                }

                fileResults.push({
                    line: index + 1,
                    content: stripped,
                    category: classify(lines, index)
                });
            });

            if (fileResults.length > 0) {
                results[file] = fileResults;
            }
        } catch (error) {
            console.error(`Erro ao ler ${file}: ${error.message}`);
        }
    });

    return results;
}

function generateReport(results) {
    let auditContent = '';
    Object.keys(results).forEach(filePath => {
        auditContent += `File: ${filePath}\n`;
        results[filePath].forEach(item => {
            auditContent += `  L${item.line} [${item.category}]: ${item.content}\n`;
        });
        auditContent += '\n';
    });
    fs.writeFileSync(AUDIT_FILE, auditContent, 'utf-8');

    let markdown = '# Null checks analysis\n\n';
    markdown += '| Class | Total checks | Potentially redundant |\n';
    markdown += '|-------|--------------|-----------------------|\n';

    Object.entries(results)
        .sort((a, b) => b[1].length - a[1].length)
        .forEach(([filePath, items]) => {
            const filename = path.basename(filePath);
            const redundant = items.filter(item => item.category === 'POTENTIALLY_REDUNDANT').length;
            markdown += `| ${filename} | ${items.length} | ${redundant} |\n`;
        });

    fs.writeFileSync(ANALYSIS_FILE, markdown, 'utf-8');
}

try {
    if (process.argv.includes('--help') || process.argv.includes('-h')) {
        exibirAjudaComando({
            comandoSgc: 'java auditar-null',
            scriptDireto: 'java-auditar-null.cjs',
            descricao: 'Audita verificacoes de null no codigo Java e gera dois relatorios na raiz do repositorio.',
            exemplos: [
                'node etc/scripts/sgc.js backend java auditar-null'
            ]
        });
        process.exit(0);
    }

    console.log('Scanning files for null checks...');
    const data = scanFiles();
    console.log(`Found null checks in ${Object.keys(data).length} files.`);
    generateReport(data);
    console.log(`Reports generated: ${AUDIT_FILE}, ${ANALYSIS_FILE}`);
} catch (error) {
    console.error(`Erro ao auditar verificacoes null: ${error.message}`);
    process.exit(1);
}
