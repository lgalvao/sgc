#!/usr/bin/env node
import fs from "node:fs/promises";
import path from "node:path";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
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
        md += `| ${i + 1} | \`${h.arquivo}\` | ${h.scoreImpacto.toFixed(1)} | ${h.statementsTotal - h.statementsCobertos} | ${h.coberturaLinhas}% | ${prioridade} |\n`;
    });

    md += `\n\n_Gerado automaticamente pelo toolkit SGC em ${new Date().toLocaleString('pt-BR')}._\n`;

    await fs.mkdir(path.dirname(caminho), {recursive: true});
    await fs.writeFile(caminho, md, "utf8");
    return caminho;
}

async function principal(argumentos = process.argv.slice(2)) {
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoSgc: 'frontend cobertura auditoria',
            scriptDireto: 'frontend/cobertura-auditoria.js',
            descricao: 'Auditoria unificada de cobertura e risco (Frontend).',
            opcoes: [
                '--json     Saída em formato JSON para integração com outras ferramentas.',
                '--output <arquivo> Caminho do arquivo Markdown a ser gerado.',
                '--arquivo <arquivo> Usa um relatório V8 específico.',
                '--base <diretorio> Resolve o relatório relativo a outra base.',
                '--min <percentual> Falha se a cobertura de linhas for menor que N.'
            ]
        });
        return;
    }

    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ));
    const arquivo = lerOpcao(argumentos, "--arquivo", undefined);
    const caminhoSaida = path.resolve(diretorioBase, lerOpcao(argumentos, "--output", CAMINHO_PADRAO_OUTPUT));
    const metaMinima = Number(lerOpcao(argumentos, "--min", "0"));

    if (!emitirJson) {
        imprimirCabecalho("AUDITORIA DE COBERTURA FRONTEND");
    }

    try {
        const coleta = await extrairCoberturaFrontend(arquivo, {diretorioBase});

        const hotspots = coleta.arquivos
            .map(a => ({
                ...a,
                scoreImpacto: calcularScoreImpacto(a)
            }))
            .filter(a => a.scoreImpacto > 0)
            .toSorted((a, b) => b.scoreImpacto - a.scoreImpacto)
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

        if (emitirJson) {
            imprimirJson(resultado);
            if (metaMinima > 0 && coleta.lines.percentual < metaMinima) process.exitCode = 1;
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

        await gerarRelatorioMarkdown(resultado, caminhoSaida);
        escreverLinha(`\n${pc.green("✓")} Relatório detalhado gerado em: ${pc.dim(path.relative(process.cwd(), caminhoSaida).replaceAll("\\", "/"))}`);

        if (metaMinima > 0 && coleta.lines.percentual < metaMinima) {
            escreverLinha(pc.red(`\nFALHA: Cobertura de linhas (${coleta.lines.percentual}%) abaixo da meta (${metaMinima}%).`));
            process.exitCode = 1;
        }

    } catch (erro) {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        if (emitirJson) {
            imprimirJson({status: "erro", mensagem});
        } else {
            escreverLinha(pc.red(`Erro na auditoria: ${mensagem}`));
        }
        process.exitCode = 1;
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro na auditoria de cobertura: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    principal,
};
