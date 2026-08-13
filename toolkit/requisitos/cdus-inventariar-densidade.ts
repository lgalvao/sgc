// Inventário de densidade documental dos casos de uso CDU.

import path from "node:path";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";
import {analisarArquivo, lerArquivo, listarArquivosCdu, obterOpcoesCdu} from "./cdus-lib.js";

interface DocumentoDensidade {
    arquivo: string;
    palavras: number;
    passos: number;
    profundidadeListas: number;
    placeholders: number;
    elementosUi: number;
}

interface ResumoDensidade {
    mediaPalavras: number;
    mediaPassos: number;
    maxPalavras: number;
    maxPassos: number;
    maxProfundidadeListas: number;
}

interface InventarioDensidade {
    base: string;
    totalArquivos: number;
    resumo: ResumoDensidade;
    documentos: DocumentoDensidade[];
}

function calcularProfundidadeMaxima(linhas: string[]): number {
    let maximo = 0;
    for (const linha of linhas) {
        const correspondencia = linha.match(/^(\s*)[-*]\s+/);
        if (!correspondencia) {
            continue;
        }
        const profundidade = Math.floor(correspondencia[1].length / 4) + 1;
        maximo = Math.max(maximo, profundidade);
    }
    return maximo;
}

async function inventariarDensidade(base: string, arquivosInformados?: string[]): Promise<InventarioDensidade> {
    const arquivos = arquivosInformados ?? await listarArquivosCdu(base);
    const documentos: DocumentoDensidade[] = arquivos.map(caminhoArquivo => {
        const texto = lerArquivo(caminhoArquivo);
        const analise = analisarArquivo(caminhoArquivo, texto);
        const profundidadeListas = calcularProfundidadeMaxima(analise.linhas);
        return {
            arquivo: path.relative(base, caminhoArquivo).replaceAll("\\", "/"),
            palavras: analise.contagens.palavras,
            passos: analise.passos.length,
            profundidadeListas,
            placeholders: analise.contagens.placeholdersCanonicos + analise.contagens.placeholdersLegados,
            elementosUi: analise.contagens.uiEmCrases
        };
    });

    const totalPalavras = documentos.reduce((soma, doc) => soma + doc.palavras, 0);
    const totalPassos = documentos.reduce((soma, doc) => soma + doc.passos, 0);
    const resumo: ResumoDensidade = {
        mediaPalavras: documentos.length > 0 ? totalPalavras / documentos.length : 0,
        mediaPassos: documentos.length > 0 ? totalPassos / documentos.length : 0,
        maxPalavras: Math.max(...documentos.map(doc => doc.palavras), 0),
        maxPassos: Math.max(...documentos.map(doc => doc.passos), 0),
        maxProfundidadeListas: Math.max(...documentos.map(doc => doc.profundidadeListas), 0)
    };

    const resultado: InventarioDensidade = {
        base,
        totalArquivos: documentos.length,
        resumo,
        documentos: documentos.toSorted((a, b) => b.palavras - a.palavras || b.profundidadeListas - a.profundidadeListas)
    };

    return resultado;
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const {emitirJson, base} = obterOpcoesCdu(argumentos);
    const resultado = await inventariarDensidade(base);

    if (emitirJson) {
        imprimirJson(resultado);
        return;
    }

    escreverLinha(`Inventário de densidade dos CDUs em ${path.join(base, "specs")}`);
    escreverLinha(`Arquivos analisados: ${resultado.totalArquivos}`);
    escreverLinha(`Média de palavras: ${resultado.resumo.mediaPalavras.toFixed(1)}`);
    escreverLinha(`Média de passos: ${resultado.resumo.mediaPassos.toFixed(1)}`);
    escreverLinha();

    for (const doc of resultado.documentos.slice(0, 15)) {
        escreverLinha(`${doc.arquivo}: ${doc.palavras} palavras | ${doc.passos} passos | profundidade ${doc.profundidadeListas}`);
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}

export {
    inventariarDensidade
};
