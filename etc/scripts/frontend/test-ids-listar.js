import fs from "fs";
import path from "path";
import { globby } from "globby";

const argBaseIdx = process.argv.indexOf('--base');
const searchDir = argBaseIdx >= 0 && process.argv[argBaseIdx + 1]
    ? path.resolve(process.argv[argBaseIdx + 1])
    : path.join(import.meta.dirname, '../../../frontend/src');

const regex = /\b(data-test-codigo|test-codigo|data-testid)=["']([^"']+)["']/g;

console.log(`🔍 Buscando por test-ids em: ${searchDir}\n`);

try {
    const padraoVue = path.join(searchDir, '**/*.vue').replace(/\\/g, '/');
    const files = await globby(padraoVue, { absolute: true });

    const findings = [];
    for (const file of files) {
        const content = fs.readFileSync(file, 'utf-8');
        let match;
        regex.lastIndex = 0;
        while ((match = regex.exec(content)) !== null) {
            findings.push({
                file: path.relative(import.meta.dirname, file),
                attribute: match[1],
                value: match[2]
            });
        }
    }

    if (findings.length === 0) {
        console.log("Nenhum test-codigo encontrado.");
    } else {
        // Agrupar por arquivo para facilitar a leitura
        const grouped = {};
        findings.forEach(item => {
            if (!grouped[item.file]) grouped[item.file] = [];
            grouped[item.file].push(`${item.attribute}="${item.value}"`);
        });

        for (const [file, ids] of Object.entries(grouped)) {
            console.log(`📄 ${file}`);
            ids.forEach(codigo => console.log(`   └─ ${codigo}`));
            console.log(''); // Linha em branco
        }

        console.log(`Total encontrado: ${findings.length}`);
    }
} catch (error) {
    console.error("Erro ao executar o script:", error.message);
}
