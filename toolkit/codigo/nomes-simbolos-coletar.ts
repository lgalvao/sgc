
import fs from "node:fs/promises";
import path from "node:path";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../biblioteca/saida.js";
import {obterCaminhoSimbolos} from "./nomes-caminhos.js";
import {VERSAO_INVENTARIO_SIMBOLOS} from "./nomes-contrato.js";

type Linguagem = "java" | "typescript" | "javascript" | "vue";
type CategoriaTipo = "class" | "interface" | "enum" | "record" | "type";
type CategoriaMembro =
    | "funcao"
    | "funcao-arrow"
    | "metodo"
    | "construtor"
    | "campo"
    | "propriedade"
    | "campo-record"
    | "atributo";

interface TipoSimbolo {
    categoria: CategoriaTipo;
    nome: string;
}

interface MembroSimbolo {
    categoria: CategoriaMembro;
    nome: string;
    assinatura: string;
    parametros: string[];
    retorno?: string | null;
}

export interface ArquivoSimbolos {
    caminho: string;
    linguagem: Linguagem;
    pacote: string | null;
    tipos: TipoSimbolo[];
    membros: MembroSimbolo[];
}

interface EstatisticasLinguagem {
    arquivos: number;
    tipos: number;
    membros: number;
}

type EstatisticasPorLinguagem = Record<Linguagem, EstatisticasLinguagem>;

interface PacoteJava {
    nome: string;
    totalArquivos: number;
    arquivos: string[];
}

export interface InventarioSimbolos {
    versao: typeof VERSAO_INVENTARIO_SIMBOLOS;
    geradoEm: string;
    base: string;
    totais: {
        arquivos: number;
        pacotesJava: number;
        tipos: number;
        membros: number;
    };
    porLinguagem: EstatisticasPorLinguagem;
    pacotesJava: PacoteJava[];
    arquivos: ArquivoSimbolos[];
}

interface OpcoesColeta {
    base?: string;
    json?: boolean;
    gravar?: boolean;
    arquivoSaida?: string | null;
    silencioso?: boolean;
}

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

function normalizarSeparadores(caminhoArquivo: string): string {
    return caminhoArquivo.split(path.sep).join("/");
}

