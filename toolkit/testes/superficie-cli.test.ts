import path from "node:path";
import {execa} from "execa";
import {describe, expect, test} from "vitest";
import {
    DIRETORIO_RAIZ,
    CAMINHO_TSX,
    executarSgc,
    existe
} from "./apoio.js";

const CAMINHO_CLIENTE_COBERTURA_AUDITORIA = path.join(
    DIRETORIO_RAIZ,
    "toolkit",
    "cliente",
    "cobertura-auditoria.ts"
);
const DIRETORIO_SCRIPTS_SERVIDOR_LEGADO = path.join(DIRETORIO_RAIZ, "backend", "etc", "scripts");
const DIRETORIO_SCRIPTS_CLIENTE_LEGADO = path.join(DIRETORIO_RAIZ, "frontend", "etc", "scripts");
const CAMINHO_INVENTARIO_SIMBOLOS = path.join(DIRETORIO_RAIZ, "toolkit", "codigo", "nomes-simbolos-coletar.ts");
const CAMINHO_COLETA_QUALIDADE = path.join(DIRETORIO_RAIZ, "toolkit", "qualidade", "coleta.ts");

async function executarAjudaClienteCobertura(): Promise<{exitCode?: number; stdout: string}> {
    const resultado = await execa(CAMINHO_TSX, [CAMINHO_CLIENTE_COBERTURA_AUDITORIA, "--help"], {
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
        expect(resultado.stdout).toContain("Toolkit de ferramentas de projeto");
        expect(resultado.stdout).toContain("projeto ambiente");
    });

    test("despacha ajuda de um comando de auditoria do servidor", async () => {
        const resultado = await executarSgc(["servidor", "cobertura", "auditoria", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Auditoria unificada de cobertura e risco do servidor segundo as exclusoes do perfil SGC.");
        expect(resultado.stdout).toContain("--minimo <percentual>");
    });

    test("despacha ajuda da auditoria de assuntos de notificacao do servidor", async () => {
        const resultado = await executarSgc(["servidor", "notificacoes", "auditar-assuntos", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Audita literais de assunto de notificação fora de AssuntosNotificacao.");
    });

    test("despacha ajuda da validacao de modais do cliente", async () => {
        const resultado = await executarSgc(["cliente", "modais", "validar", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("ModalPadrao");
        expect(resultado.stdout).toContain("BModal diretamente no cliente");
    });

    test("falha antes de executar o script quando recebe uma opção desconhecida", async () => {
        const resultado = await executarSgc(["servidor", "arquitetura", "auditar", "--opcao-inexistente"]);
        expect(resultado.exitCode).toBe(1);
        expect(`${resultado.stdout}\n${resultado.stderr}`).toContain("Opção desconhecida");
    });

    test("rejeita posicionais extras em comando sem argumentos posicionais", async () => {
        const resultado = await executarSgc(["cliente", "modais", "validar", "extra"]);
        expect(resultado.exitCode).toBe(1);
        expect(`${resultado.stdout}\n${resultado.stderr}`).toContain("Quantidade de argumentos posicionais inválida");
    });

    test("rejeita opções desconhecidas no comando especial de coleta", async () => {
        const resultado = await executarSgc(["qualidade", "coletar", "--opcao-inexistente"]);
        expect(resultado.exitCode).not.toBe(0);
        expect(`${resultado.stdout}\n${resultado.stderr}`).toContain("unknown option");
    });

    test("aplica o mesmo contrato ao entrypoint direto catalogado", async () => {
        const resultado = await execa(CAMINHO_TSX, [CAMINHO_INVENTARIO_SIMBOLOS, "--opcao-inexistente"], {
            cwd: DIRETORIO_RAIZ,
            reject: false,
        });

        expect(resultado.exitCode).not.toBe(0);
        expect(`${resultado.stdout}\n${resultado.stderr}`).toContain("Opção desconhecida");
    });

    test("normaliza atribuição de opção no entrypoint direto", async () => {
        const resultado = await execa(CAMINHO_TSX, [CAMINHO_INVENTARIO_SIMBOLOS, "--help", "--base=/tmp/projeto"], {
            cwd: DIRETORIO_RAIZ,
            reject: false,
        });

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Gera inventario completo");
    });

    test("aplica o contrato ao entrypoint direto de coleta de qualidade", async () => {
        const resultado = await execa(CAMINHO_TSX, [CAMINHO_COLETA_QUALIDADE, "--opcao-inexistente"], {
            cwd: DIRETORIO_RAIZ,
            reject: false,
        });

        expect(resultado.exitCode).not.toBe(0);
        expect(`${resultado.stdout}\n${resultado.stderr}`).toContain("Opção desconhecida");
    });

    test("exibe ajuda padronizada no script cliente cobertura auditoria", async () => {
        const resultado = await executarAjudaClienteCobertura();
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Auditoria unificada de cobertura e risco (Cliente).");
        expect(resultado.stdout).toContain("--minimo <percentual>");
    });

    test("nao possui diretorios legados de scripts do servidor e do cliente", async () => {
        expect(await existe(DIRETORIO_SCRIPTS_SERVIDOR_LEGADO)).toBe(false);
        expect(await existe(DIRETORIO_SCRIPTS_CLIENTE_LEGADO)).toBe(false);
    });
});
