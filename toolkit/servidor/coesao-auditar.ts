// Auditoria de coesão dos services do servidor.

import fs from "node:fs/promises";
import path from "node:path";
import pc from "picocolors";
import {globby} from "globby";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../biblioteca/saida.js";

const VERSAO_RELATORIO = 2 as const;

// Agrupamento por responsabilidade a partir de prefixos de método
const CATEGORIAS = {
    consulta: {
        prefixos: ["buscar", "listar", "obter", "encontrar", "pesquisar", "verificar", "checar", "contar", "existir", "existe"],
        descricao: "consulta/leitura"
    },
    mutacao: {
        prefixos: ["criar", "salvar", "atualizar", "excluir", "remover", "apagar", "inserir", "editar", "alterar", "registrar"],
        descricao: "mutação/escrita"
    },
    workflow: {
        prefixos: ["iniciar", "finalizar", "ativar", "desativar", "aprovar", "rejeitar", "submeter", "disponibilizar",
            "aceitar", "devolver", "homologar", "transitar", "mover", "validar", "processar", "executar", "aplicar",
            "reabrir", "bloquear", "liberar", "cancelar"],
        descricao: "workflow/transição"
    },
    notificacao: {
        prefixos: ["notificar", "enviar", "lembrar", "alertar", "comunicar", "publicar", "emitir", "disparar"],
        descricao: "notificação/comunicação"
    },
    permissao: {
        prefixos: ["checarAcesso", "temPermissao", "podeRealizar", "autorizar", "verificarPermissao", "hasPermission",
            "isPermitido", "possuiPermissao"],
        descricao: "permissão/acesso"
    }
};

type Categoria = keyof typeof CATEGORIAS;
type CategoriaDetectada = Categoria | "outro";
type Severidade = "critico" | "alerta" | "ok";

interface AnaliseCoesao {
    porCategoria: Partial<Record<CategoriaDetectada, string[]>>;
    categoriasPresentes: Categoria[];
    quantidadeCategorias: number;
    severidade: Severidade;
    motivos: string[];
}

interface ResultadoCoesao extends AnaliseCoesao {
    nomeArquivo: string;
    caminhoRelativo: string;
    pacote: string;
    totalMetodos: number;
}

interface RelatorioCoesao {
    versao: typeof VERSAO_RELATORIO;
    geradoEm: string;
    criterio: string;
    resumo: {
        totalAnalisados: number;
        criticos: number;
        alertas: number;
        ok: number;
    };
    pontosCriticos: ResultadoCoesao[];
    todos: ResultadoCoesao[];
}

function normalizarCaminho(caminho: string): string {
    return caminho.replaceAll(path.sep, "/");
}

function extrairNomesMetodosPublicos(conteudo: string): string[] {
    const regex = /^\s+public\s+(?:static\s+)?(?!class|interface|enum|record\b)(?:@\w+(?:\([^)]*\))?\s+)*[\w<>[\],\s]+\s+([a-z]\w*)\s*\(/gm;
    const nomes: string[] = [];
    for (const correspondencia of conteudo.matchAll(regex)) {
        if (correspondencia[1]) nomes.push(correspondencia[1]);
    }
    return nomes;
}

function classificarMetodo(nomeMetodo: string): CategoriaDetectada {
    const nomeLower = nomeMetodo.toLowerCase();
    for (const categoria of Object.keys(CATEGORIAS) as Categoria[]) {
        const def = CATEGORIAS[categoria];
        if (def.prefixos.some((pref) => nomeLower.startsWith(pref.toLowerCase()))) {
            return categoria;
        }
    }
    return "outro";
}

function analisarCoesao(metodos: string[]): AnaliseCoesao {
    const porCategoria: Partial<Record<CategoriaDetectada, string[]>> = {};

    for (const metodo of metodos) {
        const cat = classificarMetodo(metodo);
        const metodosDaCategoria = porCategoria[cat] ?? [];
        metodosDaCategoria.push(metodo);
        porCategoria[cat] = metodosDaCategoria;
    }

    const categoriasPresentes = (Object.keys(porCategoria) as CategoriaDetectada[])
        .filter((categoria): categoria is Categoria => categoria !== "outro" && (porCategoria[categoria]?.length ?? 0) > 0);
    const quantidadeCategorias = categoriasPresentes.length;

    let severidade: Severidade;
    if (quantidadeCategorias >= 4) severidade = "critico";
    else if (quantidadeCategorias >= 3) severidade = "alerta";
    else severidade = "ok";

    const motivoPartes = categoriasPresentes.map((categoria) => {
        const def = CATEGORIAS[categoria];
        return `${def.descricao} (${porCategoria[categoria]?.length ?? 0})`;
    });

    return {porCategoria, categoriasPresentes, quantidadeCategorias, severidade, motivos: motivoPartes};
}

