import {access, readFile} from "node:fs/promises";
import path from "node:path";
import {globby} from "globby";
import {resolverNaRaiz} from "./caminhos.js";
import {resolverCaminhoConfigurado} from "./configuracao.js";

type FotografiaQualidade = Record<string, unknown>;

interface ResultadoFotografiaQualidade {
    caminho: string;
    fotografia: FotografiaQualidade;
}

const NOME_ARQUIVO_FOTOGRAFIA = "fotografia.json";

function obterDiretorioArtefatos(diretorioBase = resolverNaRaiz()): string {
    return resolverCaminhoConfigurado("artefatosQualidade", diretorioBase);
}

function obterCaminhoUltimaFotografia(diretorioBase = resolverNaRaiz()): string {
    return path.join(obterDiretorioArtefatos(diretorioBase), "mais-recente", NOME_ARQUIVO_FOTOGRAFIA);
}

async function lerFotografia(caminho: string): Promise<FotografiaQualidade> {
    return JSON.parse(await readFile(caminho, "utf8")) as FotografiaQualidade;
}

async function existeArquivo(caminho: string): Promise<boolean> {
    try {
        await access(caminho);
        return true;
    } catch {
        return false;
    }
}

async function resolverFotografiaQualidade(
    caminhoInformado: string | null = null,
    diretorioBase = resolverNaRaiz()
): Promise<ResultadoFotografiaQualidade> {
    if (caminhoInformado) {
        const caminhoAbsoluto = path.isAbsolute(caminhoInformado)
            ? caminhoInformado
            : path.resolve(diretorioBase, caminhoInformado);
        return {
            caminho: caminhoAbsoluto,
            fotografia: await lerFotografia(caminhoAbsoluto)
        };
    }

    const caminhoUltimaFotografia = obterCaminhoUltimaFotografia(diretorioBase);
    if (await existeArquivo(caminhoUltimaFotografia)) {
        return {
            caminho: caminhoUltimaFotografia,
            fotografia: await lerFotografia(caminhoUltimaFotografia)
        };
    }

    const candidatos = await globby(path.join(obterDiretorioArtefatos(diretorioBase), "execucoes", "**", NOME_ARQUIVO_FOTOGRAFIA), {
        cwd: diretorioBase,
        absolute: true,
        onlyFiles: true
    });

    const maisRecente = candidatos.toSorted((a, b) => b.localeCompare(a))[0];
    if (!maisRecente) {
        throw new Error("Nenhuma fotografia de qualidade foi encontrada. Execute `npx tsx toolkit/sgc.js qualidade coletar --perfil rapido`.");
    }

    return {
        caminho: maisRecente,
        fotografia: await lerFotografia(maisRecente)
    };
}

export {
    NOME_ARQUIVO_FOTOGRAFIA,
    obterCaminhoUltimaFotografia,
    obterDiretorioArtefatos,
    resolverFotografiaQualidade
};
