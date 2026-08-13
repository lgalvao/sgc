#!/usr/bin/env node

import fs from "node:fs/promises";
import path from "node:path";
import pc from "picocolors";
import {globby} from "globby";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

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

function normalizarCaminho(caminho) {
    return caminho.replaceAll(path.sep, "/");
}

function extrairImports(conteudo) {
    const imports = conteudo.match(/^import\s+[^;]+;/gm) ?? [];
    const exatos = new Map();
    const curingas = [];

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

function extrairTiposRetornoController(conteudo) {
    const tipos = [];
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

function resolverTipoRetorno(assinaturaRetorno) {
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

function extrairTipoInternoGenerico(texto, wrapper) {
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

function extrairCamposExpostos(conteudo, nomeTipo) {
    const campos = [];
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

function extrairTiposSimples(tipoDeclarado) {
    return [...tipoDeclarado.matchAll(/\b([A-Z][A-Za-z0-9_]+)\b/g)].map((item) => item[1]);
}

async function indexarArquivosJava(diretorioCodigo) {
    const arquivos = await globby(path.join(diretorioCodigo, "**/*.java").replace(/\\/g, "/"), {absolute: true});
    const porNome = new Map();

    for (const arquivo of arquivos) {
        const nome = path.basename(arquivo, ".java");
        if (!porNome.has(nome)) {
            porNome.set(nome, []);
        }
        porNome.get(nome).push(arquivo);
    }

    return {arquivos, porNome};
}

async function lerCacheado(cacheArquivos, arquivo) {
    if (!cacheArquivos.has(arquivo)) {
        cacheArquivos.set(arquivo, await fs.readFile(arquivo, "utf-8"));
    }
    return cacheArquivos.get(arquivo);
}

async function listarTiposPacoteModelo(pacoteCuringa, cacheArquivos, diretorioBase, diretorioCodigo) {
    const diretorioRaizJava = path.dirname(diretorioCodigo);
    const diretorio = path.join(diretorioRaizJava, ...pacoteCuringa.split("."));
    const candidatos = await globby(path.join(diretorio, "*.java").replace(/\\/g, "/"), {absolute: true});
    const tipos = new Map();

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

function classificarTipoModelo(conteudo) {
    if (conteudo.includes("@Entity")) {
        return "entidade";
    }
    if (conteudo.match(/\benum\s+[A-Z]/)) {
        return "enum";
    }
    return "modelo";
}

async function resolverTiposModeloDisponiveis(imports, indiceArquivos, cacheArquivos, diretorioBase, diretorioCodigo) {
    const tiposModelo = new Map();

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
        const tiposPacote = await listarTiposPacoteModelo(pacote, cacheArquivos, diretorioBase, diretorioCodigo);
        for (const [simples, meta] of tiposPacote.entries()) {
            if (!tiposModelo.has(simples)) {
                tiposModelo.set(simples, meta);
            }
        }
    }

    return tiposModelo;
}

async function auditarContratos(diretorioCodigo, diretorioBase) {
    const indiceArquivos = await indexarArquivosJava(diretorioCodigo);
    const cacheArquivos = new Map();
    const controladores = indiceArquivos.arquivos.filter((arquivo) => arquivo.endsWith("Controller.java"));
    const achados = [];

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
            const tiposModelo = await resolverTiposModeloDisponiveis(imports, indiceArquivos, cacheArquivos, diretorioBase, diretorioCodigo);
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

function gerarMarkdown(relatorio) {
    const linhas = [];
    linhas.push("# Auditoria de contratos HTTP do backend", "");
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

async function gravarRelatorio(relatorio, diretorioSaida) {
    const caminhoMarkdown = path.join(diretorioSaida, "contratos-auditoria.md");
    await fs.mkdir(diretorioSaida, {recursive: true});
    await fs.writeFile(caminhoMarkdown, gerarMarkdown(relatorio), "utf-8");
    return caminhoMarkdown;
}

function exibirAjuda() {
    exibirAjudaComando({
        comandoSgc: "backend contratos auditar",
        scriptDireto: "backend/contratos-auditar.js",
        descricao: "Audita DTOs e responses expostos por controllers para detectar vazamento de tipos model.* no contrato HTTP.",
        opcoes: [
            "--json              Emite o relatório em JSON.",
            "--sem-gravar        Nao grava o Markdown em disco.",
            "--help, -h          Exibe esta ajuda."
        ],
        exemplos: [
            "npx tsx toolkit/sgc.ts backend contratos auditar",
            "npx tsx toolkit/sgc.ts backend contratos auditar --json",
            "npx tsx toolkit/sgc.ts backend contratos auditar --sem-gravar"
        ]
    });
}

async function principal(argumentos = process.argv.slice(2)) {
    const emitirJson = argumentos.includes("--json");
    const semGravar = argumentos.includes("--sem-gravar");
    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ));
    const diretorioCodigo = resolverCaminhoConfigurado("backendCodigo", diretorioBase);
    const diretorioSaida = path.join(resolverCaminhoConfigurado("artefatosQualidade", diretorioBase), "backend", "latest");

    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjuda();
        return;
    }

    if (!emitirJson) {
        imprimirCabecalho("AUDITORIA DE CONTRATOS HTTP (BACKEND)");
        escreverLinha(`Base analisada: ${pc.dim(diretorioCodigo)}`);
    }

    const relatorio = await auditarContratos(diretorioCodigo, diretorioBase);

    if (!semGravar) {
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
        escreverLinha(pc.red(`Erro ao auditar contratos do backend: ${erro instanceof Error ? erro.message : String(erro)}`));
        process.exitCode = 1;
    });
}

export {
    principal,
};
