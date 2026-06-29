#!/usr/bin/env node

import path from "node:path";
import {imprimirJson} from "../lib/saida.js";
import {listarArquivosCdu, lerArquivo} from "./cdus-lib.js";
import {
    extrairAssuntos,
    extrairDescricoes,
    extrairMensagens,
    extrairToasts
} from "./cdus-mensagens-lib.js";
import {carregarMensagensCanonicas, normalizarTextoComparacao, sugerirCanonicos} from "./cdus-mensagens-codigo-lib.js";

function consolidarOcorrencias(base, arquivos, extrator, tipo) {
    const mapa = new Map();

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

function auditarItem(item, indiceCanonicos, todosCanonicos) {
    const categorias = {
        descricoes: "descricao",
        assuntos: "assunto",
        mensagens: "mensagem",
        toasts: "toast"
    };
    const categoria = categorias[item.tipo];
    const canonicosCategoria = todosCanonicos.filter(canonico => canonico.categoria === categoria);
    const referenciasExatas = indiceCanonicos.get(normalizarTextoComparacao(item.valor))?.filter(ref => ref.categoria === categoria) ?? [];
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

const args = process.argv.slice(2);
const emitirJson = args.includes("--json");
const indiceBase = args.indexOf("--base");
const base = indiceBase >= 0 ? path.resolve(args[indiceBase + 1]) : process.cwd();

const arquivos = await listarArquivosCdu(base);
const {itens: canonicos, indice: indiceCanonicos} = carregarMensagensCanonicas(base);

const itens = [
    ...consolidarOcorrencias(base, arquivos, extrairDescricoes, "descricoes"),
    ...consolidarOcorrencias(base, arquivos, extrairAssuntos, "assuntos"),
    ...consolidarOcorrencias(base, arquivos, extrairMensagens, "mensagens"),
    ...consolidarOcorrencias(base, arquivos, extrairToasts, "toasts")
].sort((a, b) => a.tipo.localeCompare(b.tipo, "pt-BR") || b.ocorrencias.length - a.ocorrencias.length || a.valor.localeCompare(b.valor, "pt-BR"));

const relatorio = itens.map(item => auditarItem(item, indiceCanonicos, canonicos));
const resumo = {
    base,
    totalArquivos: arquivos.length,
    totalItens: relatorio.length,
    itensComReferenciaExata: relatorio.filter(item => item.referenciasExatas.length > 0).length,
    itensSemReferenciaExata: relatorio.filter(item => item.referenciasExatas.length === 0).length,
    itensComSugestao: relatorio.filter(item => item.referenciasExatas.length === 0 && item.sugestoes.length > 0).length
};

if (emitirJson) {
    imprimirJson({resumo, relatorio});
    process.exit(0);
}

console.log(`Auditoria de mensagens dos CDUs contra o código em ${base}`);
console.log(`Arquivos analisados: ${resumo.totalArquivos}`);
console.log(`Itens auditados: ${resumo.totalItens}`);
console.log(`Com referência exata: ${resumo.itensComReferenciaExata}`);
console.log(`Sem referência exata: ${resumo.itensSemReferenciaExata}`);
console.log(`Sem referência exata, mas com sugestão: ${resumo.itensComSugestao}`);
console.log("");

for (const item of relatorio.filter(entrada => entrada.referenciasExatas.length === 0)) {
    console.log(`${item.tipo}: ${item.valor}`);
    console.log(`- ocorrências: ${item.quantidade}`);
    if (item.sugestoes.length === 0) {
        console.log("- sugestões: nenhuma");
    } else {
        for (const sugestao of item.sugestoes) {
            console.log(`- sugestão: ${sugestao.texto} (${sugestao.grupo}, ${sugestao.origem}, similaridade ${sugestao.similaridade})`);
        }
    }
    console.log("");
}
