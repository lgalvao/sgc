import {readFile} from "node:fs/promises";
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
import {resolverCaminhoConfigurado, VERSAO_CONFIGURACAO} from "../biblioteca/configuracao.js";
import {executarSemgrep, normalizarCaminhoAchado, obterComandoSemgrep} from "../codigo/semgrep-motor.js";
import {resolverDiretoriosPadrao} from "../codigo/semgrep-auditar.js";
import {executarAuditoria as executarAuditoriaCheiros} from "../codigo/cheiros-auditar.js";

describe("Auditores de código", () => {
    test("emite resumo JSON limitado para inventario de simbolos", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-simbolos-resumo-"));
        await escreverArquivo(
            path.join(base, "Exemplo.java"),
            "package exemplo; public class Exemplo { public void buscar() {} }"
        );
        await escreverArquivo(
            path.join(base, "Exemplo.ts"),
            "export function criar() { return true; }\n"
        );

        const resultado = await executarSgc([
            "codigo",
            "nomes",
            "coletar-simbolos",
            "--base",
            base,
            "--json-resumido"
        ]);

        expect(resultado.exitCode).toBe(0);
        const resumo = JSON.parse(resultado.stdout);
        expect(resumo).toMatchObject({versaoResumo: 1, truncado: true, limiteItens: 20});
        expect(resumo.totais.arquivos).toBe(2);
        expect(resumo.arquivos).toBeUndefined();
        expect(resumo.arquivosComMaisMembros).toHaveLength(2);
    });

    test("audita cheiros de codigo em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cheiros-"));
        const diretorioCliente = path.join(base, "frontend", "src");
        const diretorioServidor = path.join(base, "backend", "src", "main", "java", "sgc", "exemplo", "dto");

        await escreverArquivo(
            path.join(diretorioCliente, "Exemplo.ts"),
            [
                "export function exemplo(valor: any) {",
                "  if (valor === null) return valor || [];",
                "  return valor as any;",
                "}"
            ].join("\n")
        );
        await escreverArquivo(
            path.join(diretorioCliente, "__tests__", "Exemplo.spec.ts"),
            "const resposta: any = {};\nvoid resposta;\n"
        );

        await escreverArquivo(
            path.join(diretorioServidor, "ExemploDto.java"),
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
        expect(conteudo.versao).toBe(4);
        expect(conteudo.pontuacao.classificacao).toBe("tendencia");
        expect(conteudo.pontuacao.porEscopo).toEqual({servidor: 7, cliente: 11});
        expect(conteudo.contagens.clienteAnyTestes).toBeUndefined();
        expect(conteudo.contagens.servidorDtoNulavel).toBe(1);
        expect(conteudo.contagens.servidorVerificacoesNulas).toBe(1);
        expect(conteudo.contagens.clienteAnyProducao).toBe(2);
        expect(conteudo.contagens.clienteVerificacoesNulas).toBe(1);
        expect(conteudo.contagens.clienteFallbackOu).toBe(1);
        expect(await existe(path.join(base, "toolkit", "qualidade", "artefatos", "codigo-cheiros"))).toBe(false);

        const diretorioSaida = path.join(base, "artefatos", "cheiros");
        await executarAuditoriaCheiros({base, gravar: true, diretorioSaida});
        expect(await existe(path.join(diretorioSaida, "fotografia.json"))).toBe(true);
        expect(await existe(path.join(diretorioSaida, "resumo.md"))).toBe(true);
        const fotografia = JSON.parse(await readFile(path.join(diretorioSaida, "fotografia.json"), "utf8"));
        expect(fotografia.versao).toBe(4);
        expect(fotografia.itensSinalizados).toBeInstanceOf(Array);
        expect(fotografia.hotspots).toBeUndefined();

        const humano = await executarSgc(["codigo", "cheiros", "auditar", "--base", base]);
        expect(humano.exitCode).toBe(0);
        expect(humano.stdout).toContain("nao e severidade");
        expect(humano.stdout).toContain("Itens com sinais:");
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

    test("descreve o Semgrep como auditoria configurada, nao como piloto", async () => {
        const resultado = await executarSgc(["codigo", "semgrep", "auditar", "--help"]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Executa regras Semgrep configuradas");
        expect(resultado.stdout).not.toContain("piloto de Semgrep");
    });

    test("resolve alvos padrao do Semgrep pela configuracao da base", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-semgrep-base-"));
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                codigoServidor: "servidor/java",
                codigoCliente: "aplicacao/src"
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

    test("executa o motor Semgrep com regra, alvo e comando explícitos", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-semgrep-motor-"));
        const diretorioBinario = path.join(base, "bin");
        const caminhoExecutavel = path.join(diretorioBinario, "semgrep");
        await escreverArquivo(
            caminhoExecutavel,
            [
                "#!/bin/sh",
                "printf '%s' '{\"results\":[{\"check_id\":\"regra.exemplo\",\"path\":\"codigo/Exemplo.java\",\"start\":{\"line\":7}}]}'"
            ].join("\n")
        );
        await alterarPermissoes(caminhoExecutavel, 0o755);

        const resultado = await executarSemgrep({
            regra: "politicas/regras.yml",
            diretorios: ["codigo"],
            diretorioBase: base,
            comando: caminhoExecutavel
        });

        expect(resultado).toMatchObject({
            comando: caminhoExecutavel,
            regra: "politicas/regras.yml",
            diretorios: ["codigo"],
            codigoSaida: 0
        });
        expect(resultado.resultadoJson.results).toHaveLength(1);
    });

    test("exige alvo explícito no motor Semgrep", async () => {
        await expect(executarSemgrep({regra: "regras.yml", diretorios: []})).rejects.toThrow("pelo menos um diretório-alvo");
    });

    test("aplica filtros de cheiros aos diretorios de codigo configurados", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cheiros-base-"));
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                codigoServidor: "servidor/java",
                codigoCliente: "aplicacao/src"
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

        expect(resultado.fotografia.contagens.servidorDtoNulavel).toBe(1);
        expect(resultado.fotografia.contagens.clienteAnyProducao).toBe(1);
        expect(resultado.fotografia.contagens.clienteFallbackOu).toBe(1);
    });

    test("rejeita fotografia anterior de cheiros invalida", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cheiros-fotografia-invalida-"));
        const diretorioSaida = path.join(base, "artefatos", "cheiros");
        await escreverJson(path.join(diretorioSaida, "fotografia.json"), {versao: 99, contagens: {}});

        await expect(executarAuditoriaCheiros({base, diretorioSaida})).rejects.toThrow("versao incompativel");
    });
});
