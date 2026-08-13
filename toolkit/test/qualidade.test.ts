import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import fs from "fs-extra";
import {describe, expect, test} from "vitest";
import {DIRETORIO_RAIZ, executarSgc} from "./apoio.js";
import {VERSAO_CONFIGURACAO} from "../lib/configuracao.js";
import {
    obterOpcoesPlaywright,
    principal as coletarFotografiaQualidade,
    type AdaptadorQualidade
} from "../qualidade/coleta-execucao.js";

const FIXTURE_FOTOGRAFIA = path.join(DIRETORIO_RAIZ, "toolkit", "test", "fixtures", "qualidade", "fotografia.json");

describe("Qualidade do toolkit", () => {
    test("resume uma fotografia de qualidade a partir de fixture", async () => {
        const resultado = await executarSgc(["qualidade", "resumo", "--json", "--arquivo", FIXTURE_FOTOGRAFIA]);
        expect(resultado.exitCode).toBe(0);

        const json = JSON.parse(resultado.stdout);
        expect(json.resumo.statusGeral).toBe("verde");
        expect(json.hotspots).toHaveLength(2);
    });

    test("resume a fotografia mais recente a partir da base externa", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-resumo-base-"));
        const caminhoFotografia = path.join(diretorioBase, "toolkit", "qualidade", "artefatos", "mais-recente", "fotografia.json");
        await fs.ensureDir(path.dirname(caminhoFotografia));
        await fs.writeJson(caminhoFotografia, {
            resumo: {
                statusGeral: "verde",
                indiceSaude: 98,
                totais: {verificacoes: 1}
            },
            verificacoes: [],
            hotspots: []
        });

        const resultado = await executarSgc(["qualidade", "resumo", "--json", "--base", diretorioBase]);

        expect(resultado.exitCode).toBe(0);
        const json = JSON.parse(resultado.stdout);
        expect(json.resumo.statusGeral).toBe("verde");
        expect(json.resumo.indiceSaude).toBe(98);
        expect(json.caminho).toBe("toolkit/qualidade/artefatos/mais-recente/fotografia.json");
    });

    test("exibe ajuda de coleta de fotografia com opcao de perfil", async () => {
        const resultado = await executarSgc(["qualidade", "coletar", "--help"]);
        expect(resultado.exitCode).toBe(0);
        expect(resultado.stdout).toContain("Perfil de execucao");
        expect(resultado.stdout).toContain("rapido");
    });

    test("falha rapido para perfil invalido de fotografia", async () => {
        const resultado = await executarSgc(["qualidade", "coletar", "--perfil", "inexistente"]);
        expect(resultado.exitCode).toBe(1);
        expect(resultado.stderr).toContain("Perfil invalido");
    });

    test("resolve a configuracao Playwright a partir dos testes de integracao da base", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-playwright-configurado-"));
        await fs.outputJSON(path.join(diretorioBase, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                testesIntegracao: "testes-e2e"
            }
        });

        expect(obterOpcoesPlaywright(diretorioBase)).toEqual({
            descricao: "npx playwright test --config=testes-e2e/playwright.config.ts",
            argumentos: [
                "playwright",
                "test",
                "--config=testes-e2e/playwright.config.ts",
                "--reporter=json"
            ]
        });
    });

    test("rejeita adaptador ausente antes de criar artefatos da coleta", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-adaptador-ausente-"));

        await expect(coletarFotografiaQualidade(["--perfil", "externo", "--base", diretorioBase], {
            perfis: {externo: ["adaptadorAusente"]},
            adaptadores: {}
        })).rejects.toThrow("adaptadorAusente");

        expect(await fs.pathExists(path.join(diretorioBase, "toolkit"))).toBe(false);
    });

    test("aceita uma fabrica de contexto de artefatos para projeto externo", async () => {
        const diretorioBase = await mkdtemp(path.join(os.tmpdir(), "sgc-qualidade-contexto-externo-"));
        const diretorioArtefatos = path.join(diretorioBase, ".qualidade");
        const caminhoFotografiaPersonalizado = path.join(diretorioBase, "relatorios", "qualidade.json");
        let baseRecebida = "";
        const adaptador: AdaptadorQualidade = async contexto => ({
            codigo: "checagemExterna",
            nome: "Checagem externa",
            categoria: "qualidade",
            status: "sucesso",
            duracaoMs: 0,
            comando: "checagem",
            diretorio: contexto.base,
            sumario: "",
            metricas: {},
            erros: [],
            artefatos: []
        });

        const fotografia = await coletarFotografiaQualidade(["--perfil", "externo", "--base", diretorioBase], {
            perfis: {externo: ["checagemExterna"]},
            adaptadores: {checagemExterna: adaptador},
            coletarMetadados: async () => ({origem: "externa"}),
            prepararDiretoriosFotografia: async () => {},
            persistirFotografia: async fotografiaGerada => {
                await fs.outputJSON(caminhoFotografiaPersonalizado, fotografiaGerada);
                return caminhoFotografiaPersonalizado;
            },
            criarContexto: base => {
                baseRecebida = base;
                return {
                    base,
                    diretorioArtefatos,
                    diretorioExecucoes: path.join(diretorioArtefatos, "execucoes"),
                    diretorioMaisRecente: path.join(diretorioArtefatos, "mais-recente"),
                    diretorioBackend: path.join(base, "servidor"),
                    diretorioFrontend: path.join(base, "cliente"),
                    diretorioFrontendCodigo: path.join(base, "cliente", "src")
                };
            }
        });

        expect(baseRecebida).toBe(diretorioBase);
        expect(fotografia.metadados.git).toEqual({origem: "externa"});
        expect(fotografia.verificacoes).toHaveLength(1);
        expect(await fs.pathExists(caminhoFotografiaPersonalizado)).toBe(true);
        expect(await fs.pathExists(diretorioArtefatos)).toBe(false);
        expect(await fs.pathExists(path.join(diretorioBase, "toolkit"))).toBe(false);
    });
}, 30000);
