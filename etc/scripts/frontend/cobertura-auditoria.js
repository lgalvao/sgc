#!/usr/bin/env node
import fs from "node:fs/promises";
import pc from "picocolors";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {extrairCoberturaFrontend} from "../lib/dominios/cobertura-web.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";

const CAMINHO_PADRAO_OUTPUT = "frontend-coverage-auditoria.md";

function calcularScoreImpacto(arquivo) {
    // No frontend, focamos em statements e branches.
    // Arquivos com muitos statements descobertos e muitos branches são prioridade.
    const pesoStatements = 1.0;
    const pesoBranches = 1.5;

    const statementsDescobertos = arquivo.statementsTotal - arquivo.statementsCobertos;
    const branchesDescobertos = Math.floor(arquivo.branchesTotal * (1 - arquivo.branchesPercentual / 100));

    return (statementsDescobertos * pesoStatements) + (branchesDescobertos * pesoBranches);
}

function obterPrioridade(score) {
    if (score > 100) return pc.red("P1 (Crítico)");
    if (score > 40) return pc.yellow("P2 (Alto)");
    return pc.cyan("P3 (Médio)");
}

async function gerarRelatorioMarkdown(dados, caminho) {
    const {totais, hotspots} = dados;
    let md = "# Auditoria de Cobertura Frontend - SGC\n\n";
    
    md += "## Resumo Geral\n";
    md += `- **Cobertura de Linhas:** ${totais.lines.percentual}%\n`;
    md += `- **Cobertura de Statements:** ${totais.statements.percentual}%\n`;
    md += `- **Cobertura de Branches:** ${totais.branches.percentual}%\n`;
    md += `- **Cobertura de Funções:** ${totais.functions.percentual}%\n\n`;

    md += "## Top 10 Hotspots de Qualidade (Maior Risco)\n";
    md += "Prioridade baseada em volume de código não testado e complexidade condicional.\n\n";
    md += "| Rank | Arquivo | Score | Statements Descobertos | Cobertura Linhas | Prioridade |\n";
    md += "|------|---------|-------|-------------------------|------------------|------------|\n";

    hotspots.forEach((h, i) => {
        const prioridade = h.scoreImpacto > 100 ? "P1" : (h.scoreImpacto > 40 ? "P2" : "P3");
        md += `| ${i + 1} | \`${h.arquivo}\` | ${h.scoreImpacto.toFixed(1)} | ${h.statementsTotal - h.statementsCobertos} | ${h.linesPercentual}% | ${prioridade} |\n`;
    });

    md += `\n\n_Gerado automaticamente pelo toolkit SGC em ${new Date().toLocaleString('pt-BR')}._\n`;
    
    await fs.writeFile(caminho, md, "utf8");
    return caminho;
}

async function main() {
    const args = process.argv.slice(2);
    const jsonMode = args.includes("--json");
    const helpMode = args.includes("--help") || args.includes("-h");

    if (helpMode) {
        exibirAjudaComando({
            comandoSgc: 'frontend cobertura auditoria',
            scriptDireto: 'frontend/cobertura-auditoria.js',
            descricao: 'Auditoria unificada de cobertura e risco (Frontend).',
            opcoes: [
                '--json     Saída em formato JSON para integração com outras ferramentas.',
                '--output=X Caminho do arquivo Markdown a ser gerado (Padrão: frontend-coverage-auditoria.md).',
                '--min=N    Falha (exit 1) se a cobertura de linhas for menor que N.'
            ]
        });
        process.exit(0);
    }

    const outputArg = args.find(a => a.startsWith("--output="))?.split("=")[1] || CAMINHO_PADRAO_OUTPUT;
    const metaMinima = Number(args.find(a => a.startsWith("--min="))?.split("=")[1] || "0");

    if (!jsonMode) {
        imprimirCabecalho("AUDITORIA DE COBERTURA FRONTEND");
    }

    try {
        const coleta = await extrairCoberturaFrontend();

        const hotspots = coleta.arquivos
            .map(a => ({
                ...a,
                scoreImpacto: calcularScoreImpacto(a)
            }))
            .filter(a => a.scoreImpacto > 0)
            .sort((a, b) => b.scoreImpacto - a.scoreImpacto)
            .slice(0, 20);

        const resultado = {
            status: "ok",
            timestamp: new Date().toISOString(),
            totais: {
                lines: coleta.lines,
                statements: coleta.statements,
                branches: coleta.branches,
                functions: coleta.functions
            },
            hotspots: hotspots.map(h => ({
                arquivo: h.arquivo,
                scoreImpacto: h.scoreImpacto,
                coberturaLinhas: h.linesPercentual,
                statementsTotal: h.statementsTotal,
                statementsCobertos: h.statementsCobertos
            }))
        };

        if (jsonMode) {
            imprimirJson(resultado);
            return;
        }

        escreverLinha(`${pc.bold("Resumo do Projeto:")}`);
        escreverLinha(`  Linhas:      ${coleta.lines.percentual}%`);
        escreverLinha(`  Statements:  ${coleta.statements.percentual}%`);
        escreverLinha(`  Branches:    ${coleta.branches.percentual}%`);
        escreverLinha("");

        escreverLinha(pc.bold(pc.underline("TOP 5 PENDÊNCIAS PRIORITÁRIAS:")));
        hotspots.slice(0, 5).forEach((h, i) => {
            escreverLinha(`${i + 1}. ${pc.bold(h.arquivo)}`);
            escreverLinha(`   Impacto: ${pc.bold(h.scoreImpacto.toFixed(1))} | Prioridade: ${obterPrioridade(h.scoreImpacto)}`);
            escreverLinha(`   Lacuna: ${h.statementsTotal - h.statementsCobertos} statements sem teste.`);
        });

        const caminhoRelatorio = resolverNaRaiz(outputArg);
        await gerarRelatorioMarkdown(resultado, caminhoRelatorio);
        escreverLinha(`\n${pc.green("✓")} Relatório detalhado gerado em: ${pc.dim(outputArg)}`);

        if (metaMinima > 0 && coleta.lines.percentual < metaMinima) {
            escreverLinha(pc.red(`\nFALHA: Cobertura de linhas (${coleta.lines.percentual}%) abaixo da meta (${metaMinima}%).`));
            process.exit(1);
        }

    } catch (erro) {
        if (jsonMode) {
            imprimirJson({status: "erro", mensagem: erro.message});
        } else {
            escreverLinha(pc.red(`Erro na auditoria: ${erro.message}`));
        }
        process.exit(1);
    }
}

main();
