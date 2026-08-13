import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {execa, type Options} from "execa";
import {
    DIRETORIO_RAIZ,
    CAMINHO_TSX,
    executarSgc,
    escreverArquivo,
    escreverJson,
    lerArquivo,
    lerJson,
    existe,
    type ResultadoExecucao
} from "./apoio.js";
import {VERSAO_CONFIGURACAO} from "../biblioteca/configuracao.js";

type ObjetoJson = Record<string, unknown>;

interface RelatorioAnaliseTestesJson {
    versao: number;
    diretorioBackend: string;
    estatisticas: Record<string, number>;
    categorias: Record<string, {comTeste: ObjetoJson[]; semTeste: ObjetoJson[]}>;
}

const CAMINHO_TESTES_PRIORIZAR = path.join(DIRETORIO_RAIZ, "toolkit", "backend", "testes-priorizar.ts");

async function executarScriptTestesPriorizar(args: string[], opcoes: Options = {}): Promise<ResultadoExecucao> {
    const resultado = await execa(CAMINHO_TSX, [CAMINHO_TESTES_PRIORIZAR, ...args], {
        cwd: DIRETORIO_RAIZ,
        reject: false,
        ...opcoes
    });
    return {
        exitCode: resultado.exitCode,
        stdout: String(resultado.stdout),
        stderr: String(resultado.stderr)
    };
}

