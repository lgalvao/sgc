#!/usr/bin/env node

import fs from "node:fs/promises";
import path from "node:path";
import pc from "picocolors";
import {globby} from "globby";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

const DIRETORIO_BACKEND = resolverNaRaiz("backend/src/main/java/sgc");
const CAMINHO_RELATORIO_MD = resolverNaRaiz("toolkit/qualidade/backend/latest/coesao-auditoria.md");
const CAMINHO_RELATORIO_JSON = resolverNaRaiz("toolkit/qualidade/backend/latest/coesao-auditoria.json");

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

function normalizarCaminho(caminho) {
    return caminho.replaceAll(path.sep, "/");
}

function extrairNomesMetodosPublicos(conteudo) {
    const regex = /^\s+public\s+(?:static\s+)?(?!class|interface|enum|record\b)(?:@\w+(?:\([^)]*\))?\s+)*[\w<>[\],\s]+\s+([a-z]\w*)\s*\(/gm;
    const nomes = [];
    for (const correspondencia of conteudo.matchAll(regex)) {
        if (correspondencia[1]) nomes.push(correspondencia[1]);
    }
    return nomes;
}

function classificarMetodo(nomeMetodo) {
    const nomeLower = nomeMetodo.toLowerCase();
    for (const [categoria, def] of Object.entries(CATEGORIAS)) {
        if (def.prefixos.some((pref) => nomeLower.startsWith(pref.toLowerCase()))) {
            return categoria;
        }
    }
    return "outro";
}

function analisarCoesao(nomeArquivo, metodos) {
    const porCategoria = {};

    for (const metodo of metodos) {
        const cat = classificarMetodo(metodo);
        if (!porCategoria[cat]) porCategoria[cat] = [];
        porCategoria[cat].push(metodo);
    }

    const categoriasPresentes = Object.keys(porCategoria).filter((c) => c !== "outro" && porCategoria[c].length > 0);
    const quantidadeCategorias = categoriasPresentes.length;

    let severidade;
    if (quantidadeCategorias >= 4) severidade = "critico";
    else if (quantidadeCategorias >= 3) severidade = "alerta";
    else severidade = "ok";

    const motivoPartes = categoriasPresentes.map((c) => {
        const def = CATEGORIAS[c];
        return `${def.descricao} (${porCategoria[c].length})`;
    });

    return {porCategoria, categoriasPresentes, quantidadeCategorias, severidade, motivos: motivoPartes};
}

function extrairPacote(conteudo) {
    return conteudo.match(/^package\s+([\w.]+)\s*;/m)?.[1] ?? "";
}

async function auditarCoesao() {
    const arquivos = await globby(path.join(DIRETORIO_BACKEND, "**/*Service.java").replace(/\\/g, "/"), {absolute: true});
    const resultados = [];

    for (const arquivo of arquivos) {
        const conteudo = await fs.readFile(arquivo, "utf-8");
        const nomeArquivo = path.basename(arquivo);
        const pacote = extrairPacote(conteudo);
        const caminhoRelativo = normalizarCaminho(path.relative(resolverNaRaiz("."), arquivo));

        // Ignorar interfaces e classes de teste
        if (conteudo.includes("interface ") && !conteudo.includes("class ")) continue;
        if (caminhoRelativo.includes("/test/")) continue;

        const metodos = extrairNomesMetodosPublicos(conteudo);
        const analise = analisarCoesao(nomeArquivo, metodos);

        resultados.push({
            nomeArquivo,
            caminhoRelativo,
            pacote,
            totalMetodos: metodos.length,
            ...analise
        });
    }

    resultados.sort((a, b) => {
        const ordem = {critico: 0, alerta: 1, ok: 2};
        if (ordem[a.severidade] !== ordem[b.severidade]) return ordem[a.severidade] - ordem[b.severidade];
        return b.quantidadeCategorias - a.quantidadeCategorias;
    });

    const criticos = resultados.filter((r) => r.severidade === "critico");
    const alertas = resultados.filter((r) => r.severidade === "alerta");

    return {
        geradoEm: new Date().toISOString(),
        criterio: "Services com 3+ categorias de responsabilidade: consulta, mutação, workflow, notificação, permissão.",
        resumo: {
            totalAnalisados: resultados.length,
            criticos: criticos.length,
            alertas: alertas.length,
            ok: resultados.filter((r) => r.severidade === "ok").length
        },
        hotspots: resultados.filter((r) => r.severidade !== "ok"),
        todos: resultados
    };
}

