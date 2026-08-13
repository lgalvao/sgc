import os from "node:os";
import path from "node:path";
import {access, mkdtemp, mkdir, writeFile} from "node:fs/promises";
import {execa} from "execa";
import {describe, expect, test} from "vitest";

const DIRETORIO_RAIZ = path.resolve(import.meta.dirname, "..", "..");
const CAMINHO_SGC = path.join(DIRETORIO_RAIZ, "toolkit", "sgc.ts");
const CAMINHO_TSX = path.join(
    DIRETORIO_RAIZ,
    "node_modules",
    ".bin",
    process.platform === "win32" ? "tsx.cmd" : "tsx"
);

interface ResultadoExecucao {
    exitCode?: number;
    stdout: string;
    stderr: string;
}

async function escreverArquivo(caminho: string, conteudo: string): Promise<void> {
    await mkdir(path.dirname(caminho), {recursive: true});
    await writeFile(caminho, conteudo, "utf8");
}

async function executarSgc(diretorioBase: string, argumentos: string[]): Promise<ResultadoExecucao> {
    const resultado = await execa(CAMINHO_TSX, [CAMINHO_SGC, ...argumentos], {
        cwd: diretorioBase,
        reject: false,
    });
    return {
        exitCode: resultado.exitCode,
        stdout: String(resultado.stdout),
        stderr: String(resultado.stderr),
    };
}

describe("reuso do toolkit em projeto externo", () => {
    test("usa layout Java/Vue configurado sem depender dos caminhos do SGC", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-projeto-externo-"));

        await escreverArquivo(
            path.join(diretorioBase, "servidor", "src", "main", "java", "exemplo", "controle", "ExemploController.java"),
            [
                "package exemplo.controle;",
                "",
                "public class ExemploController {",
                "    public String consultar() {",
                "        return \"ok\";",
                "    }",
                "}"
            ].join("\n")
        );
        await escreverArquivo(
            path.join(diretorioBase, "cliente", "src", "views", "ExemploView.vue"),
            [
                "<template>",
                "  <main data-testid=\"exemplo\">Exemplo</main>",
                "</template>",
                "",
                "<script setup lang=\"ts\">",
                "const titulo = \"Exemplo\";",
                "</script>"
            ].join("\n")
        );
        await escreverArquivo(
            path.join(diretorioBase, "configuracao-toolkit.json"),
            JSON.stringify({
                versao: 1,
                diretorios: {
                    backendCodigo: "servidor/src/main/java",
                    frontendCodigo: "cliente/src",
                    artefatosQualidade: "artefatos/qualidade"
                }
            })
        );

        const auditoriaBackend = await executarSgc(diretorioBase, [
            "backend",
            "arquitetura",
            "auditar",
            "--json",
            "--base",
            diretorioBase
        ]);
        expect(auditoriaBackend.exitCode).toBe(0);
        expect(JSON.parse(auditoriaBackend.stdout)).toMatchObject({
            resumo: {totalAnalisados: 1},
            todos: [{caminhoRelativo: "servidor/src/main/java/exemplo/controle/ExemploController.java"}]
        });

        const auditoriaFrontend = await executarSgc(diretorioBase, [
            "frontend",
            "arquitetura",
            "auditar",
            "--json",
            "--base",
            diretorioBase
        ]);
        expect(auditoriaFrontend.exitCode).toBe(0);
        expect(JSON.parse(auditoriaFrontend.stdout)).toMatchObject({
            base: diretorioBase,
            resumo: {arquivosProducao: 1}
        });

        const identificadores = await executarSgc(diretorioBase, [
            "frontend",
            "identificadores-teste",
            "listar",
            "--json",
            "--base",
            diretorioBase
        ]);
        expect(identificadores.exitCode).toBe(0);
        expect(JSON.parse(identificadores.stdout)).toMatchObject({
            diretorioBusca: path.join(diretorioBase, "cliente", "src"),
            identificadores: [{arquivo: "cliente/src/views/ExemploView.vue", valor: "exemplo"}]
        });

        const residuos = await executarSgc(diretorioBase, [
            "frontend",
            "residuos",
            "auditar",
            "--json",
            "--base",
            diretorioBase
        ]);
        expect(residuos.exitCode).toBe(0);
        expect(JSON.parse(residuos.stdout)).toMatchObject({
            base: diretorioBase,
            resumo: {arquivosFrontend: 1}
        });

        await expect(access(path.join(diretorioBase, "backend", "src"))).rejects.toThrow("ENOENT");
        await expect(access(path.join(diretorioBase, "frontend", "src"))).rejects.toThrow("ENOENT");
        await expect(access(path.join(diretorioBase, "toolkit"))).rejects.toThrow("ENOENT");
    }, 60000);
});
