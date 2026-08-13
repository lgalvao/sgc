import path from "node:path";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {analisarArquiteturaFrontend, gravarFotografiaArquitetura, resolverDiretorioSaidaArquitetura, type FotografiaArquitetura} from "./arquitetura-lib.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../lib/execucao.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

interface OpcoesAuditoriaArquiteturaFrontend {
    base?: string;
    saida?: string;
    gravar?: boolean;
}

async function executarAuditoriaArquiteturaFrontend(
    opcoes: OpcoesAuditoriaArquiteturaFrontend = {}
): Promise<FotografiaArquitetura> {
    const snapshot = await analisarArquiteturaFrontend({base: opcoes.base});

    if (opcoes.gravar) {
        const diretorioSaida = opcoes.saida ? path.resolve(snapshot.base, opcoes.saida) : undefined;
        await gravarFotografiaArquitetura(snapshot, diretorioSaida);
    }

    return snapshot;
}

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoSgc: "frontend arquitetura auditar",
            scriptDireto: "frontend/arquitetura-auditar.ts",
            descricao: "Audita vazamentos arquiteturais do frontend, incluindo estrategia de cache exposta nas views, hubs centrais sobrecarregados e server state caseiro.",
            opcoes: [
                "--json               Emite a fotografia em JSON.",
                "--gravar             Atualiza fotografia e resumo em disco.",
                "--base <diretorio>   Sobrescreve o diretorio base da auditoria.",
                "--saida <diretorio>  Sobrescreve o diretorio de saida da fotografia."
            ],
            exemplos: [
                "npx tsx toolkit/sgc.ts frontend arquitetura auditar",
                "npx tsx toolkit/sgc.ts frontend arquitetura auditar --json",
                "npx tsx toolkit/sgc.ts frontend arquitetura auditar --gravar --base /tmp/sgc"
            ]
        });
        return;
    }

    const base = lerOpcao(argumentos, "--base", undefined);
    const baseResolvida = path.resolve(base ?? DIRETORIO_RAIZ);
    const saidaPadrao = resolverDiretorioSaidaArquitetura(baseResolvida);
    const saida = lerOpcao(argumentos, "--saida", saidaPadrao);
    const snapshot = await executarAuditoriaArquiteturaFrontend({
        base: baseResolvida,
        saida,
        gravar: argumentos.includes("--gravar"),
    });

    if (emitirJson) {
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
    escreverLinha(`Fachadas puras: ${snapshot.resumo.metricas.fachadasPuras}`);
    escreverLinha(`Composables minúsculos: ${snapshot.resumo.metricas.composablesMinusculos}`);
    escreverLinha(`Famílias pulverizadas: ${snapshot.resumo.metricas.familiasPulverizadas}`);
    escreverLinha("");
    escreverLinha(pc.bold("Top 5 hotspots:"));
    snapshot.hotspots.slice(0, 5).forEach((hotspot, indice) => {
        escreverLinha(`${indice + 1}. ${hotspot.arquivo} [${hotspot.camada}]`);
        escreverLinha(`   Score: ${hotspot.score} | Sinais: ${hotspot.sinaisAtivos.join(", ")}`);
        escreverLinha(`   Fan-out: ${hotspot.metricasAst.categoriasAcoplamento} categorias / ${hotspot.metricasAst.importacoesArquiteturais} imports`);
    });

    if (argumentos.includes("--gravar")) {
        const diretorio = path.resolve(snapshot.base, saida ?? resolverDiretorioSaidaArquitetura(snapshot.base));
        escreverLinha("");
        escreverLinha(`${pc.green("✓")} Fotografia salva em ${path.relative(snapshot.base, diretorio).replaceAll("\\", "/")}`);
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro na auditoria arquitetural: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    executarAuditoriaArquiteturaFrontend,
    principal,
};
