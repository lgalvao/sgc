import fs from "node:fs/promises";
import pc from "picocolors";
import {execSync} from "node:child_process";

async function main() {
    const file = process.argv[2];
    if (!file) {
        console.error("Uso: node toolkit/frontend/gap-detector.js <arquivo>");
        process.exit(1);
    }

    console.log(pc.blue(`Analizando gaps de cobertura para: ${file}\n`));

    let output;
    try {
        output = execSync(`cd frontend && npx vitest run src/composables/__tests__/$(basename ${file} .ts).spec.ts --coverage --reporter=dot --no-color --coverage.reporter=text --coverage.include="${file}"`, {encoding: 'utf8', stdio: ['pipe', 'pipe', 'pipe']});
    } catch (e) {
        // Vitest retorna exit code 1 se thresholds falharem
        output = e.stdout || e.message;
    }

    const lines = output.split('\n');
    const fileLine = lines.find(l => l.includes(file.split('/').pop()));
    
    if (!fileLine) {
        console.log(pc.red("Não foi possível encontrar dados de cobertura para o arquivo especificado."));
        console.log(output);
        process.exit(1);
    }

    const parts = fileLine.split('|');
    const uncoveredLineNumbers = parts[parts.length - 1].trim();

    if (!uncoveredLineNumbers) {
        console.log(pc.green("✓ 100% de cobertura de linhas!"));
        return;
    }

    console.log(pc.yellow(`Linhas não cobertas: ${uncoveredLineNumbers}\n`));

    const content = await fs.readFile(file, 'utf8');
    const contentLines = content.split('\n');

    const ranges = uncoveredLineNumbers.split(',');
    for (const range of ranges) {
        if (range.includes('-')) {
            const [start, end] = range.split('-').map(Number);
            console.log(pc.bold(`\n--- Bloco ${start}-${end} ---`));
            for (let i = start; i <= end; i++) {
                console.log(`${i.toString().padStart(4)}: ${contentLines[i-1]}`);
            }
        } else if (range.trim()) {
            const line = Number(range);
            console.log(`${line.toString().padStart(4)}: ${contentLines[line-1]}`);
        }
    }
}

main();
