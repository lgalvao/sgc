/* eslint-disable */
const fs = require('fs');
const path = require('path');

const searchDir = path.join(__dirname, '../../src');
const extensions = ['.vue'];
// Melhora o regex para capturar data-testid estáticos e dinâmicos
// Suporta: data-testid="valor", :data-testid="'valor'", data-testid='valor'
const regex = /\b:?(data-test-id|test-id|data-testid)=("([^"]*)"|'([^']*)')/g;

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
                const attrName = match[1];
                let value = match[3] || match[4] || '';

                // Se for um binding dinâmico (começa com :), tentamos extrair o literal se for simples
                // Ex: :data-testid="'meu-id'" -> meu-id
                // Se for complexo (ex: prop || 'padrao'), ignoramos para evitar falsos positivos
                const isDynamic = match[0].startsWith(':');
                if (isDynamic) {
                    const literalMatch = value.match(/^['"]([^'"]+)['"]$/);
                    if (literalMatch) {
                        value = literalMatch[1];
                    } else if (value.includes('||') || value.includes('?') || value.includes('!')) {
                        // Ignora expressões complexas pois o test-id real depende do estado
                        continue;
                    }
                }

                results.push({
                    file: path.relative(__dirname, absolutePath),
                    attribute: (isDynamic ? ':' : '') + attrName,
                    value: value
                });
            }
        }
    }
    return results;
}

console.log(`Buscando por test-ids em: ${searchDir}\n`);

try {
    const findings = scanDirectory(searchDir);

    if (findings.length === 0) {
        console.log('Nenhum test-id encontrado.');
        process.exit(0);
    }

    // Agrupar por valor do test-id
    const groupedByValue = {};
    findings.forEach(item => {
        if (!groupedByValue[item.value]) groupedByValue[item.value] = [];
        groupedByValue[item.value].push(item);
    });

    // Filtrar apenas duplicados
    const duplicates = Object.entries(groupedByValue)
        .filter(([value, items]) => items.length > 1)
        .sort((a, b) => b[1].length - a[1].length);

    if (duplicates.length === 0) {
        console.log('Nenhum test-id duplicado encontrado.');
        process.exit(0);
    }

    console.log('Test-ids duplicados encontrados:\n');
    duplicates.forEach(([value, items]) => {
        console.log(`>> "${value}" — ${items.length} ocorrências`);
        items.forEach(it => {
            if (it.attribute !== 'data-testid') console.log(`   └─ ${it.file} (${it.attribute})`);
            else console.log(`   └─ ${it.file}`);
        });
        console.log('');
    });

    const totalOccurrences = duplicates.reduce((sum, [, items]) => sum + items.length, 0);
    console.log(`Total de ids duplicados distintos: ${duplicates.length}`);
    console.log(`Total de ocorrências duplicadas: ${totalOccurrences}`);

} catch (error) {
    console.error('Erro ao executar o script:', error.message);
    process.exit(1);
}
