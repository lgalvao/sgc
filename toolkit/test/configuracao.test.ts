import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {escreverJson} from "./apoio.js";
import {carregarConfiguracao, validarConfiguracao, VERSAO_CONFIGURACAO} from "../lib/configuracao.js";
import {resolverEscoposAuditoria} from "../projeto/dependencias-auditar.js";
import {executarTarefasQualidade} from "../qualidade/tarefas-executar.js";

type ChamadaComando = {
    comando: string;
    argumentos: readonly string[];
    diretorio?: string;
};

describe("Configuracao do toolkit", () => {
    test("carrega configuracao versionada e preserva defaults ausentes", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-configuracao-valida-"));
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
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
        expect(() => validarConfiguracao({
            versao: VERSAO_CONFIGURACAO,
            execucoes: {dependencias: [{titulo: "auditoria", segmento: ".", comando: "npm", argumentos: [], codigoNaoZeroIndicaAchados: "sim"}]}
        })).toThrow("codigoNaoZeroIndicaAchados deve ser booleano");
    });

    test("configura execucoes de projeto sem substituir os defaults do SGC", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-configuracao-execucoes-"));
        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            execucoes: {
                dependencias: [{
                    titulo: "Auditar cliente",
                    segmento: "cliente",
                    comando: "npm",
                    argumentos: ["run", "auditar-dependencias"],
                    codigoNaoZeroIndicaAchados: true
                }],
                qualidade: {
                    rapido: {
                        descricao: "Qualidade externa",
                        tarefas: [{titulo: "Verificar cliente", comando: "node", argumentos: ["--version"]}]
                    }
                }
            }
        });

        expect(resolverEscoposAuditoria(base)).toMatchObject([{
            titulo: "Auditar cliente",
            segmento: "cliente",
            diretorio: path.join(base, "cliente"),
            codigoNaoZeroIndicaAchados: true
        }]);
        const chamadas: ChamadaComando[] = [];
        await executarTarefasQualidade("rapido", {
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
