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

interface ViolacaoJson {
    regra: string;
}

describe("Validadores estruturais do cliente", () => {
    test("valida previsibilidade estrutural das views em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-views-templates-"));
        const viewsDir = path.join(base, "frontend", "src", "views");

        await escreverArquivo(
            path.join(viewsDir, "PainelView.vue"),
            [
                "<template>",
                "  <LayoutPadrao>",
                "    <PageHeader title=\"Painel\" />",
                "  </LayoutPadrao>",
                "</template>"
            ].join("\n")
        );

        await escreverArquivo(
            path.join(viewsDir, "LegacyView.vue"),
            [
                "<template>",
                "  <div>",
                "    <BModal />",
                "  </div>",
                "</template>"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "cliente",
            "views",
            "templates-validar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(1);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.resumo.totalViews).toBe(2);
        expect(conteudo.violacoes.some((violacao: ViolacaoJson) => violacao.regra === "view-com-bmodal-cru")).toBe(true);
        expect(conteudo.violacoes.some((violacao: ViolacaoJson) => violacao.regra === "view-sem-layout-padrao")).toBe(true);
        expect(conteudo.violacoes.some((violacao: ViolacaoJson) => violacao.regra === "view-sem-cabecalho-padrao")).toBe(true);
    });

    test("valida padronizacao de modais em um recorte controlado", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-modais-validar-"));
        await escreverArquivo(
            path.join(base, "frontend", "src", "components", "comum", "ModalPadrao.vue"),
            "<template><BModal title=\"Base\" /></template>"
        );
        await escreverArquivo(
            path.join(base, "frontend", "src", "components", "mapa", "ImpactoMapaModal.vue"),
            "<template><BModal title=\"Impacto\" /></template>"
        );

        const resultado = await executarSgc([
            "cliente",
            "modais",
            "validar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(1);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.violacoes).toHaveLength(1);
        expect(conteudo.violacoes[0].arquivo).toBe("frontend/src/components/mapa/ImpactoMapaModal.vue");
        expect(conteudo.violacoes[0].regra).toBe("componente-com-bmodal-cru");
    });

    test("resolve codigoCliente configurado nos validadores estruturais", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cliente-validadores-configurados-"));
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {codigoCliente: "cliente/src"},
        });
        await escreverArquivo(
            path.join(base, "cliente", "src", "views", "PainelView.vue"),
            "<template><LayoutPadrao><PageHeader title=\"Painel\" /></LayoutPadrao></template>"
        );
        await escreverArquivo(
            path.join(base, "cliente", "src", "components", "comum", "ModalPadrao.vue"),
            "<template><BModal title=\"Base\" /></template>"
        );

        const [resultadoViews, resultadoModais] = await Promise.all([
            executarSgc(["cliente", "views", "templates-validar", "--json", "--base", base]),
            executarSgc(["cliente", "modais", "validar", "--json", "--base", base]),
        ]);

        expect(resultadoViews.exitCode).toBe(0);
        expect(JSON.parse(resultadoViews.stdout).resumo.totalViews).toBe(1);
        expect(resultadoModais.exitCode).toBe(0);
        expect(JSON.parse(resultadoModais.stdout).resumo.totalViolacoes).toBe(0);
    });
});
