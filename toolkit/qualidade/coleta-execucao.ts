import fs from "node:fs/promises";
import path from "node:path";
import process from "node:process";
import {execa} from "execa";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";
import {NOME_ARQUIVO_FOTOGRAFIA, obterDiretorioArtefatos} from "../lib/qualidade.js";
import {escreverErro, escreverLinha} from "../lib/saida.js";
import {criarAdaptadoresSgc, PERFIS_SGC} from "./coleta-adaptadores-sgc.js";
import {executarComando, executarComandoSgc} from "./coleta-executor.js";

const VERSAO_SCHEMA = "1.0.0" as const;

type CategoriaExecucao = "teste" | "cobertura" | "qualidade";
type StatusExecucao = "nao_executado" | "sucesso" | "falha";
type AdaptadorQualidade = (contexto: ContextoColeta) => Promise<ExecucaoQualidade>;
type CatalogoAdaptadores = Readonly<Record<string, AdaptadorQualidade>>;
type CatalogoPerfisColeta = Readonly<Record<string, readonly string[]>>;

interface OpcoesColeta {
    adaptadores?: CatalogoAdaptadores;
    perfis?: CatalogoPerfisColeta;
}

interface ContextoColeta {
    base: string;
    diretorioArtefatos: string;
    diretorioExecucoes: string;
    diretorioMaisRecente: string;
    diretorioBackend: string;
    diretorioFrontend: string;
    diretorioFrontendCodigo: string;
}

interface ExecucaoQualidade {
    codigo: string;
    nome: string;
    categoria: CategoriaExecucao;
    status: StatusExecucao;
    duracaoMs: number;
    comando: string;
    diretorio: string;
    sumario: string;
    metricas: unknown;
    erros: string[];
    artefatos: string[];
}

interface ResultadoComando {
    codigoSaida: number;
    saida: string;
    erro: string;
    duracaoMs: number;
}

interface OpcoesComando {
    comando: string;
    args: string[];
    cwd: string;
    env?: Record<string, string>;
}

interface OpcoesPlaywright {
    descricao: string;
    argumentos: string[];
}

interface ResultadoJUnit extends Record<string, unknown> {
    testes: number;
    falhas: number;
    ignorados: number;
    tempoSegundos: number;
    sucessos: number;
    arquivosXml: string[];
}

interface ResultadoResiduos {
    status?: string;
    resumo?: {
        scoreTotal?: number;
        faixa?: string;
    };
    violacoes?: unknown[];
    avisos?: unknown[];
    hotspots?: HotspotQualidade[];
}

interface ResultadoArquitetura {
    resumo?: {
        scoreTotal?: number;
        faixa?: string;
        metricas?: Record<string, unknown>;
    };
    hotspots?: unknown[];
}

interface ResultadoPlaywright extends Record<string, unknown> {
    stats?: Record<string, unknown> & {
        expected?: number;
    };
}

interface HotspotQualidade {
    arquivo: string;
    score: number;
}

interface FotografiaColeta {
    versaoSchema: string;
    metadados: {
        geradoEm: string;
        perfilExecucao: string;
        duracaoTotalMs: number;
        git: Record<string, string>;
    };
    verificacoes: ExecucaoQualidade[];
    resumo: {
        statusGeral: "verde" | "vermelho";
        totais: {
            verificacoes: number;
            sucesso: number;
            falha: number;
        };
    };
    hotspots: Array<{
        nome: string;
        risco: number;
        origem: string;
    }>;
}

const PERFIS = PERFIS_SGC;

function criarContextoColeta(base: string = DIRETORIO_RAIZ): ContextoColeta {
    const baseResolvida = path.resolve(base ?? DIRETORIO_RAIZ);
    const diretorioArtefatos = obterDiretorioArtefatos(baseResolvida);

    return {
        base: baseResolvida,
        diretorioArtefatos,
        diretorioExecucoes: path.join(diretorioArtefatos, "execucoes"),
        diretorioMaisRecente: path.join(diretorioArtefatos, "mais-recente"),
        diretorioBackend: resolverCaminhoConfigurado("backend", baseResolvida),
        diretorioFrontend: resolverCaminhoConfigurado("frontend", baseResolvida),
        diretorioFrontendCodigo: resolverCaminhoConfigurado("frontendCodigo", baseResolvida),
    };
}

