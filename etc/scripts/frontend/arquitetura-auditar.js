#!/usr/bin/env node
import path from "node:path";
import {pathToFileURL} from "node:url";
import pc from "picocolors";
import {analisarArquiteturaFrontend, DIRETORIO_SAIDA_PADRAO, gravarSnapshotArquitetura} from "./arquitetura-lib.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

function lerOpcao(argumentos, nome) {
    const indice = argumentos.indexOf(nome);
    if (indice === -1) {
        return null;
    }
    return argumentos[indice + 1] ?? null;
}

async function executarAuditoriaArquiteturaFrontend(opcoes = {}) {
    const snapshot = await analisarArquiteturaFrontend({base: opcoes.base});

    if (!opcoes.semGravar) {
        await gravarSnapshotArquitetura(snapshot, opcoes.saida);
    }

    return snapshot;
}

async function main() {
    const args = process.argv.slice(2);
    const jsonMode = args.includes("--json");
    const helpMode = args.includes("--help") || args.includes("-h");

    if (helpMode) {
        exibirAjudaComando({
            comandoSgc: "frontend arquitetura auditar",
            scriptDireto: "frontend/arquitetura-auditar.js",
            descricao: "Audita vazamentos arquiteturais do frontend, incluindo estrategia de cache exposta nas views, hubs centrais sobrecarregados e server state caseiro.",
            opcoes: [
                "--json               Emite o snapshot em JSON.",
                "--sem-gravar         Nao grava snapshot/resumo em disco.",
                "--base <diretorio>   Sobrescreve o diretorio base da auditoria.",
                "--saida <diretorio>  Sobrescreve o diretorio de saida do snapshot."
            ],
            exemplos: [
                "node etc/scripts/sgc.js frontend arquitetura auditar",
                "node etc/scripts/sgc.js frontend arquitetura auditar --json",
                "node etc/scripts/sgc.js frontend arquitetura auditar --sem-gravar --base /tmp/sgc"
            ]
        });
        process.exit(0);
    }

    const snapshot = await executarAuditoriaArquiteturaFrontend({
        base: lerOpcao(args, "--base"),
        saida: lerOpcao(args, "--saida") ?? DIRETORIO_SAIDA_PADRAO,
        semGravar: args.includes("--sem-gravar"),
    });

    if (jsonMode) {
        imprimirJson(snapshot);
        return;
    }

    imprimirCabecalho("AUDITORIA ARQUITETURAL DO FRONTEND");
    escreverLinha(`Score total: ${pc.bold(String(snapshot.resumo.scoreTotal))} (${snapshot.resumo.faixa})`);
    escreverLinha(`Arquivos de producao: ${snapshot.resumo.arquivosProducao}`);
    escreverLinha(`Views com vazamento de cache: ${snapshot.resumo.metricas.viewsComVazamentoCache}`);
    escreverLinha(`Views com service direto: ${snapshot.resumo.metricas.viewsComServiceDireto}`);
    escreverLinha(`Views com server state caseiro: ${snapshot.resumo.metricas.viewsComServerStateCaseiro}`);
    escreverLinha(`Views com fan-out alto: ${snapshot.resumo.metricas.viewsComFanoutAlto}`);
    escreverLinha(`Acessos diretos a cache: ${snapshot.resumo.metricas.acessosDiretosCache}`);
    escreverLinha(`Metodos xxxEmCache consumidos: ${snapshot.resumo.metricas.metodosEmCache}`);
    escreverLinha(`Booleanos posicionais: ${snapshot.resumo.metricas.booleanosPosicionais}`);
    escreverLinha(`Ocorrencias de forcar: ${snapshot.resumo.metricas.ocorrenciasForcar}`);
    escreverLinha(`Bolsas largas de dependencias/estado: ${snapshot.resumo.metricas.arquivosComBolsaDependenciasLarga}`);
    escreverLinha(`Superficies exportadas amplas: ${snapshot.resumo.metricas.arquivosComSuperficieAmpla}`);
    escreverLinha(`Arquivos com mistura de camadas: ${snapshot.resumo.metricas.arquivosComMisturaCamadas}`);
    escreverLinha(`Arquivos com server state caseiro: ${snapshot.resumo.metricas.arquivosComServerStateCaseiro}`);
    escreverLinha(`Hubs centrais com sinais: ${snapshot.resumo.metricas.hubsCentraisComSinais}`);
    escreverLinha("");
    escreverLinha(pc.bold("Top 5 hotspots:"));
    snapshot.hotspots.slice(0, 5).forEach((hotspot, indice) => {
        escreverLinha(`${indice + 1}. ${hotspot.arquivo} [${hotspot.camada}]`);
        escreverLinha(`   Score: ${hotspot.score} | Sinais: ${hotspot.sinaisAtivos.join(", ")}`);
        escreverLinha(`   Fan-out: ${hotspot.metricasAst.categoriasAcoplamento} categorias / ${hotspot.metricasAst.importacoesArquiteturais} imports`);
    });

    if (!args.includes("--sem-gravar")) {
        const diretorio = lerOpcao(args, "--saida") ?? DIRETORIO_SAIDA_PADRAO;
        escreverLinha("");
        escreverLinha(`${pc.green("✓")} Snapshot salvo em ${path.relative(process.cwd(), diretorio).replaceAll("\\", "/")}`);
    }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
    main().catch((erro) => {
        escreverLinha(pc.red(`Erro na auditoria arquitetural: ${erro.message}`));
        process.exit(1);
    });
}

export {
    executarAuditoriaArquiteturaFrontend
};
