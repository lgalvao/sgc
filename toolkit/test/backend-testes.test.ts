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
import {VERSAO_CONFIGURACAO} from "../lib/configuracao.js";

type ObjetoJson = Record<string, unknown>;

interface RelatorioAnaliseTestesJson {
    backend_dir: string;
    estatisticas: Record<string, number>;
    categorias: Record<string, {tested: ObjetoJson[]; untested: ObjetoJson[]}>;
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

        const resultado = await executarSgc(["backend", "testes", "analisar", "--saida", markdown, "--saida-json", json]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Resumo:");
        expect(resultado.stdout).toContain("Repositories:");
        expect(resultado.stdout).toContain("Cobertura indireta:");
        expect(resultado.stdout).toContain("DTOs:");
        expect(await existe(markdown)).toBe(true);
        expect(await existe(json)).toBe(true);

        const conteudoJson = await lerJson<RelatorioAnaliseTestesJson>(json);
        expect(conteudoJson.estatisticas.total_classes).toBeGreaterThan(0);
        expect(typeof conteudoJson.estatisticas.classes_com_cobertura_indireta).toBe("number");
        expect(typeof conteudoJson.estatisticas.classes_sem_evidencia_no_escopo).toBe("number");
        expect(typeof conteudoJson.estatisticas.classes_fora_escopo_jacoco).toBe("number");
        expect(typeof conteudoJson.estatisticas.classes_ruido_ignorado).toBe("number");
        expect(conteudoJson.categorias.Repositories.tested.length).toBeGreaterThanOrEqual(1);
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
            "--base",
            base,
            "--saida",
            markdown,
            "--saida-json",
            json
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = await lerJson<RelatorioAnaliseTestesJson>(json);
        expect(conteudo.backend_dir).toBe(path.join(base, "servidor", "java"));
        expect(conteudo.estatisticas.classes_com_teste_dedicado).toBe(1);
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
        expect(conteudo.backend_dir).toBe(path.join(base, "servidor", "src", "main", "java"));
        expect(conteudo.estatisticas.classes_com_teste_dedicado).toBe(1);
    });

    test("ignora DTOs estruturais e contratuais do backlog real", async () => {
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
            "--diretorio",
            backendDir,
            "--saida",
            markdown,
            "--saida-json",
            json
        ], {cwd: base});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("DTOs: 0/1 testados no backlog real (2 ignorados)");

        const conteudoJson = await lerJson<RelatorioAnaliseTestesJson>(json);
        expect(conteudoJson.estatisticas.dtos_comportamentais).toBe(1);
        expect(conteudoJson.estatisticas.dtos_estruturais).toBe(2);
        expect(conteudoJson.estatisticas.dtos_estruturais_contratuais).toBe(1);
        expect(conteudoJson.estatisticas.classes_ruido_ignorado).toBe(2);

        const dtoUntested = conteudoJson.categorias.DTOs.untested;
        expect(dtoUntested.find((item: ObjetoJson) => item.classe === "DtoEstrutural")!.dto_ruido_ignorado).toBe(true);
        expect(dtoUntested.find((item: ObjetoJson) => item.classe === "RequestContratual")!.perfil_dto).toBe("estrutural_contrato");
        expect(dtoUntested.find((item: ObjetoJson) => item.classe === "DtoComportamental")!.dto_ruido_ignorado).toBe(false);
    });

    test("ignora models estruturais e contratuais do backlog real", async () => {
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
            "--diretorio",
            backendDir,
            "--saida",
            markdown,
            "--saida-json",
            json
        ], {cwd: base});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Models: 0/1 testados no backlog real (2 ignorados)");

        const conteudoJson = await lerJson<RelatorioAnaliseTestesJson>(json);
        expect(conteudoJson.estatisticas.models_comportamentais).toBe(1);
        expect(conteudoJson.estatisticas.models_estruturais).toBe(2);
        expect(conteudoJson.estatisticas.models_estruturais_contratuais).toBe(1);

        const modelUntested = conteudoJson.categorias.Models.untested;
        expect(modelUntested.find((item: ObjetoJson) => item.classe === "SituacaoExemplo")!.model_ruido_ignorado).toBe(true);
        expect(modelUntested.find((item: ObjetoJson) => item.classe === "AnotacaoExemplo")!.perfil_model).toBe("estrutural_contrato");
        expect(modelUntested.find((item: ObjetoJson) => item.classe === "ProcessoExemplo")!.model_ruido_ignorado).toBe(false);
    });

    test("ignora others estruturais e contratuais do backlog real e reclassifica commands como DTOs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analise-others-"));
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
            "--diretorio",
            backendDir,
            "--saida",
            markdown,
            "--saida-json",
            json
        ], {cwd: base});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Others: 0/1 testados no backlog real (2 ignorados)");
        expect(resultado.stdout).toContain("DTOs: 0/0 testados no backlog real (1 ignorados)");

        const conteudoJson = await lerJson<RelatorioAnaliseTestesJson>(json);
        expect(conteudoJson.estatisticas.others_comportamentais).toBe(1);
        expect(conteudoJson.estatisticas.others_estruturais).toBe(2);
        expect(conteudoJson.estatisticas.others_estruturais_contratuais).toBe(1);

        const otherUntested = conteudoJson.categorias.Others.untested;
        expect(otherUntested.find((item: ObjetoJson) => item.classe === "Mensagens")!.other_ruido_ignorado).toBe(true);
        expect(otherUntested.find((item: ObjetoJson) => item.classe === "AnotacaoSegura")!.perfil_other).toBe("estrutural_contrato");
        expect(otherUntested.find((item: ObjetoJson) => item.classe === "LimitadorExemplo")!.other_ruido_ignorado).toBe(false);

        const dtoUntested = conteudoJson.categorias.DTOs.untested;
        expect(dtoUntested.find((item: ObjetoJson) => item.classe === "WorkflowCommand")!.dto_ruido_ignorado).toBe(true);
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
        expect(conteudoJson.estatisticas.classes_com_teste_dedicado).toBe(1);
        expect(conteudoJson.estatisticas.classes_com_cobertura_indireta).toBe(1);
        expect(conteudoJson.estatisticas.classes_sem_evidencia_no_escopo).toBe(1);
        expect(conteudoJson.estatisticas.classes_fora_escopo_jacoco).toBe(1);

        const others = conteudoJson.categorias.Others;
        expect(others.tested).toHaveLength(1);
        expect(others.untested).toHaveLength(3);
        expect(others.untested.find((item: ObjetoJson) => item.classe === "ClasseIndireta")!.coberta_somente_indiretamente).toBe(true);
        expect(others.untested.find((item: ObjetoJson) => item.classe === "ClasseSemEvidencia")!.evidencia_qualidade).toBe("sem_evidencia_no_escopo");
        expect(others.untested.find((item: ObjetoJson) => item.classe === "ClasseForaEscopo")!.fora_escopo_jacoco).toBe(true);
    });

    test("prioriza testes usando sidecar JSON automaticamente quando disponivel", async () => {
        const diretorioSaida = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-priorizar-"));
        const markdown = path.join(diretorioSaida, "analise-testes.md");
        const json = path.join(diretorioSaida, "analise-testes.json");
        const saida = path.join(diretorioSaida, "priorizacao-testes.md");

        await escreverArquivo(markdown, "# Relatorio simplificado\n");
        await escreverJson(json, {
            categorias: {
                Services: {
                    untested: [
                        {caminho_relativo: "sgc/mapa/service/MapaCriticoService.java"}
                    ]
                },
                Repositories: {
                    untested: [
                        {caminho_relativo: "sgc/mapa/model/CompetenciaRepo.java"}
                    ]
                }
            }
        });

        const resultado = await executarScriptTestesPriorizar(["--saida", saida], {cwd: diretorioSaida});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Entrada utilizada: analise-testes.json");
        expect(resultado.stdout).toContain("Encontrados 1 P1, 0 P2, 1 P3");

        const conteudo = await lerArquivo(saida, "utf-8");
        expect(conteudo).toContain("sgc/mapa/service/MapaCriticoService.java");
        expect(conteudo).toContain("sgc/mapa/model/CompetenciaRepo.java");
    });

    test("prioriza apenas backlog acionavel do JSON e preserva evidencia", async () => {
        const diretorioSaida = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-priorizar-real-"));
        const json = path.join(diretorioSaida, "analise-testes.json");
        const saida = path.join(diretorioSaida, "priorizacao-testes.md");

        await escreverJson(json, {
            categorias: {
                Services: {
                    untested: [
                        {
                            caminho_relativo: "sgc/mapa/service/MapaCriticoService.java",
                            evidencia_qualidade: "sem_evidencia_no_escopo"
                        }
                    ]
                },
                DTOs: {
                    untested: [
                        {
                            caminho_relativo: "sgc/mapa/dto/MapaRuidoCommand.java",
                            evidencia_qualidade: "ruido_dto_estrutural",
                            dto_ruido_ignorado: true
                        }
                    ]
                },
                Others: {
                    untested: [
                        {caminho_relativo: "sgc/comum/Mensagens.java", evidencia_qualidade: "fora_escopo_jacoco"},
                        {
                            caminho_relativo: "sgc/seguranca/AcaoPermissao.java",
                            evidencia_qualidade: "cobertura_indireta"
                        }
                    ]
                }
            }
        });

        const resultado = await executarScriptTestesPriorizar(["--entrada", json, "--saida", saida]);

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
