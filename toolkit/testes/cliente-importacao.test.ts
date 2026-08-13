import path from "node:path";
import {pathToFileURL} from "node:url";
import {describe, expect, test} from "vitest";
import {execa} from "execa";
import {DIRETORIO_RAIZ} from "./apoio.js";

const DIRETORIO_TOOLKIT = path.join(DIRETORIO_RAIZ, "toolkit");
const CAMINHOS_COMANDOS_ESTRUTURA_CLIENTE = [
    "arquitetura-auditar.ts",
    "arquitetura-validar.ts",
    "modais-validar.ts",
    "residuos-auditar.ts",
    "residuos-validar.ts",
    "identificadores-teste-listar.ts",
    "identificadores-teste-listar-duplicados.ts",
    "views-templates-validar.ts"
].map(nome => path.join(DIRETORIO_TOOLKIT, "cliente", nome));
const CAMINHOS_COMANDOS_COBERTURA_CLIENTE = [
    "cobertura-auditoria.ts",
    "cobertura-ramificacoes.ts",
    "cobertura-ramificacoes-erros.ts"
].map(nome => path.join(DIRETORIO_TOOLKIT, "cliente", nome));
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

describe("Importação segura dos comandos do cliente", () => {
    test("pode importar auditores estruturais do cliente sem auditar o projeto", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_ESTRUTURA_CLIENTE.map(importarSemExecutar));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("pode importar comandos de cobertura do cliente sem ler o relatorio V8", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_COBERTURA_CLIENTE.map(importarSemExecutar));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

});
