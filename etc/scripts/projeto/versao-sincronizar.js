import fs from "node:fs";
import path from "node:path";

const novaVersao = process.argv[2];
if (!novaVersao) {
    console.error("Uso: node etc/scripts/projeto/versao-sincronizar.js <versao>");
    process.exit(1);
}

const raiz = process.cwd();

// 1. Sincronizar gradle.properties
const caminhoGradle = path.join(raiz, "gradle.properties");
if (fs.existsSync(caminhoGradle)) {
    let conteudo = fs.readFileSync(caminhoGradle, "utf-8");
    conteudo = conteudo.replace(/^version=.*$/m, `version=${novaVersao}`);
    fs.writeFileSync(caminhoGradle, conteudo, "utf-8");
    console.log(`[v] gradle.properties atualizado para ${novaVersao}`);
}

// 2. Sincronizar frontend/package.json (via fs para evitar dependência de path de executável)
const caminhoFront = path.join(raiz, "frontend", "package.json");
if (fs.existsSync(caminhoFront)) {
    const pkg = JSON.parse(fs.readFileSync(caminhoFront, "utf-8"));
    pkg.version = novaVersao;
    fs.writeFileSync(caminhoFront, JSON.stringify(pkg, null, 2) + "\n", "utf-8");
    console.log(`[v] frontend/package.json atualizado para ${novaVersao}`);
}
