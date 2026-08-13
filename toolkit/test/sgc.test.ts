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
import {principal as coletarFotografiaQualidade, type AdaptadorQualidade} from "../qualidade/coleta-execucao.js";

interface VerificacaoDiagnosticoJson {
    nome: string;
    status?: string;
    detalhe?: string;
}

describe("CLI raiz do toolkit", () => {
    test("audita assuntos literais fora de AssuntosNotificacao", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-assuntos-auditar-"));
        const dir = path.join(base, "backend", "src", "main", "java", "sgc");

        await escreverArquivo(
            path.join(dir, "alerta", "AssuntosNotificacao.java"),
            [
                "package sgc.alerta;",
                "public final class AssuntosNotificacao {",
                "  public static String ok() {",
                "    return \"SGC: Assunto centralizado\";",
                "  }",
                "}"
            ].join("\n")
        );

        await escreverArquivo(
            path.join(dir, "diagnostico", "ServicoInvalido.java"),
            [
                "package sgc.diagnostico;",
                "class ServicoInvalido {",
                "  void enviar() {",
                "    String assunto = \"SGC: Assunto espalhado\";",
                "  }",
                "}"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "backend",
            "notificacoes",
            "auditar-assuntos",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(1);
        const corpo = JSON.parse(resultado.stdout);
        expect(corpo.resumo.arquivosComViolacao).toBe(1);
        expect(corpo.relatorio[0].arquivo).toBe("backend/src/main/java/sgc/diagnostico/ServicoInvalido.java");
        expect(corpo.relatorio[0].achados.some((item: {regra: string}) => item.regra === "literal_sgc")).toBe(true);
    });

    test("audita assuntos no diretorio backend configurado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-assuntos-configurado-"));
        const dir = path.join(base, "servidor", "java");
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {backendCodigo: "servidor/java"}
        });
        await escreverArquivo(
            path.join(dir, "diagnostico", "ServicoInvalido.java"),
            "class ServicoInvalido { void enviar() { String assunto = \"SGC: Assunto espalhado\"; } }\n"
        );

        const resultado = await executarSgc([
            "backend",
            "notificacoes",
            "auditar-assuntos",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(1);
        const corpo = JSON.parse(resultado.stdout);
        expect(corpo.resumo.arquivosComViolacao).toBe(1);
        expect(corpo.relatorio[0].arquivo).toBe("servidor/java/diagnostico/ServicoInvalido.java");
    });

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

    test("grava fotografia de qualidade na base externa", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-base-"));
        const adaptadores = ["testeExterno", "coberturaExterna"];
        const perfis = {externo: adaptadores};
        const adaptadoresFalsos: Record<string, AdaptadorQualidade> = {};
        for (const nome of adaptadores) {
            adaptadoresFalsos[nome] = async contexto => ({
                codigo: nome,
                nome,
                categoria: "qualidade",
                status: "sucesso",
                duracaoMs: 0,
                comando: "teste",
                diretorio: contexto.base,
                sumario: "",
                metricas: {},
                erros: [],
                artefatos: []
            });
        }

        const fotografia = await coletarFotografiaQualidade([
            "--perfil",
            "externo",
            "--base",
            base,
        ], {adaptadores: adaptadoresFalsos, perfis});

        const caminhoFotografia = path.join(
            base,
            "toolkit",
            "qualidade",
            "artefatos",
            "mais-recente",
            "fotografia.json"
        );
        expect(fotografia.resumo.statusGeral).toBe("verde");
        expect(fotografia.metadados.perfilExecucao).toBe("externo");
        expect(fotografia.verificacoes).toHaveLength(adaptadores.length);
        expect(await existe(caminhoFotografia)).toBe(true);
    });

    test("projeto diagnostico identifica corretamente a ausencia de arquivos essenciais e falha com codigo 1", async () => {
        const diretorioVazio = await mkdtemp(path.join(os.tmpdir(), "sgc-diagnostico-vazio-"));

        // Executamos o diagnostico passando o diretorio temporario vazio como base
        const resultado = await executarSgc(["projeto", "diagnostico", "--json", "--base", diretorioVazio]);

        // Como arquivos essenciais como gradlew e package.json estão ausentes, deve retornar código de erro 1
        expect(resultado.exitCode).toBe(1);

        const dados = JSON.parse(resultado.stdout);
        expect(dados.statusGeral).toBe("falha");
        expect(dados.totais.falha).toBeGreaterThan(0);

        // Verifica se um dos arquivos obrigatórios ausentes foi reportado como falha
        const falhaGradlew = dados.verificacoes.find((verificacao: VerificacaoDiagnosticoJson) => verificacao.nome === "gradlew");
        expect(falhaGradlew).toBeDefined();
        expect(falhaGradlew.status).toBe("falha");
        expect(falhaGradlew.detalhe).toContain("gradlew ausente");
    });

    test("lista com sucesso identificadores de teste do frontend em recorte controlado", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-testids-listar-"));

        // Criar arquivos .vue de teste com identificadores
        await escreverArquivo(
            path.join(diretorioBase, "ComponenteA.vue"),
            "<template><button data-test-codigo=\"btn-salvar\">Salvar</button></template>"
        );
        await escreverArquivo(
            path.join(diretorioBase, "ComponenteB.vue"),
            "<template><input data-testid=\"input-nome\" /></template>"
        );

        const resultado = await executarSgc(["frontend", "identificadores-teste", "listar", "--diretorio", diretorioBase]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("ComponenteA.vue");
        expect(resultado.stdout).toContain("btn-salvar");
        expect(resultado.stdout).toContain("ComponenteB.vue");
        expect(resultado.stdout).toContain("input-nome");
    });

    test("resolve frontendCodigo configurado para identificadores de teste", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testids-configurado-"));
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {frontendCodigo: "aplicacao/src"},
        });
        await escreverArquivo(
            path.join(base, "aplicacao", "src", "components", "Componente.vue"),
            "<template><button data-testid=\"btn-configurado\">Salvar</button></template>"
        );

        const resultado = await executarSgc(["frontend", "identificadores-teste", "listar", "--base", base]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("aplicacao/src/components/Componente.vue");
        expect(resultado.stdout).toContain("btn-configurado");
    });

    test("detecta corretamente identificadores de teste duplicados e falha com codigo 1", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-testids-duplicados-"));

        // Criar dois arquivos com o mesmo test-id
        await escreverArquivo(
            path.join(diretorioBase, "ComponenteX.vue"),
            "<template><button data-testid=\"btn-acao\">Ação X</button></template>"
        );
        await escreverArquivo(
            path.join(diretorioBase, "ComponenteY.vue"),
            "<template><div data-testid=\"btn-acao\">Ação Y</div></template>"
        );

        const resultado = await executarSgc(["frontend", "identificadores-teste", "listar-duplicados", "--diretorio", diretorioBase]);

        // O script deve falhar com exitCode 1 quando encontra duplicados
        expect(resultado.exitCode).toBe(1);
        expect(resultado.stdout).toContain("Identificadores de teste duplicados encontrados");
        expect(resultado.stdout).toContain("btn-acao");
        expect(resultado.stdout).toContain("ComponenteX.vue");
        expect(resultado.stdout).toContain("ComponenteY.vue");
    });

    test("passa com sucesso se nao houver identificadores de teste duplicados", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-testids-unicos-"));

        await escreverArquivo(
            path.join(diretorioBase, "ComponenteUnico.vue"),
            "<template><button data-testid=\"btn-unico\">Ação Única</button></template>"
        );

        const resultado = await executarSgc(["frontend", "identificadores-teste", "listar-duplicados", "--diretorio", diretorioBase]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Nenhum identificador de teste duplicado encontrado.");
    });
}, 30000);