describe("Análise e priorização dos testes backend", () => {
    test("analisa testes do backend com resumo no console e sidecar JSON", async () => {
        const diretorioSaida = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analisar-"));
        const markdown = path.join(diretorioSaida, "relatorio.md");
        const json = path.join(diretorioSaida, "relatorio.json");

        const resultado = await executarSgc(["backend", "testes", "analisar", "--gravar", "--saida", markdown, "--saida-json", json]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Resumo:");
        expect(resultado.stdout).toContain("repositorios:");
        expect(resultado.stdout).toContain("Cobertura indireta:");
        expect(resultado.stdout).toContain("dtos:");
        expect(await existe(markdown)).toBe(true);
        expect(await existe(json)).toBe(true);

        const conteudoJson = await lerJson<RelatorioAnaliseTestesJson>(json);
        expect(conteudoJson.versao).toBe(1);
        expect(conteudoJson.estatisticas.totalClasses).toBeGreaterThan(0);
        expect(typeof conteudoJson.estatisticas.classesComCoberturaIndireta).toBe("number");
        expect(typeof conteudoJson.estatisticas.classesSemEvidenciaNoEscopo).toBe("number");
        expect(typeof conteudoJson.estatisticas.classesForaEscopoJacoco).toBe("number");
        expect(typeof conteudoJson.estatisticas.classesRuidoIgnorado).toBe("number");
        expect(conteudoJson.categorias.repositorios.comTeste.length).toBeGreaterThanOrEqual(1);
    }, 60000);

    test("analisa testes sem gravar por padrao e permite JSON no stdout", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analisar-somente-leitura-"));
        const fonte = path.join(base, "backend", "src", "main", "java", "com", "exemplo");
        const testes = path.join(base, "backend", "src", "test", "java", "com", "exemplo");
        const markdown = path.join(base, "relatorio.md");
        const json = path.join(base, "relatorio.json");

        await escreverArquivo(
            path.join(fonte, "ExemploService.java"),
            "package com.exemplo; public class ExemploService { public String buscar() { return \"ok\"; } }"
        );
        await escreverArquivo(path.join(testes, "ExemploServiceTest.java"), "package com.exemplo; class ExemploServiceTest {}");

        const resultado = await executarSgc([
            "backend",
            "testes",
            "analisar",
            "--base",
            base,
            "--diretorio",
            "backend",
            "--saida",
            markdown,
            "--saida-json",
            json,
            "--json"
        ]);

        expect(resultado.exitCode).toBe(0);
        expect(() => JSON.parse(resultado.stdout)).not.toThrow();
        expect(await existe(markdown)).toBe(false);
        expect(await existe(json)).toBe(false);
    }, 60000);

    test("analisa fontes e testes backend pelos diretorios configurados", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analisar-configurado-"));
        const fonte = path.join(base, "servidor", "java", "com", "exemplo");
        const testes = path.join(base, "servidor", "testes", "com", "exemplo");
        const markdown = path.join(base, "relatorio.md");
        const json = path.join(base, "relatorio.json");

        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                backendCodigo: "servidor/java",
                backendTestes: "servidor/testes"
            }
        });
        await escreverArquivo(
            path.join(fonte, "ExemploService.java"),
            "package com.exemplo; public class ExemploService { public String buscar() { return \"ok\"; } }"
        );
        await escreverArquivo(
            path.join(testes, "ExemploServiceTest.java"),
            "package com.exemplo; class ExemploServiceTest {}"
        );

        const resultado = await executarSgc([
            "backend",
            "testes",
            "analisar",
            "--gravar",
            "--base",
            base,
            "--saida",
            markdown,
            "--saida-json",
            json
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = await lerJson<RelatorioAnaliseTestesJson>(json);
        expect(conteudo.diretorioBackend).toBe(path.join(base, "servidor", "java"));
        expect(conteudo.estatisticas.classesComTesteDedicado).toBe(1);
    });

    test("resolve diretorio backend relativo a base explicita", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analisar-diretorio-relativo-"));
        const fonte = path.join(base, "servidor", "src", "main", "java", "com", "exemplo");
        const testes = path.join(base, "servidor", "src", "test", "java", "com", "exemplo");
        const markdown = path.join(base, "relatorio.md");
        const json = path.join(base, "relatorio.json");

        await escreverArquivo(
            path.join(fonte, "ExemploService.java"),
            "package com.exemplo; public class ExemploService { public String buscar() { return \"ok\"; } }"
        );
        await escreverArquivo(
            path.join(testes, "ExemploServiceTest.java"),
            "package com.exemplo; class ExemploServiceTest {}"
        );

        const resultado = await executarSgc([
            "backend",
            "testes",
            "analisar",
            "--gravar",
            "--base",
            base,
            "--diretorio",
            "servidor",
            "--saida",
            markdown,
            "--saida-json",
            json
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = await lerJson<RelatorioAnaliseTestesJson>(json);
        expect(conteudo.diretorioBackend).toBe(path.join(base, "servidor", "src", "main", "java"));
        expect(conteudo.estatisticas.classesComTesteDedicado).toBe(1);
    });

    test("ignora dtos estruturais e contratuais do backlog real", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analise-dto-"));
        const backendDir = path.join(base, "backend-fake");
        const dtoDir = path.join(backendDir, "src", "main", "java", "sgc", "exemplo", "dto");
        const markdown = path.join(base, "relatorio.md");
        const json = path.join(base, "relatorio.json");

        await escreverArquivo(
            path.join(dtoDir, "DtoEstrutural.java"),
            "package sgc.exemplo.dto; public record DtoEstrutural(Long codigo, String nome) {}"
        );
        await escreverArquivo(
            path.join(dtoDir, "RequestContratual.java"),
            "package sgc.exemplo.dto; import jakarta.validation.constraints.NotBlank; public record RequestContratual(@NotBlank String nome) {}"
        );
        await escreverArquivo(
            path.join(dtoDir, "DtoComportamental.java"),
            "package sgc.exemplo.dto; public class DtoComportamental { public static DtoComportamental of(String valor) { return new DtoComportamental(); } }"
        );

        const resultado = await executarSgc([
            "backend",
            "testes",
            "analisar",
            "--gravar",
            "--diretorio",
            backendDir,
            "--saida",
            markdown,
            "--saida-json",
            json
        ], {cwd: base});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("dtos: 0/1 testados no backlog real (2 ignorados)");

        const conteudoJson = await lerJson<RelatorioAnaliseTestesJson>(json);
        expect(conteudoJson.estatisticas.dtosComportamentais).toBe(1);
        expect(conteudoJson.estatisticas.dtosEstruturais).toBe(2);
        expect(conteudoJson.estatisticas.dtosEstruturaisContratuais).toBe(1);
        expect(conteudoJson.estatisticas.classesRuidoIgnorado).toBe(2);

        const semTesteDtos = conteudoJson.categorias.dtos.semTeste;
        expect(semTesteDtos.find((item: ObjetoJson) => item.classe === "DtoEstrutural")!.ruidoDtoIgnorado).toBe(true);
        expect(semTesteDtos.find((item: ObjetoJson) => item.classe === "RequestContratual")!.perfilDto).toBe("estruturalContrato");
        expect(semTesteDtos.find((item: ObjetoJson) => item.classe === "DtoComportamental")!.ruidoDtoIgnorado).toBe(false);
    });

    test("ignora modelos estruturais e contratuais do backlog real", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analise-model-"));
        const backendDir = path.join(base, "backend-fake");
        const modelDir = path.join(backendDir, "src", "main", "java", "sgc", "exemplo", "model");
        const markdown = path.join(base, "relatorio.md");
        const json = path.join(base, "relatorio.json");

        await escreverArquivo(
            path.join(modelDir, "SituacaoExemplo.java"),
            "package sgc.exemplo.model; public enum SituacaoExemplo { ATIVO }"
        );
        await escreverArquivo(
            path.join(modelDir, "AnotacaoExemplo.java"),
            "package sgc.exemplo.model; import java.lang.annotation.*; public @interface AnotacaoExemplo { String value() default \"\"; }"
        );
        await escreverArquivo(
            path.join(modelDir, "ProcessoExemplo.java"),
            "package sgc.exemplo.model; import java.util.*; public class ProcessoExemplo { public void sincronizar(Set<Long> codigos) { if (codigos.isEmpty()) return; codigos.stream().filter(Objects::nonNull).toList(); } }"
        );

        const resultado = await executarSgc([
            "backend",
            "testes",
            "analisar",
            "--gravar",
            "--diretorio",
            backendDir,
            "--saida",
            markdown,
            "--saida-json",
            json
        ], {cwd: base});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("modelos: 0/1 testados no backlog real (2 ignorados)");

        const conteudoJson = await lerJson<RelatorioAnaliseTestesJson>(json);
        expect(conteudoJson.estatisticas.modelosComportamentais).toBe(1);
        expect(conteudoJson.estatisticas.modelosEstruturais).toBe(2);
        expect(conteudoJson.estatisticas.modelosEstruturaisContratuais).toBe(1);

        const semTesteModelos = conteudoJson.categorias.modelos.semTeste;
        expect(semTesteModelos.find((item: ObjetoJson) => item.classe === "SituacaoExemplo")!.ruidoModeloIgnorado).toBe(true);
        expect(semTesteModelos.find((item: ObjetoJson) => item.classe === "AnotacaoExemplo")!.perfilModelo).toBe("estruturalContrato");
        expect(semTesteModelos.find((item: ObjetoJson) => item.classe === "ProcessoExemplo")!.ruidoModeloIgnorado).toBe(false);
    });

    test("ignora outros estruturais e contratuais do backlog real e reclassifica comandos como dtos", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analise-outros-"));
        const backendDir = path.join(base, "backend-fake");
        const otherDir = path.join(backendDir, "src", "main", "java", "sgc", "exemplo");
        const dtoDir = path.join(otherDir, "dto");
        const markdown = path.join(base, "relatorio.md");
        const json = path.join(base, "relatorio.json");

        await escreverArquivo(
            path.join(otherDir, "Mensagens.java"),
            "package sgc.exemplo; public final class Mensagens { private Mensagens() {} public static final String OI = \"oi\"; }"
        );
        await escreverArquivo(
            path.join(otherDir, "AnotacaoSegura.java"),
            "package sgc.exemplo; public @interface AnotacaoSegura {}"
        );
        await escreverArquivo(
            path.join(otherDir, "LimitadorExemplo.java"),
            "package sgc.exemplo; import java.util.*; public class LimitadorExemplo { public void verificar(String valor) { if (valor.isBlank()) return; List.of(valor).stream().toList(); } }"
        );
        await escreverArquivo(
            path.join(dtoDir, "WorkflowCommand.java"),
            "package sgc.exemplo.dto; public record WorkflowCommand(String nome) {}"
        );

        const resultado = await executarSgc([
            "backend",
            "testes",
            "analisar",
            "--gravar",
            "--diretorio",
            backendDir,
            "--saida",
            markdown,
            "--saida-json",
            json
        ], {cwd: base});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("outros: 0/1 testados no backlog real (2 ignorados)");
        expect(resultado.stdout).toContain("dtos: 0/0 testados no backlog real (1 ignorados)");

        const conteudoJson = await lerJson<RelatorioAnaliseTestesJson>(json);
        expect(conteudoJson.estatisticas.outrosComportamentais).toBe(1);
        expect(conteudoJson.estatisticas.outrosEstruturais).toBe(2);
        expect(conteudoJson.estatisticas.outrosEstruturaisContratuais).toBe(1);

        const semTesteOutros = conteudoJson.categorias.outros.semTeste;
        expect(semTesteOutros.find((item: ObjetoJson) => item.classe === "Mensagens")!.ruidoOutroIgnorado).toBe(true);
        expect(semTesteOutros.find((item: ObjetoJson) => item.classe === "AnotacaoSegura")!.perfilOutro).toBe("estruturalContrato");
        expect(semTesteOutros.find((item: ObjetoJson) => item.classe === "LimitadorExemplo")!.ruidoOutroIgnorado).toBe(false);

        const semTesteDtos = conteudoJson.categorias.dtos.semTeste;
        expect(semTesteDtos.find((item: ObjetoJson) => item.classe === "WorkflowCommand")!.ruidoDtoIgnorado).toBe(true);
    });

    test("classifica separadamente teste dedicado, cobertura indireta, sem evidencia e fora do escopo", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analise-jacoco-"));
        const backendDir = path.join(base, "backend-fake");
        const srcDir = path.join(backendDir, "src", "main", "java", "sgc", "exemplo");
        const testDir = path.join(backendDir, "src", "test", "java", "sgc", "exemplo");
        const markdown = path.join(base, "relatorio.md");
        const json = path.join(base, "relatorio.json");
        const jacoco = path.join(base, "jacoco.xml");

        await escreverArquivo(path.join(srcDir, "ClasseDireta.java"), "package sgc.exemplo; public class ClasseDireta {}");
        await escreverArquivo(
            path.join(srcDir, "ClasseIndireta.java"),
            "package sgc.exemplo; public class ClasseIndireta { public String calcular(boolean ativo) { return ativo ? \"ok\" : \"pendente\"; } }"
        );
        await escreverArquivo(
            path.join(srcDir, "ClasseSemEvidencia.java"),
            "package sgc.exemplo; public class ClasseSemEvidencia { public boolean validar(String valor) { return valor != null && !valor.isBlank(); } }"
        );
        await escreverArquivo(
            path.join(srcDir, "ClasseForaEscopo.java"),
            "package sgc.exemplo; public class ClasseForaEscopo { public int contarPositivos(java.util.List<Integer> valores) { return (int) valores.stream().filter(valor -> valor > 0).count(); } }"
        );
        await escreverArquivo(path.join(testDir, "ClasseDiretaTest.java"), "package sgc.exemplo; class ClasseDiretaTest {}");
        await escreverArquivo(jacoco, `
<report name="fake">
  <package name="sgc/exemplo">
    <sourcefile name="ClasseDireta.java">
      <line nr="1" mi="0" ci="1" mb="0" cb="0"/>
      <counter type="LINE" missed="0" covered="1"/>
    </sourcefile>
    <sourcefile name="ClasseIndireta.java">
      <line nr="1" mi="0" ci="1" mb="0" cb="0"/>
      <counter type="LINE" missed="0" covered="1"/>
    </sourcefile>
    <sourcefile name="ClasseSemEvidencia.java">
      <line nr="1" mi="1" ci="0" mb="0" cb="0"/>
      <counter type="LINE" missed="1" covered="0"/>
    </sourcefile>
  </package>
</report>`.trim());

        const resultado = await executarSgc([
            "backend",
            "testes",
            "analisar",
            "--gravar",
            "--diretorio",
            backendDir,
            "--saida",
            markdown,
            "--saida-json",
            json,
            "--arquivo-jacoco",
            jacoco
        ], {cwd: base});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Cobertura indireta: 1");
        expect(resultado.stdout).toContain("Sem evidencia no escopo: 1");
        expect(resultado.stdout).toContain("Fora do escopo do JaCoCo: 1");

        const conteudoJson = await lerJson<RelatorioAnaliseTestesJson>(json);
        expect(conteudoJson.estatisticas.classesComTesteDedicado).toBe(1);
        expect(conteudoJson.estatisticas.classesComCoberturaIndireta).toBe(1);
        expect(conteudoJson.estatisticas.classesSemEvidenciaNoEscopo).toBe(1);
        expect(conteudoJson.estatisticas.classesForaEscopoJacoco).toBe(1);

        const itensOutros = conteudoJson.categorias.outros;
        expect(itensOutros.comTeste).toHaveLength(1);
        expect(itensOutros.semTeste).toHaveLength(3);
        expect(itensOutros.semTeste.find((item: ObjetoJson) => item.classe === "ClasseIndireta")!.cobertaSomenteIndiretamente).toBe(true);
        expect(itensOutros.semTeste.find((item: ObjetoJson) => item.classe === "ClasseSemEvidencia")!.evidenciaQualidade).toBe("semEvidenciaNoEscopo");
        expect(itensOutros.semTeste.find((item: ObjetoJson) => item.classe === "ClasseForaEscopo")!.foraEscopoJacoco).toBe(true);
    });

    test("prioriza testes usando sidecar JSON automaticamente quando disponivel", async () => {
        const diretorioSaida = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-priorizar-"));
        const markdown = path.join(diretorioSaida, "analise-testes.md");
        const json = path.join(diretorioSaida, "analise-testes.json");
        const saida = path.join(diretorioSaida, "priorizacao-testes.md");

        await escreverArquivo(markdown, "# Relatorio simplificado\n");
        await escreverJson(json, {
            versao: 1,
            categorias: {
                servicos: {
                    semTeste: [
                        {caminhoRelativo: "sgc/mapa/service/MapaCriticoService.java"}
                    ]
                },
                repositorios: {
                    semTeste: [
                        {caminhoRelativo: "sgc/mapa/model/CompetenciaRepo.java"}
                    ]
                }
            }
        });

        const resultado = await executarScriptTestesPriorizar(["--gravar", "--saida", saida], {cwd: diretorioSaida});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Entrada utilizada: analise-testes.json");
        expect(resultado.stdout).toContain("Encontrados 1 P1, 0 P2, 1 P3");

        const conteudo = await lerArquivo(saida, "utf-8");
        expect(conteudo).toContain("sgc/mapa/service/MapaCriticoService.java");
        expect(conteudo).toContain("sgc/mapa/model/CompetenciaRepo.java");
    });

    test("emite priorizacao JSON versionada", async () => {
        const diretorioSaida = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-priorizar-json-"));
        const entrada = path.join(diretorioSaida, "analise-testes.json");

        await escreverJson(entrada, {
            versao: 1,
            categorias: {
                servicos: {
                    semTeste: [{caminhoRelativo: "sgc/mapa/service/MapaCriticoService.java"}]
                }
            }
        });

        const resultado = await executarScriptTestesPriorizar(["--entrada", entrada, "--json"]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stderr).toBe("");
        const conteudo = JSON.parse(resultado.stdout) as {versao: number; prioridades: Record<string, ObjetoJson[]>};
        expect(conteudo.versao).toBe(1);
        expect(conteudo.prioridades.P1).toHaveLength(1);
    });

    test("rejeita versao incompativel do relatorio de analise", async () => {
        const diretorioSaida = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-priorizar-versao-"));
        const entrada = path.join(diretorioSaida, "analise-testes.json");

        await escreverJson(entrada, {
            versao: 99,
            categorias: {
                servicos: {
                    semTeste: [{caminhoRelativo: "sgc/mapa/service/MapaCriticoService.java"}]
                }
            }
        });

        const resultado = await executarScriptTestesPriorizar(["--entrada", entrada]);

        expect(resultado.exitCode).toBe(1);
        expect(resultado.stdout).toBe("");
        expect(resultado.stderr).toContain("versao ausente ou incompativel");
    });

    test("prioriza testes em stdout sem gravar por padrao", async () => {
        const diretorioSaida = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-priorizar-somente-leitura-"));
        const entrada = path.join(diretorioSaida, "analise-testes.json");

        await escreverJson(entrada, {
            versao: 1,
            categorias: {
                servicos: {
                    semTeste: [{caminhoRelativo: "sgc/mapa/service/MapaCriticoService.java"}]
                }
            }
        });

        const resultado = await executarScriptTestesPriorizar(["--entrada", entrada], {cwd: diretorioSaida});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("# Plano de Priorizacao de Testes Unitarios");
        expect(await existe(path.join(diretorioSaida, "priorizacao-testes.md"))).toBe(false);
    });

    test("prioriza apenas backlog acionavel do JSON e preserva evidencia", async () => {
        const diretorioSaida = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-priorizar-real-"));
        const json = path.join(diretorioSaida, "analise-testes.json");
        const saida = path.join(diretorioSaida, "priorizacao-testes.md");

        await escreverJson(json, {
            versao: 1,
            categorias: {
                servicos: {
                    semTeste: [
                        {
                            caminhoRelativo: "sgc/mapa/service/MapaCriticoService.java",
                            evidenciaQualidade: "semEvidenciaNoEscopo"
                        }
                    ]
                },
                dtos: {
                    semTeste: [
                        {
                            caminhoRelativo: "sgc/mapa/dto/MapaRuidoCommand.java",
                            evidenciaQualidade: "ruidoDtoEstrutural",
                            ruidoDtoIgnorado: true
                        }
                    ]
                },
                outros: {
                    semTeste: [
                        {caminhoRelativo: "sgc/comum/Mensagens.java", evidenciaQualidade: "foraEscopoJacoco"},
                        {
                            caminhoRelativo: "sgc/seguranca/AcaoPermissao.java",
                            evidenciaQualidade: "coberturaIndireta"
                        }
                    ]
                }
            }
        });

        const resultado = await executarScriptTestesPriorizar(["--gravar", "--entrada", json, "--saida", saida]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Encontrados 1 P1, 0 P2, 1 P3");

        const conteudo = await lerArquivo(saida, "utf-8");
        expect(conteudo).toContain("sgc/mapa/service/MapaCriticoService.java");
        expect(conteudo).toContain("sem evidência");
        expect(conteudo).toContain("sgc/seguranca/AcaoPermissao.java");
        expect(conteudo).toContain("cobertura indireta");
        expect(conteudo).not.toContain("Mensagens.java");
        expect(conteudo).not.toContain("MapaRuidoCommand.java");
    });
});