function dividirParametros(textoParametros: string): string[] {
    const parametros: string[] = [];
    let atual = "";
    let nivelGenerico = 0;
    let nivelParenteses = 0;
    let nivelColchetes = 0;
    let nivelChaves = 0;
    let aspasSimples = false;
    let aspasDuplas = false;

    for (let indice = 0; indice < textoParametros.length; indice += 1) {
        const caractere = textoParametros[indice];
        const anterior = indice > 0 ? textoParametros[indice - 1] : "";

        if (caractere === "'" && anterior !== "\\" && !aspasDuplas) {
            aspasSimples = !aspasSimples;
        } else if (caractere === '"' && anterior !== "\\" && !aspasSimples) {
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
            } else if (caractere === "{") {
                nivelChaves += 1;
            } else if (caractere === "}") {
                nivelChaves = Math.max(0, nivelChaves - 1);
            } else if (caractere === ","
                && nivelGenerico === 0
                && nivelParenteses === 0
                && nivelColchetes === 0
                && nivelChaves === 0) {
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

function extrairNomeParametro(parametroBruto: string): string | null {
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
    const ultimoToken = tokens.at(-1);
    if (!ultimoToken) {
        return null;
    }

    return ultimoToken
        .replace(/\?+$/g, "")
        .replace(/\]+$/g, "")
        .replace(/\[+$/g, "");
}

function extrairParametros(textoParametros: string): string[] {
    if (!textoParametros || textoParametros.trim().length === 0) {
        return [];
    }

    return dividirParametros(textoParametros)
        .map(extrairNomeParametro)
        .filter((valor): valor is string => valor !== null);
}

function detectarLinguagem(caminhoRelativo: string): Linguagem {
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

function extrairTipoAlvo(texto: string): TipoSimbolo[] {
    const tipos: TipoSimbolo[] = [];
    const regex = /^\s*(?:export\s+)?(?:default\s+)?(?:abstract\s+)?(class|interface|enum|record|type)\s+([A-Za-z_]\w*)/gm;
    let correspondencia = regex.exec(texto);
    while (correspondencia) {
        tipos.push({
            categoria: correspondencia[1] as CategoriaTipo,
            nome: correspondencia[2]
        });
        correspondencia = regex.exec(texto);
    }
    return tipos;
}

function extrairFuncoesTsJs(texto: string): MembroSimbolo[] {
    const funcoes: MembroSimbolo[] = [];
    const assinaturasRegistradas = new Set<string>();

    const regexFunction = /^\s*(?:export\s+)?(?:default\s+)?(?:async\s+)?function\s+([A-Za-z_]\w*)\s*(?:<[^>]+>\s*)?\(([^)]*)\)\s*(?::\s*[^({=]+)?\s*\{/gm;
    let correspondencia = regexFunction.exec(texto);
    while (correspondencia) {
        if (correspondencia[2].trim().startsWith("(")) {
            correspondencia = regexFunction.exec(texto);
            continue;
        }
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
        if (correspondencia[2].trim().startsWith("(")) {
            correspondencia = regexArrow.exec(texto);
            continue;
        }
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

    const regexMetodo = /^\s*(?:(?:public|private|protected|static|readonly|async|get|set)\s+)*([A-Za-z_]\w*)\s*\(([^)]*)\)\s*(?::\s*[^=;{]+)?\s*\{/gm;
    correspondencia = regexMetodo.exec(texto);
    while (correspondencia) {
        const nome = correspondencia[1];
        if (PALAVRAS_CHAVE_METODO.has(nome)
            || correspondencia[2].trim().startsWith("(")
            || correspondencia[0].includes("=>")) {
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

function extrairDadosJava(texto: string): {pacote: string | null; tipos: TipoSimbolo[]; membros: MembroSimbolo[]} {
    const pacote = texto.match(/^\s*package\s+([a-zA-Z0-9_.]+)\s*;/m)?.[1] ?? null;
    const tipos: TipoSimbolo[] = [];
    const regexTipos = /^\s*(?:public|protected|private)?\s*(?:abstract\s+|final\s+|sealed\s+|non-sealed\s+)*\b(class|interface|enum|record)\s+([A-Za-z_]\w*)/gm;
    let correspondencia = regexTipos.exec(texto);
    while (correspondencia) {
        tipos.push({
            categoria: correspondencia[1] as CategoriaTipo,
            nome: correspondencia[2]
        });
        correspondencia = regexTipos.exec(texto);
    }

    const membros: MembroSimbolo[] = [];
    const assinaturasRegistradas = new Set<string>();
    const regexMetodos = /^\s*(?:@\w+(?:\([^)]*\))?\s*)*(?:(?:public|protected|private)\s+)?(?:(?:static|final|abstract|synchronized|native|strictfp|default)\s+)*(?:<[^>{}]+>\s*)?([A-Za-z_$][\w$<>[\].?]*(?:\s+[A-Za-z_$][\w$<>[\].?]*)*)\s+([a-z_]\w*)\s*\(([^)]*)\)\s*(?:throws [^{;]+)?\s*(?:\{|;)\s*$/gm;
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

    const nomesTipos = new Set(tipos.map(tipo => tipo.nome));
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

    return {pacote, tipos, membros};
}

function extrairConteudoScriptVue(texto: string): string {
    const scripts: string[] = [];
    const regex = /<script\b[^>]*>([\s\S]*?)<\/script>/gi;
    let correspondencia = regex.exec(texto);
    while (correspondencia) {
        scripts.push(correspondencia[1]);
        correspondencia = regex.exec(texto);
    }
    return scripts.join("\n");
}

async function listarArquivos(
    baseResolvida: string,
    caminhoAtual: string = baseResolvida,
    arquivos: string[] = []
): Promise<string[]> {
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

function ordenarPorNome(colecao: string[]): string[] {
    return [...colecao].toSorted((a, b) => a.localeCompare(b, "pt-BR"));
}

function montarResumoMarkdown(inventario: InventarioSimbolos): string {
    const linhas: string[] = [];
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
        .toSorted((a, b) => b.membros.length - a.membros.length || b.tipos.length - a.tipos.length || a.caminho.localeCompare(b.caminho, "pt-BR"))
        .slice(0, 20)) {
        linhas.push(`| ${arquivo.caminho} | ${arquivo.linguagem} | ${arquivo.tipos.length} | ${arquivo.membros.length} | ${arquivo.pacote ?? "-"} |`);
    }

    return `${linhas.join("\n")}\n`;
}

async function executarColeta({
    base = DIRETORIO_RAIZ,
    json = false,
    gravar = false,
    arquivoSaida = null,
    silencioso = false
}: OpcoesColeta = {}): Promise<InventarioSimbolos> {
    const baseResolvida = path.resolve(base);
    const caminhoSaida = arquivoSaida ?? obterCaminhoSimbolos(baseResolvida);
    const arquivos = await listarArquivos(baseResolvida);
    const resultadoArquivos: ArquivoSimbolos[] = [];
    const mapaPacotes = new Map<string, string[]>();
    const porLinguagem: EstatisticasPorLinguagem = {
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
                const arquivosPacote = mapaPacotes.get(dadosJava.pacote) ?? [];
                arquivosPacote.push(caminhoRelativo);
                mapaPacotes.set(dadosJava.pacote, arquivosPacote);
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

    const pacotesJava: PacoteJava[] = ordenarPorNome([...mapaPacotes.keys()]).map(nomePacote => {
        const arquivosPacote = mapaPacotes.get(nomePacote) ?? [];
        return {
            nome: nomePacote,
            totalArquivos: arquivosPacote.length,
            arquivos: ordenarPorNome(arquivosPacote)
        };
    });

    const inventario: InventarioSimbolos = {
        versao: VERSAO_INVENTARIO_SIMBOLOS,
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
        arquivos: resultadoArquivos.toSorted((a, b) => a.caminho.localeCompare(b.caminho, "pt-BR"))
    };

    if (gravar) {
        const destinoJson = path.isAbsolute(caminhoSaida) ? caminhoSaida : path.resolve(baseResolvida, caminhoSaida);
        const destinoMarkdown = path.join(path.dirname(destinoJson), "simbolos-resumo.md");
        await fs.mkdir(path.dirname(destinoJson), {recursive: true});
        await fs.writeFile(destinoJson, JSON.stringify(inventario, null, 2));
        await fs.writeFile(destinoMarkdown, montarResumoMarkdown(inventario));
    }

    if (json) {
        if (!silencioso) {
            imprimirJson(inventario);
        }
        return inventario;
    }

    if (!silencioso) {
        imprimirCabecalho("Inventario de simbolos", `Base: ${inventario.base}`);
        escreverLinha(`Arquivos analisados: ${inventario.totais.arquivos}`);
        escreverLinha(`Pacotes Java: ${inventario.totais.pacotesJava}`);
        escreverLinha(`Tipos catalogados: ${inventario.totais.tipos}`);
        escreverLinha(`Membros catalogados: ${inventario.totais.membros}`);
        if (gravar) {
            const destinoJson = path.isAbsolute(caminhoSaida) ? caminhoSaida : path.resolve(baseResolvida, caminhoSaida);
            escreverLinha("");
            escreverLinha(`Inventario salvo em ${destinoJson}`);
            escreverLinha(`Resumo salvo em ${path.join(path.dirname(destinoJson), "simbolos-resumo.md")}`);
        }
    }

    return inventario;
}

function ehInventarioSimbolos(valor: unknown): valor is InventarioSimbolos {
    if (typeof valor !== "object" || valor === null) {
        return false;
    }
    const registro = valor as Record<string, unknown>;
    const totais = registro.totais;
    return registro.versao === VERSAO_INVENTARIO_SIMBOLOS
        && typeof registro.geradoEm === "string"
        && typeof registro.base === "string"
        && typeof totais === "object"
        && totais !== null
        && typeof registro.porLinguagem === "object"
        && registro.porLinguagem !== null
        && Array.isArray(registro.pacotesJava)
        && Array.isArray(registro.arquivos);
}

async function carregarInventario(caminhoInventario: string, base: string, gravar: boolean): Promise<InventarioSimbolos> {
    const caminhoAbsoluto = path.isAbsolute(caminhoInventario) ? caminhoInventario : path.resolve(base, caminhoInventario);
    try {
        const valor: unknown = JSON.parse(await fs.readFile(caminhoAbsoluto, "utf8"));
        if (!ehInventarioSimbolos(valor)) {
            throw new Error(`Inventario de simbolos invalido ou com versao incompativel: esperado ${VERSAO_INVENTARIO_SIMBOLOS}.`);
        }
        return valor;
    } catch (erro) {
        if (typeof erro === "object" && erro !== null && "code" in erro && erro.code === "ENOENT") {
            return executarColeta({
                base,
                gravar,
                arquivoSaida: caminhoAbsoluto,
                silencioso: true
            });
        }
        throw erro;
    }
}

function lerOpcoes(argv: string[]): OpcoesColeta {
    const opcoes: Required<OpcoesColeta> = {
        base: DIRETORIO_RAIZ,
        json: false,
        gravar: false,
        arquivoSaida: null,
        silencioso: false
    };

    for (let indice = 0; indice < argv.length; indice += 1) {
        const argumento = argv[indice];
        if (argumento === "--json") {
            opcoes.json = true;
            continue;
        }
        if (argumento === "--gravar") {
            opcoes.gravar = true;
            continue;
        }
        if (argumento === "--base") {
            opcoes.base = argv[indice + 1] ?? DIRETORIO_RAIZ;
            indice += 1;
            continue;
        }
        if (argumento === "--saida") {
            opcoes.arquivoSaida = argv[indice + 1] ?? null;
            indice += 1;
        }
    }

    return opcoes;
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const argumentosValidados = validarArgumentosEntradaDireta(import.meta.url, argumentos);
    if (argumentosValidados.includes("--help") || argumentosValidados.includes("-h")) {
        escreverLinha("Uso: npx tsx toolkit/ferramentas.ts codigo nomes coletar-simbolos [--json] [--gravar] [--base <diretorio>] [--saida <arquivo.json>]");
        escreverLinha("");
        escreverLinha("Gera inventario completo de pacotes, arquivos, tipos e membros.");
        return;
    }

    await executarColeta(lerOpcoes(argumentosValidados));
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro: unknown) => {
        escreverLinha(`Erro ao coletar simbolos: ${erro instanceof Error ? erro.message : String(erro)}`);
        process.exitCode = 1;
    });
}

export {
    carregarInventario,
    executarColeta,
    principal
};
