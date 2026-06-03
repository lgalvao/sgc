const { exec } = require('child_process');
exec('npx oxlint --format json', { maxBuffer: 10 * 1024 * 1024 }, (err, stdout, stderr) => {
    try {
        const parsed = JSON.parse(stdout);
        const errs = parsed.filter ? parsed : parsed.diagnostics; 
        for (const i of errs || []) {
            if (i.severity === 'error') {
                console.log(`${i.code}: ${i.filename}:${i.labels?.[0]?.span?.line}`);
            }
        }
    } catch(e) {
        console.error("Parse error", e);
    }
});
