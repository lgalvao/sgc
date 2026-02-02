const fs = require('fs');
const path = require('path');
const { createInterface } = require('readline');

// Configuração
const CSV_PATH = path.join(__dirname, '../../backend/build/reports/jacoco/test/jacocoTestReport.csv');
const OUTPUT_PATH = path.join(__dirname, '../../complexity-ranking.md');

// Estrutura de dados para Métricas
class ClassMetrics {
    constructor(data) {
        this.package = data.PACKAGE;
        this.name = data.CLASS;
        this.instructionsCovered = parseInt(data.INSTRUCTION_COVERED || 0, 10);
        this.instructionsMissed = parseInt(data.INSTRUCTION_MISSED || 0, 10);
        this.branchesCovered = parseInt(data.BRANCH_COVERED || 0, 10);
        this.branchesMissed = parseInt(data.BRANCH_MISSED || 0, 10);
        this.linesCovered = parseInt(data.LINE_COVERED || 0, 10);
        this.linesMissed = parseInt(data.LINE_MISSED || 0, 10);
        this.complexityCovered = parseInt(data.COMPLEXITY_COVERED || 0, 10);
        this.complexityMissed = parseInt(data.COMPLEXITY_MISSED || 0, 10);
        this.methodsCovered = parseInt(data.METHOD_COVERED || 0, 10);
        this.methodsMissed = parseInt(data.METHOD_MISSED || 0, 10);
    }

    get totalBranches() { return this.branchesCovered + this.branchesMissed; }
    get totalLines() { return this.linesCovered + this.linesMissed; }
    get totalComplexity() { return this.complexityCovered + this.complexityMissed; }
    get totalMethods() { return this.methodsCovered + this.methodsMissed; }
    
    get avgComplexityPerMethod() {
        return this.totalMethods === 0 ? 0.0 : this.totalComplexity / this.totalMethods;
    }

    get branchCoveragePercentage() {
        if (this.totalBranches === 0) return 100.0;
        return (this.branchesCovered / this.totalBranches) * 100;
    }

    get complexityScore() {
        const normComplexity = this.totalComplexity;
        const normBranches = this.totalBranches;
        const normLines = this.totalLines / 10;
        const normAvgComplexity = this.avgComplexityPerMethod * 5;

        return (
            normComplexity * 0.40 +
            normBranches * 0.30 +
            normLines * 0.20 +
            normAvgComplexity * 0.10
        );
    }

    get fullName() { return `${this.package}.${this.name}`; }
}

function categorizeClass(className) {
    if (className.includes('Controller')) return 'Controller';
    if (className.includes('Service') || className.includes('Facade')) return 'Service/Facade';
    if (className.includes('Repo') || className.includes('Repository')) return 'Repository';
    if (className.includes('Mapper')) return 'Mapper';
    if (className.includes('Listener')) return 'Listener';
    if (className.includes('Request') || className.includes('Response') || className.includes('Dto')) return 'DTO';
    if (/^[A-Z]/.test(className) && !['Service', 'Controller', 'Repo'].some(x => className.includes(x))) return 'Model/Entity';
    return 'Other';
}

async function parseJacocoCsv(filePath) {
    const fileStream = fs.createReadStream(filePath);
    const rl = createInterface({ input: fileStream, crlfDelay: Infinity });

    const metrics = [];
    let headers = null;

    for await (const line of rl) {
        if (!line.trim()) continue;
        const cols = line.split(',');
        
        if (!headers) {
            headers = cols;
            continue;
        }

        const row = {};
        headers.forEach((h, i) => row[h] = cols[i]);

        // Ignorar se não tiver CLASSE definida (resumos de pacote)
        if (!row.CLASS) continue;

        metrics.push(new ClassMetrics(row));
    }

    return metrics;
}

