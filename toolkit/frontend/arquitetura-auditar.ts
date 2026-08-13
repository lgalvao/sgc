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
    const fotografia = await analisarArquiteturaFrontend({base: opcoes.base});

    if (opcoes.gravar) {
        const diretorioSaida = opcoes.saida ? path.resolve(fotografia.base, opcoes.saida) : undefined;
        await gravarFotografiaArquitetura(fotografia, diretorioSaida);
    }

    return fotografia;
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
    const fotografia = await executarAuditoriaArquiteturaFrontend({
        base: baseResolvida,
        saida,
        gravar: argumentos.includes("--gravar"),
    });

    if (emitirJson) {
        imprimirJson(fotografia);
        return;
    }

    imprimirCabecalho("AUDITORIA ARQUITETURAL DO FRONTEND");
    escreverLinha(`Pontuacao total: ${pc.bold(String(fotografia.resumo.pontuacaoTotal))} (${fotografia.resumo.faixa})`);
    escreverLinha(`Arquivos de producao: ${fotografia.resumo.arquivosProducao}`);
    escreverLinha(`Views com vazamento de cache: ${fotografia.resumo.metricas.viewsComVazamentoCache}`);
    escreverLinha(`Views com service direto: ${fotografia.resumo.metricas.viewsComServiceDireto}`);
    escreverLinha(`Views com server state caseiro: ${fotografia.resumo.metricas.viewsComServerStateCaseiro}`);
    escreverLinha(`Views com fan-out alto: ${fotografia.resumo.metricas.viewsComFanoutAlto}`);
    escreverLinha(`Acessos diretos a cache: ${fotografia.resumo.metricas.acessosDiretosCache}`);
    escreverLinha(`Metodos xxxEmCache consumidos: ${fotografia.resumo.metricas.metodosEmCache}`);
    escreverLinha(`Booleanos posicionais: ${fotografia.resumo.metricas.booleanosPosicionais}`);
    escreverLinha(`Ocorrencias de forcar: ${fotografia.resumo.metricas.ocorrenciasForcar}`);
    escreverLinha(`Bolsas largas de dependencias/estado: ${fotografia.resumo.metricas.arquivosComBolsaDependenciasLarga}`);
    escreverLinha(`Superficies exportadas amplas: ${fotografia.resumo.metricas.arquivosComSuperficieAmpla}`);
    escreverLinha(`Arquivos com mistura de camadas: ${fotografia.resumo.metricas.arquivosComMisturaCamadas}`);
    escreverLinha(`Arquivos com server state caseiro: ${fotografia.resumo.metricas.arquivosComServerStateCaseiro}`);
    escreverLinha(`Hubs centrais com sinais: ${fotografia.resumo.metricas.hubsCentraisComSinais}`);
    escreverLinha(`Fachadas puras: ${fotografia.resumo.metricas.fachadasPuras}`);
    escreverLinha(`Composables minúsculos: ${fotografia.resumo.metricas.composablesMinusculos}`);
    escreverLinha(`Famílias pulverizadas: ${fotografia.resumo.metricas.familiasPulverizadas}`);
    escreverLinha("");
    escreverLinha(pc.bold("5 principais pontos criticos:"));
    fotografia.pontosCriticos.slice(0, 5).forEach((pontoCritico, indice) => {
        escreverLinha(`${indice + 1}. ${pontoCritico.arquivo} [${pontoCritico.camada}]`);
        escreverLinha(`   Pontuacao: ${pontoCritico.pontuacao} | Sinais: ${pontoCritico.sinaisAtivos.join(", ")}`);
        escreverLinha(`   Fan-out: ${pontoCritico.metricasAst.categoriasAcoplamento} categorias / ${pontoCritico.metricasAst.importacoesArquiteturais} imports`);
    });

    if (argumentos.includes("--gravar")) {
        const diretorio = path.resolve(fotografia.base, saida ?? resolverDiretorioSaidaArquitetura(fotografia.base));
        escreverLinha("");
        escreverLinha(`${pc.green("✓")} Fotografia salva em ${path.relative(fotografia.base, diretorio).replaceAll("\\", "/")}`);
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
