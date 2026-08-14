import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {
    executarSgc,
    escreverArquivo,
    escreverJson,
    existe,
    lerJson
} from "./apoio.js";

describe("Auditorias de consistência e nomenclatura", () => {
    test("mantem coleta de simbolos read-only por padrao e grava sob demanda", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-nomenclatura-base-"));
        await escreverArquivo(
            path.join(diretorioBase, "frontend", "src", "exemplo.ts"),
            "export function carregarExemplo(codigo: string) { return codigo; }\n"
        );

        const resultado = await executarSgc([
            "codigo",
            "nomes",
            "coletar-simbolos",
            "--json",
            "--base",
            diretorioBase
        ]);

        expect(resultado.exitCode).toBe(0);
        const inventario = JSON.parse(resultado.stdout);
        expect(inventario.versao).toBe(1);
        expect(inventario.base).toBe(diretorioBase);
        expect(inventario.totais.arquivos).toBe(1);
        const caminhoSimbolos = path.join(
            diretorioBase,
            "toolkit",
            "qualidade",
            "artefatos",
            "nomenclatura",
            "mais-recente",
            "simbolos.json"
        );
        expect(await existe(caminhoSimbolos)).toBe(false);

        const gravacao = await executarSgc([
            "codigo",
            "nomes",
            "coletar-simbolos",
            "--json",
            "--gravar",
            "--base",
            diretorioBase
        ]);

        expect(gravacao.exitCode).toBe(0);
        expect(await existe(caminhoSimbolos)).toBe(true);
        expect((await lerJson<{versao: number}>(caminhoSimbolos)).versao).toBe(1);
    });

    test("mantem auditoria de nomenclatura read-only e propaga gravacao ao inventario", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-nomenclatura-read-only-"));
        await escreverArquivo(
            path.join(diretorioBase, "frontend", "src", "exemplo.ts"),
            "export function carregarExemplo(codigo: string) { return codigo; }\n"
        );

        const resultado = await executarSgc([
            "codigo",
            "nomes",
            "auditar-consistencia",
            "--json",
            "--base",
            diretorioBase
        ]);

        expect(resultado.exitCode).toBe(0);
        expect(JSON.parse(resultado.stdout).versao).toBe(1);
        expect(JSON.parse(resultado.stdout).base).toBe(diretorioBase);
        expect(await existe(path.join(diretorioBase, "toolkit"))).toBe(false);

        const gravacao = await executarSgc([
            "codigo",
            "nomes",
            "auditar-consistencia",
            "--json",
            "--gravar",
            "--base",
            diretorioBase
        ]);

        expect(gravacao.exitCode).toBe(0);
        expect(await existe(path.join(
            diretorioBase,
            "toolkit",
            "qualidade",
            "artefatos",
            "nomenclatura",
            "mais-recente",
            "consistencia.json"
        ))).toBe(true);
        expect((await lerJson<{versao: number}>(path.join(
            diretorioBase,
            "toolkit",
            "qualidade",
            "artefatos",
            "nomenclatura",
            "mais-recente",
            "consistencia.json"
        ))).versao).toBe(1);
    });

    test("emite resumo JSON limitado da auditoria de nomenclatura", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-nomenclatura-resumo-"));
        await escreverArquivo(
            path.join(diretorioBase, "frontend", "src", "exemplo.ts"),
            "export function getExemplo(id: string) { return id; }\n"
        );

        const resultado = await executarSgc([
            "codigo",
            "nomes",
            "auditar-consistencia",
            "--base",
            diretorioBase,
            "--json-resumido"
        ]);

        expect(resultado.exitCode).toBe(0);
        const resumo = JSON.parse(resultado.stdout);
        expect(resumo).toMatchObject({versaoResumo: 1, truncado: true, limiteItens: 20});
        expect(resumo.indicadores.arquivos).toBe(1);
        expect(resumo.indicadores.parametrosComId).toBe(1);
        expect(resumo.achados.parametrosComId).toHaveLength(1);
        expect(resumo.formatosArquivos[".ts"]).toBeDefined();
        expect(resumo.tiposForaPadrao).toBeUndefined();
    });

    test("mantem auditoria de idioma read-only e grava sob demanda", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-idioma-"));
        await escreverArquivo(
            path.join(diretorioBase, "frontend", "src", "exemplo.ts"),
            "export function getExemplo(id: string) { return id; }\n"
        );

        const resultado = await executarSgc([
            "codigo",
            "nomes",
            "auditar-idioma",
            "--json",
            "--base",
            diretorioBase
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.versao).toBe(2);
        expect(conteudo.indicadores.membrosIngles).toBeGreaterThan(0);
        expect(conteudo.indicadores.pontuacaoTotal).toBeTypeOf("number");
        expect(conteudo.indicadores.scoreTotal).toBeUndefined();
        expect(await existe(path.join(diretorioBase, "toolkit"))).toBe(false);

        const gravacao = await executarSgc([
            "codigo",
            "nomes",
            "auditar-idioma",
            "--json",
            "--gravar",
            "--base",
            diretorioBase
        ]);

        expect(gravacao.exitCode).toBe(0);
        expect(await existe(path.join(
            diretorioBase,
            "toolkit",
            "qualidade",
            "artefatos",
            "nomenclatura",
            "mais-recente",
            "idioma.json"
        ))).toBe(true);
        expect((await lerJson<{versao: number}>(path.join(
            diretorioBase,
            "toolkit",
            "qualidade",
            "artefatos",
            "nomenclatura",
            "mais-recente",
            "idioma.json"
        ))).versao).toBe(2);
    });

    test("rejeita inventario de simbolos invalido em vez de substitui-lo silenciosamente", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-nomenclatura-inventario-invalido-"));
        const caminhoInventario = path.join(diretorioBase, "inventario-invalido.json");
        await escreverArquivo(
            path.join(diretorioBase, "frontend", "src", "exemplo.ts"),
            "export function carregarExemplo(codigo: string) { return codigo; }\n"
        );
        await escreverJson(caminhoInventario, {versao: 99, base: diretorioBase, arquivos: []});

        const resultado = await executarSgc([
            "codigo",
            "nomes",
            "auditar-consistencia",
            "--json",
            "--base",
            diretorioBase,
            "--inventario",
            caminhoInventario
        ]);

        expect(resultado.exitCode).toBe(1);
        expect(resultado.stdout).toBe("");
        expect(resultado.stderr).toContain("versao incompativel");
    });
});
