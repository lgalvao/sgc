#!/usr/bin/env node

/**
 * 🎵 Maestro de Cobertura - O Painel de Controle Definitivo para Qualidade
 * 
 * Este script automatiza o ciclo completo de:
 * 1. Geração de relatório (Gradle)
 * 2. Análise de lacunas (XML JaCoCo)
 * 3. Priorização por Score de Ação (Gaps + Complexidade)
 * 4. Planejamento de tarefas (Markdown)
 */

const fs = require('node:fs');
const path = require('node:path');
const { execSync } = require('node:child_process');
const xml2js = require('xml2js');

// Configuração de Caminhos
const BASE_DIR = path.join(__dirname, '../..');
const REPORT_PATH = path.join(BASE_DIR, 'build/reports/jacoco/test/jacocoTestReport.xml');
const PLAN_PATH = path.join(BASE_DIR, '../plano-maestro-cobertura.md');
const isWindows = process.platform === 'win32';
const GRADLE_CMD = isWindows ? 'gradlew.bat :backend:jacocoTestReport' : './gradlew :backend:jacocoTestReport';

async function main() {
    const args = process.argv.slice(2);
    const skipRun = args.includes('--skip-run');

    console.log('\n🎼 [Maestro] Iniciando sinfonia de análise...');

    if (!skipRun) {
        console.log('🚀 [1/3] Atualizando relatório JaCoCo via Gradle...');
        try {
            execSync(GRADLE_CMD, { cwd: BASE_DIR, stdio: 'inherit' });
        } catch (error) {
            console.warn('⚠️  [Aviso] Gradle reportou erros (testes falhando?), mas prosseguindo com a análise do que foi gerado.');
        }
    } else {
        console.log('⏭️  [1/3] Pulando atualização do relatório (--skip-run)...');
    }

    if (!fs.existsSync(REPORT_PATH)) {
        console.error(`❌ [Erro] Relatório não encontrado em: ${REPORT_PATH}`);
        process.exit(1);
    }

    console.log('📊 [2/3] Analisando XML e calculando Scores de Ação...');
    const data = fs.readFileSync(REPORT_PATH);
    const parser = new xml2js.Parser();
    const result = await parser.parseStringPromise(data);
    
    const report = result.report;
    const packages = report.package || [];
    const allStats = [];

    packages.forEach(pkg => {
        const pkgName = pkg.$.name.replaceAll('/', '.');
        const sourceFiles = pkg.sourcefile || [];

        sourceFiles.forEach(sf => {
            const fileName = sf.$.name;
            const className = `${pkgName}.${fileName.replace('.java', '')}`;
            
            const metrics = getMetrics(sf);
            if (metrics.lineCoverage < 100 || metrics.branchCoverage < 100) {
                // Cálculo do Score de Ação:
                // Peso maior para branches (lógica de decisão) e complexidade
                const actionScore = (metrics.missedLines * 1) + 
                                    (metrics.missedBranches * 2) + 
                                    (metrics.complexity * 0.5);
                
                allStats.push({
                    className,
                    fileName,
                    ...metrics,
                    actionScore
                });
            }
        });
    });

    // Ordenar por Score de Ação (maior primeiro)
    allStats.sort((a, b) => b.actionScore - a.actionScore);

    console.log('📝 [3/3] Gerando Plano Maestro de Cobertura...');
    generateMarkdown(allStats);

    printSummary(allStats);
}

