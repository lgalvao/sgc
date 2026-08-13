
import fs from "node:fs/promises";
import path from "node:path";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../lib/execucao.js";
import {escreverErro, escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {URL_OPENAPI_PADRAO, resolverCaminhoArquivoOpenapi, resolverCaminhosOpenapi} from "./contratos-openapi-caminhos.js";

interface OpcoesExportarOpenapi {
    base?: string;
    url?: string;
    saida?: string;
}

interface DocumentoOpenapi {
    [chave: string]: unknown;
}

interface ResultadoExportacaoOpenapi {
    base: string;
    url: string;
    saida: string;
    titulo: string | null;
    versao: string | null;
    quantidadeRotas: number;
}

function ehObjeto(valor: unknown): valor is DocumentoOpenapi {
    return typeof valor === "object" && valor !== null && !Array.isArray(valor);
}

function obterTexto(valor: unknown): string | null {
    return typeof valor === "string" ? valor : null;
}

async function exportarOpenapi({base = DIRETORIO_RAIZ, url = URL_OPENAPI_PADRAO, saida}: OpcoesExportarOpenapi = {}): Promise<ResultadoExportacaoOpenapi> {
    const baseResolvida = path.resolve(base ?? DIRETORIO_RAIZ);
    const saidaResolvida = resolverCaminhoArquivoOpenapi(
        baseResolvida,
        saida ?? resolverCaminhosOpenapi(baseResolvida).caminhoAtual
    );
    const resposta = await fetch(url, {
        headers: {
            Accept: "application/json"
        }
    });

    if (!resposta.ok) {
        throw new Error(`Falha ao buscar OpenAPI em ${url}: HTTP ${resposta.status}`);
    }

    const json = await resposta.json() as unknown;
    if (!ehObjeto(json)) {
        throw new Error("O endpoint OpenAPI retornou um JSON que nao representa um documento.");
    }

    const info = ehObjeto(json.info) ? json.info : {};
    const rotas = ehObjeto(json.paths) ? json.paths : {};
    await fs.mkdir(path.dirname(saidaResolvida), {recursive: true});
    await fs.writeFile(saidaResolvida, `${JSON.stringify(json, null, 2)}\n`, "utf-8");

    return {
        base: baseResolvida,
        url,
        saida: saidaResolvida,
        titulo: obterTexto(info.title),
        versao: obterTexto(info.version),
        quantidadeRotas: Object.keys(rotas).length
    };
}

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "integracao contratos exportar-openapi",
            scriptDireto: "integracao/contratos-exportar-openapi.ts",
            descricao: "Busca o documento OpenAPI da aplicação em execução e grava uma fotografia local para auditorias de contrato.",
            opcoes: [
                "--base <diretorio>   Base do projeto que receberá os artefatos.",
                "--url <url>          URL do endpoint OpenAPI (padrão: http://127.0.0.1:10000/api-docs).",
                "--saida <arquivo>    Caminho do arquivo JSON a ser gerado.",
                "--json               Emite o resultado em JSON."
            ],
            exemplos: [
                "npx tsx toolkit/sgc.ts integracao contratos exportar-openapi",
                "npx tsx toolkit/sgc.ts integracao contratos exportar-openapi --url http://127.0.0.1:10000/api-docs",
                "npx tsx toolkit/sgc.ts integracao contratos exportar-openapi --saida /tmp/sgc-openapi.json --json"
            ]
        });
        return;
    }

    const emitirJson = argumentos.includes("--json");
    const base = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const url = lerOpcao(argumentos, "--url", URL_OPENAPI_PADRAO) ?? URL_OPENAPI_PADRAO;
    const saida = lerOpcao(argumentos, "--saida", resolverCaminhosOpenapi(base).caminhoAtual) ?? resolverCaminhosOpenapi(base).caminhoAtual;

    if (!emitirJson) {
        imprimirCabecalho("EXPORTACAO DO OPENAPI");
        escreverLinha(`Origem: ${pc.dim(url)}`);
        escreverLinha(`Saida: ${pc.dim(saida)}`);
    }

    try {
        const resultado = await exportarOpenapi({base, url, saida});
        if (emitirJson) {
            imprimirJson(resultado);
        } else {
            escreverLinha(pc.green(`OpenAPI exportado com sucesso.`));
            escreverLinha(`Titulo: ${resultado.titulo ?? "-"}`);
            escreverLinha(`Versao: ${resultado.versao ?? "-"}`);
            escreverLinha(`Rotas: ${resultado.quantidadeRotas}`);
        }
    } catch (erro) {
        escreverErro(`Erro ao exportar OpenAPI: ${erro instanceof Error ? erro.message : String(erro)}\n`);
        escreverErro("Dica: execute o backend com perfil `e2e` ou informe `--url` para uma instância já ativa.\n");
        process.exitCode = 1;
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}

export {
    exportarOpenapi,
    principal
};
