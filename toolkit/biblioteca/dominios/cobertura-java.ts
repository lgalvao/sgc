import fs from "node:fs/promises";
import path from "node:path";
import {parseStringPromise} from "xml2js";

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
    ramificacoes: ResumoContador;
    instrucoes: ResumoContador;
    metodos: ResumoContador;
    complexidade: ResumoContador;
}

export interface TotaisCobertura {
    totalArquivos: number;
    totalLinhas: number;
    linhasCobertas: number;
    totalRamificacoes: number;
    ramificacoesCobertas: number;
    coberturaGlobalLinhas: number;
    coberturaGlobalRamificacoes: number;
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
    totalRamificacoes: number;
    ramificacoesCobertas: number;
    ramificacoesPerdidas: number;
    ramificacoesPerdidasLista: string[];
    ramificacoesPercentual: number;
    coberturaRamificacoes: number;
    complexidade: number;
    contadoresGlobais: MetricasGlobais;
}

export interface OpcoesCoberturaJacoco {
    diretorioBase?: string;
    incluirSemLacunas?: boolean;
    aplicarExclusoes?: boolean;
    padroesExclusao?: readonly RegExp[];
    filtro?: string | null;
}

export interface ResultadoCoberturaJacoco extends MetricasGlobais {
    totais: TotaisCobertura;
    classes: ClasseCobertura[];
}

function deveExcluirClasse(nomeClasse: string, padroesExclusao: readonly RegExp[] = []): boolean {
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
        ramificacoes: resumo.BRANCH ?? {cobertos: 0, perdidos: 0, percentual: 0},
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
    caminhoRelatorio: string,
    opcoes: OpcoesCoberturaJacoco = {}
): Promise<ResultadoCoberturaJacoco> {
    if (caminhoRelatorio.trim() === "") {
        throw new Error("O caminho do relatório JaCoCo é obrigatório.");
    }

    const diretorioBase = opcoes.diretorioBase ?? process.cwd();
    const caminhoXml = path.isAbsolute(caminhoRelatorio)
        ? caminhoRelatorio
        : path.resolve(diretorioBase, caminhoRelatorio);
    const relatorio = await lerRelatorioJacoco(caminhoXml);
    if (!relatorio) {
        throw new Error(`Relatorio JaCoCo nao encontrado em ${caminhoXml}`);
    }

    const {
        incluirSemLacunas = true,
        aplicarExclusoes = false,
        padroesExclusao = [],
        filtro = null
    } = opcoes;

    const classes = [];
    const counters = relatorio.report.counter ?? [];
    const metricasGlobais = extrairContadoresGlobais(counters);

    const totais: TotaisCobertura = {
        totalArquivos: 0,
        totalLinhas: 0,
        linhasCobertas: 0,
        totalRamificacoes: 0,
        ramificacoesCobertas: 0,
        coberturaGlobalLinhas: 0,
        coberturaGlobalRamificacoes: 0,
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
            let totalRamificacoes = 0;
            let ramificacoesCobertas = 0;
            const linhasPerdidasLista = [];
            const ramificacoesParciais = [];

            for (const line of linhas) {
                const numeroLinha = Number.parseInt(line.$.nr ?? "0", 10);
                const instrucoesCobertas = Number.parseInt(line.$.ci ?? "0", 10);
                const ramificacoesPerdidasLinha = Number.parseInt(line.$.mb ?? "0", 10);
                const ramificacoesCobertasLinha = Number.parseInt(line.$.cb ?? "0", 10);
                const ramificacoesLinha = ramificacoesPerdidasLinha + ramificacoesCobertasLinha;

                totalLinhas++;

                if (instrucoesCobertas > 0) {
                    linhasCobertas++;
                } else {
                    linhasPerdidasLista.push(numeroLinha);
                }

                if (ramificacoesLinha > 0) {
                    totalRamificacoes += ramificacoesLinha;
                    ramificacoesCobertas += ramificacoesCobertasLinha;

                    if (ramificacoesPerdidasLinha > 0) {
                        ramificacoesParciais.push(`${numeroLinha}(${ramificacoesPerdidasLinha}/${ramificacoesLinha})`);
                    }
                }
            }

            const linhasPerdidasCount = totalLinhas - linhasCobertas;
            const ramificacoesPerdidasCount = totalRamificacoes - ramificacoesCobertas;
            const temLacunas = linhasPerdidasCount > 0 || ramificacoesPerdidasCount > 0;

            totais.totalArquivos++;
            totais.totalLinhas += totalLinhas;
            totais.linhasCobertas += linhasCobertas;
            totais.totalRamificacoes += totalRamificacoes;
            totais.ramificacoesCobertas += ramificacoesCobertas;

            if (incluirSemLacunas || temLacunas) {
                const sourceFileCounters = sourceFile.counter || [];
                const complexidadeCounter = sourceFileCounters.find(c => c.$.type === 'COMPLEXITY');
                const complexidade = complexidadeCounter ? extrairInteiroCounter(complexidadeCounter, 'covered') + extrairInteiroCounter(complexidadeCounter, 'missed') : 0;

                const linhasPercentual = calcularPercentual(linhasCobertas, linhasPerdidasCount);
                const ramificacoesPercentual = totalRamificacoes > 0 ? calcularPercentual(ramificacoesCobertas, ramificacoesPerdidasCount) : 100;

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
                    totalRamificacoes,
                    ramificacoesCobertas,
                    ramificacoesPerdidas: ramificacoesPerdidasCount,
                    ramificacoesPerdidasLista: ramificacoesParciais,
                    ramificacoesPercentual,
                    coberturaRamificacoes: ramificacoesPercentual,
                    complexidade,
                    contadoresGlobais: extrairContadoresGlobais(sourceFileCounters)
                });
            }
        }
    }

    totais.coberturaGlobalLinhas = calcularPercentual(totais.linhasCobertas, totais.totalLinhas - totais.linhasCobertas);
    totais.coberturaGlobalRamificacoes = totais.totalRamificacoes > 0
        ? calcularPercentual(totais.ramificacoesCobertas, totais.totalRamificacoes - totais.ramificacoesCobertas)
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
