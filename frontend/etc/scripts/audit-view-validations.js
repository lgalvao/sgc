/* eslint-disable */
const fs = require('fs');
const path = require('path');

const SOURCE_DIR = path.join(__dirname, '../../backend/src/main/java/sgc');
const AUDIT_FILE = path.join(__dirname, '../../view-validations-audit.md');

const GUARANTEED_FIELDS = [
    // Unidade
    { getter: 'getSigla', entity: 'Unidade', source: 'VW_UNIDADE' },
    { getter: 'getNome', entity: 'Unidade', source: 'VW_UNIDADE' },
    { getter: 'getTipo', entity: 'Unidade', source: 'VW_UNIDADE' },

    // Usuario
    { getter: 'getUnidadeLotacao', entity: 'Usuario', source: 'VW_USUARIO' },
    { getter: 'getTituloEleitoral', entity: 'Usuario', source: 'VW_USUARIO' },
    { getter: 'getUnidadeCompetencia', entity: 'Usuario', source: 'VW_USUARIO' },
];

function getAllFiles(dirPath, arrayOfFiles) {
    const files = fs.readdirSync(dirPath);
    arrayOfFiles = arrayOfFiles || [];

    files.forEach(function(file) {
        if (fs.statSync(dirPath + "/" + file).isDirectory()) {
            arrayOfFiles = getAllFiles(dirPath + "/" + file, arrayOfFiles);
        } else {
            if (file.endsWith('.java')) {
                arrayOfFiles.push(path.join(dirPath, "/", file));
            }
        }
    });

    return arrayOfFiles;
}

function scanFiles() {
    const results = [];
    const files = getAllFiles(SOURCE_DIR);

    files.forEach(file => {
        try {
            const content = fs.readFileSync(file, 'utf-8');
            const lines = content.split(/\r?\n/);

            lines.forEach((line, i) => {
                for (const check of GUARANTEED_FIELDS) {
                    const getter = check.getter;
                    // Regex matches: getSigla() != null, getSigla() == null
                    // Also matches: getSigla() == null
                    // Using regex to match getter followed by null check
                    const pattern = new RegExp(`${getter}\(\)\s*(!=|==)\s*null`);

                    if (pattern.test(line)) {
                        results.push({
                            file: file,
                            line: i + 1,
                            content: line.trim(),
                            field: getter,
                            guarantee: check.source
                        });
                    }
                }
            });
        } catch (e) {
            console.error(`Error reading ${file}:`, e);
        }
    });

    return results;
}

function generateReport(results) {
    let md = "# View Validations Audit\n\n";
    md += "Validations found on fields guaranteed by Database Views:\n\n";

    if (results.length === 0) {
        md += "No redundant validations found.\n";
    } else {
        // Group by file
        const files = {};
        results.forEach(item => {
            if (!files[item.file]) files[item.file] = [];
            files[item.file].push(item);
        });

        Object.keys(files).forEach(filePath => {
            md += `### ${path.basename(filePath)}\n`;
            md += '`' + filePath + '`\n\n';
            md += "| Line | Field | Source View | Code |\n";
            md += "|------|-------|-------------|------|\n";
            
            files[filePath].forEach(item => {
                let content = item.content.replace(/\|/g, '\\|');
                // Use concatenation to avoid template literal issues
                md += "| " + item.line + " | `" + item.field + "()` | " + item.guarantee + " | `" + content + "` |\n";
            });
            md += "\n";
        });
    }

    fs.writeFileSync(AUDIT_FILE, md, 'utf-8');
}

console.log("Scanning for redundant view validations...");
const data = scanFiles();
generateReport(data);
console.log(`Report generated: ${AUDIT_FILE}`);
