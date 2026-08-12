#!/usr/bin/env node
import path from "node:path";
import pc from "picocolors";
import {
    analisarResiduosFrontend,
    CAMINHO_ORCAMENTO_PADRAO,
    CAMINHO_EXCECOES_PADRAO,
    carregarExcecoes,
    DIRETORIO_SAIDA_PADRAO,
    gravarFotografiaAuditoria
} from "./residuos-lib.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";

function criarViolacao(tipo, mensagem, detalhes = {}) {
    return {
        tipo,
        mensagem,
        ...detalhes,
    };
}

function indexarExcecoes(excecoes) {
    return new Map(excecoes.map((excecao) => [excecao.arquivo, excecao]));
}

function resumirResultado(resultado) {
    return {
        status: resultado.status,
        geradoEm: resultado.geradoEm,
        resumo: resultado.resumo,
        orcamento: resultado.orcamento,
        excecoes: resultado.excecoes,
        violacoes: resultado.violacoes,
        avisos: resultado.avisos,
        hotspots: resultado.fotografia.hotspots,
    };
}

async function executarValidacaoFrontendResiduos(opcoes = {}) {
    const caminhoOrcamento = path.resolve(opcoes.orcamento ?? CAMINHO_ORCAMENTO_PADRAO);
    const caminhoExcecoes = path.resolve(opcoes.excecoes ?? CAMINHO_EXCECOES_PADRAO);
    const fotografia = await analisarResiduosFrontend({
        base: opcoes.base,
        caminhoOrcamento,
    });
    const excecoes = await carregarExcecoes(caminhoExcecoes);
    const excecoesPorArquivo = indexarExcecoes(excecoes.excecoes);
    const violacoes = [];
    const avisos = [];

    const maximos = fotografia.orcamento.metricas?.maximosProducao ?? {};
    for (const [chave, maximo] of Object.entries(maximos)) {
        if (chave === "arquivosAcimaMetaPorCamada") {
            continue;
        }
        const valorAtual = fotografia.contagens.producao[chave];
        if (typeof valorAtual !== "number" || typeof maximo !== "number") {
            continue;
        }
        if (valorAtual > maximo) {
            violacoes.push(criarViolacao(
                "metrica_global",
                `Metrica ${chave} acima do orcamento: ${valorAtual} > ${maximo}`,
                {chave, valorAtual, maximo}
            ));
        }
    }

    const maximosCamada = fotografia.orcamento.metricas?.maximosProducao?.arquivosAcimaMetaPorCamada ?? {};
    for (const [camada, maximo] of Object.entries(maximosCamada)) {
        const valorAtual = fotografia.contagens.producao.arquivosAcimaMeta[camada] ?? 0;
        if (valorAtual > maximo) {
            violacoes.push(criarViolacao(
                "quantidade_acima_meta",
                `Camada ${camada} excedeu o numero permitido de arquivos acima da meta: ${valorAtual} > ${maximo}`,
                {camada, valorAtual, maximo}
            ));
        }
    }

    for (const arquivo of fotografia.arquivos.filter((item) => item.categoriaArquivo === "producao")) {
        const excecao = excecoesPorArquivo.get(arquivo.arquivo);
        if (arquivo.linhas > arquivo.limites.meta) {
            if (!excecao) {
                violacoes.push(criarViolacao(
                    "arquivo_sem_excecao",
                    `Arquivo acima da meta sem excecao: ${arquivo.arquivo} (${arquivo.linhas} linhas)`,
                    {
                        arquivo: arquivo.arquivo,
                        camada: arquivo.camada,
                        linhas: arquivo.linhas,
                        meta: arquivo.limites.meta
                    }
                ));
            } else if (arquivo.linhas > excecao.maxLinhas) {
                violacoes.push(criarViolacao(
                    "arquivo_cresceu",
                    `Arquivo excedeu a excecao de tamanho: ${arquivo.arquivo} (${arquivo.linhas} > ${excecao.maxLinhas})`,
                    {
                        arquivo: arquivo.arquivo,
                        camada: arquivo.camada,
                        linhas: arquivo.linhas,
                        maxLinhas: excecao.maxLinhas
                    }
                ));
            }
        } else if (excecao) {
            avisos.push(criarViolacao(
                "excecao_obsoleta",
                `Excecao pode ser removida: ${arquivo.arquivo} ja voltou a meta (${arquivo.linhas} <= ${arquivo.limites.meta})`,
                {arquivo: arquivo.arquivo, camada: arquivo.camada, linhas: arquivo.linhas}
            ));
        }
    }

    if (!opcoes.semGravar) {
        await gravarFotografiaAuditoria(fotografia, opcoes.saida);
    }

    return {
        status: violacoes.length === 0 ? "ok" : "falha",
        geradoEm: new Date().toISOString(),
        resumo: {
            scoreTotal: fotografia.resumo.scoreTotal,
            faixa: fotografia.resumo.faixa,
            violacoes: violacoes.length,
            avisos: avisos.length,
        },
        orcamento: path.relative(process.cwd(), caminhoOrcamento).replaceAll("\\", "/"),
        excecoes: path.relative(process.cwd(), caminhoExcecoes).replaceAll("\\", "/"),
        fotografia,
        violacoes,
        avisos,
    };
}

