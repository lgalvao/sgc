
import fs from "node:fs/promises";
import path from "node:path";
import {execa} from "execa";
import pc from "picocolors";
import which from "which";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverErro, escreverLinha, imprimirCabecalho, imprimirJson} from "../biblioteca/saida.js";

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

interface OpcoesSemgrep {
    regra: string;
    diretorios?: string[];
    auto?: boolean;
    diretorioBase?: string;
}

interface ExecucaoSemgrep {
    comando: string;
    regra: string;
    diretorios: string[];
    auto: boolean;
    codigoSaida: number;
    resultadoJson: ResultadoSemgrep;
}

interface CaminhosRelatorios {
    caminhoResultadoJson: string;
    caminhoResultadoMd: string;
}

function extrairLista(argumentos: string[], nome: string): string[] {
    const valores: string[] = [];
    const prefixoAtribuicao = `${nome}=`;
    for (let indice = 0; indice < argumentos.length; indice += 1) {
        if (argumentos[indice] === nome) {
            const valor = argumentos[indice + 1];
            if (!valor || valor.startsWith("--")) {
                throw new Error(`Informe um valor para ${nome}.`);
            }
            valores.push(valor);
            indice += 1;
        } else if (argumentos[indice].startsWith(prefixoAtribuicao)) {
            const valor = argumentos[indice].slice(prefixoAtribuicao.length);
            if (!valor) {
                throw new Error(`Informe um valor para ${nome}.`);
            }
            valores.push(valor);
        }
    }
    return valores;
}

function resolverDiretoriosPadrao(diretorioBase: string = DIRETORIO_RAIZ): string[] {
    const baseResolvida = path.resolve(diretorioBase);
    return [
        path.relative(baseResolvida, resolverCaminhoConfigurado("codigoServidor", baseResolvida)),
        path.relative(baseResolvida, resolverCaminhoConfigurado("codigoCliente", baseResolvida))
    ];
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

function criarResumo(resultadoJson: ResultadoSemgrep, regra: string, diretorioBase: string): string {
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

    linhas.push("| Regra | Achados |", "|---|---:|");
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
    diretorioBase = DIRETORIO_RAIZ
}: OpcoesSemgrep): Promise<ExecucaoSemgrep> {
    const baseResolvida = path.resolve(diretorioBase);
    const alvos = diretorios ?? resolverDiretoriosPadrao(baseResolvida);
    const comando = obterComandoSemgrep();
    const diretorioComando = path.isAbsolute(comando) ? path.dirname(comando) : "";
    const caminhoBusca = [diretorioComando, process.env.PATH]
        .filter((valor): valor is string => Boolean(valor))
        .join(path.delimiter);
    const configs = auto ? ["--config", "auto", "--config", regra] : ["--config", regra];
    const resultado = await execa(comando, [
        "scan",
        ...configs,
        "--json",
        ...alvos
    ], {
        cwd: baseResolvida,
        env: {PATH: caminhoBusca},
        reject: false
    });

    const valor: unknown = JSON.parse(resultado.stdout || "{}");
    return {
        comando,
        regra,
        diretorios: alvos,
        auto,
        codigoSaida: resultado.exitCode ?? 0,
        resultadoJson: normalizarResultadoSemgrep(valor)
    };
}