function getMetrics(sf) {
    let linesTotal = 0;
    let linesCovered = 0;
    let branchesTotal = 0;
    let branchesCovered = 0;
    let complexity = 0;
    const missedLines = [];
    const missedBranches = [];

    if (sf.line) {
        sf.line.forEach(line => {
            const nr = parseInt(line.$.nr);
            const ci = parseInt(line.$.ci || 0);
            const mb = parseInt(line.$.mb || 0);
            const cb = parseInt(line.$.cb || 0);

            linesTotal++;
            if (ci > 0) {
                linesCovered++;
            } else {
                missedLines.push(nr);
            }

            const lineBranches = mb + cb;
            if (lineBranches > 0) {
                branchesTotal += lineBranches;
                branchesCovered += cb;
                if (mb > 0) missedBranches.push(`${nr}(${mb}/${lineBranches})`);
            }
        });
    }

    if (sf.counter) {
        const compCounter = sf.counter.find(c => c.$.type === 'COMPLEXITY');
        if (compCounter) {
            complexity = parseInt(compCounter.$.covered) + parseInt(compCounter.$.missed);
        }
    }

    return {
        linesTotal,
        linesCovered,
        missedLines: linesTotal - linesCovered,
        lineCoverage: linesTotal > 0 ? (linesCovered / linesTotal) * 100 : 100,
        branchesTotal,
        branchesCovered,
        missedBranches: branchesTotal - branchesCovered,
        branchCoverage: branchesTotal > 0 ? (branchesCovered / branchesTotal) * 100 : 100,
        complexity,
        missedLinesList: missedLines,
        missedBranchesList: missedBranches
    };
}

function printSummary(stats) {
    console.log('\n' + '='.repeat(80));
    console.log('🏆 TOP 10 PRIORIDADES DE COBERTURA');
    console.log('='.repeat(80));
    
    stats.slice(0, 10).forEach((s, i) => {
        const emoji = s.actionScore > 20 ? '🔴' : (s.actionScore > 10 ? '🟡' : '🟢');
        console.log(`${i + 1}. ${emoji} [Score: ${s.actionScore.toFixed(1)}] ${s.className}`);
        console.log(`   L: ${s.lineCoverage.toFixed(1)}% (${s.missedLines} perdidas) | B: ${s.branchCoverage.toFixed(1)}% (${s.missedBranches} perdidas)`);
        console.log(`   Comando: node backend/etc/scripts/gerar-testes-cobertura.cjs ${s.className.split('.').pop()}`);
        console.log('-'.repeat(80));
    });

    console.log(`\n✅ Plano completo gerado em: ${PLAN_PATH}`);
    console.log('🚀 Dica: Comece pelos arquivos 🔴 (Score > 20) pois impactam mais a qualidade geral.\n');
}

function generateMarkdown(stats) {
    let md = `# 🎯 Plano Maestro de Cobertura\n\n`;
    md += `**Gerado em:** ${new Date().toLocaleString('pt-BR')}\n\n`;
    md += `Este plano prioriza classes com maior impacto na lógica de negócio e complexidade técnica.\n\n`;
    
    md += `## 📊 Resumo de Pendências\n\n`;
    md += `- **Total de arquivos com lacunas:** ${stats.length}\n`;
    md += `- **Prioridade Crítica (Score > 20):** ${stats.filter(s => s.actionScore > 20).length}\n`;
    md += `- **Prioridade Média (Score 10-20):** ${stats.filter(s => s.actionScore <= 20 && s.actionScore > 10).length}\n\n`;

    md += `## 📋 Lista de Ações (Ordenada por Impacto)\n\n`;
    md += `| Prioridade | Classe | Cobertura L | Cobertura B | Score | Ação |\n`;
    md += `| :--- | :--- | :--- | :--- | :--- | :--- |\n`;

    stats.forEach(s => {
        const priority = s.actionScore > 20 ? '🔴 CRÍTICO' : (s.actionScore > 10 ? '🟡 MÉDIO' : '🟢 BAIXO');
        const shortName = s.className.split('.').pop();
        md += `| ${priority} | \`${s.className}\` | ${s.lineCoverage.toFixed(1)}% | ${s.branchCoverage === 100 ? 'N/A' : s.branchCoverage.toFixed(1) + '%'} | ${s.actionScore.toFixed(1)} | \`node backend/etc/scripts/gerar-testes-cobertura.cjs ${shortName}\` |\n`;
    });

    md += `\n\n---\n*Maestro de Cobertura v1.0*`;
    
    fs.writeFileSync(PLAN_PATH, md);
}

main().catch(console.error);
