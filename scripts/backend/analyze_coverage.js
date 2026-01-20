const fs = require('fs');
const path = require('path');

// Try to find the file in root or relative to the script
let coveragePath = 'coverage-final.json';
if (!fs.existsSync(coveragePath)) {
    coveragePath = path.join(__dirname, '../coverage-final.json');
}

console.log(`Reading coverage from: ${coveragePath}`);

try {
    if (!fs.existsSync(coveragePath)) {
        console.error("coverage-final.json not found!");
        process.exit(1);
    }

    const content = fs.readFileSync(coveragePath, 'utf8');
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

        // Normalize path to be relative to src usually or project root
        let relativePath = filePath.replace(/\\/g, '/'); // Normalize slashes
        if (relativePath.includes('frontend/src')) {
            relativePath = relativePath.substring(relativePath.indexOf('frontend/src'));
        } else if (relativePath.includes('src')) {
            relativePath = relativePath.substring(relativePath.indexOf('src'));
        }

        // Exclude node_modules or test files if they appear
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

    console.log('File | Coverage % | Statements (Covered/Total)');
    console.log('--- | --- | ---');

    let lowCoverageCount = 0;
    summary.forEach(item => {
        if (item.pct < 80) { // Filter for interesting ones
            console.log(`${item.file} | ${item.pct}% | ${item.covered}/${item.total}`);
            lowCoverageCount++;
        }
    });

    console.log(`\nFound ${lowCoverageCount} files with < 80% coverage.`);

} catch (err) {
    console.error('Error reading or parsing coverage file:', err);
}
