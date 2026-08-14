import path from "node:path";
import {describe, expect, test} from "vitest";
import {DIRETORIO_RAIZ, importarModuloSemExecutar} from "./apoio.js";

const DIRETORIO_TOOLKIT = path.join(DIRETORIO_RAIZ, "toolkit");
const CAMINHO_SEMGREP_AUDITAR = path.join(DIRETORIO_TOOLKIT, "codigo", "semgrep-auditar.ts");
const CAMINHO_CHEIROS_AUDITAR = path.join(DIRETORIO_TOOLKIT, "codigo", "cheiros-auditar.ts");

describe("Importação segura dos auditores de código", () => {
    test("pode importar o auditor Semgrep sem executar a ferramenta externa", async () => {
        const resultado = await importarModuloSemExecutar(CAMINHO_SEMGREP_AUDITAR);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toBe("importacao-ok");
    });

    test("pode importar o auditor de cheiros sem ler o projeto", async () => {
        const resultado = await importarModuloSemExecutar(CAMINHO_CHEIROS_AUDITAR);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toBe("importacao-ok");
    });
});
