import process from "node:process";
import {ehEntradaPrincipal} from "../biblioteca/execucao.js";
import {escreverErro} from "../biblioteca/saida.js";
import {criarAdaptadoresSgc, PERFIS_SGC} from "./coleta-adaptadores-sgc.js";
import {executarComando, executarComandoSgc} from "./coleta-executor.js";
import {consolidarJUnit, parseJsonSeguro} from "./coleta-leitores.js";
import {
    criarExecucao,
    executarColeta,
    obterOpcoesPlaywright,
    registrarResultadoExecucao,
    type AdaptadorQualidade,
    type CatalogoAdaptadores,
    type CatalogoPerfisColeta,
    type ExecucaoQualidade,
    type OpcoesComando,
    type OpcoesPlaywright,
    type ResultadoComando
} from "./coleta-motor.js";
import type {ContextoColeta, FotografiaColeta, MetadadosControleVersao, OpcoesColetaMotor, PontoCriticoQualidade, ResultadoJUnit} from "./coleta-motor.js";
import {prepararDiretoriosFotografia, persistirFotografia} from "./coleta-fotografia.js";

interface OpcoesColeta extends Omit<OpcoesColetaMotor, "adaptadores" | "perfis"> {
    adaptadores?: CatalogoAdaptadores;
    perfis?: CatalogoPerfisColeta;
}

const PERFIS = PERFIS_SGC;

const ADAPTADORES: CatalogoAdaptadores = criarAdaptadoresSgc({
    criarExecucao,
    executarComando,
    executarComandoSgc,
    consolidarJUnit,
    registrarResultadoExecucao,
    parseJsonSeguro,
    obterOpcoesPlaywright
});

async function principal(
    argumentos: string[] = process.argv.slice(2),
    opcoes: OpcoesColeta = {}
): Promise<FotografiaColeta> {
    return executarColeta(argumentos, {
        ...opcoes,
        adaptadores: opcoes.adaptadores ?? ADAPTADORES,
        perfis: opcoes.perfis ?? PERFIS,
        prepararDiretoriosFotografia: opcoes.prepararDiretoriosFotografia ?? prepararDiretoriosFotografia,
        persistirFotografia: opcoes.persistirFotografia ?? persistirFotografia
    });
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverErro(`Erro ao coletar qualidade: ${mensagem}\n`);
        process.exitCode = 1;
    });
}

export {
    ADAPTADORES,
    PERFIS,
    obterOpcoesPlaywright,
    principal,
    type AdaptadorQualidade,
    type CatalogoAdaptadores,
    type CatalogoPerfisColeta,
    type ContextoColeta,
    type ExecucaoQualidade,
    type FotografiaColeta,
    type MetadadosControleVersao,
    type OpcoesColeta,
    type OpcoesComando,
    type OpcoesPlaywright,
    type PontoCriticoQualidade,
    type ResultadoComando,
    type ResultadoJUnit
};
