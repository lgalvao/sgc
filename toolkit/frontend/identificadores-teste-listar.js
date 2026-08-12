import fs from "fs";
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

const regex = /\b(data-test-codigo|test-codigo|data-testid)=["']([^"']+)["']/g;

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
            findings.push({
                file: path.relative(DIRETORIO_RAIZ, file).replaceAll("\\", "/"),
                attribute: match[1],
                value: match[2]
            });
        }
    }

    if (findings.length === 0) {
        console.log("Nenhum identificador de teste encontrado.");
    } else {
        // Agrupar por arquivo para facilitar a leitura
        const grouped = {};
        findings.forEach(item => {
            if (!grouped[item.file]) grouped[item.file] = [];
            grouped[item.file].push(`${item.attribute}="${item.value}"`);
        });

        for (const [file, ids] of Object.entries(grouped)) {
            console.log(`Arquivo: ${file}`);
            ids.forEach(codigo => console.log(`   └─ ${codigo}`));
            console.log(''); // Linha em branco
        }

        console.log(`Total encontrado: ${findings.length}`);
    }
} catch (error) {
    console.error("Erro ao executar o script:", error.message);
    process.exitCode = 1;
}
