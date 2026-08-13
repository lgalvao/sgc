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

interface PontoArquiteturalJson {
    arquivo: string;
    sinaisAtivos: string[];
    hubCentral?: boolean;
}

describe("Auditoria arquitetural do frontend", () => {
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
});
