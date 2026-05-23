/* eslint-disable max-depth */
import fs from "node:fs/promises";
import {parseStringPromise} from "xml2js";
import {resolverNaRaiz} from "../caminhos.js";

const PADROES_EXCLUSAO = [
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

function deveExcluirClasse(nomeClasse, padroesExclusao = PADROES_EXCLUSAO) {
    return padroesExclusao.some((pattern) => pattern.test(nomeClasse));
}

function calcularPercentual(cobertos, perdidos) {
    const total = cobertos + perdidos;
    if (total <= 0) return 0;
    return Number(((cobertos / total) * 100).toFixed(2));
}

function extrairInteiroCounter(counter, campo) {
    return Number.parseInt(counter?.$?.[campo] ?? "0", 10);
}

function extrairContadoresGlobais(counters = []) {
    const resumo = {};
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

async function lerRelatorioJacoco(caminhoAbsoluto) {
    try {
        const conteudo = await fs.readFile(caminhoAbsoluto, "utf-8");
        return await parseStringPromise(conteudo);
    } catch {
        return null;
    }
}

async function extrairCoberturaJacoco(caminhoRelativo = "backend/build/reports/jacoco/test/jacocoTestReport.xml", opcoes = {}) {
    const caminhoXml = resolverNaRaiz(caminhoRelativo);
    const relatorio = await lerRelatorioJacoco(caminhoXml);
    if (!relatorio) {
        throw new Error(`Relatório JaCoCo não encontrado em ${caminhoRelativo}`);
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

    let totais = {
        totalArquivos: 0,
        totalLinhas: 0,
        linhasCobertas: 0,
        totalBranches: 0,
        branchesCobertos: 0
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
                const numeroLinha = Number.parseInt(line.$.nr || 0, 10);
                const instrucoesCobertas = Number.parseInt(line.$.ci || 0, 10);
                const branchesPerdidosLinha = Number.parseInt(line.$.mb || 0, 10);
                const branchesCobertosLinha = Number.parseInt(line.$.cb || 0, 10);
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
        classes: classes.sort((a, b) => a.linhasPercentual - b.linhasPercentual)
    };
}

export {
    PADROES_EXCLUSAO,
    calcularPercentual,
    deveExcluirClasse,
    extrairCoberturaJacoco,
    lerRelatorioJacoco
};