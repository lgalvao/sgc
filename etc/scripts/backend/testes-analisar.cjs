#!/usr/bin/env node
const fs = require('node:fs');
const path = require('node:path');
const {exibirAjudaComando} = require('./lib/cli-ajuda.cjs');

const EXTENSAO_JAVA = '.java';
const CATEGORIAS_PRIORITARIAS = ['Controllers', 'Facades', 'Services', 'Mappers'];
const CATEGORIAS_SECUNDARIAS = ['Models', 'Repositories', 'DTOs', 'Others'];
const SUFIXOS_TESTE = ['Test', 'CoverageTest', 'UnitTest', 'IntegrationTest'];

function parseArgs(argv) {
    const resultado = {
        dir: 'backend',
        output: 'unit-test-report.md',
        outputJson: null
    };

    for (let indice = 0; indice < argv.length; indice++) {
        const arg = argv[indice];
        if (arg === '--dir') {
            resultado.dir = argv[++indice];
        } else if (arg === '--output') {
            resultado.output = argv[++indice];
        } else if (arg === '--output-json') {
            resultado.outputJson = argv[++indice];
        } else if (arg === '--help' || arg === '-h') {
            imprimirAjuda();
            process.exit(0);
        }
    }

    return resultado;
}

function imprimirAjuda() {
    exibirAjudaComando({
        comandoSgc: 'testes analisar',
        scriptDireto: 'testes-analisar.cjs',
        descricao: 'Analisa classes sem testes correspondentes e gera relatorios em Markdown e JSON.',
        opcoes: [
            '--dir <caminho>         Diretorio raiz do backend (padrao: backend)',
            '--output <arquivo>      Arquivo de saida em Markdown',
            '--output-json <arquivo> Arquivo de saida estruturado em JSON',
            '--help, -h              Exibe esta ajuda'
        ],
        exemplos: [
            'node etc/scripts/sgc.js backend testes analisar --dir backend --output analise-testes.md --output-json analise-testes.json'
        ]
    });
}

function normalizarCaminho(caminho) {
    return caminho.replaceAll('\\', '/');
}

function inferirCategoria(nomeClasse, caminhoRelativo) {
    const caminhoNormalizado = normalizarCaminho(caminhoRelativo);

    if (nomeClasse.includes('Controller')) return 'Controllers';
    if (nomeClasse.includes('Service') || nomeClasse.includes('Policy')) return 'Services';
    if (nomeClasse.includes('Facade')) return 'Facades';
    if (nomeClasse.includes('Mapper')) return 'Mappers';
    if (nomeClasse.includes('Dto') || nomeClasse.includes('Request') || nomeClasse.includes('Response')) return 'DTOs';
    if (nomeClasse.includes('Repo')) return 'Repositories';
    if (caminhoNormalizado.includes('/model/') || caminhoNormalizado.includes('/dominio/')) return 'Models';
    return 'Others';
}

function listarFontes(backendSrc) {
    const arquivos = [];

    function visitar(diretorio) {
        const entradas = fs.readdirSync(diretorio, {withFileTypes: true});
        entradas.forEach(entrada => {
            const caminhoCompleto = path.join(diretorio, entrada.name);
            if (entrada.isDirectory()) {
                visitar(caminhoCompleto);
                return;
            }

            if (!entrada.name.endsWith(EXTENSAO_JAVA) || entrada.name === 'package-info.java') {
                return;
            }

            const caminhoRelativo = normalizarCaminho(path.relative(backendSrc, caminhoCompleto));
            const nomeClasse = path.basename(caminhoRelativo, EXTENSAO_JAVA);
            const pacote = normalizarCaminho(path.dirname(caminhoRelativo));

            arquivos.push({
                caminho_relativo: caminhoRelativo,
                nome_classe: nomeClasse,
                pacote,
                categoria: inferirCategoria(nomeClasse, caminhoRelativo)
            });
        });
    }

    visitar(backendSrc);
    return arquivos;
}

function indexarTestes(backendTest) {
    const indicePorNome = new Map();
    const indicePorPacote = new Map();

    if (!fs.existsSync(backendTest)) {
        return {indicePorNome, indicePorPacote};
    }

    function visitar(diretorio) {
        const entradas = fs.readdirSync(diretorio, {withFileTypes: true});
        entradas.forEach(entrada => {
            const caminhoCompleto = path.join(diretorio, entrada.name);
            if (entrada.isDirectory()) {
                visitar(caminhoCompleto);
                return;
            }

            if (!entrada.name.endsWith(EXTENSAO_JAVA)) {
                return;
            }

            const caminhoRelativo = normalizarCaminho(path.relative(backendTest, caminhoCompleto));
            const pacote = normalizarCaminho(path.dirname(caminhoRelativo));

            if (!indicePorNome.has(entrada.name)) {
                indicePorNome.set(entrada.name, []);
            }
            indicePorNome.get(entrada.name).push(caminhoRelativo);

            const chavePacote = `${pacote}::${entrada.name}`;
            if (!indicePorPacote.has(chavePacote)) {
                indicePorPacote.set(chavePacote, []);
            }
            indicePorPacote.get(chavePacote).push(caminhoRelativo);
        });
    }

    visitar(backendTest);
    return {indicePorNome, indicePorPacote};
}

