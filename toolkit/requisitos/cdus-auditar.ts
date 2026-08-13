// Auditoria estrutural dos casos de uso CDU.

import path from "node:path";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";
import {analisarArquivo, lerArquivo, listarArquivosCdu, obterOpcoesCdu, validarLinksMarkdown} from "./cdus-lib.js";
import {auditarMensagensCodigo} from "./cdus-auditar-mensagens-codigo.js";
import {auditarMensagens} from "./cdus-auditar-mensagens.js";
import {auditarEstilo} from "./cdus-auditar-estilo.js";
import {auditarVocabulario} from "./cdus-auditar-vocabulario.js";
import {possuiFontesMensagensCanonicas} from "./cdus-mensagens-codigo-lib.js";

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

type SecaoAuditoria = "estrutura" | "estilo" | "vocabulario" | "mensagens" | "mensagens-codigo";

interface AuditoriaCdus {
    versao: 1;
    base: string;
    totalArquivos: number;
    secoesIgnoradas: string[];
    secoes: {
        estrutura?: Awaited<ReturnType<typeof auditarEstrutura>>;
        estilo?: Awaited<ReturnType<typeof auditarEstilo>>;
        vocabulario?: Awaited<ReturnType<typeof auditarVocabulario>>;
        mensagens?: Awaited<ReturnType<typeof auditarMensagens>>;
        mensagensCodigo?: Awaited<ReturnType<typeof auditarMensagensCodigo>>;
    };
    resumo: {
        erros: number;
        avisos: number;
        itensSemReferenciaExata: number;
    };
}

const SECOES_AUDITORIA: readonly SecaoAuditoria[] = [
    "estrutura",
    "estilo",
    "vocabulario",
    "mensagens",
    "mensagens-codigo"
];

function selecionarSecoesAuditoria(argumentos: string[] | undefined): Set<SecaoAuditoria> {
    if (!argumentos || argumentos.length === 0 || argumentos.includes("todos")) {
        return new Set(SECOES_AUDITORIA);
    }

    const desconhecidas = argumentos.filter(secao => !SECOES_AUDITORIA.includes(secao as SecaoAuditoria));
    if (desconhecidas.length > 0) {
        throw new Error(`Seção de auditoria CDU desconhecida: ${desconhecidas.join(", ")}.`);
    }

    return new Set(argumentos as SecaoAuditoria[]);
}

async function auditarCdus(base: string, secoesInformadas?: string[]): Promise<AuditoriaCdus> {
    const arquivos = await listarArquivosCdu(base);
    const secoesSelecionadas = selecionarSecoesAuditoria(secoesInformadas);
    const secoes: AuditoriaCdus["secoes"] = {};
    const secoesIgnoradas: string[] = [];

    if (secoesSelecionadas.has("estrutura")) {
        secoes.estrutura = await auditarEstrutura(base, arquivos);
    }
    if (secoesSelecionadas.has("estilo")) {
        secoes.estilo = await auditarEstilo(base, arquivos);
    }
    if (secoesSelecionadas.has("vocabulario")) {
        secoes.vocabulario = await auditarVocabulario(base, arquivos);
    }
    if (secoesSelecionadas.has("mensagens")) {
        secoes.mensagens = await auditarMensagens(base, arquivos);
    }
    if (secoesSelecionadas.has("mensagens-codigo")) {
        if (!possuiFontesMensagensCanonicas(base)) {
            const selecaoExplícita = secoesInformadas?.some(secao => secao !== "todos") ?? false;
            if (selecaoExplícita) {
                throw new Error("A comparação com mensagens do código exige as fontes canônicas configuradas do projeto.");
            }
            secoesIgnoradas.push("mensagens-codigo (fontes canônicas ausentes)");
        } else {
            secoes.mensagensCodigo = await auditarMensagensCodigo(base, arquivos);
        }
    }

    const erros = secoes.estrutura?.resumo.erros ?? 0;
    const avisos = (secoes.estrutura?.resumo.avisos ?? 0)
        + (secoes.estilo?.resumo.avisos ?? 0)
        + (secoes.vocabulario?.resumo.avisos ?? 0)
        + (secoes.mensagens?.resumo.avisos ?? 0);

    return {
        versao: 1,
        base,
        totalArquivos: arquivos.length,
        secoesIgnoradas,
        secoes,
        resumo: {
            erros,
            avisos,
            itensSemReferenciaExata: secoes.mensagensCodigo?.resumo.itensSemReferenciaExata ?? 0
        }
    };
}

function imprimirAuditoriaCdus(resultado: AuditoriaCdus): void {
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

async function auditarEstrutura(base: string, arquivosInformados?: string[]): Promise<RelatorioAuditoriaCdu> {
    const arquivos = arquivosInformados ?? await listarArquivosCdu(base);
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

    return {resumo, relatorio};
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const {emitirJson, base, secoes} = obterOpcoesCdu(argumentos);
    const resultado = await auditarCdus(base, secoes);

    if (emitirJson) {
        imprimirJson(resultado);
        return;
    }

    imprimirAuditoriaCdus(resultado);
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}

export {
    auditarCdus,
    auditarEstrutura,
    principal
};
