#!/usr/bin/env node
/**
 * Audita inconsistências de idioma (inglês vs português) e uso de `id` vs `codigo`
 * em nomes de membros, campos e parâmetros no código-fonte do SGC.
 *
 * Uso: node etc/scripts/sgc.js codigo nomes auditar-idioma [--json] [--sem-gravar]
 */
import fs from "node:fs/promises";
import path from "node:path";
import {DIRETORIO_RAIZ, resolverNaRaiz} from "../lib/caminhos.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {executarColeta} from "./nomes-simbolos-coletar.js";

const DIRETORIO_SAIDA_PADRAO = resolverNaRaiz("etc", "qualidade", "nomenclatura", "latest");
const ARQUIVO_SIMBOLOS_PADRAO = path.join(DIRETORIO_SAIDA_PADRAO, "simbolos.json");
const ARQUIVO_JSON_AUDITORIA_PADRAO = path.join(DIRETORIO_SAIDA_PADRAO, "idioma.json");

// Prefixos/sufixos ingleses conhecidos em APIs públicas de composables/views/services
const PREFIXOS_INGLES = [
    "get", "set", "clear", "has", "is", "handle", "with", "last", "fetch",
    "reset", "update", "delete", "create", "save", "load", "show", "hide",
    "open", "close", "toggle", "add", "remove", "build", "parse"
];

// Padrão regex para detectar nomes que começam com prefixo inglês seguido de maiúscula
const REGEX_PREFIXO_INGLES = new RegExp(
    `^(${PREFIXOS_INGLES.join("|")})[A-Z]`
);

// Palavras inglesas standalone (nomes completos, não prefixos)
const NOMES_INGLES_EXATOS = new Set([
    "loading", "saving", "error", "errors", "warning", "success",
    "pending", "disabled", "enabled", "visible", "hidden",
    "loading", "submitting", "fetching"
]);

// Sufixos de identificador que deveriam ser `Codigo` no SGC
const SUFIXOS_ID_FINAL = /Id$/;
const NOME_EXATO_ID = /^id$/;

function detectarIdiomaMembro(nome) {
    if (NOMES_INGLES_EXATOS.has(nome)) {
        return {tipo: "nome-ingles-exato", nome};
    }
    if (REGEX_PREFIXO_INGLES.test(nome)) {
        const prefixo = PREFIXOS_INGLES.find(p => nome.startsWith(p) && nome.length > p.length && nome[p.length] === nome[p.length].toUpperCase());
        return {tipo: "prefixo-ingles", nome, prefixo};
    }
    return null;
}

function detectarUsoId(nome) {
    if (NOME_EXATO_ID.test(nome)) {
        return {tipo: "campo-id-exato", nome};
    }
    if (SUFIXOS_ID_FINAL.test(nome)) {
        return {tipo: "sufixo-Id", nome};
    }
    return null;
}

function analisarInventario(inventario) {
    const membrosIngles = [];
    const camposComId = [];
    const parametrosComId = [];

    for (const arquivo of inventario.arquivos) {
        for (const membro of arquivo.membros) {
            // Detectar nomes de membros em inglês
            const deteccaoIdioma = detectarIdiomaMembro(membro.nome);
            if (deteccaoIdioma) {
                membrosIngles.push({
                    arquivo: arquivo.caminho,
                    categoria: membro.categoria,
                    nome: membro.nome,
                    assinatura: membro.assinatura,
                    ...deteccaoIdioma
                });
            }

            // Detectar campos/propriedades com `id`/`*Id`
            if (membro.categoria === "campo" || membro.categoria === "propriedade" ||
                membro.categoria === "campo-record" || membro.categoria === "atributo") {
                const deteccaoId = detectarUsoId(membro.nome);
                if (deteccaoId) {
                    camposComId.push({
                        arquivo: arquivo.caminho,
                        categoria: membro.categoria,
                        nome: membro.nome,
                        assinatura: membro.assinatura,
                        ...deteccaoId
                    });
                }
            }

            // Detectar parâmetros com `id`/`*Id`
            for (const parametro of membro.parametros ?? []) {
                const nomeLimpo = parametro.replace(/^[{[\]},\s]+/, "").split(/[,\s]/)[0];
                const deteccaoId = detectarUsoId(nomeLimpo);
                if (deteccaoId) {
                    parametrosComId.push({
                        arquivo: arquivo.caminho,
                        membro: membro.assinatura,
                        parametro: nomeLimpo,
                        ...deteccaoId
                    });
                }
            }
        }
    }

    // Agrupar membros ingleses por arquivo para facilitar análise
    const porArquivo = {};
    for (const item of membrosIngles) {
        if (!porArquivo[item.arquivo]) {
            porArquivo[item.arquivo] = [];
        }
        porArquivo[item.arquivo].push(item.nome);
    }

    // Top arquivos com mais membros ingleses
    const topArquivos = Object.entries(porArquivo)
        .map(([arquivo, nomes]) => ({arquivo, quantidade: nomes.length, nomes}))
        .sort((a, b) => b.quantidade - a.quantidade)
        .slice(0, 20);

    // Distribuição por prefixo
    const porPrefixo = {};
    for (const item of membrosIngles) {
        const chave = item.prefixo ?? item.tipo;
        porPrefixo[chave] = (porPrefixo[chave] ?? 0) + 1;
    }

    return {
        membrosIngles,
        camposComId,
        parametrosComId,
        topArquivos,
        porPrefixo
    };
}

