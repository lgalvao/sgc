const { readFileSync } = require('fs');
const content = readFileSync('frontend/src/stores/mapas.ts', 'utf8');
if (content.includes('buscarImpactoMapa')) {
    console.log('Found buscarImpactoMapa in mapas.ts');
}
