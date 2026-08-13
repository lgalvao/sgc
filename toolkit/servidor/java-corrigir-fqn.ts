// Correção explícita de nomes totalmente qualificados em fontes Java.
import fs from "node:fs";
import path from "node:path";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";
import {NOME_ARQUIVO_CONFIGURACAO, resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverLinha} from "../biblioteca/saida.js";

const DIRETORIOS_ALVO = ["src/test/java", "src/main/java"];
const PREFIXO_PACOTE = "package ";
const PREFIXO_IMPORT = "import ";
const PREFIXO_ESTATICO = "static ";
const PADRAO_FQN = /("[^"]*")|(\b([a-z]\w*(?:\.[a-z]\w*)+)\.([A-Z]\w*)\b)/g;

interface AnaliseImports {
    pacoteAtual: string | null;
    importsExistentes: Map<string, string>;
    indicesLinhasImport: number[];
}

interface DecisaoSubstituicao {
    substituicao: string;
    alterado: boolean;
}

interface AnaliseLinhas {
    linhasModificadas: string[];
    importsNovos: Set<string>;
    temAlteracoes: boolean;
}

interface OpcoesCorretorFqn {
    ajuda: boolean;
    gravar: boolean;
    diretorioBase: string;
}

type CorrespondenciaFqn = [
    correspondenciaCompleta: string,
    trechoEntreAspas: string | undefined,
    fqn: string | undefined,
    pacote: string | undefined,
    classe: string | undefined,
    posicao: number,
    conteudo: string
];

function deveIgnorarFqn(partePacote: string, parteClasse: string): boolean {
    if (parteClasse === "Assertions") {
        return true;
    }
    return partePacote === "java.lang";
}

function analisarImports(linhas: string[]): AnaliseImports {
    let pacoteAtual: string | null = null;
    const importsExistentes = new Map<string, string>();
    const indicesLinhasImport: number[] = [];

    linhas.forEach((linha, indice) => {
        const linhaLimpa = linha.trim();
        if (linhaLimpa.startsWith(PREFIXO_PACOTE)) {
            pacoteAtual = linhaLimpa.split("//")[0].replace(PREFIXO_PACOTE, "").replace(";", "").trim();
            return;
        }

        if (!linhaLimpa.startsWith(PREFIXO_IMPORT)) {
            return;
        }

        indicesLinhasImport.push(indice);
        let importLimpo = linhaLimpa.split("//")[0].replace(PREFIXO_IMPORT, "").replace(";", "").trim();
        if (importLimpo.startsWith(PREFIXO_ESTATICO)) {
            importLimpo = importLimpo.replace(PREFIXO_ESTATICO, "").trim();
        }

        const nomeSimples = importLimpo.split(".").pop();
        if (nomeSimples && nomeSimples !== "*") {
            importsExistentes.set(nomeSimples, importLimpo);
        }
    });

    return {pacoteAtual, importsExistentes, indicesLinhasImport};
}

function obterPosicaoInsercao(linhas: string[], indicesLinhasImport: number[]): number {
    if (indicesLinhasImport.length > 0) {
        return indicesLinhasImport[indicesLinhasImport.length - 1] + 1;
    }

    for (let indice = 0; indice < linhas.length; indice++) {
        if (linhas[indice].trim().startsWith(PREFIXO_PACOTE)) {
            return indice + 1;
        }
    }

    return 0;
}

function existeColisao(nomeSimples: string, fqn: string, importsNovos: Set<string>): boolean {
    return [...importsNovos].some((item) => item.split(".").pop() === nomeSimples && item !== fqn);
}

