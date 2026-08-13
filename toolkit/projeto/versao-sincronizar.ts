import path from "node:path";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {resolverNaRaiz} from "../biblioteca/caminhos.js";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import {escreverErro, escreverLinha} from "../biblioteca/saida.js";
import {sincronizarVersao, type AlvoVersao, type ResultadoSincronizacao} from "./versao-sincronizacao-motor.js";

function resolverAlvosVersao(diretorioBase: string): AlvoVersao[] {
    return [
        {caminho: "gradle.properties", formato: "propriedadesGradle"},
        {caminho: path.join(resolverCaminhoConfigurado("cliente", diretorioBase), "package.json"), formato: "manifestoNpm"}
    ];
}

function principal(argumentosInformados: string[] = process.argv.slice(2)): ResultadoSincronizacao | undefined {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        escreverLinha("Uso recomendado: npx tsx toolkit/ferramentas.ts projeto versao-sincronizar <versao> [--base <diretorio>] [--gravar]");
        escreverLinha("");
        escreverLinha("Atualiza gradle.properties e o package.json do cliente configurado; por padrão, apenas simula a alteração.");
        escreverLinha("--gravar             Persiste a versão nos arquivos encontrados.");
        escreverLinha("--base <diretorio>   Resolve os arquivos a partir de outra raiz.");
        return;
    }

    const novaVersao = argumentos[0];
    if (!novaVersao) {
        escreverErro("Uso recomendado: npx tsx toolkit/ferramentas.ts projeto versao-sincronizar <versao> [--base <diretorio>] [--gravar]\n");
        escreverErro("Execução direta: npx tsx toolkit/projeto/versao-sincronizar.ts <versao>\n");
        process.exitCode = 1;
        return;
    }

    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", resolverNaRaiz()) ?? resolverNaRaiz());
    const gravar = argumentos.includes("--gravar");
    const resultado = sincronizarVersao({
        novaVersao,
        diretorioBase,
        alvos: resolverAlvosVersao(diretorioBase),
        gravar
    });
    for (const arquivo of resultado.arquivosPendentes) {
        escreverLinha(`${gravar ? "[v]" : "[simulação]"} ${arquivo} ${gravar ? "atualizado" : "seria atualizado"} para ${novaVersao}`);
    }
    if (resultado.arquivosPendentes.length === 0) {
        escreverLinha(`Nenhum arquivo precisa ser atualizado para ${novaVersao}.`);
    }

    return resultado;
}

if (ehEntradaPrincipal(import.meta.url)) {
    try {
        principal();
    } catch (erro: unknown) {
        escreverErro(`Erro ao sincronizar versão: ${erro instanceof Error ? erro.message : String(erro)}\n`);
        process.exitCode = 1;
    }
}

export {
    principal
};
