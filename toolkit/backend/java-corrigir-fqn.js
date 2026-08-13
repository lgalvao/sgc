#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha} from "../lib/saida.js";

const DIRETORIOS_ALVO = ["src/test/java", "src/main/java"];
const PREFIXO_PACOTE = "package ";
const PREFIXO_IMPORT = "import ";
const PREFIXO_ESTATICO = "static ";
const PADRAO_FQN = /("[^"]*")|(\b([a-z]\w*(?:\.[a-z]\w*)+)\.([A-Z]\w*)\b)/g;

function deveIgnorarFqn(partePacote, parteClasse) {
    if (parteClasse === "Assertions") {
        return true;
    }
    return partePacote === "java.lang";
}

function analisarImports(linhas) {
    let pacoteAtual = null;
    const importsExistentes = new Map();
    const indicesLinhasImport = [];

    linhas.forEach((linha, indice) => {
        const linhaLimpa = linha.trim();
        if (linhaLimpa.startsWith(PREFIXO_PACOTE)) {
            pacoteAtual = linhaLimpa.split("//")[0].replace(PREFIXO_PACOTE, "").replace(";", "").trim();
            return;
        }

        if (!linhaLimpa.startsWith(PREFIXO_IMPORT)) {
            return;
        }

        indicesLinhasImport.push(indice);
        let importLimpo = linhaLimpa.split("//")[0].replace(PREFIXO_IMPORT, "").replace(";", "").trim();
        if (importLimpo.startsWith(PREFIXO_ESTATICO)) {
            importLimpo = importLimpo.replace(PREFIXO_ESTATICO, "").trim();
        }

        const nomeSimples = importLimpo.split(".").pop();
        if (nomeSimples !== "*") {
            importsExistentes.set(nomeSimples, importLimpo);
        }
    });

    return {pacoteAtual, importsExistentes, indicesLinhasImport};
}

function obterPosicaoInsercao(linhas, indicesLinhasImport) {
    if (indicesLinhasImport.length > 0) {
        return indicesLinhasImport[indicesLinhasImport.length - 1] + 1;
    }

    for (let indice = 0; indice < linhas.length; indice++) {
        if (linhas[indice].trim().startsWith(PREFIXO_PACOTE)) {
            return indice + 1;
        }
    }

    return 0;
}

function existeColisao(nomeSimples, fqn, importsNovos) {
    return [...importsNovos].some((item) => item.split(".").pop() === nomeSimples && item !== fqn);
}

function determinarSubstituicao(correspondencia, pacoteAtual, importsExistentes, importsNovos) {
    if (correspondencia[1]) {
        return {substituicao: correspondencia[1], alterado: false};
    }

    const correspondenciaCompleta = correspondencia[2];
    const pacote = correspondencia[3];
    const classe = correspondencia[4];
    const fqn = `${pacote}.${classe}`;

    let deveSubstituir = false;

    if (deveIgnorarFqn(pacote, classe)) {
        deveSubstituir = true;
    } else if (pacoteAtual && pacote === pacoteAtual) {
        deveSubstituir = true;
    } else if (importsExistentes.has(classe)) {
        deveSubstituir = importsExistentes.get(classe) === fqn;
    } else if (!existeColisao(classe, fqn, importsNovos)) {
        importsNovos.add(fqn);
        deveSubstituir = true;
    }

    return {
        substituicao: deveSubstituir ? classe : correspondenciaCompleta,
        alterado: deveSubstituir
    };
}

function analisarLinhas(linhas, pacoteAtual, importsExistentes) {
    const importsNovos = new Set();
    const linhasModificadas = [];
    let temAlteracoes = false;

    linhas.forEach((linha) => {
        const linhaLimpa = linha.trim();
        if (linhaLimpa.startsWith(PREFIXO_PACOTE) || linhaLimpa.startsWith(PREFIXO_IMPORT) || linhaLimpa.startsWith("//") || linhaLimpa.startsWith("*")) {
            linhasModificadas.push(linha);
            return;
        }

        const novaLinha = linha.replace(PADRAO_FQN, (...argumentos) => {
            const {substituicao, alterado} = determinarSubstituicao(argumentos, pacoteAtual, importsExistentes, importsNovos);
            if (alterado) {
                temAlteracoes = true;
            }
            return substituicao;
        });
        linhasModificadas.push(novaLinha);
    });

    return {linhasModificadas, importsNovos, temAlteracoes};
}

