import fs from "node:fs";
import path from "node:path";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverLinha, imprimirJson} from "../biblioteca/saida.js";

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

interface ViolacaoView {
    arquivo: string;
    linha: number;
    regra: "view-sem-layout-padrao" | "view-sem-cabecalho-padrao" | "view-com-bmodal-cru";
    motivo: string;
}

interface ResultadoValidacaoViews {
    resumo: {
        totalViews: number;
        totalViolacoes: number;
        porRegra: Record<string, number>;
    };
    violacoes: ViolacaoView[];
}

interface OpcoesValidacaoViews {
    base?: string;
}

function listarViews(diretorioViews: string): string[] {
    return fs.readdirSync(diretorioViews)
        .filter((nome) => nome.endsWith(".vue"))
        .map((nome) => path.join(diretorioViews, nome))
        .toSorted();
}

function localizarLinha(conteudo: string, trecho: string): number | null {
    const indice = conteudo.indexOf(trecho);
    if (indice === -1) {
        return null;
    }
    return conteudo.slice(0, indice).split(/\r?\n/u).length;
}

function auditarView(caminhoArquivo: string, diretorioBase: string): ViolacaoView[] {
    const conteudo = fs.readFileSync(caminhoArquivo, "utf8");
    const nomeArquivo = path.basename(caminhoArquivo);
    const caminhoRelativo = path.relative(diretorioBase, caminhoArquivo).replaceAll("\\", "/");
    const violacoes: ViolacaoView[] = [];

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

function resumir(violacoes: ViolacaoView[], totalViews: number): ResultadoValidacaoViews["resumo"] {
    return {
        totalViews,
        totalViolacoes: violacoes.length,
        porRegra: violacoes.reduce<Record<string, number>>((acc, violacao) => {
            acc[violacao.regra] = (acc[violacao.regra] ?? 0) + 1;
            return acc;
        }, {}),
    };
}

async function executarValidacaoTemplatesViews(opcoes: OpcoesValidacaoViews = {}): Promise<ResultadoValidacaoViews> {
    const diretorioBase = path.resolve(opcoes.base ?? DIRETORIO_RAIZ);
    const diretorioViews = path.join(resolverCaminhoConfigurado("frontendCodigo", diretorioBase), "views");
    const views = listarViews(diretorioViews);
    const violacoes = views.flatMap((arquivo) => auditarView(arquivo, diretorioBase));
    return {
        resumo: resumir(violacoes, views.length),
        violacoes,
    };
}

function imprimirResultado(resultado: ResultadoValidacaoViews): void {
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

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoSgc: "frontend views templates-validar",
            scriptDireto: "cliente/views-templates-validar.ts",
            descricao: "Valida previsibilidade estrutural das views do frontend (shell, header e proibicao de BModal cru).",
            opcoes: [
                "--json               Emite o resultado bruto em JSON.",
                "--base <diretorio>   Sobrescreve o diretorio base da validacao.",
            ],
            exemplos: [
                "npx tsx toolkit/sgc.ts frontend views templates-validar",
                "npx tsx toolkit/sgc.ts frontend views templates-validar --json",
            ],
        });
        return;
    }

    const resultado = await executarValidacaoTemplatesViews({
        base: lerOpcao(argumentos, "--base", undefined),
    });

    if (emitirJson) {
        imprimirJson(resultado);
    } else {
        imprimirResultado(resultado);
    }

    if (resultado.violacoes.length > 0) {
        process.exitCode = 1;
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro na validacao de templates das views: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    executarValidacaoTemplatesViews,
    principal,
};
