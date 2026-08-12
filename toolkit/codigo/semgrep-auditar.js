#!/usr/bin/env node

import fs from "node:fs/promises";
import {homedir} from "node:os";
import path from "node:path";
import {execa} from "execa";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

function extrairLista(argumentos, nome) {
    const valores = [];
    const prefixoAtribuicao = `${nome}=`;
    for (let indice = 0; indice < argumentos.length; indice += 1) {
        if (argumentos[indice] === nome) {
            const valor = argumentos[indice + 1];
            if (!valor || valor.startsWith("--")) {
                throw new Error(`Informe um valor para ${nome}.`);
            }
            valores.push(valor);
            indice += 1;
        } else if (argumentos[indice].startsWith(prefixoAtribuicao)) {
            const valor = argumentos[indice].slice(prefixoAtribuicao.length);
            if (!valor) {
                throw new Error(`Informe um valor para ${nome}.`);
            }
            valores.push(valor);
        }
    }
    return valores;
}

function resolverDiretoriosPadrao(diretorioBase = DIRETORIO_RAIZ) {
    const baseResolvida = path.resolve(diretorioBase);
    return [
        path.relative(baseResolvida, resolverCaminhoConfigurado("backendCodigo", baseResolvida)),
        path.relative(baseResolvida, resolverCaminhoConfigurado("frontendCodigo", baseResolvida)),
    ];
}

function obterComandoSemgrep() {
    return path.join(homedir(), ".local", "bin", "semgrep");
}

function criarResumo(resultadoJson, regra, diretorioBase) {
    const achados = resultadoJson.results ?? [];
    const porRegra = new Map();

    for (const achado of achados) {
        const codigoRegra = achado.check_id ?? "sem-id";
        if (!porRegra.has(codigoRegra)) {
            porRegra.set(codigoRegra, []);
        }
        porRegra.get(codigoRegra).push(achado);
    }

    const linhas = [];
    linhas.push("# Auditoria Semgrep do SGC", "");
    linhas.push(`Regra: \`${regra}\``, "");
    linhas.push(`Total de achados: ${achados.length}`, "");

    if (achados.length === 0) {
        linhas.push("Nenhum achado encontrado.");
        return linhas.join("\n");
    }

    linhas.push("| Regra | Achados |");
    linhas.push("|---|---:|");
    for (const [id, itens] of [...porRegra.entries()].toSorted((a, b) => b[1].length - a[1].length)) {
        linhas.push(`| \`${id}\` | ${itens.length} |`);
    }

    linhas.push("", "## Primeiros achados", "");
    for (const achado of achados.slice(0, 20)) {
        const caminho = path.relative(diretorioBase, achado.path ?? "");
        linhas.push(`- \`${achado.check_id}\` em \`${caminho}:${achado.start?.line ?? "?"}\` - ${achado.extra?.message ?? ""}`);
    }

    return linhas.join("\n");
}

async function executarSemgrep({
                                   regra,
                                   diretorios,
                                   auto = false,
                                   diretorioBase = DIRETORIO_RAIZ,
                               }) {
    const baseResolvida = path.resolve(diretorioBase);
    const alvos = diretorios ?? resolverDiretoriosPadrao(baseResolvida);
    const comando = obterComandoSemgrep();
    const configs = auto ? ["--config", "auto", "--config", regra] : ["--config", regra];
    const resultado = await execa(comando, [
        "scan",
        ...configs,
        "--json",
        ...alvos
    ], {
        cwd: baseResolvida,
        env: {
            PATH: `${path.dirname(comando)}:${process.env.PATH ?? ""}`
        },
        reject: false
    });

    const resultadoJson = JSON.parse(resultado.stdout || "{}");
    return {
        comando,
        regra,
        diretorios: alvos,
        auto,
        codigoSaida: resultado.exitCode ?? 0,
        resultadoJson
    };
}

