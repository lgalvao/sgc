#!/usr/bin/env node
const fs = require('node:fs');
const {
    PLANO_RESUMIDO_PATH,
    coletarArquivosCobertura,
    executarGradleJaCoCo,
    lerRelatorioJacoco
} = require('./lib/cobertura-base.cjs');

function calcularScoreAcao(arquivo) {
    return arquivo.linhasPerdidas + (arquivo.branchesPerdidos * 2) + (arquivo.complexidade * 0.5);
}

function gerarMarkdown(arquivos) {
    let markdown = '# Plano de Cobertura Backend\n\n';
    markdown += `**Gerado em:** ${new Date().toLocaleString('pt-BR')}\n\n`;
    markdown += '| Prioridade | Classe | Cobertura L | Cobertura B | Score | Ação |\n';
    markdown += '| :--- | :--- | :--- | :--- | :--- | :--- |\n';

    arquivos.forEach(arquivo => {
        const scoreAcao = calcularScoreAcao(arquivo);
        const prioridade = scoreAcao > 20 ? 'CRÍTICO' : (scoreAcao > 10 ? 'MÉDIO' : 'BAIXO');
        const nomeClasse = arquivo.nomeClasse.split('.').pop();
        markdown += `| ${prioridade} | \`${arquivo.nomeClasse}\` | ${arquivo.coberturaLinhas.toFixed(1)}% | ${arquivo.coberturaBranches.toFixed(1)}% | ${scoreAcao.toFixed(1)} | \`node backend/etc/scripts/testes-gerar-stub.cjs ${nomeClasse}\` |\n`;
    });

    return markdown;
}

function imprimirResumo(arquivos) {
    console.log(`\n${'='.repeat(80)}`);
    console.log('TOP 10 PRIORIDADES DE COBERTURA');
    console.log('='.repeat(80));

    arquivos.slice(0, 10).forEach((arquivo, indice) => {
        const scoreAcao = calcularScoreAcao(arquivo);
        const severidade = scoreAcao > 20 ? 'CRITICO' : (scoreAcao > 10 ? 'MEDIO' : 'BAIXO');
        console.log(`${indice + 1}. [${severidade}] [Score: ${scoreAcao.toFixed(1)}] ${arquivo.nomeClasse}`);
        console.log(`   L: ${arquivo.coberturaLinhas.toFixed(1)}% | B: ${arquivo.coberturaBranches.toFixed(1)}%`);
        console.log(`   Comando: node backend/etc/scripts/testes-gerar-stub.cjs ${arquivo.nomeClasse.split('.').pop()}`);
    });

    console.log(`\nPlano completo gerado em: ${PLANO_RESUMIDO_PATH}`);
}

async function main() {
    const args = process.argv.slice(2);
    if (args.includes('--help') || args.includes('-h')) {
        console.log(`Uso: node backend/etc/scripts/cobertura-priorizar.cjs [opcoes]

Opcoes:
  --skip-run    Nao executa o Gradle; usa o ultimo relatorio JaCoCo disponivel
  --help, -h    Exibe esta ajuda`);
        process.exit(0);
    }

    const skipRun = args.includes('--skip-run');

    console.log('\n[Analisador] Iniciando diagnostico de cobertura...');

    if (!skipRun) {
        console.log('[1/3] Atualizando relatorio JaCoCo via Gradle...');
        try {
            executarGradleJaCoCo();
        } catch (error) {
            console.warn(`[Aviso] Gradle reportou erro, tentando continuar com o ultimo relatorio disponivel: ${error.message}`);
        }
    }

    const relatorio = await lerRelatorioJacoco();
    const {arquivos} = coletarArquivosCobertura(relatorio, {
        incluirSemLacunas: false,
        aplicarExclusoes: true
    });
    const priorizados = [...arquivos].sort((a, b) => calcularScoreAcao(b) - calcularScoreAcao(a));

    fs.writeFileSync(PLANO_RESUMIDO_PATH, gerarMarkdown(priorizados), 'utf-8');
    imprimirResumo(priorizados);
}

main().catch(error => {
    console.error(`Erro ao analisar cobertura total: ${error.message}`);
    process.exit(1);
});
