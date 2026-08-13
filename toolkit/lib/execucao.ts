import {existsSync} from "node:fs";
import path from "node:path";
import {fileURLToPath, pathToFileURL} from "node:url";
import {execa} from "execa";
import {DIRETORIO_RAIZ} from "./caminhos.js";

const DIRETORIO_EXECUCAO_TOOLKIT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const NOME_EXECUTAVEL_TSX = process.platform === "win32" ? "tsx.cmd" : "tsx";
const CAMINHO_TSX = [
    path.join(DIRETORIO_EXECUCAO_TOOLKIT, "node_modules", ".bin", NOME_EXECUTAVEL_TSX),
    path.join(DIRETORIO_RAIZ, "node_modules", ".bin", NOME_EXECUTAVEL_TSX)
].find(caminho => existsSync(caminho)) ?? NOME_EXECUTAVEL_TSX;

function ehEntradaPrincipal(urlModulo: string): boolean {
    return Boolean(process.argv[1] && urlModulo === pathToFileURL(process.argv[1]).href);
}

function garantirArquivo(relativo: string): string {
    const caminhoRelativo = relativo.startsWith("toolkit/")
        ? relativo.slice("toolkit/".length)
        : relativo;
    const absoluto = path.resolve(DIRETORIO_EXECUCAO_TOOLKIT, caminhoRelativo);
    if (!existsSync(absoluto)) {
        throw new Error(`Script nao encontrado: ${relativo}`);
    }
    return absoluto;
}

async function executarNode(relativo: string, argumentos: string[] = []) {
    const script = garantirArquivo(relativo);
    return execa(CAMINHO_TSX, [script, ...argumentos], {
        cwd: DIRETORIO_RAIZ,
        stdio: "inherit"
    });
}

export {
    CAMINHO_TSX,
    ehEntradaPrincipal,
    executarNode,
    garantirArquivo
};
