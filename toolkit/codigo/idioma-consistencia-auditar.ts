#!/usr/bin/env node

import fs from "node:fs/promises";
import path from "node:path";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {executarColeta, type InventarioSimbolos} from "./nomes-simbolos-coletar.js";
import {obterCaminhoIdioma, obterCaminhoSimbolos} from "./nomes-caminhos.js";

type TipoIdioma = "nome-ingles-exato" | "prefixo-ingles";
type TipoId = "campo-id-exato" | "sufixo-Id";

interface DeteccaoIdioma {
    tipo: TipoIdioma;
    nome: string;
    prefixo?: string;
}

interface DeteccaoId {
    tipo: TipoId;
    nome: string;
}

interface MembroIngles {
    arquivo: string;
    categoria: string;
    nome: string;
    assinatura: string;
    tipo: TipoIdioma;
    prefixo?: string;
}

interface CampoComId {
    arquivo: string;
    categoria: string;
    nome: string;
    assinatura: string;
    tipo: TipoId;
}

interface ParametroComId {
    arquivo: string;
    membro: string;
    parametro: string;
    tipo: TipoId;
}

interface ArquivoIdioma {
    arquivo: string;
    quantidade: number;
    nomes: string[];
}

interface AnaliseIdioma {
    membrosIngles: MembroIngles[];
    camposComId: CampoComId[];
    parametrosComId: ParametroComId[];
    topArquivos: ArquivoIdioma[];
    porPrefixo: Record<string, number>;
}

interface AuditoriaIdioma {
    geradoEm: string;
    base: string;
    inventarioFonte: string;
    indicadores: {
        arquivos: number;
        membrosIngles: number;
        camposComId: number;
        parametrosComId: number;
        scoreTotal: number;
    };
    porPrefixo: Record<string, number>;
    topArquivos: ArquivoIdioma[];
    membrosIngles: MembroIngles[];
    camposComId: CampoComId[];
    parametrosComId: ParametroComId[];
}

interface OpcoesAuditoriaIdioma {
    base?: string;
    json?: boolean;
    gravar?: boolean;
    inventario?: string | null;
    saidaJson?: string | null;
}

const PREFIXOS_INGLES = [
    "get", "set", "clear", "has", "is", "handle", "with", "last", "fetch",
    "reset", "update", "delete", "create", "save", "load", "show", "hide",
    "open", "close", "toggle", "add", "remove", "build", "parse"
];

const REGEX_PREFIXO_INGLES = new RegExp(`^(${PREFIXOS_INGLES.join("|")})[A-Z]`);

const NOMES_INGLES_EXATOS = new Set([
    "loading", "saving", "error", "errors", "warning", "success",
    "pending", "disabled", "enabled", "visible", "hidden",
    "loading", "submitting", "fetching"
]);

const SUFIXOS_ID_FINAL = /Id$/;
const NOME_EXATO_ID = /^id$/;

function detectarIdiomaMembro(nome: string): DeteccaoIdioma | null {
    if (NOMES_INGLES_EXATOS.has(nome)) {
        return {tipo: "nome-ingles-exato", nome};
    }
    if (REGEX_PREFIXO_INGLES.test(nome)) {
        const prefixo = PREFIXOS_INGLES.find(p => nome.startsWith(p)
            && nome.length > p.length
            && nome[p.length] === nome[p.length].toUpperCase());
        return {tipo: "prefixo-ingles", nome, prefixo};
    }
    return null;
}

function detectarUsoId(nome: string): DeteccaoId | null {
    if (NOME_EXATO_ID.test(nome)) {
        return {tipo: "campo-id-exato", nome};
    }
    if (SUFIXOS_ID_FINAL.test(nome)) {
        return {tipo: "sufixo-Id", nome};
    }
    return null;
}

