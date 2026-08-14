import pc from "picocolors";
import path from "node:path";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import {lerNumero, lerOpcao} from "../biblioteca/cli-opcoes.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {extrairCoberturaCliente, type ArquivoCobertura, type ResultadoCoberturaCliente} from "../biblioteca/dominios/cobertura-web.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../biblioteca/saida.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";

const VERSAO_SCHEMA_RESULTADO = "1.0.0" as const;

interface ArquivoRamificacoes {
    arquivo: string;
    ramificacoesTotal: number;
    ramificacoesPercentual: number;
    ramificacoesPerdidas: number;
}

interface ResultadoRamificacoes {
    status: "ok";
    versaoSchema: typeof VERSAO_SCHEMA_RESULTADO;
    geradoEm: string;
    totais: ResultadoCoberturaCliente["ramificacoes"];
    arquivos: ArquivoRamificacoes[];
}

interface ResumoRamificacoes {
    versaoResumo: 1;
    status: ResultadoRamificacoes["status"];
    versaoSchema: typeof VERSAO_SCHEMA_RESULTADO;
    geradoEm: string;
    truncado: true;
    limiteItens: number;
    totais: ResultadoRamificacoes["totais"];
    arquivos: ResultadoRamificacoes["arquivos"];
}

function criarResumoJson(resultado: ResultadoRamificacoes): ResumoRamificacoes {
    const limiteItens = 20;
    return {
        versaoResumo: 1,
        status: resultado.status,
        versaoSchema: resultado.versaoSchema,
        geradoEm: resultado.geradoEm,
        truncado: true,
        limiteItens,
        totais: resultado.totais,
        arquivos: resultado.arquivos.slice(0, limiteItens)
    };
}

function calcularRamificacoesPerdidas(arquivo: ArquivoCobertura): number {
    return Math.max(0, arquivo.ramificacoesTotal - Math.round((arquivo.ramificacoesPercentual / 100) * arquivo.ramificacoesTotal));
}

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    const emitirJsonResumido = argumentos.includes("--json-resumido");
    const emitirJson = argumentos.includes("--json") || emitirJsonResumido;
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoToolkit: "cliente cobertura ramificacoes",
            descricao: "Lista lacunas de cobertura de ramificacoes no cliente por arquivo.",
            opcoes: [
                "--json          Saída estruturada em JSON.",
                "--json-resumido Saída JSON limitada a arquivos prioritários.",
                "--limite <n>    Limita a quantidade de arquivos exibidos. Padrão: 20.",
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
    const limite = lerNumero(argumentos, "--limite", 20, {minimo: 0}) ?? 20;
    const coleta = await extrairCoberturaCliente(caminhoRelatorio, {diretorioBase});
    const arquivos: ArquivoRamificacoes[] = coleta.arquivos
        .map((arquivo) => ({
            arquivo: arquivo.arquivo,
            ramificacoesTotal: arquivo.ramificacoesTotal,
            ramificacoesPercentual: arquivo.ramificacoesPercentual,
            ramificacoesPerdidas: calcularRamificacoesPerdidas(arquivo)
        }))
        .filter((arquivo) => arquivo.ramificacoesPerdidas > 0)
        .toSorted((a, b) => b.ramificacoesPerdidas - a.ramificacoesPerdidas || a.ramificacoesPercentual - b.ramificacoesPercentual)
        .slice(0, limite);

    const resultado: ResultadoRamificacoes = {
        status: "ok",
        versaoSchema: VERSAO_SCHEMA_RESULTADO,
        geradoEm: new Date().toISOString(),
        totais: coleta.ramificacoes,
        arquivos
    };

    if (emitirJsonResumido) {
        imprimirJson(criarResumoJson(resultado));
        return;
    }

    if (emitirJson) {
        imprimirJson(resultado);
        return;
    }

    imprimirCabecalho("COBERTURA DE RAMIFICAÇÕES CLIENTE");
    escreverLinha(`Cobertura global de ramificações: ${pc.bold(`${coleta.ramificacoes.percentual}%`)} (${coleta.ramificacoes.cobertos}/${coleta.ramificacoes.total})`);
    escreverLinha("");

    if (arquivos.length === 0) {
        escreverLinha(pc.green("Nenhuma lacuna de ramificações encontrada nos arquivos auditados."));
        return;
    }

    escreverLinha(pc.bold(pc.underline(`TOP ${arquivos.length} ARQUIVOS COM LACUNAS DE RAMIFICAÇÕES:`)));
    arquivos.forEach((arquivo, indice) => {
        escreverLinha(`${indice + 1}. ${pc.bold(arquivo.arquivo)}`);
        escreverLinha(`   Ramificações perdidas: ${arquivo.ramificacoesPerdidas}/${arquivo.ramificacoesTotal} | Cobertura: ${arquivo.ramificacoesPercentual}%`);
    });
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro ao analisar ramificações do cliente: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    criarResumoJson,
    principal,
};
