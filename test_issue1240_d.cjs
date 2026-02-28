const { readFileSync } = require('fs');
const content = readFileSync('backend/src/main/java/sgc/mapa/model/AtividadeImpactadaDto.java', 'utf8');
console.log(content);
