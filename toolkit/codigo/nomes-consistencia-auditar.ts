
import fs from "node:fs/promises";
import path from "node:path";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../lib/execucao.js";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {executarColeta, type InventarioSimbolos} from "./nomes-simbolos-coletar.js";
import {obterCaminhoConsistencia, obterCaminhoSimbolos} from "./nomes-caminhos.js";

type FormatoNome = "camelCase" | "minusculo" | "PascalCase" | "kebab-case" | "snake_case" | "UPPER_SNAKE" | "outro";

interface TipoForaPadrao {
    arquivo: string;
    categoria: string;
    nome: string;
    formato: FormatoNome;
}

interface MembroForaPadrao {
    arquivo: string;
    categoria: string;
    nome: string;
    assinatura: string;
    formato: FormatoNome;
}

interface ParametroForaPadrao {
    arquivo: string;
    membro: string;
    parametro: string;
    formato: FormatoNome;
}

interface ParametroComId {
    arquivo: string;
    membro: string;
    parametro: string;
}

interface PacoteForaPadrao {
    pacote: string;
    partesInvalidas: string[];
    arquivos: string[];
}

interface AuditoriaNomes {
    geradoEm: string;
    base: string;
    inventarioFonte: string;
    indicadores: {
        arquivos: number;
        tiposForaPadrao: number;
        membrosForaPadrao: number;
        parametrosForaPadrao: number;
        parametrosComId: number;
        pacotesJavaForaPadrao: number;
    };
    formatosArquivos: Record<string, Record<string, string[]>>;
    formatosDiretorios: Record<string, FormatoNome>;
    tiposForaPadrao: TipoForaPadrao[];
    membrosForaPadrao: MembroForaPadrao[];
    parametrosForaPadrao: ParametroForaPadrao[];
    parametrosComId: ParametroComId[];
    pacotesJavaForaPadrao: PacoteForaPadrao[];
}

interface OpcoesAuditoriaNomes {
    base?: string;
    json?: boolean;
    gravar?: boolean;
    inventario?: string | null;
    saidaJson?: string | null;
}

