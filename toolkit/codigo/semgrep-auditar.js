#!/usr/bin/env node

import fs from "node:fs/promises";
import {homedir} from "node:os";
import path from "node:path";
import {pathToFileURL} from "node:url";
import {execa} from "execa";
import pc from "picocolors";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

const CAMINHO_REGRA_PADRAO = resolverCaminhoConfigurado("regrasSemgrep");
const DIRETORIO_SAIDA = path.join(resolverCaminhoConfigurado("artefatosQualidade"), "semgrep", "mais-recente");
const CAMINHO_RESULTADO_JSON = path.join(DIRETORIO_SAIDA, "resultado.json");
const CAMINHO_RESULTADO_MD = path.join(DIRETORIO_SAIDA, "resumo.md");
const DIRETORIOS_PADRAO = [
    "backend/src/main/java/sgc",
    "frontend/src"
];

function lerOpcao(args, nome, padrao) {
    const indice = args.indexOf(nome);
    if (indice === -1) {
        return padrao;
    }
    const valor = args[indice + 1];
    if (!valor || valor.startsWith("--")) {
        throw new Error(`Informe um valor para ${nome}.`);
    }
    return valor;
}

function extrairLista(args, nome) {
    const valores = [];
    for (let i = 0; i < args.length; i += 1) {
        if (args[i] === nome) {
            const valor = args[i + 1];
            if (!valor || valor.startsWith("--")) {
                throw new Error(`Informe um valor para ${nome}.`);
            }
            valores.push(valor);
            i += 1;
        }
    }
    return valores;
}

function obterComandoSemgrep() {
    return path.join(homedir(), ".local", "bin", "semgrep");
}

function criarResumo(resultadoJson, regra) {
    const findings = resultadoJson.results ?? [];
    const porRegra = new Map();

    for (const finding of findings) {
        const id = finding.check_id ?? "sem-id";
        if (!porRegra.has(id)) {
            porRegra.set(id, []);
        }
        porRegra.get(id).push(finding);
    }

    const linhas = [];
    linhas.push("# Auditoria Semgrep do SGC", "");
    linhas.push(`Regra: \`${regra}\``, "");
    linhas.push(`Total de achados: ${findings.length}`, "");

    if (findings.length === 0) {
        linhas.push("Nenhum achado encontrado.");
        return linhas.join("\n");
    }

    linhas.push("| Regra | Achados |");
    linhas.push("|---|---:|");
    for (const [id, itens] of [...porRegra.entries()].toSorted((a, b) => b[1].length - a[1].length)) {
        linhas.push(`| \`${id}\` | ${itens.length} |`);
    }

    linhas.push("", "## Primeiros achados", "");
    for (const finding of findings.slice(0, 20)) {
        const caminho = path.relative(resolverNaRaiz("."), finding.path ?? "");
        linhas.push(`- \`${finding.check_id}\` em \`${caminho}:${finding.start?.line ?? "?"}\` - ${finding.extra?.message ?? ""}`);
    }

    return linhas.join("\n");
}

async function executarSemgrep({
                                   regra = CAMINHO_REGRA_PADRAO,
                                   diretorios = DIRETORIOS_PADRAO,
                                   auto = false
                               }) {
    const comando = obterComandoSemgrep();
    const configs = auto ? ["--config", "auto", "--config", regra] : ["--config", regra];
    const resultado = await execa(comando, [
        "scan",
        ...configs,
        "--json",
        ...diretorios
    ], {
        cwd: resolverNaRaiz("."),
        env: {
            PATH: `${path.dirname(comando)}:${process.env.PATH ?? ""}`
        },
        reject: false
    });

    const resultadoJson = JSON.parse(resultado.stdout || "{}");
    return {
        comando,
        regra,
        diretorios,
        auto,
        codigoSaida: resultado.exitCode ?? 0,
        resultadoJson
    };
}

async function gravarRelatorios(execucao) {
    await fs.mkdir(path.dirname(CAMINHO_RESULTADO_JSON), {recursive: true});
    await fs.writeFile(CAMINHO_RESULTADO_JSON, `${JSON.stringify(execucao.resultadoJson, null, 2)}\n`, "utf-8");
    await fs.writeFile(CAMINHO_RESULTADO_MD, `${criarResumo(execucao.resultadoJson, execucao.regra)}\n`, "utf-8");
}

async function main() {
    const args = process.argv.slice(2);
    if (args.includes("--help") || args.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "codigo semgrep auditar",
            scriptDireto: "codigo/semgrep-auditar.js",
            descricao: "Executa um piloto de Semgrep OSS com regras locais do SGC para monitorar deriva estrutural em backend, frontend e integração.",
            opcoes: [
                "--regra <arquivo>     Sobrescreve o arquivo de regras YAML.",
                "--dir <caminho>       Adiciona diretório-alvo; pode ser repetido.",
                "--auto                Acumula as regras locais com `--config auto` do Semgrep CE.",
                "--json                Emite resumo estruturado em JSON.",
                "--sem-gravar          Não grava relatórios em disco."
            ],
            exemplos: [
                "node toolkit/sgc.js codigo semgrep auditar",
                "node toolkit/sgc.js codigo semgrep auditar --dir backend/src/main/java/sgc/subprocesso",
                "node toolkit/sgc.js codigo semgrep auditar --auto --json"
            ]
        });
        return;
    }

    const emitirJson = args.includes("--json");
    const semGravar = args.includes("--sem-gravar");
    const auto = args.includes("--auto");
    const regra = lerOpcao(args, "--regra", CAMINHO_REGRA_PADRAO);
    const diretorios = extrairLista(args, "--dir");
    const alvos = diretorios.length > 0 ? diretorios : DIRETORIOS_PADRAO;

    if (!emitirJson) {
        imprimirCabecalho("AUDITORIA SEMGREP (PILOTO)");
        escreverLinha(`Regra: ${pc.dim(regra)}`);
        escreverLinha(`Alvos: ${alvos.map((item) => pc.dim(item)).join(", ")}`);
        escreverLinha(`Modo auto: ${auto ? "sim" : "não"}`);
    }

    const execucao = await executarSemgrep({
        regra,
        diretorios: alvos,
        auto
    });

    if (!semGravar) {
        await gravarRelatorios(execucao);
        if (!emitirJson) {
            escreverLinha(`Relatório JSON: ${pc.dim(CAMINHO_RESULTADO_JSON)}`);
            escreverLinha(`Resumo Markdown: ${pc.dim(CAMINHO_RESULTADO_MD)}`);
        }
    }

    const achados = execucao.resultadoJson.results ?? [];
    if (!emitirJson) {
        escreverLinha(`Achados: ${achados.length}`);
        if (achados.length > 0) {
            for (const finding of achados.slice(0, 10)) {
                const caminho = path.relative(resolverNaRaiz("."), finding.path ?? "");
                escreverLinha(`- ${finding.check_id} em ${caminho}:${finding.start?.line ?? "?"}`);
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

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
    main().catch((erro) => {
        process.stderr.write(`Erro ao executar auditoria Semgrep: ${erro instanceof Error ? erro.message : String(erro)}\n`);
        process.exit(1);
    });
}

export {
    executarSemgrep,
    main
};
