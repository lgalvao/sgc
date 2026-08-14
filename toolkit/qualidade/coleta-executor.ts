import path from "node:path";
import process from "node:process";
import {existsSync} from "node:fs";
import {execa} from "execa";
import {DIRETORIO_RAIZ, DIRETORIO_TOOLKIT} from "../biblioteca/caminhos.js";
import {resolverCaminhoTsx} from "../biblioteca/execucao.js";
import type {ContextoColeta, OpcoesComando, ResultadoComando} from "./coleta-motor.js";

const CAMINHO_FERRAMENTAS = path.join(DIRETORIO_TOOLKIT, "ferramentas.ts");

function resolverExecutavelLocal(nome: string, diretorioBase: string): string {
    const nomeExecutavel = process.platform === "win32" ? `${nome}.cmd` : nome;
    const diretorioResolvido = path.resolve(diretorioBase);
    const candidatos = [
        path.join(diretorioResolvido, "node_modules", ".bin", nomeExecutavel),
        path.join(diretorioResolvido, "..", "node_modules", ".bin", nomeExecutavel),
        path.join(DIRETORIO_RAIZ, "node_modules", ".bin", nomeExecutavel)
    ];
    return candidatos.find(caminho => existsSync(caminho)) ?? nome;
}

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
        args: [CAMINHO_FERRAMENTAS, ...argumentos, ...(incluirBase ? ["--base", contexto.base] : [])],
        cwd: contexto.base,
    });
}

export {executarComando, executarComandoSgc, resolverExecutavelLocal};