function determinarSubstituicao(
    correspondencia: CorrespondenciaFqn,
    pacoteAtual: string | null,
    importsExistentes: Map<string, string>,
    importsNovos: Set<string>
): DecisaoSubstituicao {
    if (correspondencia[1]) {
        return {substituicao: correspondencia[1], alterado: false};
    }

    const correspondenciaCompleta = correspondencia[2];
    const pacote = correspondencia[3];
    const classe = correspondencia[4];
    if (!correspondenciaCompleta || !pacote || !classe) {
        return {substituicao: correspondencia[0], alterado: false};
    }
    const fqn = `${pacote}.${classe}`;

    let deveSubstituir = false;

    if (deveIgnorarFqn(pacote, classe)) {
        deveSubstituir = true;
    } else if (pacoteAtual && pacote === pacoteAtual) {
        deveSubstituir = true;
    } else if (importsExistentes.has(classe)) {
        deveSubstituir = importsExistentes.get(classe) === fqn;
    } else if (!existeColisao(classe, fqn, importsNovos)) {
        importsNovos.add(fqn);
        deveSubstituir = true;
    }

    return {
        substituicao: deveSubstituir ? classe : correspondenciaCompleta,
        alterado: deveSubstituir
    };
}

function analisarLinhas(
    linhas: string[],
    pacoteAtual: string | null,
    importsExistentes: Map<string, string>
): AnaliseLinhas {
    const importsNovos = new Set<string>();
    const linhasModificadas: string[] = [];
    let temAlteracoes = false;

    linhas.forEach((linha) => {
        const linhaLimpa = linha.trim();
        if (linhaLimpa.startsWith(PREFIXO_PACOTE) || linhaLimpa.startsWith(PREFIXO_IMPORT) || linhaLimpa.startsWith("//") || linhaLimpa.startsWith("*")) {
            linhasModificadas.push(linha);
            return;
        }

        const novaLinha = linha.replace(PADRAO_FQN, (...argumentos: CorrespondenciaFqn) => {
            const {substituicao, alterado} = determinarSubstituicao(argumentos, pacoteAtual, importsExistentes, importsNovos);
            if (alterado) {
                temAlteracoes = true;
            }
            return substituicao;
        });
        linhasModificadas.push(novaLinha);
    });

    return {linhasModificadas, importsNovos, temAlteracoes};
}

function processarArquivo(caminhoArquivo: string, gravar = false): boolean {
    const linhas: string[] = fs.readFileSync(caminhoArquivo, "utf-8").split(/\r?\n/).map((linha, indice, todas) => (
        indice < todas.length - 1 ? `${linha}\n` : linha
    ));
    const {pacoteAtual, importsExistentes, indicesLinhasImport} = analisarImports(linhas);
    const {linhasModificadas, importsNovos, temAlteracoes} = analisarLinhas(linhas, pacoteAtual, importsExistentes);

    if (!temAlteracoes && importsNovos.size === 0) {
        return false;
    }

    const posicaoInsercao = obterPosicaoInsercao(linhas, indicesLinhasImport);
    const importsOrdenados = [...importsNovos].toSorted((a, b) => a.localeCompare(b, "pt-BR"));
    const saidaFinal: string[] = [];

    if (posicaoInsercao === 0) {
        importsOrdenados.forEach((item) => saidaFinal.push(`${PREFIXO_IMPORT}${item};\n`));
    }

    linhasModificadas.forEach((linha, indice) => {
        saidaFinal.push(linha);
        if (indice === posicaoInsercao - 1) {
            importsOrdenados.forEach((item) => saidaFinal.push(`${PREFIXO_IMPORT}${item};\n`));
        }
    });

    if (gravar) {
        fs.writeFileSync(caminhoArquivo, saidaFinal.join(""), "utf-8");
    }

    escreverLinha(`${gravar ? "" : "[simulação] "}${gravar ? "Atualizado" : "Seria atualizado"}: ${caminhoArquivo} (${importsOrdenados.length} novo(s) import(s))`);
    return true;
}

function encontrarRaizServidor(diretorioBase: string = DIRETORIO_RAIZ): string {
    const candidatos = [path.resolve(diretorioBase), path.resolve(diretorioBase, "backend")];
    return candidatos.find((candidato) => fs.existsSync(path.join(candidato, "src"))) ?? path.resolve(diretorioBase);
}

