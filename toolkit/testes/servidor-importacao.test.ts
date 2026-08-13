import path from "node:path";
import {pathToFileURL} from "node:url";
import {describe, expect, test} from "vitest";
import {execa} from "execa";
import {DIRETORIO_RAIZ} from "./apoio.js";

const DIRETORIO_TOOLKIT = path.join(DIRETORIO_RAIZ, "toolkit");
const CAMINHOS_COMANDOS_TESTES_SERVIDOR = [
    "testes-analisar.ts",
    "testes-priorizar.ts"
].map(nome => path.join(DIRETORIO_TOOLKIT, "servidor", nome));
const CAMINHOS_COMANDOS_COBERTURA_SERVIDOR = [
    "cobertura-ramificacoes.ts",
    "cobertura-auditoria.ts"
].map(nome => path.join(DIRETORIO_TOOLKIT, "servidor", nome));
const CAMINHOS_COMANDOS_AUDITORIA_SERVIDOR = [
    "arquitetura-auditar.ts",
    "coesao-auditar.ts",
    "contratos-auditar.ts"
].map(nome => path.join(DIRETORIO_TOOLKIT, "servidor", nome));
const CAMINHO_CORRIGIR_FQN = path.join(DIRETORIO_TOOLKIT, "servidor", "java-corrigir-fqn.ts");
const CAMINHO_AUDITORIA_ASSUNTOS = path.join(DIRETORIO_TOOLKIT, "servidor", "notificacoes-assuntos-auditar.ts");

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

describe("Importação segura dos comandos do servidor", () => {
    test("pode importar comandos de cobertura do servidor sem ler JaCoCo", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_COBERTURA_SERVIDOR.map(importarSemExecutar));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("pode importar auditores estruturais do servidor sem analisar o projeto", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_AUDITORIA_SERVIDOR.map(importarSemExecutar));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("pode importar o corretor de FQN sem alterar arquivos", async () => {
        const resultado = await importarSemExecutar(CAMINHO_CORRIGIR_FQN);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toBe("importacao-ok");
    });

    test("pode importar comandos de analise de testes sem ler relatorios", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_TESTES_SERVIDOR.map(importarSemExecutar));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("pode importar auditoria de assuntos sem ler o servidor", async () => {
        const resultado = await importarSemExecutar(CAMINHO_AUDITORIA_ASSUNTOS);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toBe("importacao-ok");
    });
});
