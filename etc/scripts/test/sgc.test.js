import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import fs from "fs-extra";
import {describe, expect, test} from "vitest";
import {execaNode} from "execa";

const DIRETORIO_RAIZ = path.resolve(import.meta.dirname, "..", "..", "..");
const CAMINHO_SGC = path.join(DIRETORIO_RAIZ, "etc", "scripts", "sgc.js");
const FIXTURE_SNAPSHOT = path.join(DIRETORIO_RAIZ, "etc", "scripts", "test", "fixtures", "qa", "snapshot.json");

async function executarSgc(args, opcoes = {}) {
    return execaNode(CAMINHO_SGC, args, {
        cwd: DIRETORIO_RAIZ,
        reject: false,
        ...opcoes
    });
}

describe("CLI raiz do toolkit", () => {
    test("exibe a ajuda principal", async () => {
        const resultado = await executarSgc(["--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Toolkit do SGC");
        expect(resultado.stdout).toContain("projeto doctor");
    });

    test("despacha ajuda de um comando legado do backend", async () => {
        const resultado = await executarSgc(["backend", "cobertura", "verificar", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Consulta cobertura global e por classe.");
    });

    test("resume um snapshot de QA a partir de fixture", async () => {
        const resultado = await executarSgc(["qa", "resumo", "--json", "--arquivo", FIXTURE_SNAPSHOT]);
        expect(resultado.exitCode).toBe(0);

        const json = JSON.parse(resultado.stdout);
        expect(json.resumo.statusGeral).toBe("verde");
        expect(json.hotspots).toHaveLength(2);
    });

    test("despacha ajuda de um comando migrado do frontend", async () => {
        const resultado = await executarSgc(["frontend", "mensagens", "extrair", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Extrai mensagens do projeto.");
    });

    test("despacha ajuda do servidor do qa dashboard", async () => {
        const resultado = await executarSgc(["qa", "dashboard", "servir", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Serve o dashboard de QA localmente.");
    });

    test("executa o doctor em JSON", async () => {
        const resultado = await executarSgc(["projeto", "doctor", "--json"]);
        expect(resultado.exitCode).toBe(0);

        const json = JSON.parse(resultado.stdout);
        expect(["ok", "alerta"]).toContain(json.statusGeral);
        expect(Array.isArray(json.verificacoes)).toBe(true);
        expect(json.verificacoes.some((item) => item.nome === "node")).toBe(true);
    });

    test("simula e executa limpeza em diretório temporário", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-scripts-"));
        await fs.ensureDir(path.join(diretorioBase, "backend", "build"));
        await fs.ensureDir(path.join(diretorioBase, "etc", "qa-dashboard", "latest"));
        await fs.outputFile(path.join(diretorioBase, "plano-100-cobertura.md"), "# teste");
        await fs.outputFile(path.join(diretorioBase, "etc", "qa-dashboard", "latest", "ultimo-resumo.md"), "ok");

        const previa = await executarSgc(["projeto", "limpar", "--json", "--base", diretorioBase]);
        expect(previa.exitCode).toBe(0);
        const jsonPrevia = JSON.parse(previa.stdout);
        expect(jsonPrevia.modo).toBe("simular");
        expect(jsonPrevia.itens).toContain("backend/build");
        expect(await fs.pathExists(path.join(diretorioBase, "backend", "build"))).toBe(true);

        const execucao = await executarSgc(["projeto", "limpar", "--json", "--confirmar", "--base", diretorioBase]);
        expect(execucao.exitCode).toBe(0);
        const jsonExecucao = JSON.parse(execucao.stdout);
        expect(jsonExecucao.modo).toBe("executar");
        expect(await fs.pathExists(path.join(diretorioBase, "backend", "build"))).toBe(false);
        expect(await fs.pathExists(path.join(diretorioBase, "plano-100-cobertura.md"))).toBe(false);
    });
});
