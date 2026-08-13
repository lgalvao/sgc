#!/usr/bin/env node

import path from "node:path";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";
import {lerArquivo, listarArquivosCdu, obterOpcoesCdu} from "./cdus-lib.js";
import {extrairAssuntos, extrairDescricoes, extrairMensagens, extrairToasts} from "./cdus-mensagens-lib.js";
import {
    carregarMensagensCanonicas,
    normalizarTextoComparacao,
    sugerirCanonicos,
    type CategoriaMensagem,
    type MensagemCanonica
} from "./cdus-mensagens-codigo-lib.js";

type TipoItem = "descricoes" | "assuntos" | "mensagens" | "toasts";

interface OcorrenciasMensagem {
    tipo: TipoItem;
    valor: string;
    ocorrencias: string[];
}

interface ReferenciaRelatorio {
    texto: string;
    origem: string;
    grupo: string;
}

interface SugestaoRelatorio extends ReferenciaRelatorio {
    similaridade: number;
}

interface ItemRelatorio {
    tipo: TipoItem;
    valor: string;
    quantidade: number;
    ocorrencias: string[];
    referenciasExatas: ReferenciaRelatorio[];
    sugestoes: SugestaoRelatorio[];
}

interface ResumoRelatorio {
    base: string;
    totalArquivos: number;
    totalItens: number;
    itensComReferenciaExata: number;
    itensSemReferenciaExata: number;
    itensComSugestao: number;
}

function consolidarOcorrencias(
    base: string,
    arquivos: string[],
    extrator: (texto: string) => string[],
    tipo: TipoItem
): OcorrenciasMensagem[] {
    const mapa = new Map<string, OcorrenciasMensagem>();

    for (const caminhoArquivo of arquivos) {
        const arquivoRelativo = path.relative(base, caminhoArquivo).replaceAll("\\", "/");
        const texto = lerArquivo(caminhoArquivo);
        for (const valor of extrator(texto)) {
            const chave = `${tipo}::${valor}`;
            const item = mapa.get(chave) ?? {tipo, valor, ocorrencias: []};
            item.ocorrencias.push(arquivoRelativo);
            mapa.set(chave, item);
        }
    }

    return [...mapa.values()];
}

function auditarItem(
    item: OcorrenciasMensagem,
    indiceCanonicos: Map<string, MensagemCanonica[]>,
    todosCanonicos: MensagemCanonica[]
): ItemRelatorio {
    const categorias: Record<TipoItem, CategoriaMensagem> = {
        descricoes: "descricao",
        assuntos: "assunto",
        mensagens: "mensagem",
        toasts: "toast"
    };
    const categoria = categorias[item.tipo];
    const canonicosCategoria = todosCanonicos.filter(canonico => canonico.categoria === categoria);
    const referenciasExatas = indiceCanonicos
        .get(normalizarTextoComparacao(item.valor))
        ?.filter(ref => ref.categoria === categoria) ?? [];
    const sugestoes = referenciasExatas.length === 0 ? sugerirCanonicos(item.valor, canonicosCategoria) : [];

    return {
        tipo: item.tipo,
        valor: item.valor,
        quantidade: item.ocorrencias.length,
        ocorrencias: item.ocorrencias,
        referenciasExatas: referenciasExatas.map(ref => ({
            texto: ref.texto,
            origem: ref.origem,
            grupo: ref.grupo
        })),
        sugestoes: sugestoes.map(ref => ({
            texto: ref.texto,
            origem: ref.origem,
            grupo: ref.grupo,
            similaridade: ref.similaridade
        }))
    };
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const {emitirJson, base} = obterOpcoesCdu(argumentos);

    const arquivos = await listarArquivosCdu(base);
    const {itens: canonicos, indice: indiceCanonicos} = carregarMensagensCanonicas(base);

    const itens = [
        ...consolidarOcorrencias(base, arquivos, extrairDescricoes, "descricoes"),
        ...consolidarOcorrencias(base, arquivos, extrairAssuntos, "assuntos"),
        ...consolidarOcorrencias(base, arquivos, extrairMensagens, "mensagens"),
        ...consolidarOcorrencias(base, arquivos, extrairToasts, "toasts")
    ].toSorted((a, b) => a.tipo.localeCompare(b.tipo, "pt-BR") || b.ocorrencias.length - a.ocorrencias.length || a.valor.localeCompare(b.valor, "pt-BR"));

    const relatorio = itens.map(item => auditarItem(item, indiceCanonicos, canonicos));
    const resumo: ResumoRelatorio = {
        base,
        totalArquivos: arquivos.length,
        totalItens: relatorio.length,
        itensComReferenciaExata: relatorio.filter(item => item.referenciasExatas.length > 0).length,
        itensSemReferenciaExata: relatorio.filter(item => item.referenciasExatas.length === 0).length,
        itensComSugestao: relatorio.filter(item => item.referenciasExatas.length === 0 && item.sugestoes.length > 0).length
    };

    if (emitirJson) {
        imprimirJson({resumo, relatorio});
        return;
    }

    escreverLinha(`Auditoria de mensagens dos CDUs contra o código em ${base}`);
    escreverLinha(`Arquivos analisados: ${resumo.totalArquivos}`);
    escreverLinha(`Itens auditados: ${resumo.totalItens}`);
    escreverLinha(`Com referência exata: ${resumo.itensComReferenciaExata}`);
    escreverLinha(`Sem referência exata: ${resumo.itensSemReferenciaExata}`);
    escreverLinha(`Sem referência exata, mas com sugestão: ${resumo.itensComSugestao}`);
    escreverLinha();

    for (const item of relatorio.filter(entrada => entrada.referenciasExatas.length === 0)) {
        escreverLinha(`${item.tipo}: ${item.valor}`);
        escreverLinha(`- ocorrências: ${item.quantidade}`);
        if (item.sugestoes.length === 0) {
            escreverLinha("- sugestões: nenhuma");
        } else {
            for (const sugestao of item.sugestoes) {
                escreverLinha(`- sugestão: ${sugestao.texto} (${sugestao.grupo}, ${sugestao.origem}, similaridade ${sugestao.similaridade})`);
            }
        }
        escreverLinha();
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}

export {
    principal
};
