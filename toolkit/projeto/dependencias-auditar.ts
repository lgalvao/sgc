import {execa} from "execa";
import path from "node:path";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {imprimirCabecalho} from "../lib/saida.js";

interface DefinicaoEscopoAuditoria {
    titulo: string;
    segmento: string;
    comando: string;
    argumentos: string[];
}

interface EscopoAuditoria extends DefinicaoEscopoAuditoria {
    diretorio: string;
}

interface ResultadoEscopoAuditoria {
    escopo: string;
    codigoSaida: number;
}

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
    resultados: ResultadoEscopoAuditoria[];
}

const ESCOPOS_AUDITORIA_SGC: readonly DefinicaoEscopoAuditoria[] = [
    {
        titulo: "Auditar dependencias da raiz",
        segmento: "",
        comando: "npm",
        argumentos: ["run", "deps:audit"]
    },
    {
        titulo: "Auditar dependencias do frontend",
        segmento: "frontend",
        comando: "npm",
        argumentos: ["run", "deps:audit"]
    },
    {
        titulo: "Auditar dependencias do toolkit",
        segmento: "toolkit",
        comando: "npm",
        argumentos: ["run", "deps:audit"]
    }
];

function resolverEscoposAuditoria(
    diretorioBase: string,
    definicoes: readonly DefinicaoEscopoAuditoria[] = ESCOPOS_AUDITORIA_SGC
): EscopoAuditoria[] {
    return definicoes.map(definicao => ({
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
        process.stdout.write(`\n${escopo.titulo}\n`);
        const resultado = await executarComando(escopo.comando, escopo.argumentos, escopo.diretorio);
        resultados.push({escopo: escopo.titulo, codigoSaida: resultado.exitCode ?? 0});
    }

    const falhas = resultados.filter((resultado) => resultado.codigoSaida !== 0);
    if (falhas.length > 0) {
        throw new Error(`${falhas.length} auditoria(s) de dependencias falharam.`);
    }

    return {diretorioBase, resultados};
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
    type ResultadoEscopoAuditoria
};
