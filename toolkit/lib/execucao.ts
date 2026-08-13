import {existsSync} from "node:fs";
import path from "node:path";
import {fileURLToPath, pathToFileURL} from "node:url";
import {execa} from "execa";
import {DIRETORIO_RAIZ} from "./caminhos.js";

const DIRETORIO_EXECUCAO_TOOLKIT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const NOME_EXECUTAVEL_TSX = process.platform === "win32" ? "tsx.cmd" : "tsx";

function resolverCaminhoTsx(): string {
    return [
        path.join(DIRETORIO_EXECUCAO_TOOLKIT, "node_modules", ".bin", NOME_EXECUTAVEL_TSX),
        path.join(DIRETORIO_RAIZ, "node_modules", ".bin", NOME_EXECUTAVEL_TSX)
    ].find(caminho => existsSync(caminho)) ?? NOME_EXECUTAVEL_TSX;
}

function ehEntradaPrincipal(urlModulo: string): boolean {
    return Boolean(process.argv[1] && urlModulo === pathToFileURL(process.argv[1]).href);
}

function garantirArquivo(relativo: string): string {
    const caminhoRelativo = relativo.startsWith("toolkit/")
        ? relativo.slice("toolkit/".length)
        : relativo;
    const caminhoFonte = path.resolve(DIRETORIO_EXECUCAO_TOOLKIT, caminhoRelativo);
    if (existsSync(caminhoFonte)) {
        return caminhoFonte;
    }

    const caminhoCompilado = caminhoFonte.endsWith(".ts")
        ? `${caminhoFonte.slice(0, -3)}.js`
        : null;
    if (!caminhoCompilado || !existsSync(caminhoCompilado)) {
        throw new Error(`Script nao encontrado: ${relativo}`);
    }
    return caminhoCompilado;
}

async function executarNode(relativo: string, argumentos: string[] = []) {
    const script = garantirArquivo(relativo);
    return execa(resolverCaminhoTsx(), [script, ...argumentos], {
        cwd: DIRETORIO_RAIZ,
        stdio: "inherit"
    });
}

export {
    ehEntradaPrincipal,
    executarNode,
    garantirArquivo,
    resolverCaminhoTsx
};