async function principal(argumentos = process.argv.slice(2)) {
    const emitirJson = argumentos.includes("--json");
    const emitirJsonResumido = argumentos.includes("--json-resumido");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoSgc: "frontend residuos validar",
            scriptDireto: "frontend/residuos-validar.js",
            descricao: "Valida orcamentos e excecoes dos residuos do frontend para impedir regressao estrutural.",
            opcoes: [
                "--json               Emite o resultado em JSON.",
                "--json-resumido      Emite somente status, resumo, violacoes e hotspots.",
                "--sem-gravar         Nao atualiza a fotografia mais recente.",
                "--base <diretorio>   Sobrescreve o diretorio base da validacao.",
                "--orcamento <arquivo> Usa um arquivo de orcamento alternativo.",
                "--excecoes <arquivo>  Usa um arquivo de excecoes alternativo.",
                "--saida <diretorio>  Sobrescreve o diretorio de saida da fotografia."
            ],
            exemplos: [
                "node toolkit/sgc.js frontend residuos validar",
                "node toolkit/sgc.js frontend residuos validar --json",
                "node toolkit/sgc.js frontend residuos validar --base /tmp/sgc --orcamento /tmp/orcamento.json --excecoes /tmp/excecoes.json"
            ]
        });
        return;
    }

    const orcamento = path.resolve(lerOpcao(argumentos, "--orcamento", CAMINHO_ORCAMENTO_PADRAO));
    const excecoes = path.resolve(lerOpcao(argumentos, "--excecoes", CAMINHO_EXCECOES_PADRAO));
    const resultado = await executarValidacaoFrontendResiduos({
        base: lerOpcao(argumentos, "--base", undefined),
        orcamento,
        excecoes,
        saida: path.resolve(lerOpcao(argumentos, "--saida", DIRETORIO_SAIDA_PADRAO)),
        semGravar: argumentos.includes("--sem-gravar"),
    });

    if (emitirJson) {
        imprimirJson(resultado);
        if (resultado.status !== "ok") process.exitCode = 1;
        return;
    }

    if (emitirJsonResumido) {
        imprimirJson(resumirResultado(resultado));
        if (resultado.status !== "ok") process.exitCode = 1;
        return;
    }

    imprimirCabecalho("VALIDACAO DE RESIDUOS DO FRONTEND");
    escreverLinha(`Status: ${resultado.status === "ok" ? pc.green("ok") : pc.red("falha")}`);
    escreverLinha(`Score total: ${resultado.resumo.scoreTotal} (${resultado.resumo.faixa})`);
    escreverLinha(`Violacoes: ${resultado.resumo.violacoes}`);
    escreverLinha(`Avisos: ${resultado.resumo.avisos}`);
    escreverLinha("");

    if (resultado.violacoes.length > 0) {
        escreverLinha(pc.bold("Violacoes:"));
        resultado.violacoes.forEach((violacao, indice) => {
            escreverLinha(`${indice + 1}. ${violacao.mensagem}`);
        });
    } else {
        escreverLinha(pc.green("Nenhuma violacao de orcamento encontrada."));
    }

    if (resultado.avisos.length > 0) {
        escreverLinha("");
        escreverLinha(pc.bold("Avisos:"));
        resultado.avisos.forEach((aviso, indice) => {
            escreverLinha(`${indice + 1}. ${aviso.mensagem}`);
        });
    }

    escreverLinha("");
    escreverLinha(`Orcamento: ${resultado.orcamento}`);
    escreverLinha(`Excecoes: ${resultado.excecoes}`);
    escreverLinha(`Fotografia mais recente: ${path.relative(process.cwd(), path.resolve(lerOpcao(argumentos, "--saida", DIRETORIO_SAIDA_PADRAO))).replaceAll("\\", "/")}`);

    if (resultado.status !== "ok") process.exitCode = 1;
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro na validacao de residuos: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    executarValidacaoFrontendResiduos,
    principal,
};
