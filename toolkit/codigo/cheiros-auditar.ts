#!/usr/bin/env node

import fs from "node:fs/promises";
import path from "node:path";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

type EscopoCheiro = "backend" | "frontend" | "frontend_testes";
type FaixaPontuacao = "bom" | "atencao" | "critico";
type ChavePadrao =
    | "backend_nullable_dto"
    | "backend_null_checks"
    | "backend_objects_null"
    | "frontend_any_producao"
    | "frontend_any_testes"
    | "frontend_catch_any"
    | "frontend_null_checks"
    | "frontend_fallback_or";

interface ContextoFiltroArquivo {
    caminhoRelativo: string;
    conteudo: string;
    diretorioBackendCodigo: string;
    diretorioFrontendCodigo: string;
}

interface PadraoCheiro {
    chave: ChavePadrao;
    titulo: string;
    peso: number;
    escopo: EscopoCheiro;
    filtroArquivo: (contexto: ContextoFiltroArquivo) => boolean;
    regexes: RegExp[];
}

interface ResumoArquivoCheiros {
    arquivo: string;
    pontuacao: number;
    categorias: Partial<Record<ChavePadrao, number>>;
}

type ContagensCheiros = Record<ChavePadrao, number>;
type DeltasCheiros = Record<ChavePadrao, number>;
type PontuacaoPorEscopo = Record<EscopoCheiro, number>;

interface SnapshotCheiros {
    geradoEm: string;
    base: string;
    pontuacao: {
        total: number;
        faixa: FaixaPontuacao;
        porEscopo: PontuacaoPorEscopo;
    };
    contagens: ContagensCheiros;
    deltas: DeltasCheiros;
    hotspots: ResumoArquivoCheiros[];
}

interface ResultadoAuditoriaCheiros {
    snapshot: SnapshotCheiros;
    caminhoFotografia: string;
    caminhoResumo: string;
}

interface OpcoesAuditoriaCheiros {
    base?: string;
    semGravar?: boolean;
    diretorioSaida?: string;
}

const EXTENSOES_TEXTO = new Set([".ts", ".vue", ".java"]);

