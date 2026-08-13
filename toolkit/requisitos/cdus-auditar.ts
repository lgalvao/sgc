#!/usr/bin/env node
// Auditoria estrutural dos casos de uso CDU.

import path from "node:path";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";
import {analisarArquivo, lerArquivo, listarArquivosCdu, obterOpcoesCdu, validarLinksMarkdown} from "./cdus-lib.js";

type AnaliseCdu = ReturnType<typeof analisarArquivo>;
type SeveridadeAchado = "erro" | "aviso";

interface AchadoCdu {
    severidade: SeveridadeAchado;
    regra: string;
    mensagem: string;
}

interface RelatorioArquivoCdu {
    arquivo: string;
    achados: AchadoCdu[];
}

interface RelatorioAuditoriaCdu {
    resumo: {
        base: string;
        totalArquivos: number;
        arquivosComErro: number;
        arquivosComAviso: number;
        erros: number;
        avisos: number;
    };
    relatorio: RelatorioArquivoCdu[];
}

function adicionarAchado(
    achados: AchadoCdu[],
    severidade: SeveridadeAchado,
    regra: string,
    mensagem: string
): void {
    achados.push({severidade, regra, mensagem});
}

function auditarAnalise(analise: AnaliseCdu): AchadoCdu[] {
    const achados: AchadoCdu[] = [];
    const numeroArquivo = analise.nomeArquivo.match(/^cdu-(\d{2})\.md$/)?.[1] ?? null;

    if (!analise.temTituloCanonico) {
        adicionarAchado(achados, "erro", "titulo_canonico", "Título fora do padrão `# CDU-XX - ...`.");
    } else if (analise.tituloNumero !== numeroArquivo) {
        adicionarAchado(achados, "erro", "titulo_numero", "Número do título diverge do número do arquivo.");
    }

    if (analise.quantidadeSecoesAtoresCanonicas !== 1) {
        adicionarAchado(achados, "erro", "atores_canonicos", "Deve existir exatamente uma seção `## Atores`.");
    }

    if (!analise.temPre) {
        adicionarAchado(achados, "erro", "pre_condicoes", "Seção obrigatória `## Pré-condições` ausente.");
    }

    if (!analise.temFluxo) {
        adicionarAchado(achados, "erro", "fluxo_principal", "Seção obrigatória `## Fluxo principal` ausente.");
    }

    const {ator, pre, fluxo} = analise.indices;
    if (ator >= 0 && pre >= 0 && fluxo >= 0 && !(ator < pre && pre < fluxo)) {
        adicionarAchado(achados, "erro", "ordem_canonica", "A ordem canônica deve ser Atores, Pré-condições e Fluxo principal.");
    }

    if (analise.temAtores && analise.quantidadeAtores === 0) {
        adicionarAchado(achados, "erro", "atores_vazios", "A seção `Atores` deve conter ao menos um item.");
    }

    if (analise.temPre && analise.quantidadePreCondicoes === 0) {
        adicionarAchado(achados, "erro", "pre_condicoes_vazias", "A seção `Pré-condições` deve conter ao menos um item.");
    }

    if (analise.passos.length === 0) {
        adicionarAchado(achados, "erro", "fluxo_sem_passos", "O fluxo principal deve conter ao menos um passo numerado.");
    }

    if (analise.repeticoes.length > 0) {
        adicionarAchado(achados, "erro", "numeracao_repetida", `Numeração repetida nos passos: ${analise.repeticoes.join(", ")}.`);
    }

    if (analise.regressoes.length > 0) {
        adicionarAchado(achados, "erro", "numeracao_regressiva", `Numeração regressiva detectada: ${analise.regressoes.join(", ")}.`);
    }

    const linksInvalidos = validarLinksMarkdown(analise);
    if (linksInvalidos.length > 0) {
        adicionarAchado(achados, "erro", "links_invalidos", `Links Markdown inválidos: ${linksInvalidos.join(", ")}.`);
    }

    if (analise.contagens.palavras > 700) {
        adicionarAchado(achados, "aviso", "documento_longo", `Documento extenso (${analise.contagens.palavras} palavras).`);
    }

    const profundidadeListas = analise.linhas.filter(linha => /^\s{4,}[-*]\s+/.test(linha)).length;
    if (profundidadeListas > 15) {
        adicionarAchado(achados, "aviso", "listas_profundas", `Quantidade alta de listas profundas (${profundidadeListas}).`);
    }

    return achados;
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const {emitirJson, base} = obterOpcoesCdu(argumentos);

    const arquivos = await listarArquivosCdu(base);
    const relatorio: RelatorioArquivoCdu[] = arquivos.map(caminhoArquivo => {
        const texto = lerArquivo(caminhoArquivo);
        const analise = analisarArquivo(caminhoArquivo, texto);
        return {
            arquivo: path.relative(base, caminhoArquivo).replaceAll("\\", "/"),
            achados: auditarAnalise(analise)
        };
    });

    const resumo: RelatorioAuditoriaCdu["resumo"] = {
        base,
        totalArquivos: relatorio.length,
        arquivosComErro: relatorio.filter(item => item.achados.some(achado => achado.severidade === "erro")).length,
        arquivosComAviso: relatorio.filter(item => item.achados.some(achado => achado.severidade === "aviso")).length,
        erros: relatorio.flatMap(item => item.achados).filter(achado => achado.severidade === "erro").length,
        avisos: relatorio.flatMap(item => item.achados).filter(achado => achado.severidade === "aviso").length
    };

    if (emitirJson) {
        imprimirJson({resumo, relatorio});
        return;
    }

    escreverLinha(`Auditoria read-only dos CDUs em ${path.join(base, "specs")}`);
    escreverLinha(`Arquivos analisados: ${resumo.totalArquivos}`);
    escreverLinha(`Arquivos com erro: ${resumo.arquivosComErro}`);
    escreverLinha(`Arquivos com aviso: ${resumo.arquivosComAviso}`);
    escreverLinha();

    for (const item of relatorio.filter(entrada => entrada.achados.length > 0)) {
        escreverLinha(item.arquivo);
        for (const achado of item.achados) {
            escreverLinha(`- [${achado.severidade}] ${achado.regra}: ${achado.mensagem}`);
        }
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}

export {
    principal
};
