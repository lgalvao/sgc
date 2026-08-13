import pc from "picocolors";
import path from "node:path";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {lerNumero, lerOpcao} from "../lib/cli-opcoes.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../lib/execucao.js";
import {extrairCoberturaFrontend, type ArquivoCobertura, type ResultadoCoberturaFrontend} from "../lib/dominios/cobertura-web.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";

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
    totais: ResultadoCoberturaFrontend["ramificacoes"];
    arquivos: ArquivoRamificacoes[];
}

function calcularRamificacoesPerdidas(arquivo: ArquivoCobertura): number {
    return Math.max(0, arquivo.ramificacoesTotal - Math.round((arquivo.ramificacoesPercentual / 100) * arquivo.ramificacoesTotal));
}

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoSgc: "frontend cobertura ramificacoes",
            scriptDireto: "frontend/cobertura-ramificacoes.ts",
            descricao: "Lista lacunas de cobertura de ramificacoes no frontend por arquivo.",
            opcoes: [
                "--json          Saída estruturada em JSON.",
                "--limite <n>    Limita a quantidade de arquivos exibidos. Padrão: 20.",
                "--arquivo <arquivo> Usa um relatório V8 específico.",
                "--base <diretorio> Resolve o relatório relativo a outra base."
            ]
        });
        return;
    }

    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const caminhoRelatorio = lerOpcao(argumentos, "--arquivo", undefined);
    const limite = lerNumero(argumentos, "--limite", 20, {minimo: 0}) ?? 20;
    const coleta = await extrairCoberturaFrontend(caminhoRelatorio, {diretorioBase});
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

    if (emitirJson) {
        imprimirJson(resultado);
        return;
    }

    imprimirCabecalho("COBERTURA DE RAMIFICAÇÕES FRONTEND");
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
        escreverLinha(pc.red(`Erro ao analisar ramificações do frontend: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    principal,
};
