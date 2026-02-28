const { readFileSync } = require('fs');
const content = readFileSync('backend/src/main/java/sgc/subprocesso/service/SubprocessoService.java', 'utf8');
const lines = content.split('\n');
const start = lines.findIndex(l => l.includes('ImpactoMapaResponse verificarImpactos'));
if (start !== -1) {
    for (let i = start; i < start + 30; i++) {
        console.log(lines[i]);
    }
} else {
    console.log("Not found ImpactoMapaResponse in SubprocessoService.java");
}