function montarResumo(auditoria) {
    const linhas = [];
    linhas.push("# Auditoria de consistência de idioma (inglês vs português)");
    linhas.push("");
    linhas.push(`Gerado em: ${auditoria.geradoEm}`);
    linhas.push(`Base: ${auditoria.base}`);
    linhas.push("");
    linhas.push("## Indicadores");
    linhas.push("");
    linhas.push(`- Membros com nome inglês: ${auditoria.indicadores.membrosIngles}`);
    linhas.push(`- Campos com \`id\`/\`*Id\`: ${auditoria.indicadores.camposComId}`);
    linhas.push(`- Parâmetros com \`id\`/\`*Id\`: ${auditoria.indicadores.parametrosComId}`);
    linhas.push(`- **Score total (menor = melhor):** ${auditoria.indicadores.scoreTotal}`);
    linhas.push("");
    linhas.push("## Distribuição por prefixo inglês");
    linhas.push("");
    linhas.push("| Prefixo/Tipo | Ocorrências |");
    linhas.push("|---|---|");
    for (const [prefixo, qtd] of Object.entries(auditoria.porPrefixo).sort((a, b) => b[1] - a[1])) {
        linhas.push(`| ${prefixo} | ${qtd} |`);
    }
    if (Object.keys(auditoria.porPrefixo).length === 0) {
        linhas.push("| (nenhum) | 0 |");
    }

    linhas.push("");
    linhas.push("## Top 20 arquivos com mais membros em inglês");
    linhas.push("");
    for (const item of auditoria.topArquivos) {
        linhas.push(`- **${item.arquivo}** (${item.quantidade}): ${item.nomes.join(", ")}`);
    }
    if (auditoria.topArquivos.length === 0) {
        linhas.push("- Nenhum encontrado ✅");
    }

    linhas.push("");
    linhas.push("## Campos com `id`/`*Id` (deveriam usar `codigo`)");
    linhas.push("");
    for (const item of auditoria.camposComId.slice(0, 30)) {
        linhas.push(`- \`${item.nome}\` (${item.categoria}) em ${item.arquivo}`);
    }
    if (auditoria.camposComId.length === 0) {
        linhas.push("- Nenhum encontrado ✅");
    }
    if (auditoria.camposComId.length > 30) {
        linhas.push(`- ... e mais ${auditoria.camposComId.length - 30}`);
    }

    return `${linhas.join("\n")}\n`;
}

async function carregarInventario(caminhoInventario, base) {
    const caminhoAbsoluto = path.isAbsolute(caminhoInventario) ? caminhoInventario : path.resolve(base, caminhoInventario);
    try {
        return JSON.parse(await fs.readFile(caminhoAbsoluto, "utf8"));
    } catch {
        return await executarColeta({
            base,
            semGravar: false,
            arquivoSaida: caminhoAbsoluto
        });
    }
}

