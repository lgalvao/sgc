const fs = require('fs');
const path = require('path');
const xml2js = require('xml2js');

// Configuração
const REPORT_PATH = path.join(__dirname, '../../build/reports/jacoco/test/jacocoTestReport.xml');

// Args parsing
const args = process.argv.slice(2);
const help = args.includes('--help') || args.includes('-h');
const filterArg = args.find(a => !a.startsWith('-')) || null;
const minCovArg = args.find(a => a.startsWith('--min='))?.split('=')[1] || '99';
const showMissed = args.includes('--missed') || args.includes('--details');
const simpleMode = args.includes('--simple');

if (help) {
    console.log(`
Uso: node scripts/backend/check-coverage.js [filtro] [opções]

Argumentos:
  [filtro]       Filtrar por nome de pacote ou classe (opcional)

Opções:
  --min=<n>      Filtrar classes com cobertura de linha menor que <n>% (Padrão: 99)
  --missed       Exibir detalhes das linhas e branches não cobertos (Ranking de perdidos)
  --simple       Saída simplificada (apenas classe e linhas/branches perdidas)
  --help, -h     Exibir esta ajuda
`);
    process.exit(0);
}

const MIN_COVERAGE = parseFloat(minCovArg);

function calculatePercentage(covered, missed) {
    const total = covered + missed;
    if (total === 0) return 0.0;
    return (covered / total) * 100;
}

function getCounters(element) {
    const counters = {};
    if (element.counter) {
        element.counter.forEach(c => {
            counters[c.$.type] = {
                covered: parseInt(c.$.covered, 10),
                missed: parseInt(c.$.missed, 10)
            };
        });
    }
    return counters;
}

async function parseXml(filePath) {
    const parser = new xml2js.Parser();
    const data = fs.readFileSync(filePath);
    return parser.parseStringPromise(data);
}