function processarArquivo(caminhoArquivo, apenasSimulacao = false) {
    const linhas = fs.readFileSync(caminhoArquivo, "utf-8").split(/\r?\n/).map((linha, indice, todas) => (
        indice < todas.length - 1 ? `${linha}\n` : linha
    ));
    const {pacoteAtual, importsExistentes, indicesLinhasImport} = analisarImports(linhas);
    const {linhasModificadas, importsNovos, temAlteracoes} = analisarLinhas(linhas, pacoteAtual, importsExistentes);

    if (!temAlteracoes && importsNovos.size === 0) {
        return false;
    }

    const posicaoInsercao = obterPosicaoInsercao(linhas, indicesLinhasImport);
    const importsOrdenados = [...importsNovos].toSorted((a, b) => a.localeCompare(b, "pt-BR"));
    const saidaFinal = [];

    if (posicaoInsercao === 0) {
        importsOrdenados.forEach((item) => saidaFinal.push(`${PREFIXO_IMPORT}${item};\n`));
    }

    linhasModificadas.forEach((linha, indice) => {
        saidaFinal.push(linha);
        if (indice === posicaoInsercao - 1) {
            importsOrdenados.forEach((item) => saidaFinal.push(`${PREFIXO_IMPORT}${item};\n`));
        }
    });

    if (!apenasSimulacao) {
        fs.writeFileSync(caminhoArquivo, saidaFinal.join(""), "utf-8");
    }

    escreverLinha(`${apenasSimulacao ? "[simulacao] " : ""}Atualizado: ${caminhoArquivo} (${importsOrdenados.length} novo(s) import(s))`);
    return true;
}

function encontrarRaizBackend(diretorioBase = DIRETORIO_RAIZ) {
    const candidatos = [path.resolve(diretorioBase), path.resolve(diretorioBase, "backend")];
    return candidatos.find((candidato) => fs.existsSync(path.join(candidato, "src"))) ?? path.resolve(diretorioBase);
}

function lerArgumentos(argumentos) {
    return {
        ajuda: argumentos.includes("--help") || argumentos.includes("-h"),
        apenasSimulacao: argumentos.includes("--dry-run"),
        diretorioBase: lerOpcao(argumentos, "--base", DIRETORIO_RAIZ),
    };
}

function exibirAjuda() {
    exibirAjudaComando({
        comandoSgc: "backend java corrigir-fqn",
        scriptDireto: "backend/java-corrigir-fqn.js",
        descricao: "Substitui nomes totalmente qualificados por imports em arquivos Java.",
        opcoes: [
            "--dry-run           Apenas mostra os arquivos que seriam alterados.",
            "--base <diretorio>  Usa uma raiz de backend alternativa.",
            "--help, -h          Exibe esta ajuda.",
        ],
        exemplos: [
            "npx tsx toolkit/sgc.ts backend java corrigir-fqn --dry-run",
            "npx tsx toolkit/sgc.ts backend java corrigir-fqn --base /tmp/backend",
        ],
    });
}

function principal(argumentos = process.argv.slice(2)) {
    const opcoes = lerArgumentos(argumentos);
    if (opcoes.ajuda) {
        exibirAjuda();
        return;
    }

    const raizBackend = encontrarRaizBackend(opcoes.diretorioBase);
    let totalArquivosAnalisados = 0;
    let totalArquivosAtualizados = 0;

    escreverLinha("Procurando FQNs no projeto...");
    escreverLinha(`Raiz do backend resolvida: ${raizBackend}`);

    DIRETORIOS_ALVO.forEach((diretorioRelativo) => {
        const diretorioAlvo = path.join(raizBackend, diretorioRelativo);
        if (!fs.existsSync(diretorioAlvo)) {
            escreverLinha(`Diretorio nao encontrado: ${diretorioAlvo}`);
            return;
        }

        escreverLinha(`Processando diretorio: ${diretorioAlvo}`);
        const pilha = [diretorioAlvo];
        while (pilha.length > 0) {
            const diretorioAtual = pilha.pop();
            const entradas = fs.readdirSync(diretorioAtual, {withFileTypes: true});
            entradas.forEach((entrada) => {
                const caminhoCompleto = path.join(diretorioAtual, entrada.name);
                if (entrada.isDirectory()) {
                    pilha.push(caminhoCompleto);
                    return;
                }

                if (!entrada.name.endsWith(".java")) {
                    return;
                }

                totalArquivosAnalisados++;
                if (processarArquivo(caminhoCompleto, opcoes.apenasSimulacao)) {
                    totalArquivosAtualizados++;
                }
            });
        }
    });

    escreverLinha(`Total de arquivos analisados: ${totalArquivosAnalisados}`);
    escreverLinha(`Total de arquivos atualizados: ${totalArquivosAtualizados}`);
}

if (ehEntradaPrincipal(import.meta.url)) {
    try {
        principal();
    } catch (erro) {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(`Erro ao ajustar FQNs: ${mensagem}`);
        process.exitCode = 1;
    }
}

export {
    principal,
};
