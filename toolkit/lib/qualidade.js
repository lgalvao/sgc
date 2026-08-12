import path from "node:path";
import {globby} from "globby";
import fs from "fs-extra";
import {resolverNaRaiz} from "./caminhos.js";
import {resolverCaminhoConfigurado} from "./configuracao.js";

const NOME_ARQUIVO_FOTOGRAFIA = "fotografia.json";

function obterDiretorioArtefatos(diretorioBase = resolverNaRaiz()) {
    return resolverCaminhoConfigurado("artefatosQualidade", diretorioBase);
}

function obterCaminhoUltimaFotografia(diretorioBase = resolverNaRaiz()) {
    return path.join(obterDiretorioArtefatos(diretorioBase), "mais-recente", NOME_ARQUIVO_FOTOGRAFIA);
}

async function resolverFotografiaQualidade(caminhoInformado = null, diretorioBase = resolverNaRaiz()) {
    if (caminhoInformado) {
        const caminhoAbsoluto = path.isAbsolute(caminhoInformado)
            ? caminhoInformado
            : path.resolve(diretorioBase, caminhoInformado);
        return {
            caminho: caminhoAbsoluto,
            fotografia: await fs.readJson(caminhoAbsoluto)
        };
    }

    const caminhoUltimaFotografia = obterCaminhoUltimaFotografia(diretorioBase);
    if (await fs.pathExists(caminhoUltimaFotografia)) {
        return {
            caminho: caminhoUltimaFotografia,
            fotografia: await fs.readJson(caminhoUltimaFotografia)
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
        fotografia: await fs.readJson(maisRecente)
    };
}

export {
    NOME_ARQUIVO_FOTOGRAFIA,
    obterCaminhoUltimaFotografia,
    obterDiretorioArtefatos,
    resolverFotografiaQualidade
};
