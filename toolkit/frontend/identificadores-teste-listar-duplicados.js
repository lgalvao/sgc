/* eslint-disable */
import fs from "node:fs";
import path from "node:path";
import {globby} from "globby";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";

function obterDiretorioBusca() {
    const indiceBase = process.argv.indexOf("--base");
    return indiceBase >= 0 && process.argv[indiceBase + 1]
        ? path.resolve(process.argv[indiceBase + 1])
        : path.join(resolverCaminhoConfigurado("frontend"), "src");
}

const diretorioBusca = obterDiretorioBusca();

const idsCompartilhadosPermitidos = new Set([
    'subprocesso-header__txt-header-unidade',
]);
// Melhora o regex para capturar data-testid estáticos e dinâmicos
// Suporta: data-testid="valor", :data-testid="'valor'", data-testid='valor'
const regex = /(^|[\s<])(:?)(data-test-id|test-id|data-testid)=("([^"]*)"|'([^']*)')/gm;

console.log(`Buscando por identificadores de teste em: ${diretorioBusca}\n`);

try {
    if (!fs.existsSync(diretorioBusca)) {
        throw new Error(`Diretorio frontend nao encontrado: ${path.relative(DIRETORIO_RAIZ, diretorioBusca)}`);
    }

    const padraoVue = path.join(diretorioBusca, "**/*.vue").replace(/\\/g, "/");
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
                file: path.relative(DIRETORIO_RAIZ, file).replaceAll("\\", "/"),
                attribute: (isDynamic ? ':' : '') + attrName,
                value: value
            });
        }
    }

    if (findings.length === 0) {
        console.log("Nenhum identificador de teste encontrado.");
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
    console.log("Nenhum identificador de teste duplicado encontrado.");
        process.exit(0);
    }

    console.log("Identificadores de teste duplicados encontrados:\n");
    duplicates.forEach(([value, items]) => {
        console.log(`>> "${value}" — ${items.length} ocorrências`);
        items.forEach(it => {
            if (it.attribute !== 'data-testid') console.log(`   └─ ${it.file} (${it.attribute})`);
            else console.log(`   └─ ${it.file}`);
        });
        console.log('');
    });

    const totalOccurrences = duplicates.reduce((sum, [, items]) => sum + items.length, 0);
    console.log(`Total de identificadores duplicados distintos: ${duplicates.length}`);
    console.log(`Total de ocorrencias duplicadas: ${totalOccurrences}`);

    // Falha o processo para uso como portão de qualidade no CI
    process.exit(1);

} catch (error) {
    console.error('Erro ao executar o script:', error.message);
    process.exit(1);
}
