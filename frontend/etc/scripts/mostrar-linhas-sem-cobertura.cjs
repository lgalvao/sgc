/* eslint-disable */
const fs = require('fs');
const path = require('path');

const COVERAGE_PATH = path.join(__dirname, '../../coverage/coverage-final.json');
const targetFile = process.argv[2];

if (!targetFile) {
    console.error("Uso: node mostrar-linhas-sem-cobertura.cjs <caminho-do-arquivo>");
    console.error("Exemplo: node mostrar-linhas-sem-cobertura.cjs src/views/processo/MapaVisualizacaoView.vue");
    process.exit(1);
}

try {
    if (!fs.existsSync(COVERAGE_PATH)) {
        console.error("Erro: coverage-final.json não encontrado. Execute 'npm run coverage:unit' primeiro.");
        process.exit(1);
    }

    const coverage = JSON.parse(fs.readFileSync(COVERAGE_PATH, 'utf8'));
    
    // Tenta encontrar o arquivo no relatório
    let filePath = Object.keys(coverage).find(p => p.endsWith(targetFile));
    
    if (!filePath) {
        console.error(`Arquivo '${targetFile}' não encontrado no relatório de cobertura.`);
        process.exit(1);
    }

    const fileCoverage = coverage[filePath];
    const source = fs.readFileSync(filePath, 'utf8').split('\n');
    const statementMap = fileCoverage.statementMap;
    const s = fileCoverage.s;

    console.log(`\nAnálise de Cobertura para: ${targetFile}`);
    console.log(`------------------------------------------`);

    const uncoveredLines = new Set();
    Object.keys(s).forEach(key => {
        if (s[key] === 0) {
            const range = statementMap[key];
            for (let i = range.start.line; i <= range.end.line; i++) {
                uncoveredLines.add(i);
            }
        }
    });

    source.forEach((line, index) => {
        const lineNum = index + 1;
        const prefix = uncoveredLines.has(lineNum) ? '>>> ' : '    ';
        console.log(`${lineNum.toString().padStart(4)} | ${prefix}${line}`);
    });

    console.log(`\nLegenda: >>> Linha sem cobertura`);

} catch (err) {
    console.error('Erro:', err);
}
