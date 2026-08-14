import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {escreverJson} from "./apoio.js";
import {carregarConfiguracao, validarConfiguracao, VERSAO_CONFIGURACAO} from "../biblioteca/configuracao.js";
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
                codigoServidor: "servidor/java"
            }
        });

        const configuracao = carregarConfiguracao(base);

        expect(configuracao.versao).toBe(VERSAO_CONFIGURACAO);
        expect(configuracao.diretorios.codigoServidor).toBe("servidor/java");
        expect(configuracao.diretorios.cliente).toBe("frontend");
        expect(configuracao.requisitos.cdus.padraoArquivos).toBe("specs/cdu/cdu-*.md");
        expect(configuracao.requisitos.cdus.fontesMensagensCodigo).toHaveLength(7);
        expect(configuracao.requisitos.cdus.vocabulario.perfisCanonicos).toEqual(["ADMIN", "GESTOR", "CHEFE", "SERVIDOR"]);
        expect(configuracao.requisitos.cdus.vocabulario.arquivoSituacoesCanonicas).toBe("specs/intro_3_situacoes.md");
    });

    test("rejeita configuracao com versao, chave ou caminho invalido", () => {
        expect(() => validarConfiguracao({diretorios: {}})).toThrow("deve informar a versão");
        expect(() => validarConfiguracao({versao: 1})).toThrow("versão 1");
        expect(() => validarConfiguracao({versao: VERSAO_CONFIGURACAO, diretorios: {codigoServidoro: "servidor/java"}})).toThrow("codigoServidoro");
        expect(() => validarConfiguracao({versao: VERSAO_CONFIGURACAO, diretorios: {codigoServidor: 42}})).toThrow("codigoServidor");
        expect(() => validarConfiguracao({versao: VERSAO_CONFIGURACAO, diretorios: {codigoServidor: "   "}})).toThrow("não vazio");
        expect(() => validarConfiguracao({
            versao: VERSAO_CONFIGURACAO,
            requisitos: {cdus: {padraoArquivos: "   "}}
        })).toThrow("requisitos.cdus.padraoArquivos");
        expect(() => validarConfiguracao({
            versao: VERSAO_CONFIGURACAO,
            requisitos: {cdus: {padraoArquivoss: "docs/*.md"}}
        })).toThrow("padraoArquivoss");
        expect(() => validarConfiguracao({
            versao: VERSAO_CONFIGURACAO,
            requisitos: {cdus: {fontesMensagensCodigo: [{caminho: "mensagens.java", tipo: "kotlin"}]}}
        })).toThrow("tipo de fonte conhecido");
        expect(() => validarConfiguracao({
            versao: VERSAO_CONFIGURACAO,
            requisitos: {cdus: {vocabulario: {perfisCanonicos: ["ADMIN", ""]}}}
        })).toThrow("perfisCanonicos[1]");
        expect(() => validarConfiguracao({
            versao: VERSAO_CONFIGURACAO,
            requisitos: {cdus: {estilo: {perfisEmCrases: "ADMIN"}}}
        })).toThrow("perfisEmCrases deve ser uma lista");
        expect(() => validarConfiguracao({
            versao: VERSAO_CONFIGURACAO,
            requisitos: {cdus: {politicaMensagensCodigo: {categoriaInvalida: "descricao"}}}
        })).toThrow("categoriaInvalida");
        expect(() => validarConfiguracao({
            versao: VERSAO_CONFIGURACAO,
            requisitos: {cdus: {politicaMensagensCodigo: {regrasJava: [{prefixo: "HIST_", categoria: "evento", grupo: "historico"}]}}}
        })).toThrow("categoria de mensagem conhecida");
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
        expect(() => validarConfiguracao({
            versao: VERSAO_CONFIGURACAO,
            execucoes: {dependencias: [{titulo: "auditoria", segmento: ".", comando: "npm", argumentos: [], ignorarAtualizacoes: "sim"}]}
        })).toThrow("ignorarAtualizacoes deve ser uma lista");
        expect(() => validarConfiguracao({
            versao: VERSAO_CONFIGURACAO,
            execucoes: {dependencias: [{titulo: "auditoria", segmento: ".", comando: "npm", argumentos: [], ignorarAtualizacoes: [{pacote: "typescript", major: -1}]}]}
        })).toThrow("major deve ser inteiro não negativo");
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
                    codigoNaoZeroIndicaAchados: true,
                    ignorarAtualizacoes: [{pacote: "typescript", major: 7}]
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
            codigoNaoZeroIndicaAchados: true,
            ignorarAtualizacoes: [{pacote: "typescript", major: 7}]
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

        await escreverJson(path.join(base, "configuracao-toolkit.json"), {
            versao: VERSAO_CONFIGURACAO,
            requisitos: {
                cdus: {
                    politicaMensagensCodigo: {
                        assuntosJava: {prefixo: "APP: "}
                    }
                }
            }
        });
        const politica = carregarConfiguracao(base).requisitos.cdus.politicaMensagensCodigo;
        expect(politica.assuntosJava.prefixo).toBe("APP: ");
        expect(politica.assuntosJava.grupo).toBe("assunto_servidor");
        expect(politica.typescript.grupoResultado).toBe("resultado_cliente");
    });
}, 30000);
