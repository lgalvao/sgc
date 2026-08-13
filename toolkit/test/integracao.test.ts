import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {execa} from "execa";
import {pathToFileURL} from "node:url";
import {resolverCaminhosOpenapi} from "../integracao/contratos-openapi-caminhos.js";
import {VERSAO_CONFIGURACAO} from "../lib/configuracao.js";
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
            paths: 1
        });
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
    });
}, 30000);
