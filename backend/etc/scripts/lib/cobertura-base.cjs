const fs = require('node:fs');
const path = require('node:path');
const {execFileSync} = require('node:child_process');
const xml2js = require('xml2js');

const BACKEND_DIR = path.resolve(__dirname, '../../..');
const ROOT_DIR = path.resolve(BACKEND_DIR, '..');
const REPORT_XML_PATH = path.join(BACKEND_DIR, 'build/reports/jacoco/test/jacocoTestReport.xml');
const REPORT_CSV_PATH = path.join(BACKEND_DIR, 'build/reports/jacoco/test/jacocoTestReport.csv');
const LACUNAS_JSON_PATH = path.join(ROOT_DIR, 'cobertura_lacunas.json');
const PLANO_100_PATH = path.join(ROOT_DIR, 'plano-100-cobertura.md');
const PLANO_RESUMIDO_PATH = path.join(ROOT_DIR, 'plano-cobertura-backend.md');

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

function calcularPercentual(cobertos, perdidos) {
    const total = cobertos + perdidos;
    return total === 0 ? 100 : (cobertos / total) * 100;
}

function obterContadores(elemento) {
    const contadores = {};
    const lista = elemento.counter || [];

    lista.forEach(contador => {
        contadores[contador.$.type] = {
            cobertos: Number.parseInt(contador.$.covered || 0, 10),
            perdidos: Number.parseInt(contador.$.missed || 0, 10)
        };
    });

    return contadores;
}

function obterComplexidade(sourceFile) {
    const contadores = sourceFile.counter || [];
    const contador = contadores.find(item => item.$.type === 'COMPLEXITY');
    if (!contador) {
        return 0;
    }

    return Number.parseInt(contador.$.covered || 0, 10) + Number.parseInt(contador.$.missed || 0, 10);
}

function extrairMetricasArquivo(pkg, sourceFile) {
    const nomePacote = pkg.$.name.replaceAll('/', '.');
    const nomeArquivo = sourceFile.$.name;
    const nomeClasse = `${nomePacote}.${nomeArquivo.replace('.java', '')}`;
    const linhas = sourceFile.line || [];

    let totalLinhas = 0;
    let linhasCobertas = 0;
    let totalBranches = 0;
    let branchesCobertos = 0;

    const linhasPerdidas = [];
    const branchesParciais = [];

    linhas.forEach(line => {
        const numeroLinha = Number.parseInt(line.$.nr || 0, 10);
        const instrucoesCobertas = Number.parseInt(line.$.ci || 0, 10);
        const branchesPerdidosLinha = Number.parseInt(line.$.mb || 0, 10);
        const branchesCobertosLinha = Number.parseInt(line.$.cb || 0, 10);
        const branchesLinha = branchesPerdidosLinha + branchesCobertosLinha;

        totalLinhas++;

        if (instrucoesCobertas > 0) {
            linhasCobertas++;
        } else {
            linhasPerdidas.push(numeroLinha);
        }

        if (branchesLinha > 0) {
            totalBranches += branchesLinha;
            branchesCobertos += branchesCobertosLinha;

            if (branchesPerdidosLinha > 0) {
                branchesParciais.push(`${numeroLinha}(${branchesPerdidosLinha}/${branchesLinha})`);
            }
        }
    });

    const linhasPerdidasCount = totalLinhas - linhasCobertas;
    const branchesPerdidosCount = totalBranches - branchesCobertos;

    return {
        nomePacote,
        nomeArquivo,
        nomeClasse,
        totalLinhas,
        linhasCobertas,
        linhasPerdidas: linhasPerdidasCount,
        linhasPerdidasLista: linhasPerdidas,
        totalBranches,
        branchesCobertos,
        branchesPerdidos: branchesPerdidosCount,
        branchesPerdidosLista: branchesParciais,
        coberturaLinhas: calcularPercentual(linhasCobertas, linhasPerdidasCount),
        coberturaBranches: totalBranches > 0 ? calcularPercentual(branchesCobertos, branchesPerdidosCount) : 100,
        complexidade: obterComplexidade(sourceFile),
        contadores: obterContadores(sourceFile)
    };
}

