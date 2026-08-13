import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {pathToFileURL} from "node:url";
import {describe, expect, test} from "vitest";
import {execa, execaNode} from "execa";
import {
    DIRETORIO_RAIZ,
    CAMINHO_FERRAMENTAS,
    escreverArquivo,
    existe
} from "./apoio.js";
import {CATALOGO_COMANDOS_COMPLETO} from "../biblioteca/catalogo-comandos.js";
import {program} from "../ferramentas.js";

const CAMINHO_FERRAMENTAS_COMPILADO = path.join(DIRETORIO_RAIZ, "toolkit", "dist", "ferramentas.js");

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

    test("declara finalidade e efeitos observáveis para cada comando", () => {
        for (const definicao of CATALOGO_COMANDOS_COMPLETO) {
            expect(["auditar", "inventariar", "gerar", "transformar", "orquestrar"])
                .toContain(definicao.finalidade);
            expect(definicao.efeitos).toEqual({
                persistencia: expect.stringMatching(/^(nenhuma|opcional|intrinseca)$/),
                remocao: expect.any(Boolean),
                subprocessos: expect.any(Boolean),
                rede: expect.any(Boolean),
            });
        }

        const exportarOpenapi = CATALOGO_COMANDOS_COMPLETO.find(item => item.caminho.join(" ") === "integracao contratos exportar-openapi");
        expect(exportarOpenapi?.efeitos).toMatchObject({persistencia: "intrinseca", rede: true});

        const limparArtefatos = CATALOGO_COMANDOS_COMPLETO.find(item => item.caminho.join(" ") === "projeto artefatos limpar");
        expect(limparArtefatos?.efeitos.remocao).toBe(true);
    });

    test("pode ser importada sem executar a CLI", async () => {
        const caminhoSgc = pathToFileURL(CAMINHO_FERRAMENTAS).href;
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
            "ferramentas",
            "--",
            "--help",
        ], {
            cwd: DIRETORIO_RAIZ,
            reject: false,
        });

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Toolkit de ferramentas de projeto");
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

        const resultado = await execaNode(CAMINHO_FERRAMENTAS_COMPILADO, [
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
