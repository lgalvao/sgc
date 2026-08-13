// Auditoria de contratos HTTP expostos pelo servidor.

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

const TIPOS_ESCALARES = new Set([
    "Void",
    "void",
    "String",
    "Long",
    "Integer",
    "Boolean",
    "Double",
    "Float",
    "Byte",
    "Short",
    "Character",
    "Object",
    "SseEmitter"
]);

type CategoriaModelo = "entidade" | "enum" | "modelo";

interface Importacoes {
    exatos: Map<string, string>;
    curingas: string[];
}

interface TipoRetorno {
    metodo: string;
    tipo: string;
}

interface CampoExposto {
    tipo: string;
    nome: string;
}

interface IndiceArquivos {
    arquivos: string[];
    porNome: Map<string, string[]>;
}

interface TipoModelo {
    fqn: string;
    arquivo: string;
    categoria: CategoriaModelo;
}

interface AchadoContrato {
    controlador: string;
    metodo: string;
    tipoRetorno: string;
    arquivoDto: string;
    campo: string;
    tipoCampo: string;
    categoria: CategoriaModelo;
    tipoModelo: string;
}

interface RelatorioContratos {
    geradoEm: string;
    base: string;
    resumo: {
        totalAchados: number;
        controladoresAfetados: number;
        dtosAfetados: number;
    };
    achados: AchadoContrato[];
}

function normalizarCaminho(caminho: string): string {
    return caminho.replaceAll(path.sep, "/");
}

function extrairImports(conteudo: string): Importacoes {
    const imports = conteudo.match(/^import\s+[^;]+;/gm) ?? [];
    const exatos = new Map<string, string>();
    const curingas: string[] = [];

    for (const item of imports) {
        const nome = item.replace(/^import\s+/, "").replace(/;$/, "").trim();
        if (nome.endsWith(".*")) {
            curingas.push(nome.slice(0, -2));
            continue;
        }

        const simples = nome.split(".").at(-1);
        if (simples) {
            exatos.set(simples, nome);
        }
    }

    return {exatos, curingas};
}

