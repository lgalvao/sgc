#!/usr/bin/env node
import fs from "node:fs/promises";
import pc from "picocolors";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {extrairCoberturaJacoco} from "../lib/dominios/cobertura-java.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";

const CAMINHO_PADRAO_OUTPUT = "backend-coverage-auditoria.md";

function calcularScoreImpacto(classe) {
    // Fatores de risco: 
    // Classes complexas com lacunas são os verdadeiros hotspots.
    // Se não há lacunas (100% de cobertura), o score deve ser 0.

    if (classe.linhasPerdidas === 0 && classe.branchesPerdidos === 0) {
        return 0;
    }

    const pesoLinhas = 1.0;
    const pesoBranches = 1.5;
    const fatorComplexidade = 1 + (classe.complexidade / 50); // Multiplicador baseado na complexidade

    const scoreLacunas = (classe.linhasPerdidas * pesoLinhas) +
        (classe.branchesPerdidos * pesoBranches);

    return scoreLacunas * fatorComplexidade;
}

function obterPrioridade(score) {
    if (score > 50) return pc.red("P1 (Crítico)");
    if (score > 20) return pc.yellow("P2 (Alto)");
    return pc.cyan("P3 (Médio)");
}

async function gerarRelatorioMarkdown(dados, caminho) {
    const {totais, hotspots} = dados;
    let md = `# Auditoria de Cobertura Backend - SGC\n\n`;

    md += `## Resumo Geral\n`;
    md += `- **Cobertura Global (Instruções):** ${totais.instrucoes.percentual}%\n`;
    md += `- **Cobertura de Linhas:** ${totais.linhas.percentual}%\n`;
    md += `- **Cobertura de Branches:** ${totais.branches.percentual}%\n`;
    md += `- **Complexidade Total:** ${totais.complexidade.cobertos + totais.complexidade.perdidos}\n\n`;

    md += `## Top 10 Hotspots de Qualidade (Maior Risco)\n`;
    md += `Prioridade baseada em complexidade ciclomática cruzada com lacunas de teste.\n\n`;
    md += `| Rank | Classe | Score | Complexidade | Linhas S/ Cobertura | Branches S/ Cobertura | Prioridade |\n`;
    md += `|------|--------|-------|--------------|---------------------|-----------------------|------------|\n`;

    hotspots.slice(0, 10).forEach((h, i) => {
        const prioridade = h.scoreImpacto > 50 ? "P1" : (h.scoreImpacto > 20 ? "P2" : "P3");
        md += `| ${i + 1} | \`${h.nome}\` | ${h.scoreImpacto.toFixed(1)} | ${h.complexidade} | ${h.linhasPerdidas} | ${h.branchesPerdidos} | ${prioridade} |\n`;
    });

    md += `\n## Detalhamento das Lacunas dos Principais Hotspots\n\n`;
    hotspots.slice(0, 10).forEach((h) => {
        if (h.linhasPerdidas > 0 || h.branchesPerdidos > 0) {
            md += `### \`${h.nome}\` (Risco: ${h.scoreImpacto.toFixed(1)})\n`;
            if (h.linhasPerdidas > 0) {
                md += `- **Linhas 100% descobertas:** ${h.linhasPerdidasLista.join(", ")}\n`;
            }
            if (h.branchesPerdidos > 0) {
                md += `- **Branches sem cobertura total/parcial (linha(perdidos/total)):** ${h.branchesPerdidosLista.join(", ")}\n`;
            }
            md += `\n`;
        }
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
            comandoSgc: 'backend cobertura auditoria',
            scriptDireto: 'backend/cobertura-auditoria.js',
            descricao: 'Auditoria unificada de cobertura e risco (Backend).',
            opcoes: [
                '--json     Saída em formato JSON para integração com outras ferramentas.',
                '--output=X Caminho do arquivo Markdown a ser gerado (Padrão: backend-coverage-auditoria.md).',
                '--min=N    Falha (exit 1) se a cobertura global for menor que N.'
            ]
        });
        process.exit(0);
    }

    const outputArg = args.find(a => a.startsWith("--output="))?.split("=")[1] || CAMINHO_PADRAO_OUTPUT;
    const metaMinima = Number(args.find(a => a.startsWith("--min="))?.split("=")[1] || "0");

    if (!jsonMode) {
        imprimirCabecalho("AUDITORIA DE COBERTURA BACKEND");
    }

    try {
        const coleta = await extrairCoberturaJacoco(undefined, {
            incluirSemLacunas: true,
            aplicarExclusoes: true
        });

        const hotspots = coleta.classes
            .map(c => ({
                ...c,
                scoreImpacto: calcularScoreImpacto(c)
            }))
            .filter(c => c.scoreImpacto > 0)
            .sort((a, b) => b.scoreImpacto - a.scoreImpacto)
            .slice(0, 20);

        const resultado = {
            status: "ok",
            timestamp: new Date().toISOString(),
            totais: coleta,
            hotspots: hotspots.map(h => ({
                nome: h.nome,
                complexidade: h.complexidade,
                linhasPerdidas: h.linhasPerdidas,
                linhasPerdidasLista: h.linhasPerdidasLista,
                branchesPerdidos: h.branchesPerdidos,
                branchesPerdidosLista: h.branchesPerdidosLista,
                scoreImpacto: h.scoreImpacto,
                coberturaLinhas: h.linhasPercentual
            }))
        };

        if (jsonMode) {
            imprimirJson(resultado);
            return;
        }

        escreverLinha(`${pc.bold("Resumo do Projeto:")}`);
        escreverLinha(`  Instruções: ${coleta.instrucoes.percentual}%`);
        escreverLinha(`  Linhas:      ${coleta.linhas.percentual}%`);
        escreverLinha(`  Branches:    ${coleta.branches.percentual}%`);
        escreverLinha("");

        escreverLinha(pc.bold(pc.underline("TOP 5 PENDÊNCIAS PRIORITÁRIAS:")));
        hotspots.slice(0, 5).forEach((h, i) => {
            escreverLinha(`${i + 1}. ${pc.bold(h.nome)}`);
            escreverLinha(`   Impacto: ${pc.bold(h.scoreImpacto.toFixed(1))} | Prioridade: ${obterPrioridade(h.scoreImpacto)}`);
            escreverLinha(`   Lacunas: ${h.linhasPerdidas} linhas e ${h.branchesPerdidos} branches sem teste.`);
            if (h.linhasPerdidas > 0) {
                escreverLinha(`     ↳ Linhas sem cobertura: ${pc.dim(h.linhasPerdidasLista.join(", "))}`);
            }
            if (h.branchesPerdidos > 0) {
                escreverLinha(`     ↳ Branches sem cobertura (linha(perdidos/total)): ${pc.dim(h.branchesPerdidosLista.join(", "))}`);
            }
        });

        const caminhoRelatorio = resolverNaRaiz(outputArg);
        await gerarRelatorioMarkdown(resultado, caminhoRelatorio);
        escreverLinha(`\n${pc.green("✓")} Relatório detalhado gerado em: ${pc.dim(outputArg)}`);

        if (metaMinima > 0 && coleta.instrucoes.percentual < metaMinima) {
            escreverLinha(pc.red(`\nFALHA: Cobertura global (${coleta.instrucoes.percentual}%) abaixo da meta (${metaMinima}%).`));
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
