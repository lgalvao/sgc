#!/usr/bin/env node
import path from "node:path";
import {pathToFileURL} from "node:url";
import pc from "picocolors";
import {
    analisarCruftFrontend,
    CAMINHO_BUDGET_PADRAO,
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

async function executarAuditoriaFrontendCruft(opcoes = {}) {
    const snapshot = await analisarCruftFrontend({
        base: opcoes.base,
        caminhoBudget: opcoes.budget,
    });

    if (!opcoes.semGravar) {
        await gravarSnapshotAuditoria(snapshot, opcoes.saida);
    }

    return snapshot;
}

async function main() {
    const args = process.argv.slice(2);
    const jsonMode = args.includes("--json");
    const helpMode = args.includes("--help") || args.includes("-h");

    if (helpMode) {
        exibirAjudaComando({
            comandoSgc: "frontend cruft auditar",
            scriptDireto: "frontend/cruft-auditar.js",
            descricao: "Audita sinais de cruft estrutural e defensividade acidental no frontend.",
            opcoes: [
                "--json               Emite o snapshot em JSON.",
                "--sem-gravar         Nao grava snapshot/markdown em disco.",
                "--base <diretorio>   Sobrescreve o diretorio base da auditoria.",
                "--budget <arquivo>   Usa um arquivo de budget alternativo.",
                "--saida <diretorio>  Sobrescreve o diretorio de saida do snapshot."
            ],
            exemplos: [
                "node toolkit/sgc.js frontend cruft auditar",
                "node toolkit/sgc.js frontend cruft auditar --json",
                "node toolkit/sgc.js frontend cruft auditar --sem-gravar --base /tmp/sgc"
            ]
        });
        process.exit(0);
    }

    const snapshot = await executarAuditoriaFrontendCruft({
        base: lerOpcao(args, "--base"),
        budget: lerOpcao(args, "--budget") ?? CAMINHO_BUDGET_PADRAO,
        saida: lerOpcao(args, "--saida") ?? DIRETORIO_SAIDA_PADRAO,
        semGravar: args.includes("--sem-gravar"),
    });

    if (jsonMode) {
        imprimirJson(snapshot);
        return;
    }

    imprimirCabecalho("AUDITORIA DE CRUFT DO FRONTEND");
    escreverLinha(`Score total: ${pc.bold(String(snapshot.resumo.scoreTotal))} (${snapshot.resumo.faixa})`);
    escreverLinha(`Arquivos de producao: ${snapshot.resumo.arquivosProducao}`);
    escreverLinha(`Arquivos de teste/story: ${snapshot.resumo.arquivosTeste}`);
    escreverLinha("");
    escreverLinha("Sinais em producao:");
    escreverLinha(`- any explicito: ${snapshot.contagens.producao.anyExplicito}`);
    escreverLinha(`- checks de null: ${snapshot.contagens.producao.checksNull}`);
    escreverLinha(`- fallbacks defensivos: ${snapshot.contagens.producao.fallbacksDefensivos}`);
    escreverLinha(`- blocos catch: ${snapshot.contagens.producao.catchBlocks}`);
    escreverLinha(`- casts duplos: ${snapshot.contagens.producao.castsDuplos}`);
    escreverLinha(`- storage direto: ${snapshot.contagens.producao.storageDireto}`);
    escreverLinha(`- exports suspeitos: ${snapshot.contagens.producao.exportsSuspeitos}`);
    escreverLinha("");
    escreverLinha(pc.bold("Top 5 hotspots:"));
    snapshot.hotspots.slice(0, 5).forEach((hotspot, indice) => {
        escreverLinha(`${indice + 1}. ${hotspot.arquivo} [${hotspot.camada}]`);
        escreverLinha(`   Linhas: ${hotspot.linhas} | Score: ${hotspot.score}`);
    });

    if (!args.includes("--sem-gravar")) {
        const diretorio = lerOpcao(args, "--saida") ?? DIRETORIO_SAIDA_PADRAO;
        escreverLinha("");
        escreverLinha(`${pc.green("✓")} Snapshot salvo em ${path.relative(process.cwd(), diretorio).replaceAll("\\", "/")}`);
    }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
    main().catch((erro) => {
        escreverLinha(pc.red(`Erro na auditoria de cruft: ${erro.message}`));
        process.exit(1);
    });
}

export {
    executarAuditoriaFrontendCruft
};
