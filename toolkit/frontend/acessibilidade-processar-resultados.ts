#!/usr/bin/env node
import fs from "node:fs/promises";
import path from "node:path";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverErro, escreverLinha} from "../lib/saida.js";

interface NoAcessibilidade {
    target: string[];
}

interface ViolacaoAcessibilidade {
    impact: string | null;
    id: string;
    help: string;
    helpUrl: string;
    description: string;
    nodes: NoAcessibilidade[];
}

interface ResultadoPaginaAcessibilidade {
    name: string;
    route: string;
    violations: ViolacaoAcessibilidade[];
}

interface ResultadoProcessamentoAcessibilidade {
    entrada: string;
    saida: string;
    paginas: number;
}

function ehObjeto(valor: unknown): valor is Record<string, unknown> {
    return typeof valor === "object" && valor !== null && !Array.isArray(valor);
}

function exigirTexto(registro: Record<string, unknown>, chave: string): string {
    const valor = registro[chave];
    if (typeof valor !== "string") {
        throw new Error(`Resultado de acessibilidade invalido: ${chave} deve ser texto.`);
    }
    return valor;
}

function normalizarResultados(valor: unknown): ResultadoPaginaAcessibilidade[] {
    if (!Array.isArray(valor)) {
        throw new Error("Resultado de acessibilidade invalido: a raiz deve ser uma lista de paginas.");
    }

    return valor.map((item, indice) => {
        if (!ehObjeto(item) || !Array.isArray(item.violations)) {
            throw new Error(`Resultado de acessibilidade invalido na pagina ${indice + 1}.`);
        }

        return {
            name: exigirTexto(item, "name"),
            route: exigirTexto(item, "route"),
            violations: item.violations.map((violacao, indiceViolacao) => {
                if (!ehObjeto(violacao) || !Array.isArray(violacao.nodes)) {
                    throw new Error(`Violacao invalida na pagina ${indice + 1}, item ${indiceViolacao + 1}.`);
                }

                return {
                    impact: typeof violacao.impact === "string" ? violacao.impact : null,
                    id: exigirTexto(violacao, "id"),
                    help: exigirTexto(violacao, "help"),
                    helpUrl: exigirTexto(violacao, "helpUrl"),
                    description: exigirTexto(violacao, "description"),
                    nodes: violacao.nodes.map((no, indiceNo) => {
                        if (!ehObjeto(no) || !Array.isArray(no.target) || !no.target.every(alvo => typeof alvo === "string")) {
                            throw new Error(`No invalido na pagina ${indice + 1}, violacao ${indiceViolacao + 1}, item ${indiceNo + 1}.`);
                        }
                        return {target: no.target};
                    })
                };
            })
        };
    });
}

function gerarRelatorioMarkdown(resultados: ResultadoPaginaAcessibilidade[]): string {
    const linhas = [
        "# Relatorio completo de acessibilidade",
        "",
        `Data da auditoria: ${new Date().toLocaleString("pt-BR")}`,
        "",
        "## Resumo geral",
        "",
        `- Paginas auditadas: ${resultados.length}`,
        `- Total de violacoes: ${resultados.reduce((total, pagina) => total + pagina.violations.length, 0)}`,
        "",
        "## Detalhamento por pagina",
        ""
    ];

    for (const pagina of resultados) {
        linhas.push(`### ${pagina.name} (\`${pagina.route}\`)`, "");
        if (pagina.violations.length === 0) {
            linhas.push("Nenhuma violacao detectada.", "");
            continue;
        }

        linhas.push(
            "| Impacto | Problema | Elementos afetados |",
            "|:---:|:---|:---|"
        );
        for (const violacao of pagina.violations) {
            const elementos = violacao.nodes.map((no) => `\`${no.target.join(" ")}\``).join("<br>");
            linhas.push(`| ${violacao.impact} | **${violacao.id}**: ${violacao.help} | ${elementos} |`);
        }
        linhas.push("");
    }

    linhas.push("## Recomendacoes tecnicas", "");
    const recomendacoes = new Set<string>();
    for (const pagina of resultados) {
        for (const violacao of pagina.violations) {
            recomendacoes.add(`- **${violacao.id}**: ${violacao.description} [Referencia](${violacao.helpUrl})`);
        }
    }
    linhas.push(...recomendacoes, "");
    return `${linhas.join("\n")}\n`;
}

interface OpcoesProcessamentoAcessibilidade {
    entrada: string;
    saida: string;
}

async function processarResultadosAcessibilidade({entrada, saida}: OpcoesProcessamentoAcessibilidade): Promise<ResultadoProcessamentoAcessibilidade> {
    const conteudo = await fs.readFile(entrada, "utf8");
    const resultados = normalizarResultados(JSON.parse(conteudo) as unknown);
    await fs.mkdir(path.dirname(saida), {recursive: true});
    await fs.writeFile(saida, gerarRelatorioMarkdown(resultados), "utf8");
    return {entrada, saida, paginas: resultados.length};
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<ResultadoProcessamentoAcessibilidade | undefined> {
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "frontend acessibilidade processar",
            scriptDireto: "frontend/acessibilidade-processar-resultados.ts",
            descricao: "Consolida os resultados JSON do Axe em um relatorio Markdown.",
            opcoes: [
                "--entrada <arquivo> Arquivo JSON produzido pelo crawler.",
                "--saida <arquivo>   Relatorio Markdown de acessibilidade.",
                "--base <diretorio>   Resolve os caminhos padrao a partir de outra base."
            ]
        });
        return;
    }

    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const entrada = path.resolve(diretorioBase, lerOpcao(argumentos, "--entrada", "a11y-scan-results.json") ?? "a11y-scan-results.json");
    const saidaPadrao = path.join(
        resolverCaminhoConfigurado("artefatosQualidade", diretorioBase),
        "acessibilidade",
        "relatorio.md"
    );
    const saida = path.resolve(diretorioBase, lerOpcao(argumentos, "--saida", saidaPadrao) ?? saidaPadrao);
    const resultado = await processarResultadosAcessibilidade({entrada, saida});
    escreverLinha(`Relatorio de acessibilidade gerado em: ${resultado.saida}`);
    return resultado;
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverErro(`Erro ao processar acessibilidade: ${mensagem}\n`);
        process.exitCode = 1;
    });
}

export {
    gerarRelatorioMarkdown,
    normalizarResultados,
    processarResultadosAcessibilidade,
    principal,
};