function deveExcluirClasse(nomeClasse, padroesExclusao = PADROES_EXCLUSAO) {
    return padroesExclusao.some(pattern => pattern.test(nomeClasse));
}

async function lerRelatorioJacoco(caminhoRelatorio = REPORT_XML_PATH) {
    if (!fs.existsSync(caminhoRelatorio)) {
        throw new Error(`Relatório JaCoCo não encontrado em ${caminhoRelatorio}`);
    }

    const parser = new xml2js.Parser();
    const xml = fs.readFileSync(caminhoRelatorio);
    return parser.parseStringPromise(xml);
}

function coletarArquivosCobertura(relatorio, opcoes = {}) {
    const {
        incluirSemLacunas = true,
        aplicarExclusoes = false,
        padroesExclusao = PADROES_EXCLUSAO,
        filtro = null
    } = opcoes;

    const packages = relatorio.report.package || [];
    const arquivos = [];
    const totais = {
        totalArquivos: 0,
        totalLinhas: 0,
        linhasCobertas: 0,
        totalBranches: 0,
        branchesCobertos: 0
    };

    packages.forEach(pkg => {
        const sourceFiles = pkg.sourcefile || [];

        sourceFiles.forEach(sourceFile => {
            const metricas = extrairMetricasArquivo(pkg, sourceFile);

            if (filtro && !metricas.nomeClasse.includes(filtro) && !`${metricas.nomePacote}.${metricas.nomeArquivo}`.includes(filtro)) {
                return;
            }

            if (aplicarExclusoes && deveExcluirClasse(metricas.nomeClasse, padroesExclusao)) {
                return;
            }

            totais.totalArquivos++;
            totais.totalLinhas += metricas.totalLinhas;
            totais.linhasCobertas += metricas.linhasCobertas;
            totais.totalBranches += metricas.totalBranches;
            totais.branchesCobertos += metricas.branchesCobertos;

            const temLacunas = metricas.linhasPerdidas > 0 || metricas.branchesPerdidos > 0;
            if (incluirSemLacunas || temLacunas) {
                arquivos.push(metricas);
            }
        });
    });

    return {
        arquivos,
        totais: {
            ...totais,
            coberturaGlobalLinhas: calcularPercentual(totais.linhasCobertas, totais.totalLinhas - totais.linhasCobertas),
            coberturaGlobalBranches: totais.totalBranches > 0
                ? calcularPercentual(totais.branchesCobertos, totais.totalBranches - totais.branchesCobertos)
                : 100
        }
    };
}

function ordenarPorLacunas(arquivos) {
    return [...arquivos].sort((a, b) => {
        const scoreA = a.linhasPerdidas + (a.branchesPerdidos * 0.5);
        const scoreB = b.linhasPerdidas + (b.branchesPerdidos * 0.5);
        return scoreB - scoreA || a.nomeClasse.localeCompare(b.nomeClasse, 'pt-BR');
    });
}

function executarGradleJaCoCo(opcoes = {}) {
    const {incluirTestes = false} = opcoes;
    const args = incluirTestes
        ? [':backend:test', ':backend:jacocoTestReport']
        : [':backend:jacocoTestReport'];
    const executavel = process.platform === 'win32' ? 'gradlew.bat' : './gradlew';

    execFileSync(executavel, args, {
        cwd: ROOT_DIR,
        stdio: 'inherit'
    });
}

module.exports = {
    BACKEND_DIR,
    ROOT_DIR,
    REPORT_XML_PATH,
    REPORT_CSV_PATH,
    LACUNAS_JSON_PATH,
    PLANO_100_PATH,
    PLANO_RESUMIDO_PATH,
    PADROES_EXCLUSAO,
    calcularPercentual,
    coletarArquivosCobertura,
    deveExcluirClasse,
    executarGradleJaCoCo,
    extrairMetricasArquivo,
    lerRelatorioJacoco,
    obterContadores,
    ordenarPorLacunas
};
