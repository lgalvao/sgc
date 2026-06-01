#!/usr/bin/env node

import fs from "node:fs/promises";
import path from "node:path";
import pc from "picocolors";
import {globby} from "globby";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

const DIRETORIO_BACKEND = resolverNaRaiz("backend/src/main/java/sgc");
const CAMINHO_RELATORIO_MD = resolverNaRaiz("etc/qualidade/backend/latest/arquitetura-auditoria.md");
const CAMINHO_RELATORIO_JSON = resolverNaRaiz("etc/qualidade/backend/latest/arquitetura-auditoria.json");

const LIMITE_LINHAS_ALERTA = 400;
const LIMITE_LINHAS_CRITICO = 700;
const LIMITE_METODOS_ALERTA = 15;
const LIMITE_METODOS_CRITICO = 25;
const LIMITE_DEPENDENCIAS_ALERTA = 8;
const LIMITE_DEPENDENCIAS_CRITICO = 12;

const SUFIXOS_ALVO = ["Service.java", "Facade.java", "Controller.java"];

function normalizarCaminho(caminho) {
    return caminho.replaceAll(path.sep, "/");
}

function contarLinhas(conteudo) {
    return conteudo.split(/\r?\n/).filter((linha) => {
        const limpa = linha.trim();
        return limpa.length > 0 && !limpa.startsWith("//") && !limpa.startsWith("*");
    }).length;
}

