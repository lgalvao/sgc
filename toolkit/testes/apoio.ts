import path from "node:path";
import {
    access as verificarAcesso,
    chmod as alterarModo,
    cp as copiarRecursivamente,
    mkdir as criarPasta,
    readFile as lerEntrada,
    writeFile as escreverSaida
} from "node:fs/promises";
import {execa, type Options} from "execa";

const DIRETORIO_RAIZ = path.resolve(import.meta.dirname, "..", "..");
const CAMINHO_FERRAMENTAS = path.join(DIRETORIO_RAIZ, "toolkit", "ferramentas.ts");
const CAMINHO_TSX = path.join(DIRETORIO_RAIZ, "node_modules", ".bin", process.platform === "win32" ? "tsx.cmd" : "tsx");

interface ResultadoExecucao {
    exitCode?: number;
    stdout: string;
    stderr: string;
}

async function executarSgc(args: string[], opcoes: Options = {}): Promise<ResultadoExecucao> {
    const resultado = await execa(CAMINHO_TSX, [CAMINHO_FERRAMENTAS, ...args], {
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

async function criarDiretorio(caminho: string): Promise<void> {
    await criarPasta(caminho, {recursive: true});
}

async function escreverArquivo(caminho: string, conteudo: string | Uint8Array): Promise<void> {
    await criarDiretorio(path.dirname(caminho));
    await escreverSaida(caminho, conteudo);
}

async function escreverJson(caminho: string, valor: unknown): Promise<void> {
    const conteudo = JSON.stringify(valor, null, 2);
    if (conteudo === undefined) {
        throw new TypeError("Não foi possível serializar o valor como JSON");
    }
    await escreverArquivo(caminho, `${conteudo}\n`);
}

async function lerArquivo(caminho: string, codificacao: BufferEncoding = "utf8"): Promise<string> {
    return lerEntrada(caminho, {encoding: codificacao});
}

async function lerJson<T = unknown>(caminho: string): Promise<T> {
    return JSON.parse(await lerArquivo(caminho)) as T;
}

async function existe(caminho: string): Promise<boolean> {
    try {
        await verificarAcesso(caminho);
        return true;
    } catch {
        return false;
    }
}

async function copiar(caminhoOrigem: string, caminhoDestino: string): Promise<void> {
    await criarDiretorio(path.dirname(caminhoDestino));
    await copiarRecursivamente(caminhoOrigem, caminhoDestino, {recursive: true, force: true});
}

async function alterarPermissoes(caminho: string, modo: number): Promise<void> {
    await alterarModo(caminho, modo);
}

export {
    DIRETORIO_RAIZ,
    CAMINHO_FERRAMENTAS,
    CAMINHO_TSX,
    executarSgc,
    criarDiretorio,
    escreverArquivo,
    escreverJson,
    lerArquivo,
    lerJson,
    existe,
    copiar,
    alterarPermissoes
};

export type {ResultadoExecucao};
