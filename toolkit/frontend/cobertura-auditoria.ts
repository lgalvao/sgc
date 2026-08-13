import fs from "node:fs/promises";
import path from "node:path";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {lerNumero, lerOpcao} from "../lib/cli-opcoes.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {extrairCoberturaFrontend, type ArquivoCobertura, type ResultadoCoberturaFrontend} from "../lib/dominios/cobertura-web.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";

const CAMINHO_PADRAO_SAIDA = "frontend-cobertura-auditoria.md";

interface ResumoMetricasFrontend {
    linhas: ResultadoCoberturaFrontend["linhas"];
    instrucoes: ResultadoCoberturaFrontend["instrucoes"];
    ramificacoes: ResultadoCoberturaFrontend["ramificacoes"];
    funcoes: ResultadoCoberturaFrontend["funcoes"];
}

interface PontoCriticoRelatorioFrontend {
    arquivo: string;
    scoreImpacto: number;
    coberturaLinhas: number;
    instrucoesTotal: number;
    instrucoesCobertas: number;
}

interface ResultadoAuditoriaFrontend {
    status: "ok";
    timestamp: string;
    totais: ResumoMetricasFrontend;
    hotspots: PontoCriticoRelatorioFrontend[];
}

function calcularPontuacaoImpacto(arquivo: ArquivoCobertura): number {
    // No frontend, focamos em instruções e ramificações.
    // Arquivos com muitas instruções descobertas e muitas ramificações são prioridade.
    const pesoInstrucoes = 1.0;
    const pesoRamificacoes = 1.5;

    const instrucoesDescobertas = arquivo.instrucoesTotal - arquivo.instrucoesCobertas;
    const ramificacoesDescobertas = Math.floor(arquivo.ramificacoesTotal * (1 - arquivo.ramificacoesPercentual / 100));

    return (instrucoesDescobertas * pesoInstrucoes) + (ramificacoesDescobertas * pesoRamificacoes);
}

function obterPrioridade(pontuacao: number): string {
    if (pontuacao > 100) return pc.red("P1 (Crítico)");
    if (pontuacao > 40) return pc.yellow("P2 (Alto)");
    return pc.cyan("P3 (Médio)");
}

