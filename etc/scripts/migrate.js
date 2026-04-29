import fs from "node:fs/promises";
import path from "node:path";
import { globby } from "globby";

async function migrate() {
    const files = await globby("etc/scripts/**/*.cjs", { absolute: true });
    
    for (const file of files) {
        let content = await fs.readFile(file, "utf8");
        
        // 1. Substituir require simples
        // const fs = require('node:fs'); -> import fs from "node:fs";
        content = content.replace(/const\s+([a-zA-Z0-9_]+)\s+=\s+require\(['"](.+?)['"]\);/g, 'import $1 from "$2";');
        
        // 2. Substituir require de desestruturação
        // const { execFileSync } = require('node:child_process'); -> import { execFileSync } from "node:child_process";
        content = content.replace(/const\s+\{(.+?)\}\s+=\s+require\(['"](.+?)['"]\);/g, 'import {$1} from "$2";');

        // 3. Ajustar caminhos locais de .cjs para .js nas strings de importação
        content = content.replace(/from\s+["'](\..+?)\.cjs["']/g, 'from "$1.js"');
        
        // 4. Substituir module.exports
        content = content.replace(/module\.exports\s*=\s*\{/g, "export {");
        content = content.replace(/module\.exports\s*=\s*([a-zA-Z0-9_]+);/g, "export default $1;");
        
        // 5. Substituir variáveis globais do CJS
        content = content.replace(/__dirname/g, "import.meta.dirname");
        content = content.replace(/__filename/g, "import.meta.filename");
        
        const newFile = file.replace(/\.cjs$/, ".js");
        await fs.writeFile(newFile, content, "utf8");
        await fs.rm(file);
        console.log(`Migrated ${file} -> ${newFile}`);
    }
    
    // 6. Atualizar sgc.js
    const sgcPath = "etc/scripts/sgc.js";
    let sgcContent = await fs.readFile(sgcPath, "utf8");
    sgcContent = sgcContent.replace(/\.cjs/g, ".js");
    await fs.writeFile(sgcPath, sgcContent, "utf8");
    console.log("Updated sgc.js");
}

migrate().catch(console.error);
