/* eslint-disable */
import fs from "fs";
import path from "path";
import {globby} from "globby";

const argBaseIdx = process.argv.indexOf('--base');
const searchDir = argBaseIdx >= 0 && process.argv[argBaseIdx + 1]
    ? path.resolve(process.argv[argBaseIdx + 1])
    : path.join(import.meta.dirname, '../../../frontend/src');

const idsCompartilhadosPermitidos = new Set([
    'subprocesso-header__txt-header-unidade',
]);
// Melhora o regex para capturar data-testid estáticos e dinâmicos
// Suporta: data-testid="valor", :data-testid="'valor'", data-testid='valor'
const regex = /(^|[\s<])(:?)(data-test-id|test-id|data-testid)=("([^"]*)"|'([^']*)')/gm;

console.log(`Buscando por test-ids em: ${searchDir}\n`);

try {
    const padraoVue = path.join(searchDir, '**/*.vue').replace(/\\/g, '/');
    const files = await globby(padraoVue, {absolute: true});

    const findings = [];
    for (const file of files) {
        const content = fs.readFileSync(file, 'utf-8');
        let match;
        regex.lastIndex = 0;
        while ((match = regex.exec(content)) !== null) {
            const attrName = match[3];
            let value = match[5] || match[6] || '';

            // Se for um binding dinâmico (começa com :), só aceitamos literal estático.
            // Ex: :data-testid="'meu-id'" -> meu-id
            // Ex: :data-testid="dataTestid" -> ignorado, pois o valor real vem por prop/estado
            const isDynamic = match[2] === ':';
            if (isDynamic) {
                const literalMatch = value.match(/^['"]([^'"]+)['"]$/);
                if (literalMatch) {
                    value = literalMatch[1];
                } else {
                    continue;
                }
            }

            findings.push({
                file: path.relative(import.meta.dirname, file),
                attribute: (isDynamic ? ':' : '') + attrName,
                value: value
            });
        }
    }

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
        .filter(([value]) => !idsCompartilhadosPermitidos.has(value))
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

    // Falha o processo para uso como portão de qualidade no CI
    process.exit(1);

} catch (error) {
    console.error('Erro ao executar o script:', error.message);
    process.exit(1);
}
