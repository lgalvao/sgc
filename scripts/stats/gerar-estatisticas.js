const fs = require('fs');
const { glob } = require('glob');
const sloc = require('sloc');
const path = require('path');

// Caminhos relativos à pasta scripts/stats
const PATHS = {
    backend: '../../backend/src/main/java/**/*.java',
    frontend: '../../frontend/src/**/*.{js,ts,vue}'
};

function contarMetodosJava(conteudo) {
    // Heurística para métodos Java
    const regex = /(public|protected|private|static|\s) +[\w<>[\]]+\s+(\w+) *\([^\)]*\)\s*(throws\s+[\w,\s]+)?\s*\{/g;
    let count = 0;
    let match;
    while ((match = regex.exec(conteudo)) !== null) {
        const nome = match[2];
        if (!['if', 'for', 'while', 'switch', 'catch', 'synchronized'].includes(nome)) {
             count++;
        }
    }
    return count;
}

function contarFuncoesJS(conteudo) {
    // 1. function nome()
    const functionRegex = /function\s+\w+/g;
    // 2. Arrow functions =>
    const arrowRegex = /=>/g;
    // 3. Métodos de classe/objeto nome() {
    const methodRegex = /(\w+)\s*\([^\)]*\)\s*\{/g;

    let count = 0;
    count += (conteudo.match(functionRegex) || []).length;
    count += (conteudo.match(arrowRegex) || []).length;

    let match;
    while ((match = methodRegex.exec(conteudo)) !== null) {
        const nome = match[1];
        if (!['if', 'for', 'while', 'switch', 'catch', 'constructor', 'data', 'setup', 'function'].includes(nome)) {
             count++;
        }
    }
    return count;
}

async function processarArquivos(pattern, tipo) {
    const files = await glob(pattern, { cwd: __dirname });

    let stats = {
        arquivos: 0,
        linhasTotal: 0,
        linhasCodigo: 0,
        comentarios: 0,
        metodos: 0
    };

    for (const file of files) {
        const filePath = path.join(__dirname, file);
        if (fs.statSync(filePath).isDirectory()) continue;

        const content = fs.readFileSync(filePath, 'utf8');
        const ext = path.extname(file).replace('.', '');

        stats.arquivos++;

        // SLOC
        let slocExt = ext;
        if (ext === 'vue') slocExt = 'html';

        try {
            const metrics = sloc(content, slocExt);
            stats.linhasTotal += metrics.total;
            stats.linhasCodigo += metrics.source;
            stats.comentarios += metrics.comment;
        } catch (e) {
            stats.linhasTotal += content.split('\n').length;
        }

        // Métodos
        if (tipo === 'backend') {
            stats.metodos += contarMetodosJava(content);
        } else {
            if (ext === 'vue') {
                const scriptMatch = content.match(/<script[^>]*>([\s\S]*?)<\/script>/);
                if (scriptMatch) {
                     stats.metodos += contarFuncoesJS(scriptMatch[1]);
                }
            } else {
                stats.metodos += contarFuncoesJS(content);
            }
        }
    }

    return stats;
}

async function run() {
    console.log("Gerando estatísticas do projeto SGC...\n");

    try {
        const backendStats = await processarArquivos(PATHS.backend, 'backend');
        const frontendStats = await processarArquivos(PATHS.frontend, 'frontend');

        const imprimir = (titulo, s) => {
            console.log(`### ${titulo} ###`);
            console.log(`Arquivos: ${s.arquivos}`);
            console.log(`Linhas Totais: ${s.linhasTotal}`);
            console.log(`Linhas de Código (Source): ${s.linhasCodigo}`);
            console.log(`Comentários: ${s.comentarios}`);
            console.log(`Métodos/Funções (Estimado): ${s.metodos}`);
            console.log('-----------------------------------');
        };

        imprimir("Backend (Java)", backendStats);
        imprimir("Frontend (Vue/TS/JS)", frontendStats);

        const total = {
            arquivos: backendStats.arquivos + frontendStats.arquivos,
            linhasTotal: backendStats.linhasTotal + frontendStats.linhasTotal,
            linhasCodigo: backendStats.linhasCodigo + frontendStats.linhasCodigo,
            comentarios: backendStats.comentarios + frontendStats.comentarios,
            metodos: backendStats.metodos + frontendStats.metodos
        };

        imprimir("TOTAL DO PROJETO", total);

    } catch (e) {
        console.error("Erro ao gerar estatísticas:", e);
    }
}

run();
