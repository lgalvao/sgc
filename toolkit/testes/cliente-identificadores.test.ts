import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {
    executarSgc,
    escreverArquivo,
    escreverJson
} from "./apoio.js";
import {VERSAO_CONFIGURACAO} from "../biblioteca/configuracao.js";

describe("Identificadores de teste do cliente", () => {
    test("lista com sucesso identificadores de teste do cliente em recorte controlado", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-testids-listar-"));

        // Criar arquivos .vue de teste com identificadores
        await escreverArquivo(
            path.join(diretorioBase, "ComponenteA.vue"),
            "<template><button data-test-codigo=\"btn-salvar\">Salvar</button></template>"
        );
        await escreverArquivo(
            path.join(diretorioBase, "ComponenteB.vue"),
            "<template><input data-testid=\"input-nome\" /></template>"
        );

        const resultado = await executarSgc(["cliente", "identificadores-teste", "listar", "--diretorio", diretorioBase]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("ComponenteA.vue");
        expect(resultado.stdout).toContain("btn-salvar");
        expect(resultado.stdout).toContain("ComponenteB.vue");
        expect(resultado.stdout).toContain("input-nome");
    });

    test("resolve codigoCliente configurado para identificadores de teste", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-testids-configurado-"));
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {codigoCliente: "aplicacao/src"},
        });
        await escreverArquivo(
            path.join(base, "aplicacao", "src", "components", "Componente.vue"),
            "<template><button data-testid=\"btn-configurado\">Salvar</button></template>"
        );

        const resultado = await executarSgc(["cliente", "identificadores-teste", "listar", "--base", base]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("aplicacao/src/components/Componente.vue");
        expect(resultado.stdout).toContain("btn-configurado");
    });

    test("inventaria identificadores repetidos sem transformar repeticao textual em gate", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-testids-duplicados-"));

        // Criar dois arquivos com o mesmo test-id
        await escreverArquivo(
            path.join(diretorioBase, "ComponenteX.vue"),
            "<template><button data-testid=\"btn-acao\">Ação X</button></template>"
        );
        await escreverArquivo(
            path.join(diretorioBase, "ComponenteY.vue"),
            "<template><div data-testid=\"btn-acao\">Ação Y</div></template>"
        );

        const resultado = await executarSgc(["cliente", "identificadores-teste", "listar-duplicados", "--diretorio", diretorioBase]);

        // A repeticao global e apenas inventario: o motor nao conhece o DOM simultaneo nem as rotas renderizadas.
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Identificadores de teste duplicados encontrados");
        expect(resultado.stdout).toContain("btn-acao");
        expect(resultado.stdout).toContain("ComponenteX.vue");
        expect(resultado.stdout).toContain("ComponenteY.vue");
    });

    test("passa com sucesso se nao houver identificadores de teste duplicados", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-testids-unicos-"));

        await escreverArquivo(
            path.join(diretorioBase, "ComponenteUnico.vue"),
            "<template><button data-testid=\"btn-unico\">Ação Única</button></template>"
        );

        const resultado = await executarSgc(["cliente", "identificadores-teste", "listar-duplicados", "--diretorio", diretorioBase]);

        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Nenhum identificador de teste duplicado encontrado.");
    });
}, 30000);
