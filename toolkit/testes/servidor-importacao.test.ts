import path from "node:path";
import {describe, expect, test} from "vitest";
import {DIRETORIO_RAIZ, importarModuloSemExecutar} from "./apoio.js";

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

describe("Importação segura dos comandos do servidor", () => {
    test("pode importar comandos de cobertura do servidor sem ler JaCoCo", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_COBERTURA_SERVIDOR.map(importarModuloSemExecutar));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("pode importar auditores estruturais do servidor sem analisar o projeto", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_AUDITORIA_SERVIDOR.map(importarModuloSemExecutar));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("pode importar o corretor de FQN sem alterar arquivos", async () => {
        const resultado = await importarModuloSemExecutar(CAMINHO_CORRIGIR_FQN);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toBe("importacao-ok");
    });

    test("pode importar comandos de analise de testes sem ler relatorios", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_TESTES_SERVIDOR.map(importarModuloSemExecutar));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("pode importar auditoria de assuntos sem ler o servidor", async () => {
        const resultado = await importarModuloSemExecutar(CAMINHO_AUDITORIA_ASSUNTOS);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toBe("importacao-ok");
    });
});
