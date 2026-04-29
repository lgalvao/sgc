#!/usr/bin/env node
import fs from "node:fs/promises";
import path from "node:path";
import pc from "picocolors";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {exibirAjudaComando} from "./lib/cli-ajuda.js";
import {imprimirCabecalho, escreverLinha} from "../lib/saida.js";
import {globby} from "globby";

const SOURCE_DIR = resolverNaRaiz('backend/src/main/java/sgc');
const AUDIT_FILE = resolverNaRaiz('null-checks-audit.txt');
const ANALYSIS_FILE = resolverNaRaiz('null-checks-analysis.md');

function classify(lines, index) {
    const contextRange = 20;
    const start = Math.max(0, index - contextRange);
    const contextLines = lines.slice(start, index + 1).join('\n');
    return contextLines.includes('@Nullable') ? 'MAYBE_LEGIT' : 'POTENTIALLY_REDUNDANT';
}

async function scanFiles() {
    const results = {};
    const files = await globby(path.join(SOURCE_DIR, '**/*.java').replace(/\\/g, '/'), {absolute: true});

    for (const file of files) {
        try {
            const content = await fs.readFile(file, 'utf-8');
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
    }

    return results;
}

async function generateReport(results) {
    let auditContent = '';
    Object.keys(results).forEach(filePath => {
        auditContent += `File: ${filePath}\n`;
        results[filePath].forEach(item => {
            auditContent += `  L${item.line} [${item.category}]: ${item.content}\n`;
        });
        auditContent += '\n';
    });
    await fs.writeFile(AUDIT_FILE, auditContent, 'utf-8');

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

    await fs.writeFile(ANALYSIS_FILE, markdown, 'utf-8');
}

async function fixNullChecks(results) {
    let fixedCount = 0;

    for (const filePath of Object.keys(results)) {
        const items = results[filePath].filter(item => item.category === 'POTENTIALLY_REDUNDANT');
        if (items.length === 0) continue;

        let content = await fs.readFile(filePath, 'utf-8');
        let lines = content.split(/\r?\n/);
        let modified = false;

        // Análise heurística simplificada:
        // Se a verificação é var != null, e a variável foi recebida como parâmetro no escopo atual,
        // vamos tentar encontrar o parâmetro e adicionar @Nullable.
        // Como o parsing AST de Java em JS é complexo, vamos adotar uma abordagem heurística simples
        // para injetar @Nullable em métodos que tenham "if (var != null)" onde var corresponde a um parâmetro.

        for (const item of items) {
            const match = item.content.match(/if\s*\(\s*([a-zA-Z0-9_]+)\s*(?:!=|==)\s*null\s*\)/);
            if (!match) continue;

            const varName = match[1];
            
            // Buscar a declaração do método mais próximo antes desta linha
            for (let i = item.line - 2; i >= 0; i--) {
                const methodLine = lines[i];
                if (methodLine.includes('public') || methodLine.includes('private') || methodLine.includes('protected')) {
                    if (methodLine.includes('(') && methodLine.includes(')') && methodLine.includes(varName)) {
                        // Tentar injetar @Nullable
                        const paramRegex = new RegExp(`([^a-zA-Z0-9_@])([a-zA-Z0-9_<>\[\]]+)\s+${varName}\b`);
                        if (paramRegex.test(methodLine) && !methodLine.includes(`@Nullable`)) {
                            lines[i] = methodLine.replace(paramRegex, `$1@org.jspecify.annotations.Nullable $2 ${varName}`);
                            modified = true;
                            fixedCount++;
                            
                            // Adicionar o import se não houver
                            const hasImport = lines.some(l => l.includes('org.jspecify.annotations.Nullable'));
                            if (!hasImport) {
                                const packageIdx = lines.findIndex(l => l.startsWith('package '));
                                if (packageIdx !== -1) {
                                    lines.splice(packageIdx + 1, 0, '\nimport org.jspecify.annotations.Nullable;');
                                }
                            }
                        }
                    }
                    break; // Só analisa o escopo do método atual
                }
            }
        }

        if (modified) {
            await fs.writeFile(filePath, lines.join('\n'), 'utf-8');
        }
    }

    return fixedCount;
}

async function main() {
    const args = process.argv.slice(2);
    const fixMode = args.includes("--fix");

    if (args.includes('--help') || args.includes('-h')) {
        exibirAjudaComando({
            comandoSgc: 'java auditar-null',
            scriptDireto: 'java-auditar-null.js',
            descricao: 'Audita verificacoes de null no codigo Java e gera relatorios. Pode injetar @Nullable automaticamente com --fix.',
            opcoes: [
                '--fix     Injeta anotações @Nullable do JSpecify automaticamente em parâmetros de métodos onde verificações nulas são feitas.',
                '--help, -h Exibe esta ajuda.'
            ],
            exemplos: [
                'node etc/scripts/sgc.js backend java auditar-null',
                'node etc/scripts/sgc.js backend java auditar-null --fix'
            ]
        });
        process.exit(0);
    }

    imprimirCabecalho("AUDITORIA DE NULL CHECKS (JAVA)");
    escreverLinha('Escaneando arquivos...');
    const data = await scanFiles();
    escreverLinha(`Verificações encontradas em ${Object.keys(data).length} arquivos.`);
    
    await generateReport(data);
    escreverLinha(`Relatórios gerados: ${pc.dim(AUDIT_FILE)}, ${pc.dim(ANALYSIS_FILE)}`);

    if (fixMode) {
        escreverLinha('\nExecutando Auto-Fix (--fix)...');
        const fixed = await fixNullChecks(data);
        if (fixed > 0) {
            escreverLinha(pc.green(`✓ ${fixed} anotações @Nullable foram injetadas com sucesso.`));
        } else {
            escreverLinha(pc.yellow(`Nenhum local seguro para auto-fix foi encontrado.`));
        }
    } else {
        const totalRedundant = Object.values(data).flat().filter(i => i.category === 'POTENTIALLY_REDUNDANT').length;
        if (totalRedundant > 0) {
            escreverLinha(pc.yellow(`\n⚠️ ${totalRedundant} verificações potencialmente redundantes encontradas. Execute com --fix para injetar @Nullable.`));
        }
    }
}

main().catch(error => {
    console.error(`Erro ao auditar verificacoes null: ${error.message}`);
    process.exit(1);
});
