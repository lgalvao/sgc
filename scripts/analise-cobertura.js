const fs = require('node:fs');
const path = require('node:path');
const { execSync } = require('node:child_process');
const xml2js = require('xml2js');

// Configuração
const REPORT_PATH = path.join(__dirname, '../backend/build/reports/jacoco/test/jacocoTestReport.xml');
const GRADLE_CMD = './gradlew :backend:test :backend:jacocoTestReport';
const GRADLE_DIR = path.join(__dirname, '..');

// Função principal
async function main() {
    console.log('=== Iniciando Análise de Cobertura ===');
    console.log(`Executando comando: ${GRADLE_CMD}`);
    console.log('Aguarde, isso pode levar alguns minutos...\n');

    try {
        execSync(GRADLE_CMD, { cwd: GRADLE_DIR, stdio: 'inherit' });
    } catch (error) {
        console.warn('\nA execução do Gradle falhou (possivelmente devido a testes falhando).', error.message);
        console.warn('Tentando ler o relatório mesmo assim...\n');
    }

    if (!fs.existsSync(REPORT_PATH)) {
        console.error(`ERRO: Relatório não encontrado em ${REPORT_PATH}`);
        console.error('Verifique se os testes rodaram corretamente e se o plugin jacoco está configurado para gerar XML.');
        process.exit(1);
    }

    console.log(`\nLendo relatório: ${REPORT_PATH}`);

    const parser = new xml2js.Parser();
    const xmlData = fs.readFileSync(REPORT_PATH);

    parser.parseString(xmlData, (err, result) => {
        if (err) {
            console.error('Erro ao fazer parse do XML:', err);
            return;
        }
        processCoverageData(result);
    });
}

function processCoverageData(report) {
    const packages = report.report.package || [];
    const tableData = [];

    packages.forEach(pkg => {
        const packageName = pkg.$.name;
        const sourceFiles = pkg.sourcefile || [];

        sourceFiles.forEach(sf => {
            const fileName = sf.$.name;
            const fullClassName = `${packageName}/${fileName}`;
            const lines = sf.line || [];
            const counters = sf.counter || [];

            // Métricas de Linhas e Branches via elementos <line>
            let linesTotal = 0;
            let linesCoveredCount = 0;
            let linesMissedCount = 0;
            let branchesTotal = 0;
            let branchesCoveredCount = 0;
            let branchesMissedCount = 0;

            const missedLinesList = [];
            const missedBranchesLinesList = [];

            lines.forEach(line => {
                const nr = Number.parseInt(line.$.nr, 10);
                const ci = Number.parseInt(line.$.ci, 10); // Covered instructions
                const mb = Number.parseInt(line.$.mb, 10); // Missed branches
                const cb = Number.parseInt(line.$.cb, 10); // Covered branches

                // Contagem de Linhas
                linesTotal++;

                if (ci > 0) {
                    linesCoveredCount++;
                } else {
                    linesMissedCount++;
                    missedLinesList.push(nr);
                }

                // Contagem de Branches
                const lineBranches = mb + cb;
                if (lineBranches > 0) {
                    branchesTotal += lineBranches;
                    branchesCoveredCount += cb;
                    branchesMissedCount += mb;

                    if (mb > 0) {
                        missedBranchesLinesList.push(nr);
                    }
                }
            });

            // Métricas de Complexidade via elementos <counter>
            let complexityTotal = 0;
            let complexityCovered = 0;
            let complexityMissed = 0;

            const complexityCounter = counters.find(c => c.$.type === 'COMPLEXITY');
            if (complexityCounter) {
                complexityMissed = Number.parseInt(complexityCounter.$.missed, 10);
                complexityCovered = Number.parseInt(complexityCounter.$.covered, 10);
                complexityTotal = complexityMissed + complexityCovered;
            } else {
                // Fallback se não encontrar o contador de complexidade explícito
                // (Geralmente JaCoCo fornece, mas podemos tentar estimar ou deixar 0)
            }

            // Só adiciona se houver linhas mapeadas
            if (linesTotal > 0) {
                const lineCoveragePct = (linesCoveredCount / linesTotal) * 100;
                const branchCoveragePct = branchesTotal > 0 ? (branchesCoveredCount / branchesTotal) * 100 : 100;
                // Cxn = Complexidade

                tableData.push({
                    Arquivo: fullClassName,
                    'Cxn Total': complexityTotal, // Nova coluna
                    'Linhas T.': linesTotal,
                    'Linhas Cob.': linesCoveredCount,
                    'Linhas N.C.': linesMissedCount,
                    '% Linhas': lineCoveragePct.toFixed(1) + '%',
                    'Lista N.C.': formatLineList(missedLinesList),
                    'Branches T.': branchesTotal,
                    'Branches Cob.': branchesCoveredCount,
                    'Branches N.C.': branchesMissedCount,
                    '% Branches': branchesTotal > 0 ? branchCoveragePct.toFixed(1) + '%' : 'N/A',
                    'Lista Br. N.C.': formatLineList(missedBranchesLinesList)
                });
            }
        });
    });

    // Ordenar por menor cobertura de linhas
    tableData.sort((a, b) => {
        const pctA = Number.parseFloat(a['% Linhas']);
        const pctB = Number.parseFloat(b['% Linhas']);
        return pctA - pctB;
    });

    console.table(tableData);

    // Resumo Geral
    console.log('\n--- Resumo Geral ---');
    console.log(`Total de arquivos analisados: ${tableData.length}`);
}

// Helper para formatar lista de linhas (ex: "1, 2, 5-10")
function formatLineList(lines) {
    if (lines.length === 0) return '';

    if (lines.length > 20) {
        return lines.slice(0, 15).join(', ') + '... (+ ' + (lines.length - 15) + ')';
    }

    return lines.join(', ');
}

try {
    await main();
} catch (err) {
    console.error('Falha catastrófica na execução da análise:', err);
    process.exit(1);
}
