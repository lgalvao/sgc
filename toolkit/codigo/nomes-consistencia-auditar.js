#!/usr/bin/env node
import fs from "node:fs/promises";
import path from "node:path";
import {DIRETORIO_RAIZ, resolverNaRaiz} from "../lib/caminhos.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {executarColeta} from "./nomes-simbolos-coletar.js";

const DIRETORIO_SAIDA_PADRAO = resolverNaRaiz("etc", "qualidade", "nomenclatura", "latest");
const ARQUIVO_SIMBOLOS_PADRAO = path.join(DIRETORIO_SAIDA_PADRAO, "simbolos.json");
const ARQUIVO_JSON_AUDITORIA_PADRAO = path.join(DIRETORIO_SAIDA_PADRAO, "consistencia.json");

function classificarFormatoNome(nome) {
    if (/^[a-z][a-zA-Z0-9]*$/.test(nome)) {
        if (/[A-Z]/.test(nome)) {
            return "camelCase";
        }
        return "minusculo";
    }
    if (/^[A-Z][a-zA-Z0-9]*$/.test(nome)) {
        return "PascalCase";
    }
    if (/^[a-z0-9]+(?:-[a-z0-9]+)+$/.test(nome)) {
        return "kebab-case";
    }
    if (/^[a-z0-9]+(?:_[a-z0-9]+)+$/.test(nome)) {
        return "snake_case";
    }
    if (/^[A-Z0-9]+(?:_[A-Z0-9]+)+$/.test(nome)) {
        return "UPPER_SNAKE";
    }
    return "outro";
}

function coletarFormatosArquivos(inventario) {
    const porExtensao = {};
    for (const arquivo of inventario.arquivos) {
        const nomeArquivo = path.basename(arquivo.caminho);
        const extensao = path.extname(nomeArquivo) || "<sem-ext>";
        const semExtensao = nomeArquivo.slice(0, nomeArquivo.length - extensao.length);
        const formato = classificarFormatoNome(semExtensao);

        if (!porExtensao[extensao]) {
            porExtensao[extensao] = {};
        }
        if (!porExtensao[extensao][formato]) {
            porExtensao[extensao][formato] = [];
        }
        porExtensao[extensao][formato].push(arquivo.caminho);
    }
    return porExtensao;
}

function coletarSegmentosDiretorio(inventario) {
    const formatos = {};
    for (const arquivo of inventario.arquivos) {
        const segmentos = arquivo.caminho.split("/").slice(0, -1);
        for (const segmento of segmentos) {
            if (!formatos[segmento]) {
                formatos[segmento] = classificarFormatoNome(segmento);
            }
        }
    }
    return formatos;
}

function filtrarSimbolosForaPadrao(inventario) {
    const tiposForaPadrao = [];
    const membrosForaPadrao = [];
    const parametrosForaPadrao = [];
    const parametrosComId = [];

    for (const arquivo of inventario.arquivos) {
        for (const tipo of arquivo.tipos) {
            const formato = classificarFormatoNome(tipo.nome);
            if (formato !== "PascalCase") {
                tiposForaPadrao.push({
                    arquivo: arquivo.caminho,
                    categoria: tipo.categoria,
                    nome: tipo.nome,
                    formato
                });
            }
        }

        for (const membro of arquivo.membros) {
            if (membro.categoria === "construtor") {
                continue;
            }
            const formato = classificarFormatoNome(membro.nome);
            if (formato !== "camelCase" && formato !== "minusculo") {
                membrosForaPadrao.push({
                    arquivo: arquivo.caminho,
                    categoria: membro.categoria,
                    nome: membro.nome,
                    assinatura: membro.assinatura,
                    formato
                });
            }

            for (const parametro of membro.parametros ?? []) {
                const formatoParametro = classificarFormatoNome(parametro);
                if (formatoParametro !== "camelCase" && formatoParametro !== "minusculo" && !parametro.startsWith("{") && !parametro.startsWith("[")) {
                    parametrosForaPadrao.push({
                        arquivo: arquivo.caminho,
                        membro: membro.assinatura,
                        parametro,
                        formato: formatoParametro
                    });
                }

                if (parametro === "id" || parametro.endsWith("Id") || parametro.endsWith("_id")) {
                    parametrosComId.push({
                        arquivo: arquivo.caminho,
                        membro: membro.assinatura,
                        parametro
                    });
                }
            }
        }
    }

    return {
        tiposForaPadrao,
        membrosForaPadrao,
        parametrosForaPadrao,
        parametrosComId
    };
}