function obterDiretoriosAlvo(diretorioBase: string, raizServidor: string): string[] {
    if (fs.existsSync(path.join(diretorioBase, NOME_ARQUIVO_CONFIGURACAO))) {
        return [
            resolverCaminhoConfigurado("testesServidor", diretorioBase),
            resolverCaminhoConfigurado("codigoServidor", diretorioBase)
        ];
    }

    return DIRETORIOS_ALVO.map(diretorioRelativo => path.join(raizServidor, diretorioRelativo));
}

function lerArgumentos(argumentos: string[]): OpcoesCorretorFqn {
    return {
        ajuda: argumentos.includes("--help") || argumentos.includes("-h"),
        gravar: argumentos.includes("--gravar"),
        diretorioBase: lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ,
    };
}

function exibirAjuda(): void {
    exibirAjudaComando({
        comandoToolkit: "servidor java corrigir-fqn",
        scriptDireto: "servidor/java-corrigir-fqn.ts",
        descricao: "Substitui nomes totalmente qualificados por imports em arquivos Java.",
        opcoes: [
            "--gravar            Persiste as substituições nos arquivos Java.",
            "--base <diretorio>  Usa uma raiz de servidor alternativa.",
            "--help, -h          Exibe esta ajuda.",
        ],
        exemplos: [
            "npx tsx toolkit/ferramentas.ts servidor java corrigir-fqn",
            "npx tsx toolkit/ferramentas.ts servidor java corrigir-fqn --gravar",
            "npx tsx toolkit/ferramentas.ts servidor java corrigir-fqn --base /tmp/backend",
        ],
    });
}

function principal(argumentos: string[] = process.argv.slice(2)): void {
    const opcoes = lerArgumentos(validarArgumentosEntradaDireta(import.meta.url, argumentos));
    if (opcoes.ajuda) {
        exibirAjuda();
        return;
    }

    const raizServidor = encontrarRaizServidor(opcoes.diretorioBase);
    const diretoriosAlvo = obterDiretoriosAlvo(opcoes.diretorioBase, raizServidor);
    let totalArquivosAnalisados = 0;
    let totalArquivosAtualizados = 0;

    escreverLinha("Procurando FQNs no projeto...");
    escreverLinha(`Raiz do servidor resolvida: ${raizServidor}`);

    diretoriosAlvo.forEach((diretorioAlvo) => {
        if (!fs.existsSync(diretorioAlvo)) {
            escreverLinha(`Diretorio nao encontrado: ${diretorioAlvo}`);
            return;
        }

        escreverLinha(`Processando diretorio: ${diretorioAlvo}`);
        const pilha = [diretorioAlvo];
        while (pilha.length > 0) {
            const diretorioAtual = pilha.pop();
            if (!diretorioAtual) {
                continue;
            }
            const entradas = fs.readdirSync(diretorioAtual, {withFileTypes: true});
            entradas.forEach((entrada) => {
                const caminhoCompleto = path.join(diretorioAtual, entrada.name);
                if (entrada.isDirectory()) {
                    pilha.push(caminhoCompleto);
                    return;
                }

                if (!entrada.name.endsWith(".java")) {
                    return;
                }

                totalArquivosAnalisados++;
                if (processarArquivo(caminhoCompleto, opcoes.gravar)) {
                    totalArquivosAtualizados++;
                }
            });
        }
    });

    escreverLinha(`Total de arquivos analisados: ${totalArquivosAnalisados}`);
    escreverLinha(`Total de arquivos ${opcoes.gravar ? "atualizados" : "com alterações"}: ${totalArquivosAtualizados}`);
}

if (ehEntradaPrincipal(import.meta.url)) {
    try {
        principal();
    } catch (erro) {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(`Erro ao ajustar FQNs: ${mensagem}`);
        process.exitCode = 1;
    }
}

export {
    principal,
};
