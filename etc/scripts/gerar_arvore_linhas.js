import {execSync} from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';

function getFiles() {
    const output = execSync('git ls-files', {encoding: 'utf-8', maxBuffer: 1024 * 1024 * 10});
    return output.trim().split(/\r?\n/).filter(Boolean);
}

function countLines(filePath) {
    if (!fs.existsSync(filePath)) return 0;
    const content = fs.readFileSync(filePath, 'utf-8');
    return content.split(/\r?\n/).length;
}

function buildTree(fileList) {
    const root = {name: '.', count: 0, children: {}, isDir: true};

    fileList.forEach(filePath => {
        const normalizedPath = filePath.replaceAll('\\', '/');
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

function getTreeConnectors(isRoot, isLast) {
    if (isRoot) {
        return {connector: '', childPrefix: ''};
    }
    return {
        connector: isLast ? '└── ' : '├── ',
        childPrefix: isLast ? '    ' : '│   '
    };
}

function printNode(node, prefix, connector) {
    const reset = '\x1b[0m';
    const blue = '\x1b[34m'; // Diretórios
    const green = '\x1b[32m'; // Arquivos
    const yellow = '\x1b[33m'; // Números

    const color = node.isDir ? blue : green;
    const icon = node.isDir ? '📁' : '📄';
    const nameLine = `${prefix}${connector}${icon} ${color}${node.name}${reset} ${yellow}[${node.count.toLocaleString()}]${reset}`;
    console.log(nameLine);
}

function printTree(node, options, prefix = '', isLast = true, isRoot = true, currentDepth = 0) {
    const {maxDepth, minLines} = options;

    if (minLines !== undefined && node.count < minLines) return;

    const {connector, childPrefix} = getTreeConnectors(isRoot, isLast);

    const isRootDot = isRoot && node.name === '.';
    if (!isRootDot) {
        printNode(node, prefix, connector);
    }

    if (!node.isDir) return;
    if (maxDepth !== undefined && currentDepth >= maxDepth) return;

    let childKeys = Object.keys(node.children);

    if (minLines !== undefined) {
        childKeys = childKeys.filter(key => node.children[key].count >= minLines);
    }

    childKeys.sort((a, b) => {
        const countDiff = node.children[b].count - node.children[a].count;
        return countDiff === 0 ? a.localeCompare(b) : countDiff;
    });

    childKeys.forEach((key, index) => {
        const isLastChild = index === childKeys.length - 1;
        printTree(node.children[key], options, prefix + childPrefix, isLastChild, false, currentDepth + 1);
    });
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
        options.maxDepth = Number.parseInt(args[depthIndex + 1], 10);
    }

    const minLinesIndex = args.indexOf('--min-lines');
    if (minLinesIndex !== -1 && args[minLinesIndex + 1]) {
        options.minLines = Number.parseInt(args[minLinesIndex + 1], 10);
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
    const normalizedPath = filePath.replaceAll('\\', '/');
    return testPatterns.some(pattern => pattern.test(normalizedPath));
}

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
