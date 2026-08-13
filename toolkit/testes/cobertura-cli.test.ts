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
import {VERSAO_CONFIGURACAO} from "../biblioteca/configuracao.js";
import {extrairCoberturaJacoco} from "../biblioteca/dominios/cobertura-java.js";

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
            "    <sourcefile name=\"Dto.java\">",
            "      <line nr=\"3\" mi=\"0\" ci=\"1\" mb=\"0\" cb=\"0\"/>",
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

        const servidorLeitura = await executarSgc([
            "servidor",
            "cobertura",
            "auditoria",
            "--json",
            "--base",
            base,
            "--arquivo",
            caminhoJacoco
        ]);
        const clienteLeitura = await executarSgc([
            "cliente",
            "cobertura",
            "auditoria",
            "--json",
            "--base",
            base,
            "--arquivo",
            caminhoV8
        ]);
        const servidorRamificacoes = await executarSgc([
            "servidor",
            "cobertura",
            "ramificacoes",
            "--json",
            "--base",
            base,
            "--arquivo",
            caminhoJacoco
        ]);

        expect(servidorLeitura.exitCode).toBe(0);
        expect(clienteLeitura.exitCode).toBe(0);
        expect(servidorRamificacoes.exitCode).toBe(0);
        const servidorJson = JSON.parse(servidorLeitura.stdout);
        const clienteJson = JSON.parse(clienteLeitura.stdout);
        expect(servidorJson).toMatchObject({
            versaoSchema: "1.0.0",
            status: "ok",
        });
        expect(servidorJson.totais.totais.totalArquivos).toBe(1);
        expect(servidorJson.geradoEm).toBeTypeOf("string");
        expect(servidorJson.pontosCriticos).toBeInstanceOf(Array);
        expect(servidorJson.hotspots).toBeUndefined();
        expect(clienteJson).toMatchObject({
            versaoSchema: "1.0.0",
            status: "ok",
        });
        expect(clienteJson.geradoEm).toBeTypeOf("string");
        expect(clienteJson.pontosCriticos).toBeInstanceOf(Array);
        expect(clienteJson.hotspots).toBeUndefined();
        const servidorRamificacoesJson = JSON.parse(servidorRamificacoes.stdout);
        expect(servidorRamificacoesJson).toMatchObject({versaoSchema: "1.0.0", status: "ok"});
        expect(servidorRamificacoesJson.geradoEm).toBeTypeOf("string");
        expect(servidorRamificacoesJson.timestamp).toBeUndefined();
        expect(await existe(path.join(base, "servidor-cobertura-auditoria.md"))).toBe(false);
        expect(await existe(path.join(base, "cliente-cobertura-auditoria.md"))).toBe(false);

        const servidorGravacao = await executarSgc([
            "servidor",
            "cobertura",
            "auditoria",
            "--json",
            "--gravar",
            "--base",
            base,
            "--arquivo",
            caminhoJacoco
        ]);
        const clienteGravacao = await executarSgc([
            "cliente",
            "cobertura",
            "auditoria",
            "--json",
            "--gravar",
            "--base",
            base,
            "--arquivo",
            caminhoV8
        ]);

        expect(servidorGravacao.exitCode).toBe(0);
        expect(clienteGravacao.exitCode).toBe(0);
        expect(await existe(path.join(base, "servidor-cobertura-auditoria.md"))).toBe(true);
        expect(await existe(path.join(base, "cliente-cobertura-auditoria.md"))).toBe(true);
    });

    test("não injeta exclusões do SGC no domínio JaCoCo", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cobertura-politica-generica-"));
        const caminhoJacoco = path.join(base, "relatorios", "jacoco.xml");
        await escreverArquivo(caminhoJacoco, [
            "<report>",
            "  <package name=\"exemplo\">",
            "    <sourcefile name=\"Dto.java\">",
            "      <line nr=\"1\" ci=\"1\" mi=\"0\" mb=\"0\" cb=\"0\"/>",
            "    </sourcefile>",
            "  </package>",
            "</report>",
            ""
        ].join("\n"));

        const semPolitica = await extrairCoberturaJacoco(caminhoJacoco, {
            diretorioBase: base,
            aplicarExclusoes: true
        });
        const comPolitica = await extrairCoberturaJacoco(caminhoJacoco, {
            diretorioBase: base,
            aplicarExclusoes: true,
            padroesExclusao: [/Dto$/]
        });

        expect(semPolitica.totais.totalArquivos).toBe(1);
        expect(comPolitica.totais.totalArquivos).toBe(0);
    });

    test("analisa cobertura do cliente a partir de base e relatorio V8 externos", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-cobertura-cliente-"));
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
            "cliente",
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
        expect(conteudo.versaoSchema).toBe("1.0.0");
        expect(conteudo.geradoEm).toBeTypeOf("string");
        expect(conteudo.timestamp).toBeUndefined();
        expect(conteudo.totais.total).toBe(2);
        expect(conteudo.arquivos[0]).toMatchObject({
            arquivo: "frontend/src/exemplo.ts",
            ramificacoesPerdidas: 1,
        });
    });

    test("preserva caminhos de cobertura do cliente em layout configurado", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-cobertura-cliente-configurado-"));
        const caminhoRelativo = path.join("cliente", "codigo", "exemplo.ts");
        const caminhoArquivo = path.join(diretorioBase, caminhoRelativo);
        const caminhoRelatorio = path.join(diretorioBase, "coverage", "coverage-final.json");

        await escreverJson(path.join(diretorioBase, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {codigoCliente: "cliente/codigo"},
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
            "cliente",
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

    test("ignora fontes removidas do relatório ao cruzar ramificações de erro", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cobertura-erros-fonte-ausente-"));
        const caminhoRelatorio = path.join(base, "coverage", "coverage-final.json");
        await escreverJson(caminhoRelatorio, {
            [path.join(base, "frontend", "src", "removido.ts")]: {
                s: {"1": 0},
                f: {"1": 1},
                b: {"1": [0, 1]},
                statementMap: {"1": {}}
            }
        });

        const resultado = await executarSgc([
            "cliente",
            "cobertura",
            "ramificacoes-erros",
            "--json",
            "--base",
            base,
            "--arquivo",
            caminhoRelatorio
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.versaoSchema).toBe("1.0.0");
        expect(conteudo.geradoEm).toBeTypeOf("string");
        expect(conteudo.timestamp).toBeUndefined();
        expect(conteudo.arquivos).toEqual([]);
    });
});
