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

describe("Auditorias backend", () => {
    test("auditores backend usam caminhos configurados e gravam somente com acao explicita", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-configuracao-codigo-backend-"));
        const codigoBackend = path.join(base, "servidor", "java");
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                backendCodigo: "servidor/java",
                artefatosQualidade: "artefatos"
            }
        });
        await escreverArquivo(
            path.join(codigoBackend, "exemplo", "ExemploService.java"),
            [
                "package exemplo;",
                "public class ExemploService {",
                "    public void buscar() {}",
                "    public void criar() {}",
                "    public void iniciar() {}",
                "    public void notificar() {}",
                "}"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "backend",
            "coesao",
            "auditar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.versao).toBe(2);
        expect(conteudo.pontosCriticos).toBeInstanceOf(Array);
        expect(conteudo.hotspots).toBeUndefined();
        expect(conteudo.resumo.totalAnalisados).toBe(1);
        expect(conteudo.todos[0].caminhoRelativo).toBe("servidor/java/exemplo/ExemploService.java");
        expect(await existe(path.join(base, "artefatos", "backend", "mais-recente", "coesao-auditoria.json"))).toBe(false);

        const gravacao = await executarSgc([
            "backend",
            "coesao",
            "auditar",
            "--json",
            "--gravar",
            "--base",
            base
        ]);
        expect(gravacao.exitCode).toBe(0);
        expect(await existe(path.join(base, "artefatos", "backend", "mais-recente", "coesao-auditoria.json"))).toBe(true);
    });

    test("audita service acima do limiar arquitetural sem gravar por padrao", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-backend-"));
        const codigoBackend = path.join(base, "servidor", "java");
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                backendCodigo: "servidor/java",
                artefatosQualidade: "artefatos"
            }
        });
        await escreverArquivo(
            path.join(codigoBackend, "exemplo", "ExemploService.java"),
            [
                "package exemplo;",
                "public class ExemploService {",
                ...Array.from({length: 8}, (_, indice) => `    private final Dependencia${indice} dependencia${indice};`),
                ...Array.from({length: 15}, (_, indice) => `    public void buscar${indice}() {}`),
                "}"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "backend",
            "arquitetura",
            "auditar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.versao).toBe(2);
        expect(conteudo.pontosCriticos).toBeInstanceOf(Array);
        expect(conteudo.hotspots).toBeUndefined();
        expect(conteudo.resumo).toMatchObject({totalAnalisados: 1, criticos: 0, alertas: 1, ok: 0});
        expect(conteudo.todos[0]).toMatchObject({
            nomeArquivo: "ExemploService.java",
            caminhoRelativo: "servidor/java/exemplo/ExemploService.java",
            tipo: "service",
            metodos: 15,
            dependencias: 8,
            severidade: "alerta"
        });
        expect(conteudo.todos[0].motivos).toContain("15 métodos públicos (>=15)");
        expect(await existe(path.join(base, "artefatos", "backend", "mais-recente", "arquitetura-auditoria.md"))).toBe(false);

        const gravacao = await executarSgc([
            "backend",
            "arquitetura",
            "auditar",
            "--json",
            "--gravar",
            "--base",
            base
        ]);
        expect(gravacao.exitCode).toBe(0);
        expect(await existe(path.join(base, "artefatos", "backend", "mais-recente", "arquitetura-auditoria.md"))).toBe(true);
    });

    test("audita vazamento de modelo em DTO de controlador sem gravar por padrao", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-contratos-backend-"));
        const codigoBackend = path.join(base, "servidor", "java");
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                backendCodigo: "servidor/java",
                artefatosQualidade: "artefatos"
            }
        });
        await escreverArquivo(
            path.join(codigoBackend, "exemplo", "web", "UsuarioController.java"),
            [
                "package exemplo.web;",
                "import exemplo.web.dto.UsuarioResponse;",
                "public class UsuarioController {",
                "    public UsuarioResponse obter() { return null; }",
                "}"
            ].join("\n")
        );
        await escreverArquivo(
            path.join(codigoBackend, "exemplo", "web", "dto", "UsuarioResponse.java"),
            [
                "package exemplo.web.dto;",
                "import exemplo.model.Usuario;",
                "public record UsuarioResponse(Usuario usuario) {}"
            ].join("\n")
        );
        await escreverArquivo(
            path.join(codigoBackend, "exemplo", "model", "Usuario.java"),
            [
                "package exemplo.model;",
                "public class Usuario {}"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "backend",
            "contratos",
            "auditar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.resumo.totalAchados).toBe(1);
        expect(conteudo.achados[0]).toMatchObject({
            controlador: "UsuarioController.java",
            metodo: "obter",
            tipoRetorno: "UsuarioResponse",
            campo: "usuario",
            tipoModelo: "exemplo.model.Usuario"
        });
        expect(await existe(path.join(base, "artefatos", "backend", "mais-recente", "contratos-auditoria.md"))).toBe(false);

        const gravacao = await executarSgc([
            "backend",
            "contratos",
            "auditar",
            "--json",
            "--gravar",
            "--base",
            base
        ]);
        expect(gravacao.exitCode).toBe(0);
        expect(await existe(path.join(base, "artefatos", "backend", "mais-recente", "contratos-auditoria.md"))).toBe(true);
    });
});
