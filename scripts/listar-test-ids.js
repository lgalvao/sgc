const fs = require('fs');
const path = require('path');

const searchDir = path.join(__dirname, 'frontend', 'src');
const extensions = ['.vue'];
const regex = /\b(data-test-id|test-id|data-testid)=["']([^"']+)["']/g;

function scanDirectory(directory) {
    let results = [];
    const files = fs.readdirSync(directory);

    for (const file of files) {
        const absolutePath = path.join(directory, file);
        const stat = fs.statSync(absolutePath);

        if (stat.isDirectory()) {
            results = results.concat(scanDirectory(absolutePath));
        } else if (extensions.includes(path.extname(absolutePath))) {
            const content = fs.readFileSync(absolutePath, 'utf-8');
            let match;
            while ((match = regex.exec(content)) !== null) {
                // match[0] é a string completa (ex: data-testid="valor")
                // match[1] é o nome do atributo (ex: data-testid)
                // match[2] é o valor (ex: valor)
                results.push({
                    file: path.relative(__dirname, absolutePath),
                    attribute: match[1],
                    value: match[2]
                });
            }
            
        }
    }
    return results;
}

console.log(`🔍 Buscando por test-ids em: ${searchDir}\n`);

try {
    const findings = scanDirectory(searchDir);
    
    if (findings.length === 0) {
        console.log("Nenhum test-id encontrado.");
    } else {
        // Agrupar por arquivo para facilitar a leitura
        const grouped = {};
        findings.forEach(item => {
            if (!grouped[item.file]) grouped[item.file] = [];
            grouped[item.file].push(`${item.attribute}="${item.value}"`);
        });

        for (const [file, ids] of Object.entries(grouped)) {
            console.log(`📄 ${file}`);
            ids.forEach(id => console.log(`   └─ ${id}`));
            console.log(''); // Linha em branco
        }
        
        console.log(`Total encontrado: ${findings.length}`);
    }
} catch (error) {
    console.error("Erro ao executar o script:", error.message);
}
