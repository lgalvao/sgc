import {execFileSync} from "node:child_process";
import {existsSync, readFileSync} from "node:fs";
import path from "node:path";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {lerNumero, lerOpcao} from "../biblioteca/cli-opcoes.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import logger from "../biblioteca/logger.js";
import {escrever, escreverLinha} from "../biblioteca/saida.js";

interface NoArvore {
    nome: string;
    linhas: number;
    filhos: Record<string, NoArvore>;
    ehDiretorio: boolean;
}

interface OpcoesArvore {
    ajuda?: boolean;
    diretorioBase?: string;
    profundidadeMaxima?: number;
    minimoLinhas?: number;
    excluirTestes?: boolean;
}

interface ConectoresArvore {
    conector: string;
    prefixoFilho: string;
}

const PROFUNDIDADE_PADRAO = 3;
const MINIMO_LINHAS_PADRAO = 500;

function listarArquivosGit(diretorioBase = DIRETORIO_RAIZ): string[] {
    try {
        const saida = execFileSync("git", ["ls-files"], {
            cwd: diretorioBase,
            encoding: "utf-8",
            maxBuffer: 1024 * 1024 * 10,
            stdio: ["ignore", "pipe", "pipe"]
        });
        return saida.trim().split(/\r?\n/).filter(Boolean);
    } catch {
        throw new Error(`O diretorio informado nao e um repositorio Git acessivel: ${path.resolve(diretorioBase)}`);
    }
}

function contarLinhas(caminhoArquivo: string, diretorioBase: string): number {
    const caminhoAbsoluto = path.resolve(diretorioBase, caminhoArquivo);
    if (!existsSync(caminhoAbsoluto)) return 0;
    const conteudo = readFileSync(caminhoAbsoluto, "utf-8");
    return conteudo.split(/\r?\n/).length;
}

function construirArvore(listaArquivos: string[], diretorioBase = DIRETORIO_RAIZ): NoArvore {
    const raiz: NoArvore = {nome: ".", linhas: 0, filhos: {}, ehDiretorio: true};

    listaArquivos.forEach(caminhoArquivo => {
        const caminhoNormalizado = caminhoArquivo.replaceAll("\\", "/");
        const linhas = contarLinhas(caminhoNormalizado, diretorioBase);

        const partes = caminhoNormalizado.split("/");
        let atual = raiz;

        partes.forEach((parte, indice) => {
            const ehUltimo = indice === partes.length - 1;

            if (!atual.filhos[parte]) {
                atual.filhos[parte] = {
                    nome: parte,
                    linhas: 0,
                    filhos: {},
                    ehDiretorio: !ehUltimo
                };
            }
            atual = atual.filhos[parte];

            if (ehUltimo) {
                atual.linhas = linhas;
            }
        });
    });

    return raiz;
}

function calcularTotais(no: NoArvore): number {
    if (!no.ehDiretorio) {
        return no.linhas;
    }

    let soma = 0;
    for (const filho of Object.values(no.filhos)) {
        soma += calcularTotais(filho);
    }
    no.linhas = soma;
    return soma;
}

function obterConectoresArvore(ehRaiz: boolean, ehUltimo: boolean): ConectoresArvore {
    if (ehRaiz) {
        return {conector: "", prefixoFilho: ""};
    }
    return {
        conector: ehUltimo ? "└── " : "├── ",
        prefixoFilho: ehUltimo ? "    " : "│   "
    };
}

function imprimirNo(no: NoArvore, prefixo: string, conector: string): void {
    const resetar = "\x1b[0m";
    const azul = "\x1b[34m";
    const verde = "\x1b[32m";
    const amarelo = "\x1b[33m";

    const cor = no.ehDiretorio ? azul : verde;
    const icone = no.ehDiretorio ? "📁" : "📄";
    const linha = `${prefixo}${conector}${icone} ${cor}${no.nome}${resetar} ${amarelo}[${no.linhas.toLocaleString()}]${resetar}`;
    escreverLinha(linha);
}

