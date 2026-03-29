import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import fs from "fs-extra";
import {describe, expect, test} from "vitest";
import {execaNode} from "execa";

const DIRETORIO_RAIZ = path.resolve(import.meta.dirname, "..", "..", "..");
const CAMINHO_SGC = path.join(DIRETORIO_RAIZ, "etc", "scripts", "sgc.js");
const CAMINHO_TESTES_PRIORIZAR = path.join(DIRETORIO_RAIZ, "etc", "scripts", "backend", "testes-priorizar.cjs");
const FIXTURE_SNAPSHOT = path.join(DIRETORIO_RAIZ, "etc", "scripts", "test", "fixtures", "qa", "snapshot.json");
const CAMINHO_FRONTEND_COBERTURA_VERIFICAR = path.join(DIRETORIO_RAIZ, "etc", "scripts", "frontend", "cobertura-verificar.cjs");
const DIRETORIO_SCRIPTS_BACKEND_LEGADO = path.join(DIRETORIO_RAIZ, "backend", "etc", "scripts");
const DIRETORIO_SCRIPTS_FRONTEND_LEGADO = path.join(DIRETORIO_RAIZ, "frontend", "etc", "scripts");

async function executarSgc(args, opcoes = {}) {
    return execaNode(CAMINHO_SGC, args, {
        cwd: DIRETORIO_RAIZ,
        reject: false,
        ...opcoes
    });
}

async function executarScriptFrontendCobertura(args, opcoes = {}) {
    return execaNode(CAMINHO_FRONTEND_COBERTURA_VERIFICAR, args, {
        cwd: DIRETORIO_RAIZ,
        reject: false,
        ...opcoes
    });
}