function extrairPacote(conteudo: string): string {
    return conteudo.match(/^package\s+([\w.]+)\s*;/m)?.[1] ?? "";
}

async function auditarCoesao(diretorioCodigo: string, diretorioBase: string): Promise<RelatorioCoesao> {
    const arquivos = await globby(path.join(diretorioCodigo, "**/*Service.java").replace(/\\/g, "/"), {absolute: true});
    const resultados: ResultadoCoesao[] = [];

    for (const arquivo of arquivos) {
        const conteudo = await fs.readFile(arquivo, "utf-8");
        const nomeArquivo = path.basename(arquivo);
        const pacote = extrairPacote(conteudo);
        const caminhoRelativo = normalizarCaminho(path.relative(diretorioBase, arquivo));

        // Ignorar interfaces e classes de teste
        if (conteudo.includes("interface ") && !conteudo.includes("class ")) continue;
        if (caminhoRelativo.includes("/test/")) continue;

        const metodos = extrairNomesMetodosPublicos(conteudo);
        const analise = analisarCoesao(metodos);

        const resultado: ResultadoCoesao = {
            nomeArquivo,
            caminhoRelativo,
            pacote,
            totalMetodos: metodos.length,
            ...analise
        };
        resultados.push(resultado);
    }

    resultados.sort((a, b) => {
        const ordem = {critico: 0, alerta: 1, ok: 2};
        if (ordem[a.severidade] !== ordem[b.severidade]) return ordem[a.severidade] - ordem[b.severidade];
        return b.quantidadeCategorias - a.quantidadeCategorias;
    });

    const criticos = resultados.filter((r) => r.severidade === "critico");
    const alertas = resultados.filter((r) => r.severidade === "alerta");

    return {
        versao: VERSAO_RELATORIO,
        geradoEm: new Date().toISOString(),
        criterio: "Services com 3+ categorias de responsabilidade: consulta, mutação, workflow, notificação, permissão.",
        resumo: {
            totalAnalisados: resultados.length,
            criticos: criticos.length,
            alertas: alertas.length,
            ok: resultados.filter((r) => r.severidade === "ok").length
        },
        pontosCriticos: resultados.filter((r) => r.severidade !== "ok"),
        todos: resultados
    };
}

function gerarMarkdown(relatorio: RelatorioCoesao): string {
    const linhas: string[] = [];
    linhas.push("# Auditoria de coesão do servidor", "");
    linhas.push(`Gerado em: ${relatorio.geradoEm}`, "");
    linhas.push(`> ${relatorio.criterio}`, "");
    linhas.push("## Resumo", "");
    linhas.push(`- Analisados: ${relatorio.resumo.totalAnalisados}`);
    linhas.push(`- Críticos (4+ categorias): ${relatorio.resumo.criticos}`);
    linhas.push(`- Alertas (3 categorias): ${relatorio.resumo.alertas}`);
    linhas.push(`- OK: ${relatorio.resumo.ok}`, "");

    if (relatorio.pontosCriticos.length === 0) {
        linhas.push("Nenhum service com responsabilidades misturadas encontrado.");
        return linhas.join("\n");
    }

    linhas.push("## Pontos criticos", "");
    linhas.push("| Arquivo | Métodos | Categorias | Distribuição | Severidade |");
    linhas.push("|---------|---------|------------|-------------|-----------|");

    for (const item of relatorio.pontosCriticos) {
        const sev = item.severidade === "critico" ? "🔴 crítico" : "🟡 alerta";
        const distribuicao = item.motivos.join(", ");
        linhas.push(`| \`${item.nomeArquivo}\` | ${item.totalMetodos} | ${item.quantidadeCategorias} | ${distribuicao} | ${sev} |`);
    }

    linhas.push("", "## Detalhes dos pontos criticos", "");
    for (const item of relatorio.pontosCriticos) {
        linhas.push(`### ${item.nomeArquivo}`, "");
        linhas.push(`- Pacote: \`${item.pacote}\``);
        linhas.push(`- Total de métodos públicos: ${item.totalMetodos}`);
        linhas.push(`- Categorias detectadas: ${item.quantidadeCategorias}`, "");

        for (const cat of item.categoriasPresentes) {
            const def = CATEGORIAS[cat];
            const metodos = item.porCategoria[cat] ?? [];
            linhas.push(`**${def.descricao}** (${metodos.length}): ${metodos.map((m) => `\`${m}\``).join(", ")}`);
        }

        const metodosNaoClassificados = item.porCategoria.outro ?? [];
        if (metodosNaoClassificados.length > 0) {
            linhas.push(`\n**não classificados**: ${metodosNaoClassificados.map((m) => `\`${m}\``).join(", ")}`);
        }
        linhas.push("");
    }

    linhas.push("## Por que isso importa", "");
    linhas.push("Um service com muitas categorias de responsabilidade:");
    linhas.push("- acumula dependências de domínios diferentes;");
    linhas.push("- torna difícil testar cada responsabilidade em isolamento;");
    linhas.push("- aumenta o risco de efeito colateral entre fluxos distintos;");
    linhas.push("- dificulta fatiamento futuro por caso de uso.", "");
    linhas.push("## Primeiro corte sugerido", "");
    if (relatorio.pontosCriticos.length > 0) {
        const top = relatorio.pontosCriticos[0];
        linhas.push(`Começar por \`${top.nomeArquivo}\`.`);
        linhas.push("Separar os métodos por categoria e verificar quais dependências cada grupo realmente precisa.");
        linhas.push("Extrair apenas quando a fronteira representar um conceito real — não por contagem de linhas.");
    }

    return linhas.join("\n");
}