function imprimirArvore(
    no: NoArvore,
    opcoes: OpcoesArvore,
    prefixo = "",
    ehUltimo = true,
    ehRaiz = true,
    profundidadeAtual = 0
): void {
    const {profundidadeMaxima, minimoLinhas} = opcoes;

    if (minimoLinhas !== undefined && no.linhas < minimoLinhas) return;

    const {conector, prefixoFilho} = obterConectoresArvore(ehRaiz, ehUltimo);

    const ehRaizPonto = ehRaiz && no.nome === ".";
    if (!ehRaizPonto) {
        imprimirNo(no, prefixo, conector);
    }

    if (!no.ehDiretorio) return;
    if (profundidadeMaxima !== undefined && profundidadeAtual >= profundidadeMaxima) return;

    let nomesFilhos = Object.keys(no.filhos);

    if (minimoLinhas !== undefined) {
        nomesFilhos = nomesFilhos.filter(nome => no.filhos[nome].linhas >= minimoLinhas);
    }

    nomesFilhos.sort((a, b) => {
        const diferencaLinhas = no.filhos[b].linhas - no.filhos[a].linhas;
        return diferencaLinhas === 0 ? a.localeCompare(b) : diferencaLinhas;
    });

    nomesFilhos.forEach((nome, indice) => {
        const ehUltimoFilho = indice === nomesFilhos.length - 1;
        imprimirArvore(no.filhos[nome], opcoes, prefixo + prefixoFilho, ehUltimoFilho, false, profundidadeAtual + 1);
    });
}

function lerOpcoes(argumentos: string[] = process.argv.slice(2)): OpcoesArvore {
    const opcoes: OpcoesArvore = {diretorioBase: lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ};

    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        escrever(`
Uso recomendado: ferramentas projeto arvore-linhas [opções]

Opções:
  --profundidade <n>    Limita a profundidade da árvore exibida (padrão: 3)
  --minimo-linhas <n>   Exibe apenas itens com no mínimo n linhas (padrão: 500)
  --excluir-testes      Exclui arquivos de teste da contagem e da árvore
  --base <diretorio>    Diretório Git analisado (padrão: diretório atual)
  --help, -h            Exibe esta mensagem de ajuda
\n`);
        return {ajuda: true};
    }

    opcoes.profundidadeMaxima = lerNumero(argumentos, "--profundidade", PROFUNDIDADE_PADRAO, {minimo: 0});
    opcoes.minimoLinhas = lerNumero(argumentos, "--minimo-linhas", MINIMO_LINHAS_PADRAO, {minimo: 0});

    if (argumentos.includes("--excluir-testes")) {
        opcoes.excluirTestes = true;
    }

    return opcoes;
}

const PADROES_TESTE: RegExp[] = [
    /\.spec\.(js|ts|vue)$/,
    /\.test\.(js|ts|vue)$/,
    /__tests__\//,
    /e2e\//,
    /\/src\/test\//
];

function ehArquivoTeste(caminhoArquivo: string): boolean {
    const caminhoNormalizado = caminhoArquivo.replaceAll("\\", "/");
    return PADROES_TESTE.some(padrao => padrao.test(caminhoNormalizado));
}

function principal(argumentos: string[] = process.argv.slice(2)): void {
    const argumentosValidados = validarArgumentosEntradaDireta(import.meta.url, argumentos);
    const opcoes = lerOpcoes(argumentosValidados);
    if (opcoes.ajuda) {
        return;
    }

    logger.info("Gerando árvore de contagem de linhas...");
    const diretorioBase = opcoes.diretorioBase ?? DIRETORIO_RAIZ;
    let listaArquivos = listarArquivosGit(diretorioBase);

    if (opcoes.excluirTestes) {
        const quantidadeOriginal = listaArquivos.length;
        listaArquivos = listaArquivos.filter(caminhoArquivo => !ehArquivoTeste(caminhoArquivo));
        logger.info(`Excluídos ${quantidadeOriginal - listaArquivos.length} arquivos de teste.`);
    }
    const arvore = construirArvore(listaArquivos, diretorioBase);
    calcularTotais(arvore);

    escreverLinha(`\x1b[1mProjeto: ${path.basename(path.resolve(diretorioBase))}\x1b[0m`);
    escreverLinha(`\x1b[1mTotal de Linhas: \x1b[33m${arvore.linhas.toLocaleString()}\x1b[0m`);
    escreverLinha();

    imprimirArvore(arvore, opcoes);
}

if (ehEntradaPrincipal(import.meta.url)) {
    try {
        principal();
    } catch (erro: unknown) {
        escreverLinha(`Erro ao gerar arvore de linhas: ${erro instanceof Error ? erro.message : String(erro)}`);
        process.exitCode = 1;
    }
}

export {
    construirArvore,
    calcularTotais,
    ehArquivoTeste,
    lerOpcoes,
    listarArquivosGit,
    principal
};
