const {execSync} = require('child_process');
const fs = require('fs');
const path = require('path');

function getFiles() {
    try {
        // Tenta usar git ls-files para listar arquivos rastreados
        const output = execSync('git ls-files', {encoding: 'utf-8', maxBuffer: 1024 * 1024 * 10});
        return output.trim().split(/\r?\n/).filter(line => line);
    } catch (e) {
        console.log("Git não encontrado ou erro ao executar. Tentando varredura manual...");
        return walkDir('.');
    }
}

function walkDir(dir, fileList = []) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        if (file === '.git' || file === 'node_modules' || file === 'dist' || file === 'build' || file === '.gradle') continue;

        const filePath = path.join(dir, file);
        try {
            const stat = fs.statSync(filePath);
            if (stat.isDirectory()) {
                walkDir(filePath, fileList);
            } else {
                // Normaliza o caminho para usar /
                fileList.push(filePath.replace(/\\/g, '/'));
            }
        } catch (e) {
            // Ignora arquivos inacessíveis
        }
    }
    return fileList;
}

function countLines(filePath) {
    try {
        const content = fs.readFileSync(filePath, 'utf-8');
        // Conta linhas cheias + 1 se o arquivo não terminar com newline
        return content.split(/\r?\n/).length;
    } catch (e) {
        // Retorna 0 para arquivos binários ou erros de leitura
        return 0;
    }
}

function buildTree(fileList) {
    const root = {name: '.', count: 0, children: {}, isDir: true};

    fileList.forEach(filePath => {
        // Normaliza separadores
        const normalizedPath = filePath.replace(/\\/g, '/');
        const count = countLines(normalizedPath);

        const parts = normalizedPath.split('/');
        let current = root;

        parts.forEach((part, index) => {
            const isLast = index === parts.length - 1;

            if (!current.children[part]) {
                current.children[part] = {
                    name: part,
                    count: 0,
                    children: {},
                    isDir: !isLast
                };
            }
            current = current.children[part];

            if (isLast) {
                current.count = count;
            }
        });
    });

    return root;
}

function calculateTotals(node) {
    if (!node.isDir) {
        return node.count;
    }

    let sum = 0;
    for (const childName in node.children) {
        sum += calculateTotals(node.children[childName]);
    }
    node.count = sum;
    return sum;
}

function printTree(node, options, prefix = '', isLast = true, isRoot = true, currentDepth = 0) {
    const {maxDepth, minLines} = options;

    if (minLines !== undefined && node.count < minLines) return;

    // Formatação
    const connector = isRoot ? '' : (isLast ? '└── ' : '├── ');
    const childPrefix = isRoot ? '' : (isLast ? '    ' : '│   ');

    // Cores (ANSI escape codes)
    const reset = '\x1b[0m';
    const blue = '\x1b[34m'; // Diretórios
    const green = '\x1b[32m'; // Arquivos
    const yellow = '\x1b[33m'; // Números

    const color = node.isDir ? blue : green;
    const icon = node.isDir ? '📁' : '📄';
    const nameLine = `${prefix}${connector}${icon} ${color}${node.name}${reset} ${yellow}[${node.count.toLocaleString()}]${reset}`;

    if (!isRoot || (isRoot && node.name !== '.')) {
        console.log(nameLine);
    }

    if (node.isDir) {
        if (maxDepth !== undefined && currentDepth >= maxDepth) return;

        let childKeys = Object.keys(node.children);

        if (minLines !== undefined) {
            childKeys = childKeys.filter(key => node.children[key].count >= minLines);
        }

        childKeys.sort((a, b) => {
            const nodeA = node.children[a];
            const nodeB = node.children[b];

            // Ordena por contagem (maior para menor)
            if (nodeB.count !== nodeA.count) {
                return nodeB.count - nodeA.count;
            }

            // Em caso de empate na contagem, ordena alfabeticamente
            return a.localeCompare(b);
        });

        childKeys.forEach((key, index) => {
            printTree(node.children[key], options, prefix + childPrefix, index === childKeys.length - 1, false, currentDepth + 1);
        });
    }
}

function parseArgs() {
    const args = process.argv.slice(2);
    const options = {};

    if (args.includes('--help') || args.includes('-h')) {
        console.log(`
Uso: node scripts/gerar_arvore_linhas.js [opções]

Opções:
  --depth <n>          Limita a profundidade da árvore exibida (ex: --depth 2)
  --min-lines <n>      Exibe apenas itens com no mínimo n linhas
  --exclude-tests      Exclui arquivos de teste da contagem e da árvore
  --help, -h           Exibe esta mensagem de ajuda
`);
        process.exit(0);
    }

    const depthIndex = args.indexOf('--depth');
    if (depthIndex !== -1 && args[depthIndex + 1]) {
        options.maxDepth = parseInt(args[depthIndex + 1], 10);
    }

    const minLinesIndex = args.indexOf('--min-lines');
    if (minLinesIndex !== -1 && args[minLinesIndex + 1]) {
        options.minLines = parseInt(args[minLinesIndex + 1], 10);
    }

    if (args.includes('--exclude-tests')) {
        options.excludeTests = true;
    }

    return options;
}

// Padrões para identificar arquivos/diretórios de teste
const testPatterns = [
    /\.spec\.(js|ts|vue)$/,
    /\.test\.(js|ts|vue)$/,
    /__tests__\//,
    /e2e\//,
    /frontend\/src\/__tests__\//,
    /backend\/src\/test\//,
];

function isTestFile(filePath) {
    // Normaliza para barras para correspondência de padrão
    const normalizedPath = filePath.replace(/\\/g, '/');
    return testPatterns.some(pattern => pattern.test(normalizedPath));
}

// Execução
const options = parseArgs();

console.log("Gerando árvore de contagem de linhas...\n");
let fileList = getFiles();

if (options.excludeTests) {
    const originalCount = fileList.length;
    fileList = fileList.filter(filePath => !isTestFile(filePath));
    console.log(`Excluídos ${originalCount - fileList.length} arquivos de teste.`);
}
const tree = buildTree(fileList);
calculateTotals(tree);

console.log(`\x1b[1mProjeto: ${process.cwd().split(path.sep).pop()}\x1b[0m`);
console.log(`\x1b[1mTotal de Linhas: \x1b[33m${tree.count.toLocaleString()}\x1b[0m\n`);

printTree(tree, options);
