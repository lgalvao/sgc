import path from "node:path";
import {execa, type Options} from "execa";

const DIRETORIO_RAIZ = path.resolve(import.meta.dirname, "..", "..");
const CAMINHO_SGC = path.join(DIRETORIO_RAIZ, "toolkit", "sgc.ts");
const CAMINHO_TSX = path.join(DIRETORIO_RAIZ, "node_modules", ".bin", process.platform === "win32" ? "tsx.cmd" : "tsx");

interface ResultadoExecucao {
    exitCode?: number;
    stdout: string;
    stderr: string;
}

async function executarSgc(args: string[], opcoes: Options = {}): Promise<ResultadoExecucao> {
    const resultado = await execa(CAMINHO_TSX, [CAMINHO_SGC, ...args], {
        cwd: DIRETORIO_RAIZ,
        reject: false,
        ...opcoes
    });
    return {
        exitCode: resultado.exitCode,
        stdout: String(resultado.stdout),
        stderr: String(resultado.stderr)
    };
}

export {
    DIRETORIO_RAIZ,
    CAMINHO_SGC,
    CAMINHO_TSX,
    executarSgc
};

export type {ResultadoExecucao};
