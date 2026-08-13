import path from "node:path";
import {pathToFileURL} from "node:url";
import {describe, expect, test} from "vitest";
import {execa} from "execa";
import {DIRETORIO_RAIZ} from "./apoio.js";

const DIRETORIO_TOOLKIT = path.join(DIRETORIO_RAIZ, "toolkit");
const CAMINHOS_COMANDOS_ACESSIBILIDADE = [
    "acessibilidade-crawler.ts",
    "acessibilidade-processar-resultados.ts"
].map(nome => path.join(DIRETORIO_TOOLKIT, "e2e", nome));

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

describe("Importação segura dos comandos E2E", () => {
    test("pode importar comandos de acessibilidade sem executar o crawler ou ler resultados", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_ACESSIBILIDADE.map(importarSemExecutar));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });
});
