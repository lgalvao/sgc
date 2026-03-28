#!/usr/bin/env node
const fs = require('node:fs');
const path = require('node:path');
const {exibirAjudaComando} = require('./lib/cli-ajuda.cjs');

const TARGET_DIRS = ['src/test/java', 'src/main/java'];
const PACKAGE_PREFIX = 'package ';
const IMPORT_PREFIX = 'import ';
const STATIC_PREFIX = 'static ';
const MATCH_PTRN = /("[^"]*")|(\b([a-z]\w*(?:\.[a-z]\w*)+)\.([A-Z]\w*)\b)/g;

function shouldIgnoreFqn(packagePart, classPart) {
    if (classPart === 'Assertions') {
        return true;
    }
    return packagePart === 'java.lang';
}

function parseImports(lines) {
    let currentPackage = null;
    const existingImports = new Map();
    const importLineIndices = [];

    lines.forEach((line, index) => {
        const stripped = line.trim();
        if (stripped.startsWith(PACKAGE_PREFIX)) {
            currentPackage = stripped.split('//')[0].replace(PACKAGE_PREFIX, '').replace(';', '').trim();
            return;
        }

        if (!stripped.startsWith(IMPORT_PREFIX)) {
            return;
        }

        importLineIndices.push(index);
        let clean = stripped.split('//')[0].replace(IMPORT_PREFIX, '').replace(';', '').trim();
        if (clean.startsWith(STATIC_PREFIX)) {
            clean = clean.replace(STATIC_PREFIX, '').trim();
        }

        const simpleName = clean.split('.').pop();
        if (simpleName !== '*') {
            existingImports.set(simpleName, clean);
        }
    });

    return {currentPackage, existingImports, importLineIndices};
}

function getInsertPosition(lines, importLineIndices) {
    if (importLineIndices.length > 0) {
        return importLineIndices[importLineIndices.length - 1] + 1;
    }

    for (let index = 0; index < lines.length; index++) {
        if (lines[index].trim().startsWith(PACKAGE_PREFIX)) {
            return index + 1;
        }
    }

    return 0;
}

function checkCollision(simpleName, fqn, newImportsToAdd) {
    return [...newImportsToAdd].some(item => item.split('.').pop() === simpleName && item !== fqn);
}

function determineReplacement(match, currentPackage, existingImports, newImportsToAdd) {
    if (match[1]) {
        return {replacement: match[1], changed: false};
    }

    const fullMatch = match[2];
    const pkg = match[3];
    const cls = match[4];
    const fqn = `${pkg}.${cls}`;

    let shouldReplace = false;

    if (shouldIgnoreFqn(pkg, cls)) {
        shouldReplace = true;
    } else if (currentPackage && pkg === currentPackage) {
        shouldReplace = true;
    } else if (existingImports.has(cls)) {
        shouldReplace = existingImports.get(cls) === fqn;
    } else if (!checkCollision(cls, fqn, newImportsToAdd)) {
        newImportsToAdd.add(fqn);
        shouldReplace = true;
    }

    return {
        replacement: shouldReplace ? cls : fullMatch,
        changed: shouldReplace
    };
}

function scanLines(lines, currentPackage, existingImports) {
    const newImportsToAdd = new Set();
    const modifiedLines = [];
    let hasModifications = false;

    lines.forEach(line => {
        const stripped = line.trim();
        if (stripped.startsWith(PACKAGE_PREFIX) || stripped.startsWith(IMPORT_PREFIX) || stripped.startsWith('//') || stripped.startsWith('*')) {
            modifiedLines.push(line);
            return;
        }

        const newLine = line.replace(MATCH_PTRN, (...args) => {
            const {replacement, changed} = determineReplacement(args, currentPackage, existingImports, newImportsToAdd);
            if (changed) {
                hasModifications = true;
            }
            return replacement;
        });
        modifiedLines.push(newLine);
    });

    return {modifiedLines, newImportsToAdd, hasModifications};
}

