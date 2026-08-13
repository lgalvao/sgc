import {existsSync, readFileSync, writeFileSync} from "node:fs";
import path from "node:path";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {escreverLinha} from "../lib/saida.js";

interface ResultadoSincronizacao {
    novaVersao: string;
    arquivosAtualizados: string[];
}

function sincronizarVersao(novaVersao: string | undefined, diretorioBase = resolverNaRaiz()): ResultadoSincronizacao {
    if (!novaVersao) {
        throw new Error("Informe a versão que deve ser sincronizada.");
    }

    const arquivosAtualizados: string[] = [];

    const resolver = (caminho: string): string => path.resolve(diretorioBase, caminho);
    const caminhoGradle = resolver("gradle.properties");
    if (existsSync(caminhoGradle)) {
        let conteudo = readFileSync(caminhoGradle, "utf-8");
        conteudo = conteudo.replace(/^version=.*$/m, `version=${novaVersao}`);
        writeFileSync(caminhoGradle, conteudo, "utf-8");
        arquivosAtualizados.push("gradle.properties");
    }

    const caminhoFrontend = resolver("frontend/package.json");
    if (existsSync(caminhoFrontend)) {
        const pacote = JSON.parse(readFileSync(caminhoFrontend, "utf-8")) as Record<string, unknown>;
        pacote.version = novaVersao;
        writeFileSync(caminhoFrontend, `${JSON.stringify(pacote, null, 2)}\n`, "utf-8");
        arquivosAtualizados.push("frontend/package.json");
    }

    return {
        novaVersao,
        arquivosAtualizados
    };
}

function principal(argumentos: string[] = process.argv.slice(2)): ResultadoSincronizacao | undefined {
    const novaVersao = argumentos[0];
    if (!novaVersao) {
        process.stderr.write("Uso recomendado: npx tsx toolkit/sgc.ts projeto versao-sincronizar <versao>\n");
        process.stderr.write("Execução direta: npx tsx toolkit/projeto/versao-sincronizar.ts <versao>\n");
        process.exitCode = 1;
        return;
    }

    const resultado = sincronizarVersao(novaVersao);
    for (const arquivo of resultado.arquivosAtualizados) {
        escreverLinha(`[v] ${arquivo} atualizado para ${novaVersao}`);
    }

    return resultado;
}

if (ehEntradaPrincipal(import.meta.url)) {
    try {
        principal();
    } catch (erro: unknown) {
        process.stderr.write(`Erro ao sincronizar versão: ${erro instanceof Error ? erro.message : String(erro)}\n`);
        process.exitCode = 1;
    }
}

export {
    principal,
    sincronizarVersao
};
