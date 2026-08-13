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
    copiar,
    alterarPermissoes,
    type ResultadoExecucao
} from "./apoio.js";
import {resolverCaminhoConfigurado, VERSAO_CONFIGURACAO} from "../lib/configuracao.js";
import {principal as coletarFotografiaQualidade, type AdaptadorQualidade} from "../qualidade/coleta-execucao.js";
import {normalizarCaminhoAchado, obterComandoSemgrep, resolverDiretoriosPadrao} from "../codigo/semgrep-auditar.js";
import {executarAuditoria as executarAuditoriaCheiros} from "../codigo/cheiros-auditar.js";

const CAMINHO_TESTES_PRIORIZAR = path.join(DIRETORIO_RAIZ, "toolkit", "backend", "testes-priorizar.ts");

type ObjetoJson = Record<string, unknown>;

interface RelatorioAnaliseTestesJson {
    backend_dir: string;
    estatisticas: Record<string, number>;
    categorias: Record<string, {tested: ObjetoJson[]; untested: ObjetoJson[]}>;
}

interface PontoArquiteturalJson {
    arquivo: string;
    sinaisAtivos: string[];
    hubCentral?: boolean;
}

interface ViolacaoJson {
    regra: string;
}

interface VerificacaoDiagnosticoJson {
    nome: string;
    status?: string;
    detalhe?: string;
}

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

    test("trata politicas de residuos como overrides opcionais e explicita arquivos invalidos", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-residuos-politicas-opcionais-"));
        const caminhoOrcamento = path.join(base, "politicas", "orcamento.json");
        const caminhoExcecoes = path.join(base, "politicas", "excecoes.json");
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                orcamentoResiduosFrontend: "politicas/orcamento.json",
                excecoesResiduosFrontend: "politicas/excecoes.json"
            }
        });
        await escreverJson(caminhoOrcamento, {
            versaoSchema: "1.0.0",
            camadas: {},
            metricas: {maximosProducao: {}}
        });
        await escreverJson(caminhoExcecoes, {versaoSchema: "1.0.0", excecoes: []});

        const resultado = await executarSgc([
            "frontend", "residuos", "validar", "--json", "--base", base
        ]);
        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.orcamento).toBe(path.relative(base, caminhoOrcamento));
        expect(conteudo.excecoes).toBe(path.relative(base, caminhoExcecoes));
        const gravacao = await executarSgc([
            "frontend", "residuos", "validar", "--json", "--gravar", "--base", base
        ]);
        expect(gravacao.exitCode).toBe(0);
        expect(await existe(path.join(base, "toolkit", "qualidade", "artefatos", "frontend-residuos", "mais-recente", "fotografia.json"))).toBe(true);

        await escreverArquivo(caminhoOrcamento, "{");
        const falha = await executarSgc([
            "frontend", "residuos", "validar", "--json", "--base", base
        ]);
        expect(falha.exitCode).toBe(1);
        expect(`${falha.stdout}\n${falha.stderr}`).toContain("Nao foi possivel ler a politica de residuos");
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

    test("audita residuos do frontend em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-residuos-auditar-"));
        const frontendDir = path.join(base, "frontend", "src");
        const orcamento = path.join(base, "orcamento.json");

        await escreverJson(orcamento, {
            versaoSchema: "1.0.0",
            camadas: {
                service: {meta: 4, limite: 8},
                component: {meta: 6, limite: 10},
                outro: {meta: 6, limite: 10}
            },
            metricas: {
                maximosProducao: {
                    anyExplicito: 1,
                    checksNull: 1,
                    fallbacksDefensivos: 1,
                    catchBlocks: 1,
                    castsDuplos: 0,
                    storageDireto: 1,
                    exportacoesSuspeitas: 1,
                    arquivosAcimaMetaPorCamada: {
                        service: 1,
                        component: 1,
                        outro: 0
                    }
                }
            }
        });

        await escreverArquivo(
            path.join(frontendDir, "services", "exemploService.ts"),
            [
                "export function exemploService(valor: any) {",
                "  if (valor === null) {",
                "    return valor || [];",
                "  }",
                "  return valor;",
                "}",
            ].join("\n")
        );
        await escreverArquivo(
            path.join(frontendDir, "components", "ExemploCard.vue"),
            [
                "<script setup lang=\"ts\">",
                "const salvar = () => localStorage.setItem('chave', 'valor');",
                "</script>",
                "<template><button @click=\"salvar\">Salvar</button></template>"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "frontend",
            "residuos",
            "auditar",
            "--json",
            "--base",
            base,
            "--orcamento",
            "orcamento.json"
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.contagens.producao.anyExplicito).toBe(1);
        expect(conteudo.contagens.producao.checksNull).toBe(1);
        expect(conteudo.contagens.producao.fallbacksDefensivos).toBe(1);
        expect(conteudo.contagens.producao.storageDireto).toBe(1);
        expect(conteudo.contagens.producao.exportacoesSuspeitas).toBe(1);
        expect(conteudo.contagens.producao.arquivosAcimaMeta.service).toBe(1);

        const gravacao = await executarSgc([
            "frontend",
            "residuos",
            "auditar",
            "--json",
            "--gravar",
            "--base",
            base,
            "--orcamento",
            "orcamento.json",
            "--saida",
            "artefatos/residuos"
        ]);
        expect(gravacao.exitCode).toBe(0);
        expect(await existe(path.join(base, "artefatos", "residuos", "fotografia.json"))).toBe(true);
    });

    test("calcula a saida padrao de residuos a partir da base externa", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-residuos-saida-base-"));
        await escreverArquivo(
            path.join(base, "frontend", "src", "exemplo.ts"),
            "export function carregarExemplo(codigo: string) { return codigo; }\n"
        );

        const resultado = await executarSgc([
            "frontend",
            "residuos",
            "auditar",
            "--json",
            "--gravar",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const fotografia = JSON.parse(resultado.stdout);
        expect(fotografia.base).toBe(base);
        expect(await existe(path.join(
            base,
            "toolkit",
            "qualidade",
            "artefatos",
            "frontend-residuos",
            "mais-recente",
            "fotografia.json"
        ))).toBe(true);
    });

    test("classifica residuos usando frontendCodigo configurado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-residuos-codigo-configurado-"));
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {frontendCodigo: "cliente/codigo"},
        });
        await escreverArquivo(
            path.join(base, "cliente", "codigo", "services", "exemploService.ts"),
            "export function carregarExemplo(codigo: string) { return codigo; }\n"
        );

        const resultado = await executarSgc([
            "frontend",
            "residuos",
            "auditar",
            "--json",
            "--base",
            base,
        ]);

        expect(resultado.exitCode).toBe(0);
        const fotografia = JSON.parse(resultado.stdout);
        expect(fotografia.resumo.arquivosProducao).toBe(1);
        expect(fotografia.arquivos[0].arquivo).toBe("cliente/codigo/services/exemploService.ts");
        expect(fotografia.arquivos[0].camada).toBe("service");
    });

    test("audita vazamentos arquiteturais do frontend em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-auditar-"));
        const frontendDir = path.join(base, "frontend", "src");

        await escreverArquivo(path.join(frontendDir, "stores", "unidade.ts"), "export const useUnidadeStore = () => ({ invalidar: () => undefined, obterUnidade: () => undefined, recarregarUnidade: () => undefined, dadosEdicaoValidos: () => true, sincronizarUnidade: () => undefined, marcarUnidadeParaAtualizacao: () => undefined, limparContextoAtual: () => undefined, resetar: () => undefined, contextoAtual: null, erroAtual: null, carregando: false });");
        await escreverArquivo(path.join(frontendDir, "services", "unidadeService.ts"), "export async function buscarUnidade() { return null; }");
        await escreverArquivo(path.join(frontendDir, "composables", "useUnidadeTela.ts"), "export function useUnidadeTela() { return { carregar: () => undefined }; }");
        await escreverArquivo(path.join(frontendDir, "router", "unidade.routes.ts"), "export const rotasUnidade = [];");

        await escreverArquivo(
            path.join(frontendDir, "views", "UnidadeView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {buscarUnidade} from '@/services/unidadeService';",
                "import {useUnidadeTela} from '@/composables/useUnidadeTela';",
                "import {rotasUnidade} from '@/router/unidade.routes';",
                "const unidadeStore = useUnidadeStore();",
                "const unidadeTela = useUnidadeTela();",
                "function carregarDados(forcar = false) {",
                "  unidadeTela.carregar();",
                "  unidadeStore.obterUnidade(1, true);",
                "  unidadeStore.recarregarUnidade(1);",
                "  unidadeStore.invalidar();",
                "  return buscarUnidade();",
                "}",
                "const emCache = unidadeStore.cacheUnidades.get(1);",
                "const stale = false;",
                "console.log(rotasUnidade);",
                "</script>"
            ].join("\n")
        );
        await escreverArquivo(
            path.join(frontendDir, "composables", "useCadastroUnidade.ts"),
            [
                "interface DependenciasCadastroUnidade {",
                "  alpha: string;",
                "  beta: string;",
                "  gamma: string;",
                "  delta: string;",
                "  epsilon: string;",
                "  zeta: string;",
                "}",
                "export function useCadastroUnidade() {",
                "  return {",
                "    a: 1,",
                "    b: 2,",
                "    c: 3,",
                "    d: 4,",
                "    e: 5,",
                "    f: 6,",
                "    g: 7,",
                "    h: 8,",
                "    i: 9,",
                "  };",
                "}",
            ].join("\n")
        );

        const resultado = await executarSgc([
            "frontend",
            "arquitetura",
            "auditar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.resumo.metricas.viewsComVazamentoCache).toBe(1);
        expect(conteudo.resumo.metricas.acessosDiretosCache).toBe(1);
        expect(conteudo.resumo.metricas.booleanosPosicionais).toBe(1);
        expect(conteudo.resumo.metricas.ocorrenciasForcar).toBeGreaterThanOrEqual(1);
        expect(conteudo.resumo.metricas.viewsComServiceDireto).toBe(1);
        expect(conteudo.resumo.metricas.viewsComServerStateCaseiro).toBe(1);
        expect(conteudo.resumo.metricas.viewsComFanoutAlto).toBe(1);
        expect(conteudo.resumo.metricas.arquivosComBolsaDependenciasLarga).toBe(1);
        expect(conteudo.resumo.metricas.arquivosComSuperficieAmpla).toBe(1);
        expect(conteudo.resumo.metricas.arquivosComMisturaCamadas).toBe(1);
        expect(conteudo.resumo.metricas.arquivosComServerStateCaseiro).toBe(1);
        expect(conteudo.hotspots[0].arquivo).toBe("frontend/src/views/UnidadeView.vue");
        expect(conteudo.hotspots[0].sinaisAtivos).toContain("serverStateCaseiro");
        expect(conteudo.hotspots.some((hotspot: PontoArquiteturalJson) => hotspot.hubCentral && hotspot.sinaisAtivos.includes("superficieAmpla"))).toBe(false);
        expect(await existe(path.join(base, "toolkit", "qualidade", "artefatos", "frontend-arquitetura"))).toBe(false);

        const diretorioSaida = path.join("artefatos", "arquitetura");
        const gravacao = await executarSgc([
            "frontend", "arquitetura", "auditar", "--json", "--gravar", "--saida", diretorioSaida, "--base", base
        ]);
        expect(gravacao.exitCode).toBe(0);
        expect(await existe(path.join(base, diretorioSaida, "fotografia.json"))).toBe(true);
        expect(await existe(path.join(base, diretorioSaida, "resumo.md"))).toBe(true);
    });

    test("calcula a saida padrao de arquitetura a partir da base externa", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-saida-base-"));
        await escreverArquivo(
            path.join(base, "frontend", "src", "exemplo.ts"),
            "export function carregarExemplo(codigo: string) { return codigo; }\n"
        );

        const resultado = await executarSgc([
            "frontend",
            "arquitetura",
            "auditar",
            "--json",
            "--gravar",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const fotografia = JSON.parse(resultado.stdout);
        expect(fotografia.base).toBe(base);
        expect(await existe(path.join(
            base,
            "toolkit",
            "qualidade",
            "artefatos",
            "frontend-arquitetura",
            "mais-recente",
            "fotografia.json"
        ))).toBe(true);
    });

    test("analisa arquitetura usando frontendCodigo configurado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-codigo-configurado-"));
        const frontendDir = path.join(base, "cliente", "codigo");
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {frontendCodigo: "cliente/codigo"},
        });
        await escreverArquivo(
            path.join(frontendDir, "services", "exemploService.ts"),
            "export async function buscarExemplo() { return null; }"
        );
        await escreverArquivo(
            path.join(frontendDir, "views", "ExemploView.vue"),
            "<script setup lang=\"ts\">import {buscarExemplo} from '@/services/exemploService'; void buscarExemplo();</script>"
        );

        const resultado = await executarSgc([
            "frontend",
            "arquitetura",
            "auditar",
            "--json",
            "--base",
            base,
        ]);

        expect(resultado.exitCode).toBe(0);
        const fotografia = JSON.parse(resultado.stdout);
        expect(fotografia.resumo.arquivosProducao).toBe(2);
        expect(fotografia.resumo.metricas.viewsComServiceDireto).toBe(1);
        expect(fotografia.hotspots[0].arquivo).toBe("cliente/codigo/views/ExemploView.vue");
    });

    test("tipos internos de store nao disparam bolsaDependenciasLarga", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-contexto-store-"));
        await escreverArquivo(
            path.join(base, "frontend", "src", "stores", "subprocesso", "tipos.ts"),
            `export type ConfiguracaoContexto<T> = {
                tipoCodigo: string;
                tipoProcessoUnidade: string;
                contextoRef: unknown;
                contextoInvalidoRef: unknown;
                codigosPorProcessoUnidade: unknown;
                buscarPorCodigo: () => Promise<unknown>;
                buscarPorProcessoEUnidade: () => Promise<unknown>;
                registrar: () => void;
                mensagemCodigo: () => string;
                mensagemProcessoUnidade: () => string;
            };`
        );
        const resultado = await executarSgc(["frontend", "arquitetura", "auditar", "--json", "--base", base]);
        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.resumo.metricas.arquivosComBolsaDependenciasLarga).toBe(0);
    });

    test("hub central nao dispara superficieAmpla", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-hub-central-"));
        const frontendDir = path.join(base, "frontend", "src");

        await escreverArquivo(
            path.join(frontendDir, "stores", "perfil.ts"),
            [
                "import {defineStore} from 'pinia';",
                "export const usePerfilStore = defineStore('perfil', () => {",
                "  return {",
                "    a: 1, b: 2, c: 3, d: 4, e: 5, f: 6, g: 7, h: 8, i: 9, j: 10, k: 11, l: 12,",
                "  };",
                "});",
                "export const perfilA = 1;",
                "export const perfilB = 2;",
                "export const perfilC = 3;",
                "export const perfilD = 4;",
                "export const perfilE = 5;",
                "export const perfilF = 6;",
                "export const perfilG = 7;",
                "export const perfilH = 8;",
                "export const perfilI = 9;",
                "export const perfilJ = 10;",
                "export const perfilK = 11;",
                "export const perfilL = 12;",
            ].join("\n")
        );

        const resultado = await executarSgc(["frontend", "arquitetura", "auditar", "--json", "--base", base]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        const hotspot = conteudo.hotspots.find((item: Pick<PontoArquiteturalJson, "arquivo">) => item.arquivo === "frontend/src/stores/perfil.ts");
        expect(hotspot).toBeUndefined();
    });

    test("gate arquitetural falha quando view importa service diretamente", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-gate-falha-"));
        const frontendDir = path.join(base, "frontend");

        await escreverJson(path.join(frontendDir, "package.json"), {name: "frontend-fixture", private: true});
        await escreverJson(path.join(frontendDir, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {
                    "@/*": ["./src/*"],
                },
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await escreverArquivo(path.join(frontendDir, "src", "services", "unidadeService.ts"), "export async function buscarUnidade() { return null; }");
        await escreverArquivo(
            path.join(frontendDir, "src", "views", "UnidadeView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {buscarUnidade} from '../services/unidadeService';",
                "void buscarUnidade();",
                "</script>",
            ].join("\n")
        );
        await copiar(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(frontendDir, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["frontend", "arquitetura", "validar", "--base", base]);
        expect(resultado.exitCode).not.toBe(0);
        expect(resultado.stdout).toContain("view-sem-service-direto");
    });

    test("gate arquitetural passa quando view usa composable", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-gate-ok-"));
        const frontendDir = path.join(base, "frontend");

        await escreverJson(path.join(frontendDir, "package.json"), {name: "frontend-fixture", private: true});
        await escreverJson(path.join(frontendDir, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {
                    "@/*": ["./src/*"],
                },
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await escreverArquivo(path.join(frontendDir, "src", "services", "unidadeService.ts"), "export async function buscarUnidade() { return null; }");
        await escreverArquivo(
            path.join(frontendDir, "src", "composables", "useUnidadeTela.ts"),
            [
                "import {buscarUnidade} from '../services/unidadeService';",
                "export function useUnidadeTela() {",
                "  return { carregar: () => buscarUnidade() };",
                "}",
            ].join("\n")
        );
        await escreverArquivo(
            path.join(frontendDir, "src", "views", "UnidadeView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {useUnidadeTela} from '../composables/useUnidadeTela';",
                "const tela = useUnidadeTela();",
                "void tela.carregar();",
                "</script>",
            ].join("\n")
        );
        await copiar(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(frontendDir, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["frontend", "arquitetura", "validar", "--base", base]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Nenhuma violacao arquitetural encontrada");
    });

    test("resolve diretorio frontend configurado no gate arquitetural", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-diretorio-configurado-"));
        const frontendDir = path.join(base, "cliente");

        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {frontend: "cliente"},
        });
        await escreverJson(path.join(frontendDir, "package.json"), {name: "frontend-fixture", private: true});
        await escreverJson(path.join(frontendDir, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {"@/*": ["./src/*"]},
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await escreverArquivo(path.join(frontendDir, "src", "services", "unidadeService.ts"), "export async function buscarUnidade() { return null; }");
        await escreverArquivo(
            path.join(frontendDir, "src", "composables", "useUnidadeTela.ts"),
            "import {buscarUnidade} from '../services/unidadeService'; export function useUnidadeTela() { return {carregar: () => buscarUnidade()}; }"
        );
        await escreverArquivo(
            path.join(frontendDir, "src", "views", "UnidadeView.vue"),
            "<script setup lang=\"ts\">import {useUnidadeTela} from '../composables/useUnidadeTela'; const tela = useUnidadeTela(); void tela.carregar();</script>"
        );
        await copiar(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(frontendDir, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["frontend", "arquitetura", "validar", "--base", base]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Nenhuma violacao arquitetural encontrada");
    });

    test("gate arquitetural falha quando frontend calcula habilitacao de acao por perfil ou situacao", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-acoes-backend-falha-"));
        const frontendDir = path.join(base, "frontend");

        await escreverJson(path.join(frontendDir, "package.json"), {name: "frontend-fixture", private: true});
        await escreverJson(path.join(frontendDir, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {
                    "@/*": ["./src/*"],
                },
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await escreverArquivo(
            path.join(frontendDir, "src", "views", "ConsensoView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {computed} from 'vue';",
                "import {Perfil} from '@/types/perfil';",
                "const perfilStore = { perfilSelecionado: Perfil.SERVIDOR };",
                "const consenso = { situacao: 'CONSENSO_CRIADO' };",
                "const habilitarAprovarConsenso = computed(() => perfilStore.perfilSelecionado === Perfil.SERVIDOR && consenso.situacao === 'CONSENSO_CRIADO');",
                "</script>",
            ].join("\n")
        );
        await copiar(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(frontendDir, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["frontend", "arquitetura", "validar", "--base", base]);
        expect(resultado.exitCode).not.toBe(0);
        expect(resultado.stdout).toContain("frontend-sem-regra-local-acoes");
        expect(resultado.stdout).toContain("habilitarAprovarConsenso");
    });

    test("gate arquitetural permite flag de acao vinda diretamente do backend", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-acoes-backend-ok-"));
        const frontendDir = path.join(base, "frontend");

        await escreverJson(path.join(frontendDir, "package.json"), {name: "frontend-fixture", private: true});
        await escreverJson(path.join(frontendDir, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {
                    "@/*": ["./src/*"],
                },
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await escreverArquivo(
            path.join(frontendDir, "src", "views", "ConsensoView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {computed} from 'vue';",
                "const query = { data: { value: { habilitarAprovarConsenso: true } } };",
                "const habilitarAprovarConsenso = computed(() => query.data.value.habilitarAprovarConsenso ?? false);",
                "</script>",
            ].join("\n")
        );
        await copiar(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(frontendDir, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["frontend", "arquitetura", "validar", "--base", base]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Nenhum calculo local novo de habilitacao/exibicao de acoes encontrado");
    });

    test("composable fachada de store não é penalizado por chamadasStore >= 8", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-facade-"));
        const frontendDir = path.join(base, "frontend", "src");

        await escreverArquivo(
            path.join(frontendDir, "stores", "perfil.ts"),
            "import {defineStore} from 'pinia'; export const usePerfilStore = defineStore('perfil', () => ({ a: 1, b: 2, c: 3, d: 4, e: 5, f: 6, g: 7, h: 8, i: 9, j: 10, k: 11, l: 12 }));"
        );

        // Composable que só delega para uma única store (fachada) — acessa a store 12 vezes
        await escreverArquivo(
            path.join(frontendDir, "composables", "usePerfil.ts"),
            [
                "import {computed} from 'vue';",
                "import {usePerfilStore} from '@/stores/perfil';",
                "export function usePerfil() {",
                "  const store = usePerfilStore();",
                "  return {",
                "    a: computed(() => store.a),",
                "    b: computed(() => store.b),",
                "    c: computed(() => store.c),",
                "    d: computed(() => store.d),",
                "    e: computed(() => store.e),",
                "    f: computed(() => store.f),",
                "    g: computed(() => store.g),",
                "    h: computed(() => store.h),",
                "    i: computed(() => store.i),",
                "    j: computed(() => store.j),",
                "    k: computed(() => store.k),",
                "    l: computed(() => store.l),",
                "  };",
                "}",
            ].join("\n")
        );

        const resultado = await executarSgc([
            "frontend", "arquitetura", "auditar", "--json", "--base", base,
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        const hotspot = conteudo.hotspots.find((ponto: Pick<PontoArquiteturalJson, "arquivo">) => ponto.arquivo.endsWith("usePerfil.ts"));
        // Fachada de store: acessar a store muitas vezes é esperado — sem penalidade
        expect(hotspot).toBeUndefined();
    });

    test("módulo em stores/ sem defineStore não é penalizado como store Pinia", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-nao-store-"));
        const frontendDir = path.join(base, "frontend", "src");

        await escreverArquivo(
            path.join(frontendDir, "services", "autenticacaoService.ts"),
            "export async function login() { return null; } export async function logout() { return null; } export async function renovar() { return null; }"
        );

        // Módulo de funções puras em stores/ que NÃO usa defineStore (orquestração de autenticação)
        await escreverArquivo(
            path.join(frontendDir, "stores", "autenticacao.ts"),
            [
                "import * as autenticacaoService from '@/services/autenticacaoService';",
                "export async function entrar() { return autenticacaoService.login(); }",
                "export async function sair() { return autenticacaoService.logout(); }",
                "export async function renovarSessao() { return autenticacaoService.renovar(); }",
            ].join("\n")
        );

        const resultado = await executarSgc([
            "frontend", "arquitetura", "auditar", "--json", "--base", base,
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        const hotspot = conteudo.hotspots.find((ponto: Pick<PontoArquiteturalJson, "arquivo">) => ponto.arquivo.endsWith("autenticacao.ts"));
        // Orquestração sem defineStore: chamar serviços é esperado — sem score nem sinal serviceDireto
        expect(hotspot).toBeUndefined();
    });

    test("composable que chama serviço diretamente não recebe sinal serviceDireto", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-composable-servico-"));
        const frontendDir = path.join(base, "frontend", "src");

        await escreverArquivo(
            path.join(frontendDir, "services", "itemService.ts"),
            "export async function buscarItens() { return []; }"
        );

        // Composable com superfície exportada ampla E chamada de serviço direta
        // → deve aparecer em hotspots pelo superficieAmpla, mas NÃO pelo serviceDireto
        await escreverArquivo(
            path.join(frontendDir, "composables", "useItens.ts"),
            [
                "import * as itemService from '@/services/itemService';",
                "export function useItens() {",
                "  return {",
                "    a: 1, b: 2, c: 3, d: 4, e: 5, f: 6, g: 7, h: 8, i: 9,",
                "    carregar: () => itemService.buscarItens(),",
                "  };",
                "}",
            ].join("\n")
        );

        const resultado = await executarSgc([
            "frontend", "arquitetura", "auditar", "--json", "--base", base,
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        const hotspot = conteudo.hotspots.find((ponto: Pick<PontoArquiteturalJson, "arquivo">) => ponto.arquivo.endsWith("useItens.ts"));
        // Composable aparece por superficieAmpla, mas chamar serviços não é sinalizado
        expect(hotspot).toBeDefined();
        expect(hotspot.sinaisAtivos).toContain("superficieAmpla");
        expect(hotspot.sinaisAtivos).not.toContain("serviceDireto");
    });

    test("valida residuos do frontend com excecao de tamanho", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-residuos-validar-"));
        const frontendDir = path.join(base, "frontend", "src");
        const orcamento = path.join(base, "orcamento.json");
        const excecoes = path.join(base, "excecoes.json");

        await escreverJson(orcamento, {
            versaoSchema: "1.0.0",
            camadas: {
                service: {meta: 3, limite: 6},
                outro: {meta: 6, limite: 10}
            },
            metricas: {
                maximosProducao: {
                    anyExplicito: 0,
                    checksNull: 0,
                    fallbacksDefensivos: 0,
                    catchBlocks: 0,
                    castsDuplos: 0,
                    storageDireto: 0,
                    exportacoesSuspeitas: 2,
                    arquivosAcimaMetaPorCamada: {
                        service: 1,
                        outro: 0
                    }
                }
            }
        });
        await escreverJson(excecoes, {
            versaoSchema: "1.0.0",
            excecoes: [
                {
                    arquivo: "frontend/src/services/exemploService.ts",
                    camada: "service",
                    maxLinhas: 6,
                    responsavel: "teste",
                    justificativa: "Congelamento de baseline",
                    criterioRemocao: "Reduzir o arquivo."
                }
            ]
        });
        await escreverArquivo(
            path.join(frontendDir, "services", "exemploService.ts"),
            [
                "export function exemploService() {",
                "  return 1;",
                "}",
                "export function outro() {",
                "  return 2;",
                "}"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "frontend",
            "residuos",
            "validar",
            "--json",
            "--base",
            base,
            "--orcamento",
            "orcamento.json",
            "--excecoes",
            "excecoes.json"
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.status).toBe("ok");
        expect(conteudo.violacoes).toEqual([]);
        expect(await existe(path.join(base, "toolkit", "qualidade", "artefatos", "frontend-residuos"))).toBe(false);
        const gravacao = await executarSgc([
            "frontend",
            "residuos",
            "validar",
            "--json",
            "--gravar",
            "--base",
            base,
            "--orcamento",
            "orcamento.json",
            "--excecoes",
            "excecoes.json"
        ]);
        expect(gravacao.exitCode).toBe(0);
        expect(await existe(path.join(base, "toolkit", "qualidade", "artefatos", "frontend-residuos", "mais-recente", "fotografia.json"))).toBe(true);
    });

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
        // language=XML
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

    test("valida previsibilidade estrutural das views em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-views-templates-"));
        const viewsDir = path.join(base, "frontend", "src", "views");

        await escreverArquivo(
            path.join(viewsDir, "PainelView.vue"),
            [
                "<template>",
                "  <LayoutPadrao>",
                "    <PageHeader title=\"Painel\" />",
                "  </LayoutPadrao>",
                "</template>"
            ].join("\n")
        );

        await escreverArquivo(
            path.join(viewsDir, "LegacyView.vue"),
            [
                "<template>",
                "  <div>",
                "    <BModal />",
                "  </div>",
                "</template>"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "frontend",
            "views",
            "templates-validar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(1);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.resumo.totalViews).toBe(2);
        expect(conteudo.violacoes.some((violacao: ViolacaoJson) => violacao.regra === "view-com-bmodal-cru")).toBe(true);
        expect(conteudo.violacoes.some((violacao: ViolacaoJson) => violacao.regra === "view-sem-layout-padrao")).toBe(true);
        expect(conteudo.violacoes.some((violacao: ViolacaoJson) => violacao.regra === "view-sem-cabecalho-padrao")).toBe(true);
    });

    test("valida padronizacao de modais em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-modais-validar-"));
        await escreverArquivo(
            path.join(base, "frontend", "src", "components", "comum", "ModalPadrao.vue"),
            "<template><BModal title=\"Base\" /></template>"
        );
        await escreverArquivo(
            path.join(base, "frontend", "src", "components", "mapa", "ImpactoMapaModal.vue"),
            "<template><BModal title=\"Impacto\" /></template>"
        );

        const resultado = await executarSgc([
            "frontend",
            "modais",
            "validar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(1);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.violacoes).toHaveLength(1);
        expect(conteudo.violacoes[0].arquivo).toBe("frontend/src/components/mapa/ImpactoMapaModal.vue");
        expect(conteudo.violacoes[0].regra).toBe("componente-com-bmodal-cru");
    });

    test("resolve frontendCodigo configurado nos validadores estruturais", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-frontend-validadores-configurados-"));
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {frontendCodigo: "cliente/src"},
        });
        await escreverArquivo(
            path.join(base, "cliente", "src", "views", "PainelView.vue"),
            "<template><LayoutPadrao><PageHeader title=\"Painel\" /></LayoutPadrao></template>"
        );
        await escreverArquivo(
            path.join(base, "cliente", "src", "components", "comum", "ModalPadrao.vue"),
            "<template><BModal title=\"Base\" /></template>"
        );

        const [resultadoViews, resultadoModais] = await Promise.all([
            executarSgc(["frontend", "views", "templates-validar", "--json", "--base", base]),
            executarSgc(["frontend", "modais", "validar", "--json", "--base", base]),
        ]);

        expect(resultadoViews.exitCode).toBe(0);
        expect(JSON.parse(resultadoViews.stdout).resumo.totalViews).toBe(1);
        expect(resultadoModais.exitCode).toBe(0);
        expect(JSON.parse(resultadoModais.stdout).resumo.totalViolacoes).toBe(0);
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
