import fs from "fs";
import path from "path";

const FRONTEND_DIR = path.join(import.meta.dirname, '../../../frontend/src');
const REPORT_FILE = path.join(import.meta.dirname, '../../../frontend-a11y-audit.md');

function getAllFiles(dirPath, ext, arrayOfFiles) {
    const files = fs.readdirSync(dirPath);
    arrayOfFiles = arrayOfFiles || [];

    files.forEach(function (file) {
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

function scanA11y() {
    const results = [];
    const files = getAllFiles(FRONTEND_DIR, '.vue');

    const INTERACTIVE_BASE = [
        'button', 'a', 'input', 'select', 'textarea', 'label', 'summary', 'router-link'
    ];
    
    // Explicit interactive components
    const INTERACTIVE_COMPONENTS = [
        // BootstrapVueNext
        'BButton', 'b-button', 'b-btn', 'BLink', 'b-link', 
        'BDropdownItem', 'b-dropdown-item', 'BDropdownItemButton', 'b-dropdown-item-button',
        'BFormInput', 'b-form-input', 'BFormSelect', 'b-form-select', 
        'BFormCheckbox', 'b-form-checkbox', 'BFormRadio', 'b-form-radio',
        'BFormTextarea', 'b-form-textarea', 'BFormTag', 'b-form-tag',
        // Vuetify
        'VBtn', 'v-btn', 'VTextField', 'v-text-field', 'VSelect', 'v-select',
        'VCheckbox', 'v-checkbox', 'VSwitch', 'v-switch', 'VListItem', 'v-list-item',
        // App specific
        'LoadingButton', 'loadingbutton', 'FeedbackButton', 'feedbackbutton'
    ];

    const isInteractive = (tagName) => {
        if (INTERACTIVE_BASE.includes(tagName.toLowerCase())) return true;
        if (INTERACTIVE_COMPONENTS.includes(tagName)) return true;
        // Handle kebab-case for custom components if needed
        if (INTERACTIVE_COMPONENTS.includes(tagName.toLowerCase())) return true;
        return false;
    };

    files.forEach(file => {
        try {
            const content = fs.readFileSync(file, 'utf-8');
            
            // Extract tags using a regex that handles multi-line
            const tagRegex = /<([a-zA-Z0-9-]+)([^>]*?)(\/?>)/gs;
            let match;
            
            while ((match = tagRegex.exec(content)) !== null) {
                const tagName = match[1];
                const attributes = match[2];
                const fullTag = match[0];
                const offset = match.index;
                const line = content.substring(0, offset).split('\n').length;

                // 1. Imagens sem alt
                if (tagName.toLowerCase() === 'img') {
                    if (!attributes.includes('alt=') && !attributes.includes(':alt=')) {
                        results.push({
                            file: file,
                            line: line,
                            type: 'img_missing_alt',
                            content: fullTag.replace(/\s+/g, ' ').substring(0, 100)
                        });
                    }
                }

                // 2. Botões com ícones apenas
                if (['button', 'BButton', 'v-btn', 'VBtn', 'b-button', 'loadingbutton', 'LoadingButton'].includes(tagName)) {
                    const hasLabel = attributes.includes('aria-label') || attributes.includes(':aria-label') || attributes.includes('title=') || attributes.includes(':title=') || attributes.includes('label=') || attributes.includes(':label=') || attributes.includes('text=') || attributes.includes(':text=');
                    
                    if (!hasLabel) {
                        const closingTag = `</${tagName}>`;
                        const startOfClosing = content.indexOf(closingTag, offset);
                        if (startOfClosing !== -1) {
                            const innerContent = content.substring(offset + fullTag.length, startOfClosing).trim();
                            const innerText = innerContent.replace(/<[^>]+>/g, '').trim();
                            if (innerText === '' && (innerContent.includes('<i') || innerContent.includes('<v-icon') || innerContent.includes('<b-icon') || innerContent.includes('bi-'))) {
                                results.push({
                                    file: file,
                                    line: line,
                                    type: 'button_missing_label',
                                    content: fullTag.replace(/\s+/g, ' ').substring(0, 100)
                                });
                            }
                        }
                    }
                }

                // 3. Inputs sem labels
                if (['input', 'v-text-field', 'VTextField', 'v-select', 'VSelect', 'BFormInput', 'BFormSelect', 'b-form-input', 'b-form-select'].includes(tagName)) {
                    const hasLabel = attributes.includes('id=') || attributes.includes(':id=') || attributes.includes('label=') || attributes.includes(':label=') || attributes.includes('aria-label') || attributes.includes(':aria-label');
                    if (!hasLabel && !attributes.includes('type="hidden"') && !attributes.includes("type='hidden'")) {
                        results.push({
                            file: file,
                            line: line,
                            type: 'input_missing_label',
                            content: fullTag.replace(/\s+/g, ' ').substring(0, 100)
                        });
                    }
                }

                // 4. Tabindex > 0
                if (attributes.includes('tabindex=')) {
                    const tabMatch = attributes.match(/tabindex=["'](\d+)["']/);
                    if (tabMatch && parseInt(tabMatch[1]) > 0) {
                        results.push({
                            file: file,
                            line: line,
                            type: 'tabindex_gt_zero',
                            content: fullTag.replace(/\s+/g, ' ').substring(0, 100)
                        });
                    }
                }

                // 5. Cliques em elementos não interativos
                if (attributes.includes('@click=') || attributes.includes('v-on:click=')) {
                    if (!isInteractive(tagName) && !attributes.includes('tabindex=') && !attributes.includes('role=')) {
                         results.push({
                            file: file,
                            line: line,
                            type: 'click_on_non_interactive',
                            content: fullTag.replace(/\s+/g, ' ').substring(0, 100)
                        });
                    }
                }
            }
        } catch (e) {
            console.error(e);
        }
    });
    return results;
}

function generateReport(data) {
    let md = "# Frontend Accessibility Audit (Static Analysis)\n\n";

    if (data.length === 0) {
        md += "No accessibility issues found by static analysis.\n";
    } else {
        md += "Found " + data.length + " potential issues.\n\n";
        md += "| File | Line | Issue Type | Code |\n";
        md += "|------|------|------------|------|\n";

        data.forEach(item => {
            let content = item.content;
            if (content.length > 100) content = content.substring(0, 100) + '...';
            content = content.replace(/\|/g, '\\|'); 
            md += "| " + path.basename(item.file) + " | " + item.line + " | " + item.type + " | `" + content + "` |\n";
        });
    }

    fs.writeFileSync(REPORT_FILE, md, 'utf-8');
}

console.log("Running Accessibility Audit...");
const results = scanA11y();
generateReport(results);
console.log(`Report generated: ${REPORT_FILE}`);
console.log(`Found ${results.length} potential issues.`);
