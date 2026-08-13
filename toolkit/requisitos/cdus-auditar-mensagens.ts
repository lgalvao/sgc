
import path from "node:path";
import {lerArquivo, listarArquivosCdu, obterLinhas} from "./cdus-documentos-lib.js";
import {extrairAssuntos, extrairDescricoes, extrairMensagens, extrairToasts} from "./cdus-mensagens-lib.js";

type Severidade = "aviso";
type RegraMensagens =
    | "descricao_espacamento"
    | "descricao_placeholder_legado"
    | "assunto_fechamento_suspeito"
    | "assunto_placeholder_legado"
    | "mensagem_placeholder_legado"
    | "toast_placeholder_legado";

interface AchadoMensagem {
    severidade: Severidade;
    regra: RegraMensagens;
    mensagem: string;
    linha: number | null;
}

interface RelatorioArquivoMensagens {
    arquivo: string;
    achados: AchadoMensagem[];
}

interface ResumoMensagens {
    base: string;
    totalArquivos: number;
    arquivosComAviso: number;
    avisos: number;
}

function adicionarAchado(
    achados: AchadoMensagem[],
    severidade: Severidade,
    regra: RegraMensagens,
    mensagem: string,
    linha: number | null = null
): void {
    achados.push({severidade, regra, mensagem, linha});
}

function localizarLinha(linhas: string[], trecho: string): number | null {
    const indice = linhas.findIndex(linha => linha.includes(trecho));
    return indice >= 0 ? indice + 1 : null;
}

function auditarArquivo(caminhoArquivo: string): AchadoMensagem[] {
    const texto = lerArquivo(caminhoArquivo);
    const linhas = obterLinhas(texto);
    const achados: AchadoMensagem[] = [];

    for (const descricao of extrairDescricoes(texto)) {
        if (/\s{2,}/.test(descricao)) {
            adicionarAchado(
                achados,
                "aviso",
                "descricao_espacamento",
                `Descrição com espaçamento suspeito: "${descricao}".`,
                localizarLinha(linhas, descricao)
            );
        }
        if (/\[\w/.test(descricao)) {
            adicionarAchado(
                achados,
                "aviso",
                "descricao_placeholder_legado",
                `Descrição ainda contém placeholder legado: "${descricao}".`,
                localizarLinha(linhas, descricao)
            );
        }
    }

    for (const assunto of extrairAssuntos(texto)) {
        if (assunto.endsWith("]")) {
            adicionarAchado(
                achados,
                "aviso",
                "assunto_fechamento_suspeito",
                `Assunto com fechamento suspeito: "${assunto}".`,
                localizarLinha(linhas, assunto)
            );
        }
        if (/\[\w/.test(assunto)) {
            adicionarAchado(
                achados,
                "aviso",
                "assunto_placeholder_legado",
                `Assunto ainda contém placeholder legado: "${assunto}".`,
                localizarLinha(linhas, assunto)
            );
        }
    }

    for (const mensagem of extrairMensagens(texto)) {
        if (/\[\w/.test(mensagem)) {
            adicionarAchado(
                achados,
                "aviso",
                "mensagem_placeholder_legado",
                `Mensagem ainda contém placeholder legado: "${mensagem}".`,
                localizarLinha(linhas, mensagem)
            );
        }
    }

    for (const toast of extrairToasts(texto)) {
        if (/\[\w/.test(toast)) {
            adicionarAchado(
                achados,
                "aviso",
                "toast_placeholder_legado",
                `Toast ainda contém placeholder legado: "${toast}".`,
                localizarLinha(linhas, toast)
            );
        }
    }

    return achados;
}

async function auditarMensagens(base: string, arquivosInformados?: string[]): Promise<{resumo: ResumoMensagens; relatorio: RelatorioArquivoMensagens[]}> {
    const arquivos = arquivosInformados ?? await listarArquivosCdu(base);
    const relatorio: RelatorioArquivoMensagens[] = arquivos.map(caminhoArquivo => ({
        arquivo: path.relative(base, caminhoArquivo).replaceAll("\\", "/"),
        achados: auditarArquivo(caminhoArquivo)
    }));

    const resumo: ResumoMensagens = {
        base,
        totalArquivos: relatorio.length,
        arquivosComAviso: relatorio.filter(item => item.achados.length > 0).length,
        avisos: relatorio.flatMap(item => item.achados).length
    };

    return {resumo, relatorio};
}

export {
    auditarMensagens
};