async function gravarRelatorios(execucao, diretorioSaida, diretorioBase) {
    const caminhoResultadoJson = path.join(diretorioSaida, "resultado.json");
    const caminhoResultadoMd = path.join(diretorioSaida, "resumo.md");
    await fs.mkdir(diretorioSaida, {recursive: true});
    await fs.writeFile(caminhoResultadoJson, `${JSON.stringify(execucao.resultadoJson, null, 2)}\n`, "utf-8");
    await fs.writeFile(caminhoResultadoMd, `${criarResumo(execucao.resultadoJson, execucao.regra, diretorioBase)}\n`, "utf-8");
    return {caminhoResultadoJson, caminhoResultadoMd};
}

async function principal(argumentos = process.argv.slice(2)) {
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "codigo semgrep auditar",
            scriptDireto: "codigo/semgrep-auditar.js",
            descricao: "Executa um piloto de Semgrep OSS com regras locais do SGC para monitorar deriva estrutural em backend, frontend e integração.",
            opcoes: [
                "--regra <arquivo>     Sobrescreve o arquivo de regras YAML.",
                "--dir <caminho>       Adiciona diretório-alvo; pode ser repetido.",
                "--base <diretorio>    Usa outra raiz para execução e artefatos.",
                "--auto                Acumula as regras locais com `--config auto` do Semgrep CE.",
                "--json                Emite resumo estruturado em JSON.",
                "--sem-gravar          Não grava relatórios em disco."
            ],
            exemplos: [
                "npx tsx toolkit/sgc.js codigo semgrep auditar",
                "npx tsx toolkit/sgc.js codigo semgrep auditar --dir backend/src/main/java/sgc/subprocesso",
                "npx tsx toolkit/sgc.js codigo semgrep auditar --auto --json"
            ]
        });
        return;
    }

    const emitirJson = argumentos.includes("--json");
    const semGravar = argumentos.includes("--sem-gravar");
    const auto = argumentos.includes("--auto");
    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ));
    const regra = lerOpcao(argumentos, "--regra", resolverCaminhoConfigurado("regrasSemgrep", diretorioBase));
    const diretorios = extrairLista(argumentos, "--dir");
    const alvos = diretorios.length > 0 ? diretorios : resolverDiretoriosPadrao(diretorioBase);
    const diretorioSaida = path.join(resolverCaminhoConfigurado("artefatosQualidade", diretorioBase), "semgrep", "mais-recente");

    if (!emitirJson) {
        imprimirCabecalho("AUDITORIA SEMGREP (PILOTO)");
        escreverLinha(`Regra: ${pc.dim(regra)}`);
        escreverLinha(`Alvos: ${alvos.map((item) => pc.dim(item)).join(", ")}`);
        escreverLinha(`Modo auto: ${auto ? "sim" : "não"}`);
    }

    const execucao = await executarSemgrep({
        regra,
        diretorios: alvos,
        auto,
        diretorioBase,
    });

    if (!semGravar) {
        const caminhosRelatorios = await gravarRelatorios(execucao, diretorioSaida, diretorioBase);
        if (!emitirJson) {
            escreverLinha(`Relatório JSON: ${pc.dim(caminhosRelatorios.caminhoResultadoJson)}`);
            escreverLinha(`Resumo Markdown: ${pc.dim(caminhosRelatorios.caminhoResultadoMd)}`);
        }
    }

    const achados = execucao.resultadoJson.results ?? [];
    if (!emitirJson) {
        escreverLinha(`Achados: ${achados.length}`);
        if (achados.length > 0) {
            for (const achado of achados.slice(0, 10)) {
                const caminho = path.relative(diretorioBase, achado.path ?? "");
                escreverLinha(`- ${achado.check_id} em ${caminho}:${achado.start?.line ?? "?"}`);
            }
        } else {
            escreverLinha(pc.green("Nenhum achado encontrado pelas regras locais."));
        }
    }

    if (emitirJson) {
        imprimirJson({
            regra,
            diretorios: alvos,
            auto,
            codigoSaida: execucao.codigoSaida,
            totalAchados: achados.length
        });
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        process.stderr.write(`Erro ao executar auditoria Semgrep: ${erro instanceof Error ? erro.message : String(erro)}\n`);
        process.exitCode = 1;
    });
}

export {
    executarSemgrep,
    principal,
    resolverDiretoriosPadrao,
};
