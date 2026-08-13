#!/usr/bin/env node
// Inventário de duplicações textuais nos casos de uso CDU.

import path from "node:path";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";
import {lerArquivo, listarArquivosCdu, obterOpcoesCdu} from "./cdus-lib.js";
import {extrairAssuntos, extrairDescricoes, extrairMensagens, extrairToasts} from "./cdus-mensagens-lib.js";

type TipoDuplicacao = "bloco_texto" | "assunto" | "descricao" | "mensagem" | "toast";

interface ItemRegistrado {
    arquivos: string[];
    tipo: TipoDuplicacao;
    amostra: string;
}

interface Duplicacao {
    ocorrencias: number;
    arquivos: string[];
    tipo: TipoDuplicacao;
    amostra: string;
}

interface ResultadoDuplicacoes {
    base: string;
    totalArquivos: number;
    duplicacoes: Duplicacao[];
}

function normalizarBloco(texto: string): string {
    return texto
        .trim()
        .replaceAll(/\r/g, "")
        .replaceAll(/[ \t]+/g, " ")
        .replaceAll(/\n{2,}/g, "\n");
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const {emitirJson, base} = obterOpcoesCdu(argumentos);

    const arquivos = await listarArquivosCdu(base);
    const blocos = new Map<string, ItemRegistrado>();
    const itens = new Map<string, ItemRegistrado>();

    function registrar(
        mapa: Map<string, ItemRegistrado>,
        chave: string,
        arquivo: string,
        tipo: TipoDuplicacao,
        amostra: string
    ): void {
        const atual = mapa.get(chave) ?? {arquivos: [], tipo, amostra};
        atual.arquivos.push(arquivo);
        mapa.set(chave, atual);
    }

    for (const caminhoArquivo of arquivos) {
        const texto = lerArquivo(caminhoArquivo);
        const arquivoRelativo = path.relative(base, caminhoArquivo).replaceAll("\\", "/");
        for (const match of texto.matchAll(/```text\n([\s\S]*?)```/g)) {
            const bloco = normalizarBloco(match[1]);
            if (bloco.length < 40) {
                continue;
            }
            registrar(blocos, bloco, arquivoRelativo, "bloco_texto", bloco.split("\n").slice(0, 6).join("\n"));
        }

        for (const assunto of extrairAssuntos(texto)) {
            registrar(itens, `assunto:${assunto}`, arquivoRelativo, "assunto", assunto);
        }

        for (const descricao of extrairDescricoes(texto)) {
            registrar(itens, `descricao:${descricao}`, arquivoRelativo, "descricao", descricao);
        }

        for (const mensagem of extrairMensagens(texto)) {
            registrar(itens, `mensagem:${mensagem}`, arquivoRelativo, "mensagem", mensagem);
        }

        for (const toast of extrairToasts(texto)) {
            registrar(itens, `toast:${toast}`, arquivoRelativo, "toast", toast);
        }
    }

    const duplicacoes: Duplicacao[] = [
        ...[...blocos.values()].map(item => ({
            ocorrencias: item.arquivos.length,
            arquivos: [...new Set(item.arquivos)].toSorted((a, b) => a.localeCompare(b, "pt-BR")),
            tipo: item.tipo,
            amostra: item.amostra
        })),
        ...[...itens.values()].map(item => ({
            ocorrencias: item.arquivos.length,
            arquivos: [...new Set(item.arquivos)].toSorted((a, b) => a.localeCompare(b, "pt-BR")),
            tipo: item.tipo,
            amostra: item.amostra
        }))
    ]
        .filter(item => item.arquivos.length > 1)
        .toSorted((a, b) => b.ocorrencias - a.ocorrencias || a.arquivos[0].localeCompare(b.arquivos[0], "pt-BR"));

    const resultado: ResultadoDuplicacoes = {
        base,
        totalArquivos: arquivos.length,
        duplicacoes
    };

    if (emitirJson) {
        imprimirJson(resultado);
        return;
    }

    escreverLinha(`Inventário de duplicações dos CDUs em ${path.join(base, "specs")}`);
    escreverLinha(`Arquivos analisados: ${resultado.totalArquivos}`);
    escreverLinha(`Blocos duplicados: ${resultado.duplicacoes.length}`);
    escreverLinha();

    for (const duplicacao of resultado.duplicacoes.slice(0, 20)) {
        escreverLinha(`${duplicacao.ocorrencias}x [${duplicacao.tipo}] ${duplicacao.arquivos.join(", ")}`);
        escreverLinha(duplicacao.amostra);
        escreverLinha();
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}

export {
    principal
}
