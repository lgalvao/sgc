const fs = require('node:fs');
const {
    LACUNAS_JSON_PATH,
    coletarArquivosCobertura,
    executarGradleJaCoCo,
    lerRelatorioJacoco,
    ordenarPorLacunas
} = require('./lib/cobertura-base.cjs');

async function main() {
    const args = process.argv.slice(2);
    if (args.includes('--help') || args.includes('-h')) {
        console.log(`Uso: node backend/etc/scripts/super-cobertura.cjs [opcoes]

Opcoes:
  --run         Executa o Gradle antes de analisar o relatorio JaCoCo
  --help, -h    Exibe esta ajuda`);
        process.exit(0);
    }

    if (args.includes('--run')) {
        console.log('Executando :backend:jacocoTestReport...');
        try {
            executarGradleJaCoCo();
        } catch (error) {
            console.warn(`Gradle terminou com erro. Tentando continuar com o ultimo relatorio disponivel: ${error.message}`);
        }
    }

    const relatorio = await lerRelatorioJacoco();
    const {arquivos, totais} = coletarArquivosCobertura(relatorio, {
        incluirSemLacunas: false,
        aplicarExclusoes: true
    });
    const ordenados = ordenarPorLacunas(arquivos);

    const resumo = {
        globalLineCoverage: `${totais.coberturaGlobalLinhas.toFixed(2)}%`,
        globalBranchCoverage: `${totais.coberturaGlobalBranches.toFixed(2)}%`,
        totalFilesWithGaps: ordenados.length,
        gaps: ordenados.map(arquivo => ({
            class: arquivo.nomeClasse,
            coverage: `${arquivo.coberturaLinhas.toFixed(2)}%`,
            branchCoverage: `${arquivo.coberturaBranches.toFixed(2)}%`,
            missed: arquivo.linhasPerdidasLista,
            partial: arquivo.branchesPerdidosLista,
            score: arquivo.linhasPerdidas + (arquivo.branchesPerdidos * 0.5)
        }))
    };

    fs.writeFileSync(LACUNAS_JSON_PATH, JSON.stringify(resumo, null, 2), 'utf-8');

    console.log('\nRELATORIO DE LACUNAS DE COBERTURA');
    console.log(`Cobertura Global de Linhas: ${resumo.globalLineCoverage}`);
    console.log(`Cobertura Global de Branches: ${resumo.globalBranchCoverage}`);
    console.log(`Arquivos com lacunas: ${resumo.totalFilesWithGaps}`);

    ordenados.forEach(arquivo => {
        console.log(`\n${arquivo.nomeClasse} [${arquivo.coberturaLinhas.toFixed(2)}%]`);
        if (arquivo.linhasPerdidasLista.length > 0) {
            console.log(`   Linhas perdidas: ${arquivo.linhasPerdidasLista.join(', ')}`);
        }
        if (arquivo.branchesPerdidosLista.length > 0) {
            console.log(`   Branches parciais: ${arquivo.branchesPerdidosLista.join(', ')}`);
        }
    });

    console.log(`\nRelatorio completo salvo em: ${LACUNAS_JSON_PATH}\n`);
}

main().catch(error => {
    console.error(`Erro ao gerar relatorio de lacunas: ${error.message}`);
    process.exit(1);
});
