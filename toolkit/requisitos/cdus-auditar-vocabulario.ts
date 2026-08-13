// Auditoria de vocabulário controlado dos casos de uso CDU.

import path from "node:path";
import {ehEntradaPrincipal} from "../biblioteca/execucao.js";
import {escreverLinha, imprimirJson} from "../biblioteca/saida.js";
import {lerArquivo, listarArquivosCdu, obterOpcoesCdu} from "./cdus-lib.js";
import {
    carregarSituacoesCanonicas,
    obterVocabularioCanonico,
    sugerirCanonico
} from "./cdus-vocabulario-lib.js";

type RegraVocabulario = "perfil_fora_vocabulario" | "tipo_processo_variacao" | "situacao_variacao";

interface AchadoVocabulario {
    severidade: "aviso";
    regra: RegraVocabulario;
    mensagem: string;
    linha: number | null;
}

interface RelatorioArquivoVocabulario {
    arquivo: string;
    achados: AchadoVocabulario[];
}

function adicionarAchado(
    achados: AchadoVocabulario[],
    severidade: "aviso",
    regra: RegraVocabulario,
    mensagem: string,
    linha: number | null = null
): void {
    achados.push({severidade, regra, mensagem, linha});
}

function auditarPerfis(texto: string, achados: AchadoVocabulario[], perfisCanonicos: Set<string>): void {
    const linhas = texto.split(/\r?\n/);
    const indiceAtores = linhas.findIndex(linha => /^##\s+Atores\s*$/.test(linha));
    const indicePre = linhas.findIndex(linha => /^##\s+Pré-condições\s*$/.test(linha));
    if (indiceAtores < 0 || indicePre < 0 || indicePre <= indiceAtores) {
        return;
    }

    linhas.slice(indiceAtores + 1, indicePre).forEach((linha, offset) => {
        if (!/^\s*-\s+/.test(linha)) {
            return;
        }

        const valor = linha.replace(/^\s*-\s+/, "").trim();
        if (perfisCanonicos.has(valor)) {
            return;
        }

        const sugestao = sugerirCanonico(valor, perfisCanonicos);
        const complemento = sugestao ? ` Sugestão: \`${sugestao}\`.` : "";
        adicionarAchado(
            achados,
            "aviso",
            "perfil_fora_vocabulario",
            `Perfil fora do vocabulário canônico: \`${valor}\`.${complemento}`,
            indiceAtores + offset + 2
        );
    });
}

function auditarSituacoesETipos(
    texto: string,
    achados: AchadoVocabulario[],
    situacoesCanonicas: Set<string>,
    tiposProcessoCanonicos: Set<string>
): void {
    const linhas = texto.split(/\r?\n/);
    linhas.forEach((linha, indice) => {
        const linhaNormalizada = linha.toLowerCase();
        const contextoSituacao = /situa[cç][aã]o|situa[cç][oõ]es|resultado/.test(linhaNormalizada);
        const contextoTipo = /tipo/.test(linhaNormalizada);

        for (const match of linha.matchAll(/'([^'\n]+)'/g)) {
            const valor = match[1].trim();
            const ehTipo = tiposProcessoCanonicos.has(valor);
            const ehSituacao = situacoesCanonicas.has(valor);
            if (ehTipo || ehSituacao) {
                continue;
            }

            const sugestaoSituacao = contextoSituacao
                ? sugerirCanonico(valor, situacoesCanonicas)
                : null;
            const sugestaoTipo = contextoTipo
                ? sugerirCanonico(valor, tiposProcessoCanonicos)
                : null;
            const sugestao = sugestaoSituacao ?? sugestaoTipo;
            if (!sugestao) {
                continue;
            }

            const regra = tiposProcessoCanonicos.has(sugestao)
                ? "tipo_processo_variacao"
                : "situacao_variacao";
            const categoria = tiposProcessoCanonicos.has(sugestao)
                ? "tipo de processo"
                : "situação";
            adicionarAchado(
                achados,
                "aviso",
                regra,
                `Possível variação de ${categoria}: '${valor}'. Sugestão: '${sugestao}'.`,
                indice + 1
            );
        }
    });
}

async function auditarVocabulario(base: string, arquivosInformados?: string[]): Promise<{resumo: {
    base: string;
    totalArquivos: number;
    arquivosComAviso: number;
    avisos: number;
}; relatorio: RelatorioArquivoVocabulario[]}> {
    const situacoesCanonicas = carregarSituacoesCanonicas(base);
    const {perfis: perfisCanonicos, tiposProcesso: tiposProcessoCanonicos} = obterVocabularioCanonico(base);

    const arquivos = arquivosInformados ?? await listarArquivosCdu(base);
    const relatorio: RelatorioArquivoVocabulario[] = arquivos.map(caminhoArquivo => {
        const texto = lerArquivo(caminhoArquivo);
        const achados: AchadoVocabulario[] = [];
        auditarPerfis(texto, achados, perfisCanonicos);
        auditarSituacoesETipos(texto, achados, situacoesCanonicas, tiposProcessoCanonicos);
        return {
            arquivo: path.relative(base, caminhoArquivo).replaceAll("\\", "/"),
            achados
        };
    });

    const resumo = {
        base,
        totalArquivos: relatorio.length,
        arquivosComAviso: relatorio.filter(item => item.achados.length > 0).length,
        avisos: relatorio.flatMap(item => item.achados).length
    };

    return {resumo, relatorio};
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const {emitirJson, base} = obterOpcoesCdu(argumentos);
    const resultado = await auditarVocabulario(base);

    if (emitirJson) {
        imprimirJson(resultado);
        return;
    }

    escreverLinha(`Auditoria de vocabulário dos CDUs em ${path.join(base, "specs")}`);
    escreverLinha(`Arquivos analisados: ${resultado.resumo.totalArquivos}`);
    escreverLinha(`Arquivos com aviso: ${resultado.resumo.arquivosComAviso}`);
    escreverLinha(`Avisos: ${resultado.resumo.avisos}`);
    escreverLinha();

    for (const item of resultado.relatorio.filter(entrada => entrada.achados.length > 0)) {
        escreverLinha(item.arquivo);
        for (const achado of item.achados) {
            const sufixoLinha = achado.linha ? ` (linha ${achado.linha})` : "";
            escreverLinha(`- [${achado.severidade}] ${achado.regra}${sufixoLinha}: ${achado.mensagem}`);
        }
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}

export {
    auditarVocabulario
};
