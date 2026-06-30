import fs from "fs";
import path from "path";

const SCAN_RESULTS_FILE = path.join(process.cwd(), 'a11y-scan-results.json');
const REPORT_FILE = path.join(process.cwd(), 'relatorio-a11y-completo.md');

function generateMarkdown(results) {
    let md = "# Relatório Completo de Acessibilidade (Axe-core Crawler)\n\n";
    md += `Data da auditoria: ${new Date().toLocaleString()}\n\n`;

    let totalViolations = 0;
    results.forEach(r => totalViolations += r.violations.length);

    md += `## Resumo Geral\n`;
    md += `- **Páginas Auditadas:** ${results.length}\n`;
    md += `- **Total de Violações:** ${totalViolations}\n\n`;

    md += `## Detalhamento por Página\n\n`;

    results.forEach(result => {
        md += `### ${result.name} (\`${result.route}\`)\n`;

        if (result.violations.length === 0) {
            md += "✅ Nenhuma violação detectada.\n\n";
        } else {
            md += "| Impacto | Problema | Elementos Afetados |\n";
            md += "|:---:|:---|:---|\n";

            result.violations.forEach(v => {
                const elementos = v.nodes.map(n => `\`${n.target.join(' ')}\``).join('<br>');
                md += `| ${v.impact} | **${v.id}**: ${v.help} | ${elementos} |\n`;
            });
            md += "\n";
        }
    });

    md += `## Recomendações Técnicas Baseadas no Axe-core\n\n`;
    md += `Para cada violação listada acima, o Axe-core recomenda:\n\n`;

    // De-duplicate recommendations
    const recommendations = new Set();
    results.forEach(r => {
        r.violations.forEach(v => {
            recommendations.add(`- **${v.id}**: ${v.description} [Referência](${v.helpUrl})`);
        });
    });

    recommendations.forEach(rec => md += rec + "\n");

    return md;
}

if (!fs.existsSync(SCAN_RESULTS_FILE)) {
    console.error("Arquivo de resultados não encontrado. Execute o crawler primeiro.");
    process.exit(1);
}

const scanResults = JSON.parse(fs.readFileSync(SCAN_RESULTS_FILE, 'utf-8'));
const reportMd = generateMarkdown(scanResults);
fs.writeFileSync(REPORT_FILE, reportMd);

console.log(`Relatório consolidado gerado em: ${REPORT_FILE}`);
