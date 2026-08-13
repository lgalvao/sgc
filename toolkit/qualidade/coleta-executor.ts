import path from "node:path";
import process from "node:process";
import {execa} from "execa";
import {DIRETORIO_TOOLKIT} from "../biblioteca/caminhos.js";
import {resolverCaminhoTsx} from "../biblioteca/execucao.js";
import type {ContextoColeta, OpcoesComando, ResultadoComando} from "./coleta-motor.js";

const CAMINHO_SGC = path.join(DIRETORIO_TOOLKIT, "sgc.ts");

async function executarComando({comando, args, cwd, env}: OpcoesComando): Promise<ResultadoComando> {
    const inicio = Date.now();
    try {
        const resultado = await execa(comando, args, {
            cwd,
            env: {...process.env, ...env},
            shell: process.platform === "win32",
            reject: false
        });
        return {
            codigoSaida: resultado.exitCode ?? -1,
            saida: resultado.stdout,
            erro: resultado.stderr || (resultado.failed ? resultado.shortMessage ?? "" : ""),
            duracaoMs: Date.now() - inicio
        };
    } catch (erro: unknown) {
        return {
            codigoSaida: -1,
            saida: "",
            erro: erro instanceof Error ? erro.message : String(erro),
            duracaoMs: Date.now() - inicio
        };
    }
}

async function executarComandoSgc(
    contexto: ContextoColeta,
    argumentos: string[],
    incluirBase = true
): Promise<ResultadoComando> {
    return executarComando({
        comando: resolverCaminhoTsx(),
        args: [CAMINHO_SGC, ...argumentos, ...(incluirBase ? ["--base", contexto.base] : [])],
        cwd: contexto.base,
    });
}

export {executarComando, executarComandoSgc};
