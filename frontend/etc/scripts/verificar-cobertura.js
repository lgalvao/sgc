/* eslint-disable */
const fs = require('fs');
const path = require('path');

// Caminho do relatório de cobertura do Frontend (Vitest/Istanbul)
const COVERAGE_PATH = path.join(__dirname, '../../frontend/coverage/coverage-final.json');

console.log(`Lendo relatório de cobertura de: ${COVERAGE_PATH}`);

try {
    if (!fs.existsSync(COVERAGE_PATH)) {
        console.error("Erro: coverage-final.json não encontrado.");
        console.error("Execute 'npm run coverage:unit' no diretório frontend primeiro.");
        process.exit(1);
    }

    const content = fs.readFileSync(COVERAGE_PATH, 'utf8');
    const coverage = JSON.parse(content);

    const summary = [];

    for (const filePath in coverage) {
        const fileCoverage = coverage[filePath];
        const statementMap = fileCoverage.statementMap;
        const s = fileCoverage.s;

        const totalStatements = Object.keys(statementMap).length;
        let coveredStatements = 0;
        for (const key in s) {
            if (s[key] > 0) {
                coveredStatements++;
            }
        }

        const percentage = totalStatements === 0 ? 100 : (coveredStatements / totalStatements) * 100;

        // Normalize path
        let relativePath = filePath.replace(/\\/g, '/');
        if (relativePath.includes('frontend/src')) {
            relativePath = relativePath.substring(relativePath.indexOf('frontend/src'));
        } else if (relativePath.includes('src')) {
            relativePath = relativePath.substring(relativePath.indexOf('src'));
        }

        // Exclude tests
        if (!relativePath.includes('node_modules') && !relativePath.includes('.spec.ts') && !relativePath.includes('.test.ts')) {
            summary.push({
                file: relativePath,
                pct: parseFloat(percentage.toFixed(2)),
                total: totalStatements,
                covered: coveredStatements
            });
        }
    }

    // Sort by percentage ascending
    summary.sort((a, b) => a.pct - b.pct);

    console.log('\nArquivo | Cobertura % | Statements (Coberto/Total)');
    console.log('--- | --- | ---');

    let lowCoverageCount = 0;
    summary.forEach(item => {
        if (item.pct < 80) {
            console.log(`${item.file} | ${item.pct}% | ${item.covered}/${item.total}`);
            lowCoverageCount++;
        }
    });

    console.log(`\nEncontrados ${lowCoverageCount} arquivos com < 80% de cobertura.`);

} catch (err) {
    console.error('Erro ao ler ou processar arquivo de cobertura:', err);
    process.exit(1);
}