async function executarAuditoriaIdioma({
    base = DIRETORIO_RAIZ,
    json = false,
    semGravar = false,
    inventario = ARQUIVO_SIMBOLOS_PADRAO,
    saidaJson = ARQUIVO_JSON_AUDITORIA_PADRAO
} = {}) {
    const baseResolvida = path.resolve(base);
    const dadosInventario = await carregarInventario(inventario, baseResolvida);
    const {membrosIngles, camposComId, parametrosComId, topArquivos, porPrefixo} = analisarInventario(dadosInventario);

    const scoreTotal = membrosIngles.length + camposComId.length + parametrosComId.length;

    const auditoria = {
        geradoEm: new Date().toISOString(),
        base: baseResolvida,
        inventarioFonte: inventario,
        indicadores: {
            arquivos: dadosInventario.totais.arquivos,
            membrosIngles: membrosIngles.length,
            camposComId: camposComId.length,
            parametrosComId: parametrosComId.length,
            scoreTotal
        },
        porPrefixo,
        topArquivos,
        membrosIngles,
        camposComId,
        parametrosComId
    };

    if (!semGravar) {
        const destinoJson = path.isAbsolute(saidaJson) ? saidaJson : path.resolve(baseResolvida, saidaJson);
        const destinoMarkdown = path.join(path.dirname(destinoJson), "idioma-resumo.md");
        await fs.mkdir(path.dirname(destinoJson), {recursive: true});
        await fs.writeFile(destinoJson, JSON.stringify(auditoria, null, 2));
        await fs.writeFile(destinoMarkdown, montarResumo(auditoria));
    }

    if (json) {
        imprimirJson(auditoria);
        return auditoria;
    }

    imprimirCabecalho("Auditoria de idioma (inglês vs português)", `Base: ${auditoria.base}`);
    escreverLinha(`Arquivos analisados: ${auditoria.indicadores.arquivos}`);
    escreverLinha(`Membros com nome inglês: ${auditoria.indicadores.membrosIngles}`);
    escreverLinha(`Campos com id/*Id: ${auditoria.indicadores.camposComId}`);
    escreverLinha(`Parâmetros com id/*Id: ${auditoria.indicadores.parametrosComId}`);
    escreverLinha(`Score total (menor = melhor): ${auditoria.indicadores.scoreTotal}`);

    if (auditoria.topArquivos.length > 0) {
        escreverLinha("");
        escreverLinha("Top arquivos com membros em inglês:");
        for (const item of auditoria.topArquivos.slice(0, 10)) {
            escreverLinha(`  ${item.arquivo}: ${item.quantidade} (${item.nomes.slice(0, 5).join(", ")}${item.nomes.length > 5 ? "..." : ""})`);
        }
    }

    if (!semGravar) {
        const destinoJson = path.isAbsolute(saidaJson) ? saidaJson : path.resolve(baseResolvida, saidaJson);
        escreverLinha("");
        escreverLinha(`Auditoria salva em ${destinoJson}`);
        escreverLinha(`Resumo salvo em ${path.join(path.dirname(destinoJson), "idioma-resumo.md")}`);
    }

    return auditoria;
}

function parseArgs(argv) {
    const opcoes = {
        base: DIRETORIO_RAIZ,
        json: false,
        semGravar: false,
        inventario: ARQUIVO_SIMBOLOS_PADRAO,
        saidaJson: ARQUIVO_JSON_AUDITORIA_PADRAO
    };

    for (let indice = 0; indice < argv.length; indice += 1) {
        const argumento = argv[indice];
        if (argumento === "--json") {
            opcoes.json = true;
        } else if (argumento === "--sem-gravar") {
            opcoes.semGravar = true;
        } else if (argumento === "--base") {
            opcoes.base = argv[indice + 1] ?? DIRETORIO_RAIZ;
            indice += 1;
        } else if (argumento === "--inventario") {
            opcoes.inventario = argv[indice + 1] ?? ARQUIVO_SIMBOLOS_PADRAO;
            indice += 1;
        } else if (argumento === "--saida") {
            opcoes.saidaJson = argv[indice + 1] ?? ARQUIVO_JSON_AUDITORIA_PADRAO;
            indice += 1;
        }
    }

    return opcoes;
}

if (process.argv.includes("--help") || process.argv.includes("-h")) {
    escreverLinha("Uso: node etc/scripts/sgc.js codigo nomes auditar-idioma [--json] [--sem-gravar] [--base <diretorio>] [--inventario <arquivo.json>] [--saida <arquivo.json>]");
    escreverLinha("");
    escreverLinha("Detecta membros com nomes em inglês e campos com 'id' que deveriam usar 'codigo'.");
    process.exit(0);
}

try {
    await executarAuditoriaIdioma(parseArgs(process.argv.slice(2)));
} catch (erro) {
    escreverLinha(`Erro ao auditar idioma: ${erro instanceof Error ? erro.message : String(erro)}`);
    process.exit(1);
}

export {executarAuditoriaIdioma};
