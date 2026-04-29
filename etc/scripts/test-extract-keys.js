import fs from "node:fs";

const content = fs.readFileSync("frontend/src/constants/textos.ts", "utf-8");

// Extrator simplificado de chaves baseado na indentação e estrutura
function extractKeys(text) {
    const keys = [];
    const lines = text.split("\n");
    let currentCategory = null;

    for (const line of lines) {
        // Detectar categoria: "  categoria: {"
        const categoryMatch = line.match(/^\s{2}([a-z][a-zA-Z0-9]+):\s+\{/);
        if (categoryMatch) {
            currentCategory = categoryMatch[1];
            continue;
        }

        // Detectar chave: "    CHAVE: " ou "    CHAVE(arg) => "
        if (currentCategory) {
            const keyMatch = line.match(/^\s{4}([A-Z0-9_]+)[\s:(]/);
            if (keyMatch) {
                keys.push(`${currentCategory}.${keyMatch[1]}`);
            }

            // Fechar categoria
            if (line.match(/^\s{2}\},/)) {
                currentCategory = null;
            }
        }
    }
    return keys;
}

const keys = extractKeys(content);
console.log(JSON.stringify(keys, null, 2));
