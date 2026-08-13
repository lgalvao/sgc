import {readFileSync} from "node:fs";
import path from "node:path";
import type {PoliticaClassificacaoTestes} from "./biblioteca/testes-analisar-regras.js";

const CAMPOS_POLITICA = [
    "anotacoesContrato",
    "nomesModelosEstruturais",
    "prefixosModelosEstruturais",
    "sufixosModelosEstruturais",
    "nomesOutrosEstruturais",
    "prefixosOutrosEstruturais",
    "sufixosOutrosEstruturais",
    "caminhosOutrosEstruturais"
] as const;

function ehRegistro(valor: unknown): valor is Record<string, unknown> {
    return typeof valor === "object" && valor !== null && !Array.isArray(valor);
}

function validarListaPolitica(valor: unknown, caminho: string): string[] {
    if (!Array.isArray(valor) || valor.some(item => typeof item !== "string" || item.trim() === "")) {
        throw new Error(`A política de testes.${caminho} deve ser uma lista de textos não vazios.`);
    }
    return [...valor];
}

function carregarPoliticaClassificacao(caminhoArquivo: string): PoliticaClassificacaoTestes {
    const caminhoResolvido = path.resolve(caminhoArquivo);
    const conteudo = JSON.parse(readFileSync(caminhoResolvido, "utf-8")) as unknown;
    if (!ehRegistro(conteudo)) {
        throw new Error(`A política de testes em ${caminhoResolvido} deve ser um objeto JSON.`);
    }

    const camposDesconhecidos = Object.keys(conteudo).filter(campo => !CAMPOS_POLITICA.includes(campo as typeof CAMPOS_POLITICA[number]));
    if (camposDesconhecidos.length > 0) {
        throw new Error(`A política de testes possui campo(s) desconhecido(s): ${camposDesconhecidos.join(", ")}.`);
    }

    return {
        anotacoesContrato: validarListaPolitica(conteudo.anotacoesContrato, "anotacoesContrato"),
        nomesModelosEstruturais: validarListaPolitica(conteudo.nomesModelosEstruturais, "nomesModelosEstruturais"),
        prefixosModelosEstruturais: validarListaPolitica(conteudo.prefixosModelosEstruturais, "prefixosModelosEstruturais"),
        sufixosModelosEstruturais: validarListaPolitica(conteudo.sufixosModelosEstruturais, "sufixosModelosEstruturais"),
        nomesOutrosEstruturais: validarListaPolitica(conteudo.nomesOutrosEstruturais, "nomesOutrosEstruturais"),
        prefixosOutrosEstruturais: validarListaPolitica(conteudo.prefixosOutrosEstruturais, "prefixosOutrosEstruturais"),
        sufixosOutrosEstruturais: validarListaPolitica(conteudo.sufixosOutrosEstruturais, "sufixosOutrosEstruturais"),
        caminhosOutrosEstruturais: validarListaPolitica(conteudo.caminhosOutrosEstruturais, "caminhosOutrosEstruturais")
    };
}

export {carregarPoliticaClassificacao};
