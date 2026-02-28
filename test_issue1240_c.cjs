const { readFileSync } = require('fs');
const content = readFileSync('frontend/src/types/tipos.ts', 'utf8');
if (content.includes('ImpactoMapa')) {
    console.log('Found ImpactoMapa in tipos.ts');
}
