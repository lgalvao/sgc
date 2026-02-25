import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const SOURCE_DIR = path.join(__dirname, '../../src/main/java/sgc');
const AUDIT_FILE = path.join(__dirname, '../../../null-checks-audit.txt');
const ANALYSIS_FILE = path.join(__dirname, '../../../null-checks-analysis.md');

function getAllFiles(dirPath, arrayOfFiles) {
    const files = fs.readdirSync(dirPath);

    arrayOfFiles = arrayOfFiles || [];

    files.forEach(function(file) {
        if (fs.statSync(dirPath + "/" + file).isDirectory()) {
            arrayOfFiles = getAllFiles(dirPath + "/" + file, arrayOfFiles);
        } else if (file.endsWith('.java')) {
                arrayOfFiles.push(path.join(dirPath, "/", file));
            }
    });

    return arrayOfFiles;
}

function classify(lines, index, content) {
    const contextRange = 20;
    const start = Math.max(0, index - contextRange);
    const contextLines = lines.slice(start, index + 1).join('\n');

    if (contextLines.includes('@Nullable')) {
        return "MAYBE_LEGIT";
    }

    return "POTENTIALLY_REDUNDANT";
}

function scanFiles() {
    const results = {};
    const files = getAllFiles(SOURCE_DIR);

    files.forEach(file => {
        try {
            const content = fs.readFileSync(file, 'utf-8');
            const lines = content.split(/\r?\n/);
            const fileResults = [];

            lines.forEach((line, i) => {
                if (line.includes('!= null') || line.includes('== null')) {
                    const stripped = line.trim();
                    if (stripped.startsWith('//') || stripped.startsWith('*')) return;

                    const category = classify(lines, i, stripped);
                    fileResults.push({
                        line: i + 1,
                        content: stripped,
                        category: category
                    });
                }
            });

            if (fileResults.length > 0) {
                results[file] = fileResults;
            }
        } catch (e) {
            console.error(`Erro ao ler ${file}:`, e);
        }
    });

    return results;
}

function generateReport(results) {
    let auditContent = '';
    
    Object.keys(results).forEach(filePath => {
        auditContent += `File: ${filePath}\n`;
        results[filePath].forEach(item => {
            auditContent += `  L${item.line} [${item.category}]: ${item.content}\n`;
        });
        auditContent += '\n';
    });

    fs.writeFileSync(AUDIT_FILE, auditContent, 'utf-8');

    let mdContent = `# Null Checks Analysis\n\n`;
    mdContent += `| Class | Total Checks | Potentially Redundant |\n`;
    mdContent += `|-------|--------------|-----------------------|\n`;

    const sortedFiles = Object.entries(results).sort((a, b) => b[1].length - a[1].length);

    sortedFiles.forEach(([filePath, items]) => {
        const filename = path.basename(filePath);
        const total = items.length;
        const redundant = items.filter(x => x.category === 'POTENTIALLY_REDUNDANT').length;
        mdContent += `| ${filename} | ${total} | ${redundant} |\n`;
    });

    fs.writeFileSync(ANALYSIS_FILE, mdContent, 'utf-8');
}

console.log("Scanning files for null checks...");
const data = scanFiles();
console.log(`Found null checks in ${Object.keys(data).length} files.`);
generateReport(data);
console.log(`Reports generated: ${AUDIT_FILE}, ${ANALYSIS_FILE}`);
