#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";

const CAMINHOS_PERMITIDOS_BMODAL = new Set([
    "frontend/src/components/comum/ModalPadrao.vue",
]);

function listarArquivosVue(diretorio) {
    const entradas = fs.readdirSync(diretorio, {withFileTypes: true});
    return entradas.flatMap((entrada) => {
        const caminhoEntrada = path.join(diretorio, entrada.name);
        if (entrada.isDirectory()) {
            return listarArquivosVue(caminhoEntrada);
        }
        return entrada.name.endsWith(".vue") ? [caminhoEntrada] : [];
    });
}

function localizarLinha(conteudo, trecho) {
    const indice = conteudo.indexOf(trecho);
    if (indice === -1) {
        return null;
    }
    return conteudo.slice(0, indice).split(/\r?\n/u).length;
}

function auditarArquivo(caminhoArquivo, diretorioBase) {
    const caminhoRelativo = path.relative(diretorioBase, caminhoArquivo).replaceAll("\\", "/");
    if (CAMINHOS_PERMITIDOS_BMODAL.has(caminhoRelativo)) {
        return [];
    }

    const conteudo = fs.readFileSync(caminhoArquivo, "utf8");
    if (!conteudo.includes("<BModal")) {
        return [];
    }

    return [{
        arquivo: caminhoRelativo,
        linha: localizarLinha(conteudo, "<BModal") ?? 1,
        regra: "componente-com-bmodal-cru",
        motivo: "Componentes devem compor ModalPadrao em vez de abrir BModal diretamente.",
    }];
}

async function executarValidacaoModais(opcoes = {}) {
    const diretorioBase = path.resolve(opcoes.base ?? DIRETORIO_RAIZ);
    const diretorioComponentes = path.join(diretorioBase, "frontend", "src", "components");
    const arquivosVue = listarArquivosVue(diretorioComponentes);
    const violacoes = arquivosVue.flatMap((arquivo) => auditarArquivo(arquivo, diretorioBase));
    return {
        resumo: {
            totalArquivos: arquivosVue.length,
            totalViolacoes: violacoes.length,
        },
        violacoes,
    };
}

function imprimirResultado(resultado) {
    if (resultado.violacoes.length === 0) {
        escreverLinha(`${pc.green("✓")} Modais padronizados. Nenhum BModal cru encontrado fora de ModalPadrao.`);
        return;
    }

    escreverLinha(pc.red(`Foram encontradas ${resultado.violacoes.length} violacoes de padronizacao de modais:`));
    resultado.violacoes.forEach((violacao, indice) => {
        escreverLinha(`${indice + 1}. [${violacao.regra}] ${violacao.arquivo}:${violacao.linha}`);
        escreverLinha(`   ${violacao.motivo}`);
    });
}

async function principal(argumentos = process.argv.slice(2)) {
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoSgc: "frontend modais validar",
            scriptDireto: "frontend/modais-validar.js",
            descricao: "Valida que apenas ModalPadrao abre BModal diretamente no frontend.",
            opcoes: [
                "--json               Emite o resultado bruto em JSON.",
                "--base <diretorio>   Sobrescreve o diretorio base da validacao.",
            ],
            exemplos: [
                "node toolkit/sgc.js frontend modais validar",
                "node toolkit/sgc.js frontend modais validar --json",
            ],
        });
        return;
    }

    const resultado = await executarValidacaoModais({
        base: lerOpcao(argumentos, "--base", undefined),
    });

    if (emitirJson) {
        imprimirJson(resultado);
    } else {
        imprimirResultado(resultado);
    }

    if (resultado.violacoes.length > 0) {
        process.exitCode = 1;
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro na validacao de modais: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    executarValidacaoModais,
    principal,
};
