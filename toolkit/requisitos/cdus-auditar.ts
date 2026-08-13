// Comando de auditoria consolidada dos casos de uso CDU.

import path from "node:path";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverLinha, imprimirJson} from "../biblioteca/saida.js";
import {obterOpcoesCdu} from "./cdus-opcoes.js";
import {auditarCdus, type AuditoriaCdus} from "./cdus-auditoria-motor.js";

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
