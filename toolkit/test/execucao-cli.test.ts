import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {pathToFileURL} from "node:url";
import {describe, expect, test} from "vitest";
import {execa, execaNode} from "execa";
import {
    DIRETORIO_RAIZ,
    CAMINHO_SGC,
    escreverArquivo,
    existe
} from "./apoio.js";
import {CATALOGO_COMANDOS_COMPLETO} from "../lib/catalogo-comandos.js";
import {program} from "../sgc.js";

const CAMINHO_SGC_COMPILADO = path.join(DIRETORIO_RAIZ, "toolkit", "dist", "sgc.js");

describe("Execução e distribuição da CLI", () => {
    test("mantem o catalogo de scripts sincronizado com a arvore da CLI", async () => {
        const caminhos = CATALOGO_COMANDOS_COMPLETO.map(definicao => definicao.caminho.join(" "));
        expect(new Set(caminhos).size).toBe(caminhos.length);

        for (const definicao of CATALOGO_COMANDOS_COMPLETO) {
            let comando = program;
            for (const segmento of definicao.caminho) {
                const proximo = comando.commands.find(item => item.name() === segmento);
                expect(proximo, `Comando ausente: ${definicao.caminho.join(" ")}`).toBeDefined();
                if (!proximo) {
                    throw new Error(`Comando ausente: ${definicao.caminho.join(" ")}`);
                }
                comando = proximo;
            }

            expect(comando.description()).toBe(definicao.descricao);
            const arquivoExiste = "arquivo" in definicao
                ? await existe(path.join(DIRETORIO_RAIZ, "toolkit", definicao.arquivo))
                : true;
            expect(arquivoExiste).toBe(true);
        }
    });

    test("pode ser importada sem executar a CLI", async () => {
        const caminhoSgc = pathToFileURL(CAMINHO_SGC).href;
        const resultado = await execa(process.execPath, [
            "--import=tsx",
            "--input-type=module",
            "-e",
            `process.argv.push("--help"); await import(${JSON.stringify(caminhoSgc)}); process.stdout.write("importacao-ok\\n");`
        ], {
            cwd: DIRETORIO_RAIZ,
            reject: false
        });

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toBe("importacao-ok");
    });

    test("binario npm executa a entrada TypeScript pelo tsx", async () => {
        const resultado = await execa("npm", [
            "exec",
            "--workspace",
            "toolkit",
            "sgc",
            "--",
            "--help",
        ], {
            cwd: DIRETORIO_RAIZ,
            reject: false,
        });

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Toolkit do SGC");
    });

    test("CLI compilada despacha scripts TypeScript pelo tsx", async () => {
        const compilacao = await execa("npm", ["run", "build"], {
            cwd: path.join(DIRETORIO_RAIZ, "toolkit"),
            reject: false
        });
        expect(compilacao.exitCode).toBe(0);

        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cli-compilada-"));
        await escreverArquivo(
            path.join(base, "frontend", "src", "Exemplo.ts"),
            "export function exemplo(valor: unknown) { return valor; }\n"
        );

        const resultado = await execaNode(CAMINHO_SGC_COMPILADO, [
            "codigo",
            "cheiros",
            "auditar",
            "--json",
            "--base",
            base
        ], {
            cwd: DIRETORIO_RAIZ,
            reject: false
        });

        expect(resultado.exitCode).toBe(0);
        expect(JSON.parse(String(resultado.stdout)).base).toBe(base);
    });
});
