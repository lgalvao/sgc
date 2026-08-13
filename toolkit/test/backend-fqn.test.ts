import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {
    executarSgc,
    escreverArquivo,
    escreverJson,
    lerArquivo
} from "./apoio.js";
import {VERSAO_CONFIGURACAO} from "../lib/configuracao.js";

describe("Correção de FQN backend", () => {
    test("corrige FQNs em uma raiz de backend externa no modo simulacao", async () => {
        const diretorioBackend = await mkdtemp(path.join(os.tmpdir(), "sgc-corrigir-fqn-"));
        const caminhoJava = path.join(diretorioBackend, "src", "main", "java", "exemplo", "Exemplo.java");
        const conteudoOriginal = [
            "package exemplo;",
            "",
            "public class Exemplo {",
            "    com.externo.Alvo alvo;",
            "}"
        ].join("\n");
        await escreverArquivo(caminhoJava, conteudoOriginal);

        const resultado = await executarSgc([
            "backend",
            "java",
            "corrigir-fqn",
            "--base",
            diretorioBackend
        ]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("[simulação] Seria atualizado");
        expect(await lerArquivo(caminhoJava, "utf8")).toBe(conteudoOriginal);
    });

    test("corrige FQNs no modo de escrita sem duplicar linhas e permanece idempotente", async () => {
        const diretorioBackend = await mkdtemp(path.join(os.tmpdir(), "sgc-corrigir-fqn-escrita-"));
        const caminhoJava = path.join(diretorioBackend, "src", "main", "java", "exemplo", "Exemplo.java");
        const conteudoOriginal = [
            "package exemplo;",
            "",
            "public class Exemplo {",
            "    com.externo.Alvo alvo;",
            "}"
        ].join("\n");
        const conteudoEsperado = [
            "package exemplo;",
            "import com.externo.Alvo;",
            "",
            "public class Exemplo {",
            "    Alvo alvo;",
            "}"
        ].join("\n");
        await escreverArquivo(caminhoJava, conteudoOriginal);

        const primeiraExecucao = await executarSgc([
            "backend",
            "java",
            "corrigir-fqn",
            "--base",
            diretorioBackend
        ]);

        expect(primeiraExecucao.exitCode).toBe(0);
        expect(primeiraExecucao.stdout).toContain("[simulação] Seria atualizado");
        expect(await lerArquivo(caminhoJava, "utf8")).toBe(conteudoOriginal);

        const segundaExecucao = await executarSgc([
            "backend",
            "java",
            "corrigir-fqn",
            "--base",
            diretorioBackend,
            "--gravar"
        ]);

        expect(segundaExecucao.exitCode).toBe(0);
        expect(segundaExecucao.stdout).toContain("Atualizado");
        expect(await lerArquivo(caminhoJava, "utf8")).toBe(conteudoEsperado);

        const terceiraExecucao = await executarSgc([
            "backend",
            "java",
            "corrigir-fqn",
            "--base",
            diretorioBackend,
            "--gravar"
        ]);

        expect(terceiraExecucao.exitCode).toBe(0);
        expect(terceiraExecucao.stdout).toContain("Total de arquivos atualizados: 0");
        expect(await lerArquivo(caminhoJava, "utf8")).toBe(conteudoEsperado);
    });

    test("corrige FQNs nos diretorios Java configurados", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-corrigir-fqn-configurado-"));
        await escreverJson(path.join(diretorioBase, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                backendCodigo: "servidor/java",
                backendTestes: "servidor/testes"
            }
        });

        const conteudoJava = [
            "package exemplo;",
            "",
            "public class Exemplo {",
            "    com.externo.Alvo alvo;",
            "}"
        ].join("\n");
        const caminhoFonte = path.join(diretorioBase, "servidor", "java", "exemplo", "Exemplo.java");
        const caminhoTeste = path.join(diretorioBase, "servidor", "testes", "exemplo", "ExemploTest.java");
        await escreverArquivo(caminhoFonte, conteudoJava);
        await escreverArquivo(caminhoTeste, conteudoJava.replace("Exemplo", "ExemploTest"));

        const resultado = await executarSgc([
            "backend",
            "java",
            "corrigir-fqn",
            "--base",
            diretorioBase,
            "--gravar"
        ]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Total de arquivos atualizados: 2");
        expect(await lerArquivo(caminhoFonte, "utf8")).toContain("import com.externo.Alvo;");
        expect(await lerArquivo(caminhoTeste, "utf8")).toContain("import com.externo.Alvo;");
    });
});
