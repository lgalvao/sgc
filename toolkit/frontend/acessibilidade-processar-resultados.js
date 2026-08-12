#!/usr/bin/env node
import fs from "node:fs/promises";
import path from "node:path";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha} from "../lib/saida.js";

function gerarRelatorioMarkdown(resultados) {
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
    const recomendacoes = new Set();
    for (const pagina of resultados) {
        for (const violacao of pagina.violations) {
            recomendacoes.add(`- **${violacao.id}**: ${violacao.description} [Referencia](${violacao.helpUrl})`);
        }
    }
    linhas.push(...recomendacoes, "");
    return `${linhas.join("\n")}\n`;
}

async function processarResultadosAcessibilidade({entrada, saida}) {
    const conteudo = await fs.readFile(entrada, "utf8");
    const resultados = JSON.parse(conteudo);
    await fs.mkdir(path.dirname(saida), {recursive: true});
    await fs.writeFile(saida, gerarRelatorioMarkdown(resultados), "utf8");
    return {entrada, saida, paginas: resultados.length};
}

async function principal(argumentos = process.argv.slice(2)) {
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "frontend acessibilidade processar",
            scriptDireto: "frontend/acessibilidade-processar-resultados.js",
            descricao: "Consolida os resultados JSON do Axe em um relatorio Markdown.",
            opcoes: [
                "--entrada <arquivo> Arquivo JSON produzido pelo crawler.",
                "--saida <arquivo>   Relatorio Markdown de acessibilidade.",
                "--base <diretorio>   Resolve os caminhos padrao a partir de outra base."
            ]
        });
        return;
    }

    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ));
    const entrada = path.resolve(diretorioBase, lerOpcao(argumentos, "--entrada", "a11y-scan-results.json"));
    const saidaPadrao = path.join(
        resolverCaminhoConfigurado("artefatosQualidade", diretorioBase),
        "acessibilidade",
        "relatorio.md"
    );
    const saida = path.resolve(diretorioBase, lerOpcao(argumentos, "--saida", saidaPadrao));
    const resultado = await processarResultadosAcessibilidade({entrada, saida});
    escreverLinha(`Relatorio de acessibilidade gerado em: ${resultado.saida}`);
    return resultado;
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        process.stderr.write(`Erro ao processar acessibilidade: ${mensagem}\n`);
        process.exitCode = 1;
    });
}

export {
    gerarRelatorioMarkdown,
    processarResultadosAcessibilidade,
    principal,
};
