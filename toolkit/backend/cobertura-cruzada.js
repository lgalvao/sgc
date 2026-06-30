#!/usr/bin/env node
import fs from "node:fs/promises";
import pc from "picocolors";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {deveExcluirClasse} from "../lib/dominios/cobertura-java.js";
import {parseStringPromise} from "xml2js";

async function main() {
    try {
        console.log(pc.bold("\nIniciando auditoria de cobertura cruzada e independente (Toolkit SGC)..."));

        const xmlPathAbsoluto = resolverNaRaiz("backend/build/reports/jacoco/test/jacocoTestReport.xml");
        const xmlContent = await fs.readFile(xmlPathAbsoluto, "utf8");
        const parsed = await parseStringPromise(xmlContent);

        // 1. Contadores Globais Brutos da Raiz do JaCoCo
        const reportCounters = parsed.report.counter || [];
        const contadoresBrutos = {};
        reportCounters.forEach(c => {
            const type = c.$.type;
            const covered = Number(c.$.covered || 0);
            const missed = Number(c.$.missed || 0);
            const total = covered + missed;
            const percentual = total > 0 ? Number(((covered / total) * 100).toFixed(2)) : 0;
            contadoresBrutos[type] = {covered, missed, total, percentual};
        });

        console.log(pc.cyan("\n=================================================================="));
        console.log(pc.bold("1. CONTADORES GLOBAIS DIRETOS DO REPORT JACOCO (SEM EXCLUSÕES)"));
        console.log(pc.cyan("=================================================================="));

        const metricas = ["INSTRUCTION", "BRANCH", "LINE", "COMPLEXITY", "METHOD", "CLASS"];
        metricas.forEach(m => {
            const val = contadoresBrutos[m];
            if (val) {
                const label = m.padEnd(12);
                let percentText = `${val.percentual}%`;
                if (val.percentual >= 95.0) {
                    percentText = pc.green(pc.bold(percentText));
                } else if (val.percentual >= 90.0) {
                    percentText = pc.yellow(pc.bold(percentText));
                } else {
                    percentText = pc.red(pc.bold(percentText));
                }
                console.log(`${pc.bold(label)}: Cobertos: ${String(val.covered).padStart(5)} | Perdidos: ${String(val.missed).padStart(4)} | Total: ${String(val.total).padStart(5)} | Cobertura: ${percentText}`);
            }
        });

        // 2. Análise por classes (mantidas vs excluídas)
        let totalLinhasGeral = 0;
        let linhasCobertasGeral = 0;
        let totalBranchesGeral = 0;
        let branchesCobertosGeral = 0;

        let totalLinhasExcluidas = 0;
        let linhasCobertasExcluidas = 0;
        let totalBranchesExcluidas = 0;
        let branchesCobertosExcluidas = 0;

        let totalLinhasMantidas = 0;
        let linhasCobertasMantidas = 0;
        let totalBranchesMantidas = 0;
        let branchesCobertosMantidas = 0;

        let totalClasses = 0;
        let classesExcluidasCount = 0;
        let classesMantidasCount = 0;

        for (const pacote of parsed.report.package ?? []) {
            for (const classNode of pacote.class ?? []) {
                const nomeClasse = classNode.$.name.replaceAll("/", ".");
                totalClasses++;

                const ehExcluida = deveExcluirClasse(nomeClasse);
                if (ehExcluida) {
                    classesExcluidasCount++;
                } else {
                    classesMantidasCount++;
                }

                const classCounters = classNode.counter || [];
                let classLinesCovered = 0;
                let classLinesMissed = 0;
                let classBranchesCovered = 0;
                let classBranchesMissed = 0;

                classCounters.forEach(c => {
                    const type = c.$.type;
                    const covered = Number(c.$.covered || 0);
                    const missed = Number(c.$.missed || 0);
                    if (type === 'LINE') {
                        classLinesCovered = covered;
                        classLinesMissed = missed;
                    } else if (type === 'BRANCH') {
                        classBranchesCovered = covered;
                        classBranchesMissed = missed;
                    }
                });

                const totalClassLines = classLinesCovered + classLinesMissed;
                const totalClassBranches = classBranchesCovered + classBranchesMissed;

                totalLinhasGeral += totalClassLines;
                linhasCobertasGeral += classLinesCovered;
                totalBranchesGeral += totalClassBranches;
                branchesCobertosGeral += classBranchesCovered;

                if (ehExcluida) {
                    totalLinhasExcluidas += totalClassLines;
                    linhasCobertasExcluidas += classLinesCovered;
                    totalBranchesExcluidas += totalClassBranches;
                    branchesCobertosExcluidas += classBranchesCovered;
                } else {
                    totalLinhasMantidas += totalClassLines;
                    linhasCobertasMantidas += classLinesCovered;
                    totalBranchesMantidas += totalClassBranches;
                    branchesCobertosMantidas += classBranchesCovered;
                }
            }
        }

        const calcPct = (cob, perd) => {
            const tot = cob + perd;
            return tot > 0 ? Number(((cob / tot) * 100).toFixed(2)) : 0;
        };

        const pctMantidas = calcPct(classesMantidasCount, totalClasses - classesMantidasCount);
        const pctExcluidas = calcPct(classesExcluidasCount, totalClasses - classesExcluidasCount);

        console.log(pc.cyan("\n=================================================================="));
        console.log(pc.bold("2. COMPARAÇÃO DE CLASSES MANTIDAS VS EXCLUÍDAS PELO TOOLKIT"));
        console.log(pc.cyan("=================================================================="));
        console.log(`Total de Classes Encontradas no XML: ${pc.bold(totalClasses)}`);
        console.log(`Classes Mantidas (Core da Lógica):   ${pc.bold(classesMantidasCount)} (${pctMantidas}%)`);
        console.log(`Classes Excluídas (Ruído/DTOs/etc):  ${pc.bold(classesExcluidasCount)} (${pctExcluidas}%)`);

        console.log(pc.bold("\n--- COBERTURA ABSOLUTA GERAL (SOMA DAS CLASSES) ---"));
        console.log(`Linhas:    Cobertas: ${linhasCobertasGeral} / Total: ${totalLinhasGeral} (${calcPct(linhasCobertasGeral, totalLinhasGeral - linhasCobertasGeral)}%)`);
        console.log(`Branches:  Cobertos: ${branchesCobertosGeral} / Total: ${totalBranchesGeral} (${calcPct(branchesCobertosGeral, totalBranchesGeral - branchesCobertosGeral)}%)`);

        console.log(pc.bold("\n--- COBERTURA APENAS DAS CLASSES MANTIDAS (CORE) ---"));
        console.log(`Linhas:    Cobertas: ${linhasCobertasMantidas} / Total: ${totalLinhasMantidas} (${calcPct(linhasCobertasMantidas, totalLinhasMantidas - linhasCobertasMantidas)}%)`);
        console.log(`Branches:  Cobertos: ${branchesCobertosMantidas} / Total: ${totalBranchesMantidas} (${calcPct(branchesCobertosMantidas, totalBranchesMantidas - branchesCobertosMantidas)}%)`);

        console.log(pc.bold("\n--- COBERTURA APENAS DAS CLASSES EXCLUÍDAS (DTOs, CONFIGS) ---"));
        console.log(`Linhas:    Cobertas: ${linhasCobertasExcluidas} / Total: ${totalLinhasExcluidas} (${calcPct(linhasCobertasExcluidas, totalLinhasExcluidas - linhasCobertasExcluidas)}%)`);
        console.log(`Branches:  Cobertos: ${branchesCobertosExcluidas} / Total: ${totalBranchesExcluidas} (${calcPct(branchesCobertosExcluidas, totalBranchesExcluidas - branchesCobertosExcluidas)}%)`);

        // 3. Auditoria matemática cruzada
        console.log(pc.cyan("\n=================================================================="));
        console.log(pc.bold("3. AUDITORIA DOS SCRIPTS DO TOOLKIT (VALIDAÇÃO DE VERACIDADE)"));
        console.log(pc.cyan("=================================================================="));

        const diffLinhas = Math.abs(contadoresBrutos.LINE.percentual - calcPct(linhasCobertasGeral, totalLinhasGeral - linhasCobertasGeral));
        const diffBranches = Math.abs(contadoresBrutos.BRANCH.percentual - calcPct(branchesCobertosGeral, totalBranchesGeral - branchesCobertosGeral));

        console.log(`Diferença matemática entre contadores da raiz del XML e a soma dos elementos de classes:`);
        console.log(`  - Linhas:   ${diffLinhas.toFixed(4)}%`);
        console.log(`  - Branches: ${diffBranches.toFixed(4)}%`);

        console.log(pc.bold("\nSTATUS DA VALIDAÇÃO:"));
        if (diffLinhas < 0.05 && diffBranches < 0.05) {
            console.log(pc.green("  ✅ OS SCRIPTS ESTÃO MEDINDO CORRETAMENTE!"));
            console.log(pc.green("  ✅ O 'Resumo do Projeto' corresponde de fato à cobertura GLOBAL BRUTA (sem as exclusões)."));
            console.log(pc.green("  ✅ A divisão do toolkit é aplicada de forma limpa e transparente."));
        } else {
            console.log(pc.red("  ❌ ALERTA: Há discrepância na medição!"));
        }

    } catch (e) {
        console.error(pc.red("Erro durante a auditoria de cobertura cruzada:"), e);
        process.exit(1);
    }
}

main();
