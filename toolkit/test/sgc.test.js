import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import fs from "fs-extra";
import {describe, expect, test} from "vitest";
import {execaNode} from "execa";

const DIRETORIO_RAIZ = path.resolve(import.meta.dirname, "..", "..");
const CAMINHO_SGC = path.join(DIRETORIO_RAIZ, "toolkit", "sgc.js");
const CAMINHO_TESTES_PRIORIZAR = path.join(DIRETORIO_RAIZ, "toolkit", "backend", "testes-priorizar.js");
const FIXTURE_SNAPSHOT = path.join(DIRETORIO_RAIZ, "toolkit", "test", "fixtures", "qa", "snapshot.json");
const CAMINHO_FRONTEND_COBERTURA_AUDITORIA = path.join(DIRETORIO_RAIZ, "toolkit", "frontend", "cobertura-auditoria.js");
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
    return execaNode(CAMINHO_FRONTEND_COBERTURA_AUDITORIA, args, {
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

    test("despacha ajuda de um comando de auditoria do backend", async () => {
        const resultado = await executarSgc(["backend", "cobertura", "auditoria", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Auditoria unificada de cobertura e risco (Backend).");
    });

    test("despacha ajuda da jornada de cobertura do backend", async () => {
        const resultado = await executarSgc(["backend", "cobertura", "jornada", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Executa a jornada consolidada de cobertura do backend.");
    });

    test("despacha ajuda da auditoria de assuntos de notificacao do backend", async () => {
        const resultado = await executarSgc(["backend", "notificacoes", "auditar-assuntos", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Audita literais de assunto de notificacao fora de AssuntosNotificacao.");
    });

    test("audita assuntos literais fora de AssuntosNotificacao", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-assuntos-auditar-"));
        const dir = path.join(base, "backend", "src", "main", "java", "sgc");

        await fs.outputFile(
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

        await fs.outputFile(
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
        const corpo = JSON.parse(resultado.stdout.slice(resultado.stdout.indexOf("{")));
        expect(corpo.resumo.arquivosComViolacao).toBe(1);
        expect(corpo.relatorio[0].arquivo).toBe("backend/src/main/java/sgc/diagnostico/ServicoInvalido.java");
        expect(corpo.relatorio[0].achados.some(item => item.regra === "literal_sgc")).toBe(true);
    });

    test("audita cheiros de codigo em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-smells-"));
        const frontendDir = path.join(base, "frontend", "src");
        const backendDir = path.join(base, "backend", "src", "main", "java", "sgc", "exemplo", "dto");

        await fs.outputFile(
            path.join(frontendDir, "Exemplo.ts"),
            [
                "export function exemplo(valor: any) {",
                "  if (valor === null) return valor || [];",
                "  return valor as any;",
                "}"
            ].join("\n")
        );

        await fs.outputFile(
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
            "smells",
            "auditar",
            "--json",
            "--sem-gravar",
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
    });

    test("audita cruft do frontend em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cruft-auditar-"));
        const frontendDir = path.join(base, "frontend", "src");
        const budget = path.join(base, "budget.json");

        await fs.outputJson(budget, {
            versaoSchema: "1.0.0",
            camadas: {
                service: {target: 4, hard: 8},
                component: {target: 6, hard: 10},
                outro: {target: 6, hard: 10}
            },
            metricas: {
                maximosProducao: {
                    anyExplicito: 1,
                    checksNull: 1,
                    fallbacksDefensivos: 1,
                    catchBlocks: 1,
                    castsDuplos: 0,
                    storageDireto: 1,
                    exportsSuspeitos: 1,
                    arquivosAcimaTargetPorCamada: {
                        service: 1,
                        component: 1,
                        outro: 0
                    }
                }
            }
        });

        await fs.outputFile(
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
        await fs.outputFile(
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
            "cruft",
            "auditar",
            "--json",
            "--sem-gravar",
            "--base",
            base,
            "--budget",
            budget
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.contagens.producao.anyExplicito).toBe(1);
        expect(conteudo.contagens.producao.checksNull).toBe(1);
        expect(conteudo.contagens.producao.fallbacksDefensivos).toBe(1);
        expect(conteudo.contagens.producao.storageDireto).toBe(1);
        expect(conteudo.contagens.producao.exportsSuspeitos).toBe(1);
        expect(conteudo.contagens.producao.arquivosAcimaTarget.service).toBe(1);
    });

    test("audita vazamentos arquiteturais do frontend em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-auditar-"));
        const frontendDir = path.join(base, "frontend", "src");

        await fs.outputFile(path.join(frontendDir, "stores", "unidade.ts"), "export const useUnidadeStore = () => ({ invalidar: () => undefined, obterUnidade: () => undefined, recarregarUnidade: () => undefined, dadosEdicaoValidos: () => true, sincronizarUnidade: () => undefined, marcarUnidadeParaAtualizacao: () => undefined, limparContextoAtual: () => undefined, resetar: () => undefined, contextoAtual: null, erroAtual: null, carregando: false });");
        await fs.outputFile(path.join(frontendDir, "services", "unidadeService.ts"), "export async function buscarUnidade() { return null; }");
        await fs.outputFile(path.join(frontendDir, "composables", "useUnidadeTela.ts"), "export function useUnidadeTela() { return { carregar: () => undefined }; }");
        await fs.outputFile(path.join(frontendDir, "router", "unidade.routes.ts"), "export const rotasUnidade = [];");

        await fs.outputFile(
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
        await fs.outputFile(
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
            "--sem-gravar",
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
        expect(conteudo.hotspots.some((hotspot) => hotspot.hubCentral && hotspot.sinaisAtivos.includes("superficieAmpla"))).toBe(false);
    });

    test("tipos internos de store nao disparam bolsaDependenciasLarga", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-contexto-store-"));
        await fs.outputFile(
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
        const resultado = await executarSgc(["frontend", "arquitetura", "auditar", "--json", "--sem-gravar", "--base", base]);
        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.resumo.metricas.arquivosComBolsaDependenciasLarga).toBe(0);
    });

    test("hub central nao dispara superficieAmpla", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-hub-central-"));
        const frontendDir = path.join(base, "frontend", "src");

        await fs.outputFile(
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

        const resultado = await executarSgc(["frontend", "arquitetura", "auditar", "--json", "--sem-gravar", "--base", base]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        const hotspot = conteudo.hotspots.find((item) => item.arquivo === "frontend/src/stores/perfil.ts");
        expect(hotspot).toBeUndefined();
    });

    test("gate arquitetural falha quando view importa service diretamente", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-gate-falha-"));
        const frontendDir = path.join(base, "frontend");

        await fs.outputJson(path.join(frontendDir, "package.json"), {name: "frontend-fixture", private: true});
        await fs.outputJson(path.join(frontendDir, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {
                    "@/*": ["./src/*"],
                },
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await fs.outputFile(path.join(frontendDir, "src", "services", "unidadeService.ts"), "export async function buscarUnidade() { return null; }");
        await fs.outputFile(
            path.join(frontendDir, "src", "views", "UnidadeView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {buscarUnidade} from '../services/unidadeService';",
                "void buscarUnidade();",
                "</script>",
            ].join("\n")
        );
        await fs.copy(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(frontendDir, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["frontend", "arquitetura", "validar", "--base", base]);
        expect(resultado.exitCode).not.toBe(0);
        expect(resultado.stdout).toContain("view-sem-service-direto");
    });

    test("gate arquitetural passa quando view usa composable", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-gate-ok-"));
        const frontendDir = path.join(base, "frontend");

        await fs.outputJson(path.join(frontendDir, "package.json"), {name: "frontend-fixture", private: true});
        await fs.outputJson(path.join(frontendDir, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {
                    "@/*": ["./src/*"],
                },
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await fs.outputFile(path.join(frontendDir, "src", "services", "unidadeService.ts"), "export async function buscarUnidade() { return null; }");
        await fs.outputFile(
            path.join(frontendDir, "src", "composables", "useUnidadeTela.ts"),
            [
                "import {buscarUnidade} from '../services/unidadeService';",
                "export function useUnidadeTela() {",
                "  return { carregar: () => buscarUnidade() };",
                "}",
            ].join("\n")
        );
        await fs.outputFile(
            path.join(frontendDir, "src", "views", "UnidadeView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {useUnidadeTela} from '../composables/useUnidadeTela';",
                "const tela = useUnidadeTela();",
                "void tela.carregar();",
                "</script>",
            ].join("\n")
        );
        await fs.copy(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(frontendDir, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["frontend", "arquitetura", "validar", "--base", base]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Nenhuma violacao arquitetural encontrada");
    });

    test("gate arquitetural falha quando frontend calcula habilitacao de acao por perfil ou situacao", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-acoes-backend-falha-"));
        const frontendDir = path.join(base, "frontend");

        await fs.outputJson(path.join(frontendDir, "package.json"), {name: "frontend-fixture", private: true});
        await fs.outputJson(path.join(frontendDir, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {
                    "@/*": ["./src/*"],
                },
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await fs.outputFile(
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
        await fs.copy(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(frontendDir, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["frontend", "arquitetura", "validar", "--base", base]);
        expect(resultado.exitCode).not.toBe(0);
        expect(resultado.stdout).toContain("frontend-sem-regra-local-acoes");
        expect(resultado.stdout).toContain("habilitarAprovarConsenso");
    });

    test("gate arquitetural permite flag de acao vinda diretamente do backend", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-acoes-backend-ok-"));
        const frontendDir = path.join(base, "frontend");

        await fs.outputJson(path.join(frontendDir, "package.json"), {name: "frontend-fixture", private: true});
        await fs.outputJson(path.join(frontendDir, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {
                    "@/*": ["./src/*"],
                },
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await fs.outputFile(
            path.join(frontendDir, "src", "views", "ConsensoView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {computed} from 'vue';",
                "const query = { data: { value: { habilitarAprovarConsenso: true } } };",
                "const habilitarAprovarConsenso = computed(() => query.data.value.habilitarAprovarConsenso ?? false);",
                "</script>",
            ].join("\n")
        );
        await fs.copy(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(frontendDir, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["frontend", "arquitetura", "validar", "--base", base]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Nenhum calculo local novo de habilitacao/exibicao de acoes encontrado");
    });

    test("composable fachada de store não é penalizado por chamadasStore >= 8", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-facade-"));
        const frontendDir = path.join(base, "frontend", "src");

        await fs.outputFile(
            path.join(frontendDir, "stores", "perfil.ts"),
            "import {defineStore} from 'pinia'; export const usePerfilStore = defineStore('perfil', () => ({ a: 1, b: 2, c: 3, d: 4, e: 5, f: 6, g: 7, h: 8, i: 9, j: 10, k: 11, l: 12 }));"
        );

        // Composable que só delega para uma única store (fachada) — acessa a store 12 vezes
        await fs.outputFile(
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
            "frontend", "arquitetura", "auditar", "--json", "--sem-gravar", "--base", base,
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        const hotspot = conteudo.hotspots.find((h) => h.arquivo.endsWith("usePerfil.ts"));
        // Fachada de store: acessar a store muitas vezes é esperado — sem penalidade
        expect(hotspot).toBeUndefined();
    });

    test("módulo em stores/ sem defineStore não é penalizado como store Pinia", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-nao-store-"));
        const frontendDir = path.join(base, "frontend", "src");

        await fs.outputFile(
            path.join(frontendDir, "services", "autenticacaoService.ts"),
            "export async function login() { return null; } export async function logout() { return null; } export async function renovar() { return null; }"
        );

        // Módulo de funções puras em stores/ que NÃO usa defineStore (orquestração de autenticação)
        await fs.outputFile(
            path.join(frontendDir, "stores", "autenticacao.ts"),
            [
                "import * as autenticacaoService from '@/services/autenticacaoService';",
                "export async function entrar() { return autenticacaoService.login(); }",
                "export async function sair() { return autenticacaoService.logout(); }",
                "export async function renovarSessao() { return autenticacaoService.renovar(); }",
            ].join("\n")
        );

        const resultado = await executarSgc([
            "frontend", "arquitetura", "auditar", "--json", "--sem-gravar", "--base", base,
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        const hotspot = conteudo.hotspots.find((h) => h.arquivo.endsWith("autenticacao.ts"));
        // Orquestração sem defineStore: chamar serviços é esperado — sem score nem sinal serviceDireto
        expect(hotspot).toBeUndefined();
    });

    test("composable que chama serviço diretamente não recebe sinal serviceDireto", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-composable-servico-"));
        const frontendDir = path.join(base, "frontend", "src");

        await fs.outputFile(
            path.join(frontendDir, "services", "itemService.ts"),
            "export async function buscarItens() { return []; }"
        );

        // Composable com superfície exportada ampla E chamada de serviço direta
        // → deve aparecer em hotspots pelo superficieAmpla, mas NÃO pelo serviceDireto
        await fs.outputFile(
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
            "frontend", "arquitetura", "auditar", "--json", "--sem-gravar", "--base", base,
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        const hotspot = conteudo.hotspots.find((h) => h.arquivo.endsWith("useItens.ts"));
        // Composable aparece por superficieAmpla, mas chamar serviços não é sinalizado
        expect(hotspot).toBeDefined();
        expect(hotspot.sinaisAtivos).toContain("superficieAmpla");
        expect(hotspot.sinaisAtivos).not.toContain("serviceDireto");
    });

    test("valida cruft do frontend com waiver de tamanho", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cruft-validar-"));
        const frontendDir = path.join(base, "frontend", "src");
        const budget = path.join(base, "budget.json");
        const waivers = path.join(base, "waivers.json");

        await fs.outputJson(budget, {
            versaoSchema: "1.0.0",
            camadas: {
                service: {target: 3, hard: 6},
                outro: {target: 6, hard: 10}
            },
            metricas: {
                maximosProducao: {
                    anyExplicito: 0,
                    checksNull: 0,
                    fallbacksDefensivos: 0,
                    catchBlocks: 0,
                    castsDuplos: 0,
                    storageDireto: 0,
                    exportsSuspeitos: 2,
                    arquivosAcimaTargetPorCamada: {
                        service: 1,
                        outro: 0
                    }
                }
            }
        });
        await fs.outputJson(waivers, {
            versaoSchema: "1.0.0",
            waivers: [
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
        await fs.outputFile(
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
            "cruft",
            "validar",
            "--json",
            "--sem-gravar",
            "--base",
            base,
            "--budget",
            budget,
            "--waivers",
            waivers
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.status).toBe("ok");
        expect(conteudo.violacoes).toEqual([]);
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
    }, 60000);

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
        await fs.outputFile(
            path.join(srcDir, "ClasseIndireta.java"),
            "package sgc.exemplo; public class ClasseIndireta { public String calcular(boolean ativo) { return ativo ? \"ok\" : \"pendente\"; } }"
        );
        await fs.outputFile(
            path.join(srcDir, "ClasseSemEvidencia.java"),
            "package sgc.exemplo; public class ClasseSemEvidencia { public boolean validar(String valor) { return valor != null && !valor.isBlank(); } }"
        );
        await fs.outputFile(
            path.join(srcDir, "ClasseForaEscopo.java"),
            "package sgc.exemplo; public class ClasseForaEscopo { public int contarPositivos(java.util.List<Integer> valores) { return (int) valores.stream().filter(valor -> valor > 0).count(); } }"
        );
        await fs.outputFile(path.join(testDir, "ClasseDiretaTest.java"), "package sgc.exemplo; class ClasseDiretaTest {}");
        // language=XML
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

    test("exibe comando canonico e alias legado para auditoria de views", async () => {
        const resultado = await executarSgc(["frontend", "views", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("validacoes-auditar");
        expect(resultado.stdout).toContain("auditar-validacoes");
        expect(resultado.stdout).toContain("templates-validar");
    });

    test("despacha ajuda da validacao de modais do frontend", async () => {
        const resultado = await executarSgc(["frontend", "modais", "validar", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("ModalPadrao");
        expect(resultado.stdout).toContain("componente-base");
    });

    test("valida previsibilidade estrutural das views em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-views-templates-"));
        const viewsDir = path.join(base, "frontend", "src", "views");

        await fs.outputFile(
            path.join(viewsDir, "PainelView.vue"),
            [
                "<template>",
                "  <LayoutPadrao>",
                "    <PageHeader title=\"Painel\" />",
                "  </LayoutPadrao>",
                "</template>"
            ].join("\n")
        );

        await fs.outputFile(
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
        expect(conteudo.violacoes.some((violacao) => violacao.regra === "view-com-bmodal-cru")).toBe(true);
        expect(conteudo.violacoes.some((violacao) => violacao.regra === "view-sem-layout-padrao")).toBe(true);
        expect(conteudo.violacoes.some((violacao) => violacao.regra === "view-sem-cabecalho-padrao")).toBe(true);
    });

    test("valida padronizacao de modais em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-modais-validar-"));
        await fs.outputFile(
            path.join(base, "frontend", "src", "components", "comum", "ModalPadrao.vue"),
            "<template><BModal title=\"Base\" /></template>"
        );
        await fs.outputFile(
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

    test("exibe ajuda padronizada no script frontend cobertura auditoria", async () => {
        const resultado = await executarScriptFrontendCobertura(["--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Auditoria unificada de cobertura e risco (Frontend).");
    });

    test("despacha ajuda do servidor do qa dashboard", async () => {
        const resultado = await executarSgc(["qa", "dashboard", "servir", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Serve o dashboard de QA localmente.");
    });

    test("exibe ajuda do comando de sincronizacao de versao do projeto", async () => {
        const resultado = await executarSgc(["projeto", "versao-sincronizar", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Sincroniza a versao entre gradle.properties e frontend/package.json.");
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
        await fs.outputFile(path.join(diretorioBase, "backend-coverage-auditoria.md"), "# teste");
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
        expect(await fs.pathExists(path.join(diretorioBase, "backend-coverage-auditoria.md"))).toBe(false);
    });

    test("nao possui diretorios legados de scripts em backend/frontend", async () => {
        expect(await fs.pathExists(DIRETORIO_SCRIPTS_BACKEND_LEGADO)).toBe(false);
        expect(await fs.pathExists(DIRETORIO_SCRIPTS_FRONTEND_LEGADO)).toBe(false);
    });

    test("exibe ajuda do comando de cobertura cruzada do backend", async () => {
        const resultado = await executarSgc(["backend", "cobertura", "cruzada", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Auditoria de cobertura cruzada e independente (Backend).");
    });

    test("exibe ajuda do alias de test-ids duplicados do frontend", async () => {
        const resultado = await executarSgc(["frontend", "test-ids", "duplicados", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Alias legado para 'listar-duplicados'.");
    });

    test("exibe ajuda dos novos comandos de comunicacao", async () => {
        const resultado = await executarSgc(["comunicacao", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("cobertura-notificacoes");
        expect(resultado.stdout).toContain("strings");
        expect(resultado.stdout).toContain("templates-email");
    });

    test("exibe ajuda do comando de auditar-null do java", async () => {
        const resultado = await executarSgc(["backend", "java", "auditar-null", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Audita verificacoes de null no backend");
    });

    test("projeto doctor identifica corretamente a ausência de arquivos essenciais e falha com código 1", async () => {
        const diretorioVazio = await mkdtemp(path.join(os.tmpdir(), "sgc-doctor-vazio-"));

        // Executamos o doctor passando o diretório temporário vazio como base
        const resultado = await executarSgc(["projeto", "doctor", "--json", "--base", diretorioVazio]);

        // Como arquivos essenciais como gradlew e package.json estão ausentes, deve retornar código de erro 1
        expect(resultado.exitCode).toBe(1);

        const dados = JSON.parse(resultado.stdout);
        expect(dados.statusGeral).toBe("falha");
        expect(dados.totais.falha).toBeGreaterThan(0);

        // Verifica se um dos arquivos obrigatórios ausentes foi reportado como falha
        const falhaGradlew = dados.verificacoes.find((v) => v.nome === "gradlew");
        expect(falhaGradlew).toBeDefined();
        expect(falhaGradlew.status).toBe("falha");
        expect(falhaGradlew.detalhe).toContain("gradlew ausente");
    });

    test("lista com sucesso test-ids do frontend em recorte controlado", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-testids-listar-"));

        // Criar arquivos .vue de teste com test-ids
        await fs.outputFile(
            path.join(diretorioBase, "ComponenteA.vue"),
            "<template><button data-test-codigo=\"btn-salvar\">Salvar</button></template>"
        );
        await fs.outputFile(
            path.join(diretorioBase, "ComponenteB.vue"),
            "<template><input data-testid=\"input-nome\" /></template>"
        );

        const resultado = await executarSgc(["frontend", "test-ids", "listar", "--base", diretorioBase]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("ComponenteA.vue");
        expect(resultado.stdout).toContain("btn-salvar");
        expect(resultado.stdout).toContain("ComponenteB.vue");
        expect(resultado.stdout).toContain("input-nome");
    });

    test("detecta corretamente test-ids duplicados e falha com codigo 1", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-testids-duplicados-"));

        // Criar dois arquivos com o mesmo test-id
        await fs.outputFile(
            path.join(diretorioBase, "ComponenteX.vue"),
            "<template><button data-testid=\"btn-acao\">Ação X</button></template>"
        );
        await fs.outputFile(
            path.join(diretorioBase, "ComponenteY.vue"),
            "<template><div data-testid=\"btn-acao\">Ação Y</div></template>"
        );

        const resultado = await executarSgc(["frontend", "test-ids", "listar-duplicados", "--base", diretorioBase]);

        // O script deve falhar com exitCode 1 quando encontra duplicados
        expect(resultado.exitCode).toBe(1);
        expect(resultado.stdout).toContain("Test-ids duplicados encontrados");
        expect(resultado.stdout).toContain("btn-acao");
        expect(resultado.stdout).toContain("ComponenteX.vue");
        expect(resultado.stdout).toContain("ComponenteY.vue");
    });

    test("passa com sucesso se nao houver test-ids duplicados", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-testids-unicos-"));

        await fs.outputFile(
            path.join(diretorioBase, "ComponenteUnico.vue"),
            "<template><button data-testid=\"btn-unico\">Ação Única</button></template>"
        );

        const resultado = await executarSgc(["frontend", "test-ids", "listar-duplicados", "--base", diretorioBase]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Nenhum test-id duplicado encontrado.");
    });
}, 30000);