function gerarMarkdown(relatorio) {
    const linhas = [];
    linhas.push("# Auditoria de coesão do backend", "");
    linhas.push(`Gerado em: ${relatorio.geradoEm}`, "");
    linhas.push(`> ${relatorio.criterio}`, "");
    linhas.push("## Resumo", "");
    linhas.push(`- Analisados: ${relatorio.resumo.totalAnalisados}`);
    linhas.push(`- Críticos (4+ categorias): ${relatorio.resumo.criticos}`);
    linhas.push(`- Alertas (3 categorias): ${relatorio.resumo.alertas}`);
    linhas.push(`- OK: ${relatorio.resumo.ok}`, "");

    if (relatorio.hotspots.length === 0) {
        linhas.push("Nenhum service com responsabilidades misturadas encontrado.");
        return linhas.join("\n");
    }

    linhas.push("## Hotspots", "");
    linhas.push("| Arquivo | Métodos | Categorias | Distribuição | Severidade |");
    linhas.push("|---------|---------|------------|-------------|-----------|");

    for (const item of relatorio.hotspots) {
        const sev = item.severidade === "critico" ? "🔴 crítico" : "🟡 alerta";
        const distribuicao = item.motivos.join(", ");
        linhas.push(`| \`${item.nomeArquivo}\` | ${item.totalMetodos} | ${item.quantidadeCategorias} | ${distribuicao} | ${sev} |`);
    }

    linhas.push("", "## Detalhes dos hotspots", "");
    for (const item of relatorio.hotspots) {
        linhas.push(`### ${item.nomeArquivo}`, "");
        linhas.push(`- Pacote: \`${item.pacote}\``);
        linhas.push(`- Total de métodos públicos: ${item.totalMetodos}`);
        linhas.push(`- Categorias detectadas: ${item.quantidadeCategorias}`, "");

        for (const cat of item.categoriasPresentes) {
            const def = CATEGORIAS[cat];
            const metodos = item.porCategoria[cat] ?? [];
            linhas.push(`**${def.descricao}** (${metodos.length}): ${metodos.map((m) => `\`${m}\``).join(", ")}`);
        }

        if (item.porCategoria.outro?.length > 0) {
            linhas.push(`\n**não classificados**: ${item.porCategoria.outro.map((m) => `\`${m}\``).join(", ")}`);
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
    if (relatorio.hotspots.length > 0) {
        const top = relatorio.hotspots[0];
        linhas.push(`Começar por \`${top.nomeArquivo}\`.`);
        linhas.push("Separar os métodos por categoria e verificar quais dependências cada grupo realmente precisa.");
        linhas.push("Extrair apenas quando a fronteira representar um conceito real — não por contagem de linhas.");
    }

    return linhas.join("\n");
}

async function gravarRelatorios(relatorio) {
    await fs.mkdir(path.dirname(CAMINHO_RELATORIO_MD), {recursive: true});
    await fs.writeFile(CAMINHO_RELATORIO_MD, gerarMarkdown(relatorio), "utf-8");
    await fs.writeFile(CAMINHO_RELATORIO_JSON, JSON.stringify(relatorio, null, 2), "utf-8");
}

function exibirAjuda() {
    exibirAjudaComando({
        comandoSgc: "backend coesao auditar",
        scriptDireto: "toolkit/backend/coesao-auditar.js",
        descricao: "Audita Services do backend detectando mistura de responsabilidades (consulta, mutação, workflow, notificação, permissão).",
        opcoes: [
            "--json              Emite o relatório em JSON.",
            "--sem-gravar        Não grava os arquivos em disco.",
            "--help, -h          Exibe esta ajuda."
        ],
        exemplos: [
            "node toolkit/sgc.js backend coesao auditar",
            "node toolkit/sgc.js backend coesao auditar --json",
            "node toolkit/sgc.js backend coesao auditar --sem-gravar"
        ]
    });
}

async function main() {
    const args = process.argv.slice(2);

    if (args.includes("--help") || args.includes("-h")) {
        exibirAjuda();
        return;
    }

    const emitirJson = args.includes("--json");
    const semGravar = args.includes("--sem-gravar");

    imprimirCabecalho("AUDITORIA DE COESÃO DO BACKEND");
    escreverLinha(`Base analisada: ${pc.dim(DIRETORIO_BACKEND)}`);

    const relatorio = await auditarCoesao();

    if (!semGravar) {
        await gravarRelatorios(relatorio);
        escreverLinha(`Relatório Markdown: ${pc.dim(CAMINHO_RELATORIO_MD)}`);
        escreverLinha(`Relatório JSON: ${pc.dim(CAMINHO_RELATORIO_JSON)}`);
    }

    escreverLinha(`Analisados: ${relatorio.resumo.totalAnalisados} — Críticos: ${pc.red(String(relatorio.resumo.criticos))} — Alertas: ${pc.yellow(String(relatorio.resumo.alertas))} — OK: ${pc.green(String(relatorio.resumo.ok))}`);

    if (relatorio.hotspots.length > 0) {
        escreverLinha("");
        escreverLinha(pc.bold("Hotspots:"));
        for (const item of relatorio.hotspots.slice(0, 10)) {
            const cor = item.severidade === "critico" ? pc.red : pc.yellow;
            escreverLinha(`  ${cor("●")} ${item.nomeArquivo} — ${item.motivos.join(", ")}`);
        }
    } else {
        escreverLinha(pc.green("Nenhum service com responsabilidades misturadas encontrado."));
    }

    if (emitirJson) {
        imprimirJson(relatorio);
    }
}

main().catch((erro) => {
    escreverLinha(pc.red(`Erro ao auditar coesão: ${erro instanceof Error ? erro.message : String(erro)}`));
    process.exit(1);
});
