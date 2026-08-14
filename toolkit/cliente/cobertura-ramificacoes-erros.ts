import fs from "node:fs/promises";
import path from "node:path";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import {lerNumero, lerOpcao} from "../biblioteca/cli-opcoes.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../biblioteca/saida.js";
import {extrairCoberturaCliente, type ArquivoCobertura, type ResultadoCoberturaCliente} from "../biblioteca/dominios/cobertura-web.js";

const VERSAO_SCHEMA_RESULTADO = "1.0.0" as const;

interface LinhaSuspeita {
    numero: number;
    texto: string;
}

interface ArquivoRamificacoesErros extends Pick<ArquivoCobertura, "arquivo" | "ramificacoesTotal" | "ramificacoesPercentual"> {
    ramificacoesPerdidas: number;
    linhasSuspeitas: LinhaSuspeita[];
}

interface ResultadoRamificacoesErros {
    status: "ok";
    versaoSchema: typeof VERSAO_SCHEMA_RESULTADO;
    geradoEm: string;
    totais: ResultadoCoberturaCliente["ramificacoes"];
    arquivos: ArquivoRamificacoesErros[];
}

const PADROES_SUSPEITOS: RegExp[] = [
    /\bcatch\s*\(/,
    /\bnormalizarErro\s*\(/,
    /\bnotify\s*\(/,
    /\bdeveNotificarGlobalmente\s*\(/,
    /\bapp\.config\.errorHandler\b/,
    /\bultimoErro\b/,
    /\berro[A-Z_a-zA-Z0-9]*\s*=/,
    /\bPromise\.reject\b/,
];

function calcularRamificacoesPerdidas(arquivo: ArquivoCobertura): number {
    return Math.max(0, arquivo.ramificacoesTotal - Math.round((arquivo.ramificacoesPercentual / 100) * arquivo.ramificacoesTotal));
}

async function coletarLinhasSuspeitas(caminhoRelativo: string, diretorioBase: string): Promise<LinhaSuspeita[]> {
    const caminhoAbsoluto = path.resolve(diretorioBase, caminhoRelativo);
    let conteudo: string;
    try {
        conteudo = await fs.readFile(caminhoAbsoluto, "utf8");
    } catch (erro: unknown) {
        if (typeof erro === "object" && erro !== null && "code" in erro && erro.code === "ENOENT") {
            return [];
        }
        throw erro;
    }
    const linhas = conteudo.split(/\r?\n/);
    return linhas
        .map((linha, indice): LinhaSuspeita => ({numero: indice + 1, texto: linha.trim()}))
        .filter(({texto}) => PADROES_SUSPEITOS.some((padrao) => padrao.test(texto)))
        .slice(0, 12);
}

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoToolkit: "cliente cobertura ramificacoes-erros",
            descricao: "Cruza lacunas de ramificacoes do cliente com sinais de tratamento de erro suspeito.",
            opcoes: [
                "--json          Saída estruturada em JSON.",
                "--limite <n>    Limita a quantidade de arquivos inspecionados. Padrão: 15.",
                "--arquivo <arquivo> Usa um relatório V8 específico.",
                "--base <diretorio> Resolve o relatório relativo a outra base."
            ]
        });
        return;
    }

    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const caminhoInformado = lerOpcao(argumentos, "--arquivo", undefined);
    const caminhoRelatorio = caminhoInformado
        ? path.resolve(diretorioBase, caminhoInformado)
        : resolverCaminhoConfigurado("coberturaCliente", diretorioBase);
    const limite = lerNumero(argumentos, "--limite", 15, {minimo: 0}) ?? 15;
    const coleta = await extrairCoberturaCliente(caminhoRelatorio, {diretorioBase});
    const candidatos = coleta.arquivos
        .map((arquivo) => ({
            ...arquivo,
            ramificacoesPerdidas: calcularRamificacoesPerdidas(arquivo),
        }))
        .filter((arquivo) => arquivo.ramificacoesPerdidas > 0)
        .toSorted((a, b) => b.ramificacoesPerdidas - a.ramificacoesPerdidas || a.ramificacoesPercentual - b.ramificacoesPercentual)
        .slice(0, limite);

    const arquivos: ArquivoRamificacoesErros[] = [];
    for (const candidato of candidatos) {
        const linhasSuspeitas = await coletarLinhasSuspeitas(candidato.arquivo, diretorioBase);
        if (linhasSuspeitas.length === 0) {
            continue;
        }
        arquivos.push({
            arquivo: candidato.arquivo,
            ramificacoesPerdidas: candidato.ramificacoesPerdidas,
            ramificacoesTotal: candidato.ramificacoesTotal,
            ramificacoesPercentual: candidato.ramificacoesPercentual,
            linhasSuspeitas,
        });
    }

    const resultado: ResultadoRamificacoesErros = {
        status: "ok",
        versaoSchema: VERSAO_SCHEMA_RESULTADO,
        geradoEm: new Date().toISOString(),
        totais: coleta.ramificacoes,
        arquivos,
    };

    if (emitirJson) {
        imprimirJson(resultado);
        return;
    }

    imprimirCabecalho("RAMIFICAÇÕES DE ERRO SUSPEITAS NO CLIENTE");
    escreverLinha(`Cobertura global de ramificações: ${pc.bold(`${coleta.ramificacoes.percentual}%`)} (${coleta.ramificacoes.cobertos}/${coleta.ramificacoes.total})`);
    escreverLinha("");

    if (arquivos.length === 0) {
        escreverLinha(pc.green("Nenhum ponto critico com sinais claros de tratamento de erro foi encontrado no recorte atual."));
        return;
    }

    arquivos.forEach((arquivo, indice) => {
        escreverLinha(`${indice + 1}. ${pc.bold(arquivo.arquivo)}`);
        escreverLinha(`   Ramificações perdidas: ${arquivo.ramificacoesPerdidas}/${arquivo.ramificacoesTotal} | Cobertura: ${arquivo.ramificacoesPercentual}%`);
        arquivo.linhasSuspeitas.forEach((linha) => {
            escreverLinha(`   L${linha.numero}: ${pc.dim(linha.texto)}`);
        });
        escreverLinha("");
    });
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro ao cruzar ramificações de erro suspeitas: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    principal,
};
