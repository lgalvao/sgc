import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {
    DIRETORIO_RAIZ,
    executarSgc,
    escreverArquivo,
    escreverJson,
    copiar
} from "./apoio.js";
import {VERSAO_CONFIGURACAO} from "../biblioteca/configuracao.js";

describe("Gates arquiteturais do cliente", () => {
    test("gate arquitetural falha quando view importa service diretamente", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-gate-falha-"));
        const diretorioCliente = path.join(base, "frontend");

        await escreverJson(path.join(diretorioCliente, "package.json"), {name: "cliente-fixture", private: true});
        await escreverJson(path.join(diretorioCliente, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {
                    "@/*": ["./src/*"],
                },
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await escreverArquivo(path.join(diretorioCliente, "src", "services", "unidadeService.ts"), "export async function buscarUnidade() { return null; }");
        await escreverArquivo(
            path.join(diretorioCliente, "src", "views", "UnidadeView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {buscarUnidade} from '../services/unidadeService';",
                "void buscarUnidade();",
                "</script>",
            ].join("\n")
        );
        await copiar(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(diretorioCliente, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["cliente", "arquitetura", "validar", "--base", base]);
        expect(resultado.exitCode).not.toBe(0);
        expect(resultado.stdout).toContain("view-sem-service-direto");
    });

    test("gate arquitetural passa quando view usa composable", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-gate-ok-"));
        const diretorioCliente = path.join(base, "frontend");

        await escreverJson(path.join(diretorioCliente, "package.json"), {name: "cliente-fixture", private: true});
        await escreverJson(path.join(diretorioCliente, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {
                    "@/*": ["./src/*"],
                },
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await escreverArquivo(path.join(diretorioCliente, "src", "services", "unidadeService.ts"), "export async function buscarUnidade() { return null; }");
        await escreverArquivo(
            path.join(diretorioCliente, "src", "composables", "useUnidadeTela.ts"),
            [
                "import {buscarUnidade} from '../services/unidadeService';",
                "export function useUnidadeTela() {",
                "  return { carregar: () => buscarUnidade() };",
                "}",
            ].join("\n")
        );
        await escreverArquivo(
            path.join(diretorioCliente, "src", "views", "UnidadeView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {useUnidadeTela} from '../composables/useUnidadeTela';",
                "const tela = useUnidadeTela();",
                "void tela.carregar();",
                "</script>",
            ].join("\n")
        );
        await copiar(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(diretorioCliente, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["cliente", "arquitetura", "validar", "--base", base]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Nenhuma violacao arquitetural encontrada");
    });

    test("emite resumo JSON limitado do gate arquitetural", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-gate-resumo-"));
        const diretorioCliente = path.join(base, "frontend");

        await escreverJson(path.join(diretorioCliente, "package.json"), {name: "cliente-fixture", private: true});
        await escreverJson(path.join(diretorioCliente, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {"@/*": ["./src/*"]},
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await escreverArquivo(path.join(diretorioCliente, "src", "services", "unidadeService.ts"), "export async function buscarUnidade() { return null; }");
        await escreverArquivo(
            path.join(diretorioCliente, "src", "views", "UnidadeView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {buscarUnidade} from '../services/unidadeService';",
                "void buscarUnidade();",
                "</script>",
            ].join("\n")
        );
        await copiar(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(diretorioCliente, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["cliente", "arquitetura", "validar", "--base", base, "--json-resumido"]);

        expect(resultado.exitCode).not.toBe(0);
        const resumo = JSON.parse(resultado.stdout);
        expect(resumo).toMatchObject({versaoResumo: 1, truncado: true, limiteItens: 20});
        expect(resumo.totalModulos).toBeGreaterThan(0);
        expect(resumo.resumo.totalViolacoes).toBeGreaterThan(0);
        expect(resumo.violacoes[0]).toMatchObject({regra: "view-sem-service-direto"});
        expect(resumo.modules).toBeUndefined();
    });

    test("resolve diretorio cliente configurado no gate arquitetural", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-diretorio-configurado-"));
        const diretorioCliente = path.join(base, "cliente");

        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {cliente: "cliente"},
        });
        await escreverJson(path.join(diretorioCliente, "package.json"), {name: "cliente-fixture", private: true});
        await escreverJson(path.join(diretorioCliente, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {"@/*": ["./src/*"]},
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await escreverArquivo(path.join(diretorioCliente, "src", "services", "unidadeService.ts"), "export async function buscarUnidade() { return null; }");
        await escreverArquivo(
            path.join(diretorioCliente, "src", "composables", "useUnidadeTela.ts"),
            "import {buscarUnidade} from '../services/unidadeService'; export function useUnidadeTela() { return {carregar: () => buscarUnidade()}; }"
        );
        await escreverArquivo(
            path.join(diretorioCliente, "src", "views", "UnidadeView.vue"),
            "<script setup lang=\"ts\">import {useUnidadeTela} from '../composables/useUnidadeTela'; const tela = useUnidadeTela(); void tela.carregar();</script>"
        );
        await copiar(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(diretorioCliente, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["cliente", "arquitetura", "validar", "--base", base]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Nenhuma violacao arquitetural encontrada");
    });

    test("gate arquitetural falha quando cliente calcula habilitacao de acao por perfil ou situacao", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-acoes-servidor-falha-"));
        const diretorioCliente = path.join(base, "frontend");

        await escreverJson(path.join(diretorioCliente, "package.json"), {name: "cliente-fixture", private: true});
        await escreverJson(path.join(diretorioCliente, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {
                    "@/*": ["./src/*"],
                },
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await escreverArquivo(
            path.join(diretorioCliente, "src", "views", "ConsensoView.vue"),
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
        await copiar(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(diretorioCliente, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["cliente", "arquitetura", "validar", "--base", base]);
        expect(resultado.exitCode).not.toBe(0);
        expect(resultado.stdout).toContain("cliente-sem-regra-local-acoes");
        expect(resultado.stdout).toContain("habilitarAprovarConsenso");
    });

    test("gate arquitetural permite flag de acao vinda diretamente do servidor", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-arquitetura-acoes-servidor-ok-"));
        const diretorioCliente = path.join(base, "frontend");

        await escreverJson(path.join(diretorioCliente, "package.json"), {name: "cliente-fixture", private: true});
        await escreverJson(path.join(diretorioCliente, "tsconfig.json"), {
            compilerOptions: {
                baseUrl: ".",
                paths: {
                    "@/*": ["./src/*"],
                },
            },
            include: ["src/**/*.ts", "src/**/*.vue"],
        });
        await escreverArquivo(
            path.join(diretorioCliente, "src", "views", "ConsensoView.vue"),
            [
                "<script setup lang=\"ts\">",
                "import {computed} from 'vue';",
                "const query = { data: { value: { habilitarAprovarConsenso: true } } };",
                "const habilitarAprovarConsenso = computed(() => query.data.value.habilitarAprovarConsenso ?? false);",
                "</script>",
            ].join("\n")
        );
        await copiar(path.join(DIRETORIO_RAIZ, "frontend", ".dependency-cruiser.cjs"), path.join(diretorioCliente, ".dependency-cruiser.cjs"));

        const resultado = await executarSgc(["cliente", "arquitetura", "validar", "--base", base]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Nenhum calculo local novo de habilitacao/exibicao de acoes encontrado");
    });
});
