const {
    calcularPercentual,
    coletarArquivosCobertura,
    lerRelatorioJacoco
} = require('./lib/cobertura-base.cjs');
const {exibirAjudaComando} = require('./lib/cli-ajuda.cjs');

const args = process.argv.slice(2);
const help = args.includes('--help') || args.includes('-h');
const jsonMode = args.includes('--json');
const filterArg = args.find(arg => !arg.startsWith('-')) || null;
const minCovArg = args.find(arg => arg.startsWith('--min='))?.split('=')[1] || '99';
const showMissed = args.includes('--missed') || args.includes('--details');
const simpleMode = args.includes('--simple');
const failUnderArg = args.find(arg => arg.startsWith('--fail-under='))?.split('=')[1] || null;
const minCoverage = Number.parseFloat(minCovArg);

if (help) {
    exibirAjudaComando({
        comandoSgc: 'cobertura verificar',
        scriptDireto: 'cobertura-verificar.cjs',
        descricao: 'Consulta a cobertura global e por classe, com filtros e modo detalhado.',
        argumentos: '[filtro]',
        opcoes: [
            '--min=<n>           Filtra classes com cobertura de linha menor que <n>% (padrao: 99)',
            '--missed            Exibe detalhes das linhas e branches nao cobertos',
            '--simple            Saida simplificada no modo --missed',
            '--fail-under=<n>    Falha (exit 1) se a cobertura global de instrucoes for inferior a <n>%',
            '--json              Emite saida estruturada em JSON',
            '--help, -h          Exibe esta ajuda'
        ],
        exemplos: [
            'node etc/scripts/sgc.js backend cobertura verificar',
            'node etc/scripts/sgc.js backend cobertura verificar sgc.subprocesso --min=95',
            'node etc/scripts/sgc.js backend cobertura verificar --missed'
        ]
    });
    process.exit(0);
}

function imprimirJson(dados) {
    console.log(JSON.stringify(dados, null, 2));
}

