const { execSync } = require('child_process');
const fs = require('fs');

function getFileStats() {
    try {
        // Tenta usar git ls-files para respeitar .gitignore
        // O comando wc -l imprime "linhas  caminho"
        // xargs garante que lidamos com muitos arquivos sem estourar o limite de args
        const output = execSync('git ls-files | xargs wc -l', { encoding: 'utf-8', maxBuffer: 1024 * 1024 * 10 });
        return output.trim().split('\n');
    } catch (e) {
        console.log("Git não encontrado ou erro ao executar. Tentando fallback para 'find'...");
        // Fallback simplificado
        const output = execSync('find . -type f -not -path "*/node_modules/*" -not -path "*/.git/*" | xargs wc -l', { encoding: 'utf-8' });
        return output.trim().split('\n');
    }
}

function buildTree(lines) {
    const root = { name: '.', count: 0, children: {}, isDir: true };

    lines.forEach(line => {
        line = line.trim();
        if (!line) return;
        
        // wc -l output format: "  123 path/to/file"
        const match = line.match(/^\s*(\d+)\s+(.+)$/);
        if (!match) return; // Ignora linha de 'total' ou linhas malformadas

        const count = parseInt(match[1], 10);
        const path = match[2];
        
        if (path === 'total') return; // Ignora a linha final do wc

        const parts = path.split('/');
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

function printTree(node, prefix = '', isLast = true, isRoot = true) {
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
        const childKeys = Object.keys(node.children).sort((a, b) => {
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
            printTree(node.children[key], prefix + childPrefix, index === childKeys.length - 1, false);
        });
    }
}

// Execução
console.log("Gerando árvore de contagem de linhas...\n");
const rawLines = getFileStats();
const tree = buildTree(rawLines);
calculateTotals(tree);

console.log(`\x1b[1mProjeto: ${process.cwd().split('/').pop()}\x1b[0m`);
console.log(`\x1b[1mTotal de Linhas: \x1b[33m${tree.count.toLocaleString()}\x1b[0m\n`);

printTree(tree);
