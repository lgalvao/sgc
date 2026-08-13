import path from "node:path";
import {pathToFileURL} from "node:url";
import {describe, expect, test} from "vitest";
import {execa} from "execa";
import {DIRETORIO_RAIZ} from "./apoio.js";

const DIRETORIO_TOOLKIT = path.join(DIRETORIO_RAIZ, "toolkit");
const CAMINHO_SEMGREP_AUDITAR = path.join(DIRETORIO_TOOLKIT, "codigo", "semgrep-auditar.ts");
const CAMINHO_CHEIROS_AUDITAR = path.join(DIRETORIO_TOOLKIT, "codigo", "cheiros-auditar.ts");

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

describe("Importação segura dos auditores de código", () => {
    test("pode importar o auditor Semgrep sem executar a ferramenta externa", async () => {
        const resultado = await importarSemExecutar(CAMINHO_SEMGREP_AUDITAR);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toBe("importacao-ok");
    });

    test("pode importar o auditor de cheiros sem ler o projeto", async () => {
        const resultado = await importarSemExecutar(CAMINHO_CHEIROS_AUDITAR);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toBe("importacao-ok");
    });
});
