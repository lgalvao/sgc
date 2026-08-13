import fs from "node:fs";
import path from "node:path";
import {globby} from "globby";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";

const PADRAO_IDENTIFICADOR = /(^|[\s<])(:?)(data-test-codigo|test-codigo|data-test-id|test-id|data-testid)=("([^"]*)"|'([^']*)')/gm;

interface IdentificadorTeste {
    arquivo: string;
    atributo: string;
    valor: string;
}

interface ResultadoColetaIdentificadores {
    diretorioBusca: string;
    identificadores: IdentificadorTeste[];
}

function obterDiretorioBusca(argumentos: string[] = [], diretorioBase: string = DIRETORIO_RAIZ): string {
    const diretorioInformado = lerOpcao(argumentos, "--diretorio", undefined);
    return diretorioInformado
        ? path.resolve(diretorioBase, diretorioInformado)
        : resolverCaminhoConfigurado("frontendCodigo", diretorioBase);
}

function normalizarCaminhoArquivo(caminhoArquivo: string, diretorioBase: string, diretorioBusca: string): string {
    const relativoRaiz = path.relative(diretorioBase, caminhoArquivo).replaceAll("\\", "/");
    if (!relativoRaiz.startsWith("../") && relativoRaiz !== "..") {
        return relativoRaiz;
    }
    return path.relative(diretorioBusca, caminhoArquivo).replaceAll("\\", "/");
}

async function coletarIdentificadores(
    diretorioBusca: string,
    diretorioBase: string = DIRETORIO_RAIZ
): Promise<ResultadoColetaIdentificadores> {
    if (!fs.existsSync(diretorioBusca)) {
        throw new Error(`Diretorio frontend nao encontrado: ${path.relative(diretorioBase, diretorioBusca)}`);
    }

    const padraoVue = path.join(diretorioBusca, "**/*.vue").replace(/\\/g, "/");
    const caminhosArquivos = await globby(padraoVue, {absolute: true});
    const identificadores: IdentificadorTeste[] = [];

    for (const caminhoArquivo of caminhosArquivos) {
        const conteudo = fs.readFileSync(caminhoArquivo, "utf8");
        PADRAO_IDENTIFICADOR.lastIndex = 0;
        let correspondencia: RegExpExecArray | null;
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
    type IdentificadorTeste,
    type ResultadoColetaIdentificadores,
};
