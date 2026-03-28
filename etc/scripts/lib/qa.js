import path from "node:path";
import {pathToFileURL} from "node:url";
import {globby} from "globby";
import fs from "fs-extra";
import {resolverNaRaiz} from "./caminhos.js";

const CAMINHO_SNAPSHOT_LATEST = resolverNaRaiz("etc", "qa-dashboard", "latest", "ultimo-snapshot.json");

async function resolverSnapshotQa(caminhoInformado = null) {
    if (caminhoInformado) {
        const caminhoAbsoluto = path.isAbsolute(caminhoInformado)
            ? caminhoInformado
            : resolverNaRaiz(caminhoInformado);
        return {
            caminho: caminhoAbsoluto,
            snapshot: await fs.readJson(caminhoAbsoluto)
        };
    }

    if (await fs.pathExists(CAMINHO_SNAPSHOT_LATEST)) {
        return {
            caminho: CAMINHO_SNAPSHOT_LATEST,
            snapshot: await fs.readJson(CAMINHO_SNAPSHOT_LATEST)
        };
    }

    const candidatos = await globby("etc/qa-dashboard/runs/**/snapshot.json", {
        cwd: resolverNaRaiz(),
        absolute: true,
        onlyFiles: true
    });

    const maisRecente = candidatos.sort((a, b) => b.localeCompare(a))[0];
    if (!maisRecente) {
        throw new Error("Nenhum snapshot de QA foi encontrado. Execute `node etc/scripts/sgc.js qa snapshot coletar --perfil rapido`.");
    }

    return {
        caminho: maisRecente,
        snapshot: await fs.readJson(maisRecente)
    };
}

async function carregarModuloQaDashboard() {
    const caminho = resolverNaRaiz("etc", "qa-dashboard", "dashboard.html");
    return {
        caminhoArquivo: caminho,
        urlArquivo: pathToFileURL(caminho).href
    };
}

export {
    carregarModuloQaDashboard,
    resolverSnapshotQa
};