function processFile(filePath, dryRun = false) {
    const lines = fs.readFileSync(filePath, 'utf-8').split(/\r?\n/).map((line, index, all) => (
        index < all.length - 1 ? `${line}\n` : line
    ));
    const {currentPackage, existingImports, importLineIndices} = parseImports(lines);
    const {modifiedLines, newImportsToAdd, hasModifications} = scanLines(lines, currentPackage, existingImports);

    if (!hasModifications && newImportsToAdd.size === 0) {
        return false;
    }

    const insertPos = getInsertPosition(lines, importLineIndices);
    const sortedNew = [...newImportsToAdd].sort((a, b) => a.localeCompare(b, 'pt-BR'));
    const finalOutput = [];

    if (insertPos === 0) {
        sortedNew.forEach(item => finalOutput.push(`${IMPORT_PREFIX}${item};\n`));
    }

    modifiedLines.forEach((line, index) => {
        finalOutput.push(line);
        if (index === insertPos - 1) {
            sortedNew.forEach(item => finalOutput.push(`${IMPORT_PREFIX}${item};\n`));
        }
    });

    if (!dryRun) {
        fs.writeFileSync(filePath, finalOutput.join(''), 'utf-8');
    }

    console.log(`${dryRun ? '[dry-run] ' : ''}Atualizado: ${filePath} (${sortedNew.length} novo(s) import(s))`);
    return true;
}

function findBackendRoot() {
    let current = path.dirname(__filename);
    while (current !== path.parse(current).root) {
        if (fs.existsSync(path.join(current, 'src'))) {
            return current;
        }
        current = path.dirname(current);
    }
    return process.cwd();
}

function parseArgs(argv) {
    return {
        help: argv.includes('--help') || argv.includes('-h'),
        dryRun: argv.includes('--dry-run')
    };
}

function main() {
    const {dryRun, help} = parseArgs(process.argv.slice(2));
    if (help) {
        exibirAjudaComando({
            comandoSgc: 'java corrigir-fqn',
            scriptDireto: 'java-corrigir-fqn.cjs',
            descricao: 'Substitui nomes totalmente qualificados por imports em arquivos Java.',
            opcoes: [
                '--dry-run     Apenas mostra os arquivos que seriam alterados',
                '--help, -h    Exibe esta ajuda'
            ],
            exemplos: [
                'node backend/etc/scripts/sgc.cjs java corrigir-fqn --dry-run',
                'node backend/etc/scripts/sgc.cjs java corrigir-fqn'
            ]
        });
        process.exit(0);
    }

    const backendRoot = findBackendRoot();
    let totalFilesAnalyzed = 0;
    let totalFilesUpdated = 0;

    console.log('Procurando FQNs no projeto...');
    console.log(`Raiz do backend resolvida: ${backendRoot}`);

    TARGET_DIRS.forEach(relativeDir => {
        const targetDir = path.join(backendRoot, relativeDir);
        if (!fs.existsSync(targetDir)) {
            console.log(`Diretorio nao encontrado: ${targetDir}`);
            return;
        }

        console.log(`Processando diretorio: ${targetDir}`);
        const stack = [targetDir];
        while (stack.length > 0) {
            const currentDir = stack.pop();
            const entries = fs.readdirSync(currentDir, {withFileTypes: true});
            entries.forEach(entry => {
                const fullPath = path.join(currentDir, entry.name);
                if (entry.isDirectory()) {
                    stack.push(fullPath);
                    return;
                }

                if (!entry.name.endsWith('.java')) {
                    return;
                }

                totalFilesAnalyzed++;
                if (processFile(fullPath, dryRun)) {
                    totalFilesUpdated++;
                }
            });
        }
    });

    console.log(`Total de arquivos analisados: ${totalFilesAnalyzed}`);
    console.log(`Total de arquivos atualizados: ${totalFilesUpdated}`);
}

try {
    main();
} catch (error) {
    console.error(`Erro ao ajustar FQNs: ${error.message}`);
    process.exit(1);
}
