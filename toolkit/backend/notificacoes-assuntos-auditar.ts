#!/usr/bin/env node
// Auditoria de assuntos de notificação do perfil SGC.

import fs from "node:fs/promises";
import path from "node:path";
import pc from "picocolors";
import {globby} from "globby";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

type RegraAssunto = "literal_sgc" | "assunto_literal" | "titulo_literal" | "builder_assunto_literal";

interface AchadoAssunto {
    linha: number;
    regra: RegraAssunto;
    trecho: string;
}

interface RelatorioArquivoAssuntos {
    arquivo: string;
    achados: AchadoAssunto[];
}

interface RelatorioAssuntos {
    base: string;
    resumo: {
        arquivosAnalisados: number;
        arquivosComViolacao: number;
        violacoes: number;
    };
    relatorio: RelatorioArquivoAssuntos[];
}

function normalizarCaminho(caminho: string): string {
    return caminho.replaceAll(path.sep, "/");
}

function extrairAchados(conteudo: string): AchadoAssunto[] {
    const achados: AchadoAssunto[] = [];
    const linhas = conteudo.split(/\r?\n/);

    for (const [indice, linha] of linhas.entries()) {
        if (linha.includes("\"SGC: ")) {
            const match = linha.match(/"SGC:\s*([^"]+)"/);
            achados.push({
                linha: indice + 1,
                regra: "literal_sgc",
                trecho: match ? `SGC: ${match[1]}` : linha.trim()
            });
            continue;
        }

        if (linha.match(/\bString\s+assunto\s*=\s*"/)) {
            achados.push({
                linha: indice + 1,
                regra: "assunto_literal",
                trecho: linha.trim()
            });
            continue;
        }

        if (linha.match(/setVariable\(VAR_TITULO,\s*"/)) {
            achados.push({
                linha: indice + 1,
                regra: "titulo_literal",
                trecho: linha.trim()
            });
            continue;
        }

        if (linha.match(/\.assunto\("/)) {
            achados.push({
                linha: indice + 1,
                regra: "builder_assunto_literal",
                trecho: linha.trim()
            });
        }
    }

    return achados;
}

async function auditarAssuntos(base: string): Promise<RelatorioAssuntos> {
    const diretorioBackend = resolverCaminhoConfigurado("backendCodigo", base);
    const arquivos = await globby(path.join(diretorioBackend, "**/*.java").replaceAll("\\", "/"), {absolute: true});
    const ignorados = new Set([
        normalizarCaminho(path.join(diretorioBackend, "alerta", "AssuntosNotificacao.java")),
        normalizarCaminho(path.join(diretorioBackend, "e2e", "E2eController.java"))
    ]);

    const relatorio: RelatorioArquivoAssuntos[] = [];
    for (const arquivo of arquivos) {
        const caminhoNormalizado = normalizarCaminho(arquivo);
        if (ignorados.has(caminhoNormalizado)) {
            continue;
        }
        const conteudo = await fs.readFile(arquivo, "utf-8");
        const achados = extrairAchados(conteudo);
        if (achados.length > 0) {
            relatorio.push({
                arquivo: normalizarCaminho(path.relative(base, arquivo)),
                achados
            });
        }
    }

    relatorio.sort((a, b) => a.arquivo.localeCompare(b.arquivo, "pt-BR"));
    return {
        base,
        resumo: {
            arquivosAnalisados: arquivos.length - ignorados.size,
            arquivosComViolacao: relatorio.length,
            violacoes: relatorio.flatMap(item => item.achados).length
        },
        relatorio
    };
}

function exibirAjuda(): void {
    exibirAjudaComando({
        comandoSgc: "backend notificacoes auditar-assuntos",
        scriptDireto: "backend/notificacoes-assuntos-auditar.ts",
        descricao: "Audita literais de assunto de notificação fora de AssuntosNotificacao.",
        opcoes: [
            "--json              Emite o relatório em JSON.",
            "--base <diretorio>  Sobrescreve o diretório base para auditoria.",
            "--help, -h          Exibe esta ajuda."
        ],
        exemplos: [
            "npx tsx toolkit/sgc.ts backend notificacoes auditar-assuntos",
            "npx tsx toolkit/sgc.ts backend notificacoes auditar-assuntos --json"
        ]
    });
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjuda();
        return;
    }

    const emitirJson = argumentos.includes("--json");
    const base = path.resolve(lerOpcao(argumentos, "--base", process.cwd()) ?? process.cwd());
    const resultado = await auditarAssuntos(base);

    if (emitirJson) {
        imprimirJson(resultado);
    } else {
        imprimirCabecalho("AUDITORIA DE ASSUNTOS DE NOTIFICACAO");
        escreverLinha(`Base analisada: ${pc.dim(resolverCaminhoConfigurado("backendCodigo", base))}`);
        escreverLinha(`Arquivos analisados: ${resultado.resumo.arquivosAnalisados}`);
        escreverLinha(`Arquivos com violação: ${resultado.resumo.arquivosComViolacao}`);
        escreverLinha(`Violações: ${resultado.resumo.violacoes}`);

        if (resultado.relatorio.length > 0) {
            escreverLinha("");
            for (const item of resultado.relatorio) {
                escreverLinha(item.arquivo);
                for (const achado of item.achados) {
                    escreverLinha(`- [falha] ${achado.regra} (linha ${achado.linha}): ${achado.trecho}`);
                }
            }
        }
    }

    if (resultado.resumo.violacoes > 0) {
        process.exitCode = 1;
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        escreverLinha(pc.red(`Erro ao auditar assuntos: ${erro instanceof Error ? erro.message : String(erro)}`));
        process.exitCode = 1;
    });
}

export {
    auditarAssuntos,
    principal
};