function contarMetodosPublicos(conteudo) {
    const regex = /^\s+public\s+(?:static\s+)?(?!class|interface|enum|record\b)(?:(?:@\w+\s+)*)(?:[\w<>[\],\s]+)\s+[a-z]\w*\s*\(/gm;
    return (conteudo.match(regex) ?? []).length;
}

function contarDependencias(conteudo) {
    // @RequiredArgsConstructor style: private final Type field;
    const camposFinais = (conteudo.match(/^\s+private\s+final\s+\w[\w.<>[\]]+\s+\w+\s*;/gm) ?? []).length;
    // @Autowired style
    const autowired = (conteudo.match(/@Autowired/g) ?? []).length;
    return camposFinais + autowired;
}

function extrairPacote(conteudo) {
    return conteudo.match(/^package\s+([\w.]+)\s*;/m)?.[1] ?? "";
}

function classificarTipo(nomeArquivo) {
    if (nomeArquivo.endsWith("Controller.java")) return "controller";
    if (nomeArquivo.endsWith("Facade.java")) return "facade";
    if (nomeArquivo.endsWith("Service.java")) return "service";
    return "outro";
}

function calcularSeveridade(linhas, metodos, dependencias) {
    let pontos = 0;
    if (linhas >= LIMITE_LINHAS_CRITICO) pontos += 2;
    else if (linhas >= LIMITE_LINHAS_ALERTA) pontos += 1;
    if (metodos >= LIMITE_METODOS_CRITICO) pontos += 2;
    else if (metodos >= LIMITE_METODOS_ALERTA) pontos += 1;
    if (dependencias >= LIMITE_DEPENDENCIAS_CRITICO) pontos += 2;
    else if (dependencias >= LIMITE_DEPENDENCIAS_ALERTA) pontos += 1;
    if (pontos >= 4) return "critico";
    if (pontos >= 2) return "alerta";
    return "ok";
}

function motivosSeveridade(linhas, metodos, dependencias) {
    const motivos = [];
    if (linhas >= LIMITE_LINHAS_CRITICO) motivos.push(`${linhas} linhas (>=${LIMITE_LINHAS_CRITICO})`);
    else if (linhas >= LIMITE_LINHAS_ALERTA) motivos.push(`${linhas} linhas (>=${LIMITE_LINHAS_ALERTA})`);
    if (metodos >= LIMITE_METODOS_CRITICO) motivos.push(`${metodos} métodos públicos (>=${LIMITE_METODOS_CRITICO})`);
    else if (metodos >= LIMITE_METODOS_ALERTA) motivos.push(`${metodos} métodos públicos (>=${LIMITE_METODOS_ALERTA})`);
    if (dependencias >= LIMITE_DEPENDENCIAS_CRITICO) motivos.push(`${dependencias} dependências (>=${LIMITE_DEPENDENCIAS_CRITICO})`);
    else if (dependencias >= LIMITE_DEPENDENCIAS_ALERTA) motivos.push(`${dependencias} dependências (>=${LIMITE_DEPENDENCIAS_ALERTA})`);
    return motivos;
}

async function auditarArquitetura() {
    const arquivos = await globby(path.join(DIRETORIO_BACKEND, "**/*.java").replace(/\\/g, "/"), {absolute: true});
    const alvos = arquivos.filter((f) => SUFIXOS_ALVO.some((s) => f.endsWith(s)));

    const resultados = await Promise.all(alvos.map(async (arquivo) => {
        const conteudo = await fs.readFile(arquivo, "utf-8");
        const nomeArquivo = path.basename(arquivo);
        const linhas = contarLinhas(conteudo);
        const metodos = contarMetodosPublicos(conteudo);
        const dependencias = contarDependencias(conteudo);
        const severidade = calcularSeveridade(linhas, metodos, dependencias);
        const motivos = motivosSeveridade(linhas, metodos, dependencias);
        const pacote = extrairPacote(conteudo);
        const tipo = classificarTipo(nomeArquivo);
        const caminhoRelativo = normalizarCaminho(path.relative(resolverNaRaiz("."), arquivo));

        return {nomeArquivo, caminhoRelativo, pacote, tipo, linhas, metodos, dependencias, severidade, motivos};
    }));

    resultados.sort((a, b) => {
        const ordem = {critico: 0, alerta: 1, ok: 2};
        if (ordem[a.severidade] !== ordem[b.severidade]) return ordem[a.severidade] - ordem[b.severidade];
        return b.linhas - a.linhas;
    });

    const criticos = resultados.filter((r) => r.severidade === "critico");
    const alertas = resultados.filter((r) => r.severidade === "alerta");

    return {
        geradoEm: new Date().toISOString(),
        limites: {
            linhas: {alerta: LIMITE_LINHAS_ALERTA, critico: LIMITE_LINHAS_CRITICO},
            metodos: {alerta: LIMITE_METODOS_ALERTA, critico: LIMITE_METODOS_CRITICO},
            dependencias: {alerta: LIMITE_DEPENDENCIAS_ALERTA, critico: LIMITE_DEPENDENCIAS_CRITICO}
        },
        resumo: {
            totalAnalisados: resultados.length,
            criticos: criticos.length,
            alertas: alertas.length,
            ok: resultados.filter((r) => r.severidade === "ok").length
        },
        hotspots: resultados.filter((r) => r.severidade !== "ok"),
        todos: resultados
    };
}

function gerarMarkdown(relatorio) {
    const linhas = [];
    linhas.push("# Auditoria de arquitetura do backend", "");
    linhas.push(`Gerado em: ${relatorio.geradoEm}`, "");
    linhas.push("## Resumo", "");
    linhas.push(`- Analisados: ${relatorio.resumo.totalAnalisados}`);
    linhas.push(`- Críticos: ${relatorio.resumo.criticos}`);
    linhas.push(`- Alertas: ${relatorio.resumo.alertas}`);
    linhas.push(`- OK: ${relatorio.resumo.ok}`, "");

    linhas.push("## Limites", "");
    linhas.push(`| Métrica | Alerta | Crítico |`);
    linhas.push(`|---------|--------|---------|`);
    linhas.push(`| Linhas efetivas | ${relatorio.limites.linhas.alerta} | ${relatorio.limites.linhas.critico} |`);
    linhas.push(`| Métodos públicos | ${relatorio.limites.metodos.alerta} | ${relatorio.limites.metodos.critico} |`);
    linhas.push(`| Dependências injetadas | ${relatorio.limites.dependencias.alerta} | ${relatorio.limites.dependencias.critico} |`);
    linhas.push("");

    if (relatorio.hotspots.length === 0) {
        linhas.push("Nenhum hotspot encontrado.");
        return linhas.join("\n");
    }

    linhas.push("## Hotspots", "");
    linhas.push("| Arquivo | Tipo | Linhas | Métodos | Dependências | Severidade | Motivos |");
    linhas.push("|---------|------|--------|---------|--------------|-----------|---------|");

    for (const item of relatorio.hotspots) {
        const sev = item.severidade === "critico" ? "🔴 crítico" : "🟡 alerta";
        linhas.push(`| \`${item.nomeArquivo}\` | ${item.tipo} | ${item.linhas} | ${item.metodos} | ${item.dependencias} | ${sev} | ${item.motivos.join("; ")} |`);
    }

    linhas.push("", "## Por que isso importa", "");
    linhas.push("Classes com muitas linhas, métodos e dependências são hubs de complexidade:");
    linhas.push("- cada alteração exige leitura excessiva;");
    linhas.push("- a chance de duplicar lógica cresce a cada nova funcionalidade;");
    linhas.push("- testes ficam difíceis de isolar;");
    linhas.push("- responsabilidades se sobrepõem.", "");
    linhas.push("## Primeiro corte sugerido", "");
    if (relatorio.hotspots.length > 0) {
        const top = relatorio.hotspots[0];
        linhas.push(`Começar por \`${top.nomeArquivo}\` (${top.motivos.join(", ")}).`);
        linhas.push("Identificar os casos de uso reais e separar por responsabilidade concreta (consulta, mutação, workflow, notificação).");
    }

    return linhas.join("\n");
}

async function gravarRelatorios(relatorio) {
    await fs.mkdir(path.dirname(CAMINHO_RELATORIO_MD), {recursive: true});
    await fs.writeFile(CAMINHO_RELATORIO_MD, gerarMarkdown(relatorio), "utf-8");
    await fs.writeFile(CAMINHO_RELATORIO_JSON, JSON.stringify(relatorio, null, 2), "utf-8");
}

function exibirAjuda() {
    exibirAjudaComando({
        comandoSgc: "backend arquitetura auditar",
        scriptDireto: "etc/scripts/backend/arquitetura-auditar.js",
        descricao: "Audita Services, Facades e Controllers do backend detectando god objects por linhas, métodos e dependências.",
        opcoes: [
            "--json              Emite o relatório em JSON.",
            "--sem-gravar        Não grava os arquivos em disco.",
            "--help, -h          Exibe esta ajuda."
        ],
        exemplos: [
            "node etc/scripts/sgc.js backend arquitetura auditar",
            "node etc/scripts/sgc.js backend arquitetura auditar --json",
            "node etc/scripts/sgc.js backend arquitetura auditar --sem-gravar"
        ]
    });
}

async function main() {
    const args = process.argv.slice(2);

    if (args.includes("--help") || args.includes("-h")) {
        exibirAjuda();
        return;
    }

    const emitirJson = args.includes("--json");
    const semGravar = args.includes("--sem-gravar");

    imprimirCabecalho("AUDITORIA DE ARQUITETURA DO BACKEND");
    escreverLinha(`Base analisada: ${pc.dim(DIRETORIO_BACKEND)}`);

    const relatorio = await auditarArquitetura();

    if (!semGravar) {
        await gravarRelatorios(relatorio);
        escreverLinha(`Relatório Markdown: ${pc.dim(CAMINHO_RELATORIO_MD)}`);
        escreverLinha(`Relatório JSON: ${pc.dim(CAMINHO_RELATORIO_JSON)}`);
    }

    escreverLinha(`Analisados: ${relatorio.resumo.totalAnalisados} — Críticos: ${pc.red(String(relatorio.resumo.criticos))} — Alertas: ${pc.yellow(String(relatorio.resumo.alertas))} — OK: ${pc.green(String(relatorio.resumo.ok))}`);

    if (relatorio.hotspots.length > 0) {
        escreverLinha("");
        escreverLinha(pc.bold("Hotspots:"));
        for (const item of relatorio.hotspots.slice(0, 10)) {
            const cor = item.severidade === "critico" ? pc.red : pc.yellow;
            escreverLinha(`  ${cor("●")} ${item.nomeArquivo} — ${item.motivos.join(", ")}`);
        }
    } else {
        escreverLinha(pc.green("Nenhum hotspot encontrado."));
    }

    if (emitirJson) {
        imprimirJson(relatorio);
    }
}

main().catch((erro) => {
    escreverLinha(pc.red(`Erro ao auditar arquitetura: ${erro instanceof Error ? erro.message : String(erro)}`));
    process.exit(1);
});
