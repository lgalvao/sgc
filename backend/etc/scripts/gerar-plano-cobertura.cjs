#!/usr/bin/env node

/**
 * Script para gerar um plano de ação abrangente para alcançar 100% de cobertura
 * 
 * Este script:
 * 1. Analisa o relatório JaCoCo
 * 2. Identifica arquivos com cobertura < 100%
 * 3. Prioriza por complexidade e importância
 * 4. Gera plano de ação em Markdown com tarefas específicas
 * 5. Identifica linhas e branches específicos não cobertos
 */

const fs = require('node:fs');
const path = require('node:path');
const { execSync } = require('node:child_process');
const xml2js = require('xml2js');

// Configuração
const BASE_DIR = path.join(__dirname, '../..');
const REPORT_PATH = path.join(BASE_DIR, 'build/reports/jacoco/test/jacocoTestReport.xml');
const OUTPUT_FILE = path.join(BASE_DIR, '../plano-100-cobertura.md');
const isWindows = process.platform === 'win32';
const GRADLE_CMD = isWindows ? 'gradlew.bat :backend:test :backend:jacocoTestReport' : './gradlew :backend:test :backend:jacocoTestReport';

// Categorias de arquivos (para priorização)
const CATEGORIES = {
    P1_CRITICAL: {
        patterns: [/Service\.java$/, /Facade\.java$/, /Policy\.java$/, /Validator\.java$/,
                  /Listener\.java$/, /Factory\.java$/, /Builder\.java$/,
                  /Calculator\.java$/, /Finalizador\.java$/, /Inicializador\.java$/],
        priority: 1,
        description: '🔴 CRÍTICO - Lógica de negócio central'
    },
    P2_IMPORTANT: {
        patterns: [/Controller\.java$/, /Mapper\.java$/],
        priority: 2,
        description: '🟡 IMPORTANTE - API e transformação de dados'
    },
    P3_NORMAL: {
        patterns: [/Model\.java$/, /\.java$/],
        priority: 3,
        description: '🟢 NORMAL - Entidades e utilitários'
    }
};

// Arquivos que podem ser ignorados - match build.gradle.kts jacocoTestReport exclusions
const IGNORE_PATTERNS = [
    // Gerados automaticamente
    /MapperImpl/,
    
    // Bootstrap e configuração
    /Sgc\.java$/,
    /Config.*\.java$/,
    /Properties\.java$/,
    
    // DTOs e Request/Response (apenas dados)
    /Dto\.java$/,
    /Request\.java$/,
    /Response\.java$/,
    
    // Exceções (maioria simples)
    /Erro.*\.java$/,
    /Exception\.java$/,
    
    // Mocks de teste
    /Mock\.java$/,
    /Test\.java$/,
    
    // Repositórios (interfaces JPA)
    /Repo\.java$/,
    
    // Entidades JPA simples (sem lógica de negócio)
    /model\/Perfil\.java$/,
    /model\/Usuario\.java$/,
    /model\/Unidade.*\.java$/,
    /model\/Administrador\.java$/,
    /model\/Vinculacao.*\.java$/,
    /model\/Atribuicao.*\.java$/,
    /model\/Parametro\.java$/,
    /model\/Movimentacao\.java$/,
    /model\/Analise\.java$/,
    /model\/Alerta.*\.java$/,
    /model\/Conhecimento\.java$/,
    /model\/Mapa\.java$/,
    /model\/Atividade\.java$/,
    /model\/Competencia.*\.java$/,
    /model\/Notificacao\.java$/,
    /model\/Processo\.java$/,
    
    // Enums simples sem lógica de negócio
    /Status.*\.java$/,
    /Tipo.*\.java$/
];

async function parseXml(filePath) {
    const data = fs.readFileSync(filePath);
    const parser = new xml2js.Parser();
    return parser.parseStringPromise(data);
}

function calculatePercentage(covered, missed) {
    const total = covered + missed;
    return total === 0 ? 100 : (covered / total) * 100;
}

function categorizeFile(className) {
    // Verificar se deve ser ignorado
    if (IGNORE_PATTERNS.some(pattern => pattern.test(className))) {
        return null;
    }

    // Categorizar por prioridade
    for (const [categoryName, category] of Object.entries(CATEGORIES)) {
        if (category.patterns.some(pattern => pattern.test(className))) {
            return { name: categoryName, ...category };
        }
    }

    return CATEGORIES.P3_NORMAL;
}

