import {existsSync} from "node:fs";
import path from "node:path";
import {pathToFileURL} from "node:url";
import {fileURLToPath} from "node:url";
import {execa} from "execa";
import {DIRETORIO_RAIZ, DIRETORIO_TOOLKIT} from "./caminhos.js";
import {obterDefinicaoComandoArquivo} from "./catalogo-comandos.js";
import {validarArgumentos} from "./cli-opcoes.js";

const NOME_EXECUTAVEL_TSX = process.platform === "win32" ? "tsx.cmd" : "tsx";

function resolverCaminhoTsx(): string {
    return [
        path.join(DIRETORIO_TOOLKIT, "node_modules", ".bin", NOME_EXECUTAVEL_TSX),
        path.join(DIRETORIO_TOOLKIT, "..", "node_modules", ".bin", NOME_EXECUTAVEL_TSX),
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
    const caminhoFonte = path.resolve(DIRETORIO_TOOLKIT, caminhoRelativo);
    if (existsSync(caminhoFonte)) {
        return caminhoFonte;
    }

    throw new Error(`Script TypeScript nao encontrado: ${relativo}`);
}

function validarArgumentosEntradaDireta(urlModulo: string, argumentos: readonly string[] = process.argv.slice(2)): string[] {
    const arquivo = path.relative(DIRETORIO_TOOLKIT, fileURLToPath(urlModulo)).replaceAll(path.sep, "/");
    const definicao = obterDefinicaoComandoArquivo(arquivo);
    return definicao ? validarArgumentos(argumentos, definicao.argumentos) : [...argumentos];
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
    resolverCaminhoTsx,
    validarArgumentosEntradaDireta
};
