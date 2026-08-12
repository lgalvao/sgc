#!/usr/bin/env node
import path from "node:path";
import pc from "picocolors";
import {
    analisarResiduosFrontend,
    gravarFotografiaAuditoria,
    resolverCaminhoOrcamentoResiduos,
    resolverDiretorioSaidaResiduos
} from "./residuos-lib.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";

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

async function principal(argumentos = process.argv.slice(2)) {
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
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
                "npx tsx toolkit/sgc.js frontend residuos auditar",
                "npx tsx toolkit/sgc.js frontend residuos auditar --json",
                "npx tsx toolkit/sgc.js frontend residuos auditar --sem-gravar --base /tmp/sgc"
            ]
        });
        return;
    }

    const base = lerOpcao(argumentos, "--base", undefined);
    const fotografia = await executarAuditoriaFrontendResiduos({
        base,
        orcamento: lerOpcao(argumentos, "--orcamento", resolverCaminhoOrcamentoResiduos(base)),
        saida: lerOpcao(argumentos, "--saida", resolverDiretorioSaidaResiduos(base)),
        semGravar: argumentos.includes("--sem-gravar"),
    });

    if (emitirJson) {
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

    if (!argumentos.includes("--sem-gravar")) {
        const diretorio = lerOpcao(argumentos, "--saida", resolverDiretorioSaidaResiduos(fotografia.base));
        escreverLinha("");
        escreverLinha(`${pc.green("✓")} Fotografia salva em ${path.relative(process.cwd(), diretorio).replaceAll("\\", "/")}`);
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro na auditoria de residuos: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    executarAuditoriaFrontendResiduos,
    principal,
};
