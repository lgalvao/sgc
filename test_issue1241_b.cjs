const { readFileSync } = require('fs');
const content = readFileSync('backend/src/main/java/sgc/processo/service/ProcessoDetalheBuilder.java', 'utf8');
const lines = content.split('\n');
const start = lines.findIndex(l => l.includes('ProcessoDetalheDto.builder()'));
if (start !== -1) {
    for (let i = start - 10; i < start + 25; i++) {
        console.log(lines[i]);
    }
}
