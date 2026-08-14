
import path from "node:path";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverErro, escreverLinha, imprimirCabecalho, imprimirJson} from "../biblioteca/saida.js";
import {executarSemgrep, gravarRelatoriosSemgrep, normalizarCaminhoAchado} from "./semgrep-motor.js";

function extrairLista(argumentos: string[], nome: string): string[] {
    const valores: string[] = [];
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

function resolverDiretoriosPadrao(diretorioBase: string = DIRETORIO_RAIZ): string[] {
    const baseResolvida = path.resolve(diretorioBase);
    return [
        path.relative(baseResolvida, resolverCaminhoConfigurado("codigoServidor", baseResolvida)),
        path.relative(baseResolvida, resolverCaminhoConfigurado("codigoCliente", baseResolvida))
    ];
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const argumentosValidados = validarArgumentosEntradaDireta(import.meta.url, argumentos);
    if (argumentosValidados.includes("--help") || argumentosValidados.includes("-h")) {
        exibirAjudaComando({
            comandoToolkit: "codigo semgrep auditar",
            descricao: "Executa regras Semgrep configuradas para monitorar deriva estrutural no projeto.",
            opcoes: [
                "--regra <arquivo>     Sobrescreve o arquivo de regras YAML.",
                "--diretorio <caminho> Adiciona diretório-alvo; pode ser repetido.",
                "--base <diretorio>    Usa outra raiz para execução e artefatos.",
                "--auto                Acumula as regras locais com `--config auto` do Semgrep CE.",
                "--json                Emite resumo estruturado em JSON.",
                "--gravar              Grava relatórios JSON e Markdown em disco."
            ],
            exemplos: [
                "ferramentas codigo semgrep auditar",
                "ferramentas codigo semgrep auditar --diretorio backend/src/main/java/exemplo",
                "ferramentas codigo semgrep auditar --auto --json"
            ]
        });
        return;
    }

    const emitirJson = argumentosValidados.includes("--json");
    const gravar = argumentosValidados.includes("--gravar");
    const auto = argumentosValidados.includes("--auto");
    const diretorioBase = path.resolve(lerOpcao(argumentosValidados, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const regra = lerOpcao(argumentosValidados, "--regra", resolverCaminhoConfigurado("regrasSemgrep", diretorioBase))
        ?? resolverCaminhoConfigurado("regrasSemgrep", diretorioBase);
    const diretorios = extrairLista(argumentosValidados, "--diretorio");
    const alvos = diretorios.length > 0 ? diretorios : resolverDiretoriosPadrao(diretorioBase);
    const diretorioSaida = path.join(resolverCaminhoConfigurado("artefatosQualidade", diretorioBase), "semgrep", "mais-recente");

    if (!emitirJson) {
        imprimirCabecalho("AUDITORIA SEMGREP (PILOTO)");
        escreverLinha(`Regra: ${pc.dim(regra)}`);
        escreverLinha(`Alvos: ${alvos.map(item => pc.dim(item)).join(", ")}`);
        escreverLinha(`Modo auto: ${auto ? "sim" : "não"}`);
    }

    const execucao = await executarSemgrep({regra, diretorios: alvos, auto, diretorioBase});

    if (gravar) {
        const caminhosRelatorios = await gravarRelatoriosSemgrep(execucao, diretorioSaida, diretorioBase);
        if (!emitirJson) {
            escreverLinha(`Relatório JSON: ${pc.dim(caminhosRelatorios.caminhoResultadoJson)}`);
            escreverLinha(`Resumo Markdown: ${pc.dim(caminhosRelatorios.caminhoResultadoMd)}`);
        }
    }

    const achados = execucao.resultadoJson.results;
    if (!emitirJson) {
        escreverLinha(`Achados: ${achados.length}`);
        if (achados.length > 0) {
            for (const achado of achados.slice(0, 10)) {
                const caminho = normalizarCaminhoAchado(achado.path ?? "", diretorioBase);
                escreverLinha(`- ${achado.check_id ?? "sem-id"} em ${caminho}:${achado.start?.line ?? "?"}`);
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
    principal().catch((erro: unknown) => {
        escreverErro(`Erro ao executar auditoria Semgrep: ${erro instanceof Error ? erro.message : String(erro)}\n`);
        process.exitCode = 1;
    });
}

export {
    principal,
    resolverDiretoriosPadrao
};
