#!/usr/bin/env node
const fs = require('node:fs');
const {
    PLANO_100_PATH,
    coletarArquivosCobertura,
    executarGradleJaCoCo,
    lerRelatorioJacoco,
    ordenarPorLacunas
} = require('./lib/cobertura-base.cjs');

const CATEGORIAS = {
    P1_CRITICAL: {
        patterns: [/Service$/, /Facade$/, /Policy$/, /Validator$/, /Listener$/, /Factory$/, /Builder$/, /Calculator$/, /Finalizador$/, /Inicializador$/],
        prioridade: 1,
        descricao: 'CRITICO - Logica de negocio central'
    },
    P2_IMPORTANT: {
        patterns: [/Controller$/, /Mapper$/],
        prioridade: 2,
        descricao: 'IMPORTANTE - API e transformacao de dados'
    },
    P3_NORMAL: {
        patterns: [/.+/],
        prioridade: 3,
        descricao: 'NORMAL - Entidades e utilitarios'
    }
};

function categorizarArquivo(nomeClasse) {
    for (const [nomeCategoria, categoria] of Object.entries(CATEGORIAS)) {
        if (categoria.patterns.some(pattern => pattern.test(nomeClasse))) {
            return {nome: nomeCategoria, ...categoria};
        }
    }

    return {nome: 'P3_NORMAL', ...CATEGORIAS.P3_NORMAL};
}

function agruparPorCategoria(arquivos) {
    const agrupado = {
        P1_CRITICAL: [],
        P2_IMPORTANT: [],
        P3_NORMAL: []
    };

    arquivos.forEach(arquivo => {
        const categoria = categorizarArquivo(arquivo.nomeClasse);
        agrupado[categoria.nome].push({
            ...arquivo,
            prioridade: categoria.prioridade
        });
    });

    Object.keys(agrupado).forEach(chave => {
        agrupado[chave].sort((a, b) => {
            const scoreA = a.linhasPerdidas + (a.branchesPerdidos * 0.5);
            const scoreB = b.linhasPerdidas + (b.branchesPerdidos * 0.5);
            return scoreB - scoreA || a.nomeClasse.localeCompare(b.nomeClasse, 'pt-BR');
        });
    });

    return agrupado;
}

function gerarMarkdown(agrupado, totais) {
    let markdown = '# Plano para Alcançar 100% de Cobertura de Testes\n\n';
    markdown += `**Gerado em:** ${new Date().toISOString().split('T')[0]}\n\n`;
    markdown += '## Situacao Atual\n\n';
    markdown += `- **Cobertura Global de Linhas:** ${totais.coberturaGlobalLinhas.toFixed(2)}%\n`;
    markdown += `- **Cobertura Global de Branches:** ${totais.coberturaGlobalBranches.toFixed(2)}%\n`;
    markdown += `- **Total de Arquivos Analisados:** ${totais.totalArquivos}\n`;

    const pendentes = Object.values(agrupado).reduce((soma, lista) => soma + lista.length, 0);
    markdown += `- **Arquivos com Cobertura < 100%:** ${pendentes}\n`;
    markdown += `- **Arquivos com 100% de Cobertura:** ${totais.totalArquivos - pendentes}\n\n`;

    markdown += '## Progresso por Categoria\n\n';
    Object.entries(CATEGORIAS).forEach(([nomeCategoria, categoria]) => {
        markdown += `- **${categoria.descricao}:** ${agrupado[nomeCategoria].length} arquivo(s) pendente(s)\n`;
    });

    markdown += '\n---\n';

    Object.entries(CATEGORIAS).forEach(([nomeCategoria, categoria]) => {
        const itens = agrupado[nomeCategoria];
        markdown += `\n## ${categoria.descricao}\n\n`;

        if (itens.length === 0) {
            markdown += 'Todos os arquivos desta categoria estao com 100% de cobertura.\n';
            return;
        }

        markdown += `**Total:** ${itens.length} arquivo(s) com lacunas\n\n`;

        itens.forEach((item, indice) => {
            markdown += `### ${indice + 1}. \`${item.nomeClasse}\`\n\n`;
            markdown += `- **Cobertura de Linhas:** ${item.coberturaLinhas.toFixed(2)}% (${item.linhasPerdidas} linha(s) nao cobertas)\n`;
            markdown += `- **Cobertura de Branches:** ${item.coberturaBranches.toFixed(2)}% (${item.branchesPerdidos} branch(es) nao cobertos)\n`;

            if (item.linhasPerdidasLista.length > 0) {
                markdown += `- **Linhas nao cobertas:** ${item.linhasPerdidasLista.slice(0, 50).join(', ')}`;
                if (item.linhasPerdidasLista.length > 50) {
                    markdown += ` ... (+${item.linhasPerdidasLista.length - 50} mais)`;
                }
                markdown += '\n';
            }

            if (item.branchesPerdidosLista.length > 0) {
                markdown += `- **Branches nao cobertos:** ${item.branchesPerdidosLista.slice(0, 20).join(', ')}\n`;
            }

            markdown += `\n**Acao necessaria:** Criar ou expandir \`${item.nomeArquivo.replace('.java', 'CoverageTest.java')}\` para cobrir todas as linhas e branches.\n\n`;
        });
    });

    markdown += '---\n\n';
    markdown += '## Scripts Disponiveis\n\n';
    markdown += '1. `node backend/etc/scripts/super-cobertura.cjs --run` - Gera relatorio de lacunas\n';
    markdown += '2. `node backend/etc/scripts/verificar-cobertura.cjs --missed` - Lista arquivos com mais gaps\n';
    markdown += '3. `node backend/etc/scripts/analisar-cobertura.cjs` - Analise detalhada com tabelas\n';
    markdown += '4. `node backend/etc/scripts/analyze_tests.cjs` - Identifica arquivos sem testes\n';
    markdown += '5. `node backend/etc/scripts/prioritize_tests.cjs` - Prioriza criacao de testes\n';

    return markdown;
}

async function main() {
    const args = process.argv.slice(2);
    if (args.includes('--help') || args.includes('-h')) {
        console.log(`Uso: node backend/etc/scripts/gerar-plano-cobertura.cjs [opcoes]

Opcoes:
  --run         Executa os testes e gera o relatorio JaCoCo antes da analise
  --help, -h    Exibe esta ajuda`);
        process.exit(0);
    }

    if (args.includes('--run')) {
        console.log('Executando testes e gerando relatorio JaCoCo...');
        try {
            executarGradleJaCoCo({incluirTestes: true});
        } catch (error) {
            console.warn(`Alguns testes falharam. Tentando continuar com o ultimo relatorio disponivel: ${error.message}`);
        }
    }

    const relatorio = await lerRelatorioJacoco();
    const coleta = coletarArquivosCobertura(relatorio, {
        incluirSemLacunas: false,
        aplicarExclusoes: true
    });
    const agrupado = agruparPorCategoria(ordenarPorLacunas(coleta.arquivos));
    const markdown = gerarMarkdown(agrupado, coleta.totais);

    fs.writeFileSync(PLANO_100_PATH, markdown, 'utf-8');
    console.log(`\nPlano de acao gerado em: ${PLANO_100_PATH}`);
    console.log(`${coleta.arquivos.length} arquivo(s) precisam de cobertura adicional`);
    console.log(`Meta: De ${coleta.totais.coberturaGlobalLinhas.toFixed(2)}% -> 100%`);
}

main().catch(error => {
    console.error(`Erro ao gerar plano de cobertura: ${error.message}`);
    process.exit(1);
});
