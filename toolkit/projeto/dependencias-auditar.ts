import {execa} from "execa";
import path from "node:path";
import {carregarConfiguracao, type EscopoComandoConfigurado} from "../lib/configuracao.js";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {escreverLinha, imprimirCabecalho} from "../lib/saida.js";

type DefinicaoEscopoAuditoria = EscopoComandoConfigurado;

interface EscopoAuditoria extends DefinicaoEscopoAuditoria {
    diretorio: string;
}

interface ResultadoEscopoAuditoria {
    escopo: string;
    codigoSaida: number;
    status: StatusAuditoriaDependencias;
}

type StatusAuditoriaDependencias = "ok" | "achados" | "falha";

interface OpcoesAuditoriaDependencias {
    base?: string;
    escopos?: readonly DefinicaoEscopoAuditoria[];
    executarComando?: ExecutarComando;
}

type ExecutarComando = (
    comando: string,
    argumentos: readonly string[],
    diretorio: string
) => Promise<{exitCode?: number}>;

interface ResultadoAuditoriaDependencias {
    diretorioBase: string;
    statusGeral: StatusAuditoriaDependencias;
    resultados: ResultadoEscopoAuditoria[];
}

const ESCOPOS_AUDITORIA_SGC: readonly DefinicaoEscopoAuditoria[] = [
    {
        titulo: "Auditar uso e declaracao de dependencias com Knip",
        segmento: "",
        comando: "npm",
        argumentos: ["run", "deps:audit"],
        codigoNaoZeroIndicaAchados: true
    },
    {
        titulo: "Verificar dependencias npm desatualizadas",
        segmento: "",
        comando: "npm",
        argumentos: ["outdated", "--workspaces", "--include-workspace-root", "--json"],
        codigoNaoZeroIndicaAchados: true
    },
    {
        titulo: "Verificar vulnerabilidades npm",
        segmento: "",
        comando: "npm",
        argumentos: ["audit", "--workspaces", "--include-workspace-root", "--json"],
        codigoNaoZeroIndicaAchados: true
    },
    {
        titulo: "Verificar atualizacoes de dependencias Gradle",
        segmento: "",
        comando: "./gradlew",
        argumentos: [
            "dependencyUpdates",
            "--no-parallel",
            "-Drevision=release",
            "--console=plain"
        ]
    }
];

function resolverEscoposAuditoria(
    diretorioBase: string,
    definicoes?: readonly DefinicaoEscopoAuditoria[]
): EscopoAuditoria[] {
    const definicoesResolvidas = definicoes
        ?? carregarConfiguracao(diretorioBase).execucoes?.dependencias
        ?? ESCOPOS_AUDITORIA_SGC;
    return definicoesResolvidas.map(definicao => ({
        ...definicao,
        diretorio: path.resolve(diretorioBase, definicao.segmento)
    }));
}

const executarComandoPadrao: ExecutarComando = async (comando, argumentos, diretorio) => execa(comando, argumentos, {
    cwd: diretorio,
    stdio: "inherit",
    shell: process.platform === "win32",
    reject: false
});

async function executarAuditoriaDependencias(
    opcoes: OpcoesAuditoriaDependencias = {}
): Promise<ResultadoAuditoriaDependencias> {
    const diretorioBase = path.resolve(opcoes.base ?? resolverNaRaiz());
    const escopos = resolverEscoposAuditoria(diretorioBase, opcoes.escopos);
    const executarComando = opcoes.executarComando ?? executarComandoPadrao;

    imprimirCabecalho(
        "Auditoria de dependencias",
        "Executa a auditoria configurada nos escopos do projeto."
    );

    const resultados: ResultadoEscopoAuditoria[] = [];
    for (const escopo of escopos) {
        escreverLinha();
        escreverLinha(escopo.titulo);
        const resultado = await executarComando(escopo.comando, escopo.argumentos, escopo.diretorio);
        const codigoSaida = resultado.exitCode;
        const status: StatusAuditoriaDependencias = codigoSaida === undefined
            ? "falha"
            : codigoSaida === 0
                ? "ok"
                : escopo.codigoNaoZeroIndicaAchados
                    ? "achados"
                    : "falha";
        resultados.push({escopo: escopo.titulo, codigoSaida: codigoSaida ?? 1, status});
    }

    const statusGeral: StatusAuditoriaDependencias = resultados.some(resultado => resultado.status === "falha")
        ? "falha"
        : resultados.some(resultado => resultado.status === "achados")
            ? "achados"
            : "ok";
    const achados = resultados.filter(resultado => resultado.status === "achados").length;
    const falhas = resultados.filter(resultado => resultado.status === "falha").length;
    if (achados > 0 || falhas > 0) {
        escreverLinha();
        escreverLinha(`Resumo: ${achados} escopo(s) com achados; ${falhas} escopo(s) com falha de execucao.`);
    }

    return {diretorioBase, statusGeral, resultados};
}

export {
    ESCOPOS_AUDITORIA_SGC,
    executarAuditoriaDependencias,
    resolverEscoposAuditoria,
    type DefinicaoEscopoAuditoria,
    type EscopoAuditoria,
    type ExecutarComando,
    type OpcoesAuditoriaDependencias,
    type ResultadoAuditoriaDependencias,
    type ResultadoEscopoAuditoria,
    type StatusAuditoriaDependencias
};
