#!/usr/bin/env node
import fs from "node:fs/promises";
import path from "node:path";
import {DIRETORIO_RAIZ, resolverNaRaiz} from "../lib/caminhos.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

const DIRETORIO_SAIDA_PADRAO = resolverNaRaiz("etc", "qualidade", "nomenclatura", "latest");
const ARQUIVO_JSON_PADRAO = path.join(DIRETORIO_SAIDA_PADRAO, "simbolos.json");
const ARQUIVO_MD_PADRAO = path.join(DIRETORIO_SAIDA_PADRAO, "simbolos-resumo.md");

const EXTENSOES_SUPORTADAS = new Set([".java", ".ts", ".tsx", ".js", ".jsx", ".vue"]);
const DIRETORIOS_IGNORADOS = new Set([
    ".git",
    ".idea",
    ".vscode",
    ".gradle",
    "node_modules",
    "dist",
    "build",
    "coverage",
    "playwright-report",
    "test-results",
    ".next",
    "out",
    ".turbo",
    ".cache"
]);
const PALAVRAS_CHAVE_METODO = new Set([
    "if",
    "for",
    "while",
    "switch",
    "catch",
    "return",
    "throw",
    "new",
    "typeof",
    "instanceof",
    "do",
    "else",
    "case"
]);

function normalizarSeparadores(caminhoArquivo) {
    return caminhoArquivo.split(path.sep).join("/");
}

function dividirParametros(textoParametros) {
    const parametros = [];
    let atual = "";
    let nivelGenerico = 0;
    let nivelParenteses = 0;
    let nivelColchetes = 0;
    let aspasSimples = false;
    let aspasDuplas = false;

    for (let indice = 0; indice < textoParametros.length; indice += 1) {
        const caractere = textoParametros[indice];
        const anterior = indice > 0 ? textoParametros[indice - 1] : "";

        if (caractere === "'" && anterior !== "\\" && !aspasDuplas) {
            aspasSimples = !aspasSimples;
        } else if (caractere === "\"" && anterior !== "\\" && !aspasSimples) {
            aspasDuplas = !aspasDuplas;
        }

        if (!aspasSimples && !aspasDuplas) {
            if (caractere === "<") {
                nivelGenerico += 1;
            } else if (caractere === ">") {
                nivelGenerico = Math.max(0, nivelGenerico - 1);
            } else if (caractere === "(") {
                nivelParenteses += 1;
            } else if (caractere === ")") {
                nivelParenteses = Math.max(0, nivelParenteses - 1);
            } else if (caractere === "[") {
                nivelColchetes += 1;
            } else if (caractere === "]") {
                nivelColchetes = Math.max(0, nivelColchetes - 1);
            } else if (caractere === ","
                && nivelGenerico === 0
                && nivelParenteses === 0
                && nivelColchetes === 0) {
                if (atual.trim().length > 0) {
                    parametros.push(atual.trim());
                }
                atual = "";
                continue;
            }
        }

        atual += caractere;
    }

    if (atual.trim().length > 0) {
        parametros.push(atual.trim());
    }

    return parametros;
}

function extrairNomeParametro(parametroBruto) {
    const parametro = parametroBruto.trim();
    if (parametro.length === 0) {
        return null;
    }

    if (parametro.startsWith("{") || parametro.startsWith("[")) {
        return parametro;
    }

    const semAtribuicao = parametro.split("=")[0].trim();
    const semAnotacoes = semAtribuicao
        .replace(/@\w+(?:\([^)]*\))?\s*/g, "")
        .replace(/\b(final|readonly|public|private|protected)\b/g, "")
        .trim();
    const semRest = semAnotacoes.replace(/^\.\.\./, "");

    if (semRest.includes(":")) {
        const nomeEsquerda = semRest.split(":")[0].trim();
        if (nomeEsquerda.length > 0) {
            return nomeEsquerda.replace(/\?$/, "");
        }
    }

    const tokens = semRest.split(/\s+/).filter(Boolean);
    const nome = tokens.at(-1)?.replace(/[\[\]?]+$/g, "");
    return nome ?? null;
}

