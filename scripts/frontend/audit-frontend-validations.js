const fs = require('fs');
const path = require('path');

const FRONTEND_DIR = path.join(__dirname, '../../frontend/src');
const BACKEND_DIR = path.join(__dirname, '../../backend/src/main/java/sgc');
const REPORT_FILE = path.join(__dirname, '../../frontend-backend-validation-comparison.md');

function getAllFiles(dirPath, ext, arrayOfFiles) {
    const files = fs.readdirSync(dirPath);
    arrayOfFiles = arrayOfFiles || [];

    files.forEach(function(file) {
        if (fs.statSync(dirPath + "/" + file).isDirectory()) {
            arrayOfFiles = getAllFiles(dirPath + "/" + file, ext, arrayOfFiles);
        } else {
            if (file.endsWith(ext)) {
                arrayOfFiles.push(path.join(dirPath, "/", file));
            }
        }
    });

    return arrayOfFiles;
}

function scanFrontend() {
    const results = [];
    const files = getAllFiles(FRONTEND_DIR, '.vue');

    files.forEach(file => {
        try {
            const content = fs.readFileSync(file, 'utf-8');
            const lines = content.split(/\r?\n/);

            lines.forEach((line, i) => {
                if (line.includes(':disabled')) {
                    results.push({
                        file: file,
                        line: i + 1,
                        type: 'disabled_check',
                        content: line.trim()
                    });
                }
                if (line.includes('required') && line.includes('<')) {
                    results.push({
                        file: file,
                        line: i + 1,
                        type: 'html_required',
                        content: line.trim()
                    });
                }
                if (line.includes(':rules') || line.includes('v-model')) {
                    const lower = line.toLowerCase();
                    if (lower.includes('rule') || lower.includes('valid')) {
                        results.push({
                            file: file,
                            line: i + 1,
                            type: 'vue_rule',
                            content: line.trim()
                        });
                    }
                }
            });
        } catch (e) {
            console.error(e);
        }
    });
    return results;
}

function scanBackend() {
    const results = [];
    const files = getAllFiles(BACKEND_DIR, '.java');

    files.forEach(file => {
        // Filter only DTOs/Requests
        if (!file.includes('Dto') && !file.includes('Request')) return;

        try {
            const content = fs.readFileSync(file, 'utf-8');
            const lines = content.split(/\r?\n/);

            lines.forEach((line, i) => {
                if (['@NotNull', '@NotEmpty', '@NotBlank', '@Size'].some(ann => line.includes(ann))) {
                    results.push({
                        file: file,
                        line: i + 1,
                        type: 'bean_validation',
                        content: line.trim()
                    });
                }
            });
        } catch (e) {
            console.error(e);
        }
    });
    return results;
}

function generateReport(feData, beData) {
    let md = "# Frontend vs Backend Validation Audit\n\n";

    md += "## Backend Validations (DTOs)\n";
    md += "| File | Line | Annotation | Code |\n";
    md += "|------|------|------------|------|\n";
    
    beData.forEach(item => {
        const annotation = item.content.split('(')[0].split(' ')[0];
        // Using concatenation to avoid backtick hell
        md += "| " + path.basename(item.file) + " | " + item.line + " | " + annotation + " | `" + item.content + "` |\n";
    });

    md += "\n## Frontend Validations (Vue)\n";
    md += "| File | Line | Type | Code |\n";
    md += "|------|------|------|------|\n";
    
    feData.forEach(item => {
        let content = item.content;
        if (content.length > 100) content = content.substring(0, 100) + '...';
        content = content.replace(/\|/g, '\\|'); // Escape pipe for markdown table
        md += "| " + path.basename(item.file) + " | " + item.line + " | " + item.type + " | `" + content + "` |\n";
    });

    fs.writeFileSync(REPORT_FILE, md, 'utf-8');
}

console.log("Scanning validations...");
const fe = scanFrontend();
const be = scanBackend();
generateReport(fe, be);
console.log(`Report generated: ${REPORT_FILE}`);