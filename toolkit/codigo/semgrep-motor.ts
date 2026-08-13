import fs from "node:fs/promises";
import path from "node:path";
import {execa} from "execa";
import which from "which";

interface PosicaoSemgrep {
    line?: number;
}

interface ExtraSemgrep {
    message?: string;
}

interface AchadoSemgrep {
    check_id?: string;
    path?: string;
    start?: PosicaoSemgrep;
    extra?: ExtraSemgrep;
    [chave: string]: unknown;
}

interface ResultadoSemgrep {
    results: AchadoSemgrep[];
    [chave: string]: unknown;
}

interface OpcoesExecucaoSemgrep {
    regra: string;
    diretorios: readonly string[];
    auto?: boolean;
    diretorioBase?: string;
    comando?: string;
}

interface ExecucaoSemgrep {
    comando: string;
    regra: string;
    diretorios: string[];
    auto: boolean;
    codigoSaida: number;
    resultadoJson: ResultadoSemgrep;
}

interface CaminhosRelatoriosSemgrep {
    caminhoResultadoJson: string;
    caminhoResultadoMd: string;
}

function obterComandoSemgrep(caminhoBusca: string | undefined = process.env.PATH): string {
    return which.sync("semgrep", {path: caminhoBusca, nothrow: true}) ?? "semgrep";
}

function ehRegistro(valor: unknown): valor is Record<string, unknown> {
    return typeof valor === "object" && valor !== null;
}

function ehAchadoSemgrep(valor: unknown): valor is AchadoSemgrep {
    return ehRegistro(valor);
}

function normalizarResultadoSemgrep(valor: unknown): ResultadoSemgrep {
    if (!ehRegistro(valor)) {
        return {results: []};
    }

    const resultados = Array.isArray(valor.results) ? valor.results.filter(ehAchadoSemgrep) : [];
    return {...valor, results: resultados};
}

function normalizarCaminhoAchado(caminho: string, diretorioBase: string): string {
    if (!caminho) {
        return "";
    }
    const caminhoAbsoluto = path.isAbsolute(caminho)
        ? caminho
        : path.resolve(diretorioBase, caminho);
    return path.relative(diretorioBase, caminhoAbsoluto).replaceAll("\\", "/");
}

function criarResumoSemgrep(resultadoJson: ResultadoSemgrep, regra: string, diretorioBase: string): string {
    const achados = resultadoJson.results;
    const porRegra = new Map<string, AchadoSemgrep[]>();

    for (const achado of achados) {
        const codigoRegra = achado.check_id ?? "sem-id";
        const itens = porRegra.get(codigoRegra) ?? [];
        itens.push(achado);
        porRegra.set(codigoRegra, itens);
    }

    const linhas: string[] = [];
    linhas.push("# Auditoria Semgrep", "", `Regra: \`${regra}\``, "", `Total de achados: ${achados.length}`, "");

    if (achados.length === 0) {
        linhas.push("Nenhum achado encontrado.");
        return linhas.join("\n");
    }

    linhas.push("| Regra | Achados |", "|---|---:|", "");
    for (const [codigoRegra, itens] of [...porRegra.entries()].toSorted((a, b) => b[1].length - a[1].length)) {
        linhas.push(`| \`${codigoRegra}\` | ${itens.length} |`);
    }

    linhas.push("", "## Primeiros achados", "");
    for (const achado of achados.slice(0, 20)) {
        const caminho = normalizarCaminhoAchado(achado.path ?? "", diretorioBase);
        linhas.push(`- \`${achado.check_id ?? "sem-id"}\` em \`${caminho}:${achado.start?.line ?? "?"}\` - ${achado.extra?.message ?? ""}`);
    }

    return linhas.join("\n");
}

async function executarSemgrep({
    regra,
    diretorios,
    auto = false,
    diretorioBase = process.cwd(),
    comando
}: OpcoesExecucaoSemgrep): Promise<ExecucaoSemgrep> {
    if (diretorios.length === 0) {
        throw new Error("A execução do Semgrep exige pelo menos um diretório-alvo.");
    }

    const baseResolvida = path.resolve(diretorioBase);
    const alvos = [...diretorios];
    const comandoResolvido = comando ?? obterComandoSemgrep();
    const diretorioComando = path.isAbsolute(comandoResolvido) ? path.dirname(comandoResolvido) : "";
    const caminhoBusca = [diretorioComando, process.env.PATH]
        .filter((valor): valor is string => Boolean(valor))
        .join(path.delimiter);
    const configs = auto ? ["--config", "auto", "--config", regra] : ["--config", regra];
    const resultado = await execa(comandoResolvido, [
        "scan",
        ...configs,
        "--json",
        ...alvos
    ], {
        cwd: baseResolvida,
        env: {...process.env, PATH: caminhoBusca},
        reject: false
    });

    const valor: unknown = JSON.parse(resultado.stdout || "{}");
    return {
        comando: comandoResolvido,
        regra,
        diretorios: alvos,
        auto,
        codigoSaida: resultado.exitCode ?? 0,
        resultadoJson: normalizarResultadoSemgrep(valor)
    };
}

async function gravarRelatoriosSemgrep(
    execucao: ExecucaoSemgrep,
    diretorioSaida: string,
    diretorioBase: string
): Promise<CaminhosRelatoriosSemgrep> {
    const caminhoResultadoJson = path.join(diretorioSaida, "resultado.json");
    const caminhoResultadoMd = path.join(diretorioSaida, "resumo.md");
    await fs.mkdir(diretorioSaida, {recursive: true});
    await fs.writeFile(caminhoResultadoJson, `${JSON.stringify(execucao.resultadoJson, null, 2)}\n`, "utf-8");
    await fs.writeFile(caminhoResultadoMd, `${criarResumoSemgrep(execucao.resultadoJson, execucao.regra, diretorioBase)}\n`, "utf-8");
    return {caminhoResultadoJson, caminhoResultadoMd};
}

export {
    executarSemgrep,
    gravarRelatoriosSemgrep,
    normalizarCaminhoAchado,
    obterComandoSemgrep
};
