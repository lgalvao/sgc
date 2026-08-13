import fs from "node:fs/promises";
import path from "node:path";
import {parseStringPromise} from "xml2js";
import {DIRETORIO_RAIZ} from "../caminhos.js";
import {resolverCaminhoConfigurado} from "../configuracao.js";

interface AtributosXml {
    [nome: string]: string;
}

interface ContadorXml {
    $: AtributosXml;
}

interface LinhaXml {
    $: AtributosXml;
}

interface ArquivoFonteXml {
    $: {name: string};
    line?: LinhaXml[];
    counter?: ContadorXml[];
}

interface PacoteXml {
    $: {name: string};
    sourcefile?: ArquivoFonteXml[];
}

interface RelatorioJacocoXml {
    report: {
        counter?: ContadorXml[];
        package?: PacoteXml[];
    };
}

export interface ResumoContador {
    cobertos: number;
    perdidos: number;
    percentual: number;
}

export interface MetricasGlobais {
    linhas: ResumoContador;
    branches: ResumoContador;
    instrucoes: ResumoContador;
    metodos: ResumoContador;
    complexidade: ResumoContador;
}

export interface TotaisCobertura {
    totalArquivos: number;
    totalLinhas: number;
    linhasCobertas: number;
    totalBranches: number;
    branchesCobertos: number;
    coberturaGlobalLinhas: number;
    coberturaGlobalBranches: number;
}

export interface ClasseCobertura {
    nomePacote: string;
    nomeArquivo: string;
    nomeClasse: string;
    nome: string;
    totalLinhas: number;
    linhasCobertas: number;
    linhasPerdidas: number;
    linhasPerdidasLista: number[];
    linhasPercentual: number;
    coberturaLinhas: number;
    totalBranches: number;
    branchesCobertos: number;
    branchesPerdidos: number;
    branchesPerdidosLista: string[];
    branchesPercentual: number;
    coberturaBranches: number;
    complexidade: number;
    contadoresGlobais: MetricasGlobais;
}

export interface OpcoesCoberturaJacoco {
    diretorioBase?: string;
    incluirSemLacunas?: boolean;
    aplicarExclusoes?: boolean;
    padroesExclusao?: RegExp[];
    filtro?: string | null;
}

export interface ResultadoCoberturaJacoco extends MetricasGlobais {
    totais: TotaisCobertura;
    classes: ClasseCobertura[];
}

const PADROES_EXCLUSAO: RegExp[] = [
    /MapperImpl$/,
    /\.Sgc$/,
    /(?:^|\.).*Config(?:\..*)?$/,
    /(?:^|\.).*Configuration(?:\..*)?$/,
    /Properties$/,
    /Dto$/,
    /Request$/,
    /Response$/,
    /(?:^|\.)Erro.+$/,
    /Exception$/,
    /Repo$/,
    /\.model\.(Perfil|Usuario|Unidade.+|Administrador|Vinculacao.+|Atribuicao.+|Parametro|Movimentacao|Analise|Alerta.+|Conhecimento|Mapa|Atividade|Competencia.+|Notificacao|Processo)$/,
    /Builder$/,
    /BuilderImpl$/,
    /(?:^|\.).*Status.+$/,
    /(?:^|\.).*Tipo.+$/,
    /(?:^|\.).*Situacao.+$/
];

function deveExcluirClasse(nomeClasse: string, padroesExclusao: RegExp[] = PADROES_EXCLUSAO): boolean {
    return padroesExclusao.some((pattern) => pattern.test(nomeClasse));
}

function calcularPercentual(cobertos: number, perdidos: number): number {
    const total = cobertos + perdidos;
    if (total <= 0) return 0;
    return Number(((cobertos / total) * 100).toFixed(2));
}

function extrairInteiroCounter(counter: ContadorXml, campo: string): number {
    return Number.parseInt(counter?.$?.[campo] ?? "0", 10);
}

function extrairContadoresGlobais(counters: ContadorXml[] = []): MetricasGlobais {
    const resumo: Record<string, ResumoContador> = {};
    for (const counter of counters) {
        const type = counter.$.type;
        const cobertos = extrairInteiroCounter(counter, "covered");
        const perdidos = extrairInteiroCounter(counter, "missed");
        resumo[type] = {
            cobertos,
            perdidos,
            percentual: calcularPercentual(cobertos, perdidos)
        };
    }
    return {
        linhas: resumo.LINE ?? {cobertos: 0, perdidos: 0, percentual: 0},
        branches: resumo.BRANCH ?? {cobertos: 0, perdidos: 0, percentual: 0},
        instrucoes: resumo.INSTRUCTION ?? {cobertos: 0, perdidos: 0, percentual: 0},
        metodos: resumo.METHOD ?? {cobertos: 0, perdidos: 0, percentual: 0},
        complexidade: resumo.COMPLEXITY ?? {cobertos: 0, perdidos: 0, percentual: 0}
    };
}

async function lerRelatorioJacoco(caminhoAbsoluto: string): Promise<RelatorioJacocoXml | null> {
    try {
        const conteudo = await fs.readFile(caminhoAbsoluto, "utf-8");
        return await parseStringPromise(conteudo) as RelatorioJacocoXml;
    } catch {
        return null;
    }
}