async function runTests() {
    console.log('🚀 Executando testes e gerando relatório JaCoCo...');
    try {
        execSync(GRADLE_CMD, { cwd: BASE_DIR, stdio: 'inherit' });
    } catch (error) {
        console.warn('⚠️ Alguns testes falharam, mas prosseguindo com análise...');
    }
}

async function analyzeAndGeneratePlan() {
    if (!fs.existsSync(REPORT_PATH)) {
        console.error(`❌ Relatório não encontrado: ${REPORT_PATH}`);
        console.error('Execute os testes primeiro: ./gradlew :backend:test :backend:jacocoTestReport');
        process.exit(1);
    }

    console.log('📊 Analisando relatório JaCoCo...');
    const report = await parseXml(REPORT_PATH);
    const packages = report.report.package || [];

    const results = {
        P1_CRITICAL: [],
        P2_IMPORTANT: [],
        P3_NORMAL: []
    };

    let totalFiles = 0;
    let filesWithGaps = 0;
    let totalLines = 0;
    let coveredLines = 0;
    let totalBranches = 0;
    let coveredBranches = 0;

    packages.forEach(pkg => {
        const packageName = pkg.$.name.replaceAll('/', '.');
        const sourceFiles = pkg.sourcefile || [];

        sourceFiles.forEach(sf => {
            const fileName = sf.$.name;
            const className = `${packageName}.${fileName.replace('.java', '')}`;
            const category = categorizeFile(className);

            if (!category) return; // Ignora arquivos que não precisam de cobertura

            totalFiles++;

            let linesTotal = 0;
            let linesCovered = 0;
            let branchesTotal = 0;
            let branchesCovered = 0;
            const missedLines = [];
            const missedBranches = [];

            if (sf.line) {
                sf.line.forEach(line => {
                    const nr = Number.parseInt(line.$.nr);
                    const ci = Number.parseInt(line.$.ci || 0);
                    const mb = Number.parseInt(line.$.mb || 0);
                    const cb = Number.parseInt(line.$.cb || 0);

                    linesTotal++;
                    totalLines++;

                    if (ci > 0) {
                        linesCovered++;
                        coveredLines++;
                    } else {
                        missedLines.push(nr);
                    }

                    const lineBranches = mb + cb;
                    if (lineBranches > 0) {
                        branchesTotal += lineBranches;
                        totalBranches += lineBranches;
                        branchesCovered += cb;
                        coveredBranches += cb;

                        if (mb > 0) {
                            missedBranches.push(`${nr}(${mb}/${lineBranches})`);
                        }
                    }
                });
            }

            const lineCoverage = calculatePercentage(linesCovered, linesTotal - linesCovered);
            const branchCoverage = branchesTotal > 0 ? calculatePercentage(branchesCovered, branchesTotal - branchesCovered) : 100;

            // Se não tem 100% de cobertura
            if (lineCoverage < 100 || branchCoverage < 100) {
                filesWithGaps++;
                
                const score = (linesTotal - linesCovered) + (branchesTotal - branchesCovered) * 0.5;

                const categoryName = category.name || 'P3_NORMAL';
                if (!results[categoryName]) {
                    results[categoryName] = [];
                }

                results[categoryName].push({
                    class: className,
                    fileName: fileName,
                    lineCoverage: lineCoverage.toFixed(2),
                    branchCoverage: branchCoverage.toFixed(2),
                    missedLines: missedLines.length,
                    missedBranches: branchesTotal - branchesCovered,
                    missedLinesDetails: missedLines.slice(0, 50), // Limita para não ficar muito grande
                    missedBranchesDetails: missedBranches.slice(0, 20),
                    score,
                    priority: category.priority
                });
            }
        });
    });

    // Ordenar por score (mais gaps primeiro) dentro de cada categoria
    Object.keys(results).forEach(category => {
        results[category].sort((a, b) => b.score - a.score);
    });

    // Gerar Markdown
    const globalLineCoverage = calculatePercentage(coveredLines, totalLines - coveredLines);
    const globalBranchCoverage = totalBranches > 0 ? calculatePercentage(coveredBranches, totalBranches - coveredBranches) : 100;

    let markdown = `# 🎯 Plano para Alcançar 100% de Cobertura de Testes

**Gerado em:** ${new Date().toISOString().split('T')[0]}

## 📊 Situação Atual

- **Cobertura Global de Linhas:** ${globalLineCoverage.toFixed(2)}%
- **Cobertura Global de Branches:** ${globalBranchCoverage.toFixed(2)}%
- **Total de Arquivos Analisados:** ${totalFiles}
- **Arquivos com Cobertura < 100%:** ${filesWithGaps}
- **Arquivos com 100% de Cobertura:** ${totalFiles - filesWithGaps}

## 🎯 Objetivo

Alcançar **100% de cobertura** em todas as classes relevantes do projeto.

## 📋 Progresso por Categoria

`;

    // Adicionar resumo por categoria
    Object.entries(CATEGORIES).forEach(([categoryName, category]) => {
        const items = results[categoryName];
        markdown += `- **${category.description}:** ${items.length} arquivo(s) pendente(s)\n`;
    });

    markdown += `\n---\n\n`;

    // Detalhar cada categoria
    Object.entries(CATEGORIES).forEach(([categoryName, category]) => {
        const items = results[categoryName];
        
        if (items.length === 0) {
            markdown += `## ${category.description}\n\n✅ **Todos os arquivos desta categoria têm 100% de cobertura!**\n\n`;
            return;
        }

        markdown += `## ${category.description}\n\n`;
        markdown += `**Total:** ${items.length} arquivo(s) com lacunas\n\n`;

        items.forEach((item, index) => {
            markdown += `### ${index + 1}. \`${item.class}\`\n\n`;
            markdown += `- **Cobertura de Linhas:** ${item.lineCoverage}% (${item.missedLines} linha(s) não cobertas)\n`;
            markdown += `- **Cobertura de Branches:** ${item.branchCoverage}% (${item.missedBranches} branch(es) não cobertos)\n`;
            
            if (item.missedLinesDetails.length > 0) {
                markdown += `- **Linhas não cobertas:** ${item.missedLinesDetails.join(', ')}`;
                if (item.missedLines > item.missedLinesDetails.length) {
                    markdown += ` ... (+${item.missedLines - item.missedLinesDetails.length} mais)`;
                }
                markdown += `\n`;
            }

            if (item.missedBranchesDetails.length > 0) {
                markdown += `- **Branches não cobertos:** ${item.missedBranchesDetails.join(', ')}\n`;
            }

            markdown += `\n**Ação necessária:** Criar ou expandir \`${item.fileName.replace('.java', 'CoverageTest.java')}\` para cobrir todas as linhas e branches.\n\n`;
        });
    });

    markdown += `\n---\n\n`;
    markdown += `## 🛠️ Scripts Disponíveis\n\n`;
    markdown += `Use os seguintes scripts em \`backend/etc/scripts/\` para auxiliar:\n\n`;
    markdown += `1. \`node super-cobertura.cjs --run\` - Gera relatório de lacunas\n`;
    markdown += `2. \`node verificar-cobertura.cjs --missed\` - Lista arquivos com mais gaps\n`;
    markdown += `3. \`node analisar-cobertura.cjs\` - Análise detalhada com tabelas\n`;
    markdown += `4. \`python3 analyze_tests.py\` - Identifica arquivos sem testes\n`;
    markdown += `5. \`python3 prioritize_tests.py\` - Prioriza criação de testes\n\n`;

    // Salvar arquivo
    fs.writeFileSync(OUTPUT_FILE, markdown);
    console.log(`\n✅ Plano de ação gerado em: ${OUTPUT_FILE}`);
    console.log(`📊 ${filesWithGaps} arquivo(s) precisam de cobertura adicional`);
    console.log(`🎯 Meta: De ${globalLineCoverage.toFixed(2)}% → 100%`);
}

async function main() {
    const runTestsArg = process.argv.includes('--run');
    
    if (runTestsArg) {
        await runTests();
    }

    await analyzeAndGeneratePlan();
}

main().catch(err => {
    console.error('❌ Erro:', err);
    process.exit(1);
});
