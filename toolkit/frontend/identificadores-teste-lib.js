import fs from "node:fs";
import path from "node:path";
import {globby} from "globby";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";

const PADRAO_IDENTIFICADOR = /(^|[\s<])(:?)(data-test-codigo|test-codigo|data-test-id|test-id|data-testid)=("([^"]*)"|'([^']*)')/gm;

function obterDiretorioBusca(argumentos = [], diretorioBase = DIRETORIO_RAIZ) {
    const diretorioInformado = lerOpcao(argumentos, "--dir", null);
    return diretorioInformado
        ? path.resolve(diretorioBase, diretorioInformado)
        : resolverCaminhoConfigurado("frontendCodigo", diretorioBase);
}

function normalizarCaminhoArquivo(caminhoArquivo, diretorioBase, diretorioBusca) {
    const relativoRaiz = path.relative(diretorioBase, caminhoArquivo).replaceAll("\\", "/");
    if (!relativoRaiz.startsWith("../") && relativoRaiz !== "..") {
        return relativoRaiz;
    }
    return path.relative(diretorioBusca, caminhoArquivo).replaceAll("\\", "/");
}

async function coletarIdentificadores(diretorioBusca, diretorioBase = DIRETORIO_RAIZ) {
    if (!fs.existsSync(diretorioBusca)) {
        throw new Error(`Diretorio frontend nao encontrado: ${path.relative(diretorioBase, diretorioBusca)}`);
    }

    const padraoVue = path.join(diretorioBusca, "**/*.vue").replace(/\\/g, "/");
    const caminhosArquivos = await globby(padraoVue, {absolute: true});
    const identificadores = [];

    for (const caminhoArquivo of caminhosArquivos) {
        const conteudo = fs.readFileSync(caminhoArquivo, "utf8");
        PADRAO_IDENTIFICADOR.lastIndex = 0;
        let correspondencia;
        while ((correspondencia = PADRAO_IDENTIFICADOR.exec(conteudo)) !== null) {
            const dinamico = correspondencia[2] === ":";
            let valor = correspondencia[5] ?? correspondencia[6] ?? "";
            if (dinamico) {
                const literal = valor.match(/^["']([^"']+)["']$/);
                if (!literal) {
                    continue;
                }
                valor = literal[1];
            }

            identificadores.push({
                arquivo: normalizarCaminhoArquivo(caminhoArquivo, diretorioBase, diretorioBusca),
                atributo: `${dinamico ? ":" : ""}${correspondencia[3]}`,
                valor,
            });
        }
    }

    return {
        diretorioBusca,
        identificadores,
    };
}

export {
    coletarIdentificadores,
    obterDiretorioBusca,
};
