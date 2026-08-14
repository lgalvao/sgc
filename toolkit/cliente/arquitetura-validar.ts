import path from "node:path";
import {createRequire} from "node:module";
import {realpathSync} from "node:fs";
import pc from "picocolors";
import {cruise, type IConfiguration, type ICruiseResult, type IReporterOutput, type IRuleSummary, type IViolation} from "dependency-cruiser";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverLinha, imprimirJson} from "../biblioteca/saida.js";
import {auditarAcoesServidorCliente} from "./acoes-servidor-lib.js";

interface ViolacaoAcaoArquitetura {
    regra: string;
    arquivo: string;
    linha: number;
    identificador: string;
    motivo: string;
    trecho: string;
}

interface ResultadoAcoesServidor {
    dispensadas?: number;
    violacoes?: ViolacaoAcaoArquitetura[];
}

interface ResultadoValidacaoArquitetura extends ICruiseResult {
    exitCode: number;
    acoesServidor: ResultadoAcoesServidor;
}

interface ResumoValidacaoArquitetura {
    versaoResumo: 1;
    geradoEm: string;
    truncado: true;
    limiteItens: number;
    exitCode: number;
    totalModulos: number;
    resumo: {
        totalViolacoes: number;
        porRegra: Array<{regra: string; total: number}>;
    };
    violacoes: Array<ReturnType<typeof formatarViolacao>>;
    acoesServidor: {
        dispensadas: number;
        totalViolacoes: number;
        violacoes: ViolacaoAcaoArquitetura[];
    };
}

function carregarRegras(caminhoConfiguracao: string): IConfiguration {
    const require = createRequire(import.meta.url);
    delete require.cache[caminhoConfiguracao];
    return require(caminhoConfiguracao) as IConfiguration;
}

function extrairViolacoes(parsed: ICruiseResult): IViolation[] {
    return parsed.summary.violations ?? [];
}

function normalizarSaidaCaminho(caminhoArquivo: string | null | undefined, diretorioBase: string): string {
    if (!caminhoArquivo) {
        return "(desconhecido)";
    }
    if (!path.isAbsolute(caminhoArquivo)) {
        return caminhoArquivo.replaceAll("\\", "/");
    }
    return path.relative(diretorioBase, caminhoArquivo).replaceAll("\\", "/");
}

function formatarViolacao(violacao: IViolation, diretorioBase: string): {
    origem: string;
    destino: string;
    regra: string;
    comentario: string;
} {
    const origem = normalizarSaidaCaminho(violacao.from, diretorioBase);
    const destinoBruto = typeof violacao.to === "string" ? violacao.to : "(destino desconhecido)";
    const destino = typeof destinoBruto === "string"
        ? normalizarSaidaCaminho(destinoBruto, diretorioBase)
        : JSON.stringify(destinoBruto);
    const regra = violacao.rule?.name ?? "regra-desconhecida";
    const comentarioRegra = violacao.rule as IRuleSummary & {comment?: string};
    const comentario = comentarioRegra.comment ?? violacao.comment ?? "Violacao arquitetural detectada.";
    return {origem, destino, regra, comentario};
}

function imprimirViolacoes(violacoes: IViolation[], diretorioBase: string): void {
    if (violacoes.length === 0) {
        escreverLinha(`${pc.green("✓")} Nenhuma violacao arquitetural encontrada.`);
        return;
    }

    escreverLinha(pc.red(`Foram encontradas ${violacoes.length} violacoes arquiteturais:`));
    violacoes.forEach((violacao, indice) => {
        const item = formatarViolacao(violacao, diretorioBase);
        escreverLinha(`${indice + 1}. [${item.regra}] ${item.origem} -> ${item.destino}`);
        escreverLinha(`   ${item.comentario}`);
    });
}

function imprimirViolacoesAcoesServidor(resultado: ResultadoAcoesServidor): void {
    const violacoes = resultado.violacoes ?? [];
    if (violacoes.length === 0) {
        const detalheExcecoes = (resultado.dispensadas ?? 0) > 0 ? ` (${resultado.dispensadas} dispensadas por excecao)` : "";
        escreverLinha(`${pc.green("✓")} Nenhum calculo local novo de habilitacao/exibicao de acoes encontrado${detalheExcecoes}.`);
        return;
    }

    escreverLinha(pc.red(`Foram encontrados ${violacoes.length} calculos locais de habilitacao/exibicao de acoes:`));
    violacoes.forEach((violacao, indice) => {
        escreverLinha(`${indice + 1}. [${violacao.regra}] ${violacao.arquivo}:${violacao.linha} (${violacao.identificador})`);
        escreverLinha(`   ${violacao.motivo}`);
        escreverLinha(`   ${violacao.trecho}`);
    });
}

