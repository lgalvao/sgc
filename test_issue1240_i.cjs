const { readFileSync } = require('fs');
const content = readFileSync('backend/src/main/java/sgc/mapa/service/ImpactoMapaService.java', 'utf8');
const lines = content.split('\n');
const start = lines.findIndex(l => l.includes('verificarImpactos(Subprocesso'));
if (start !== -1) {
    for (let i = start; i < start + 60; i++) {
        console.log(lines[i]);
    }
} else {
    console.log("Not found in ImpactoMapaService.java");
}
