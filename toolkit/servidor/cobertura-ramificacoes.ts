import path from "node:path";
import pc from "picocolors";
import {lerNumero, lerOpcao} from "../biblioteca/cli-opcoes.js";
import {resolverNaRaiz} from "../biblioteca/caminhos.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {extrairCoberturaJacoco} from "../biblioteca/dominios/cobertura-java.js";
import type {ClasseCobertura, ResultadoCoberturaJacoco} from "../biblioteca/dominios/cobertura-java.js";
import {escreverErro, escreverLinha, imprimirCabecalho, imprimirJson} from "../biblioteca/saida.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";
import {PADROES_EXCLUSAO_COBERTURA_SGC} from "./cobertura-padroes.js";

const VERSAO_SCHEMA_RESULTADO = "1.0.0" as const;

interface ClasseRamificacoes {
    nome: string;
    ramificacoesPerdidas: number;
    totalRamificacoes: number;
    ramificacoesPercentual: number;
    ramificacoesPerdidasLista: string[];
}

interface ResultadoRamificacoes {
    status: "ok";
    versaoSchema: typeof VERSAO_SCHEMA_RESULTADO;
    geradoEm: string;
    totais: ResultadoCoberturaJacoco["ramificacoes"];
    classes: ClasseRamificacoes[];
}

interface ResumoRamificacoes {
    versaoResumo: 1;
    status: ResultadoRamificacoes["status"];
    versaoSchema: typeof VERSAO_SCHEMA_RESULTADO;
    geradoEm: string;
    truncado: true;
    limiteItens: number;
    totais: ResultadoRamificacoes["totais"];
    classes: Array<Omit<ClasseRamificacoes, "ramificacoesPerdidasLista">>;
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
        classes: resultado.classes.slice(0, limiteItens).map(({ramificacoesPerdidasLista: _ramificacoesPerdidasLista, ...classe}) => classe)
    };
}

function resumirClasse(classe: ClasseCobertura): ClasseRamificacoes {
    return {
        nome: classe.nome,
        ramificacoesPerdidas: classe.ramificacoesPerdidas,
        totalRamificacoes: classe.totalRamificacoes,
        ramificacoesPercentual: classe.ramificacoesPercentual,
        ramificacoesPerdidasLista: classe.ramificacoesPerdidasLista
    };
}

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    const emitirJsonResumido = argumentos.includes("--json-resumido");
    const emitirJson = argumentos.includes("--json") || emitirJsonResumido;
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoToolkit: "servidor cobertura ramificacoes",
            descricao: "Lista classes com lacunas de ramificacoes no servidor segundo as exclusoes do perfil SGC.",
            opcoes: [
                "--json            Saída estruturada em JSON.",
                "--json-resumido   Saída JSON limitada sem listas de linhas.",
                "--limite <n>      Limita a quantidade de classes exibidas. Padrão: 20.",
                "--filtro <texto>  Filtra por nome de classe/pacote.",
                "--arquivo <xml>   Usa um relatório JaCoCo específico.",
                "--base <diretorio> Resolve o relatório relativo a outra base."
            ]
        });
        return;
    }

    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", resolverNaRaiz()) ?? resolverNaRaiz());
    const arquivoInformado = lerOpcao(argumentos, "--arquivo", "") ?? "";
    const arquivo = arquivoInformado
        ? path.resolve(diretorioBase, arquivoInformado)
        : resolverCaminhoConfigurado("coberturaServidor", diretorioBase);
    const limite = lerNumero(argumentos, "--limite", 20, {minimo: 0}) ?? 20;
    const filtro = lerOpcao(argumentos, "--filtro", "") || null;
    const coleta = await extrairCoberturaJacoco(arquivo, {
        diretorioBase,
        incluirSemLacunas: true,
        aplicarExclusoes: true,
        padroesExclusao: PADROES_EXCLUSAO_COBERTURA_SGC,
        filtro
    });

    const classes = coleta.classes
        .filter((classe) => classe.ramificacoesPerdidas > 0)
        .toSorted((a, b) => b.ramificacoesPerdidas - a.ramificacoesPerdidas || a.ramificacoesPercentual - b.ramificacoesPercentual)
        .slice(0, limite)
        .map(resumirClasse);

    const resultado: ResultadoRamificacoes = {
        status: "ok",
        versaoSchema: VERSAO_SCHEMA_RESULTADO,
        geradoEm: new Date().toISOString(),
        totais: coleta.ramificacoes,
        classes
    };

    if (emitirJsonResumido) {
        imprimirJson(criarResumoJson(resultado));
        return;
    }

    if (emitirJson) {
        imprimirJson(resultado);
        return;
    }

    imprimirCabecalho("COBERTURA DE RAMIFICAÇÕES SERVIDOR");
    escreverLinha(`Cobertura global de ramificações: ${pc.bold(`${coleta.ramificacoes.percentual}%`)} (${coleta.ramificacoes.cobertos}/${coleta.ramificacoes.cobertos + coleta.ramificacoes.perdidos})`);
    escreverLinha("");

    if (classes.length === 0) {
        escreverLinha(pc.green("Nenhuma lacuna de ramificações encontrada nas classes auditadas."));
        return;
    }

    escreverLinha(pc.bold(pc.underline(`TOP ${classes.length} CLASSES COM LACUNAS DE RAMIFICAÇÕES:`)));
    classes.forEach((classe, indice) => {
        escreverLinha(`${indice + 1}. ${pc.bold(classe.nome)}`);
        escreverLinha(`   Ramificações perdidas: ${classe.ramificacoesPerdidas}/${classe.totalRamificacoes} | Cobertura: ${classe.ramificacoesPercentual}%`);
        if (classe.ramificacoesPerdidasLista.length > 0) {
            escreverLinha(`   Linhas: ${pc.dim(classe.ramificacoesPerdidasLista.join(", "))}`);
        }
    });
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverErro(`${pc.red(`Erro ao analisar ramificações do servidor: ${mensagem}`)}\n`);
        process.exitCode = 1;
    });
}

export {
    criarResumoJson,
    principal
};
