import path from "node:path";
import {execa} from "execa";
import {describe, expect, test} from "vitest";
import {
    DIRETORIO_RAIZ,
    CAMINHO_TSX,
    executarSgc,
    existe
} from "./apoio.js";

const CAMINHO_FRONTEND_COBERTURA_AUDITORIA = path.join(
    DIRETORIO_RAIZ,
    "toolkit",
    "frontend",
    "cobertura-auditoria.ts"
);
const DIRETORIO_SCRIPTS_BACKEND_LEGADO = path.join(DIRETORIO_RAIZ, "backend", "etc", "scripts");
const DIRETORIO_SCRIPTS_FRONTEND_LEGADO = path.join(DIRETORIO_RAIZ, "frontend", "etc", "scripts");

async function executarAjudaFrontendCobertura(): Promise<{exitCode?: number; stdout: string}> {
    const resultado = await execa(CAMINHO_TSX, [CAMINHO_FRONTEND_COBERTURA_AUDITORIA, "--help"], {
        cwd: DIRETORIO_RAIZ,
        reject: false
    });
    return {
        exitCode: resultado.exitCode,
        stdout: String(resultado.stdout)
    };
}

describe("Superfície da CLI", () => {
    test("exibe a ajuda principal", async () => {
        const resultado = await executarSgc(["--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Toolkit do SGC");
        expect(resultado.stdout).toContain("projeto diagnostico");
    });

    test("despacha ajuda de um comando de auditoria do backend", async () => {
        const resultado = await executarSgc(["backend", "cobertura", "auditoria", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Auditoria unificada de cobertura e risco (Backend).");
        expect(resultado.stdout).toContain("--minimo <percentual>");
    });

    test("despacha ajuda da auditoria de assuntos de notificacao do backend", async () => {
        const resultado = await executarSgc(["backend", "notificacoes", "auditar-assuntos", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Audita literais de assunto de notificação fora de AssuntosNotificacao.");
    });

    test("despacha ajuda da validacao de modais do frontend", async () => {
        const resultado = await executarSgc(["frontend", "modais", "validar", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("ModalPadrao");
        expect(resultado.stdout).toContain("BModal diretamente no frontend");
    });

    test("exibe ajuda padronizada no script frontend cobertura auditoria", async () => {
        const resultado = await executarAjudaFrontendCobertura();
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Auditoria unificada de cobertura e risco (Frontend).");
        expect(resultado.stdout).toContain("--minimo <percentual>");
    });

    test("nao possui diretorios legados de scripts em backend/frontend", async () => {
        expect(await existe(DIRETORIO_SCRIPTS_BACKEND_LEGADO)).toBe(false);
        expect(await existe(DIRETORIO_SCRIPTS_FRONTEND_LEGADO)).toBe(false);
    });
});