function auditarPacotesJava(inventario) {
    const pacotesForaPadrao = [];
    for (const pacote of inventario.pacotesJava) {
        const partes = pacote.nome.split(".");
        const partesInvalidas = partes.filter((parte) => !/^[a-z][a-z0-9]*$/.test(parte));
        if (partesInvalidas.length > 0) {
            pacotesForaPadrao.push({
                pacote: pacote.nome,
                partesInvalidas,
                arquivos: pacote.arquivos
            });
        }
    }
    return pacotesForaPadrao;
}

function montarResumo(auditoria) {
    const linhas = [];
    linhas.push("# Auditoria de consistencia de nomenclatura");
    linhas.push("");
    linhas.push(`Gerado em: ${auditoria.geradoEm}`);
    linhas.push(`Base: ${auditoria.base}`);
    linhas.push("");
    linhas.push("## Indicadores");
    linhas.push("");
    linhas.push(`- Arquivos analisados: ${auditoria.indicadores.arquivos}`);
    linhas.push(`- Tipos fora do padrao PascalCase: ${auditoria.indicadores.tiposForaPadrao}`);
    linhas.push(`- Membros fora do padrao camelCase: ${auditoria.indicadores.membrosForaPadrao}`);
    linhas.push(`- Parametros fora do padrao camelCase: ${auditoria.indicadores.parametrosForaPadrao}`);
    linhas.push(`- Parametros com uso de 'id': ${auditoria.indicadores.parametrosComId}`);
    linhas.push(`- Pacotes Java fora de lowercase.dotted: ${auditoria.indicadores.pacotesJavaForaPadrao}`);
    linhas.push("");
    linhas.push("## Formatos de arquivos por extensao");
    linhas.push("");
    linhas.push("| Extensao | Formatos encontrados |");
    linhas.push("|---|---|");
    for (const [extensao, formatos] of Object.entries(auditoria.formatosArquivos)) {
        const resumo = Object.entries(formatos)
            .map(([formato, arquivos]) => `${formato}: ${arquivos.length}`)
            .join(", ");
        linhas.push(`| ${extensao} | ${resumo} |`);
    }

    linhas.push("");
    linhas.push("## Exemplos de divergencias");
    linhas.push("");
    linhas.push("### Tipos fora de PascalCase");
    for (const item of auditoria.tiposForaPadrao.slice(0, 20)) {
        linhas.push(`- ${item.nome} (${item.formato}) em ${item.arquivo}`);
    }
    if (auditoria.tiposForaPadrao.length === 0) {
        linhas.push("- Nenhum encontrado");
    }

    linhas.push("");
    linhas.push("### Membros fora de camelCase");
    for (const item of auditoria.membrosForaPadrao.slice(0, 20)) {
        linhas.push(`- ${item.assinatura} (${item.formato}) em ${item.arquivo}`);
    }
    if (auditoria.membrosForaPadrao.length === 0) {
        linhas.push("- Nenhum encontrado");
    }

    linhas.push("");
    linhas.push("### Parametros com `id`");
    for (const item of auditoria.parametrosComId.slice(0, 20)) {
        linhas.push(`- ${item.parametro} em ${item.membro} (${item.arquivo})`);
    }
    if (auditoria.parametrosComId.length === 0) {
        linhas.push("- Nenhum encontrado");
    }

    linhas.push("");
    linhas.push("### Pacotes Java fora do padrao");
    for (const item of auditoria.pacotesJavaForaPadrao.slice(0, 20)) {
        linhas.push(`- ${item.pacote} (partes invalidas: ${item.partesInvalidas.join(", ")})`);
    }
    if (auditoria.pacotesJavaForaPadrao.length === 0) {
        linhas.push("- Nenhum encontrado");
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

async function executarAuditoriaNomes({
    base = DIRETORIO_RAIZ,
    json = false,
    semGravar = false,
    inventario = ARQUIVO_SIMBOLOS_PADRAO,
    saidaJson = ARQUIVO_JSON_AUDITORIA_PADRAO
} = {}) {
    const baseResolvida = path.resolve(base);
    const dadosInventario = await carregarInventario(inventario, baseResolvida);
    const formatosArquivos = coletarFormatosArquivos(dadosInventario);
    const formatosDiretorios = coletarSegmentosDiretorio(dadosInventario);
    const {tiposForaPadrao, membrosForaPadrao, parametrosForaPadrao, parametrosComId} = filtrarSimbolosForaPadrao(dadosInventario);
    const pacotesJavaForaPadrao = auditarPacotesJava(dadosInventario);

    const auditoria = {
        geradoEm: new Date().toISOString(),
        base: baseResolvida,
        inventarioFonte: inventario,
        indicadores: {
            arquivos: dadosInventario.totais.arquivos,
            tiposForaPadrao: tiposForaPadrao.length,
            membrosForaPadrao: membrosForaPadrao.length,
            parametrosForaPadrao: parametrosForaPadrao.length,
            parametrosComId: parametrosComId.length,
            pacotesJavaForaPadrao: pacotesJavaForaPadrao.length
        },
        formatosArquivos,
        formatosDiretorios,
        tiposForaPadrao,
        membrosForaPadrao,
        parametrosForaPadrao,
        parametrosComId,
        pacotesJavaForaPadrao
    };

    if (!semGravar) {
        const destinoJson = path.isAbsolute(saidaJson) ? saidaJson : path.resolve(baseResolvida, saidaJson);
        const destinoMarkdown = path.join(path.dirname(destinoJson), "consistencia-resumo.md");
        await fs.mkdir(path.dirname(destinoJson), {recursive: true});
        await fs.writeFile(destinoJson, JSON.stringify(auditoria, null, 2));
        await fs.writeFile(destinoMarkdown, montarResumo(auditoria));
    }

    if (json) {
        imprimirJson(auditoria);
        return auditoria;
    }

    imprimirCabecalho("Auditoria de nomenclatura", `Base: ${auditoria.base}`);
    escreverLinha(`Arquivos analisados: ${auditoria.indicadores.arquivos}`);
    escreverLinha(`Tipos fora de PascalCase: ${auditoria.indicadores.tiposForaPadrao}`);
    escreverLinha(`Membros fora de camelCase: ${auditoria.indicadores.membrosForaPadrao}`);
    escreverLinha(`Parametros fora de camelCase: ${auditoria.indicadores.parametrosForaPadrao}`);
    escreverLinha(`Parametros com 'id': ${auditoria.indicadores.parametrosComId}`);
    escreverLinha(`Pacotes Java fora de lowercase.dotted: ${auditoria.indicadores.pacotesJavaForaPadrao}`);
    if (!semGravar) {
        const destinoJson = path.isAbsolute(saidaJson) ? saidaJson : path.resolve(baseResolvida, saidaJson);
        escreverLinha("");
        escreverLinha(`Auditoria salva em ${destinoJson}`);
        escreverLinha(`Resumo salvo em ${path.join(path.dirname(destinoJson), "consistencia-resumo.md")}`);
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
            continue;
        }
        if (argumento === "--sem-gravar") {
            opcoes.semGravar = true;
            continue;
        }
        if (argumento === "--base") {
            opcoes.base = argv[indice + 1] ?? DIRETORIO_RAIZ;
            indice += 1;
            continue;
        }
        if (argumento === "--inventario") {
            opcoes.inventario = argv[indice + 1] ?? ARQUIVO_SIMBOLOS_PADRAO;
            indice += 1;
            continue;
        }
        if (argumento === "--saida") {
            opcoes.saidaJson = argv[indice + 1] ?? ARQUIVO_JSON_AUDITORIA_PADRAO;
            indice += 1;
        }
    }

    return opcoes;
}

if (process.argv.includes("--help") || process.argv.includes("-h")) {
    escreverLinha("Uso: node toolkit/sgc.js codigo nomes auditar-consistencia [--json] [--sem-gravar] [--base <diretorio>] [--inventario <arquivo.json>] [--saida <arquivo.json>]");
    escreverLinha("");
    escreverLinha("Audita consistencia de nomenclatura com base no inventario de simbolos.");
    process.exit(0);
}

try {
    await executarAuditoriaNomes(parseArgs(process.argv.slice(2)));
} catch (erro) {
    escreverLinha(`Erro ao auditar nomenclatura: ${erro instanceof Error ? erro.message : String(erro)}`);
    process.exit(1);
}

export {executarAuditoriaNomes};
