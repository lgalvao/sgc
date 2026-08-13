// Comando de inventário consolidado dos casos de uso CDU.

import path from "node:path";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverLinha, imprimirJson} from "../biblioteca/saida.js";
import {obterOpcoesCdu} from "./cdus-opcoes.js";
import {inventariarCdus, type InventarioCdus, type InventarioFormatos} from "./cdus-inventario-motor.js";

function imprimirResumoMapa(titulo: string, mapa: Record<string, number>): void {
    const itens = Object.entries(mapa).slice(0, 8);
    escreverLinha(`${titulo}: ${Object.keys(mapa).length} valores`);
    for (const [valor, quantidade] of itens) {
        escreverLinha(`  ${quantidade}x ${valor}`);
    }
}

function imprimirFormatos(formatos: InventarioFormatos): void {
    imprimirResumoMapa("Atores", formatos.formatosAtor);
    imprimirResumoMapa("Pré-condições", formatos.formatosPreCondicoes);
    imprimirResumoMapa("Fluxo principal", formatos.formatosFluxoPrincipal);
    escreverLinha(`Numeração repetida: ${formatos.documentosComReinicioNumeracao.length}`);
    escreverLinha(`Numeração regressiva: ${formatos.documentosComRegressaoNumeracao.length}`);
}

function imprimirInventario(resultado: InventarioCdus): void {
    escreverLinha(`Inventário consolidado dos CDUs em ${path.join(resultado.base, "specs")}`);
    escreverLinha(`Arquivos analisados: ${resultado.totalArquivos}`);
    escreverLinha();

    const {formatos, vocabulario, mensagens, densidade, duplicacoes} = resultado.secoes;
    if (formatos) {
        escreverLinha("[formatos]");
        imprimirFormatos(formatos);
        escreverLinha();
    }
    if (vocabulario) {
        escreverLinha("[vocabulário]");
        imprimirResumoMapa("Perfis", vocabulario.perfis);
        imprimirResumoMapa("Situações", vocabulario.situacoes);
        imprimirResumoMapa("Tipos de processo", vocabulario.tiposProcesso);
        imprimirResumoMapa("Elementos de interface", vocabulario.elementosUi);
        escreverLinha();
    }
    if (mensagens) {
        escreverLinha("[mensagens]");
        imprimirResumoMapa("Descrições", mensagens.descricoes);
        imprimirResumoMapa("Assuntos", mensagens.assuntos);
        imprimirResumoMapa("Mensagens", mensagens.mensagens);
        imprimirResumoMapa("Toasts", mensagens.toasts);
        escreverLinha();
    }
    if (densidade) {
        escreverLinha("[densidade]");
        escreverLinha(`Média: ${densidade.resumo.mediaPalavras.toFixed(1)} palavras e ${densidade.resumo.mediaPassos.toFixed(1)} passos`);
        escreverLinha(`Máximo: ${densidade.resumo.maxPalavras} palavras e ${densidade.resumo.maxPassos} passos`);
        escreverLinha();
    }
    if (duplicacoes) {
        escreverLinha("[duplicações]");
        escreverLinha(`Itens duplicados: ${duplicacoes.duplicacoes.length}`);
    }
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const argumentosValidados = validarArgumentosEntradaDireta(import.meta.url, argumentos);
    const {emitirJson, base, secoes} = obterOpcoesCdu(argumentosValidados);
    const resultado = await inventariarCdus(base, secoes);

    if (emitirJson) {
        imprimirJson(resultado);
        return;
    }

    imprimirInventario(resultado);
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}
