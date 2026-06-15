#!/usr/bin/env node
import path from "node:path";
import {pathToFileURL} from "node:url";
import pc from "picocolors";
import {
    analisarCruftFrontend,
    CAMINHO_BUDGET_PADRAO,
    CAMINHO_WAIVERS_PADRAO,
    carregarWaivers,
    DIRETORIO_SAIDA_PADRAO,
    gravarSnapshotAuditoria
} from "./cruft-lib.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";

function lerOpcao(argumentos, nome) {
    const indice = argumentos.indexOf(nome);
    if (indice === -1) {
        return null;
    }
    return argumentos[indice + 1] ?? null;
}

function criarViolacao(tipo, mensagem, detalhes = {}) {
    return {
        tipo,
        mensagem,
        ...detalhes,
    };
}

function indexarWaivers(waivers) {
    return new Map(waivers.map((waiver) => [waiver.arquivo, waiver]));
}

function resumirResultado(resultado) {
    return {
        status: resultado.status,
        geradoEm: resultado.geradoEm,
        resumo: resultado.resumo,
        budget: resultado.budget,
        waivers: resultado.waivers,
        violacoes: resultado.violacoes,
        avisos: resultado.avisos,
        hotspots: resultado.snapshot.hotspots,
    };
}

async function executarValidacaoFrontendCruft(opcoes = {}) {
    const snapshot = await analisarCruftFrontend({
        base: opcoes.base,
        caminhoBudget: opcoes.budget,
    });
    const waivers = await carregarWaivers(opcoes.waivers);
    const waiversPorArquivo = indexarWaivers(waivers.waivers);
    const violacoes = [];
    const avisos = [];

    const maximos = snapshot.budget.metricas?.maximosProducao ?? {};
    for (const [chave, maximo] of Object.entries(maximos)) {
        if (chave === "arquivosAcimaTargetPorCamada") {
            continue;
        }
        const valorAtual = snapshot.contagens.producao[chave];
        if (typeof valorAtual !== "number" || typeof maximo !== "number") {
            continue;
        }
        if (valorAtual > maximo) {
            violacoes.push(criarViolacao(
                "metrica_global",
                `Metrica ${chave} acima do budget: ${valorAtual} > ${maximo}`,
                {chave, valorAtual, maximo}
            ));
        }
    }

    const maximosCamada = snapshot.budget.metricas?.maximosProducao?.arquivosAcimaTargetPorCamada ?? {};
    for (const [camada, maximo] of Object.entries(maximosCamada)) {
        const valorAtual = snapshot.contagens.producao.arquivosAcimaTarget[camada] ?? 0;
        if (valorAtual > maximo) {
            violacoes.push(criarViolacao(
                "quantidade_acima_target",
                `Camada ${camada} excedeu o numero permitido de arquivos acima do target: ${valorAtual} > ${maximo}`,
                {camada, valorAtual, maximo}
            ));
        }
    }

    for (const arquivo of snapshot.arquivos.filter((item) => item.categoriaArquivo === "producao")) {
        const waiver = waiversPorArquivo.get(arquivo.arquivo);
        if (arquivo.linhas > arquivo.limites.target) {
            if (!waiver) {
                violacoes.push(criarViolacao(
                    "arquivo_sem_waiver",
                    `Arquivo acima do target sem waiver: ${arquivo.arquivo} (${arquivo.linhas} linhas)`,
                    {
                        arquivo: arquivo.arquivo,
                        camada: arquivo.camada,
                        linhas: arquivo.linhas,
                        target: arquivo.limites.target
                    }
                ));
            } else if (arquivo.linhas > waiver.maxLinhas) {
                violacoes.push(criarViolacao(
                    "arquivo_cresceu",
                    `Arquivo excedeu o waiver de tamanho: ${arquivo.arquivo} (${arquivo.linhas} > ${waiver.maxLinhas})`,
                    {
                        arquivo: arquivo.arquivo,
                        camada: arquivo.camada,
                        linhas: arquivo.linhas,
                        maxLinhas: waiver.maxLinhas
                    }
                ));
            }
        } else if (waiver) {
            avisos.push(criarViolacao(
                "waiver_obsoleto",
                `Waiver pode ser removido: ${arquivo.arquivo} ja voltou ao target (${arquivo.linhas} <= ${arquivo.limites.target})`,
                {arquivo: arquivo.arquivo, camada: arquivo.camada, linhas: arquivo.linhas}
            ));
        }
    }

    if (!opcoes.semGravar) {
        await gravarSnapshotAuditoria(snapshot, opcoes.saida);
    }

    return {
        status: violacoes.length === 0 ? "ok" : "falha",
        geradoEm: new Date().toISOString(),
        resumo: {
            scoreTotal: snapshot.resumo.scoreTotal,
            faixa: snapshot.resumo.faixa,
            violacoes: violacoes.length,
            avisos: avisos.length,
        },
        budget: path.relative(process.cwd(), opcoes.budget).replaceAll("\\", "/"),
        waivers: path.relative(process.cwd(), opcoes.waivers).replaceAll("\\", "/"),
        snapshot,
        violacoes,
        avisos,
    };
}

