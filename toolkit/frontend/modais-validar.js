#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import {pathToFileURL} from "node:url";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";

const CAMINHOS_PERMITIDOS_BMODAL = new Set([
    "frontend/src/components/comum/ModalPadrao.vue",
]);

function lerOpcao(argumentos, nome) {
    const indice = argumentos.indexOf(nome);
    if (indice === -1) {
        return null;
    }
    return argumentos[indice + 1] ?? null;
}

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

async function main() {
    const args = process.argv.slice(2);
    const jsonMode = args.includes("--json");
    const helpMode = args.includes("--help") || args.includes("-h");

    if (helpMode) {
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
        process.exit(0);
    }

    const resultado = await executarValidacaoModais({
        base: lerOpcao(args, "--base"),
    });

    if (jsonMode) {
        imprimirJson(resultado);
    } else {
        imprimirResultado(resultado);
    }

    if (resultado.violacoes.length > 0) {
        process.exit(1);
    }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
    main().catch((erro) => {
        escreverLinha(pc.red(`Erro na validacao de modais: ${erro.message}`));
        process.exit(1);
    });
}

export {
    executarValidacaoModais,
};
