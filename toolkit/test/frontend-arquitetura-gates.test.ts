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
import {VERSAO_CONFIGURACAO} from "../lib/configuracao.js";

describe("Gates arquiteturais do frontend", () => {
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
});
