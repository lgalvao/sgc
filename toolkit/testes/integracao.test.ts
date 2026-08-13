import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {execa} from "execa";
import {pathToFileURL} from "node:url";
import {executarDiffContratos} from "../integracao/contratos-diff-motor.js";
import {fixarBaselineContrato} from "../integracao/contratos-baseline-motor.js";
import {resolverCaminhoArquivoOpenapi, resolverCaminhosOpenapi} from "../integracao/contratos-openapi-caminhos.js";
import {exportarOpenapi} from "../integracao/contratos-openapi-motor.js";
import {VERSAO_CONFIGURACAO} from "../biblioteca/configuracao.js";
import {DIRETORIO_RAIZ, executarSgc, escreverJson, lerArquivo, existe} from "./apoio.js";

const CAMINHOS_COMANDOS_CONTRATOS = [
    "contratos-diff.ts",
    "contratos-exportar-openapi.ts",
    "contratos-fixar-baseline.ts"
].map(nome => path.join(DIRETORIO_RAIZ, "toolkit", "integracao", nome));

describe("Integrações de contratos do toolkit", () => {
    test("pode importar comandos de contratos sem executar integrações", async () => {
        const resultados = await Promise.all(CAMINHOS_COMANDOS_CONTRATOS.map(async caminho => {
            const urlModulo = pathToFileURL(caminho).href;
            return execa(process.execPath, [
                "--import=tsx",
                "--input-type=module",
                "-e",
                `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
            ], {
                cwd: DIRETORIO_RAIZ,
                reject: false
            });
        }));

        for (const resultado of resultados) {
            expect(resultado.exitCode).toBe(0);
            expect(resultado.stdout).toBe("importacao-ok");
        }
    });

    test("resolve artefatos OpenAPI a partir da base externa", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-openapi-base-"));
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                contratosOpenapi: "artefatos/contratos"
            }
        });

        const caminhos = resolverCaminhosOpenapi(base);
        const documentoAtual = {
            openapi: "3.1.0",
            info: {title: "Projeto externo", version: "1.0.0"},
            paths: {"/exemplo": {get: {responses: {200: {description: "OK"}}}}}
        };
        const url = `data:application/json,${encodeURIComponent(JSON.stringify(documentoAtual))}`;
        const exportacao = await executarSgc([
            "integracao",
            "contratos",
            "exportar-openapi",
            "--json",
            "--base",
            base,
            "--url",
            url
        ]);

        expect(exportacao.exitCode).toBe(0);
        expect(JSON.parse(exportacao.stdout)).toMatchObject({
            base,
            saida: caminhos.caminhoAtual,
            quantidadeRotas: 1
        });
        expect(JSON.parse(exportacao.stdout).paths).toBeUndefined();
        expect(await existe(caminhos.caminhoAtual)).toBe(true);

        await escreverJson(caminhos.caminhoReferencia, {
            openapi: "3.1.0",
            info: {title: "Projeto externo", version: "0.9.0"},
            paths: {}
        });

        const diferenca = await executarSgc([
            "integracao",
            "contratos",
            "diff",
            "--json",
            "--base",
            base
        ]);

        expect(diferenca.exitCode).toBe(0);
        expect(JSON.parse(diferenca.stdout)).toMatchObject({
            base,
            anterior: caminhos.caminhoReferencia,
            atual: caminhos.caminhoAtual,
            houveMudancas: true
        });
        expect(JSON.parse(diferenca.stdout).modo).toBe("diferencaTextual");
        expect(JSON.parse(diferenca.stdout).saidaPadrao).toContain("openapi");
        expect(JSON.parse(diferenca.stdout).stdout).toBeUndefined();
        expect(await existe(caminhos.caminhoRelatorio)).toBe(false);

        const diferencaGravada = await executarSgc([
            "integracao",
            "contratos",
            "diff",
            "--json",
            "--gravar",
            "--base",
            base
        ]);

        expect(diferencaGravada.exitCode).toBe(0);
        expect(JSON.parse(diferencaGravada.stdout)).toMatchObject({
            base,
            anterior: caminhos.caminhoReferencia,
            atual: caminhos.caminhoAtual,
            houveMudancas: true
        });
        expect(await existe(caminhos.caminhoRelatorio)).toBe(true);

        const fixacao = await executarSgc([
            "integracao",
            "contratos",
            "fixar-baseline",
            "--json",
            "--base",
            base
        ]);

        expect(fixacao.exitCode).toBe(0);
        expect(JSON.parse(fixacao.stdout)).toMatchObject({
            base,
            origem: caminhos.caminhoAtual,
            destino: caminhos.caminhoReferencia
        });
        expect(await lerArquivo(caminhos.caminhoReferencia, "utf8")).toBe(await lerArquivo(caminhos.caminhoAtual, "utf8"));

        expect(resolverCaminhoArquivoOpenapi(base, "contrato.json")).toBe(path.join(base, "contrato.json"));
    });

    test("exporta OpenAPI pelo motor com URL e saída explícitas", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-openapi-motor-"));
        const documento = {
            openapi: "3.1.0",
            info: {title: "Motor externo", version: "2.0.0"},
            paths: {"/saude": {get: {responses: {200: {description: "OK"}}}}}
        };
        const resultado = await exportarOpenapi({
            base,
            url: `data:application/json,${encodeURIComponent(JSON.stringify(documento))}`,
            saida: "artefatos/openapi.json"
        });

        expect(resultado).toMatchObject({
            base,
            saida: path.join(base, "artefatos", "openapi.json"),
            titulo: "Motor externo",
            versao: "2.0.0",
            quantidadeRotas: 1
        });
        expect(await existe(resultado.saida)).toBe(true);
    });

    test("compara e promove arquivos OpenAPI pelos motores com caminhos explícitos", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-openapi-operacoes-"));
        await escreverJson(path.join(base, "anterior.json"), {openapi: "3.1.0", info: {version: "1.0.0"}, paths: {}});
        await escreverJson(path.join(base, "atual.json"), {openapi: "3.1.0", info: {version: "1.1.0"}, paths: {"/saude": {}}});

        const diferenca = await executarDiffContratos({
            base,
            anterior: "anterior.json",
            atual: "atual.json"
        });
        expect(diferenca).toMatchObject({
            base,
            houveMudancas: true,
            modo: "diferencaTextual"
        });

        const identidade = await executarDiffContratos({base, anterior: "atual.json", atual: "atual.json"});
        expect(identidade).toMatchObject({houveMudancas: false, modo: "identico"});

        const fixacao = await fixarBaselineContrato({
            base,
            origem: "atual.json",
            destino: "referencia/openapi.json"
        });
        expect(fixacao).toMatchObject({
            base,
            origem: path.join(base, "atual.json"),
            destino: path.join(base, "referencia", "openapi.json")
        });
        expect(await existe(fixacao.destino)).toBe(true);
    });

    test("resolve caminhos informados relativos a base", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-openapi-caminhos-relativos-"));
        const anterior = {openapi: "3.1.0", info: {title: "Anterior", version: "1.0.0"}, paths: {}};
        const atual = {openapi: "3.1.0", info: {title: "Atual", version: "1.1.0"}, paths: {"/exemplo": {}}};
        await escreverJson(path.join(base, "anterior.json"), anterior);
        await escreverJson(path.join(base, "atual.json"), atual);

        const diferenca = await executarSgc([
            "integracao",
            "contratos",
            "diff",
            "--json",
            "--base",
            base,
            "--anterior",
            "anterior.json",
            "--atual",
            "atual.json"
        ]);

        expect(diferenca.exitCode).toBe(0);
        expect(JSON.parse(diferenca.stdout)).toMatchObject({
            anterior: path.join(base, "anterior.json"),
            atual: path.join(base, "atual.json"),
            houveMudancas: true
        });

        const fixacao = await executarSgc([
            "integracao",
            "contratos",
            "fixar-baseline",
            "--json",
            "--base",
            base,
            "--origem",
            "atual.json",
            "--destino",
            "referencia.json"
        ]);

        expect(fixacao.exitCode).toBe(0);
        expect(JSON.parse(fixacao.stdout)).toMatchObject({
            origem: path.join(base, "atual.json"),
            destino: path.join(base, "referencia.json")
        });
        expect(await existe(path.join(base, "referencia.json"))).toBe(true);
    });
}, 30000);
