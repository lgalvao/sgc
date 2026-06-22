#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import {pathToFileURL} from "node:url";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";

const EXCECOES_SEM_LAYOUT = new Set([
    "LoginView.vue",
    "ErroGeralView.vue",
]);

const MARCADORES_CABECALHO = [
    "<PageHeader",
    "<SubprocessoResumoHeader",
    "<CadastroAcoesHeader",
    "<MapaAcoesHeader",
    "<ProcessoAcoes",
];

function lerOpcao(argumentos, nome) {
    const indice = argumentos.indexOf(nome);
    if (indice === -1) {
        return null;
    }
    return argumentos[indice + 1] ?? null;
}

function listarViews(diretorioViews) {
    return fs.readdirSync(diretorioViews)
        .filter((nome) => nome.endsWith(".vue"))
        .map((nome) => path.join(diretorioViews, nome))
        .sort();
}

function localizarLinha(conteudo, trecho) {
    const indice = conteudo.indexOf(trecho);
    if (indice === -1) {
        return null;
    }
    return conteudo.slice(0, indice).split(/\r?\n/u).length;
}

function auditarView(caminhoArquivo, diretorioBase) {
    const conteudo = fs.readFileSync(caminhoArquivo, "utf8");
    const nomeArquivo = path.basename(caminhoArquivo);
    const caminhoRelativo = path.relative(diretorioBase, caminhoArquivo).replaceAll("\\", "/");
    const violacoes = [];

    const usaLayoutPadrao = conteudo.includes("<LayoutPadrao");
    const usaCabecalhoEsperado = MARCADORES_CABECALHO.some((marcador) => conteudo.includes(marcador));
    const usaBModalCru = conteudo.includes("<BModal");

    if (!EXCECOES_SEM_LAYOUT.has(nomeArquivo) && !usaLayoutPadrao) {
        violacoes.push({
            arquivo: caminhoRelativo,
            linha: 1,
            regra: "view-sem-layout-padrao",
            motivo: "Views de caso de uso devem declarar LayoutPadrao como shell principal.",
        });
    }

    if (!EXCECOES_SEM_LAYOUT.has(nomeArquivo) && !usaCabecalhoEsperado) {
        violacoes.push({
            arquivo: caminhoRelativo,
            linha: 1,
            regra: "view-sem-cabecalho-padrao",
            motivo: "Views devem expor um cabecalho previsivel via PageHeader ou header especializado aprovado.",
        });
    }

    if (usaBModalCru) {
        violacoes.push({
            arquivo: caminhoRelativo,
            linha: localizarLinha(conteudo, "<BModal") ?? 1,
            regra: "view-com-bmodal-cru",
            motivo: "Views nao devem abrir BModal diretamente; use ModalConfirmacao, ModalPadrao ou um root de modais da feature.",
        });
    }

    return violacoes;
}

function resumir(violacoes, totalViews) {
    return {
        totalViews,
        totalViolacoes: violacoes.length,
        porRegra: violacoes.reduce((acc, violacao) => {
            acc[violacao.regra] = (acc[violacao.regra] ?? 0) + 1;
            return acc;
        }, {}),
    };
}

async function executarValidacaoTemplatesViews(opcoes = {}) {
    const diretorioBase = path.resolve(opcoes.base ?? DIRETORIO_RAIZ);
    const diretorioViews = path.join(diretorioBase, "frontend", "src", "views");
    const views = listarViews(diretorioViews);
    const violacoes = views.flatMap((arquivo) => auditarView(arquivo, diretorioBase));
    return {
        resumo: resumir(violacoes, views.length),
        violacoes,
    };
}

function imprimirResultado(resultado) {
    if (resultado.violacoes.length === 0) {
        escreverLinha(`${pc.green("✓")} Templates de views padronizados. Nenhum BModal cru encontrado em views.`);
        return;
    }

    escreverLinha(pc.red(`Foram encontradas ${resultado.violacoes.length} violacoes de previsibilidade em views:`));
    resultado.violacoes.forEach((violacao, indice) => {
        escreverLinha(`${indice + 1}. [${violacao.regra}] ${violacao.arquivo}:${violacao.linha}`);
        escreverLinha(`   ${violacao.motivo}`);
    });
}

async function main() {
    const args = process.argv.slice(2);
    const jsonMode = args.includes("--json");
    const helpMode = args.includes("--help") || args.includes("-h");

    if (helpMode) {
        exibirAjudaComando({
            comandoSgc: "frontend views templates-validar",
            scriptDireto: "frontend/views-templates-validar.js",
            descricao: "Valida previsibilidade estrutural das views do frontend (shell, header e proibicao de BModal cru).",
            opcoes: [
                "--json               Emite o resultado bruto em JSON.",
                "--base <diretorio>   Sobrescreve o diretorio base da validacao.",
            ],
            exemplos: [
                "node toolkit/sgc.js frontend views templates-validar",
                "node toolkit/sgc.js frontend views templates-validar --json",
            ],
        });
        process.exit(0);
    }

    const resultado = await executarValidacaoTemplatesViews({
        base: lerOpcao(args, "--base"),
    });

    if (jsonMode) {
        imprimirJson(resultado);
    } else {
        imprimirResultado(resultado);
    }

    if (resultado.violacoes.length > 0) {
        process.exit(1);
    }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
    main().catch((erro) => {
        escreverLinha(pc.red(`Erro na validacao de templates das views: ${erro.message}`));
        process.exit(1);
    });
}

export {
    executarValidacaoTemplatesViews,
};