function localizarTestes(nomeClasse, pacote, indicePorNome, indicePorPacote) {
    const candidatos = SUFIXOS_TESTE.map(sufixo => `${nomeClasse}${sufixo}${EXTENSAO_JAVA}`);
    const encontradosMesmoPacote = [];

    candidatos.forEach(candidato => {
        const chavePacote = `${pacote}::${candidato}`;
        (indicePorPacote.get(chavePacote) || []).forEach(item => encontradosMesmoPacote.push(item));
    });

    if (encontradosMesmoPacote.length > 0) {
        return {
            caminhos: [...new Set(encontradosMesmoPacote)].sort(),
            estrategia: 'mesmo_pacote'
        };
    }

    const encontradosPorNome = [];
    candidatos.forEach(candidato => {
        (indicePorNome.get(candidato) || []).forEach(item => encontradosPorNome.push(item));
    });

    if (encontradosPorNome.length > 0) {
        return {
            caminhos: [...new Set(encontradosPorNome)].sort(),
            estrategia: 'nome_correspondente_outro_pacote'
        };
    }

    return {
        caminhos: [],
        estrategia: 'nenhum'
    };
}

function analisarTestes(backendDir = 'backend') {
    const backendSrc = path.join(backendDir, 'src/main/java');
    const backendTest = path.join(backendDir, 'src/test/java');

    if (!fs.existsSync(backendSrc)) {
        throw new Error(`Diretorio de origem nao encontrado: ${backendSrc}`);
    }

    const arquivosFonte = listarFontes(backendSrc);
    const {indicePorNome, indicePorPacote} = indexarTestes(backendTest);

    const relatorio = {
        Controllers: {tested: [], untested: []},
        Services: {tested: [], untested: []},
        Facades: {tested: [], untested: []},
        Mappers: {tested: [], untested: []},
        Models: {tested: [], untested: []},
        DTOs: {tested: [], untested: []},
        Repositories: {tested: [], untested: []},
        Others: {tested: [], untested: []}
    };

    let totalComTeste = 0;
    let correspondenciasAmbiguas = 0;

    arquivosFonte.forEach(arquivo => {
        const {caminhos, estrategia} = localizarTestes(
            arquivo.nome_classe,
            arquivo.pacote,
            indicePorNome,
            indicePorPacote
        );
        const possuiTeste = caminhos.length > 0;
        const item = {
            classe: arquivo.nome_classe,
            caminho_relativo: arquivo.caminho_relativo,
            categoria: arquivo.categoria,
            possui_teste: possuiTeste,
            estrategia_correspondencia: estrategia,
            testes_encontrados: caminhos
        };

        if (possuiTeste) {
            totalComTeste++;
            relatorio[arquivo.categoria].tested.push(item);
            if (estrategia === 'nome_correspondente_outro_pacote') {
                correspondenciasAmbiguas++;
            }
        } else {
            relatorio[arquivo.categoria].untested.push(item);
        }
    });

    const totalClasses = arquivosFonte.length;
    const cobertura = totalClasses > 0 ? (totalComTeste / totalClasses) * 100 : 0;

    return {
        gerado_em: new Date().toISOString(),
        backend_dir: backendDir,
        estatisticas: {
            total_classes: totalClasses,
            classes_com_teste: totalComTeste,
            classes_sem_teste: totalClasses - totalComTeste,
            cobertura_arquivos_percentual: Number(cobertura.toFixed(2)),
            correspondencias_ambiguas: correspondenciasAmbiguas
        },
        categorias: relatorio
    };
}

function gerarMarkdown(dados) {
    const estatisticas = dados.estatisticas;
    const dataFormatada = new Date(dados.gerado_em).toLocaleString('pt-BR');
    const linhas = [
        '# Relatorio de Cobertura de Testes Unitarios (Backend)\n',
        `**Data:** ${dataFormatada}`,
        `**Total de Classes:** ${estatisticas.total_classes}`,
        `**Com Testes Unitarios:** ${estatisticas.classes_com_teste}`,
        `**Sem Testes Unitarios:** ${estatisticas.classes_sem_teste}`,
        `**Cobertura (Arquivos):** ${estatisticas.cobertura_arquivos_percentual.toFixed(2)}%`
    ];

    if (estatisticas.correspondencias_ambiguas > 0) {
        linhas.push(`**Aviso:** ${estatisticas.correspondencias_ambiguas} classe(s) foram marcadas como cobertas apenas por nome de teste em outro pacote.`);
    }

    linhas.push('\n## Detalhamento por Categoria\n');

    [...CATEGORIAS_PRIORITARIAS, ...CATEGORIAS_SECUNDARIAS].forEach(categoria => {
        const itens = dados.categorias[categoria];
        const total = itens.tested.length + itens.untested.length;
        if (total === 0) {
            return;
        }

        linhas.push(`### ${categoria} (${itens.tested.length}/${total} testados)`);
        if (itens.untested.length > 0) {
            linhas.push(`**Faltando Testes (${itens.untested.length}):**`);
            itens.untested
                .slice()
                .sort((a, b) => a.caminho_relativo.localeCompare(b.caminho_relativo, 'pt-BR'))
                .forEach(item => linhas.push(`- \`${item.caminho_relativo}\``));
        } else {
            linhas.push('Todos cobertos.');
        }
        linhas.push('');
    });

    return `${linhas.join('\n')}\n`;
}

function gravarArquivo(caminho, conteudo) {
    fs.writeFileSync(caminho, conteudo, 'utf-8');
}

function main() {
    const args = parseArgs(process.argv.slice(2));
    const dados = analisarTestes(args.dir);
    gravarArquivo(args.output, gerarMarkdown(dados));
    if (args.outputJson) {
        gravarArquivo(args.outputJson, JSON.stringify(dados, null, 2));
    }

    console.log(`Relatorio Markdown gerado em: ${args.output}`);
    if (args.outputJson) {
        console.log(`Relatorio JSON gerado em: ${args.outputJson}`);
    }
}

try {
    main();
} catch (error) {
    console.error(`Erro ao analisar testes: ${error.message}`);
    process.exit(1);
}