function processPackage(pkg, filter) {
    const pkgName = pkg.$.name.replaceAll('/', '.');
    if (filter && !pkgName.includes(filter)) return null;

    const metrics = getCounters(pkg);
    const classes = [];

    if (pkg.class) {
        pkg.class.forEach(cls => {
            const clsName = cls.$.name.replace(/\//g, '.');
            if (filter && !clsName.includes(filter)) return;

            const cMetrics = getCounters(cls);
            if (cMetrics.LINE) {
                const l = cMetrics.LINE;

                // Extrair linhas perdidas se necessário
                let missedLines = [];
                let partialBranches = [];
            }

            classes.push({
                name: clsName,
                metrics: cMetrics,
                lineCoverage: cMetrics.LINE ? calculatePercentage(cMetrics.LINE.covered, cMetrics.LINE.missed) : 100,
                branchCoverage: cMetrics.BRANCH ? calculatePercentage(cMetrics.BRANCH.covered, cMetrics.BRANCH.missed) : 100,
                missedLinesCount: cMetrics.LINE ? cMetrics.LINE.missed : 0,
                missedBranchesCount: cMetrics.BRANCH ? cMetrics.BRANCH.missed : 0
            });
        });
    }

    return { name: pkgName, metrics, classes };
}

// Para modo detalhado (missed lines), precisamos iterar sourcefiles
function processSourceFiles(pkg, filter) {
    const pkgName = pkg.$.name.replace(/\//g, '.');
    const results = [];

    if (pkg.sourcefile) {
        pkg.sourcefile.forEach(sf => {
            const fileName = sf.$.name;
            const fullPath = `${pkgName}.${fileName}`;

            if (filter && !fullPath.includes(filter)) return;

            const missedLines = [];
            const partialLines = [];

            if (sf.line) {
                sf.line.forEach(line => {
                    const nr = line.$.nr;
                    const ci = parseInt(line.$.ci || 0, 10);
                    const mb = parseInt(line.$.mb || 0, 10);
                    const cb = parseInt(line.$.cb || 0, 10);

                    if (ci === 0) {
                        missedLines.push(nr);
                    } else if (mb > 0) {
                        partialLines.push(`${nr}(${mb}/${mb + cb})`);
                    }
                });
            }

            const weight = missedLines.length + (partialLines.length * 0.5);
            if (weight > 0) {
                results.push({
                    file: fullPath,
                    missed: missedLines,
                    partial: partialLines,
                    weight
                });
            }
        });
    }
    return results;
}

async function main() {
    if (!fs.existsSync(REPORT_PATH)) {
        console.error(`Erro: Arquivo ${REPORT_PATH} não encontrado.`);
        console.error("Execute 'gradle test jacocoTestReport' primeiro.");
        process.exit(1);
    }

    const result = await parseXml(REPORT_PATH);
    const root = result.report;

    console.log("\n" + "=".repeat(100));
    console.log(`RELATÓRIO DE COBERTURA JACOCO`.padStart(65));
    console.log("=".repeat(100));

    if (showMissed) {
        let allMissed = [];
        if (root.package) {
            root.package.forEach(pkg => {
                allMissed = allMissed.concat(processSourceFiles(pkg, filterArg));
            });
        }

        allMissed.sort((a, b) => b.weight - a.weight);

        console.log(`\n ARQUIVOS COM MAIS LINHAS/BRANCHES PERDIDAS (Total: ${allMissed.length})\n`);

        allMissed.slice(0, 50).forEach(r => {
            if (simpleMode) {
                console.log(`${r.file}: MISS-L [${r.missed.join(',')}] MISS-B [${r.partial.join(',')}]`);
            } else {
                console.log(`📄 ${r.file}`);
                if (r.missed.length) console.log(`   🔴 Linhas não executadas: ${r.missed.join(', ')}`);
                if (r.partial.length) console.log(`   🟡 Branches perdidos (miss/total): ${r.partial.join(', ')}`);
                console.log("-".repeat(50));
            }
        });

    } else {
        // Modo Tabela (substitui check_coverage.py)
        const globalCounters = getCounters(root);

        if (globalCounters.INSTRUCTION) {
            const inst = globalCounters.INSTRUCTION;
            const line = globalCounters.LINE || { covered: 0, missed: 0 };
            const pInst = calculatePercentage(inst.covered, inst.missed);
            const pLine = calculatePercentage(line.covered, line.missed);
            const totalInst = inst.covered + inst.missed;

            console.log(`| ${'TOTAL DO PROJETO'.padEnd(45)} | ${pInst.toFixed(2).padStart(8)}% | ${pLine.toFixed(2).padStart(8)}% | ${inst.covered.toString().padStart(10)} | ${totalInst.toString().padStart(10)} |`);
        }
        console.log("=".repeat(100) + "\n");

        if (globalCounters.BRANCH) {
            const b = globalCounters.BRANCH;
            const percB = calculatePercentage(b.covered, b.missed);
            console.log(`Cobertura de Branches (Global): ${percB.toFixed(2)}% (${b.covered}/${b.covered + b.missed})`);
        }

        console.log("\nDETALHE POR CLASSE (Filtrado por < " + MIN_COVERAGE + "%)\n");
        console.log(`| ${'Classe'.padEnd(60)} | ${'Linhas %'.padEnd(10)} | ${'Missed L'.padEnd(10)} | ${'Branches %'.padEnd(10)} | ${'Missed B'.padEnd(10)} |`);
        console.log("-".repeat(115));

        let lowCovClasses = [];
        if (root.package) {
            root.package.forEach(pkg => {
                const pkgResult = processPackage(pkg, filterArg);
                if (pkgResult) {
                    lowCovClasses = lowCovClasses.concat(pkgResult.classes);
                }
            });
        }

        // Filtra e ordena
        lowCovClasses = lowCovClasses.filter(c => c.lineCoverage < MIN_COVERAGE);
        lowCovClasses.sort((a, b) => b.missedBranchesCount - a.missedBranchesCount || b.missedLinesCount - a.missedLinesCount);

        lowCovClasses.slice(0, 20).forEach(c => {
            const branchStr = c.metrics.BRANCH ? c.branchCoverage.toFixed(2) + '%' : 'N/A';
            console.log(`| ${c.name.padEnd(60)} | ${c.lineCoverage.toFixed(2).padStart(8)}% | ${c.missedLinesCount.toString().padStart(10)} | ${branchStr.padStart(10)} | ${c.missedBranchesCount.toString().padStart(10)} |`);
        });

        if (lowCovClasses.length === 0) {
            console.log("Nenhuma classe abaixo do limite de cobertura encontrado.");
        } else if (lowCovClasses.length > 20) {
            console.log(`\n... e mais ${lowCovClasses.length - 20} classes.`);
        }
    }
}

main().catch(err => console.error(err));