function classificarFormatoNome(nome: string): FormatoNome {
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

function coletarFormatosArquivos(inventario: InventarioSimbolos): Record<string, Record<string, string[]>> {
    const porExtensao: Record<string, Record<string, string[]>> = {};
    for (const arquivo of inventario.arquivos) {
        const nomeArquivo = path.basename(arquivo.caminho);
        const extensao = path.extname(nomeArquivo) || "<sem-ext>";
        const semExtensao = nomeArquivo.slice(0, nomeArquivo.length - extensao.length);
        const formato = classificarFormatoNome(semExtensao);

        porExtensao[extensao] ??= {};
        porExtensao[extensao][formato] ??= [];
        porExtensao[extensao][formato].push(arquivo.caminho);
    }
    return porExtensao;
}

function coletarSegmentosDiretorio(inventario: InventarioSimbolos): Record<string, FormatoNome> {
    const formatos: Record<string, FormatoNome> = {};
    for (const arquivo of inventario.arquivos) {
        const segmentos = arquivo.caminho.split("/").slice(0, -1);
        for (const segmento of segmentos) {
            formatos[segmento] ??= classificarFormatoNome(segmento);
        }
    }
    return formatos;
}

function filtrarSimbolosForaPadrao(inventario: InventarioSimbolos): {
    tiposForaPadrao: TipoForaPadrao[];
    membrosForaPadrao: MembroForaPadrao[];
    parametrosForaPadrao: ParametroForaPadrao[];
    parametrosComId: ParametroComId[];
} {
    const tiposForaPadrao: TipoForaPadrao[] = [];
    const membrosForaPadrao: MembroForaPadrao[] = [];
    const parametrosForaPadrao: ParametroForaPadrao[] = [];
    const parametrosComId: ParametroComId[] = [];

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
                if (formatoParametro !== "camelCase"
                    && formatoParametro !== "minusculo"
                    && !parametro.startsWith("{")
                    && !parametro.startsWith("[")) {
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

    return {tiposForaPadrao, membrosForaPadrao, parametrosForaPadrao, parametrosComId};
}

function auditarPacotesJava(inventario: InventarioSimbolos): PacoteForaPadrao[] {
    const pacotesForaPadrao: PacoteForaPadrao[] = [];
    for (const pacote of inventario.pacotesJava) {
        const partes = pacote.nome.split(".");
        const partesInvalidas = partes.filter(parte => !/^[a-z][a-z0-9]*$/.test(parte));
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

function montarResumo(auditoria: AuditoriaNomes): string {
    const linhas: string[] = [];
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

async function executarAuditoriaNomes({
    base = DIRETORIO_RAIZ,
    json = false,
    gravar = false,
    inventario = null,
    saidaJson = null
}: OpcoesAuditoriaNomes = {}): Promise<AuditoriaNomes> {
    const baseResolvida = path.resolve(base);
    const caminhoInventario = inventario ?? obterCaminhoSimbolos(baseResolvida);
    const caminhoSaida = saidaJson ?? obterCaminhoConsistencia(baseResolvida);
    const dadosInventario = await carregarInventario(caminhoInventario, baseResolvida, gravar);
    const formatosArquivos = coletarFormatosArquivos(dadosInventario);
    const formatosDiretorios = coletarSegmentosDiretorio(dadosInventario);
    const {
        tiposForaPadrao,
        membrosForaPadrao,
        parametrosForaPadrao,
        parametrosComId
    } = filtrarSimbolosForaPadrao(dadosInventario);
    const pacotesJavaForaPadrao = auditarPacotesJava(dadosInventario);

    const auditoria: AuditoriaNomes = {
        geradoEm: new Date().toISOString(),
        base: baseResolvida,
        inventarioFonte: caminhoInventario,
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

    if (gravar) {
        const destinoJson = path.isAbsolute(caminhoSaida) ? caminhoSaida : path.resolve(baseResolvida, caminhoSaida);
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
    if (gravar) {
        const destinoJson = path.isAbsolute(caminhoSaida) ? caminhoSaida : path.resolve(baseResolvida, caminhoSaida);
        escreverLinha("");
        escreverLinha(`Auditoria salva em ${destinoJson}`);
        escreverLinha(`Resumo salvo em ${path.join(path.dirname(destinoJson), "consistencia-resumo.md")}`);
    }

    return auditoria;
}

function lerOpcoes(argv: string[]): OpcoesAuditoriaNomes {
    const opcoes: OpcoesAuditoriaNomes = {
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
        if (argumento === "--inventario") {
            opcoes.inventario = argv[indice + 1] ?? null;
            indice += 1;
            continue;
        }
        if (argumento === "--saida") {
            opcoes.saidaJson = argv[indice + 1] ?? null;
            indice += 1;
        }
    }

    return opcoes;
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const argumentosValidados = validarArgumentosEntradaDireta(import.meta.url, argumentos);
    if (argumentosValidados.includes("--help") || argumentosValidados.includes("-h")) {
        escreverLinha("Uso: npx tsx toolkit/sgc.ts codigo nomes auditar-consistencia [--json] [--gravar] [--base <diretorio>] [--inventario <arquivo.json>] [--saida <arquivo.json>]");
        escreverLinha("");
        escreverLinha("Audita consistencia de nomenclatura com base no inventario de simbolos.");
        return;
    }

    await executarAuditoriaNomes(lerOpcoes(argumentosValidados));
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro: unknown) => {
        escreverLinha(`Erro ao auditar nomenclatura: ${erro instanceof Error ? erro.message : String(erro)}`);
        process.exitCode = 1;
    });
}

export {
    executarAuditoriaNomes,
    principal
};
