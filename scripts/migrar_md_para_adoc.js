const fs = require('fs');
const path = require('path');

const REQS_DIR = path.join(__dirname, '../reqs');

function removeCommonLeadingIndentation(text) {
    const lines = text.split('\n');
    let minIndentation = Infinity;
    
    // Find the minimum indentation of non-empty lines
    for (const line of lines) {
        if (line.trim().length > 0) { // Only consider non-empty lines
            const leadingSpaces = line.match(/^\s*/)[0].length;
            minIndentation = Math.min(minIndentation, leadingSpaces);
        }
    }

    if (minIndentation === Infinity || minIndentation === 0) {
        return text; // No common indentation or all lines are empty/unindented
    }

    // Remove the common indentation from each line
    const unindentedLines = lines.map(line => {
        if (line.length >= minIndentation) {
            return line.substring(minIndentation);
        }
        return line; // Handle lines shorter than minIndentation, or empty lines
    });

    return unindentedLines.join('\n');
}

function convertMdToAdoc(md) {
    let adoc = md;

    // 1. Protect Code Blocks
    const codeBlocks = [];
    // Use a placeholder that won't conflict with Markdown syntax.
    // %%%% is generally safe from MD parsers.
    adoc = adoc.replace(/```(\w*)([\s\S]*?)```/g, (match, lang, content) => {
        const placeholder = `%%%%CODE_BLOCK_${codeBlocks.length}%%%%`;
        codeBlocks.push({ lang, content });
        return placeholder;
    });

    // 2. Headers
    adoc = adoc.replace(/^# (.*$)/gm, '= $1');
    adoc = adoc.replace(/^## (.*$)/gm, '== $1');
    adoc = adoc.replace(/^### (.*$)/gm, '=== $1');
    adoc = adoc.replace(/^#### (.*$)/gm, '==== $1');
    adoc = adoc.replace(/^##### (.*$)/gm, '===== $1');

    // 3. Lists (Line-by-line processing)
    const lines = adoc.split('\n');
    const newLines = lines.map(line => {
        // Unordered lists
        const ulMatch = line.match(/^(\s*)([-*])\s+(.*)/);
        if (ulMatch) {
            const indentStr = ulMatch[1];
            const indentLen = indentStr.replace(/\t/g, '  ').length;
            const level = Math.floor(indentLen / 2) + 1;
            return '*'.repeat(level) + ' ' + ulMatch[3];
        }

        // Ordered lists (Handles 1., 9.1., 9.1.1., etc.)
        const olMatch = line.match(/^(\s*)(\d+(?:\.\d+)*)([\.:]?)\s+(.*)/);
        if (olMatch) {
            const numbering = olMatch[2]; // e.g. "9.1.1"
            const content = olMatch[4];   // e.g. "Text"
            
            // Determine level by counting parts separated by dot
            const level = numbering.split('.').length;
            
            // Output: ... Text
            return '.'.repeat(level) + ' ' + content;
        }

        // For non-list lines, remove indentation to avoid AsciiDoc Literal Blocks
        // (unless it's an empty line, which we can just keep empty)
        if (line.trim().length === 0) {
            return '';
        }
        return line.trimStart();
    });
    adoc = newLines.join('\n');

    // 7.5. Clean up empty lines between list items and code block placeholders
    // This ensures the list continuation '+' can be correctly inserted later.
    // Matches: (list item line followed by newline) + (empty line) + (code block placeholder)
    // Replaces with: (list item line followed by newline) + (code block placeholder), effectively removing the empty line.
    adoc = adoc.replace(/(\n(\*+|\.+)\s[^\n]*)\n+(%%%%CODE_BLOCK_\d+%%%%)/g, '$1\n$3');

    // 4. Bold and Italic
    adoc = adoc.replace(/\*\*(.*?)\*\*/g, '*$1*');
    adoc = adoc.replace(/_(.*?)_/g, '_$1_');

    // 5. Images
    adoc = adoc.replace(/!\[(.*?)\]\((.*?)\)/g, 'image::$2[$1]');

    // 6. Links
    adoc = adoc.replace(/(?<!!)!!\[([^\]]+)\]\(([^)]+)\)/g, 'link:$2[$1]');

    // 7. Miscellaneous
    // Horizontal rules: --- or *** -> '''
    // Apply this BEFORE restoring code blocks to avoid changing content inside them.
    adoc = adoc.replace(/^[-*]{3,}$/gm, "'''");

    // 8. Restore Code Blocks with List Continuation (FINAL STEP)
    let processedLines = [];
    const linesToProcess = adoc.split('\n');

    for (let i = 0; i < linesToProcess.length; i++) {
        const line = linesToProcess[i];
        const placeholderMatch = line.match(/%%%%CODE_BLOCK_(\d+)%%%%/);

        if (placeholderMatch) {
            const blockIndex = parseInt(placeholderMatch[1], 10);
            const block = codeBlocks[blockIndex];

            // Construct the block replacement with unindented content
            let cleanContent = block.content.replace(/\s+$/, '');
            cleanContent = removeCommonLeadingIndentation(cleanContent);
            
            const langAttr = block.lang ? `[source,${block.lang}]\n` : '';
            const blockReplacement = `${langAttr}----\n${cleanContent}\n----`;

            // Check if the previous line was a list item
            if (processedLines.length > 0) {
                let lastMeaningfulLine = null;
                for (let j = processedLines.length - 1; j >= 0; j--) {
                    if (processedLines[j].trim().length > 0) {
                        lastMeaningfulLine = processedLines[j];
                        break;
                    }
                }

                if (lastMeaningfulLine) {
                    const isListItem = lastMeaningfulLine.match(/^(\*+|\.+)\s/); 

                    if (isListItem) {
                        processedLines.push('+'); // Add list continuation
                    }
                }
            }
            processedLines.push(blockReplacement); // Add the actual block
        } else {
            processedLines.push(line); // Add non-placeholder lines as they are
        }
    }
    adoc = processedLines.join('\n');

    return adoc;
}

async function main() {
    console.log(`Checking directory: ${REQS_DIR}`);
    if (!fs.existsSync(REQS_DIR)) {
        console.error('Directory not found!');
        return;
    }

    const files = fs.readdirSync(REQS_DIR);
    const mdFiles = files.filter(f => f.endsWith('.md'));

    console.log(`Found ${mdFiles.length} Markdown files.`);

    for (const file of mdFiles) {
        const inputPath = path.join(REQS_DIR, file);
        const outputPath = path.join(REQS_DIR, file.replace('.md', '.adoc'));

        const content = fs.readFileSync(inputPath, 'utf8');
        const converted = convertMdToAdoc(content);

        fs.writeFileSync(outputPath, converted, 'utf8');
        console.log(`Converted: ${file} -> ${path.basename(outputPath)}`);
    }
}

main();
