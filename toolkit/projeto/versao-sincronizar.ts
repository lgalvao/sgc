import {existsSync, readFileSync, writeFileSync} from "node:fs";
import path from "node:path";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {escreverLinha} from "../lib/saida.js";

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
    const caminhoGradle = resolver("gradle.properties");
    if (existsSync(caminhoGradle)) {
        const conteudo = readFileSync(caminhoGradle, "utf-8");
        if (/^version=.*$/m.test(conteudo)) {
            const conteudoAtualizado = conteudo.replace(/^version=.*$/m, `version=${novaVersao}`);
            if (conteudoAtualizado !== conteudo) {
                arquivosPendentes.push("gradle.properties");
                if (gravar) {
                    writeFileSync(caminhoGradle, conteudoAtualizado, "utf-8");
                    arquivosAtualizados.push("gradle.properties");
                }
            }
        }
    }

    const caminhoFrontend = resolver("frontend/package.json");
    if (existsSync(caminhoFrontend)) {
        const pacote = JSON.parse(readFileSync(caminhoFrontend, "utf-8")) as Record<string, unknown>;
        if (pacote.version !== novaVersao) {
            arquivosPendentes.push("frontend/package.json");
            if (gravar) {
                pacote.version = novaVersao;
                writeFileSync(caminhoFrontend, `${JSON.stringify(pacote, null, 2)}\n`, "utf-8");
                arquivosAtualizados.push("frontend/package.json");
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

function principal(argumentos: string[] = process.argv.slice(2)): ResultadoSincronizacao | undefined {
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        escreverLinha("Uso recomendado: npx tsx toolkit/sgc.ts projeto versao-sincronizar <versao> [--base <diretorio>] [--gravar]");
        escreverLinha("");
        escreverLinha("Atualiza gradle.properties e frontend/package.json; por padrão, apenas simula a alteração.");
        escreverLinha("--gravar             Persiste a versão nos arquivos encontrados.");
        escreverLinha("--base <diretorio>   Resolve os arquivos a partir de outra raiz.");
        return;
    }

    const novaVersao = argumentos[0];
    if (!novaVersao) {
        process.stderr.write("Uso recomendado: npx tsx toolkit/sgc.ts projeto versao-sincronizar <versao> [--base <diretorio>] [--gravar]\n");
        process.stderr.write("Execução direta: npx tsx toolkit/projeto/versao-sincronizar.ts <versao>\n");
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
        process.stderr.write(`Erro ao sincronizar versão: ${erro instanceof Error ? erro.message : String(erro)}\n`);
        process.exitCode = 1;
    }
}

export {
    principal,
    sincronizarVersao
};
