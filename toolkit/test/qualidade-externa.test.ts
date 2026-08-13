import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {existe} from "./apoio.js";
import {principal as coletarFotografiaQualidade, type AdaptadorQualidade} from "../qualidade/coleta-execucao.js";

describe("Fotografia de qualidade externa", () => {
    test("grava fotografia de qualidade na base externa", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-base-"));
        const adaptadores = ["testeExterno", "coberturaExterna"];
        const perfis = {externo: adaptadores};
        const adaptadoresFalsos: Record<string, AdaptadorQualidade> = {};
        for (const nome of adaptadores) {
            adaptadoresFalsos[nome] = async contexto => ({
                codigo: nome,
                nome,
                categoria: "qualidade",
                status: "sucesso",
                duracaoMs: 0,
                comando: "teste",
                diretorio: contexto.base,
                sumario: "",
                metricas: {},
                erros: [],
                artefatos: []
            });
        }

        const fotografia = await coletarFotografiaQualidade([
            "--perfil",
            "externo",
            "--base",
            base,
        ], {adaptadores: adaptadoresFalsos, perfis});

        const caminhoFotografia = path.join(
            base,
            "toolkit",
            "qualidade",
            "artefatos",
            "mais-recente",
            "fotografia.json"
        );
        expect(fotografia.resumo.statusGeral).toBe("verde");
        expect(fotografia.metadados.perfilExecucao).toBe("externo");
        expect(fotografia.verificacoes).toHaveLength(adaptadores.length);
        expect(await existe(caminhoFotografia)).toBe(true);
    });
});
