import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import fs from "fs-extra";
import {describe, expect, test} from "vitest";
import {execaNode} from "execa";

const DIRETORIO_RAIZ = path.resolve(import.meta.dirname, "..", "..");
const CAMINHO_SGC = path.join(DIRETORIO_RAIZ, "toolkit", "sgc.js");

async function executarSgc(args, opcoes = {}) {
    return execaNode(CAMINHO_SGC, args, {
        cwd: DIRETORIO_RAIZ,
        reject: false,
        ...opcoes
    });
}

describe("Ferramentas de requisitos dos CDUs", () => {
    test("inventaria formatos implícitos dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-inventario-"));
        const dirSpecs = path.join(base, "specs");

        await fs.outputFile(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "",
                "## Pré-condições",
                "",
                "- Usuário autenticado",
                "",
                "## Fluxo principal",
                "",
                "1. O usuário acessa o `Painel`.",
                "2. O sistema muda a situação para 'Em andamento'."
            ].join("\n")
        );

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "inventariar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.totalArquivos).toBe(1);
        expect(conteudo.formatosAtor["## Atores"]).toBe(1);
        expect(conteudo.formatosPreCondicoes["## Pré-condições"]).toBe(1);
        expect(conteudo.formatosFluxoPrincipal["## Fluxo principal"]).toBe(1);
        expect(conteudo.situacoesMaisFrequentes["'Em andamento'"]).toBe(1);
        expect(conteudo.elementosUiMaisFrequentes["`Painel`"]).toBe(1);
    });

    test("audita a estrutura canônica mínima dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-auditoria-"));
        const dirSpecs = path.join(base, "specs");

        await fs.outputFile(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo válido",
                "",
                "## Atores",
                "",
                "- CHEFE",
                "",
                "## Pré-condições",
                "",
                "- Existe processo em andamento",
                "",
                "## Fluxo principal",
                "",
                "1. O usuário acessa o `Painel`.",
                "2. O sistema mostra a tela `Detalhes`."
            ].join("\n")
        );

        await fs.outputFile(
            path.join(dirSpecs, "cdu-02.md"),
            [
                "# CDU-99 - Exemplo inválido",
                "",
                "Ator: ADMIN",
                "",
                "## Pré-condição",
                "",
                "## Fluxo principal",
                "",
                "1. Primeiro passo.",
                "1. Passo repetido."
            ].join("\n")
        );

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "auditar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.resumo.totalArquivos).toBe(2);
        expect(conteudo.resumo.arquivosComErro).toBe(1);

        const invalido = conteudo.relatorio.find(item => item.arquivo === "specs/cdu-02.md");
        expect(invalido.achados.some(achado => achado.regra === "titulo_numero")).toBe(true);
        expect(invalido.achados.some(achado => achado.regra === "atores_canonicos")).toBe(true);
        expect(invalido.achados.some(achado => achado.regra === "pre_condicoes")).toBe(true);
        expect(invalido.achados.some(achado => achado.regra === "numeracao_repetida")).toBe(true);
    });
});
