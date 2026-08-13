import assert from "node:assert/strict";
import {execFile} from "node:child_process";
import path from "node:path";
import {promisify} from "node:util";
import {fileURLToPath, pathToFileURL} from "node:url";
import {test} from "node:test";

const executarArquivo = promisify(execFile);

const DIRETORIO_RAIZ = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const CAMINHOS_COMANDOS_ACESSIBILIDADE = [
    "acessibilidade-crawler.ts",
    "acessibilidade-processar-resultados.ts"
].map(nome => path.join(DIRETORIO_RAIZ, "e2e", nome));

async function importarSemExecutar(caminho: string): Promise<{codigoSaida?: number; stdout: string}> {
    const urlModulo = pathToFileURL(caminho).href;
    const resultado = await executarArquivo(process.execPath, [
        "--import=tsx",
        "--input-type=module",
        "-e",
        `process.argv.push("--help"); await import(${JSON.stringify(urlModulo)}); process.stdout.write("importacao-ok\\n");`
    ], {
        cwd: DIRETORIO_RAIZ
    });
    return {
        codigoSaida: 0,
        stdout: resultado.stdout.trim()
    };
}

test("pode importar comandos de acessibilidade sem executa-los", async () => {
    const resultados = await Promise.all(CAMINHOS_COMANDOS_ACESSIBILIDADE.map(importarSemExecutar));

    for (const resultado of resultados) {
        assert.equal(resultado.codigoSaida, 0);
        assert.equal(resultado.stdout, "importacao-ok");
    }
});