async function gravarRelatorios(
    execucao: ExecucaoSemgrep,
    diretorioSaida: string,
    diretorioBase: string
): Promise<CaminhosRelatorios> {
    const caminhoResultadoJson = path.join(diretorioSaida, "resultado.json");
    const caminhoResultadoMd = path.join(diretorioSaida, "resumo.md");
    await fs.mkdir(diretorioSaida, {recursive: true});
    await fs.writeFile(caminhoResultadoJson, `${JSON.stringify(execucao.resultadoJson, null, 2)}\n`, "utf-8");
    await fs.writeFile(caminhoResultadoMd, `${criarResumo(execucao.resultadoJson, execucao.regra, diretorioBase)}\n`, "utf-8");
    return {caminhoResultadoJson, caminhoResultadoMd};
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const argumentosValidados = validarArgumentosEntradaDireta(import.meta.url, argumentos);
    if (argumentosValidados.includes("--help") || argumentosValidados.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "codigo semgrep auditar",
            scriptDireto: "codigo/semgrep-auditar.ts",
            descricao: "Executa um piloto de Semgrep OSS com a política configurada para monitorar deriva estrutural no projeto.",
            opcoes: [
                "--regra <arquivo>     Sobrescreve o arquivo de regras YAML.",
                "--diretorio <caminho> Adiciona diretório-alvo; pode ser repetido.",
                "--base <diretorio>    Usa outra raiz para execução e artefatos.",
                "--auto                Acumula as regras locais com `--config auto` do Semgrep CE.",
                "--json                Emite resumo estruturado em JSON.",
                "--gravar              Grava relatórios JSON e Markdown em disco."
            ],
            exemplos: [
                "npx tsx toolkit/sgc.ts codigo semgrep auditar",
                "npx tsx toolkit/sgc.ts codigo semgrep auditar --diretorio backend/src/main/java/exemplo",
                "npx tsx toolkit/sgc.ts codigo semgrep auditar --auto --json"
            ]
        });
        return;
    }

    const emitirJson = argumentosValidados.includes("--json");
    const gravar = argumentosValidados.includes("--gravar");
    const auto = argumentosValidados.includes("--auto");
    const diretorioBase = path.resolve(lerOpcao(argumentosValidados, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const regra = lerOpcao(argumentosValidados, "--regra", resolverCaminhoConfigurado("regrasSemgrep", diretorioBase))
        ?? resolverCaminhoConfigurado("regrasSemgrep", diretorioBase);
    const diretorios = extrairLista(argumentosValidados, "--diretorio");
    const alvos = diretorios.length > 0 ? diretorios : resolverDiretoriosPadrao(diretorioBase);
    const diretorioSaida = path.join(resolverCaminhoConfigurado("artefatosQualidade", diretorioBase), "semgrep", "mais-recente");

    if (!emitirJson) {
        imprimirCabecalho("AUDITORIA SEMGREP (PILOTO)");
        escreverLinha(`Regra: ${pc.dim(regra)}`);
        escreverLinha(`Alvos: ${alvos.map(item => pc.dim(item)).join(", ")}`);
        escreverLinha(`Modo auto: ${auto ? "sim" : "não"}`);
    }

    const execucao = await executarSemgrep({regra, diretorios: alvos, auto, diretorioBase});

    if (gravar) {
        const caminhosRelatorios = await gravarRelatorios(execucao, diretorioSaida, diretorioBase);
        if (!emitirJson) {
            escreverLinha(`Relatório JSON: ${pc.dim(caminhosRelatorios.caminhoResultadoJson)}`);
            escreverLinha(`Resumo Markdown: ${pc.dim(caminhosRelatorios.caminhoResultadoMd)}`);
        }
    }

    const achados = execucao.resultadoJson.results;
    if (!emitirJson) {
        escreverLinha(`Achados: ${achados.length}`);
        if (achados.length > 0) {
            for (const achado of achados.slice(0, 10)) {
                const caminho = normalizarCaminhoAchado(achado.path ?? "", diretorioBase);
                escreverLinha(`- ${achado.check_id ?? "sem-id"} em ${caminho}:${achado.start?.line ?? "?"}`);
            }
        } else {
            escreverLinha(pc.green("Nenhum achado encontrado pelas regras locais."));
        }
    }

    if (emitirJson) {
        imprimirJson({
            regra,
            diretorios: alvos,
            auto,
            codigoSaida: execucao.codigoSaida,
            totalAchados: achados.length
        });
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro: unknown) => {
        escreverErro(`Erro ao executar auditoria Semgrep: ${erro instanceof Error ? erro.message : String(erro)}\n`);
        process.exitCode = 1;
    });
}

export {
    executarSemgrep,
    obterComandoSemgrep,
    normalizarCaminhoAchado,
    principal,
    resolverDiretoriosPadrao
};
