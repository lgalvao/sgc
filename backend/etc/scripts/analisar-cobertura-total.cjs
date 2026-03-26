#!/usr/bin/env node
const fs = require('node:fs');
const path = require('node:path');
const { execSync } = require('node:child_process');
const xml2js = require('xml2js');
const BASE_DIR = path.join(__dirname, '../..');
const REPORT_PATH = path.join(BASE_DIR, 'build/reports/jacoco/test/jacocoTestReport.xml');
const PLAN_PATH = path.join(BASE_DIR, '../plano-cobertura-backend.md');
const isWindows = process.platform === 'win32';
const GRADLE_CMD = isWindows ? 'gradlew.bat :backend:jacocoTestReport' : './gradlew :backend:jacocoTestReport';
async function main() {
    const args = process.argv.slice(2);
    const skipRun = args.includes('--skip-run');
    console.log('\n📊 [Analisador] Iniciando diagnóstico de cobertura...');
    if (!skipRun) {
        console.log('🚀 [1/3] Atualizando relatório JaCoCo via Gradle...');
        try { execSync(GRADLE_CMD, { cwd: BASE_DIR, stdio: 'inherit' }); } 
        catch (error) { console.warn('⚠️  [Aviso] Gradle reportou erros, mas prosseguindo...'); }
    }
    if (!fs.existsSync(REPORT_PATH)) { console.error('❌ Relatório não encontrado'); process.exit(1); }
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
            const metrics = getMetrics(sf);
            if (metrics.lineCoverage < 100 || metrics.branchCoverage < 100) {
                const actionScore = (metrics.missedLines * 1) + (metrics.missedBranches * 2) + (metrics.complexity * 0.5);
                allStats.push({ className: pkgName + '.' + sf.$.name.replace('.java', ''), ...metrics, actionScore });
            }
        });
    });
    allStats.sort((a, b) => b.actionScore - a.actionScore);
    fs.writeFileSync(PLAN_PATH, generateMarkdown(allStats));
    printSummary(allStats);
}
function getMetrics(sf) {
    let linesTotal = 0, linesCovered = 0, branchesTotal = 0, branchesCovered = 0, complexity = 0;
    const missedLines = [], missedBranches = [];
    if (sf.line) {
        sf.line.forEach(line => {
            const nr = parseInt(line.$.nr), ci = parseInt(line.$.ci || 0), mb = parseInt(line.$.mb || 0), cb = parseInt(line.$.cb || 0);
            linesTotal++;
            if (ci > 0) linesCovered++; else missedLines.push(nr);
            if (mb + cb > 0) { branchesTotal += (mb + cb); branchesCovered += cb; if (mb > 0) missedBranches.push(nr + '(' + mb + '/' + (mb + cb) + ')'); }
        });
    }
    if (sf.counter) {
        const comp = sf.counter.find(c => c.$.type === 'COMPLEXITY');
        if (comp) complexity = parseInt(comp.$.covered) + parseInt(comp.$.missed);
    }
    return { linesTotal, linesCovered, missedLines: linesTotal - linesCovered, lineCoverage: linesTotal > 0 ? (linesCovered / linesTotal) * 100 : 100, branchesTotal, branchesCovered, missedBranches: branchesTotal - branchesCovered, branchCoverage: branchesTotal > 0 ? (branchesCovered / branchesTotal) * 100 : 100, complexity, missedLinesList: missedLines, missedBranchesList: missedBranches };
}
function printSummary(stats) {
    console.log('\n' + '='.repeat(80));
    console.log('🏆 TOP 10 PRIORIDADES DE COBERTURA');
    console.log('='.repeat(80));
    stats.slice(0, 10).forEach((s, i) => {
        const emoji = s.actionScore > 20 ? '🔴' : (s.actionScore > 10 ? '🟡' : '🟢');
        console.log(`${i + 1}. ${emoji} [Score: ${s.actionScore.toFixed(1)}] ${s.className}`);
        console.log(`   L: ${s.lineCoverage.toFixed(1)}% | B: ${s.branchCoverage.toFixed(1)}%`);
        console.log(`   Comando: node backend/etc/scripts/gerar-stub-teste.cjs ${s.className.split('.').pop()}`);
    });
    console.log('\n✅ Plano completo gerado em: ' + PLAN_PATH);
}
function generateMarkdown(stats) {
    let md = "# 🎯 Plano de Cobertura Backend\n\n**Gerado em:** " + (new Date().toLocaleString('pt-BR')) + "\n\n";
    md += "| Prioridade | Classe | Cobertura L | Cobertura B | Score | Ação |\n| :--- | :--- | :--- | :--- | :--- | :--- |\n";
    stats.forEach(s => {
        const p = s.actionScore > 20 ? '🔴 CRÍTICO' : (s.actionScore > 10 ? '🟡 MÉDIO' : '🟢 BAIXO');
        const name = s.className.split('.').pop();
        md += `| ${p} | \`${s.className}\` | ${s.lineCoverage.toFixed(1)}% | ${s.branchCoverage.toFixed(1)}% | ${s.actionScore.toFixed(1)} | \`node backend/etc/scripts/gerar-stub-teste.cjs ${name}\` |\n`;
    });
    return md;
}
main().catch(console.error);