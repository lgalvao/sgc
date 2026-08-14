import {execa} from "execa";
import path from "node:path";
import {carregarConfiguracao, type EscopoComandoConfigurado} from "../biblioteca/configuracao.js";
import {resolverNaRaiz} from "../biblioteca/caminhos.js";
import {escreverLinha, imprimirCabecalho} from "../biblioteca/saida.js";

type DefinicaoEscopoAuditoria = EscopoComandoConfigurado;

interface EscopoAuditoria extends DefinicaoEscopoAuditoria {
    diretorio: string;
}

interface ResultadoExecucaoComando {
    exitCode?: number;
    stdout?: string;
    stderr?: string;
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
    diretorio: string,
    ignorarAtualizacoes?: readonly {pacote: string; major: number}[]
) => Promise<ResultadoExecucaoComando>;

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
        codigoNaoZeroIndicaAchados: true,
        ignorarAtualizacoes: [{pacote: "typescript", major: 7}]
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

function obterMajorVersao(versao: unknown): number | null {
    if (typeof versao !== "string") {
        return null;
    }
    const correspondencia = /^v?(\d+)/.exec(versao.trim());
    return correspondencia ? Number.parseInt(correspondencia[1], 10) : null;
}

function deveIgnorarAtualizacao(
    pacote: string,
    dados: unknown,
    regras: readonly {pacote: string; major: number}[]
): boolean {
    if (!dados || typeof dados !== "object" || Array.isArray(dados)) {
        return false;
    }
    const regra = regras.find(item => item.pacote === pacote);
    return regra !== undefined && obterMajorVersao((dados as {latest?: unknown}).latest) === regra.major;
}

function filtrarSaidaNpmOutdated(
    saida: string,
    regras: readonly {pacote: string; major: number}[],
    codigoSaidaOriginal: number
): {saida: string; codigoSaida: number} {
    if (regras.length === 0) {
        return {saida, codigoSaida: codigoSaidaOriginal};
    }
    try {
        const dados = JSON.parse(saida) as Record<string, unknown>;
        const filtrado = Object.fromEntries(Object.entries(dados).flatMap(([pacote, valor]) => {
            if (Array.isArray(valor)) {
                const itens = valor.filter(item => !deveIgnorarAtualizacao(pacote, item, regras));
                return itens.length > 0 ? [[pacote, itens]] : [];
            }
            return deveIgnorarAtualizacao(pacote, valor, regras) ? [] : [[pacote, valor]];
        }));
        return {
            saida: `${JSON.stringify(filtrado, null, 2)}\n`,
            codigoSaida: Object.keys(filtrado).length === 0 ? 0 : codigoSaidaOriginal
        };
    } catch {
        return {saida, codigoSaida: codigoSaidaOriginal};
    }
}

const executarComandoPadrao: ExecutarComando = async (comando, argumentos, diretorio, ignorarAtualizacoes = []) => {
    const filtrarNpmOutdated = comando === "npm"
        && argumentos.includes("outdated")
        && ignorarAtualizacoes.length > 0;
    const resultado = await execa(comando, argumentos, {
        cwd: diretorio,
        stdout: "pipe",
        stderr: "pipe",
        shell: process.platform === "win32",
        reject: false
    });
    const stderr = resultado.stderr === undefined ? "" : String(resultado.stderr);
    if (stderr) {
        process.stderr.write(`${stderr}\n`);
    }
    const stdout = resultado.stdout === undefined ? "" : String(resultado.stdout);
    if (!filtrarNpmOutdated) {
        if (stdout) {
            process.stdout.write(`${stdout}\n`);
        }
        return {exitCode: resultado.exitCode, stdout, stderr};
    }
    const filtrado = filtrarSaidaNpmOutdated(stdout, ignorarAtualizacoes, resultado.exitCode ?? 1);
    process.stdout.write(filtrado.saida);
    return {exitCode: filtrado.codigoSaida, stdout: filtrado.saida, stderr};
};

function ehFalhaOperacional(resultado: ResultadoExecucaoComando): boolean {
    const saida = `${resultado.stdout ?? ""}\n${resultado.stderr ?? ""}`;
    try {
        const json = JSON.parse(resultado.stdout ?? "") as {error?: unknown};
        if (json.error !== undefined) {
            return true;
        }
    } catch {
        // Saidas textuais de ferramentas como Knip sao validas.
    }
    return /\b(?:ENOENT|ENOLOCK|MODULE_NOT_FOUND)\b|npm error|command not found|not recognized as an internal or external command/i.test(saida);
}

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
        let resultado: ResultadoExecucaoComando;
        try {
            resultado = await executarComando(escopo.comando, escopo.argumentos, escopo.diretorio, escopo.ignorarAtualizacoes);
        } catch (erro: unknown) {
            const mensagem = erro instanceof Error ? erro.message : String(erro);
            escreverLinha(`Falha ao executar ${escopo.titulo}: ${mensagem}`);
            resultado = {exitCode: undefined, stderr: mensagem};
        }
        const codigoSaida = resultado.exitCode;
        const status: StatusAuditoriaDependencias = codigoSaida === undefined
            ? "falha"
            : codigoSaida === 0
                ? "ok"
                : escopo.codigoNaoZeroIndicaAchados && !ehFalhaOperacional(resultado)
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
    type StatusAuditoriaDependencias,
    filtrarSaidaNpmOutdated
};
