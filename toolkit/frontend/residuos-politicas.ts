import fs from "node:fs/promises";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {tentarResolverCaminhoConfigurado} from "../lib/configuracao.js";

const VERSAO_SCHEMA_POLITICAS = "1.0.0" as const;

interface LimitesCamada {
    meta: number;
    limite: number;
}

interface MetricasOrcamento {
    maximosProducao?: Record<string, number | Record<string, number>>;
}

interface OrcamentoResiduos {
    versaoSchema: typeof VERSAO_SCHEMA_POLITICAS;
    camadas: Record<string, LimitesCamada>;
    metricas: MetricasOrcamento;
}

interface ExcecaoResiduo {
    arquivo: string;
    maxLinhas: number;
    [chave: string]: unknown;
}

interface ResultadoExcecoesResiduos {
    versaoSchema: typeof VERSAO_SCHEMA_POLITICAS;
    excecoes: ExcecaoResiduo[];
}

const ORCAMENTO_RESIDUOS_PADRAO: OrcamentoResiduos = {
    versaoSchema: VERSAO_SCHEMA_POLITICAS,
    camadas: {},
    metricas: {
        maximosProducao: {},
    },
};

const EXCECOES_RESIDUOS_PADRAO: ResultadoExcecoesResiduos = {
    versaoSchema: VERSAO_SCHEMA_POLITICAS,
    excecoes: [],
};

function resolverCaminhoOrcamentoResiduos(base: string = DIRETORIO_RAIZ): string | undefined {
    return tentarResolverCaminhoConfigurado("orcamentoResiduosFrontend", base);
}

function resolverCaminhoExcecoesResiduos(base: string = DIRETORIO_RAIZ): string | undefined {
    return tentarResolverCaminhoConfigurado("excecoesResiduosFrontend", base);
}

async function lerJsonConfigurado<T>(caminhoArquivo: string | undefined, fallback: T): Promise<T> {
    if (!caminhoArquivo) {
        return fallback;
    }

    try {
        const valor: unknown = JSON.parse(await fs.readFile(caminhoArquivo, "utf8"));
        return valor as T;
    } catch (erro: unknown) {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        throw new Error(`Nao foi possivel ler a politica de residuos ${caminhoArquivo}: ${mensagem}`, {cause: erro});
    }
}

function ehRegistro(valor: unknown): valor is Record<string, unknown> {
    return typeof valor === "object" && valor !== null;
}

function ehNumeroNaoNegativo(valor: unknown): valor is number {
    return typeof valor === "number" && Number.isFinite(valor) && valor >= 0;
}

function ehLimitesCamada(valor: unknown): valor is LimitesCamada {
    if (!ehRegistro(valor)) {
        return false;
    }

    return ehNumeroNaoNegativo(valor.meta)
        && ehNumeroNaoNegativo(valor.limite)
        && valor.meta <= valor.limite;
}

function validarOrcamento(valor: unknown, caminhoArquivo?: string): asserts valor is OrcamentoResiduos {
    const origem = caminhoArquivo ?? "padrao-do-toolkit";
    if (!ehRegistro(valor) || valor.versaoSchema !== VERSAO_SCHEMA_POLITICAS) {
        throw new Error(`Politica de residuos invalida em ${origem}: esperado versaoSchema ${VERSAO_SCHEMA_POLITICAS}.`);
    }

    if (!ehRegistro(valor.camadas) || !ehRegistro(valor.metricas)) {
        throw new Error(`Politica de residuos invalida em ${origem}: camadas e metricas sao obrigatorios.`);
    }

    for (const [camada, limites] of Object.entries(valor.camadas)) {
        if (!ehLimitesCamada(limites)) {
            throw new Error(`Politica de residuos invalida em ${origem}: limites invalidos para a camada ${camada}.`);
        }
    }

    const maximosProducao = valor.metricas.maximosProducao;
    if (maximosProducao === undefined) {
        return;
    }

    if (!ehRegistro(maximosProducao)) {
        throw new Error(`Politica de residuos invalida em ${origem}: metricas.maximosProducao deve ser um objeto.`);
    }

    for (const [metrica, maximo] of Object.entries(maximosProducao)) {
        if (metrica === "arquivosAcimaMetaPorCamada") {
            if (!ehRegistro(maximo) || Object.values(maximo).some((valorCamada) => !ehNumeroNaoNegativo(valorCamada))) {
                throw new Error(`Politica de residuos invalida em ${origem}: limites por camada invalidos.`);
            }
            continue;
        }

        if (!ehNumeroNaoNegativo(maximo)) {
            throw new Error(`Politica de residuos invalida em ${origem}: maximo invalido para ${metrica}.`);
        }
    }
}

async function carregarOrcamento(caminhoOrcamento?: string): Promise<OrcamentoResiduos> {
    const conteudo: unknown = await lerJsonConfigurado(caminhoOrcamento, ORCAMENTO_RESIDUOS_PADRAO);
    validarOrcamento(conteudo, caminhoOrcamento);
    return conteudo;
}

function ehExcecaoResiduo(valor: unknown): valor is ExcecaoResiduo {
    if (!ehRegistro(valor)) {
        return false;
    }

    return typeof valor.arquivo === "string"
        && valor.arquivo.length > 0
        && ehNumeroNaoNegativo(valor.maxLinhas);
}

async function carregarExcecoes(caminhoExcecoes?: string): Promise<ResultadoExcecoesResiduos> {
    const conteudo: unknown = await lerJsonConfigurado(caminhoExcecoes, EXCECOES_RESIDUOS_PADRAO);
    const origem = caminhoExcecoes ?? "padrao-do-toolkit";
    if (!ehRegistro(conteudo) || conteudo.versaoSchema !== VERSAO_SCHEMA_POLITICAS || !Array.isArray(conteudo.excecoes)) {
        throw new Error(`Politica de residuos invalida em ${origem}: esperado versaoSchema ${VERSAO_SCHEMA_POLITICAS} e uma lista de excecoes.`);
    }

    const excecoesInvalidas = conteudo.excecoes.filter((excecao) => !ehExcecaoResiduo(excecao));
    if (excecoesInvalidas.length > 0) {
        throw new Error(`Politica de residuos invalida em ${origem}: existem excecoes com arquivo ou maxLinhas invalidos.`);
    }

    const excecoes = conteudo.excecoes.filter(ehExcecaoResiduo);
    return {
        versaoSchema: VERSAO_SCHEMA_POLITICAS,
        excecoes,
    };
}

export {
    carregarExcecoes,
    carregarOrcamento,
    resolverCaminhoExcecoesResiduos,
    resolverCaminhoOrcamentoResiduos,
    type ExcecaoResiduo,
    type LimitesCamada,
    type OrcamentoResiduos,
};
