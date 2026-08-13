import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {
    executarSgc,
    escreverArquivo,
    escreverJson,
    existe
} from "./apoio.js";
import {VERSAO_CONFIGURACAO} from "../lib/configuracao.js";

describe("Auditorias de cobertura da CLI", () => {
    test("mantem auditorias de cobertura read-only e grava sob demanda", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cobertura-auditoria-"));
        const caminhoJacoco = path.join(base, "relatorios", "jacoco.xml");
        const caminhoV8 = path.join(base, "relatorios", "coverage-final.json");

        await escreverArquivo(caminhoJacoco, [
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<report name=\"exemplo\">",
            "  <counter type=\"INSTRUCTION\" missed=\"1\" covered=\"1\"/>",
            "  <counter type=\"LINE\" missed=\"1\" covered=\"1\"/>",
            "  <counter type=\"BRANCH\" missed=\"1\" covered=\"0\"/>",
            "  <counter type=\"COMPLEXITY\" missed=\"1\" covered=\"1\"/>",
            "  <counter type=\"METHOD\" missed=\"1\" covered=\"1\"/>",
            "  <package name=\"exemplo\">",
            "    <sourcefile name=\"Exemplo.java\">",
            "      <line nr=\"1\" mi=\"0\" ci=\"1\" mb=\"0\" cb=\"0\"/>",
            "      <line nr=\"2\" mi=\"1\" ci=\"0\" mb=\"1\" cb=\"0\"/>",
            "    </sourcefile>",
            "  </package>",
            "</report>",
            ""
        ].join("\n"));
        await escreverJson(caminhoV8, {
            [path.join(base, "frontend", "src", "Exemplo.ts")]: {
                s: {"1": 1, "2": 0},
                f: {"1": 1},
                b: {"1": [1, 0]},
                statementMap: {"1": {}, "2": {}}
            }
        });

        const backendLeitura = await executarSgc([
            "backend",
            "cobertura",
            "auditoria",
            "--json",
            "--base",
            base,
            "--arquivo",
            caminhoJacoco
        ]);
        const frontendLeitura = await executarSgc([
            "frontend",
            "cobertura",
            "auditoria",
            "--json",
            "--base",
            base,
            "--arquivo",
            caminhoV8
        ]);

        expect(backendLeitura.exitCode).toBe(0);
        expect(frontendLeitura.exitCode).toBe(0);
        expect(await existe(path.join(base, "backend-cobertura-auditoria.md"))).toBe(false);
        expect(await existe(path.join(base, "frontend-cobertura-auditoria.md"))).toBe(false);

        const backendGravacao = await executarSgc([
            "backend",
            "cobertura",
            "auditoria",
            "--json",
            "--gravar",
            "--base",
            base,
            "--arquivo",
            caminhoJacoco
        ]);
        const frontendGravacao = await executarSgc([
            "frontend",
            "cobertura",
            "auditoria",
            "--json",
            "--gravar",
            "--base",
            base,
            "--arquivo",
            caminhoV8
        ]);

        expect(backendGravacao.exitCode).toBe(0);
        expect(frontendGravacao.exitCode).toBe(0);
        expect(await existe(path.join(base, "backend-cobertura-auditoria.md"))).toBe(true);
        expect(await existe(path.join(base, "frontend-cobertura-auditoria.md"))).toBe(true);
    });

    test("analisa cobertura frontend a partir de base e relatorio V8 externos", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-cobertura-frontend-"));
        const caminhoArquivo = path.join(diretorioBase, "frontend", "src", "exemplo.ts");
        const caminhoRelatorio = path.join(diretorioBase, "coverage", "coverage-final.json");

        await escreverArquivo(caminhoArquivo, "export function exemplo() { return true; }\n");
        await escreverJson(caminhoRelatorio, {
            [caminhoArquivo]: {
                s: {"1": 0, "2": 1},
                f: {"1": 0},
                b: {"1": [0, 1]},
                statementMap: {"1": {}, "2": {}}
            }
        });

        const resultado = await executarSgc([
            "frontend",
            "cobertura",
            "ramificacoes",
            "--json",
            "--base",
            diretorioBase,
            "--arquivo",
            caminhoRelatorio
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.totais.total).toBe(2);
        expect(conteudo.arquivos[0]).toMatchObject({
            arquivo: "frontend/src/exemplo.ts",
            ramificacoesPerdidas: 1,
        });
    });

    test("preserva caminhos de cobertura frontend em layout configurado", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-cobertura-frontend-configurado-"));
        const caminhoRelativo = path.join("cliente", "codigo", "exemplo.ts");
        const caminhoArquivo = path.join(diretorioBase, caminhoRelativo);
        const caminhoRelatorio = path.join(diretorioBase, "coverage", "coverage-final.json");

        await escreverJson(path.join(diretorioBase, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {frontendCodigo: "cliente/codigo"},
        });
        await escreverArquivo(caminhoArquivo, "export function exemplo() { return true; }\n");
        await escreverJson(caminhoRelatorio, {
            [caminhoRelativo]: {
                s: {"1": 1},
                f: {"1": 1},
                b: {"1": [0]},
                statementMap: {"1": {}},
            },
        });

        const resultado = await executarSgc([
            "frontend",
            "cobertura",
            "ramificacoes",
            "--json",
            "--base",
            diretorioBase,
            "--arquivo",
            caminhoRelatorio,
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.totais.total).toBe(1);
        expect(conteudo.arquivos[0].arquivo).toBe(caminhoRelativo);
    });
});
