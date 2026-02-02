const fs = require('node:fs');
const path = require('node:path');
const { execSync } = require('node:child_process');
const xml2js = require('xml2js');

// Configuração
const BASE_DIR = path.join(__dirname, '../../');
const REPORT_PATH = path.join(BASE_DIR, 'build/reports/jacoco/test/jacocoTestReport.xml');
const isWindows = process.platform === 'win32';
const GRADLE_CMD = isWindows ? 'gradlew.bat :backend:jacocoTestReport' : './gradlew :backend:jacocoTestReport';

async function parseXml(filePath) {
    const data = fs.readFileSync(filePath);
    const parser = new xml2js.Parser();
    return parser.parseStringPromise(data);
}

function calculatePercentage(covered, missed) {
    const total = covered + missed;
    return total === 0 ? 100 : (covered / total) * 100;
}

async function runCoverage() {
    console.log('🚀 Executando :backend:jacocoTestReport...');
    try {
        execSync(GRADLE_CMD, { cwd: BASE_DIR, stdio: 'inherit' });
    } catch (error) {
        console.warn('⚠️ Gradle terminou com avisos/erros (testes falhando?), mas prosseguindo para análise do relatório.');
    }
}

async function main() {
    if (process.argv.includes('--run')) {
        await runCoverage();
    }

    if (!fs.existsSync(REPORT_PATH)) {
        console.error(`❌ Relatório não encontrado: ${REPORT_PATH}`);
        console.error("💡 Execute './gradlew :backend:test :backend:jacocoTestReport' primeiro para gerar o relatório.");
        process.exit(1);
    }

    const report = await parseXml(REPORT_PATH);
    const packages = report.report.package || [];

    const results = [];
    let totalLines = 0;
    let coveredLines = 0;

    packages.forEach(pkg => {
        const packageName = pkg.$.name.replace(/\//g, '.');
        const sourceFiles = pkg.sourcefile || [];

        sourceFiles.forEach(sf => {
            const fileName = sf.$.name;
            const className = `${packageName}.${fileName.replace('.java', '')}`;

            let linesTotal = 0;
            let linesCovered = 0;
            const missedLines = [];
            const partialBranches = [];

            if (sf.line) {
                sf.line.forEach(line => {
                    const nr = parseInt(line.$.nr);
                    const ci = parseInt(line.$.ci);
                    const mb = parseInt(line.$.mb || 0);
                    const cb = parseInt(line.$.cb || 0);

                    linesTotal++;
                    if (ci > 0) {
                        linesCovered++;
                        if (mb > 0) partialBranches.push(`${nr}(${mb}/${mb + cb})`);
                    } else {
                        missedLines.push(nr);
                    }
                });
            }

            totalLines += linesTotal;
            coveredLines += linesCovered;

            if (linesCovered < linesTotal || partialBranches.length > 0) {
                results.push({
                    class: className,
                    coverage: calculatePercentage(linesCovered, linesTotal - linesCovered).toFixed(2) + '%',
                    missed: missedLines,
                    partial: partialBranches,
                    score: (linesTotal - linesCovered) + (partialBranches.length * 0.5)
                });
            }
        });
    });

    // Ordenar por "gravidade" (mais linhas/branches perdidas primeiro)
    results.sort((a, b) => b.score - a.score);

    const summary = {
        globalLineCoverage: calculatePercentage(coveredLines, totalLines - coveredLines).toFixed(2) + '%',
        totalFilesWithGaps: results.length,
        gaps: results
    };

    fs.writeFileSync(path.join(BASE_DIR, 'cobertura_lacunas.json'), JSON.stringify(summary, null, 2));

    console.log('\n📊 RELATÓRIO DE LACUNAS DE COBERTURA (Objetivo: 100%)');
    console.log(`Cobertura Global de Linhas: ${summary.globalLineCoverage}`);
    console.log(`Arquivos com lacunas: ${summary.totalFilesWithGaps}`);

    results.forEach(r => {
        console.log(`\n📄 ${r.class} [${r.coverage}]`);
        if (r.missed.length > 0) {
            console.log(`   🔴 Linhas perdidas: ${r.missed.join(', ')}`);
        }
        if (r.partial.length > 0) {
            console.log(`   🟡 Branches parciais: ${r.partial.join(', ')}`);
        }
    });

    console.log('\n✅ Relatório completo salvo em: cobertura_lacunas.json\n');
}

main().catch(console.error);