async function extrairCoberturaJacoco(
    caminhoRelativo: string | null = null,
    opcoes: OpcoesCoberturaJacoco = {}
): Promise<ResultadoCoberturaJacoco> {
    const diretorioBase = opcoes.diretorioBase ?? DIRETORIO_RAIZ;
    const caminhoPadrao = resolverCaminhoConfigurado("coberturaBackend", diretorioBase);
    const caminhoXml = caminhoRelativo
        ? (path.isAbsolute(caminhoRelativo) ? caminhoRelativo : path.resolve(diretorioBase, caminhoRelativo))
        : caminhoPadrao;
    const relatorio = await lerRelatorioJacoco(caminhoXml);
    if (!relatorio) {
        throw new Error(`Relatorio JaCoCo nao encontrado em ${caminhoXml}`);
    }

    const {
        incluirSemLacunas = true,
        aplicarExclusoes = false,
        padroesExclusao = PADROES_EXCLUSAO,
        filtro = null
    } = opcoes;

    const classes = [];
    const counters = relatorio.report.counter ?? [];
    const metricasGlobais = extrairContadoresGlobais(counters);

    const totais: TotaisCobertura = {
        totalArquivos: 0,
        totalLinhas: 0,
        linhasCobertas: 0,
        totalBranches: 0,
        branchesCobertos: 0,
        coberturaGlobalLinhas: 0,
        coberturaGlobalBranches: 0,
    };

    for (const pacote of relatorio.report.package ?? []) {
        const nomePacote = pacote.$.name.replaceAll("/", ".");

        for (const sourceFile of pacote.sourcefile ?? []) {
            const nomeArquivo = sourceFile.$.name;
            const nomeClasse = `${nomePacote}.${nomeArquivo.replace(".java", "")}`;

            if (filtro && !nomeClasse.includes(filtro) && !`${nomePacote}.${nomeArquivo}`.includes(filtro)) {
                continue;
            }

            if (aplicarExclusoes && deveExcluirClasse(nomeClasse, padroesExclusao)) {
                continue;
            }

            const linhas = sourceFile.line || [];
            let totalLinhas = 0;
            let linhasCobertas = 0;
            let totalBranches = 0;
            let branchesCobertos = 0;
            const linhasPerdidasLista = [];
            const branchesParciais = [];

            for (const line of linhas) {
                const numeroLinha = Number.parseInt(line.$.nr ?? "0", 10);
                const instrucoesCobertas = Number.parseInt(line.$.ci ?? "0", 10);
                const branchesPerdidosLinha = Number.parseInt(line.$.mb ?? "0", 10);
                const branchesCobertosLinha = Number.parseInt(line.$.cb ?? "0", 10);
                const branchesLinha = branchesPerdidosLinha + branchesCobertosLinha;

                totalLinhas++;

                if (instrucoesCobertas > 0) {
                    linhasCobertas++;
                } else {
                    linhasPerdidasLista.push(numeroLinha);
                }

                if (branchesLinha > 0) {
                    totalBranches += branchesLinha;
                    branchesCobertos += branchesCobertosLinha;

                    if (branchesPerdidosLinha > 0) {
                        branchesParciais.push(`${numeroLinha}(${branchesPerdidosLinha}/${branchesLinha})`);
                    }
                }
            }

            const linhasPerdidasCount = totalLinhas - linhasCobertas;
            const branchesPerdidosCount = totalBranches - branchesCobertos;
            const temLacunas = linhasPerdidasCount > 0 || branchesPerdidosCount > 0;

            totais.totalArquivos++;
            totais.totalLinhas += totalLinhas;
            totais.linhasCobertas += linhasCobertas;
            totais.totalBranches += totalBranches;
            totais.branchesCobertos += branchesCobertos;

            if (incluirSemLacunas || temLacunas) {
                const sourceFileCounters = sourceFile.counter || [];
                const complexidadeCounter = sourceFileCounters.find(c => c.$.type === 'COMPLEXITY');
                const complexidade = complexidadeCounter ? extrairInteiroCounter(complexidadeCounter, 'covered') + extrairInteiroCounter(complexidadeCounter, 'missed') : 0;

                const linhasPercentual = calcularPercentual(linhasCobertas, linhasPerdidasCount);
                const branchesPercentual = totalBranches > 0 ? calcularPercentual(branchesCobertos, branchesPerdidosCount) : 100;

                classes.push({
                    nomePacote,
                    nomeArquivo,
                    nomeClasse,
                    nome: nomeClasse,
                    totalLinhas,
                    linhasCobertas,
                    linhasPerdidas: linhasPerdidasCount,
                    linhasPerdidasLista,
                    linhasPercentual,
                    coberturaLinhas: linhasPercentual,
                    totalBranches,
                    branchesCobertos,
                    branchesPerdidos: branchesPerdidosCount,
                    branchesPerdidosLista: branchesParciais,
                    branchesPercentual,
                    coberturaBranches: branchesPercentual,
                    complexidade,
                    contadoresGlobais: extrairContadoresGlobais(sourceFileCounters)
                });
            }
        }
    }

    totais.coberturaGlobalLinhas = calcularPercentual(totais.linhasCobertas, totais.totalLinhas - totais.linhasCobertas);
    totais.coberturaGlobalBranches = totais.totalBranches > 0
        ? calcularPercentual(totais.branchesCobertos, totais.totalBranches - totais.branchesCobertos)
        : 100;

    return {
        ...metricasGlobais,
        totais,
        classes: classes.toSorted((a, b) => a.linhasPercentual - b.linhasPercentual)
    };
}

export {
    extrairCoberturaJacoco
};
