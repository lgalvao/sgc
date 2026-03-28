const {
    coletarArquivosCobertura,
    executarGradleJaCoCo,
    lerRelatorioJacoco
} = require('./lib/cobertura-base.cjs');

function formatarListaLinhas(linhas) {
    if (linhas.length === 0) {
        return '';
    }

    if (linhas.length > 20) {
        return `${linhas.slice(0, 15).join(', ')}... (+ ${linhas.length - 15})`;
    }

    return linhas.join(', ');
}

async function main() {
    const skipRun = process.argv.includes('--skip-run');

    console.log('=== Iniciando Analise de Cobertura ===');

    if (!skipRun) {
        console.log('Executando Gradle para atualizar o relatorio JaCoCo...');
        console.log('Aguarde, isso pode levar alguns minutos.\n');

        try {
            executarGradleJaCoCo();
        } catch (error) {
            console.warn(`\nA execucao do Gradle falhou, tentando seguir com o ultimo relatorio disponivel. Motivo: ${error.message}\n`);
        }
    } else {
        console.log('Pulando execucao do Gradle (--skip-run)...');
    }

    const relatorio = await lerRelatorioJacoco();
    const {arquivos} = coletarArquivosCobertura(relatorio, {
        incluirSemLacunas: true,
        aplicarExclusoes: false
    });

    const tabela = arquivos.map(arquivo => ({
        Arquivo: `${arquivo.nomePacote}/${arquivo.nomeArquivo}`,
        'Cxn Total': arquivo.complexidade,
        'Linhas T.': arquivo.totalLinhas,
        'Linhas Cob.': arquivo.linhasCobertas,
        'Linhas N.C.': arquivo.linhasPerdidas,
        '% Linhas': `${arquivo.coberturaLinhas.toFixed(1)}%`,
        'Lista N.C.': formatarListaLinhas(arquivo.linhasPerdidasLista),
        'Branches T.': arquivo.totalBranches,
        'Branches Cob.': arquivo.branchesCobertos,
        'Branches N.C.': arquivo.branchesPerdidos,
        '% Branches': arquivo.totalBranches > 0 ? `${arquivo.coberturaBranches.toFixed(1)}%` : 'N/A',
        'Lista Br. N.C.': formatarListaLinhas(arquivo.branchesPerdidosLista)
    }));

    tabela.sort((a, b) => Number.parseFloat(a['% Linhas']) - Number.parseFloat(b['% Linhas']));

    console.table(tabela);
    console.log('\n--- Resumo Geral ---');
    console.log(`Total de arquivos analisados: ${tabela.length}`);
}

main().catch(error => {
    console.error(`Falha na execucao da analise: ${error.message}`);
    process.exit(1);
});
