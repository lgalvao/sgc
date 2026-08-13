// Inventário de vocabulário controlado dos casos de uso CDU.

import path from "node:path";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";
import {lerArquivo, listarArquivosCdu, obterOpcoesCdu} from "./cdus-lib.js";
import {carregarSituacoesCanonicas, obterVocabularioCanonico} from "./cdus-vocabulario-lib.js";

type MapaContagem = Record<string, number>;

interface InventarioVocabulario {
    base: string;
    totalArquivos: number;
    perfis: MapaContagem;
    situacoes: MapaContagem;
    tiposProcesso: MapaContagem;
    elementosUi: MapaContagem;
    canonicos: {
        perfis: string[];
        situacoes: string[];
        tiposProcesso: string[];
    };
}

function acumularMapa(mapa: MapaContagem, chave: string): void {
    mapa[chave] = (mapa[chave] ?? 0) + 1;
}

function ordenarMapa(mapa: MapaContagem): MapaContagem {
    return Object.fromEntries(
        Object.entries(mapa).toSorted((a, b) => b[1] - a[1] || a[0].localeCompare(b[0], "pt-BR"))
    );
}

function extrairItensListaAtores(texto: string): string[] {
    const linhas = texto.split(/\r?\n/);
    const indiceAtores = linhas.findIndex(linha => /^##\s+Atores\s*$/.test(linha));
    const indicePre = linhas.findIndex(linha => /^##\s+Pré-condições\s*$/.test(linha));
    if (indiceAtores < 0 || indicePre < 0 || indicePre <= indiceAtores) {
        return [];
    }

    return linhas
        .slice(indiceAtores + 1, indicePre)
        .filter(linha => /^\s*-\s+/.test(linha))
        .map(linha => linha.replace(/^\s*-\s+/, "").trim());
}

async function inventariarVocabulario(base: string, arquivosInformados?: string[]): Promise<InventarioVocabulario> {
    const situacoesCanonicas = carregarSituacoesCanonicas(base);
    const vocabularioCanonico = obterVocabularioCanonico(base);
    const arquivos = arquivosInformados ?? await listarArquivosCdu(base);

    const inventario: InventarioVocabulario = {
        base,
        totalArquivos: arquivos.length,
        perfis: {},
        situacoes: {},
        tiposProcesso: {},
        elementosUi: {},
        canonicos: {
            perfis: [...vocabularioCanonico.perfis],
            situacoes: [...situacoesCanonicas],
            tiposProcesso: [...vocabularioCanonico.tiposProcesso]
        }
    };

    for (const caminhoArquivo of arquivos) {
        const texto = lerArquivo(caminhoArquivo);

        for (const perfil of extrairItensListaAtores(texto)) {
            acumularMapa(inventario.perfis, perfil);
        }

        for (const match of texto.match(/'[^'\n]+'/g) ?? []) {
            acumularMapa(inventario.situacoes, match.slice(1, -1));
        }

        for (const tipo of texto.match(/'(Mapeamento|Revisão|Diagnóstico)'/g) ?? []) {
            acumularMapa(inventario.tiposProcesso, tipo.slice(1, -1));
        }

        for (const elementoUi of texto.match(/`[^`\n]+`/g) ?? []) {
            acumularMapa(inventario.elementosUi, elementoUi.slice(1, -1));
        }
    }

    inventario.perfis = ordenarMapa(inventario.perfis);
    inventario.situacoes = ordenarMapa(inventario.situacoes);
    inventario.tiposProcesso = ordenarMapa(inventario.tiposProcesso);
    inventario.elementosUi = ordenarMapa(inventario.elementosUi);

    return inventario;
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const {emitirJson, base} = obterOpcoesCdu(argumentos);
    const inventario = await inventariarVocabulario(base);

    if (emitirJson) {
        imprimirJson(inventario);
        return;
    }

    escreverLinha(`Inventário de vocabulário dos CDUs em ${path.join(base, "specs")}`);
    escreverLinha(`Arquivos analisados: ${inventario.totalArquivos}`);
    escreverLinha();
    escreverLinha("Perfis encontrados:");
    for (const [valor, quantidade] of Object.entries(inventario.perfis)) {
        escreverLinha(`- ${quantidade}x ${valor}`);
    }
    escreverLinha();
    escreverLinha("Situações encontradas:");
    for (const [valor, quantidade] of Object.entries(inventario.situacoes)) {
        escreverLinha(`- ${quantidade}x '${valor}'`);
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}

export {
    inventariarVocabulario
};
