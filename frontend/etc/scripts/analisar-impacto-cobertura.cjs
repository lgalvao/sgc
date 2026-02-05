/* eslint-disable */
const fs = require('fs');
const path = require('path');

const COVERAGE_PATH = path.join(__dirname, '../../coverage/coverage-final.json');

try {
    if (!fs.existsSync(COVERAGE_PATH)) {
        console.error("Erro: coverage-final.json não encontrado.");
        process.exit(1);
    }

    const content = fs.readFileSync(COVERAGE_PATH, 'utf8');
    const coverage = JSON.parse(content);

    let totalStatements = 0;
    let totalCovered = 0;
    const files = [];

    for (const filePath in coverage) {
        const fileCoverage = coverage[filePath];
        const s = fileCoverage.s;

        const fileTotal = Object.keys(s).length;
        let fileCovered = 0;
        for (const key in s) {
            if (s[key] > 0) {
                fileCovered++;
            }
        }

        totalStatements += fileTotal;
        totalCovered += fileCovered;

        let relativePath = filePath.replace(/\\/g, '/');
        if (relativePath.includes('frontend/src')) {
            relativePath = relativePath.substring(relativePath.indexOf('frontend/src'));
        }

        if (!relativePath.includes('node_modules') && !relativePath.includes('.spec.ts') && !relativePath.includes('.test.ts')) {
            files.push({
                file: relativePath,
                total: fileTotal,
                covered: fileCovered,
                uncovered: fileTotal - fileCovered,
                pct: fileTotal === 0 ? 100 : (fileCovered / fileTotal) * 100
            });
        }
    }

    const currentPct = (totalCovered / totalStatements) * 100;
    console.log(`Cobertura Total Atual: ${currentPct.toFixed(2)}% (${totalCovered}/${totalStatements} statements)`);
    console.log(`Objetivo (+2%): ${(currentPct + 2).toFixed(2)}%\n`);

    // Impacto: quanto a cobertura total aumentaria se este arquivo fosse 100% coberto
    files.forEach(f => {
        f.impact = (f.uncovered / totalStatements) * 100;
    });

    files.sort((a, b) => b.impact - a.impact);

    console.log('Arquivo | Cobertura Atual | Statements Desatendidos | Impacto Potencial (p.p.)');
    console.log('--- | --- | --- | ---');
    files.slice(0, 15).forEach(f => {
        if (f.impact > 0) {
            console.log(`${f.file} | ${f.pct.toFixed(2)}% | ${f.uncovered}/${f.total} | ${f.impact.toFixed(3)}`);
        }
    });

} catch (err) {
    console.error('Erro:', err);
}