const PADROES: PadraoCheiro[] = [
    {
        chave: "backend_nullable_dto",
        titulo: "Backend DTOs com @Nullable",
        peso: 5,
        escopo: "backend",
        filtroArquivo: ({caminhoRelativo, diretorioBackendCodigo}) =>
            ehArquivoDentroDiretorio(caminhoRelativo, diretorioBackendCodigo)
            && /(Dto|Request|Response|Command)\.java$/.test(caminhoRelativo),
        regexes: [/@Nullable\b/g]
    },
    {
        chave: "backend_null_checks",
        titulo: "Backend checks explicitos de null",
        peso: 2,
        escopo: "backend",
        filtroArquivo: ({caminhoRelativo, diretorioBackendCodigo}) =>
            ehArquivoDentroDiretorio(caminhoRelativo, diretorioBackendCodigo),
        regexes: [/(?:===|!==|==|!=)\s*null/g, /null\s*(?:===|!==|==|!=)/g]
    },
    {
        chave: "backend_objects_null",
        titulo: "Backend Objects.isNull/nonNull",
        peso: 2,
        escopo: "backend",
        filtroArquivo: ({caminhoRelativo, diretorioBackendCodigo}) =>
            ehArquivoDentroDiretorio(caminhoRelativo, diretorioBackendCodigo),
        regexes: [/\bObjects\.(?:isNull|nonNull)\s*\(/g, /\bObjects::(?:isNull|nonNull)\b/g]
    },
    {
        chave: "frontend_any_producao",
        titulo: "Frontend producao com any explicito",
        peso: 4,
        escopo: "frontend",
        filtroArquivo: ({caminhoRelativo, diretorioFrontendCodigo}) =>
            ehArquivoDentroDiretorio(caminhoRelativo, diretorioFrontendCodigo)
            && !ehArquivoTesteOuStory(caminhoRelativo),
        regexes: [
            /\bas any\b/g,
            /:\s*any\b/g,
            /\bArray<any>\b/g,
            /\bPromise<any>\b/g,
            /\bref<any>\b/g,
            /\bRecord<[^>]+,\s*any>\b/g,
            /\[key:\s*string\]:\s*any\b/g
        ]
    },
    {
        chave: "frontend_any_testes",
        titulo: "Frontend testes com any explicito",
        peso: 1,
        escopo: "frontend_testes",
        filtroArquivo: ({caminhoRelativo, diretorioFrontendCodigo}) =>
            ehArquivoDentroDiretorio(caminhoRelativo, diretorioFrontendCodigo)
            && ehArquivoTesteOuStory(caminhoRelativo),
        regexes: [
            /\bas any\b/g,
            /:\s*any\b/g,
            /\bArray<any>\b/g,
            /\bPromise<any>\b/g,
            /\bref<any>\b/g
        ]
    },
    {
        chave: "frontend_catch_any",
        titulo: "Frontend catch tipado como any",
        peso: 3,
        escopo: "frontend",
        filtroArquivo: ({caminhoRelativo, diretorioFrontendCodigo}) =>
            ehArquivoDentroDiretorio(caminhoRelativo, diretorioFrontendCodigo),
        regexes: [/catch\s*\(\s*[^):]+:\s*any\s*\)/g]
    },
    {
        chave: "frontend_null_checks",
        titulo: "Frontend checks explicitos de null",
        peso: 2,
        escopo: "frontend",
        filtroArquivo: ({caminhoRelativo, diretorioFrontendCodigo}) =>
            ehArquivoDentroDiretorio(caminhoRelativo, diretorioFrontendCodigo)
            && !ehArquivoTesteOuStory(caminhoRelativo),
        regexes: [/(?:===|!==|==|!=)\s*null/g, /null\s*(?:===|!==|==|!=)/g]
    },
    {
        chave: "frontend_fallback_or",
        titulo: "Frontend fallbacks defensivos com ||",
        peso: 1,
        escopo: "frontend",
        filtroArquivo: ({caminhoRelativo, diretorioFrontendCodigo}) =>
            ehArquivoDentroDiretorio(caminhoRelativo, diretorioFrontendCodigo)
            && !ehArquivoTesteOuStory(caminhoRelativo),
        regexes: [/\|\|\s*(?:\[]|\{}|["'`]{2}|false|true|0)(?![\w$])/g]
    }
];

function ehArquivoTesteOuStory(caminhoRelativo: string): boolean {
    return caminhoRelativo.includes("/__tests__/")
        || caminhoRelativo.endsWith(".spec.ts")
        || caminhoRelativo.endsWith(".stories.ts")
        || caminhoRelativo.includes("/test-utils/");
}

function ehArquivoDentroDiretorio(caminhoRelativo: string, diretorio: string): boolean {
    const diretorioNormalizado = diretorio.replace(/\/$/, "");
    return caminhoRelativo === diretorioNormalizado
        || caminhoRelativo.startsWith(`${diretorioNormalizado}/`);
}

function criarEstruturaContagens(): ContagensCheiros {
    return Object.fromEntries(PADROES.map(padrao => [padrao.chave, 0])) as ContagensCheiros;
}

function normalizarSeparadores(caminhoArquivo: string): string {
    return caminhoArquivo.split(path.sep).join("/");
}

async function listarArquivosTexto(diretorio: string, acumulado: string[] = []): Promise<string[]> {
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

function contarOcorrencias(conteudo: string, regex: RegExp): number {
    const correspondencias = conteudo.match(regex);
    return correspondencias ? correspondencias.length : 0;
}

function somarCategoriaPorArquivo(
    resumoArquivo: ResumoArquivoCheiros,
    categoria: ChavePadrao,
    quantidade: number,
    peso: number
): void {
    resumoArquivo.categorias[categoria] = (resumoArquivo.categorias[categoria] ?? 0) + quantidade;
    resumoArquivo.pontuacao += quantidade * peso;
}

function classificarPontuacao(pontuacao: number): FaixaPontuacao {
    if (pontuacao <= 120) {
        return "bom";
    }
    if (pontuacao <= 260) {
        return "atencao";
    }
    return "critico";
}

interface FotografiaAnterior {
    contagens?: Partial<ContagensCheiros>;
}

function ehFotografiaAnterior(valor: unknown): valor is FotografiaAnterior {
    return typeof valor === "object" && valor !== null;
}

async function lerFotografiaAnterior(caminhoFotografia: string): Promise<FotografiaAnterior | null> {
    try {
        const valor: unknown = JSON.parse(await fs.readFile(caminhoFotografia, "utf8"));
        return ehFotografiaAnterior(valor) ? valor : null;
    } catch {
        return null;
    }
}

function calcularDelta(atual: ContagensCheiros, anterior: FotografiaAnterior | null): DeltasCheiros {
    const deltas = criarEstruturaContagens();
    for (const padrao of PADROES) {
        const valorAtual = atual[padrao.chave] ?? 0;
        const valorAnterior = anterior?.contagens?.[padrao.chave] ?? 0;
        deltas[padrao.chave] = valorAtual - valorAnterior;
    }
    return deltas;
}

function formatarDelta(valor: number): string {
    if (valor === 0) {
        return "0";
    }
    return valor > 0 ? `+${valor}` : `${valor}`;
}

function gerarMarkdown(snapshot: SnapshotCheiros): string {
    const linhas: string[] = [];
    linhas.push("# Auditoria de cheiros de codigo", "", `Gerado em: ${snapshot.geradoEm}`, `Pontuacao: ${snapshot.pontuacao.total} (${snapshot.pontuacao.faixa})`, "");
    linhas.push("## Contagens", "", "| Sinal | Total | Delta | Peso |", "|---|---:|---:|---:|");
    for (const padrao of PADROES) {
        linhas.push(`| ${padrao.titulo} | ${snapshot.contagens[padrao.chave]} | ${formatarDelta(snapshot.deltas[padrao.chave])} | ${padrao.peso} |`);
    }

    linhas.push("", "## Hotspots", "", "| Arquivo | Pontos | Sinais |", "|---|---:|---|");
    for (const hotspot of snapshot.hotspots) {
        const sinais = Object.entries(hotspot.categorias)
            .toSorted((a, b) => b[1] - a[1])
            .map(([categoria, valor]) => `${categoria}: ${valor}`)
            .join(", ");
        linhas.push(`| ${hotspot.arquivo} | ${hotspot.pontuacao} | ${sinais} |`);
    }

    linhas.push("", "## Escopos", "");
    for (const [escopo, valor] of Object.entries(snapshot.pontuacao.porEscopo)) {
        linhas.push(`- ${escopo}: ${valor} ponto(s)`);
    }
    return `${linhas.join("\n")}\n`;
}

async function executarAuditoria({
    base = DIRETORIO_RAIZ,
    semGravar = false,
    diretorioSaida
}: OpcoesAuditoriaCheiros = {}): Promise<ResultadoAuditoriaCheiros> {
    const baseResolvida = path.resolve(base);
    const saidaResolvida = path.resolve(
        diretorioSaida ?? path.join(resolverCaminhoConfigurado("artefatosQualidade", baseResolvida), "codigo-cheiros", "mais-recente")
    );
    const caminhoFotografia = path.join(saidaResolvida, "fotografia.json");
    const caminhoResumo = path.join(saidaResolvida, "resumo.md");
    const arquivos = await listarArquivosTexto(baseResolvida);
    const diretorioBackendCodigo = normalizarSeparadores(path.relative(
        baseResolvida,
        resolverCaminhoConfigurado("backendCodigo", baseResolvida)
    ));
    const diretorioFrontendCodigo = normalizarSeparadores(path.relative(
        baseResolvida,
        resolverCaminhoConfigurado("frontendCodigo", baseResolvida)
    ));
    const contagens = criarEstruturaContagens();
    const pontuacaoPorEscopo: PontuacaoPorEscopo = {backend: 0, frontend: 0, frontend_testes: 0};
    const arquivosPontuados: ResumoArquivoCheiros[] = [];

    for (const arquivo of arquivos) {
        const conteudo = await fs.readFile(arquivo, "utf8");
        const caminhoRelativo = normalizarSeparadores(path.relative(baseResolvida, arquivo));
        const resumoArquivo: ResumoArquivoCheiros = {arquivo: caminhoRelativo, pontuacao: 0, categorias: {}};

        for (const padrao of PADROES) {
            if (!padrao.filtroArquivo({caminhoRelativo, conteudo, diretorioBackendCodigo, diretorioFrontendCodigo})) {
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

    const anterior = semGravar ? null : await lerFotografiaAnterior(caminhoFotografia);
    const pontuacaoTotal = PADROES.reduce((soma, padrao) => soma + (contagens[padrao.chave] * padrao.peso), 0);
    const snapshot: SnapshotCheiros = {
        geradoEm: new Date().toISOString(),
        base: baseResolvida,
        pontuacao: {total: pontuacaoTotal, faixa: classificarPontuacao(pontuacaoTotal), porEscopo: pontuacaoPorEscopo},
        contagens,
        deltas: calcularDelta(contagens, anterior),
        hotspots: arquivosPontuados
            .toSorted((a, b) => b.pontuacao - a.pontuacao || a.arquivo.localeCompare(b.arquivo))
            .slice(0, 15)
    };

    if (!semGravar) {
        await fs.mkdir(saidaResolvida, {recursive: true});
        await fs.writeFile(caminhoFotografia, JSON.stringify(snapshot, null, 2));
        await fs.writeFile(caminhoResumo, gerarMarkdown(snapshot));
    }

    return {snapshot, caminhoFotografia, caminhoResumo};
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const emitirJson = argumentos.includes("--json");
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "codigo cheiros auditar",
            scriptDireto: "codigo/cheiros-auditar.ts",
            descricao: "Gera uma fotografia com contagens e pontuacao de cheiros de codigo.",
            opcoes: [
                "--json               Emite a fotografia em JSON.",
                "--sem-gravar         Nao atualiza os artefatos.",
                "--base <diretorio>   Sobrescreve a base da auditoria."
            ]
        });
        return;
    }

    const base = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const execucao = await executarAuditoria({base, semGravar: argumentos.includes("--sem-gravar")});
    const {snapshot} = execucao;
    if (emitirJson) {
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
    if (!argumentos.includes("--sem-gravar")) {
        escreverLinha("");
        escreverLinha(`Fotografia salva em ${execucao.caminhoFotografia}`);
        escreverLinha(`Resumo salvo em ${execucao.caminhoResumo}`);
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro: unknown) => {
        escreverLinha(`Erro ao auditar cheiros de codigo: ${erro instanceof Error ? erro.message : String(erro)}`);
        process.exitCode = 1;
    });
}

export {
    executarAuditoria,
    principal
};
