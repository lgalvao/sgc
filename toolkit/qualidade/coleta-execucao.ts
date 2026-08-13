import path from "node:path";
import process from "node:process";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {escreverErro, escreverLinha} from "../lib/saida.js";
import {criarAdaptadoresSgc, PERFIS_SGC} from "./coleta-adaptadores-sgc.js";
import {criarContextoColeta, type ContextoColeta} from "./coleta-contexto.js";
import {executarComando, executarComandoSgc} from "./coleta-executor.js";
import {
    VERSAO_SCHEMA_FOTOGRAFIA,
    criarFotografiaColeta,
    persistirFotografia,
    prepararDiretoriosFotografia,
    type FotografiaColeta,
    type MetadadosControleVersao
} from "./coleta-fotografia.js";
import {consolidarJUnit, parseJsonSeguro} from "./coleta-leitores.js";
import type {PontoCriticoQualidade, ResultadoJUnit} from "./coleta-leitores.js";
import {coletarMetadadosGit} from "./coleta-metadados.js";

type CategoriaExecucao = "teste" | "cobertura" | "qualidade";
type StatusExecucao = "nao_executado" | "sucesso" | "falha";
type AdaptadorQualidade = (contexto: ContextoColeta) => Promise<ExecucaoQualidade>;
type CatalogoAdaptadores = Readonly<Record<string, AdaptadorQualidade>>;
type CatalogoPerfisColeta = Readonly<Record<string, readonly string[]>>;

interface OpcoesColeta {
    adaptadores?: CatalogoAdaptadores;
    perfis?: CatalogoPerfisColeta;
    criarContexto?: (base: string) => ContextoColeta;
    coletarMetadados?: (base: string) => Promise<MetadadosControleVersao>;
    prepararDiretoriosFotografia?: typeof prepararDiretoriosFotografia;
    persistirFotografia?: typeof persistirFotografia;
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

const PERFIS = PERFIS_SGC;

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

const ADAPTADORES: CatalogoAdaptadores = criarAdaptadoresSgc({
    criarExecucao,
    executarComando,
    executarComandoSgc,
    consolidarJUnit,
    registrarResultadoExecucao,
    parseJsonSeguro,
    obterOpcoesPlaywright
});

async function principal(
    argumentos: string[] = process.argv.slice(2),
    opcoes: OpcoesColeta = {}
): Promise<FotografiaColeta> {
    const indicePerfil = argumentos.indexOf("--perfil");
    const perfilPorOpcao = indicePerfil >= 0 ? argumentos[indicePerfil + 1] : null;
    const perfilPorAtribuicao = argumentos.find(argumento => argumento.startsWith("--perfil="))?.split("=")[1] ?? null;
    const perfilInformado = perfilPorOpcao || perfilPorAtribuicao || "rapido";
    const base = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const contexto = (opcoes.criarContexto ?? criarContextoColeta)(base);
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

    const prepararDiretorios = opcoes.prepararDiretoriosFotografia ?? prepararDiretoriosFotografia;
    await prepararDiretorios({
        diretorioExecucao,
        diretorioMaisRecente: contexto.diretorioMaisRecente
    });

    const verificacoes: ExecucaoQualidade[] = [];
    for (const nomeAdaptador of adaptadoresDoPerfil) {
        const adaptador = adaptadores[nomeAdaptador];
        if (!adaptador) {
            throw new Error(`Adaptador de qualidade ausente para o perfil ${perfilInformado}: ${nomeAdaptador}`);
        }

        escreverLinha(`Executando ${nomeAdaptador}...`);
        verificacoes.push(await adaptador(contexto));
    }

    const fotografia = criarFotografiaColeta({
        versaoSchema: VERSAO_SCHEMA_FOTOGRAFIA,
        perfilExecucao: perfilInformado,
        inicio,
        verificacoes,
        controleVersao: await (opcoes.coletarMetadados ?? coletarMetadadosGit)(contexto.base).catch(() => ({}))
    });
    const persistir = opcoes.persistirFotografia ?? persistirFotografia;
    const caminhoFotografia = await persistir(fotografia, {
        diretorioExecucao,
        diretorioMaisRecente: contexto.diretorioMaisRecente
    });
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
    type MetadadosControleVersao,
    type PontoCriticoQualidade,
    type OpcoesColeta,
    type OpcoesComando,
    type OpcoesPlaywright,
    type ResultadoComando,
    type ResultadoJUnit
};
