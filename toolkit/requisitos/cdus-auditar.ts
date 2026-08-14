// Comando de auditoria consolidada dos casos de uso CDU.

import path from "node:path";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverLinha, imprimirJson} from "../biblioteca/saida.js";
import {obterOpcoesCdu} from "./cdus-opcoes.js";
import {auditarCdus, type AuditoriaCdus} from "./cdus-auditoria-motor.js";

interface AchadoParaExibicao {
    severidade?: "erro" | "aviso";
    regra: string;
    mensagem: string;
    linha?: number | null;
}

interface ArquivoComAchados {
    arquivo: string;
    achados: readonly AchadoParaExibicao[];
}

function imprimirAmostraAchados(
    titulo: string,
    relatorio: readonly ArquivoComAchados[],
    limite = 5
): boolean {
    const amostra = relatorio
        .flatMap(item => item.achados.map(achado => ({arquivo: item.arquivo, achado})))
        .toSorted((a, b) => Number(b.achado.severidade === "erro") - Number(a.achado.severidade === "erro"))
        .slice(0, limite);
    if (amostra.length === 0) {
        return false;
    }

    escreverLinha(`${titulo} (ate ${limite}):`);
    amostra.forEach(({arquivo, achado}, indice) => {
        const local = typeof achado.linha === "number" ? `:${achado.linha}` : "";
        escreverLinha(`${indice + 1}. ${arquivo}${local} [${achado.regra}] ${achado.mensagem}`);
    });
    return true;
}

function imprimirAmostraMensagensSemReferencia(
    secao: NonNullable<AuditoriaCdus["secoes"]["mensagensCodigo"]>,
    limite = 5
): boolean {
    const amostra = secao.relatorio
        .filter(item => item.referenciasExatas.length === 0)
        .slice(0, limite);
    if (amostra.length === 0) {
        return false;
    }

    escreverLinha(`Amostra de mensagens sem referência exata (ate ${limite}):`);
    amostra.forEach((item, indice) => {
        const ocorrencias = item.ocorrencias.slice(0, 2).join(", ");
        const sugestao = item.sugestoes[0]?.texto;
        escreverLinha(`${indice + 1}. [${item.tipo}] "${item.valor}" — ${item.quantidade} ocorrência(s) em ${ocorrencias}`);
        if (sugestao) {
            escreverLinha(`   Sugestão: "${sugestao}"`);
        }
    });
    return true;
}

function imprimirAuditoria(resultado: AuditoriaCdus): void {
    escreverLinha(`Auditoria consolidada dos CDUs em ${path.join(resultado.base, "specs")}`);
    escreverLinha(`Arquivos analisados: ${resultado.totalArquivos}`);
    escreverLinha(`Erros: ${resultado.resumo.erros} | Avisos: ${resultado.resumo.avisos}`);
    if (resultado.resumo.itensSemReferenciaExata > 0) {
        escreverLinha(`Mensagens sem referência exata no código: ${resultado.resumo.itensSemReferenciaExata}`);
    }
    for (const secao of resultado.secoesIgnoradas) {
        escreverLinha(`Seção ignorada: ${secao}`);
    }
    escreverLinha();

    if (resultado.secoes.estrutura) {
        const resumo = resultado.secoes.estrutura.resumo;
        escreverLinha(`[estrutura] ${resumo.erros} erros e ${resumo.avisos} avisos`);
    }
    if (resultado.secoes.estilo) {
        escreverLinha(`[estilo] ${resultado.secoes.estilo.resumo.avisos} avisos`);
    }
    if (resultado.secoes.vocabulario) {
        escreverLinha(`[vocabulário] ${resultado.secoes.vocabulario.resumo.avisos} avisos`);
    }
    if (resultado.secoes.mensagens) {
        escreverLinha(`[mensagens] ${resultado.secoes.mensagens.resumo.avisos} avisos`);
    }
    if (resultado.secoes.mensagensCodigo) {
        const resumo = resultado.secoes.mensagensCodigo.resumo;
        escreverLinha(`[mensagens-código] ${resumo.itensSemReferenciaExata} itens sem referência exata`);
    }

    const exibiuAmostra = [
        resultado.secoes.estrutura && imprimirAmostraAchados("Amostra de achados de estrutura", resultado.secoes.estrutura.relatorio),
        resultado.secoes.estilo && imprimirAmostraAchados("Amostra de achados de estilo", resultado.secoes.estilo.relatorio),
        resultado.secoes.vocabulario && imprimirAmostraAchados("Amostra de achados de vocabulário", resultado.secoes.vocabulario.relatorio),
        resultado.secoes.mensagens && imprimirAmostraAchados("Amostra de achados de mensagens", resultado.secoes.mensagens.relatorio),
        resultado.secoes.mensagensCodigo && imprimirAmostraMensagensSemReferencia(resultado.secoes.mensagensCodigo),
    ].some(Boolean);
    if (exibiuAmostra) {
        escreverLinha("");
        escreverLinha("Amostras limitadas; use --json para consultar todos os itens e ocorrências.");
    }
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const argumentosValidados = validarArgumentosEntradaDireta(import.meta.url, argumentos);
    const {emitirJson, base, secoes} = obterOpcoesCdu(argumentosValidados);
    const resultado = await auditarCdus(base, secoes);

    if (emitirJson) {
        imprimirJson(resultado);
        return;
    }

    imprimirAuditoria(resultado);
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}
