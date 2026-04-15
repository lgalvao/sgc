#!/usr/bin/env node
import fs from "node:fs/promises";
import path from "node:path";
import {DIRETORIO_RAIZ, resolverNaRaiz} from "../lib/caminhos.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

const DIRETORIO_SAIDA = resolverNaRaiz("etc", "qualidade", "codigo-cheiros", "latest");
const ARQUIVO_JSON = path.join(DIRETORIO_SAIDA, "ultimo-snapshot.json");
const ARQUIVO_MD = path.join(DIRETORIO_SAIDA, "ultimo-resumo.md");

const EXTENSOES_TEXTO = new Set([".ts", ".vue", ".java"]);

const PADROES = [
    {
        chave: "backend_nullable_dto",
        titulo: "Backend DTOs com @Nullable",
        peso: 5,
        escopo: "backend",
        filtroArquivo: ({caminhoRelativo}) =>
            caminhoRelativo.startsWith("backend/src/main/java/")
            && /(Dto|Request|Response|Command)\.java$/.test(caminhoRelativo),
        regexes: [/@Nullable\b/g]
    },
    {
        chave: "backend_null_checks",
        titulo: "Backend checks explicitos de null",
        peso: 2,
        escopo: "backend",
        filtroArquivo: ({caminhoRelativo}) =>
            caminhoRelativo.startsWith("backend/src/main/java/"),
        regexes: [/(?:===|!==|==|!=)\s*null/g, /null\s*(?:===|!==|==|!=)/g]
    },
    {
        chave: "backend_objects_null",
        titulo: "Backend Objects.isNull/nonNull",
        peso: 2,
        escopo: "backend",
        filtroArquivo: ({caminhoRelativo}) =>
            caminhoRelativo.startsWith("backend/src/main/java/"),
        regexes: [/\bObjects\.(?:isNull|nonNull)\s*\(/g, /\bObjects::(?:isNull|nonNull)\b/g]
    },
    {
        chave: "frontend_any_producao",
        titulo: "Frontend producao com any explicito",
        peso: 4,
        escopo: "frontend",
        filtroArquivo: ({caminhoRelativo}) =>
            caminhoRelativo.startsWith("frontend/src/")
            && !ehArquivoTesteOuStory(caminhoRelativo),
        regexes: [/\bas any\b/g, /:\s*any\b/g, /\bArray<any>\b/g, /\bPromise<any>\b/g, /\bref<any>\b/g, /\bRecord<[^>]+,\s*any>\b/g, /\[key:\s*string\]:\s*any\b/g]
    },
    {
        chave: "frontend_any_testes",
        titulo: "Frontend testes com any explicito",
        peso: 1,
        escopo: "frontend_testes",
        filtroArquivo: ({caminhoRelativo}) =>
            caminhoRelativo.startsWith("frontend/src/")
            && ehArquivoTesteOuStory(caminhoRelativo),
        regexes: [/\bas any\b/g, /:\s*any\b/g, /\bArray<any>\b/g, /\bPromise<any>\b/g, /\bref<any>\b/g]
    },
    {
        chave: "frontend_catch_any",
        titulo: "Frontend catch tipado como any",
        peso: 3,
        escopo: "frontend",
        filtroArquivo: ({caminhoRelativo}) =>
            caminhoRelativo.startsWith("frontend/src/"),
        regexes: [/catch\s*\(\s*[^):]+:\s*any\s*\)/g]
    },
    {
        chave: "frontend_null_checks",
        titulo: "Frontend checks explicitos de null",
        peso: 2,
        escopo: "frontend",
        filtroArquivo: ({caminhoRelativo}) =>
            caminhoRelativo.startsWith("frontend/src/")
            && !ehArquivoTesteOuStory(caminhoRelativo),
        regexes: [/(?:===|!==|==|!=)\s*null/g, /null\s*(?:===|!==|==|!=)/g]
    },
    {
        chave: "frontend_fallback_or",
        titulo: "Frontend fallbacks defensivos com ||",
        peso: 1,
        escopo: "frontend",
        filtroArquivo: ({caminhoRelativo}) =>
            caminhoRelativo.startsWith("frontend/src/")
            && !ehArquivoTesteOuStory(caminhoRelativo),
        regexes: [/\|\|\s*(?:\[]|\{}|["'`]{2}|false|true|0)(?![\w$])/g]
    }
];

function ehArquivoTesteOuStory(caminhoRelativo) {
    return caminhoRelativo.includes("/__tests__/")
        || caminhoRelativo.endsWith(".spec.ts")
        || caminhoRelativo.endsWith(".stories.ts") || caminhoRelativo.includes("/test-utils/");
}

function criarEstruturaContagens() {
    return Object.fromEntries(PADROES.map((padrao) => [padrao.chave, 0]));
}

function normalizarSeparadores(caminhoArquivo) {
    return caminhoArquivo.split(path.sep).join("/");
}

async function listarArquivosTexto(diretorio, acumulado = []) {
    const entradas = await fs.readdir(diretorio, {withFileTypes: true});

    for (const entrada of entradas) {
        const caminhoCompleto = path.join(diretorio, entrada.name);
        const caminhoNormalizado = normalizarSeparadores(caminhoCompleto);

        if (caminhoNormalizado.includes("/node_modules/")
            || caminhoNormalizado.includes("/dist/")
            || caminhoNormalizado.includes("/build/")
            || caminhoNormalizado.includes("/coverage/")
            || caminhoNormalizado.includes("/playwright-report/")
            || caminhoNormalizado.includes("/test-results/")) {
            continue;
        }

        if (entrada.isDirectory()) {
            await listarArquivosTexto(caminhoCompleto, acumulado);
            continue;
        }

        if (EXTENSOES_TEXTO.has(path.extname(entrada.name))) {
            acumulado.push(caminhoCompleto);
        }
    }

    return acumulado;
}

function contarOcorrencias(conteudo, regex) {
    const correspondencias = conteudo.match(regex);
    return correspondencias ? correspondencias.length : 0;
}

function somarCategoriaPorArquivo(resumoArquivo, categoria, quantidade, peso) {
    if (!resumoArquivo.categorias[categoria]) {
        resumoArquivo.categorias[categoria] = 0;
    }

    resumoArquivo.categorias[categoria] += quantidade;
    resumoArquivo.pontuacao += quantidade * peso;
}

function classificarPontuacao(pontuacao) {
    if (pontuacao <= 120) {
        return "bom";
    }

    if (pontuacao <= 260) {
        return "atencao";
    }

    return "critico";
}

async function lerSnapshotAnterior() {
    try {
        return JSON.parse(await fs.readFile(ARQUIVO_JSON, "utf8"));
    } catch {
        return null;
    }
}

function calcularDelta(atual, anterior) {
    const deltas = {};

    for (const padrao of PADROES) {
        const valorAtual = atual[padrao.chave] ?? 0;
        const valorAnterior = anterior?.contagens?.[padrao.chave] ?? 0;
        deltas[padrao.chave] = valorAtual - valorAnterior;
    }

    return deltas;
}

function formatarDelta(valor) {
    if (valor === 0) {
        return "0";
    }

    return valor > 0 ? `+${valor}` : `${valor}`;
}

function gerarMarkdown(snapshot) {
    const linhas = [];
    linhas.push("# Auditoria de cheiros de codigo");
    linhas.push("");
    linhas.push(`Gerado em: ${snapshot.geradoEm}`);
    linhas.push(`Pontuacao: ${snapshot.pontuacao.total} (${snapshot.pontuacao.faixa})`);
    linhas.push("");
    linhas.push("## Contagens");
    linhas.push("");
    linhas.push("| Sinal | Total | Delta | Peso |");
    linhas.push("|---|---:|---:|---:|");

    for (const padrao of PADROES) {
        linhas.push(`| ${padrao.titulo} | ${snapshot.contagens[padrao.chave]} | ${formatarDelta(snapshot.deltas[padrao.chave])} | ${padrao.peso} |`);
    }

    linhas.push("");
    linhas.push("## Hotspots");
    linhas.push("");
    linhas.push("| Arquivo | Pontos | Sinais |");
    linhas.push("|---|---:|---|");

    for (const hotspot of snapshot.hotspots) {
        const sinais = Object.entries(hotspot.categorias)
            .sort((a, b) => b[1] - a[1])
            .map(([categoria, valor]) => `${categoria}: ${valor}`)
            .join(", ");
        linhas.push(`| ${hotspot.arquivo} | ${hotspot.pontuacao} | ${sinais} |`);
    }

    linhas.push("");
    linhas.push("## Escopos");
    linhas.push("");
    for (const [escopo, valor] of Object.entries(snapshot.pontuacao.porEscopo)) {
        linhas.push(`- ${escopo}: ${valor} ponto(s)`);
    }

    return `${linhas.join("\n")}\n`;
}

async function executarAuditoria({base = DIRETORIO_RAIZ, json = false, semGravar = false} = {}) {
    const baseResolvida = path.resolve(base);
    const arquivos = await listarArquivosTexto(baseResolvida);
    const contagens = criarEstruturaContagens();
    const pontuacaoPorEscopo = {
        backend: 0,
        frontend: 0,
        frontend_testes: 0
    };
    const arquivosPontuados = [];

    for (const arquivo of arquivos) {
        const conteudo = await fs.readFile(arquivo, "utf8");
        const caminhoRelativo = normalizarSeparadores(path.relative(baseResolvida, arquivo));
        const resumoArquivo = {
            arquivo: caminhoRelativo,
            pontuacao: 0,
            categorias: {}
        };

        for (const padrao of PADROES) {
            if (!padrao.filtroArquivo({caminhoRelativo, conteudo})) {
                continue;
            }

            const total = padrao.regexes.reduce((acumulado, regex) => acumulado + contarOcorrencias(conteudo, regex), 0);
            if (total === 0) {
                continue;
            }

            contagens[padrao.chave] += total;
            pontuacaoPorEscopo[padrao.escopo] += total * padrao.peso;
            somarCategoriaPorArquivo(resumoArquivo, padrao.chave, total, padrao.peso);
        }

        if (resumoArquivo.pontuacao > 0) {
            arquivosPontuados.push(resumoArquivo);
        }
    }

    const anterior = semGravar ? null : await lerSnapshotAnterior();
    const pontuacaoTotal = PADROES.reduce((soma, padrao) => soma + (contagens[padrao.chave] * padrao.peso), 0);

    const snapshot = {
        geradoEm: new Date().toISOString(),
        base: baseResolvida,
        pontuacao: {
            total: pontuacaoTotal,
            faixa: classificarPontuacao(pontuacaoTotal),
            porEscopo: pontuacaoPorEscopo
        },
        contagens,
        deltas: calcularDelta(contagens, anterior),
        hotspots: arquivosPontuados
            .sort((a, b) => b.pontuacao - a.pontuacao || a.arquivo.localeCompare(b.arquivo))
            .slice(0, 15)
    };

    if (!semGravar) {
        await fs.mkdir(DIRETORIO_SAIDA, {recursive: true});
        await fs.writeFile(ARQUIVO_JSON, JSON.stringify(snapshot, null, 2));
        await fs.writeFile(ARQUIVO_MD, gerarMarkdown(snapshot));
    }

    if (json) {
        imprimirJson(snapshot);
        return;
    }

    imprimirCabecalho("Auditoria de cheiros de codigo", `Base: ${snapshot.base}`);
    escreverLinha(`Pontuacao total: ${snapshot.pontuacao.total} (${snapshot.pontuacao.faixa})`);
    escreverLinha("");
    for (const padrao of PADROES) {
        escreverLinha(`- ${padrao.titulo}: ${snapshot.contagens[padrao.chave]} (delta ${formatarDelta(snapshot.deltas[padrao.chave])})`);
    }

    escreverLinha("");
    escreverLinha("Hotspots:");
    for (const hotspot of snapshot.hotspots.slice(0, 10)) {
        escreverLinha(`- ${hotspot.arquivo}: ${hotspot.pontuacao} ponto(s)`);
    }

    if (!semGravar) {
        escreverLinha("");
        escreverLinha(`Snapshot salvo em ${ARQUIVO_JSON}`);
        escreverLinha(`Resumo salvo em ${ARQUIVO_MD}`);
    }
}

function parseArgs(argv) {
    const opcoes = {
        base: DIRETORIO_RAIZ,
        json: false,
        semGravar: false
    };

    for (let indice = 0; indice < argv.length; indice += 1) {
        const arg = argv[indice];

        if (arg === "--json") {
            opcoes.json = true;
            continue;
        }

        if (arg === "--sem-gravar") {
            opcoes.semGravar = true;
            continue;
        }

        if (arg === "--base") {
            opcoes.base = argv[indice + 1] ?? DIRETORIO_RAIZ;
            indice += 1;
        }
    }

    return opcoes;
}

if (process.argv.includes("--help") || process.argv.includes("-h")) {
    escreverLinha("Uso: node etc/scripts/sgc.js codigo smells auditar [--json] [--sem-gravar] [--base <diretorio>]");
    escreverLinha("");
    escreverLinha("Gera um snapshot com contagens e pontuacao de cheiros de codigo.");
    process.exit(0);
}

try {
    await executarAuditoria(parseArgs(process.argv.slice(2)));
} catch (error) {
    escreverLinha(`Erro ao auditar cheiros de codigo: ${error instanceof Error ? error.message : String(error)}`);
    process.exit(1);
}
