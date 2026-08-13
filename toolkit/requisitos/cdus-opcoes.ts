import path from "node:path";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";

interface OpcoesCdu {
    emitirJson: boolean;
    base: string;
    secoes?: string[];
}

function obterOpcoesCdu(argumentos: readonly string[] = process.argv.slice(2)): OpcoesCdu {
    let emitirJson = false;
    let baseInformada: string | undefined;
    let secoes: string[] | undefined;

    for (let indice = 0; indice < argumentos.length; indice += 1) {
        const argumento = argumentos[indice];
        if (argumento === "--json") {
            emitirJson = true;
            continue;
        }

        if (argumento === "--base" || argumento === "--secoes") {
            const valor = argumentos[indice + 1];
            if (!valor || valor.startsWith("--")) {
                throw new Error(`A opção ${argumento} exige um valor.`);
            }
            indice += 1;
            if (argumento === "--base") {
                baseInformada = valor;
            } else {
                secoes = valor.split(",").map(secao => secao.trim()).filter(Boolean);
            }
            continue;
        }

        throw new Error(`Opção ou argumento CDU desconhecido: ${argumento}`);
    }

    const base = baseInformada ? path.resolve(baseInformada) : DIRETORIO_RAIZ;
    return {emitirJson, base, secoes};
}

export {obterOpcoesCdu};
