import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import fs from "fs-extra";
import {describe, expect, test} from "vitest";
import {carregarConfiguracao, validarConfiguracao, VERSAO_CONFIGURACAO} from "../lib/configuracao.js";
import {resolverEscoposAuditoria} from "../projeto/dependencias-auditar.js";
import {resolverEscoposInstalacao} from "../projeto/preparar.js";
import {executarPerfilQualidade} from "../projeto/qualidade.js";

type ChamadaComando = {
    comando: string;
    argumentos: readonly string[];
    diretorio?: string;
};

describe("Configuracao do toolkit", () => {
    test("carrega configuracao versionada e preserva defaults ausentes", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-configuracao-valida-"));
        await fs.outputJSON(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            diretorios: {
                backendCodigo: "servidor/java"
            }
        });

        const configuracao = carregarConfiguracao(base);

        expect(configuracao.versao).toBe(VERSAO_CONFIGURACAO);
        expect(configuracao.diretorios.backendCodigo).toBe("servidor/java");
        expect(configuracao.diretorios.frontend).toBe("frontend");
    });

    test("rejeita configuracao com versao, chave ou caminho invalido", () => {
        expect(() => validarConfiguracao({diretorios: {}})).toThrow("deve informar a versão");
        expect(() => validarConfiguracao({versao: 2})).toThrow("versão 2");
        expect(() => validarConfiguracao({versao: VERSAO_CONFIGURACAO, diretorios: {backendCodigoo: "servidor/java"}})).toThrow("backendCodigoo");
        expect(() => validarConfiguracao({versao: VERSAO_CONFIGURACAO, diretorios: {backendCodigo: 42}})).toThrow("backendCodigo");
        expect(() => validarConfiguracao({versao: VERSAO_CONFIGURACAO, diretorios: {backendCodigo: "   "}})).toThrow("não vazio");
        expect(() => validarConfiguracao({
            versao: VERSAO_CONFIGURACAO,
            execucoes: {qualidade: {rapido: {descricao: "", tarefas: []}}}
        })).toThrow("execucoes.qualidade.rapido.descricao");
        expect(() => validarConfiguracao({
            versao: VERSAO_CONFIGURACAO,
            execucoes: {dependencias: [{titulo: "auditoria", segmento: ".", comando: "npm", argumentos: [42]}]}
        })).toThrow("execucoes.dependencias[0].argumentos");
    });

    test("configura execucoes de projeto sem substituir os defaults do SGC", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-configuracao-execucoes-"));
        await fs.outputJSON(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            execucoes: {
                dependencias: [{
                    titulo: "Auditar cliente",
                    segmento: "cliente",
                    comando: "npm",
                    argumentos: ["run", "auditar-dependencias"]
                }],
                qualidade: {
                    rapido: {
                        descricao: "Qualidade externa",
                        tarefas: [{titulo: "Verificar cliente", comando: "node", argumentos: ["--version"]}]
                    }
                },
                instalacao: [{titulo: "Instalar cliente", segmento: "cliente"}]
            }
        });

        expect(resolverEscoposAuditoria(base)).toMatchObject([{
            titulo: "Auditar cliente",
            segmento: "cliente",
            diretorio: path.join(base, "cliente")
        }]);
        expect(resolverEscoposInstalacao(base)).toEqual([{
            titulo: "Instalar cliente",
            segmento: "cliente",
            caminho: path.join(base, "cliente")
        }]);

        const chamadas: ChamadaComando[] = [];
        await executarPerfilQualidade("rapido", {
            base,
            executarComando: async (comando, argumentos, diretorio) => {
                chamadas.push({comando, argumentos, diretorio});
            }
        });
        expect(chamadas).toEqual([{
            comando: "node",
            argumentos: ["--version"],
            diretorio: base
        }]);
    });
}, 30000);
