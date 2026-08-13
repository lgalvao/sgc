import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {
    executarSgc,
    escreverJson,
    lerArquivo,
    existe
} from "./apoio.js";
import {VERSAO_CONFIGURACAO} from "../lib/configuracao.js";
import {executarCrawler, normalizarArgumentosPlaywright} from "../frontend/acessibilidade-crawler.js";
import {normalizarResultados} from "../frontend/acessibilidade-processar-resultados.js";

type ChamadaComando = {
    comando: string;
    argumentos: readonly string[];
    base?: string;
};

describe("Acessibilidade frontend", () => {
    test("processa resultados de acessibilidade em uma base externa", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-acessibilidade-processar-"));
        const entrada = path.join(diretorioBase, "resultados.json");
        const saida = path.join(diretorioBase, "relatorios", "acessibilidade.md");

        await escreverJson(entrada, [{
            name: "Pagina inicial",
            route: "/",
            violations: [{
                impact: "moderate",
                id: "botao-com-nome",
                help: "Botoes devem ter nome acessivel",
                helpUrl: "https://dequeuniversity.com/rules/axe/",
                description: "Verifique o nome acessivel do botao.",
                nodes: [{target: ["button"]}]
            }]
        }]);

        const resultado = await executarSgc([
            "frontend",
            "acessibilidade",
            "processar",
            "--base",
            diretorioBase,
            "--entrada",
            entrada,
            "--saida",
            saida
        ]);

        expect(resultado.exitCode).toBe(0);
        expect(await existe(saida)).toBe(true);
        expect(await lerArquivo(saida, "utf8")).toContain("botao-com-nome");
    });

    test("parametriza crawler Playwright e valida resultados de acessibilidade", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-a11y-crawler-"));
        const chamadas: ChamadaComando[] = [];
        const resultado = await executarCrawler(["--projeto", "chromium", "--lista"], {
            base: diretorioBase,
            especificacao: "testes/a11y.spec.ts",
            configuracao: "testes/playwright.config.ts",
            executarComando: async (comando, argumentos, base) => {
                chamadas.push({comando, argumentos, base});
            }
        });

        expect(normalizarArgumentosPlaywright(["--projeto", "chromium", "--lista"])).toEqual([
            "--project",
            "chromium",
            "--list"
        ]);
        expect(resultado).toMatchObject({
            diretorioBase,
            especificacao: "testes/a11y.spec.ts",
            configuracao: "testes/playwright.config.ts"
        });
        expect(chamadas).toEqual([{
            comando: "npx",
            argumentos: [
                "playwright",
                "test",
                "testes/a11y.spec.ts",
                "--config=testes/playwright.config.ts",
                "--project",
                "chromium",
                "--list"
            ],
            base: diretorioBase
        }]);

        expect(() => normalizarResultados({})).toThrow("a raiz deve ser uma lista");
    });

    test("deriva caminhos do crawler a partir do diretório de testes de integração configurado", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-a11y-crawler-configurado-"));
        const chamadas: ChamadaComando[] = [];
        await escreverJson(path.join(diretorioBase, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                testesIntegracao: "testes-e2e"
            }
        });

        const resultado = await executarCrawler([], {
            base: diretorioBase,
            executarComando: async (comando, argumentos, base) => {
                chamadas.push({comando, argumentos, base});
            }
        });

        expect(resultado).toMatchObject({
            diretorioBase,
            especificacao: "testes-e2e/a11y/crawler.spec.ts",
            configuracao: "testes-e2e/playwright.config.ts"
        });
        expect(chamadas).toEqual([{
            comando: "npx",
            argumentos: [
                "playwright",
                "test",
                "testes-e2e/a11y/crawler.spec.ts",
                "--config=testes-e2e/playwright.config.ts"
            ],
            base: diretorioBase
        }]);
    });
});
