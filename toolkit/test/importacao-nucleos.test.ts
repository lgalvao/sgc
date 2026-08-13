import path from "node:path";
import {pathToFileURL} from "node:url";
import {describe, expect, test} from "vitest";
import {execa} from "execa";
import {DIRETORIO_RAIZ} from "./apoio.js";

const DIRETORIO_TOOLKIT = path.join(DIRETORIO_RAIZ, "toolkit");
const CAMINHOS_COMANDOS_PROJETO = [
    "arvore-linhas.ts",
    "dependencias-auditar.ts",
    "diagnostico.ts",
    "limpar.ts",
    "preparar.ts",
    "qualidade.ts",
    "versao-sincronizar.ts"
].map(nome => path.join(DIRETORIO_TOOLKIT, "projeto", nome));
const CAMINHOS_COMANDOS_QUALIDADE = [
    "coleta.ts",
    "coleta-execucao.ts",
    "resumo.ts"
].map(nome => path.join(DIRETORIO_TOOLKIT, "qualidade", nome));
const CAMINHOS_COMANDOS_CONSISTENCIA = [
    "nomes-simbolos-coletar.ts",
    "nomes-consistencia-auditar.ts",
    "idioma-consistencia-auditar.ts"
].map(nome => path.join(DIRETORIO_TOOLKIT, "codigo", nome));

async function importarSemExecutar(caminho: string): Promise<{exitCode?: number; stdout: string}> {
    const urlModulo = pathToFileURL(caminho).href;
    const resultado = await execa(process.execPath, [
        "--import=tsx",
        "--input-type=module",
        "-e",
        `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
    ], {
        cwd: DIRETORIO_RAIZ,
        reject: false
    });
    return {
        exitCode: resultado.exitCode,
        stdout: resultado.stdout
    };
}

function todasImportacoesConcluidas(resultados: Array<{exitCode?: number; stdout: string}>): boolean {
    return resultados.every(resultado => resultado.exitCode === 0 && resultado.stdout === "importacao-ok");
}

describe("Importação segura dos núcleos do toolkit", () => {
    test("pode importar comandos de projeto sem executar efeitos", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_PROJETO.map(importarSemExecutar));
        expect(todasImportacoesConcluidas(resultados)).toBe(true);
    });

    test("pode importar comandos de qualidade sem executar coleta ou resumo", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_QUALIDADE.map(importarSemExecutar));
        expect(todasImportacoesConcluidas(resultados)).toBe(true);
    });

    test("pode importar auditores de consistencia sem gerar artefatos", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_CONSISTENCIA.map(importarSemExecutar));
        expect(todasImportacoesConcluidas(resultados)).toBe(true);
    });
});
