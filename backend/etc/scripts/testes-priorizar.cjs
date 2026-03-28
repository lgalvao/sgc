#!/usr/bin/env node
const fs = require('node:fs');
const {exibirAjudaComando} = require('./lib/cli-ajuda.cjs');

const PADROES_P1 = [
    /Service\.java$/,
    /Facade\.java$/,
    /Policy\.java$/,
    /Validator\.java$/,
    /Listener\.java$/,
    /Factory\.java$/,
    /Builder\.java$/,
    /Manager\.java$/,
    /Access.*\.java$/,
    /Sanitiz.*\.java$/,
    /Provider\.java$/,
    /Calculat.*\.java$/
];

const PADROES_P2 = [
    /Controller\.java$/,
    /Mapper\.java$/
];

const PADROES_IGNORADOS = [
    /Mock\.java$/,
    /Test\.java$/
];

const PADROES_ESTRUTURAIS = [
    /AccessPolicy\.java$/,
    /SanitizarHtml\.java$/,
    /Erro.*\.java$/
];

function parseArgs(argv) {
    const resultado = {
        input: 'unit-test-report.md',
        output: 'prioritized-tests.md'
    };

    for (let indice = 0; indice < argv.length; indice++) {
        const arg = argv[indice];
        if (arg === '--input') {
            resultado.input = argv[++indice];
        } else if (arg === '--output') {
            resultado.output = argv[++indice];
        } else if (arg === '--help' || arg === '-h') {
            imprimirAjuda();
            process.exit(0);
        }
    }

    return resultado;
}

function imprimirAjuda() {
    exibirAjudaComando({
        comandoSgc: 'testes priorizar',
        scriptDireto: 'testes-priorizar.cjs',
        descricao: 'Prioriza o backlog de testes a partir do relatorio estruturado ou Markdown.',
        opcoes: [
            '--input <arquivo>   Arquivo de entrada em JSON ou Markdown',
            '--output <arquivo>  Arquivo de saida em Markdown',
            '--help, -h          Exibe esta ajuda'
        ],
        exemplos: [
            'node backend/etc/scripts/sgc.cjs testes priorizar --input analise-testes.json --output priorizacao-testes.md'
        ]
    });
}

function classificarArquivo(caminhoArquivo) {
    if (PADROES_IGNORADOS.some(pattern => pattern.test(caminhoArquivo))) {
        return null;
    }
    if (PADROES_ESTRUTURAIS.some(pattern => pattern.test(caminhoArquivo))) {
        return 'P3';
    }
    if (PADROES_P1.some(pattern => pattern.test(caminhoArquivo))) {
        return 'P1';
    }
    if (PADROES_P2.some(pattern => pattern.test(caminhoArquivo))) {
        return 'P2';
    }
    return 'P3';
}

function extrairPendenciasDeJson(caminhoEntrada) {
    const dados = JSON.parse(fs.readFileSync(caminhoEntrada, 'utf-8'));
    const pendencias = [];
    Object.values(dados.categorias || {}).forEach(categoria => {
        (categoria.untested || []).forEach(item => pendencias.push(item.caminho_relativo));
    });
    return pendencias;
}

function extrairPendenciasDeMarkdown(caminhoEntrada) {
    const linhas = fs.readFileSync(caminhoEntrada, 'utf-8').split(/\r?\n/);
    return linhas
        .filter(linha => linha.trim().startsWith('- `'))
        .map(linha => linha.trim().replace('- `', '').replace(/`/g, ''));
}

function carregarPendencias(caminhoEntrada) {
    if (!fs.existsSync(caminhoEntrada)) {
        throw new Error(`Arquivo de entrada nao encontrado: ${caminhoEntrada}`);
    }
    return caminhoEntrada.endsWith('.json')
        ? extrairPendenciasDeJson(caminhoEntrada)
        : extrairPendenciasDeMarkdown(caminhoEntrada);
}

function priorizar(caminhoEntrada) {
    const pendencias = carregarPendencias(caminhoEntrada);
    const priorizadas = {P1: [], P2: [], P3: []};

    pendencias.forEach(caminhoArquivo => {
        const prioridade = classificarArquivo(caminhoArquivo);
        if (prioridade) {
            priorizadas[prioridade].push(caminhoArquivo);
        }
    });

    Object.keys(priorizadas).forEach(chave => priorizadas[chave].sort((a, b) => a.localeCompare(b, 'pt-BR')));
    return priorizadas;
}

function gerarMarkdown(priorizadas) {
    const linhas = [
        '# Plano de Priorizacao de Testes Unitarios\n',
        '## P1: Criticos (Logica de Negocio e Seguranca)\n',
        'Estas classes contem regras de negocio, validacoes, seguranca ou orquestracao complexa. A falta de testes aqui representa alto risco.\n'
    ];

    if (priorizadas.P1.length === 0) {
        linhas.push('Nenhuma pendencia critica de logica encontrada.\n');
    } else {
        priorizadas.P1.forEach(caminho => linhas.push(`- [ ] \`${caminho}\``));
    }

    linhas.push('\n## P2: Importantes (Integracao e Contratos)\n');
    linhas.push('Controladores e mappers. Importantes para garantir que a API respeite os contratos e que os dados sejam transformados corretamente.\n');
    if (priorizadas.P2.length === 0) {
        linhas.push('_Nenhum arquivo encontrado._');
    } else {
        priorizadas.P2.forEach(caminho => linhas.push(`- [ ] \`${caminho}\``));
    }

    linhas.push('\n## P3: Baixa Prioridade (Dados e Infraestrutura)\n');
    linhas.push('DTOs, modelos, repositorios e configuracoes. Geralmente cobertos por testes de integracao ou seguros por natureza.\n');
    if (priorizadas.P3.length === 0) {
        linhas.push('_Nenhum arquivo encontrado._');
    } else {
        priorizadas.P3.forEach(caminho => linhas.push(`- [ ] \`${caminho}\``));
    }

    return `${linhas.join('\n')}\n`;
}

function main() {
    const args = parseArgs(process.argv.slice(2));
    const priorizadas = priorizar(args.input);
    fs.writeFileSync(args.output, gerarMarkdown(priorizadas), 'utf-8');
    console.log(`Priorizacao concluida. Encontrados ${priorizadas.P1.length} P1, ${priorizadas.P2.length} P2, ${priorizadas.P3.length} P3.`);
    console.log(`Plano gerado em: ${args.output}`);
}

try {
    main();
} catch (error) {
    console.error(`Erro ao processar priorizacao: ${error.message}`);
    process.exit(1);
}