async function main() {
    const relatorio = await lerRelatorioJacoco();
    const coleta = coletarArquivosCobertura(relatorio, {
        incluirSemLacunas: true,
        aplicarExclusoes: false,
        filtro: filterArg
    });
    const contadoresGlobais = relatorio.report.counter || [];
    const contadorInstrucao = contadoresGlobais.find(contador => contador.$.type === 'INSTRUCTION');
    const contadorLinha = contadoresGlobais.find(contador => contador.$.type === 'LINE');
    const contadorBranch = contadoresGlobais.find(contador => contador.$.type === 'BRANCH');

    const instrucoesCobertas = Number.parseInt(contadorInstrucao?.$.covered || 0, 10);
    const instrucoesPerdidas = Number.parseInt(contadorInstrucao?.$.missed || 0, 10);
    const linhasCobertas = Number.parseInt(contadorLinha?.$.covered || 0, 10);
    const linhasPerdidas = Number.parseInt(contadorLinha?.$.missed || 0, 10);
    const branchesCobertos = Number.parseInt(contadorBranch?.$.covered || 0, 10);
    const branchesPerdidos = Number.parseInt(contadorBranch?.$.missed || 0, 10);
    const coberturaGlobal = {
        instrucaoPercentual: calcularPercentual(instrucoesCobertas, instrucoesPerdidas),
        linhaPercentual: calcularPercentual(linhasCobertas, linhasPerdidas),
        branchPercentual: calcularPercentual(branchesCobertos, branchesPerdidos),
        instrucoesCobertas,
        instrucoesPerdidas,
        linhasCobertas,
        linhasPerdidas,
        branchesCobertos,
        branchesPerdidos
    };

    if (!jsonMode) {
        console.log(`\n${'='.repeat(100)}`);
        console.log('RELATORIO DE COBERTURA JACOCO'.padStart(65));
        console.log('='.repeat(100));
    }

    if (showMissed) {
        const comLacunas = coleta.arquivos
            .filter(arquivo => arquivo.linhasPerdidas > 0 || arquivo.branchesPerdidos > 0)
            .sort((a, b) => {
                const scoreA = a.linhasPerdidas + (a.branchesPerdidos * 0.5);
                const scoreB = b.linhasPerdidas + (b.branchesPerdidos * 0.5);
                return scoreB - scoreA || a.nomeClasse.localeCompare(b.nomeClasse, 'pt-BR');
            });

        if (jsonMode) {
            imprimirJson({
                filtro: filterArg,
                modo: 'missed',
                simples: simpleMode,
                coberturaGlobal,
                totalArquivosComLacunas: comLacunas.length,
                arquivos: comLacunas.slice(0, 50)
            });
            return;
        }

        console.log(`\nARQUIVOS COM MAIS LINHAS/BRANCHES PERDIDAS (Total: ${comLacunas.length})\n`);

        comLacunas.slice(0, 50).forEach(arquivo => {
            const nomeArquivo = `${arquivo.nomePacote}.${arquivo.nomeArquivo}`;
            if (simpleMode) {
                console.log(`${nomeArquivo}: MISS-L [${arquivo.linhasPerdidasLista.join(',')}] MISS-B [${arquivo.branchesPerdidosLista.join(',')}]`);
                return;
            }

            console.log(`${nomeArquivo}`);
            if (arquivo.linhasPerdidasLista.length > 0) {
                console.log(`   Linhas nao executadas: ${arquivo.linhasPerdidasLista.join(', ')}`);
            }
            if (arquivo.branchesPerdidosLista.length > 0) {
                console.log(`   Branches perdidos (miss/total): ${arquivo.branchesPerdidosLista.join(', ')}`);
            }
            console.log('-'.repeat(50));
        });
        return;
    }

    if (contadorInstrucao) {
        const percentualInstrucao = coberturaGlobal.instrucaoPercentual;
        const percentualLinha = coberturaGlobal.linhaPercentual;
        const totalInstrucoes = instrucoesCobertas + instrucoesPerdidas;

        console.log(`| ${'TOTAL DO PROJETO'.padEnd(45)} | ${percentualInstrucao.toFixed(2).padStart(8)}% | ${percentualLinha.toFixed(2).padStart(8)}% | ${String(instrucoesCobertas).padStart(10)} | ${String(totalInstrucoes).padStart(10)} |`);
    }

    if (!jsonMode) {
        console.log(`${'='.repeat(100)}\n`);
    }

    if (!jsonMode && contadorBranch) {
        const percentualBranch = coberturaGlobal.branchPercentual;
        console.log(`Cobertura de Branches (Global): ${percentualBranch.toFixed(2)}% (${branchesCobertos}/${branchesCobertos + branchesPerdidos})`);
    }

    if (!jsonMode) {
        console.log(`\nDETALHE POR CLASSE (Filtrado por < ${minCoverage}%)\n`);
        console.log(`| ${'Classe'.padEnd(60)} | ${'Linhas %'.padEnd(10)} | ${'Missed L'.padEnd(10)} | ${'Branches %'.padEnd(10)} | ${'Missed B'.padEnd(10)} |`);
        console.log('-'.repeat(115));
    }

    const filtrados = coleta.arquivos
        .filter(arquivo => arquivo.coberturaLinhas < minCoverage)
        .sort((a, b) => b.branchesPerdidos - a.branchesPerdidos || b.linhasPerdidas - a.linhasPerdidas);

    if (jsonMode) {
        imprimirJson({
            filtro: filterArg,
            modo: 'padrao',
            minCoverage,
            coberturaGlobal,
            totalFiltrados: filtrados.length,
            classes: filtrados
        });
    }

    if (!jsonMode) {
        filtrados.slice(0, 20).forEach(arquivo => {
            const branchStr = arquivo.totalBranches > 0 ? `${arquivo.coberturaBranches.toFixed(2)}%` : 'N/A';
            console.log(`| ${arquivo.nomeClasse.padEnd(60)} | ${arquivo.coberturaLinhas.toFixed(2).padStart(8)}% | ${String(arquivo.linhasPerdidas).padStart(10)} | ${branchStr.padStart(10)} | ${String(arquivo.branchesPerdidos).padStart(10)} |`);
        });
    }

    if (!jsonMode && filtrados.length === 0) {
        console.log('Nenhuma classe abaixo do limite de cobertura encontrado.');
    } else if (!jsonMode && filtrados.length > 20) {
        console.log(`\n... e mais ${filtrados.length - 20} classes.`);
    }

    if (failUnderArg && contadorInstrucao) {
        const instrucoesCobertas = Number.parseInt(contadorInstrucao.$.covered || 0, 10);
        const instrucoesPerdidas = Number.parseInt(contadorInstrucao.$.missed || 0, 10);
        const coberturaAtual = calcularPercentual(instrucoesCobertas, instrucoesPerdidas);
        const meta = Number.parseFloat(failUnderArg);

        if (coberturaAtual < meta) {
            if (jsonMode) {
                imprimirJson({
                    status: 'erro',
                    mensagem: `Cobertura global (${coberturaAtual.toFixed(2)}%) abaixo do esperado (${meta.toFixed(2)}%).`,
                    coberturaAtual,
                    meta
                });
            } else {
                console.error(`\nERRO: Cobertura global (${coberturaAtual.toFixed(2)}%) abaixo do esperado (${meta.toFixed(2)}%).`);
            }
            process.exit(1);
        }

        if (!jsonMode) {
            console.log(`\nCobertura global (${coberturaAtual.toFixed(2)}%) atingiu a meta (${meta.toFixed(2)}%).`);
        }
    }
}

main().catch(error => {
    if (jsonMode) {
        imprimirJson({
            status: 'erro',
            mensagem: `Erro ao verificar cobertura: ${error.message}`
        });
    } else {
        console.error(`Erro ao verificar cobertura: ${error.message}`);
    }
    process.exit(1);
});