function extrairParametros(textoParametros) {
    if (!textoParametros || textoParametros.trim().length === 0) {
        return [];
    }

    return dividirParametros(textoParametros)
        .map(extrairNomeParametro)
        .filter((valor) => valor !== null);
}

function detectarLinguagem(caminhoRelativo) {
    const extensao = path.extname(caminhoRelativo);
    if (extensao === ".java") {
        return "java";
    }
    if (extensao === ".vue") {
        return "vue";
    }
    if (extensao === ".ts" || extensao === ".tsx") {
        return "typescript";
    }
    return "javascript";
}

function extrairTipoAlvo(texto) {
    const tipos = [];
    const regex = /^\s*(?:export\s+)?(?:default\s+)?(?:abstract\s+)?(class|interface|enum|record|type)\s+([A-Za-z_]\w*)/gm;
    let correspondencia = regex.exec(texto);
    while (correspondencia) {
        tipos.push({
            categoria: correspondencia[1],
            nome: correspondencia[2]
        });
        correspondencia = regex.exec(texto);
    }
    return tipos;
}

function extrairFuncoesTsJs(texto) {
    const funcoes = [];
    const assinaturasRegistradas = new Set();

    const regexFunction = /^\s*(?:export\s+)?(?:default\s+)?(?:async\s+)?function\s+([A-Za-z_]\w*)\s*(?:<[^>]+>\s*)?\(([^)]*)\)\s*(?::\s*[^({=]+)?\s*\{/gm;
    let correspondencia = regexFunction.exec(texto);
    while (correspondencia) {
        const assinatura = `${correspondencia[1]}(${correspondencia[2].trim()})`;
        if (!assinaturasRegistradas.has(assinatura)) {
            assinaturasRegistradas.add(assinatura);
            funcoes.push({
                categoria: "funcao",
                nome: correspondencia[1],
                assinatura,
                parametros: extrairParametros(correspondencia[2])
            });
        }
        correspondencia = regexFunction.exec(texto);
    }

    const regexArrow = /^\s*(?:export\s+)?const\s+([A-Za-z_]\w*)\s*=\s*(?:async\s*)?(?:<[^>]+>\s*)?\(([^)]*)\)\s*=>/gm;
    correspondencia = regexArrow.exec(texto);
    while (correspondencia) {
        const assinatura = `${correspondencia[1]}(${correspondencia[2].trim()})`;
        if (!assinaturasRegistradas.has(assinatura)) {
            assinaturasRegistradas.add(assinatura);
            funcoes.push({
                categoria: "funcao-arrow",
                nome: correspondencia[1],
                assinatura,
                parametros: extrairParametros(correspondencia[2])
            });
        }
        correspondencia = regexArrow.exec(texto);
    }

    const regexMetodo = /^\s*(?:public|private|protected|static|readonly|async|get|set|\s)+([A-Za-z_]\w*)\s*\(([^)]*)\)\s*(?::\s*[^=;{]+)?\s*\{/gm;
    correspondencia = regexMetodo.exec(texto);
    while (correspondencia) {
        const nome = correspondencia[1];
        if (PALAVRAS_CHAVE_METODO.has(nome)) {
            correspondencia = regexMetodo.exec(texto);
            continue;
        }
        const assinatura = `${nome}(${correspondencia[2].trim()})`;
        if (!assinaturasRegistradas.has(assinatura)) {
            assinaturasRegistradas.add(assinatura);
            funcoes.push({
                categoria: "metodo",
                nome,
                assinatura,
                parametros: extrairParametros(correspondencia[2])
            });
        }
        correspondencia = regexMetodo.exec(texto);
    }

    return funcoes;
}

function extrairDadosJava(texto) {
    const pacote = texto.match(/^\s*package\s+([a-zA-Z0-9_.]+)\s*;/m)?.[1] ?? null;
    const tipos = [];
    const regexTipos = /^\s*(?:public|protected|private)?\s*(?:abstract\s+|final\s+|sealed\s+|non-sealed\s+)*\b(class|interface|enum|record)\s+([A-Za-z_]\w*)/gm;
    let correspondencia = regexTipos.exec(texto);
    while (correspondencia) {
        tipos.push({
            categoria: correspondencia[1],
            nome: correspondencia[2]
        });
        correspondencia = regexTipos.exec(texto);
    }

    const membros = [];
    const assinaturasRegistradas = new Set();
    const regexMetodos = /^\s*(?:@\w+(?:\([^)]*\))?\s*)*(?:(?:public|protected|private)\s+)?(?:(?:static|final|abstract|synchronized|native|strictfp|default)\s+)*(?:<[^>{}]+>\s*)?([A-Za-z_$][\w$<>\[\].?]*(?:\s+[A-Za-z_$][\w$<>\[\].?]*)*)\s+([a-z_]\w*)\s*\(([^)]*)\)\s*(?:throws [^{;]+)?\s*(?:\{|;)\s*$/gm;
    correspondencia = regexMetodos.exec(texto);
    while (correspondencia) {
        const linha = correspondencia[0];
        if (/\b(?:throw|return|new)\b/.test(linha) || linha.includes("->")) {
            correspondencia = regexMetodos.exec(texto);
            continue;
        }

        const nome = correspondencia[2];
        if (PALAVRAS_CHAVE_METODO.has(nome)) {
            correspondencia = regexMetodos.exec(texto);
            continue;
        }
        const assinatura = `${nome}(${correspondencia[3].trim()})`;
        if (!assinaturasRegistradas.has(assinatura)) {
            assinaturasRegistradas.add(assinatura);
            membros.push({
                categoria: "metodo",
                nome,
                assinatura,
                retorno: correspondencia[1].trim(),
                parametros: extrairParametros(correspondencia[3])
            });
        }
        correspondencia = regexMetodos.exec(texto);
    }

    const nomesTipos = new Set(tipos.map((tipo) => tipo.nome));
    const regexConstrutor = /^\s*(?:public|protected|private)?\s*([A-Za-z_]\w*)\s*\(([^)]*)\)\s*(?:throws [^{;]+)?\s*\{/gm;
    correspondencia = regexConstrutor.exec(texto);
    while (correspondencia) {
        const nome = correspondencia[1];
        if (!nomesTipos.has(nome)) {
            correspondencia = regexConstrutor.exec(texto);
            continue;
        }
        const assinatura = `${nome}(${correspondencia[2].trim()})`;
        if (!assinaturasRegistradas.has(assinatura)) {
            assinaturasRegistradas.add(assinatura);
            membros.push({
                categoria: "construtor",
                nome,
                assinatura,
                retorno: null,
                parametros: extrairParametros(correspondencia[2])
            });
        }
        correspondencia = regexConstrutor.exec(texto);
    }

    return {
        pacote,
        tipos,
        membros
    };
}

function extrairConteudoScriptVue(texto) {
    const scripts = [];
    const regex = /<script\b[^>]*>([\s\S]*?)<\/script>/gi;
    let correspondencia = regex.exec(texto);
    while (correspondencia) {
        scripts.push(correspondencia[1]);
        correspondencia = regex.exec(texto);
    }
    return scripts.join("\n");
}

async function listarArquivos(baseResolvida, caminhoAtual = baseResolvida, arquivos = []) {
    const entradas = await fs.readdir(caminhoAtual, {withFileTypes: true});

    for (const entrada of entradas) {
        const caminhoCompleto = path.join(caminhoAtual, entrada.name);
        if (entrada.isDirectory()) {
            if (DIRETORIOS_IGNORADOS.has(entrada.name)) {
                continue;
            }
            await listarArquivos(baseResolvida, caminhoCompleto, arquivos);
            continue;
        }

        const extensao = path.extname(entrada.name);
        if (EXTENSOES_SUPORTADAS.has(extensao)) {
            arquivos.push(caminhoCompleto);
        }
    }

    return arquivos;
}

function ordenarPorNome(colecao) {
    return [...colecao].sort((a, b) => a.localeCompare(b, "pt-BR"));
}

function montarResumoMarkdown(inventario) {
    const linhas = [];
    linhas.push("# Inventario de simbolos");
    linhas.push("");
    linhas.push(`Gerado em: ${inventario.geradoEm}`);
    linhas.push(`Base: ${inventario.base}`);
    linhas.push("");
    linhas.push("## Totais");
    linhas.push("");
    linhas.push(`- Arquivos analisados: ${inventario.totais.arquivos}`);
    linhas.push(`- Pacotes Java: ${inventario.totais.pacotesJava}`);
    linhas.push(`- Tipos (class/interface/enum/record/type): ${inventario.totais.tipos}`);
    linhas.push(`- Metodos/funcoes/construtores: ${inventario.totais.membros}`);
    linhas.push("");
    linhas.push("## Por linguagem");
    linhas.push("");
    linhas.push("| Linguagem | Arquivos | Tipos | Membros |");
    linhas.push("|---|---:|---:|---:|");

    for (const [linguagem, dados] of Object.entries(inventario.porLinguagem)) {
        linhas.push(`| ${linguagem} | ${dados.arquivos} | ${dados.tipos} | ${dados.membros} |`);
    }

    linhas.push("");
    linhas.push("## Pacotes Java");
    linhas.push("");
    for (const pacote of inventario.pacotesJava) {
        linhas.push(`- ${pacote.nome} (${pacote.totalArquivos} arquivo(s))`);
    }

    linhas.push("");
    linhas.push("## Top 20 arquivos por densidade de membros");
    linhas.push("");
    linhas.push("| Arquivo | Linguagem | Tipos | Membros | Pacote |");
    linhas.push("|---|---|---:|---:|---|");
    for (const arquivo of inventario.arquivos
        .sort((a, b) => b.membros.length - a.membros.length || b.tipos.length - a.tipos.length || a.caminho.localeCompare(b.caminho, "pt-BR"))
        .slice(0, 20)) {
        linhas.push(`| ${arquivo.caminho} | ${arquivo.linguagem} | ${arquivo.tipos.length} | ${arquivo.membros.length} | ${arquivo.pacote ?? "-"} |`);
    }

    return `${linhas.join("\n")}\n`;
}

async function executarColeta({
    base = DIRETORIO_RAIZ,
    json = false,
    semGravar = false,
    arquivoSaida = ARQUIVO_JSON_PADRAO
} = {}) {
    const baseResolvida = path.resolve(base);
    const arquivos = await listarArquivos(baseResolvida);
    const resultadoArquivos = [];
    const mapaPacotes = new Map();
    const porLinguagem = {
        java: {arquivos: 0, tipos: 0, membros: 0},
        typescript: {arquivos: 0, tipos: 0, membros: 0},
        javascript: {arquivos: 0, tipos: 0, membros: 0},
        vue: {arquivos: 0, tipos: 0, membros: 0}
    };

    for (const arquivo of arquivos) {
        const conteudoCompleto = await fs.readFile(arquivo, "utf8");
        const caminhoRelativo = normalizarSeparadores(path.relative(baseResolvida, arquivo));
        const linguagem = detectarLinguagem(caminhoRelativo);

        porLinguagem[linguagem].arquivos += 1;

        if (linguagem === "java") {
            const dadosJava = extrairDadosJava(conteudoCompleto);
            porLinguagem.java.tipos += dadosJava.tipos.length;
            porLinguagem.java.membros += dadosJava.membros.length;

            if (dadosJava.pacote) {
                if (!mapaPacotes.has(dadosJava.pacote)) {
                    mapaPacotes.set(dadosJava.pacote, []);
                }
                mapaPacotes.get(dadosJava.pacote).push(caminhoRelativo);
            }

            resultadoArquivos.push({
                caminho: caminhoRelativo,
                linguagem,
                pacote: dadosJava.pacote,
                tipos: dadosJava.tipos,
                membros: dadosJava.membros
            });
            continue;
        }

        const conteudoAnalise = linguagem === "vue" ? extrairConteudoScriptVue(conteudoCompleto) : conteudoCompleto;
        const tipos = extrairTipoAlvo(conteudoAnalise);
        const membros = extrairFuncoesTsJs(conteudoAnalise);

        porLinguagem[linguagem].tipos += tipos.length;
        porLinguagem[linguagem].membros += membros.length;

        resultadoArquivos.push({
            caminho: caminhoRelativo,
            linguagem,
            pacote: null,
            tipos,
            membros
        });
    }

    const pacotesJava = ordenarPorNome([...mapaPacotes.keys()]).map((nomePacote) => ({
        nome: nomePacote,
        totalArquivos: mapaPacotes.get(nomePacote).length,
        arquivos: ordenarPorNome(mapaPacotes.get(nomePacote))
    }));

    const inventario = {
        geradoEm: new Date().toISOString(),
        base: baseResolvida,
        totais: {
            arquivos: resultadoArquivos.length,
            pacotesJava: pacotesJava.length,
            tipos: resultadoArquivos.reduce((acumulado, arquivo) => acumulado + arquivo.tipos.length, 0),
            membros: resultadoArquivos.reduce((acumulado, arquivo) => acumulado + arquivo.membros.length, 0)
        },
        porLinguagem,
        pacotesJava,
        arquivos: resultadoArquivos.sort((a, b) => a.caminho.localeCompare(b.caminho, "pt-BR"))
    };

    if (!semGravar) {
        const destinoJson = path.isAbsolute(arquivoSaida) ? arquivoSaida : path.resolve(baseResolvida, arquivoSaida);
        const destinoMarkdown = path.join(path.dirname(destinoJson), "simbolos-resumo.md");
        await fs.mkdir(path.dirname(destinoJson), {recursive: true});
        await fs.writeFile(destinoJson, JSON.stringify(inventario, null, 2));
        await fs.writeFile(destinoMarkdown, montarResumoMarkdown(inventario));
    }

    if (json) {
        imprimirJson(inventario);
        return inventario;
    }

    imprimirCabecalho("Inventario de simbolos", `Base: ${inventario.base}`);
    escreverLinha(`Arquivos analisados: ${inventario.totais.arquivos}`);
    escreverLinha(`Pacotes Java: ${inventario.totais.pacotesJava}`);
    escreverLinha(`Tipos catalogados: ${inventario.totais.tipos}`);
    escreverLinha(`Membros catalogados: ${inventario.totais.membros}`);
    if (!semGravar) {
        const destinoJson = path.isAbsolute(arquivoSaida) ? arquivoSaida : path.resolve(baseResolvida, arquivoSaida);
        escreverLinha("");
        escreverLinha(`Inventario salvo em ${destinoJson}`);
        escreverLinha(`Resumo salvo em ${path.join(path.dirname(destinoJson), "simbolos-resumo.md")}`);
    }

    return inventario;
}

function parseArgs(argv) {
    const opcoes = {
        base: DIRETORIO_RAIZ,
        json: false,
        semGravar: false,
        arquivoSaida: ARQUIVO_JSON_PADRAO
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
        if (argumento === "--saida") {
            opcoes.arquivoSaida = argv[indice + 1] ?? ARQUIVO_JSON_PADRAO;
            indice += 1;
        }
    }

    return opcoes;
}

if (process.argv.includes("--help") || process.argv.includes("-h")) {
    escreverLinha("Uso: node etc/scripts/sgc.js codigo nomes coletar-simbolos [--json] [--sem-gravar] [--base <diretorio>] [--saida <arquivo.json>]");
    escreverLinha("");
    escreverLinha("Gera inventario completo de pacotes, arquivos, tipos e membros.");
    process.exit(0);
}

try {
    await executarColeta(parseArgs(process.argv.slice(2)));
} catch (erro) {
    escreverLinha(`Erro ao coletar simbolos: ${erro instanceof Error ? erro.message : String(erro)}`);
    process.exit(1);
}

export {executarColeta};
