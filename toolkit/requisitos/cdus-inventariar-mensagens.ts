
import {lerArquivo, listarArquivosCdu} from "./cdus-documentos-lib.js";
import {
    acumularMapa,
    extrairAssuntos,
    extrairDescricoes,
    extrairMensagens,
    extrairToasts,
    ordenarMapa
} from "./cdus-mensagens-lib.js";

type MapaContagem = Record<string, number>;

interface InventarioMensagens {
    base: string;
    totalArquivos: number;
    descricoes: MapaContagem;
    assuntos: MapaContagem;
    mensagens: MapaContagem;
    toasts: MapaContagem;
}

async function inventariarMensagens(base: string, arquivosInformados?: string[]): Promise<InventarioMensagens> {
    const arquivos = arquivosInformados ?? await listarArquivosCdu(base);
    const inventario: InventarioMensagens = {
        base,
        totalArquivos: arquivos.length,
        descricoes: {},
        assuntos: {},
        mensagens: {},
        toasts: {}
    };

    for (const caminhoArquivo of arquivos) {
        const texto = lerArquivo(caminhoArquivo);
        for (const descricao of extrairDescricoes(texto)) {
            acumularMapa(inventario.descricoes, descricao);
        }
        for (const assunto of extrairAssuntos(texto)) {
            acumularMapa(inventario.assuntos, assunto);
        }
        for (const mensagem of extrairMensagens(texto)) {
            acumularMapa(inventario.mensagens, mensagem);
        }
        for (const toast of extrairToasts(texto)) {
            acumularMapa(inventario.toasts, toast);
        }
    }

    inventario.descricoes = ordenarMapa(inventario.descricoes);
    inventario.assuntos = ordenarMapa(inventario.assuntos);
    inventario.mensagens = ordenarMapa(inventario.mensagens);
    inventario.toasts = ordenarMapa(inventario.toasts);

    return inventario;
}

export {
    inventariarMensagens
};