async function gerarRelatorioMarkdown(dados: ResultadoAuditoriaFrontend, caminho: string): Promise<string> {
    const {totais, hotspots} = dados;
    let markdown = "# Auditoria de Cobertura Frontend\n\n";

    markdown += "## Resumo Geral\n";
    markdown += `- **Cobertura de Linhas:** ${totais.linhas.percentual}%\n`;
    markdown += `- **Cobertura de Instruções:** ${totais.instrucoes.percentual}%\n`;
    markdown += `- **Cobertura de Ramificações:** ${totais.ramificacoes.percentual}%\n`;
    markdown += `- **Cobertura de Funções:** ${totais.funcoes.percentual}%\n\n`;

    markdown += "## Top 10 Pontos Críticos de Qualidade (Maior Risco)\n";
    markdown += "Prioridade baseada em volume de código não testado e complexidade condicional.\n\n";
    markdown += "| Posição | Arquivo | Pontuação | Instruções Descobertas | Cobertura Linhas | Prioridade |\n";
    markdown += "|---------|---------|-----------|-------------------------|------------------|------------|\n";

    hotspots.forEach((ponto, indice) => {
        const prioridade = ponto.scoreImpacto > 100 ? "P1" : (ponto.scoreImpacto > 40 ? "P2" : "P3");
        markdown += `| ${indice + 1} | \`${ponto.arquivo}\` | ${ponto.scoreImpacto.toFixed(1)} | ${ponto.instrucoesTotal - ponto.instrucoesCobertas} | ${ponto.coberturaLinhas}% | ${prioridade} |\n`;
    });

    markdown += `\n\n_Gerado automaticamente pelo toolkit em ${new Date().toLocaleString("pt-BR")}._\n`;

    await fs.mkdir(path.dirname(caminho), {recursive: true});
    await fs.writeFile(caminho, markdown, "utf8");
    return caminho;
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoSgc: 'frontend cobertura auditoria',
            scriptDireto: 'frontend/cobertura-auditoria.ts',
            descricao: 'Auditoria unificada de cobertura e risco (Frontend).',
            opcoes: [
                '--json     Saída em formato JSON para integração com outras ferramentas.',
                '--saida <arquivo>   Caminho do arquivo Markdown a ser gerado.',
                '--arquivo <arquivo> Usa um relatório V8 específico.',
                '--base <diretorio> Resolve o relatório relativo a outra base.',
                '--gravar             Persiste o relatório Markdown.',
                '--minimo <percentual> Falha se a cobertura de linhas for menor que N.'
            ]
        });
        return;
    }

    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const arquivo = lerOpcao(argumentos, "--arquivo", undefined);
    const caminhoSaida = path.resolve(diretorioBase, lerOpcao(argumentos, "--saida", CAMINHO_PADRAO_SAIDA) ?? CAMINHO_PADRAO_SAIDA);
    const gravar = argumentos.includes("--gravar");
    const metaMinima = lerNumero(argumentos, "--minimo", 0, {inteiro: false, minimo: 0, maximo: 100}) ?? 0;

    if (!emitirJson) {
        imprimirCabecalho("AUDITORIA DE COBERTURA FRONTEND");
    }

    try {
        const coleta = await extrairCoberturaFrontend(arquivo, {diretorioBase});

        const pontosCriticos = coleta.arquivos
            .map(a => ({
                ...a,
                pontuacaoImpacto: calcularPontuacaoImpacto(a)
            }))
            .filter(a => a.pontuacaoImpacto > 0)
            .toSorted((a, b) => b.pontuacaoImpacto - a.pontuacaoImpacto)
            .slice(0, 20);

        const resultado: ResultadoAuditoriaFrontend = {
            status: "ok",
            timestamp: new Date().toISOString(),
            totais: {
                linhas: coleta.linhas,
                instrucoes: coleta.instrucoes,
                ramificacoes: coleta.ramificacoes,
                funcoes: coleta.funcoes
            },
            hotspots: pontosCriticos.map(ponto => ({
                arquivo: ponto.arquivo,
                scoreImpacto: ponto.pontuacaoImpacto,
                coberturaLinhas: ponto.linhasPercentual,
                instrucoesTotal: ponto.instrucoesTotal,
                instrucoesCobertas: ponto.instrucoesCobertas
            }))
        };

        if (gravar) {
            await gerarRelatorioMarkdown(resultado, caminhoSaida);
        }

        if (emitirJson) {
            imprimirJson(resultado);
            if (metaMinima > 0 && coleta.linhas.percentual < metaMinima) process.exitCode = 1;
            return;
        }

        escreverLinha(`${pc.bold("Resumo do Projeto:")}`);
        escreverLinha(`  Linhas:          ${coleta.linhas.percentual}%`);
        escreverLinha(`  Instruções:      ${coleta.instrucoes.percentual}%`);
        escreverLinha(`  Ramificações:    ${coleta.ramificacoes.percentual}%`);
        escreverLinha(`  Funções:         ${coleta.funcoes.percentual}%`);
        escreverLinha("");

        escreverLinha(pc.bold(pc.underline("TOP 5 PENDÊNCIAS PRIORITÁRIAS:")));
        pontosCriticos.slice(0, 5).forEach((ponto, indice) => {
            escreverLinha(`${indice + 1}. ${pc.bold(ponto.arquivo)}`);
            escreverLinha(`   Impacto: ${pc.bold(ponto.pontuacaoImpacto.toFixed(1))} | Prioridade: ${obterPrioridade(ponto.pontuacaoImpacto)}`);
            escreverLinha(`   Lacuna: ${ponto.instrucoesTotal - ponto.instrucoesCobertas} instruções sem teste.`);
        });

        if (gravar) {
            escreverLinha(`\n${pc.green("✓")} Relatório detalhado gerado em: ${pc.dim(path.relative(diretorioBase, caminhoSaida).replaceAll("\\", "/"))}`);
        }

        if (metaMinima > 0 && coleta.linhas.percentual < metaMinima) {
            escreverLinha(pc.red(`\nFALHA: Cobertura de linhas (${coleta.linhas.percentual}%) abaixo da meta (${metaMinima}%).`));
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