function caminhoRelativo(caminhoAbsoluto: string, base: string): string {
    return path.relative(base, caminhoAbsoluto).replace(/\\/g, "/");
}

function obterOpcoesPlaywright(diretorioBase: string): OpcoesPlaywright {
    const diretorioTestesIntegracao = resolverCaminhoConfigurado("testesIntegracao", diretorioBase);
    const configuracao = caminhoRelativo(path.join(diretorioTestesIntegracao, "playwright.config.ts"), diretorioBase);
    return {
        descricao: `npx playwright test --config=${configuracao}`,
        argumentos: ["playwright", "test", `--config=${configuracao}`, "--reporter=json"]
    };
}

function formatarTimestampArquivo(data: Date = new Date()): string {
    return data.toISOString().replaceAll(":", "-").replace(/\.\d{3}Z$/, "Z");
}


function criarExecucao(
    codigo: string,
    nome: string,
    categoria: CategoriaExecucao,
    comando: string,
    diretorio: string
): ExecucaoQualidade {
    return {
        codigo,
        nome,
        categoria,
        status: "nao_executado",
        duracaoMs: 0,
        comando,
        diretorio,
        sumario: "",
        metricas: {},
        erros: [],
        artefatos: []
    };
}


function registrarResultadoExecucao(execucao: ExecucaoQualidade, resultado: ResultadoComando): void {
    execucao.duracaoMs = resultado.duracaoMs;
    if (resultado.codigoSaida !== 0) {
        execucao.erros = [resultado.erro || resultado.saida || `Comando terminou com codigo ${resultado.codigoSaida}.`];
    }
}

function parseJsonSeguro<T>(conteudo: string, fallback: T): T {
    try {
        return JSON.parse(conteudo);
    } catch {
        return fallback;
    }
}

async function consolidarJUnit(diretorioRelatorio: string, base: string): Promise<ResultadoJUnit> {
    const entries = await fs.readdir(diretorioRelatorio, {withFileTypes: true}).catch(() => []);
    const arquivos = entries.filter(e => e.isFile() && e.name.endsWith(".xml")).map(e => path.join(diretorioRelatorio, e.name));

    const totais: ResultadoJUnit = {testes: 0, falhas: 0, ignorados: 0, tempoSegundos: 0, sucessos: 0, arquivosXml: []};
    for (const arquivo of arquivos) {
        const conteudo = await fs.readFile(arquivo, "utf-8");
        totais.testes += Number(conteudo.match(/tests="(\d+)"/)?.[1] ?? 0);
        totais.falhas += Number(conteudo.match(/failures="(\d+)"/)?.[1] ?? 0) + Number(conteudo.match(/errors="(\d+)"/)?.[1] ?? 0);
        totais.ignorados += Number(conteudo.match(/skipped="(\d+)"/)?.[1] ?? 0);
        totais.tempoSegundos += Number(conteudo.match(/time="([0-9.]+)"/)?.[1] ?? 0);
    }
    totais.sucessos = Math.max(totais.testes - totais.falhas - totais.ignorados, 0);
    totais.arquivosXml = arquivos.map((arquivo) => caminhoRelativo(arquivo, base));
    return totais;
}

function ehHotspotQualidade(valor: unknown): valor is HotspotQualidade {
    if (!valor || typeof valor !== "object") {
        return false;
    }
    const hotspot = valor as Record<string, unknown>;
    return typeof hotspot.arquivo === "string" && typeof hotspot.score === "number";
}

function extrairHotspotsQualidade(metricas: unknown): HotspotQualidade[] {
    if (!metricas || typeof metricas !== "object") {
        return [];
    }
    const hotspots = (metricas as Record<string, unknown>).hotspots;
    return Array.isArray(hotspots) ? hotspots.filter(ehHotspotQualidade) : [];
}

const ADAPTADORES: CatalogoAdaptadores = criarAdaptadoresSgc({
    criarExecucao,
    executarComando,
    executarComandoSgc,
    consolidarJUnit,
    registrarResultadoExecucao,
    parseJsonSeguro,
    obterOpcoesPlaywright
});

async function coletarGit(base: string): Promise<Record<string, string>> {
    const branch = (await execa("git", ["rev-parse", "--abbrev-ref", "HEAD"], {cwd: base})).stdout.trim();
    const commit = (await execa("git", ["rev-parse", "HEAD"], {cwd: base})).stdout.trim();
    return {branch, commit};
}