async function executarScriptTestesPriorizar(args, opcoes = {}) {
    return execaNode(CAMINHO_TESTES_PRIORIZAR, args, {
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

    test("analisa testes do backend com resumo no console e sidecar JSON", async () => {
        const diretorioSaida = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analisar-"));
        const markdown = path.join(diretorioSaida, "relatorio.md");
        const json = path.join(diretorioSaida, "relatorio.json");

        const resultado = await executarSgc(["backend", "testes", "analisar", "--output", markdown, "--output-json", json]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Resumo:");
        expect(resultado.stdout).toContain("Repositories:");
        expect(resultado.stdout).toContain("Cobertura indireta:");
        expect(resultado.stdout).toContain("DTOs:");
        expect(await fs.pathExists(markdown)).toBe(true);
        expect(await fs.pathExists(json)).toBe(true);

        const conteudoJson = await fs.readJson(json);
        expect(conteudoJson.estatisticas.total_classes).toBeGreaterThan(0);
        expect(typeof conteudoJson.estatisticas.classes_com_cobertura_indireta).toBe("number");
        expect(typeof conteudoJson.estatisticas.classes_sem_evidencia_no_escopo).toBe("number");
        expect(typeof conteudoJson.estatisticas.classes_fora_escopo_jacoco).toBe("number");
        expect(typeof conteudoJson.estatisticas.classes_ruido_ignorado).toBe("number");
        expect(conteudoJson.categorias.Repositories.tested.length).toBeGreaterThanOrEqual(1);
    });

    test("ignora DTOs estruturais e contratuais do backlog real", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analise-dto-"));
        const backendDir = path.join(base, "backend-fake");
        const dtoDir = path.join(backendDir, "src", "main", "java", "sgc", "exemplo", "dto");
        const markdown = path.join(base, "relatorio.md");
        const json = path.join(base, "relatorio.json");

        await fs.outputFile(
            path.join(dtoDir, "DtoEstrutural.java"),
            "package sgc.exemplo.dto; public record DtoEstrutural(Long codigo, String nome) {}"
        );
        await fs.outputFile(
            path.join(dtoDir, "RequestContratual.java"),
            "package sgc.exemplo.dto; import jakarta.validation.constraints.NotBlank; public record RequestContratual(@NotBlank String nome) {}"
        );
        await fs.outputFile(
            path.join(dtoDir, "DtoComportamental.java"),
            "package sgc.exemplo.dto; public class DtoComportamental { public static DtoComportamental of(String valor) { return new DtoComportamental(); } }"
        );

        const resultado = await executarSgc([
            "backend",
            "testes",
            "analisar",
            "--dir",
            backendDir,
            "--output",
            markdown,
            "--output-json",
            json
        ], {cwd: base});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("DTOs: 0/1 testados no backlog real (2 ignorados)");

        const conteudoJson = await fs.readJson(json);
        expect(conteudoJson.estatisticas.dtos_comportamentais).toBe(1);
        expect(conteudoJson.estatisticas.dtos_estruturais).toBe(2);
        expect(conteudoJson.estatisticas.dtos_estruturais_contratuais).toBe(1);
        expect(conteudoJson.estatisticas.classes_ruido_ignorado).toBe(2);

        const dtoUntested = conteudoJson.categorias.DTOs.untested;
        expect(dtoUntested.find((item) => item.classe === "DtoEstrutural").dto_ruido_ignorado).toBe(true);
        expect(dtoUntested.find((item) => item.classe === "RequestContratual").perfil_dto).toBe("estrutural_contrato");
        expect(dtoUntested.find((item) => item.classe === "DtoComportamental").dto_ruido_ignorado).toBe(false);
    });

    test("ignora models estruturais e contratuais do backlog real", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analise-model-"));
        const backendDir = path.join(base, "backend-fake");
        const modelDir = path.join(backendDir, "src", "main", "java", "sgc", "exemplo", "model");
        const markdown = path.join(base, "relatorio.md");
        const json = path.join(base, "relatorio.json");

        await fs.outputFile(
            path.join(modelDir, "SituacaoExemplo.java"),
            "package sgc.exemplo.model; public enum SituacaoExemplo { ATIVO }"
        );
        await fs.outputFile(
            path.join(modelDir, "AnotacaoExemplo.java"),
            "package sgc.exemplo.model; import java.lang.annotation.*; public @interface AnotacaoExemplo { String value() default \"\"; }"
        );
        await fs.outputFile(
            path.join(modelDir, "ProcessoExemplo.java"),
            "package sgc.exemplo.model; import java.util.*; public class ProcessoExemplo { public void sincronizar(Set<Long> codigos) { if (codigos.isEmpty()) return; codigos.stream().filter(Objects::nonNull).toList(); } }"
        );

        const resultado = await executarSgc([
            "backend",
            "testes",
            "analisar",
            "--dir",
            backendDir,
            "--output",
            markdown,
            "--output-json",
            json
        ], {cwd: base});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Models: 0/1 testados no backlog real (2 ignorados)");

        const conteudoJson = await fs.readJson(json);
        expect(conteudoJson.estatisticas.models_comportamentais).toBe(1);
        expect(conteudoJson.estatisticas.models_estruturais).toBe(2);
        expect(conteudoJson.estatisticas.models_estruturais_contratuais).toBe(1);

        const modelUntested = conteudoJson.categorias.Models.untested;
        expect(modelUntested.find((item) => item.classe === "SituacaoExemplo").model_ruido_ignorado).toBe(true);
        expect(modelUntested.find((item) => item.classe === "AnotacaoExemplo").perfil_model).toBe("estrutural_contrato");
        expect(modelUntested.find((item) => item.classe === "ProcessoExemplo").model_ruido_ignorado).toBe(false);
    });

    test("ignora others estruturais e contratuais do backlog real e reclassifica commands como DTOs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analise-others-"));
        const backendDir = path.join(base, "backend-fake");
        const otherDir = path.join(backendDir, "src", "main", "java", "sgc", "exemplo");
        const dtoDir = path.join(otherDir, "dto");
        const markdown = path.join(base, "relatorio.md");
        const json = path.join(base, "relatorio.json");

        await fs.outputFile(
            path.join(otherDir, "Mensagens.java"),
            "package sgc.exemplo; public final class Mensagens { private Mensagens() {} public static final String OI = \"oi\"; }"
        );
        await fs.outputFile(
            path.join(otherDir, "AnotacaoSegura.java"),
            "package sgc.exemplo; public @interface AnotacaoSegura {}"
        );
        await fs.outputFile(
            path.join(otherDir, "LimitadorExemplo.java"),
            "package sgc.exemplo; import java.util.*; public class LimitadorExemplo { public void verificar(String valor) { if (valor.isBlank()) return; List.of(valor).stream().toList(); } }"
        );
        await fs.outputFile(
            path.join(dtoDir, "WorkflowCommand.java"),
            "package sgc.exemplo.dto; public record WorkflowCommand(String nome) {}"
        );

        const resultado = await executarSgc([
            "backend",
            "testes",
            "analisar",
            "--dir",
            backendDir,
            "--output",
            markdown,
            "--output-json",
            json
        ], {cwd: base});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Others: 0/1 testados no backlog real (2 ignorados)");
        expect(resultado.stdout).toContain("DTOs: 0/0 testados no backlog real (1 ignorados)");

        const conteudoJson = await fs.readJson(json);
        expect(conteudoJson.estatisticas.others_comportamentais).toBe(1);
        expect(conteudoJson.estatisticas.others_estruturais).toBe(2);
        expect(conteudoJson.estatisticas.others_estruturais_contratuais).toBe(1);

        const otherUntested = conteudoJson.categorias.Others.untested;
        expect(otherUntested.find((item) => item.classe === "Mensagens").other_ruido_ignorado).toBe(true);
        expect(otherUntested.find((item) => item.classe === "AnotacaoSegura").perfil_other).toBe("estrutural_contrato");
        expect(otherUntested.find((item) => item.classe === "LimitadorExemplo").other_ruido_ignorado).toBe(false);

        const dtoUntested = conteudoJson.categorias.DTOs.untested;
        expect(dtoUntested.find((item) => item.classe === "WorkflowCommand").dto_ruido_ignorado).toBe(true);
    });

    test("classifica separadamente teste dedicado, cobertura indireta, sem evidencia e fora do escopo", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-analise-jacoco-"));
        const backendDir = path.join(base, "backend-fake");
        const srcDir = path.join(backendDir, "src", "main", "java", "sgc", "exemplo");
        const testDir = path.join(backendDir, "src", "test", "java", "sgc", "exemplo");
        const markdown = path.join(base, "relatorio.md");
        const json = path.join(base, "relatorio.json");
        const jacoco = path.join(base, "jacoco.xml");

        await fs.outputFile(path.join(srcDir, "ClasseDireta.java"), "package sgc.exemplo; public class ClasseDireta {}");
        await fs.outputFile(path.join(srcDir, "ClasseIndireta.java"), "package sgc.exemplo; public class ClasseIndireta {}");
        await fs.outputFile(path.join(srcDir, "ClasseSemEvidencia.java"), "package sgc.exemplo; public class ClasseSemEvidencia {}");
        await fs.outputFile(path.join(srcDir, "ClasseForaEscopo.java"), "package sgc.exemplo; public class ClasseForaEscopo {}");
        await fs.outputFile(path.join(testDir, "ClasseDiretaTest.java"), "package sgc.exemplo; class ClasseDiretaTest {}");
        await fs.outputFile(jacoco, `
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
            "--dir",
            backendDir,
            "--output",
            markdown,
            "--output-json",
            json,
            "--jacoco-xml",
            jacoco
        ], {cwd: base});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Cobertura indireta: 1");
        expect(resultado.stdout).toContain("Sem evidencia no escopo: 1");
        expect(resultado.stdout).toContain("Fora do escopo do JaCoCo: 1");

        const conteudoJson = await fs.readJson(json);
        expect(conteudoJson.estatisticas.classes_com_teste_dedicado).toBe(1);
        expect(conteudoJson.estatisticas.classes_com_cobertura_indireta).toBe(1);
        expect(conteudoJson.estatisticas.classes_sem_evidencia_no_escopo).toBe(1);
        expect(conteudoJson.estatisticas.classes_fora_escopo_jacoco).toBe(1);

        const others = conteudoJson.categorias.Others;
        expect(others.tested).toHaveLength(1);
        expect(others.untested).toHaveLength(3);
        expect(others.untested.find((item) => item.classe === "ClasseIndireta").coberta_somente_indiretamente).toBe(true);
        expect(others.untested.find((item) => item.classe === "ClasseSemEvidencia").evidencia_qualidade).toBe("sem_evidencia_no_escopo");
        expect(others.untested.find((item) => item.classe === "ClasseForaEscopo").fora_escopo_jacoco).toBe(true);
    });

    test("prioriza testes usando sidecar JSON automaticamente quando disponivel", async () => {
        const diretorioSaida = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-priorizar-"));
        const markdown = path.join(diretorioSaida, "unit-test-report.md");
        const json = path.join(diretorioSaida, "unit-test-report.json");
        const saida = path.join(diretorioSaida, "prioritized-tests.md");

        await fs.writeFile(markdown, "# Relatorio simplificado\n");
        await fs.writeJson(json, {
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

        const resultado = await executarScriptTestesPriorizar(["--output", saida], {cwd: diretorioSaida});

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Entrada utilizada: unit-test-report.json");
        expect(resultado.stdout).toContain("Encontrados 1 P1, 0 P2, 1 P3");

        const conteudo = await fs.readFile(saida, "utf-8");
        expect(conteudo).toContain("sgc/mapa/service/MapaCriticoService.java");
        expect(conteudo).toContain("sgc/mapa/model/CompetenciaRepo.java");
    });

    test("prioriza apenas backlog acionavel do JSON e preserva evidencia", async () => {
        const diretorioSaida = await mkdtemp(path.join(os.tmpdir(), "sgc-testes-priorizar-real-"));
        const json = path.join(diretorioSaida, "unit-test-report.json");
        const saida = path.join(diretorioSaida, "prioritized-tests.md");

        await fs.writeJson(json, {
            categorias: {
                Services: {
                    untested: [
                        {caminho_relativo: "sgc/mapa/service/MapaCriticoService.java", evidencia_qualidade: "sem_evidencia_no_escopo"}
                    ]
                },
                DTOs: {
                    untested: [
                        {caminho_relativo: "sgc/mapa/dto/MapaRuidoCommand.java", evidencia_qualidade: "ruido_dto_estrutural", dto_ruido_ignorado: true}
                    ]
                },
                Others: {
                    untested: [
                        {caminho_relativo: "sgc/comum/Mensagens.java", evidencia_qualidade: "fora_escopo_jacoco"},
                        {caminho_relativo: "sgc/seguranca/AcaoPermissao.java", evidencia_qualidade: "cobertura_indireta"}
                    ]
                }
            }
        });

        const resultado = await executarScriptTestesPriorizar(["--input", json, "--output", saida]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Encontrados 1 P1, 0 P2, 1 P3");

        const conteudo = await fs.readFile(saida, "utf-8");
        expect(conteudo).toContain("sgc/mapa/service/MapaCriticoService.java");
        expect(conteudo).toContain("sem evidência");
        expect(conteudo).toContain("sgc/seguranca/AcaoPermissao.java");
        expect(conteudo).toContain("cobertura indireta");
        expect(conteudo).not.toContain("Mensagens.java");
        expect(conteudo).not.toContain("MapaRuidoCommand.java");
    });

    test("resume um snapshot de QA a partir de fixture", async () => {
        const resultado = await executarSgc(["qa", "resumo", "--json", "--arquivo", FIXTURE_SNAPSHOT]);
        expect(resultado.exitCode).toBe(0);

        const json = JSON.parse(resultado.stdout);
        expect(json.resumo.statusGeral).toBe("verde");
        expect(json.hotspots).toHaveLength(2);
    });

    test("exibe ajuda de coleta de snapshot com opcao de perfil", async () => {
        const resultado = await executarSgc(["qa", "snapshot", "coletar", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Perfil de execucao");
        expect(resultado.stdout).toContain("rapido");
    });

    test("falha rapido para perfil invalido de snapshot", async () => {
        const resultado = await executarSgc(["qa", "snapshot", "coletar", "--perfil", "inexistente"]);
        expect(resultado.exitCode).toBe(1);
        expect(resultado.stderr).toContain("Perfil invalido");
    });

    test("despacha ajuda de um comando migrado do frontend", async () => {
        const resultado = await executarSgc(["frontend", "mensagens", "extrair", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Extrai mensagens do projeto.");
    });

    test("exibe ajuda padronizada no script frontend cobertura verificar", async () => {
        const resultado = await executarScriptFrontendCobertura(["--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("--json");
        expect(resultado.stdout).toContain("--min=<n>");
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

    test("nao possui diretorios legados de scripts em backend/frontend", async () => {
        expect(await fs.pathExists(DIRETORIO_SCRIPTS_BACKEND_LEGADO)).toBe(false);
        expect(await fs.pathExists(DIRETORIO_SCRIPTS_FRONTEND_LEGADO)).toBe(false);
    });
});
