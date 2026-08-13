
import path from "node:path";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverErro, escreverLinha, imprimirCabecalho, imprimirJson} from "../biblioteca/saida.js";
import {exportarOpenapi} from "./contratos-openapi-motor.js";
import {resolverCaminhosOpenapi} from "./contratos-openapi-caminhos.js";

const URL_OPENAPI_SGC = "http://127.0.0.1:10000/api-docs";

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjudaComando({
            comandoToolkit: "integracao contratos exportar-openapi",
            descricao: "Busca o documento OpenAPI da aplicação em execução e grava uma fotografia local para auditorias de contrato.",
            opcoes: [
                "--base <diretorio>   Base do projeto que receberá os artefatos.",
                "--url <url>          URL do endpoint OpenAPI (padrão do SGC: http://127.0.0.1:10000/api-docs).",
                "--saida <arquivo>    Caminho do arquivo JSON a ser gerado.",
                "--json               Emite o resultado em JSON."
            ],
            exemplos: [
                "ferramentas integracao contratos exportar-openapi",
                "ferramentas integracao contratos exportar-openapi --url http://127.0.0.1:10000/api-docs",
                "ferramentas integracao contratos exportar-openapi --saida /tmp/sgc-openapi.json --json"
            ]
        });
        return;
    }

    const emitirJson = argumentos.includes("--json");
    const base = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const url = lerOpcao(argumentos, "--url", URL_OPENAPI_SGC) ?? URL_OPENAPI_SGC;
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
        escreverErro("Dica: execute o servidor com perfil `e2e` ou informe `--url` para uma instância já ativa.\n");
        process.exitCode = 1;
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}

export {
    principal
};
