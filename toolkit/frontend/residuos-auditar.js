#!/usr/bin/env node
import path from "node:path";
import {pathToFileURL} from "node:url";
import pc from "picocolors";
import {
    analisarResiduosFrontend,
    CAMINHO_ORCAMENTO_PADRAO,
    DIRETORIO_SAIDA_PADRAO,
    gravarFotografiaAuditoria
} from "./residuos-lib.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";

function lerOpcao(argumentos, nome) {
    const indice = argumentos.indexOf(nome);
    if (indice === -1) {
        return null;
    }
    return argumentos[indice + 1] ?? null;
}

async function executarAuditoriaFrontendResiduos(opcoes = {}) {
    const fotografia = await analisarResiduosFrontend({
        base: opcoes.base,
        caminhoOrcamento: opcoes.orcamento,
    });

    if (!opcoes.semGravar) {
        await gravarFotografiaAuditoria(fotografia, opcoes.saida);
    }

    return fotografia;
}

async function main() {
    const args = process.argv.slice(2);
    const jsonMode = args.includes("--json");
    const helpMode = args.includes("--help") || args.includes("-h");

    if (helpMode) {
        exibirAjudaComando({
            comandoSgc: "frontend residuos auditar",
            scriptDireto: "frontend/residuos-auditar.js",
            descricao: "Audita sinais de residuos estruturais e defensividade acidental no frontend.",
            opcoes: [
                "--json               Emite a fotografia em JSON.",
                "--sem-gravar         Nao grava fotografia/resumo em disco.",
                "--base <diretorio>   Sobrescreve o diretorio base da auditoria.",
                "--orcamento <arquivo> Usa um arquivo de orcamento alternativo.",
                "--saida <diretorio>  Sobrescreve o diretorio de saida da fotografia."
            ],
            exemplos: [
                "node toolkit/sgc.js frontend residuos auditar",
                "node toolkit/sgc.js frontend residuos auditar --json",
                "node toolkit/sgc.js frontend residuos auditar --sem-gravar --base /tmp/sgc"
            ]
        });
        process.exit(0);
    }

    const fotografia = await executarAuditoriaFrontendResiduos({
        base: lerOpcao(args, "--base"),
        orcamento: lerOpcao(args, "--orcamento") ?? CAMINHO_ORCAMENTO_PADRAO,
        saida: lerOpcao(args, "--saida") ?? DIRETORIO_SAIDA_PADRAO,
        semGravar: args.includes("--sem-gravar"),
    });

    if (jsonMode) {
        imprimirJson(fotografia);
        return;
    }

    imprimirCabecalho("AUDITORIA DE RESIDUOS DO FRONTEND");
    escreverLinha(`Score total: ${pc.bold(String(fotografia.resumo.scoreTotal))} (${fotografia.resumo.faixa})`);
    escreverLinha(`Arquivos de producao: ${fotografia.resumo.arquivosProducao}`);
    escreverLinha(`Arquivos de teste/story: ${fotografia.resumo.arquivosTeste}`);
    escreverLinha("");
    escreverLinha("Sinais em producao:");
    escreverLinha(`- any explicito: ${fotografia.contagens.producao.anyExplicito}`);
    escreverLinha(`- checks de null: ${fotografia.contagens.producao.checksNull}`);
    escreverLinha(`- fallbacks defensivos: ${fotografia.contagens.producao.fallbacksDefensivos}`);
    escreverLinha(`- blocos catch: ${fotografia.contagens.producao.catchBlocks}`);
    escreverLinha(`- casts duplos: ${fotografia.contagens.producao.castsDuplos}`);
    escreverLinha(`- storage direto: ${fotografia.contagens.producao.storageDireto}`);
    escreverLinha(`- exportacoes suspeitas: ${fotografia.contagens.producao.exportacoesSuspeitas}`);
    escreverLinha("");
    escreverLinha(pc.bold("Top 5 hotspots:"));
    fotografia.hotspots.slice(0, 5).forEach((hotspot, indice) => {
        escreverLinha(`${indice + 1}. ${hotspot.arquivo} [${hotspot.camada}]`);
        escreverLinha(`   Linhas: ${hotspot.linhas} | Score: ${hotspot.score}`);
    });

    if (!args.includes("--sem-gravar")) {
        const diretorio = lerOpcao(args, "--saida") ?? DIRETORIO_SAIDA_PADRAO;
        escreverLinha("");
        escreverLinha(`${pc.green("✓")} Fotografia salva em ${path.relative(process.cwd(), diretorio).replaceAll("\\", "/")}`);
    }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
    main().catch((erro) => {
        escreverLinha(pc.red(`Erro na auditoria de residuos: ${erro.message}`));
        process.exit(1);
    });
}

export {
    executarAuditoriaFrontendResiduos
};