function criarResumoJson(resultado: ResultadoValidacaoArquitetura, diretorioBase: string): ResumoValidacaoArquitetura {
    const limiteItens = 20;
    const violacoes = resultado.summary.violations ?? [];
    const porRegra = Map.groupBy(violacoes, (violacao) => violacao.rule?.name ?? "regra-desconhecida");
    return {
        versaoResumo: 1,
        geradoEm: new Date().toISOString(),
        truncado: true,
        limiteItens,
        exitCode: resultado.exitCode,
        totalModulos: resultado.modules?.length ?? 0,
        resumo: {
            totalViolacoes: violacoes.length,
            porRegra: [...porRegra.entries()]
                .map(([regra, itens]) => ({regra, total: itens.length}))
                .toSorted((a, b) => b.total - a.total || a.regra.localeCompare(b.regra, "pt-BR")),
        },
        violacoes: violacoes
            .toSorted((a, b) => (a.rule?.name ?? "").localeCompare(b.rule?.name ?? "", "pt-BR")
                || a.from.localeCompare(b.from, "pt-BR"))
            .slice(0, limiteItens)
            .map((violacao) => formatarViolacao(violacao, diretorioBase)),
        acoesServidor: {
            dispensadas: resultado.acoesServidor.dispensadas ?? 0,
            totalViolacoes: resultado.acoesServidor.violacoes?.length ?? 0,
            violacoes: resultado.acoesServidor.violacoes?.slice(0, limiteItens) ?? [],
        },
    };
}

interface OpcoesValidacaoArquiteturaCliente {
    base?: string;
}

async function executarValidacaoArquiteturaCliente(
    opcoes: OpcoesValidacaoArquiteturaCliente = {}
): Promise<ResultadoValidacaoArquitetura> {
    const diretorioBase = realpathSync(path.resolve(opcoes.base ?? DIRETORIO_RAIZ));
    const diretorioCliente = realpathSync(resolverCaminhoConfigurado("cliente", diretorioBase));
    const caminhoSrc = "src";
    const caminhoConfiguracao = path.join(diretorioCliente, ".dependency-cruiser.cjs");
    const regrasBase = carregarRegras(caminhoConfiguracao);
    const regras = {
        ...regrasBase,
        options: {
            ...regrasBase.options,
            tsConfig: {
                fileName: path.join(diretorioCliente, "tsconfig.json"),
            },
        },
    };

    const opcoesCruise = {
            validate: true,
            ruleSet: regras,
            outputType: "json",
            // A API em runtime aceita cwd, embora a declaração atual não o exponha.
            baseDir: diretorioCliente,
            cwd: diretorioCliente,
        } as unknown as Parameters<typeof cruise>[1];
    const resultadoCruise: IReporterOutput = await cruise([caminhoSrc], opcoesCruise);

    const saida = typeof resultadoCruise.output === "string"
        ? JSON.parse(resultadoCruise.output) as ICruiseResult
        : resultadoCruise.output;
    const acoesServidor = await auditarAcoesServidorCliente({
        base: diretorioBase,
    });

    return {
        ...saida,
        exitCode: resultadoCruise.exitCode,
        acoesServidor,
        summary: {
            ...saida.summary,
            violations: extrairViolacoes(saida),
        },
    };
}

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    const emitirJson = argumentos.includes("--json") || argumentos.includes("--json-resumido");
    const emitirJsonResumido = argumentos.includes("--json-resumido");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoToolkit: "cliente arquitetura validar",
            descricao: "Valida regras arquiteturais duras do cliente usando resolucao real de modulos.",
            opcoes: [
                "--json               Emite o resultado bruto em JSON.",
                "--json-resumido      Emite totais, regras e violacoes limitadas em JSON.",
                "--base <diretorio>   Sobrescreve o diretorio base da validacao.",
            ],
            exemplos: [
                "ferramentas cliente arquitetura validar",
                "ferramentas cliente arquitetura validar --json",
                "ferramentas cliente arquitetura validar --base C:/sgc",
            ],
        });
        return;
    }

    const resultado = await executarValidacaoArquiteturaCliente({
        base: lerOpcao(argumentos, "--base", undefined),
    });

    if (emitirJson) {
        if (emitirJsonResumido) {
            const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
            imprimirJson(criarResumoJson(resultado, diretorioBase));
        } else {
            imprimirJson(resultado);
        }
    } else {
        const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
        imprimirViolacoes(resultado.summary.violations ?? [], diretorioBase);
        imprimirViolacoesAcoesServidor(resultado.acoesServidor);
    }

    if ((resultado.summary.violations ?? []).length > 0 || resultado.exitCode !== 0 || (resultado.acoesServidor?.violacoes ?? []).length > 0) {
        process.exitCode = 1;
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro na validacao arquitetural: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    executarValidacaoArquiteturaCliente,
    principal,
};
