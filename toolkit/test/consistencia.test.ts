import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {
    executarSgc,
    escreverArquivo,
    existe
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
        expect(JSON.parse(resultado.stdout).indicadores.membrosIngles).toBeGreaterThan(0);
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
    });
});