async function main() {
    const args = process.argv.slice(2);
    const jsonMode = args.includes("--json");
    const jsonResumidoMode = args.includes("--json-resumido");
    const helpMode = args.includes("--help") || args.includes("-h");

    if (helpMode) {
        exibirAjudaComando({
            comandoSgc: "frontend cruft validar",
            scriptDireto: "frontend/cruft-validar.js",
            descricao: "Valida budgets e waivers do cruft do frontend para impedir regressao estrutural.",
            opcoes: [
                "--json               Emite o resultado em JSON.",
                "--json-resumido      Emite somente status, resumo, violacoes e hotspots.",
                "--sem-gravar         Nao atualiza o snapshot latest.",
                "--base <diretorio>   Sobrescreve o diretorio base da validacao.",
                "--budget <arquivo>   Usa um arquivo de budget alternativo.",
                "--waivers <arquivo>  Usa um arquivo de waivers alternativo.",
                "--saida <diretorio>  Sobrescreve o diretorio de saida do snapshot."
            ],
            exemplos: [
                "node toolkit/sgc.js frontend cruft validar",
                "node toolkit/sgc.js frontend cruft validar --json",
                "node toolkit/sgc.js frontend cruft validar --base /tmp/sgc --budget /tmp/budget.json --waivers /tmp/waivers.json"
            ]
        });
        process.exit(0);
    }

    const budget = path.resolve(lerOpcao(args, "--budget") ?? CAMINHO_BUDGET_PADRAO);
    const waivers = path.resolve(lerOpcao(args, "--waivers") ?? CAMINHO_WAIVERS_PADRAO);
    const resultado = await executarValidacaoFrontendCruft({
        base: lerOpcao(args, "--base"),
        budget,
        waivers,
        saida: path.resolve(lerOpcao(args, "--saida") ?? DIRETORIO_SAIDA_PADRAO),
        semGravar: args.includes("--sem-gravar"),
    });

    if (jsonMode) {
        imprimirJson(resultado);
        process.exit(resultado.status === "ok" ? 0 : 1);
    }

    if (jsonResumidoMode) {
        imprimirJson(resumirResultado(resultado));
        process.exit(resultado.status === "ok" ? 0 : 1);
    }

    imprimirCabecalho("VALIDACAO DE CRUFT DO FRONTEND");
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
        escreverLinha(pc.green("Nenhuma violacao de budget encontrada."));
    }

    if (resultado.avisos.length > 0) {
        escreverLinha("");
        escreverLinha(pc.bold("Avisos:"));
        resultado.avisos.forEach((aviso, indice) => {
            escreverLinha(`${indice + 1}. ${aviso.mensagem}`);
        });
    }

    escreverLinha("");
    escreverLinha(`Budget: ${resultado.budget}`);
    escreverLinha(`Waivers: ${resultado.waivers}`);
    escreverLinha(`Snapshot latest: ${path.relative(process.cwd(), path.resolve(lerOpcao(args, "--saida") ?? DIRETORIO_SAIDA_PADRAO)).replaceAll("\\", "/")}`);

    process.exit(resultado.status === "ok" ? 0 : 1);
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
    main().catch((erro) => {
        escreverLinha(pc.red(`Erro na validacao de cruft: ${erro.message}`));
        process.exit(1);
    });
}

export {
    executarValidacaoFrontendCruft
};
