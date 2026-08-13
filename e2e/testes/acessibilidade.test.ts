import assert from "node:assert/strict";
import {access, mkdtemp, readFile, writeFile} from "node:fs/promises";
import os from "node:os";
import path from "node:path";
import {test} from "node:test";
import {executarCrawler, normalizarArgumentosPlaywright} from "../acessibilidade-crawler.js";
import {
    normalizarResultados,
    processarResultadosAcessibilidade
} from "../acessibilidade-processar-resultados.js";

interface ChamadaComando {
    comando: string;
    argumentos: readonly string[];
    diretorio: string;
}

test("processa resultados de acessibilidade em uma base externa", async () => {
    const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-acessibilidade-processar-"));
    const entrada = path.join(diretorioBase, "resultados.json");
    const saida = path.join(diretorioBase, "relatorios", "acessibilidade.md");

    await writeFile(entrada, JSON.stringify([{
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
    }]));

    const resultado = await processarResultadosAcessibilidade({entrada, saida});

    assert.equal(resultado.paginas, 1);
    await access(saida);
    assert.match(await readFile(saida, "utf8"), /botao-com-nome/);
});

test("parametriza crawler Playwright e valida resultados de acessibilidade", async () => {
    const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-a11y-crawler-"));
    const chamadas: ChamadaComando[] = [];
    const resultado = await executarCrawler(["--projeto", "chromium", "--lista"], {
        base: diretorioBase,
        especificacao: "testes/a11y.spec.ts",
        configuracao: "testes/playwright.config.ts",
        executarComando: async (comando, argumentos, diretorio) => {
            chamadas.push({comando, argumentos, diretorio});
            return {codigoSaida: 0};
        }
    });

    assert.deepEqual(normalizarArgumentosPlaywright(["--projeto", "chromium", "--lista"]), [
        "--project",
        "chromium",
        "--list"
    ]);
    assert.deepEqual(resultado, {
        diretorioBase,
        especificacao: "testes/a11y.spec.ts",
        configuracao: "testes/playwright.config.ts",
        argumentos: [
            "playwright",
            "test",
            "testes/a11y.spec.ts",
            "--config=testes/playwright.config.ts",
            "--project",
            "chromium",
            "--list"
        ]
    });
    assert.deepEqual(chamadas, [{
        comando: "npx",
        argumentos: resultado?.argumentos,
        diretorio: diretorioBase
    }]);

    assert.throws(() => normalizarResultados({}), /a raiz deve ser uma lista/);
});

test("usa os caminhos E2E padrão quando a base é substituída", async () => {
    const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-a11y-crawler-base-"));
    const chamadas: ChamadaComando[] = [];
    const resultado = await executarCrawler([], {
        base: diretorioBase,
        executarComando: async (comando, argumentos, diretorio) => {
            chamadas.push({comando, argumentos, diretorio});
            return {codigoSaida: 0};
        }
    });

    assert.equal(resultado?.especificacao, "e2e/a11y/crawler.spec.ts");
    assert.equal(resultado?.configuracao, "e2e/playwright.config.ts");
    assert.equal(chamadas[0]?.diretorio, diretorioBase);
});
