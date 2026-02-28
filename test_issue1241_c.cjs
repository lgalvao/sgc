const { readFileSync } = require('fs');
const content = readFileSync('backend/src/main/java/sgc/seguranca/SgcPermissionEvaluator.java', 'utf8');
const lines = content.split('\n');
const start = lines.findIndex(l => l.includes('private boolean checkProcesso'));
if (start !== -1) {
    for (let i = start; i < start + 30; i++) {
        console.log(lines[i]);
    }
}
