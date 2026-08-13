import {existsSync, readFileSync, writeFileSync} from "node:fs";
import path from "node:path";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {resolverNaRaiz} from "../biblioteca/caminhos.js";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import {escreverErro, escreverLinha} from "../biblioteca/saida.js";

interface ResultadoSincronizacao {
    novaVersao: string;
    arquivosAtualizados: string[];
    arquivosPendentes: string[];
    gravado: boolean;
}

function sincronizarVersao(
    novaVersao: string | undefined,
    diretorioBase = resolverNaRaiz(),
    gravar = false
): ResultadoSincronizacao {
    if (!novaVersao) {
        throw new Error("Informe a versão que deve ser sincronizada.");
    }

    const arquivosAtualizados: string[] = [];
    const arquivosPendentes: string[] = [];

    const resolver = (caminho: string): string => path.resolve(diretorioBase, caminho);
    const caminhoRelativo = (caminho: string): string => path.relative(diretorioBase, caminho).replaceAll(path.sep, "/");
    const caminhoGradle = resolver("gradle.properties");
    const nomeGradle = caminhoRelativo(caminhoGradle);
    if (existsSync(caminhoGradle)) {
        const conteudo = readFileSync(caminhoGradle, "utf-8");
        if (/^version=.*$/m.test(conteudo)) {
            const conteudoAtualizado = conteudo.replace(/^version=.*$/m, `version=${novaVersao}`);
            if (conteudoAtualizado !== conteudo) {
                arquivosPendentes.push(nomeGradle);
                if (gravar) {
                    writeFileSync(caminhoGradle, conteudoAtualizado, "utf-8");
                    arquivosAtualizados.push(nomeGradle);
                }
            }
        }
    }

    const caminhoFrontend = path.join(resolverCaminhoConfigurado("frontend", diretorioBase), "package.json");
    const nomeFrontend = caminhoRelativo(caminhoFrontend);
    if (existsSync(caminhoFrontend)) {
        const pacote = JSON.parse(readFileSync(caminhoFrontend, "utf-8")) as Record<string, unknown>;
        if (pacote.version !== novaVersao) {
            arquivosPendentes.push(nomeFrontend);
            if (gravar) {
                pacote.version = novaVersao;
                writeFileSync(caminhoFrontend, `${JSON.stringify(pacote, null, 2)}\n`, "utf-8");
                arquivosAtualizados.push(nomeFrontend);
            }
        }
    }

    return {
        novaVersao,
        arquivosAtualizados,
        arquivosPendentes,
        gravado: gravar
    };
}

function principal(argumentosInformados: string[] = process.argv.slice(2)): ResultadoSincronizacao | undefined {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        escreverLinha("Uso recomendado: npx tsx toolkit/sgc.ts projeto versao-sincronizar <versao> [--base <diretorio>] [--gravar]");
        escreverLinha("");
        escreverLinha("Atualiza gradle.properties e o package.json do frontend configurado; por padrão, apenas simula a alteração.");
        escreverLinha("--gravar             Persiste a versão nos arquivos encontrados.");
        escreverLinha("--base <diretorio>   Resolve os arquivos a partir de outra raiz.");
        return;
    }

    const novaVersao = argumentos[0];
    if (!novaVersao) {
        escreverErro("Uso recomendado: npx tsx toolkit/sgc.ts projeto versao-sincronizar <versao> [--base <diretorio>] [--gravar]\n");
        escreverErro("Execução direta: npx tsx toolkit/projeto/versao-sincronizar.ts <versao>\n");
        process.exitCode = 1;
        return;
    }

    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", resolverNaRaiz()) ?? resolverNaRaiz());
    const gravar = argumentos.includes("--gravar");
    const resultado = sincronizarVersao(novaVersao, diretorioBase, gravar);
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
    principal,
    sincronizarVersao
};