function analisarInventario(inventario: InventarioSimbolos): AnaliseIdioma {
    const membrosIngles: MembroIngles[] = [];
    const camposComId: CampoComId[] = [];
    const parametrosComId: ParametroComId[] = [];

    for (const arquivo of inventario.arquivos) {
        for (const membro of arquivo.membros) {
            const deteccaoIdioma = detectarIdiomaMembro(membro.nome);
            if (deteccaoIdioma) {
                membrosIngles.push({
                    arquivo: arquivo.caminho,
                    categoria: membro.categoria,
                    assinatura: membro.assinatura,
                    ...deteccaoIdioma
                });
            }

            if (membro.categoria === "campo"
                || membro.categoria === "propriedade"
                || membro.categoria === "campo-record"
                || membro.categoria === "atributo") {
                const deteccaoId = detectarUsoId(membro.nome);
                if (deteccaoId) {
                    camposComId.push({
                        arquivo: arquivo.caminho,
                        categoria: membro.categoria,
                        assinatura: membro.assinatura,
                        ...deteccaoId
                    });
                }
            }

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

    const porArquivo: Record<string, string[]> = {};
    for (const item of membrosIngles) {
        porArquivo[item.arquivo] ??= [];
        porArquivo[item.arquivo].push(item.nome);
    }

    const topArquivos: ArquivoIdioma[] = Object.entries(porArquivo)
        .map(([arquivo, nomes]) => ({arquivo, quantidade: nomes.length, nomes}))
        .toSorted((a, b) => b.quantidade - a.quantidade)
        .slice(0, 20);

    const porPrefixo: Record<string, number> = {};
    for (const item of membrosIngles) {
        const chave = item.prefixo ?? item.tipo;
        porPrefixo[chave] = (porPrefixo[chave] ?? 0) + 1;
    }

    return {membrosIngles, camposComId, parametrosComId, topArquivos, porPrefixo};
}

function montarResumo(auditoria: AuditoriaIdioma): string {
    const linhas: string[] = [];
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
    for (const [prefixo, quantidade] of Object.entries(auditoria.porPrefixo).toSorted((a, b) => b[1] - a[1])) {
        linhas.push(`| ${prefixo} | ${quantidade} |`);
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

function ehInventarioSimbolos(valor: unknown): valor is InventarioSimbolos {
    if (typeof valor !== "object" || valor === null) {
        return false;
    }
    const registro = valor as Record<string, unknown>;
    const totais = registro.totais;
    return typeof registro.base === "string"
        && Array.isArray(registro.arquivos)
        && typeof totais === "object"
        && totais !== null;
}

async function carregarInventario(caminhoInventario: string, base: string, gravar: boolean): Promise<InventarioSimbolos> {
    const caminhoAbsoluto = path.isAbsolute(caminhoInventario) ? caminhoInventario : path.resolve(base, caminhoInventario);
    try {
        const valor: unknown = JSON.parse(await fs.readFile(caminhoAbsoluto, "utf8"));
        if (!ehInventarioSimbolos(valor)) {
            throw new Error("Inventário de símbolos inválido.");
        }
        return valor;
    } catch {
        return executarColeta({
            base,
            gravar,
            arquivoSaida: caminhoAbsoluto,
            silencioso: true
        });
    }
}

async function executarAuditoriaIdioma({
    base = DIRETORIO_RAIZ,
    json = false,
    gravar = false,
    inventario = null,
    saidaJson = null
}: OpcoesAuditoriaIdioma = {}): Promise<AuditoriaIdioma> {
    const baseResolvida = path.resolve(base);
    const caminhoInventario = inventario ?? obterCaminhoSimbolos(baseResolvida);
    const caminhoSaida = saidaJson ?? obterCaminhoIdioma(baseResolvida);
    const dadosInventario = await carregarInventario(caminhoInventario, baseResolvida, gravar);
    const {membrosIngles, camposComId, parametrosComId, topArquivos, porPrefixo} = analisarInventario(dadosInventario);

    const scoreTotal = membrosIngles.length + camposComId.length + parametrosComId.length;
    const auditoria: AuditoriaIdioma = {
        geradoEm: new Date().toISOString(),
        base: baseResolvida,
        inventarioFonte: caminhoInventario,
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

    if (gravar) {
        const destinoJson = path.isAbsolute(caminhoSaida) ? caminhoSaida : path.resolve(baseResolvida, caminhoSaida);
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
    escreverLinha("Campos com id/*Id: " + auditoria.indicadores.camposComId);
    escreverLinha("Parâmetros com id/*Id: " + auditoria.indicadores.parametrosComId);
    escreverLinha(`Score total (menor = melhor): ${auditoria.indicadores.scoreTotal}`);

    if (auditoria.topArquivos.length > 0) {
        escreverLinha("");
        escreverLinha("Top arquivos com membros em inglês:");
        for (const item of auditoria.topArquivos.slice(0, 10)) {
            escreverLinha(`  ${item.arquivo}: ${item.quantidade} (${item.nomes.slice(0, 5).join(", ")}${item.nomes.length > 5 ? "..." : ""})`);
        }
    }

    if (gravar) {
        const destinoJson = path.isAbsolute(caminhoSaida) ? caminhoSaida : path.resolve(baseResolvida, caminhoSaida);
        escreverLinha("");
        escreverLinha(`Auditoria salva em ${destinoJson}`);
        escreverLinha(`Resumo salvo em ${path.join(path.dirname(destinoJson), "idioma-resumo.md")}`);
    }

    return auditoria;
}

function lerOpcoes(argv: string[]): OpcoesAuditoriaIdioma {
    const opcoes: OpcoesAuditoriaIdioma = {
        base: DIRETORIO_RAIZ,
        json: false,
        gravar: false,
        inventario: null,
        saidaJson: null
    };

    for (let indice = 0; indice < argv.length; indice += 1) {
        const argumento = argv[indice];
        if (argumento === "--json") {
            opcoes.json = true;
        } else if (argumento === "--gravar") {
            opcoes.gravar = true;
        } else if (argumento === "--base") {
            opcoes.base = argv[indice + 1] ?? DIRETORIO_RAIZ;
            indice += 1;
        } else if (argumento === "--inventario") {
            opcoes.inventario = argv[indice + 1] ?? null;
            indice += 1;
        } else if (argumento === "--saida") {
            opcoes.saidaJson = argv[indice + 1] ?? null;
            indice += 1;
        }
    }

    return opcoes;
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        escreverLinha("Uso: npx tsx toolkit/sgc.ts codigo nomes auditar-idioma [--json] [--gravar] [--base <diretorio>] [--inventario <arquivo.json>] [--saida <arquivo.json>]");
        escreverLinha("");
        escreverLinha("Detecta membros com nomes em inglês e campos com 'id' que deveriam usar 'codigo'.");
        return;
    }

    await executarAuditoriaIdioma(lerOpcoes(argumentos));
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro: unknown) => {
        escreverLinha(`Erro ao auditar idioma: ${erro instanceof Error ? erro.message : String(erro)}`);
        process.exitCode = 1;
    });
}

export {
    executarAuditoriaIdioma,
    principal
};
