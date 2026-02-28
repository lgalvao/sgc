const { readFileSync } = require('fs');
const content = readFileSync('backend/src/main/java/sgc/mapa/dto/ImpactoMapaResponse.java', 'utf8');
console.log(content.match(/boolean temImpactos/));