async function gravarRelatorios(relatorio: RelatorioCoesao, diretorioSaida: string): Promise<{
    caminhoMarkdown: string;
    caminhoJson: string;
}> {
    const caminhoMarkdown = path.join(diretorioSaida, "coesao-auditoria.md");
    const caminhoJson = path.join(diretorioSaida, "coesao-auditoria.json");
    await fs.mkdir(diretorioSaida, {recursive: true});
    await fs.writeFile(caminhoMarkdown, gerarMarkdown(relatorio), "utf-8");
    await fs.writeFile(caminhoJson, JSON.stringify(relatorio, null, 2), "utf-8");
    return {caminhoMarkdown, caminhoJson};
}

function exibirAjuda(): void {
    exibirAjudaComando({
        comandoSgc: "servidor coesao auditar",
        scriptDireto: "servidor/coesao-auditar.ts",
        descricao: "Audita Services do servidor detectando mistura de responsabilidades (consulta, mutação, workflow, notificação, permissão).",
        opcoes: [
            "--json              Emite o relatório em JSON.",
            "--gravar            Grava os relatórios em disco.",
            "--base <diretorio>  Sobrescreve a base da auditoria.",
            "--help, -h          Exibe esta ajuda."
        ],
        exemplos: [
            "npx tsx toolkit/sgc.ts servidor coesao auditar",
            "npx tsx toolkit/sgc.ts servidor coesao auditar --json",
            "npx tsx toolkit/sgc.ts servidor coesao auditar --gravar"
        ]
    });
}

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);

    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjuda();
        return;
    }

    const emitirJson = argumentos.includes("--json");
    const gravar = argumentos.includes("--gravar");
    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const diretorioCodigo = resolverCaminhoConfigurado("codigoServidor", diretorioBase);
    const diretorioSaida = path.join(resolverCaminhoConfigurado("artefatosQualidade", diretorioBase), "servidor", "mais-recente");

    if (!emitirJson) {
        imprimirCabecalho("AUDITORIA DE COESÃO DO SERVIDOR");
        escreverLinha(`Base analisada: ${pc.dim(diretorioCodigo)}`);
    }

    const relatorio = await auditarCoesao(diretorioCodigo, diretorioBase);

    if (gravar) {
        const caminhosRelatorios = await gravarRelatorios(relatorio, diretorioSaida);
        if (!emitirJson) {
            escreverLinha(`Relatório Markdown: ${pc.dim(caminhosRelatorios.caminhoMarkdown)}`);
            escreverLinha(`Relatório JSON: ${pc.dim(caminhosRelatorios.caminhoJson)}`);
        }
    }

    if (!emitirJson) {
        escreverLinha(`Analisados: ${relatorio.resumo.totalAnalisados} — Críticos: ${pc.red(String(relatorio.resumo.criticos))} — Alertas: ${pc.yellow(String(relatorio.resumo.alertas))} — OK: ${pc.green(String(relatorio.resumo.ok))}`);

        if (relatorio.pontosCriticos.length > 0) {
            escreverLinha("");
            escreverLinha(pc.bold("Pontos criticos:"));
            for (const item of relatorio.pontosCriticos.slice(0, 10)) {
                const cor = item.severidade === "critico" ? pc.red : pc.yellow;
                escreverLinha(`  ${cor("●")} ${item.nomeArquivo} — ${item.motivos.join(", ")}`);
            }
        } else {
            escreverLinha(pc.green("Nenhum service com responsabilidades misturadas encontrado."));
        }
    }

    if (emitirJson) {
        imprimirJson(relatorio);
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        escreverLinha(pc.red(`Erro ao auditar coesão: ${erro instanceof Error ? erro.message : String(erro)}`));
        process.exitCode = 1;
    });
}

export {
    principal,
};