function generateMarkdownReport(metrics, outputPath) {
    // Ordenar por score
    const sortedMetrics = [...metrics].sort((a, b) => b.complexityScore - a.complexityScore);

    // Estatísticas
    const totalClasses = metrics.length;
    const totalComplexity = metrics.reduce((sum, m) => sum + m.totalComplexity, 0);
    const totalBranches = metrics.reduce((sum, m) => sum + m.totalBranches, 0);
    const totalLines = metrics.reduce((sum, m) => sum + m.totalLines, 0);
    const avgComplexity = totalClasses > 0 ? totalComplexity / totalClasses : 0;

    let md = `# Ranking de Complexidade do Backend - SGC\n\n`;
    md += `Este relatório apresenta uma análise detalhada da complexidade do código backend, baseado em métricas do Jacoco e análise de complexidade ciclomática.\n\n`;
    
    md += `## Resumo Executivo\n\n`;
    md += `- **Total de Classes Analisadas:** ${totalClasses}\n`;
    md += `- **Complexidade Ciclomática Total:** ${totalComplexity}\n`;
    md += `- **Total de Branches:** ${totalBranches}\n`;
    md += `- **Total de Linhas de Código:** ${totalLines}\n`;
    md += `- **Complexidade Média por Classe:** ${avgComplexity.toFixed(2)}\n\n`;

    md += `## Metodologia\n\n`;
    md += "O **Complexity Score** é calculado através da fórmula:\n\n";
    md += "```\nScore = (Complexidade Ciclomática × 0.40) + \n        (Total de Branches × 0.30) + \n        (Linhas de Código ÷ 10 × 0.20) + \n        (Complexidade Média por Método × 5 × 0.10)\n```\n\n";

    // Top 50
    md += `## Top 50 Classes Mais Complexas\n\n`;
    md += `| Rank | Classe | Pacote | Score | Complexity | Branches | Linhas | Métodos | Avg/Método | Categoria |\n`;
    md += `|------|--------|--------|-------|------------|----------|---------|---------|------------|------------|\n`;

    sortedMetrics.slice(0, 50).forEach((m, i) => {
        md += `| ${i + 1} | \`${m.name}\` | \`${m.package}\` | ${m.complexityScore.toFixed(1)} | ${m.totalComplexity} | ${m.totalBranches} | ${m.totalLines} | ${m.totalMethods} | ${m.avgComplexityPerMethod.toFixed(1)} | ${categorizeClass(m.name)} |\n`;
    });

    // Análise por Categoria
    md += `\n## Análise por Categoria\n\n`;
    const categories = {};
    metrics.forEach(m => {
        const cat = categorizeClass(m.name);
        if (!categories[cat]) categories[cat] = [];
        categories[cat].push(m);
    });

    Object.keys(categories).sort().forEach(cat => {
        const classes = categories[cat];
        md += `### ${cat}\n\n`;
        md += `Total de classes: ${classes.length}\n\n`;
        
        const top10 = classes.sort((a, b) => b.complexityScore - a.complexityScore).slice(0, 10);
        
        md += `| Classe | Score | Complexity | Branches | Linhas | Cobertura Branches |\n`;
        md += `|--------|-------|------------|----------|--------|--------------------|\n`;
        
        top10.forEach(m => {
            md += `| \`${m.name}\` | ${m.complexityScore.toFixed(1)} | ${m.totalComplexity} | ${m.totalBranches} | ${m.totalLines} | ${m.branchCoveragePercentage.toFixed(1)}% |\n`;
        });
        md += `\n`;
    });

    // Maior complexidade por método
    md += `## Top 20 Classes com Maior Complexidade por Método\n\n`;
    const byAvgComplexity = [...metrics].filter(m => m.totalMethods > 0).sort((a, b) => b.avgComplexityPerMethod - a.avgComplexityPerMethod).slice(0, 20);

    md += `| Rank | Classe | Avg Complexity/Método | Métodos | Total Complexity | Categoria |\n`;
    md += `|------|--------|-----------------------|---------|------------------|------------|\n`;
    byAvgComplexity.forEach((m, i) => {
        md += `| ${i + 1} | \`${m.name}\` | ${m.avgComplexityPerMethod.toFixed(2)} | ${m.totalMethods} | ${m.totalComplexity} | ${categorizeClass(m.name)} |\n`;
    });

    // Mais branches
    md += `\n## Top 20 Classes com Mais Branches\n\n`;
    const byBranches = [...metrics].sort((a, b) => b.totalBranches - a.totalBranches).slice(0, 20);
    
    md += `| Rank | Classe | Branches | Covered | Missed | Coverage % | Categoria |\n`;
    md += `|------|--------|----------|---------|--------|------------|------------|\n`;
    byBranches.forEach((m, i) => {
        md += `| ${i + 1} | \`${m.name}\` | ${m.totalBranches} | ${m.branchesCovered} | ${m.branchesMissed} | ${m.branchCoveragePercentage.toFixed(1)}% | ${categorizeClass(m.name)} |\n`;
    });

    fs.writeFileSync(outputPath, md, 'utf-8');
}

async function main() {
    if (!fs.existsSync(CSV_PATH)) {
        console.error(`Erro: Arquivo ${CSV_PATH} não encontrado.`);
        console.error("Execute 'gradle test jacocoTestReport' primeiro.");
        process.exit(1);
    }

    console.log(`Lendo dados de: ${CSV_PATH}`);
    const metrics = await parseJacocoCsv(CSV_PATH);

    console.log(`Analisadas ${metrics.length} classes`);
    console.log(`Gerando relatório em: ${OUTPUT_PATH}`);

    generateMarkdownReport(metrics, OUTPUT_PATH);

    console.log("✓ Relatório gerado com sucesso!");
    console.log("\nTop 5 classes mais complexas:");
    
    const sortedMetrics = [...metrics].sort((a, b) => b.complexityScore - a.complexityScore);
    sortedMetrics.slice(0, 5).forEach((m, i) => {
        console.log(`  ${i + 1}. ${m.fullName} (Score: ${m.complexityScore.toFixed(1)})`);
    });
}

main().catch(err => console.error(err));
