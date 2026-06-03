const fs = require('fs');
const data = JSON.parse(fs.readFileSync('oxlint.json', 'utf8'));
data.forEach(i => {
    if (i.severity === 'error') {
        console.log(`${i.code}: ${i.filename}:${i.labels?.[0]?.span?.line}`);
    }
});
