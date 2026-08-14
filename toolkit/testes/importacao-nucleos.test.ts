import path from "node:path";
import {describe, expect, test} from "vitest";
import {DIRETORIO_RAIZ, importarModuloSemExecutar} from "./apoio.js";

const DIRETORIO_TOOLKIT = path.join(DIRETORIO_RAIZ, "toolkit");
const CAMINHOS_COMANDOS_PROJETO = [
    "arvore-linhas.ts",
    "dependencias-auditar.ts",
    "ambiente-verificar.ts",
    "artefatos-limpar.ts",
    "versao-sincronizar.ts"
].map(nome => path.join(DIRETORIO_TOOLKIT, "projeto", nome));
const CAMINHOS_COMANDOS_QUALIDADE = [
    "coleta.ts",
    "coleta-motor.ts",
    "coleta-execucao.ts",
    "tarefas-executar.ts",
    "resumo.ts"
].map(nome => path.join(DIRETORIO_TOOLKIT, "qualidade", nome));
const CAMINHOS_COMANDOS_CONSISTENCIA = [
    "nomes-simbolos-coletar.ts",
    "nomes-consistencia-auditar.ts",
    "idioma-consistencia-auditar.ts"
].map(nome => path.join(DIRETORIO_TOOLKIT, "codigo", nome));

function todasImportacoesConcluidas(resultados: Array<{exitCode?: number; stdout: string}>): boolean {
    return resultados.every(resultado => resultado.exitCode === 0 && resultado.stdout === "importacao-ok");
}

describe("Importação segura dos núcleos do toolkit", () => {
    test("pode importar comandos de projeto sem executar efeitos", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_PROJETO.map(importarModuloSemExecutar));
        expect(todasImportacoesConcluidas(resultados)).toBe(true);
    });

    test("pode importar comandos de qualidade sem executar coleta ou resumo", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_QUALIDADE.map(importarModuloSemExecutar));
        expect(todasImportacoesConcluidas(resultados)).toBe(true);
    });

    test("pode importar auditores de consistencia sem gerar artefatos", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_CONSISTENCIA.map(importarModuloSemExecutar));
        expect(todasImportacoesConcluidas(resultados)).toBe(true);
    });
});