async function principal(
    argumentos: string[] = process.argv.slice(2),
    opcoes: OpcoesColeta = {}
): Promise<FotografiaColeta> {
    const indicePerfil = argumentos.indexOf("--perfil");
    const perfilPorOpcao = indicePerfil >= 0 ? argumentos[indicePerfil + 1] : null;
    const perfilPorAtribuicao = argumentos.find(argumento => argumento.startsWith("--perfil="))?.split("=")[1] ?? null;
    const perfilInformado = perfilPorOpcao || perfilPorAtribuicao || "rapido";
    const base = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const contexto = criarContextoColeta(base);
    const inicio = Date.now();
    const timestamp = formatarTimestampArquivo();
    const perfis: CatalogoPerfisColeta = opcoes.perfis ?? PERFIS;
    const adaptadores = opcoes.adaptadores ?? ADAPTADORES;
    const adaptadoresDoPerfil = perfis[perfilInformado];
    if (!adaptadoresDoPerfil) {
        throw new Error(`Perfil de qualidade invalido: ${perfilInformado}`);
    }
    const adaptadoresAusentes = adaptadoresDoPerfil.filter(nomeAdaptador => !adaptadores[nomeAdaptador]);
    if (adaptadoresAusentes.length > 0) {
        throw new Error(`Adaptador(es) de qualidade ausente(s) para o perfil ${perfilInformado}: ${adaptadoresAusentes.join(", ")}`);
    }

    const diretorioExecucao = path.join(contexto.diretorioExecucoes, timestamp);

    await fs.mkdir(diretorioExecucao, {recursive: true});
    await fs.mkdir(contexto.diretorioMaisRecente, {recursive: true});

    const verificacoes: ExecucaoQualidade[] = [];
    for (const nomeAdaptador of adaptadoresDoPerfil) {
        const adaptador = adaptadores[nomeAdaptador];
        if (!adaptador) {
            throw new Error(`Adaptador de qualidade ausente para o perfil ${perfilInformado}: ${nomeAdaptador}`);
        }

        escreverLinha(`Executando ${nomeAdaptador}...`);
        verificacoes.push(await adaptador(contexto));
    }

    const hotspotsResiduos = verificacoes
        .flatMap((item) => extrairHotspotsQualidade(item.metricas).map((hotspot) => ({
            nome: hotspot.arquivo,
            risco: hotspot.score,
            origem: item.codigo
        })))
        .toSorted((a, b) => b.risco - a.risco)
        .slice(0, 20);

    const fotografia: FotografiaColeta = {
        versaoSchema: VERSAO_SCHEMA,
        metadados: {
            geradoEm: new Date().toISOString(),
            perfilExecucao: perfilInformado,
            duracaoTotalMs: Date.now() - inicio,
            git: await coletarGit(contexto.base).catch(() => ({}))
        },
        verificacoes,
        resumo: {
            statusGeral: verificacoes.some(v => v.status === "falha") ? "vermelho" : "verde",
            totais: {
                verificacoes: verificacoes.length,
                sucesso: verificacoes.filter(v => v.status === "sucesso").length,
                falha: verificacoes.filter(v => v.status === "falha").length
            }
        },
        hotspots: hotspotsResiduos
    };

    const caminhoFotografia = path.join(diretorioExecucao, NOME_ARQUIVO_FOTOGRAFIA);
    await fs.writeFile(caminhoFotografia, JSON.stringify(fotografia, null, 2));
    await fs.writeFile(path.join(contexto.diretorioMaisRecente, NOME_ARQUIVO_FOTOGRAFIA), JSON.stringify(fotografia, null, 2));
    escreverLinha(`Fotografia gerada em ${caminhoRelativo(caminhoFotografia, contexto.base)}`);

    return fotografia;
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverErro(`Erro ao coletar qualidade: ${mensagem}\n`);
        process.exitCode = 1;
    });
}

export {
    ADAPTADORES,
    PERFIS,
    obterOpcoesPlaywright,
    principal,
    type AdaptadorQualidade,
    type CatalogoAdaptadores,
    type CatalogoPerfisColeta,
    type ContextoColeta,
    type ExecucaoQualidade,
    type FotografiaColeta,
    type OpcoesColeta,
    type OpcoesComando,
    type OpcoesPlaywright,
    type ResultadoArquitetura,
    type ResultadoComando,
    type ResultadoJUnit,
    type ResultadoPlaywright,
    type ResultadoResiduos
};
