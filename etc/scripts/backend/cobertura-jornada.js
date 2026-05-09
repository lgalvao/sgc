#!/usr/bin/env node
import fs from "node:fs";
import {execFileSync} from "node:child_process";
import {DIRETORIO_RAIZ, resolverNaRaiz} from "../lib/caminhos.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";

const ARQUIVO_AUDITORIA = "backend-coverage-auditoria.md";
const ARQUIVO_ANALISE_MD = "unit-test-report.md";
const ARQUIVO_ANALISE_JSON = "unit-test-report.json";
const ARQUIVO_PRIORIZACAO = "prioritized-tests.md";

function executarComando(comando, args, opcoes = {}) {
    return execFileSync(comando, args, {
        cwd: DIRETORIO_RAIZ,
        encoding: "utf-8",
        stdio: opcoes.capture ? ["inherit", "pipe", "pipe"] : "inherit"
    });
}

function executarScriptNode(relativo, args = [], opcoes = {}) {
    const script = resolverNaRaiz("etc", "scripts", relativo);
    return executarComando("node", [script, ...args], opcoes);
}

function imprimirSecao(titulo) {
    console.log(`\n📍 ${titulo}`);
    console.log("─────────────────────────────────────────");
}

function imprimirPreviewArquivo(caminhoArquivo, maxLinhas = 20) {
    if (!fs.existsSync(caminhoArquivo)) {
        return;
    }

    const conteudo = fs.readFileSync(caminhoArquivo, "utf-8");
    const preview = conteudo.split(/\r?\n/).slice(0, maxLinhas).join("\n");
    console.log(preview);
}

function imprimirAjuda() {
    exibirAjudaComando({
        comandoSgc: "backend cobertura jornada",
        scriptDireto: "backend/cobertura-jornada.js",
        descricao: "Executa a jornada consolidada de cobertura do backend com auditoria, analise e priorizacao.",
        exemplos: [
            "node etc/scripts/sgc.js backend cobertura jornada"
        ]
    });
}

function main() {
    if (process.argv.includes("--help") || process.argv.includes("-h")) {
        imprimirAjuda();
        process.exit(0);
    }

    console.log("🎯 === JORNADA CONSOLIDADA DE COBERTURA (BACKEND) ===");

    imprimirSecao("Etapa 1: Executar testes e gerar JaCoCo");
    executarComando(process.platform === "win32" ? "gradlew.bat" : "./gradlew", [":backend:test", ":backend:jacocoTestReport"]);

    imprimirSecao("Etapa 2: Auditar cobertura e hotspots");
    executarScriptNode("backend/cobertura-auditoria.js", [`--output=${ARQUIVO_AUDITORIA}`]);

    const jsonAuditoria = executarScriptNode("backend/cobertura-auditoria.js", ["--json"], {capture: true});
    const auditoria = JSON.parse(jsonAuditoria);
    console.log(
        `Cobertura de linhas: ${auditoria.totais.linhas.percentual}% | Cobertura de branches: ${auditoria.totais.branches.percentual}%`
    );

    imprimirSecao("Etapa 3: Analisar classes sem testes");
    executarScriptNode("backend/testes-analisar.js", [
        "--dir",
        "backend",
        "--output",
        ARQUIVO_ANALISE_MD,
        "--output-json",
        ARQUIVO_ANALISE_JSON
    ]);

    imprimirSecao("Etapa 4: Priorizar backlog acionavel");
    executarScriptNode("backend/testes-priorizar.js", [
        "--input",
        ARQUIVO_ANALISE_JSON,
        "--output",
        ARQUIVO_PRIORIZACAO
    ]);
    imprimirPreviewArquivo(resolverNaRaiz(ARQUIVO_PRIORIZACAO));

    console.log("\n✅ Jornada concluida.");
    console.log("Arquivos gerados:");
    console.log(`  📄 ${ARQUIVO_AUDITORIA}`);
    console.log(`  📄 ${ARQUIVO_ANALISE_MD}`);
    console.log(`  📄 ${ARQUIVO_ANALISE_JSON}`);
    console.log(`  📄 ${ARQUIVO_PRIORIZACAO}`);
    console.log("\nPróximos passos:");
    console.log(`  1. Revisar ${ARQUIVO_AUDITORIA}.`);
    console.log(`  2. Atacar itens P1 de ${ARQUIVO_PRIORIZACAO}.`);
    console.log("  3. Gerar stubs com `node etc/scripts/sgc.js backend testes gerar-stub <Classe>` quando fizer sentido.");
}

try {
    main();
} catch (error) {
    console.error(`Erro na jornada de cobertura: ${error.message}`);
    process.exit(1);
}