function extrairTiposRetornoController(conteudo: string): TipoRetorno[] {
    const tipos: TipoRetorno[] = [];
    const regexMetodo = /public\s+([A-Za-z0-9_$.<>, ?]+)\s+([a-zA-Z0-9_]+)\s*\(/g;

    for (const correspondencia of conteudo.matchAll(regexMetodo)) {
        const assinaturaRetorno = correspondencia[1]?.trim();
        const nomeMetodo = correspondencia[2]?.trim();
        if (!assinaturaRetorno || !nomeMetodo) {
            continue;
        }

        const tipoResolvido = resolverTipoRetorno(assinaturaRetorno);
        if (!tipoResolvido || TIPOS_ESCALARES.has(tipoResolvido)) {
            continue;
        }

        tipos.push({
            metodo: nomeMetodo,
            tipo: tipoResolvido
        });
    }

    return tipos;
}

function resolverTipoRetorno(assinaturaRetorno: string): string | null {
    if (assinaturaRetorno.includes("ResponseEntity<")) {
        return extrairTipoInternoGenerico(assinaturaRetorno, "ResponseEntity");
    }
    if (assinaturaRetorno.includes("Page<")) {
        return extrairTipoInternoGenerico(assinaturaRetorno, "Page");
    }
    if (assinaturaRetorno.includes("List<")) {
        return extrairTipoInternoGenerico(assinaturaRetorno, "List");
    }
    if (assinaturaRetorno.includes("Set<")) {
        return extrairTipoInternoGenerico(assinaturaRetorno, "Set");
    }

    const simples = assinaturaRetorno.split(".").at(-1)?.trim();
    if (!simples || simples.includes("[") || simples.includes("?")) {
        return null;
    }
    return simples;
}

function extrairTipoInternoGenerico(texto: string, wrapper: string): string | null {
    const regex = new RegExp(`${wrapper}<([^>]+)>`);
    const bruto = texto.match(regex)?.[1]?.trim();
    if (!bruto) {
        return null;
    }

    const simples = bruto.split(",").at(-1)?.split(".").at(-1)?.trim();
    if (!simples || simples.includes("[") || simples.includes("?")) {
        return null;
    }

    return simples;
}

function extrairCamposExpostos(conteudo: string, nomeTipo: string): CampoExposto[] {
    const campos: CampoExposto[] = [];
    const trechoRecord = conteudo.match(new RegExp(`record\\s+${nomeTipo}\\s*\\(([\\s\\S]*?)\\)\\s*\\{`))?.[1];
    if (trechoRecord) {
        const itens = trechoRecord.split(",\n");
        for (const itemBruto of itens) {
            const item = itemBruto.replace(/\s+/g, " ").trim();
            const correspondencia = item.match(/(?:@[\w.]+\s+)*(?:final\s+)?([\w.<>?]+)\s+([a-zA-Z0-9_]+)$/);
            if (!correspondencia) {
                continue;
            }
            campos.push({
                tipo: correspondencia[1],
                nome: correspondencia[2]
            });
        }
        return campos;
    }

    const linhas = conteudo.split(/\r?\n/);
    for (const linha of linhas) {
        const limpa = linha.trim();
        if (!limpa || limpa.startsWith("@")) {
            continue;
        }
        const correspondencia = limpa.match(/^(?:private|protected|public)\s+(?:static\s+)?(?:final\s+)?([\w.<>?]+)\s+([a-zA-Z0-9_]+)\s*[=;]/);
        if (!correspondencia) {
            continue;
        }
        campos.push({
            tipo: correspondencia[1],
            nome: correspondencia[2]
        });
    }
    return campos;
}

function extrairTiposSimples(tipoDeclarado: string): string[] {
    return [...tipoDeclarado.matchAll(/\b([A-Z][A-Za-z0-9_]+)\b/g)].map((item) => item[1]);
}

async function indexarArquivosJava(diretorioCodigo: string): Promise<IndiceArquivos> {
    const arquivos = await globby(path.join(diretorioCodigo, "**/*.java").replace(/\\/g, "/"), {absolute: true});
    const porNome = new Map<string, string[]>();

    for (const arquivo of arquivos) {
        const nome = path.basename(arquivo, ".java");
        const arquivosComNome = porNome.get(nome) ?? [];
        arquivosComNome.push(arquivo);
        porNome.set(nome, arquivosComNome);
    }

    return {arquivos, porNome};
}

async function lerCacheado(cacheArquivos: Map<string, string>, arquivo: string): Promise<string> {
    const cache = cacheArquivos.get(arquivo);
    if (cache !== undefined) {
        return cache;
    }

    const conteudo = await fs.readFile(arquivo, "utf-8");
    cacheArquivos.set(arquivo, conteudo);
    return conteudo;
}

async function listarTiposPacoteModelo(
    pacoteCuringa: string,
    cacheArquivos: Map<string, string>,
    diretorioCodigo: string
): Promise<Map<string, TipoModelo>> {
    const diretorioRaizJava = path.dirname(diretorioCodigo);
    const diretorio = path.join(diretorioRaizJava, ...pacoteCuringa.split("."));
    const candidatos = await globby(path.join(diretorio, "*.java").replace(/\\/g, "/"), {absolute: true});
    const tipos = new Map<string, TipoModelo>();

    for (const arquivo of candidatos) {
        const conteudo = await lerCacheado(cacheArquivos, arquivo);
        const nome = path.basename(arquivo, ".java");
        tipos.set(nome, {
            fqn: `${pacoteCuringa}.${nome}`,
            arquivo,
            categoria: classificarTipoModelo(conteudo)
        });
    }

    return tipos;
}

function classificarTipoModelo(conteudo: string): CategoriaModelo {
    if (conteudo.includes("@Entity")) {
        return "entidade";
    }
    if (conteudo.match(/\benum\s+[A-Z]/)) {
        return "enum";
    }
    return "modelo";
}

async function resolverTiposModeloDisponiveis(
    imports: Importacoes,
    indiceArquivos: IndiceArquivos,
    cacheArquivos: Map<string, string>,
    diretorioCodigo: string
): Promise<Map<string, TipoModelo>> {
    const tiposModelo = new Map<string, TipoModelo>();

    for (const [simples, fqn] of imports.exatos.entries()) {
        if (!fqn.includes(".model.")) {
            continue;
        }
        const candidatos = indiceArquivos.porNome.get(simples) ?? [];
        const arquivo = candidatos.find((item) => normalizarCaminho(item).endsWith(`${fqn.replaceAll(".", "/")}.java`));
        if (!arquivo) {
            continue;
        }
        const conteudoTipo = await lerCacheado(cacheArquivos, arquivo);
        tiposModelo.set(simples, {
            fqn,
            arquivo,
            categoria: classificarTipoModelo(conteudoTipo)
        });
    }

    for (const pacote of imports.curingas.filter((item) => item.includes(".model"))) {
        const tiposPacote = await listarTiposPacoteModelo(pacote, cacheArquivos, diretorioCodigo);
        for (const [simples, meta] of tiposPacote.entries()) {
            if (!tiposModelo.has(simples)) {
                tiposModelo.set(simples, meta);
            }
        }
    }

    return tiposModelo;
}

async function auditarContratos(diretorioCodigo: string, diretorioBase: string): Promise<RelatorioContratos> {
    const indiceArquivos = await indexarArquivosJava(diretorioCodigo);
    const cacheArquivos = new Map<string, string>();
    const controladores = indiceArquivos.arquivos.filter((arquivo) => arquivo.endsWith("Controller.java"));
    const achados: AchadoContrato[] = [];

    for (const controlador of controladores) {
        const conteudoControlador = await lerCacheado(cacheArquivos, controlador);
        const retornos = extrairTiposRetornoController(conteudoControlador);
        const nomeControlador = path.basename(controlador);

        for (const retorno of retornos) {
            const arquivosTipo = indiceArquivos.porNome.get(retorno.tipo) ?? [];
            const arquivoTipo = arquivosTipo[0];
            if (!arquivoTipo || !arquivoTipo.includes("/dto/") && !arquivoTipo.endsWith("Dto.java") && !arquivoTipo.endsWith("Response.java")) {
                continue;
            }

            const conteudoTipo = await lerCacheado(cacheArquivos, arquivoTipo);
            const imports = extrairImports(conteudoTipo);
            const tiposModelo = await resolverTiposModeloDisponiveis(imports, indiceArquivos, cacheArquivos, diretorioCodigo);
            if (tiposModelo.size === 0) {
                continue;
            }

            const nomeTipo = path.basename(arquivoTipo, ".java");
            const campos = extrairCamposExpostos(conteudoTipo, nomeTipo);

            for (const campo of campos) {
                const tiposSimples = extrairTiposSimples(campo.tipo);
                for (const tipoSimples of tiposSimples) {
                    const meta = tiposModelo.get(tipoSimples);
                    if (!meta || meta.categoria === "enum") {
                        continue;
                    }

                    achados.push({
                        controlador: nomeControlador,
                        metodo: retorno.metodo,
                        tipoRetorno: retorno.tipo,
                        arquivoDto: normalizarCaminho(path.relative(diretorioBase, arquivoTipo)),
                        campo: campo.nome,
                        tipoCampo: campo.tipo,
                        categoria: meta.categoria,
                        tipoModelo: meta.fqn
                    });
                }
            }
        }
    }

    return {
        geradoEm: new Date().toISOString(),
        base: diretorioBase,
        resumo: {
            totalAchados: achados.length,
            controladoresAfetados: new Set(achados.map((item) => item.controlador)).size,
            dtosAfetados: new Set(achados.map((item) => item.arquivoDto)).size
        },
        achados
    };
}

function gerarMarkdown(relatorio: RelatorioContratos): string {
    const linhas: string[] = [];
    linhas.push("# Auditoria de contratos HTTP do servidor", "");
    linhas.push(`Gerado em: ${relatorio.geradoEm}`, "");
    linhas.push(`- Achados: ${relatorio.resumo.totalAchados}`);
    linhas.push(`- Controllers afetados: ${relatorio.resumo.controladoresAfetados}`);
    linhas.push(`- DTOs afetados: ${relatorio.resumo.dtosAfetados}`, "");

    if (relatorio.achados.length === 0) {
        linhas.push("Nenhum DTO publico com vazamento direto de `model.*` foi encontrado.");
        return linhas.join("\n");
    }

    linhas.push("| Controller | Metodo | Tipo retornado | Campo exposto | Tipo do campo | Categoria | Tipo de modelo | Arquivo DTO |");
    linhas.push("|---|---|---|---|---|---|---|---|");

    for (const achado of relatorio.achados) {
        linhas.push(`| ${achado.controlador} | ${achado.metodo} | ${achado.tipoRetorno} | ${achado.campo} | \`${achado.tipoCampo}\` | ${achado.categoria} | \`${achado.tipoModelo}\` | \`${achado.arquivoDto}\` |`);
    }

    return linhas.join("\n");
}

async function gravarRelatorio(relatorio: RelatorioContratos, diretorioSaida: string): Promise<string> {
    const caminhoMarkdown = path.join(diretorioSaida, "contratos-auditoria.md");
    await fs.mkdir(diretorioSaida, {recursive: true});
    await fs.writeFile(caminhoMarkdown, gerarMarkdown(relatorio), "utf-8");
    return caminhoMarkdown;
}

function exibirAjuda(): void {
    exibirAjudaComando({
        comandoToolkit: "servidor contratos auditar",
        scriptDireto: "servidor/contratos-auditar.ts",
        descricao: "Audita DTOs e responses expostos por controllers para detectar vazamento de tipos model.* no contrato HTTP.",
        opcoes: [
            "--json              Emite o relatório em JSON.",
            "--gravar            Grava o relatório Markdown em disco.",
            "--base <diretorio>  Sobrescreve a base da auditoria.",
            "--help, -h          Exibe esta ajuda."
        ],
        exemplos: [
            "npx tsx toolkit/ferramentas.ts servidor contratos auditar",
            "npx tsx toolkit/ferramentas.ts servidor contratos auditar --json",
            "npx tsx toolkit/ferramentas.ts servidor contratos auditar --gravar"
        ]
    });
}

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    const emitirJson = argumentos.includes("--json");
    const gravar = argumentos.includes("--gravar");
    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const diretorioCodigo = resolverCaminhoConfigurado("codigoServidor", diretorioBase);
    const diretorioSaida = path.join(resolverCaminhoConfigurado("artefatosQualidade", diretorioBase), "servidor", "mais-recente");

    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjuda();
        return;
    }

    if (!emitirJson) {
        imprimirCabecalho("AUDITORIA DE CONTRATOS HTTP (SERVIDOR)");
        escreverLinha(`Base analisada: ${pc.dim(diretorioCodigo)}`);
    }

    const relatorio = await auditarContratos(diretorioCodigo, diretorioBase);

    if (gravar) {
        const caminhoRelatorio = await gravarRelatorio(relatorio, diretorioSaida);
        if (!emitirJson) {
            escreverLinha(`Relatório Markdown: ${pc.dim(caminhoRelatorio)}`);
        }
    }

    if (!emitirJson) {
        escreverLinha(`Achados: ${relatorio.resumo.totalAchados}`);
        escreverLinha(`Controllers afetados: ${relatorio.resumo.controladoresAfetados}`);
        escreverLinha(`DTOs afetados: ${relatorio.resumo.dtosAfetados}`);

        if (relatorio.achados.length > 0) {
            for (const achado of relatorio.achados.slice(0, 10)) {
                escreverLinha(`- ${achado.controlador}.${achado.metodo} -> ${achado.tipoRetorno}.${achado.campo} (${achado.tipoModelo})`);
            }
        } else {
            escreverLinha(pc.green("Nenhum vazamento direto de model.* foi encontrado nos DTOs publicos auditados."));
        }
    }

    if (emitirJson) {
        imprimirJson(relatorio);
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        escreverLinha(pc.red(`Erro ao auditar contratos do servidor: ${erro instanceof Error ? erro.message : String(erro)}`));
        process.exitCode = 1;
    });
}

export {
    principal,
};
