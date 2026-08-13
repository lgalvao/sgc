import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {
    DIRETORIO_RAIZ,
    executarSgc,
    escreverArquivo,
    escreverJson,
    existe,
    alterarPermissoes
} from "./apoio.js";
import {resolverCaminhoConfigurado, VERSAO_CONFIGURACAO} from "../lib/configuracao.js";
import {normalizarCaminhoAchado, obterComandoSemgrep, resolverDiretoriosPadrao} from "../codigo/semgrep-auditar.js";
import {executarAuditoria as executarAuditoriaCheiros} from "../codigo/cheiros-auditar.js";

describe("Auditores de código", () => {
    test("audita cheiros de codigo em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cheiros-"));
        const frontendDir = path.join(base, "frontend", "src");
        const backendDir = path.join(base, "backend", "src", "main", "java", "sgc", "exemplo", "dto");

        await escreverArquivo(
            path.join(frontendDir, "Exemplo.ts"),
            [
                "export function exemplo(valor: any) {",
                "  if (valor === null) return valor || [];",
                "  return valor as any;",
                "}"
            ].join("\n")
        );

        await escreverArquivo(
            path.join(backendDir, "ExemploDto.java"),
            [
                "package sgc.exemplo.dto;",
                "import org.jspecify.annotations.Nullable;",
                "public record ExemploDto(@Nullable String nome) {",
                "  public boolean vazio(String valor) {",
                "    return valor == null;",
                "  }",
                "}"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "codigo",
            "cheiros",
            "auditar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.contagens.backend_nullable_dto).toBe(1);
        expect(conteudo.contagens.backend_null_checks).toBe(1);
        expect(conteudo.contagens.frontend_any_producao).toBe(2);
        expect(conteudo.contagens.frontend_null_checks).toBe(1);
        expect(conteudo.contagens.frontend_fallback_or).toBe(1);
        expect(await existe(path.join(base, "toolkit", "qualidade", "artefatos", "codigo-cheiros"))).toBe(false);

        const diretorioSaida = path.join(base, "artefatos", "cheiros");
        await executarAuditoriaCheiros({base, gravar: true, diretorioSaida});
        expect(await existe(path.join(diretorioSaida, "fotografia.json"))).toBe(true);
        expect(await existe(path.join(diretorioSaida, "resumo.md"))).toBe(true);
    });

    test("resolve politica Semgrep padrao a partir da instalacao do toolkit", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-configuracao-politica-"));
        const caminhoEsperado = path.join(
            DIRETORIO_RAIZ,
            "toolkit",
            "qualidade",
            "politicas",
            "semgrep",
            "sgc-qualidade.yml"
        );

        expect(resolverCaminhoConfigurado("regrasSemgrep", base)).toBe(caminhoEsperado);

        const caminhoAlternativo = path.join(base, "politicas", "regras.yml");
        await escreverArquivo(caminhoAlternativo, "rules: []\n");
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {regrasSemgrep: "politicas/regras.yml"}
        });
        expect(resolverCaminhoConfigurado("regrasSemgrep", base)).toBe(caminhoAlternativo);

        const diretorioBinario = await mkdtemp(path.join(os.tmpdir(), "sgc-semgrep-bin-"));
        const caminhoExecutavel = path.join(diretorioBinario, "semgrep");
        await escreverArquivo(caminhoExecutavel, "#!/bin/sh\nexit 0\n");
        await alterarPermissoes(caminhoExecutavel, 0o755);
        expect(obterComandoSemgrep(diretorioBinario)).toBe(caminhoExecutavel);
        expect(obterComandoSemgrep(path.join(diretorioBinario, "inexistente"))).toBe("semgrep");
    });

    test("resolve alvos padrao do Semgrep pela configuracao da base", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-semgrep-base-"));
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                backendCodigo: "servidor/java",
                frontendCodigo: "aplicacao/src"
            }
        });

        expect(resolverDiretoriosPadrao(base)).toEqual([
            "servidor/java",
            "aplicacao/src"
        ]);
    });

    test("normaliza caminhos relativos e absolutos dos achados Semgrep", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-semgrep-caminhos-"));
        const caminhoRelativo = "servidor/java/ExemploService.java";
        const caminhoAbsoluto = path.join(base, caminhoRelativo);

        expect(normalizarCaminhoAchado(caminhoRelativo, base)).toBe(caminhoRelativo);
        expect(normalizarCaminhoAchado(caminhoAbsoluto, base)).toBe(caminhoRelativo);
    });

    test("aplica filtros de cheiros aos diretorios de codigo configurados", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cheiros-base-"));
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                backendCodigo: "servidor/java",
                frontendCodigo: "aplicacao/src"
            }
        });
        await escreverArquivo(
            path.join(base, "servidor", "java", "ExemploResponse.java"),
            "class ExemploResponse { @Nullable String nome; }\n"
        );
        await escreverArquivo(
            path.join(base, "aplicacao", "src", "Exemplo.ts"),
            "export function exemplo(valor: any) { return valor || []; }\n"
        );

        const resultado = await executarAuditoriaCheiros({base});

        expect(resultado.snapshot.contagens.backend_nullable_dto).toBe(1);
        expect(resultado.snapshot.contagens.frontend_any_producao).toBe(1);
        expect(resultado.snapshot.contagens.frontend_fallback_or).toBe(1);
    });
});
