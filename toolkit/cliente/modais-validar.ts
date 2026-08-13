import fs from "node:fs";
import path from "node:path";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverLinha, imprimirJson} from "../biblioteca/saida.js";

const CAMINHOS_PERMITIDOS_BMODAL = new Set([
    "components/comum/ModalPadrao.vue",
]);

interface ViolacaoModal {
    arquivo: string;
    linha: number;
    regra: "componente-com-bmodal-cru";
    motivo: string;
}

interface ResultadoValidacaoModais {
    resumo: {
        totalArquivos: number;
        totalViolacoes: number;
    };
    violacoes: ViolacaoModal[];
}

interface OpcoesValidacaoModais {
    base?: string;
}

function listarArquivosVue(diretorio: string): string[] {
    const entradas = fs.readdirSync(diretorio, {withFileTypes: true});
    return entradas.flatMap((entrada) => {
        const caminhoEntrada = path.join(diretorio, entrada.name);
        if (entrada.isDirectory()) {
            return listarArquivosVue(caminhoEntrada);
        }
        return entrada.name.endsWith(".vue") ? [caminhoEntrada] : [];
    });
}

function localizarLinha(conteudo: string, trecho: string): number | null {
    const indice = conteudo.indexOf(trecho);
    if (indice === -1) {
        return null;
    }
    return conteudo.slice(0, indice).split(/\r?\n/u).length;
}

function auditarArquivo(caminhoArquivo: string, diretorioBase: string, diretorioCodigo: string): ViolacaoModal[] {
    const caminhoRelativo = path.relative(diretorioBase, caminhoArquivo).replaceAll("\\", "/");
    const caminhoRelativoCodigo = path.relative(diretorioCodigo, caminhoArquivo).replaceAll("\\", "/");
    if (CAMINHOS_PERMITIDOS_BMODAL.has(caminhoRelativoCodigo)) {
        return [];
    }

    const conteudo = fs.readFileSync(caminhoArquivo, "utf8");
    if (!conteudo.includes("<BModal")) {
        return [];
    }

    return [{
        arquivo: caminhoRelativo,
        linha: localizarLinha(conteudo, "<BModal") ?? 1,
        regra: "componente-com-bmodal-cru",
        motivo: "Componentes devem compor ModalPadrao em vez de abrir BModal diretamente.",
    }];
}

async function executarValidacaoModais(opcoes: OpcoesValidacaoModais = {}): Promise<ResultadoValidacaoModais> {
    const diretorioBase = path.resolve(opcoes.base ?? DIRETORIO_RAIZ);
    const diretorioCodigo = resolverCaminhoConfigurado("codigoCliente", diretorioBase);
    const diretorioComponentes = path.join(diretorioCodigo, "components");
    const arquivosVue = listarArquivosVue(diretorioComponentes);
    const violacoes = arquivosVue.flatMap((arquivo) => auditarArquivo(arquivo, diretorioBase, diretorioCodigo));
    return {
        resumo: {
            totalArquivos: arquivosVue.length,
            totalViolacoes: violacoes.length,
        },
        violacoes,
    };
}

function imprimirResultado(resultado: ResultadoValidacaoModais): void {
    if (resultado.violacoes.length === 0) {
        escreverLinha(`${pc.green("✓")} Modais padronizados. Nenhum BModal cru encontrado fora de ModalPadrao.`);
        return;
    }

    escreverLinha(pc.red(`Foram encontradas ${resultado.violacoes.length} violacoes de padronizacao de modais:`));
    resultado.violacoes.forEach((violacao, indice) => {
        escreverLinha(`${indice + 1}. [${violacao.regra}] ${violacao.arquivo}:${violacao.linha}`);
        escreverLinha(`   ${violacao.motivo}`);
    });
}

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoToolkit: "cliente modais validar",
            descricao: "Valida que apenas ModalPadrao abre BModal diretamente no cliente.",
            opcoes: [
                "--json               Emite o resultado bruto em JSON.",
                "--base <diretorio>   Sobrescreve o diretorio base da validacao.",
            ],
            exemplos: [
                "ferramentas cliente modais validar",
                "ferramentas cliente modais validar --json",
            ],
        });
        return;
    }

    const resultado = await executarValidacaoModais({
        base: lerOpcao(argumentos, "--base", undefined),
    });

    if (emitirJson) {
        imprimirJson(resultado);
    } else {
        imprimirResultado(resultado);
    }

    if (resultado.violacoes.length > 0) {
        process.exitCode = 1;
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro na validacao de modais: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    executarValidacaoModais,
    principal,
};
