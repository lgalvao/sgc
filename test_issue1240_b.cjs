const { readFileSync } = require('fs');
const content = readFileSync('frontend/src/services/subprocessoService.ts', 'utf8');
if (content.includes('verificarImpactosMapa')) {
    console.log('Found verificarImpactosMapa in subprocessoService.ts');
}
