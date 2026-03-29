#!/usr/bin/env node
const fs = require('node:fs');
const path = require('node:path');
const {exibirAjudaComando} = require('./lib/cli-ajuda.cjs');
const {coletarArquivosCobertura, lerRelatorioJacoco} = require('./lib/cobertura-base.cjs');
const {
    EXTENSAO_JAVA,
    CATEGORIAS_PRIORITARIAS,
    CATEGORIAS_SECUNDARIAS,
    SUFIXOS_TESTE,
    normalizarCaminho,
    inferirCategoria,
    lerConteudoFonte,
    classificarPerfilDto,
    construirNomeClasseCompleto,
    criarItemRelatorio
} = require('./lib/testes-analisar-regras.cjs');

function parseArgs(argv) {
    const resultado = {
        dir: 'backend',
        output: 'unit-test-report.md',
        outputJson: null,
        jacocoXml: null
    };

    for (let indice = 0; indice < argv.length; indice++) {
        const arg = argv[indice];
        if (arg === '--dir') {
            resultado.dir = argv[++indice];
        } else if (arg === '--output') {
            resultado.output = argv[++indice];
        } else if (arg === '--output-json') {
            resultado.outputJson = argv[++indice];
        } else if (arg === '--jacoco-xml') {
            resultado.jacocoXml = argv[++indice];
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
        descricao: 'Analisa classes sem testes correspondentes e gera relatorios em Markdown e JSON com resumo por categoria.',
        opcoes: [
            '--dir <caminho>         Diretorio raiz do backend (padrao: backend)',
            '--output <arquivo>      Arquivo de saida em Markdown',
            '--output-json <arquivo> Arquivo de saida estruturado em JSON (padrao: sidecar do Markdown)',
            '--jacoco-xml <arquivo>  Relatorio XML do JaCoCo para classificar cobertura indireta',
            '--help, -h              Exibe esta ajuda'
        ],
        exemplos: [
            'node etc/scripts/sgc.js backend testes analisar --dir backend --output analise-testes.md --output-json analise-testes.json'
        ]
    });
}

function resolverSaidaJsonPadrao(caminhoMarkdown) {
    if (caminhoMarkdown.toLowerCase().endsWith('.md')) {
        return caminhoMarkdown.replace(/\.md$/i, '.json');
    }
    return `${caminhoMarkdown}.json`;
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

async function carregarCoberturaPorClasse(caminhoJacocoXml = null) {
    try {
        const relatorio = await lerRelatorioJacoco(caminhoJacocoXml || undefined);
        const coleta = coletarArquivosCobertura(relatorio, {
            incluirSemLacunas: true,
            aplicarExclusoes: false
        });

        return new Map(
            coleta.arquivos.map(arquivo => [arquivo.nomeClasse, arquivo])
        );
    } catch (error) {
        if (String(error.message || '').includes('Relatório JaCoCo não encontrado')) {
            return new Map();
        }
        throw error;
    }
}

async function analisarTestes(backendDir = 'backend', caminhoJacocoXml = null) {
    const backendSrc = path.join(backendDir, 'src/main/java');
    const backendTest = path.join(backendDir, 'src/test/java');

    if (!fs.existsSync(backendSrc)) {
        throw new Error(`Diretorio de origem nao encontrado: ${backendSrc}`);
    }

    const arquivosFonte = listarFontes(backendSrc);
    const {indicePorNome, indicePorPacote} = indexarTestes(backendTest);
    const coberturaPorClasse = await carregarCoberturaPorClasse(caminhoJacocoXml);

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
    let totalComCoberturaIndireta = 0;
    let totalSemEvidenciaNoEscopo = 0;
    let totalForaEscopoJacoco = 0;
    let totalRuidoIgnorado = 0;
    let totalDtosComportamentais = 0;
    let totalDtosEstruturais = 0;
    let totalDtosEstruturaisContratuais = 0;

    arquivosFonte.forEach(arquivo => {
        const conteudoFonte = lerConteudoFonte(backendSrc, arquivo.caminho_relativo);
        const perfilDto = arquivo.categoria === 'DTOs' ? classificarPerfilDto(conteudoFonte) : null;
        const dtoEstrutural = perfilDto === 'estrutural_puro' || perfilDto === 'estrutural_contrato';
        const {caminhos, estrategia} = localizarTestes(
            arquivo.nome_classe,
            arquivo.pacote,
            indicePorNome,
            indicePorPacote
        );
        const possuiTeste = caminhos.length > 0;
        const nomeClasseCompleto = construirNomeClasseCompleto(arquivo.caminho_relativo);
        const coberturaClasse = coberturaPorClasse.get(nomeClasseCompleto) || null;
        const estaNoEscopoJacoco = coberturaPorClasse.size === 0 || coberturaClasse !== null;
        const possuiCoberturaJacoco = coberturaClasse !== null && coberturaClasse.linhasCobertas > 0;
        const possuiCoberturaSomenteIndireta = !possuiTeste && possuiCoberturaJacoco;
        const estaForaEscopoJacoco = !possuiTeste && coberturaPorClasse.size > 0 && !estaNoEscopoJacoco;
        const item = criarItemRelatorio({
            arquivo,
            perfilDto,
            dtoEstrutural,
            possuiTeste,
            estaNoEscopoJacoco,
            possuiCoberturaJacoco,
            possuiCoberturaSomenteIndireta,
            estaForaEscopoJacoco,
            estrategia,
            caminhos,
            coberturaClasse
        });

        if (arquivo.categoria === 'DTOs') {
            if (perfilDto === 'comportamental') {
                totalDtosComportamentais++;
            } else {
                totalDtosEstruturais++;
                if (perfilDto === 'estrutural_contrato') {
                    totalDtosEstruturaisContratuais++;
                }
            }
        }

        if (possuiTeste) {
            totalComTeste++;
            relatorio[arquivo.categoria].tested.push(item);
            if (estrategia === 'nome_correspondente_outro_pacote') {
                correspondenciasAmbiguas++;
            }
        } else {
            relatorio[arquivo.categoria].untested.push(item);
            if (dtoEstrutural) {
                totalRuidoIgnorado++;
            } else if (estaForaEscopoJacoco) {
                totalForaEscopoJacoco++;
            } else if (possuiCoberturaSomenteIndireta) {
                totalComCoberturaIndireta++;
            } else {
                totalSemEvidenciaNoEscopo++;
            }
        }
    });

    const totalClasses = arquivosFonte.length;
    const cobertura = totalClasses > 0 ? (totalComTeste / totalClasses) * 100 : 0;
    const totalBacklogReal = totalClasses - totalRuidoIgnorado;
    const coberturaBacklogReal = totalBacklogReal > 0
        ? (totalComTeste / totalBacklogReal) * 100
        : 0;
    const coberturaObservada = totalClasses > 0
        ? ((totalComTeste + totalComCoberturaIndireta) / totalClasses) * 100
        : 0;

    return {
        gerado_em: new Date().toISOString(),
        backend_dir: backendDir,
        estatisticas: {
            total_classes: totalClasses,
            classes_com_teste_dedicado: totalComTeste,
            classes_com_cobertura_indireta: totalComCoberturaIndireta,
            classes_sem_evidencia_no_escopo: totalSemEvidenciaNoEscopo,
            classes_fora_escopo_jacoco: totalForaEscopoJacoco,
            classes_ruido_ignorado: totalRuidoIgnorado,
            classes_sem_teste_dedicado: totalClasses - totalComTeste,
            cobertura_arquivos_percentual: Number(cobertura.toFixed(2)),
            cobertura_backlog_real_percentual: Number(coberturaBacklogReal.toFixed(2)),
            cobertura_observada_percentual: Number(coberturaObservada.toFixed(2)),
            correspondencias_ambiguas: correspondenciasAmbiguas,
            jacoco_disponivel: coberturaPorClasse.size > 0,
            dtos_comportamentais: totalDtosComportamentais,
            dtos_estruturais: totalDtosEstruturais,
            dtos_estruturais_contratuais: totalDtosEstruturaisContratuais
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
        `**Com Teste Dedicado:** ${estatisticas.classes_com_teste_dedicado}`,
        `**Com Cobertura Indireta:** ${estatisticas.classes_com_cobertura_indireta}`,
        `**Sem Evidencia no Escopo do JaCoCo:** ${estatisticas.classes_sem_evidencia_no_escopo}`,
        `**Fora do Escopo do JaCoCo:** ${estatisticas.classes_fora_escopo_jacoco}`,
        `**Ruido Ignorado no Backlog:** ${estatisticas.classes_ruido_ignorado}`,
        `**Sem Teste Dedicado:** ${estatisticas.classes_sem_teste_dedicado}`,
        `**Correspondencia Direta (Arquivos):** ${estatisticas.cobertura_arquivos_percentual.toFixed(2)}%`,
        `**Correspondencia Direta no Backlog Real:** ${estatisticas.cobertura_backlog_real_percentual.toFixed(2)}%`,
        `**Cobertura Observada (Teste Dedicado + Indireta):** ${estatisticas.cobertura_observada_percentual.toFixed(2)}%`
    ];

    if (estatisticas.correspondencias_ambiguas > 0) {
        linhas.push(`**Aviso:** ${estatisticas.correspondencias_ambiguas} classe(s) foram marcadas como cobertas apenas por nome de teste em outro pacote.`);
    }

    if (!estatisticas.jacoco_disponivel) {
        linhas.push('**Aviso:** relatório JaCoCo indisponível; a coluna de cobertura indireta não foi calculada.');
    }

    linhas.push('\n## Detalhamento por Categoria\n');

    [...CATEGORIAS_PRIORITARIAS, ...CATEGORIAS_SECUNDARIAS].forEach(categoria => {
        const itens = dados.categorias[categoria];
        const total = itens.tested.length + itens.untested.length;
        if (total === 0) {
            return;
        }

        const totalRelevanteCategoria = categoria === 'DTOs'
            ? itens.tested.length + itens.untested.filter(item => !item.dto_ruido_ignorado).length
            : total;
        linhas.push(`### ${categoria} (${itens.tested.length}/${totalRelevanteCategoria} testados${categoria === 'DTOs' ? ' no backlog real' : ''})`);
        if (itens.untested.length > 0) {
            const candidatos = categoria === 'DTOs'
                ? itens.untested.filter(item => !item.dto_ruido_ignorado)
                : itens.untested;
            const dtoRuido = categoria === 'DTOs'
                ? itens.untested.filter(item => item.dto_ruido_ignorado)
                : [];
            const indiretos = candidatos.filter(item => item.coberta_somente_indiretamente);
            const foraEscopo = candidatos.filter(item => item.fora_escopo_jacoco);
            const semEvidencia = candidatos.filter(item => !item.coberta_somente_indiretamente && !item.fora_escopo_jacoco);

            linhas.push(`**Faltando Testes Dedicados (${candidatos.length}):**`);
            if (indiretos.length > 0) {
                linhas.push(`Cobertos apenas indiretamente (${indiretos.length}):`);
                indiretos
                    .slice()
                    .sort((a, b) => a.caminho_relativo.localeCompare(b.caminho_relativo, 'pt-BR'))
                    .forEach(item => linhas.push(`- \`${item.caminho_relativo}\` (${item.cobertura?.cobertura_linhas_percentual.toFixed(2)}% linhas)`));
            }
            if (foraEscopo.length > 0) {
                linhas.push(`Fora do escopo do JaCoCo (${foraEscopo.length}):`);
                foraEscopo
                    .slice()
                    .sort((a, b) => a.caminho_relativo.localeCompare(b.caminho_relativo, 'pt-BR'))
                    .forEach(item => linhas.push(`- \`${item.caminho_relativo}\``));
            }
            if (semEvidencia.length > 0) {
                linhas.push(`Sem evidencia de cobertura no escopo (${semEvidencia.length}):`);
                semEvidencia
                    .slice()
                    .sort((a, b) => a.caminho_relativo.localeCompare(b.caminho_relativo, 'pt-BR'))
                    .forEach(item => linhas.push(`- \`${item.caminho_relativo}\``));
            }
            if (dtoRuido.length > 0) {
                linhas.push(`Ignorados como DTO estrutural/contratual (${dtoRuido.length}):`);
                dtoRuido
                    .slice()
                    .sort((a, b) => a.caminho_relativo.localeCompare(b.caminho_relativo, 'pt-BR'))
                    .forEach(item => linhas.push(`- \`${item.caminho_relativo}\` (${item.perfil_dto})`));
            }
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

function imprimirResumoConsole(dados) {
    const {estatisticas, categorias} = dados;
    console.log(`Resumo: ${estatisticas.classes_com_teste_dedicado}/${estatisticas.total_classes} classes com teste dedicado (${estatisticas.cobertura_arquivos_percentual.toFixed(2)}%).`);
    console.log(`- Cobertura indireta: ${estatisticas.classes_com_cobertura_indireta}`);
    console.log(`- Sem evidencia no escopo: ${estatisticas.classes_sem_evidencia_no_escopo}`);
    console.log(`- Fora do escopo do JaCoCo: ${estatisticas.classes_fora_escopo_jacoco}`);
    console.log(`- Ruido ignorado no backlog: ${estatisticas.classes_ruido_ignorado}`);
    console.log(`- Backlog real coberto por teste dedicado: ${estatisticas.cobertura_backlog_real_percentual.toFixed(2)}%`);
    if (estatisticas.jacoco_disponivel) {
        console.log(`- Cobertura observada: ${estatisticas.cobertura_observada_percentual.toFixed(2)}%`);
    } else {
        console.log('- Cobertura observada: indisponivel (JaCoCo ausente)');
    }

    [...CATEGORIAS_PRIORITARIAS, ...CATEGORIAS_SECUNDARIAS].forEach(categoria => {
        const itens = categorias[categoria];
        const total = itens.tested.length + itens.untested.length;
        if (total === 0) {
            return;
        }
        if (categoria === 'DTOs') {
            const totalRelevante = itens.tested.length + itens.untested.filter(item => !item.dto_ruido_ignorado).length;
            const ignorados = itens.untested.filter(item => item.dto_ruido_ignorado).length;
            console.log(`- ${categoria}: ${itens.tested.length}/${totalRelevante} testados no backlog real (${ignorados} ignorados)`);
            return;
        }
        console.log(`- ${categoria}: ${itens.tested.length}/${total} testados`);
    });

    if (estatisticas.correspondencias_ambiguas > 0) {
        console.log(`- Correspondencias ambiguas: ${estatisticas.correspondencias_ambiguas}`);
    }
}

async function main() {
    const args = parseArgs(process.argv.slice(2));
    const outputJson = args.outputJson ?? resolverSaidaJsonPadrao(args.output);
    const dados = await analisarTestes(args.dir, args.jacocoXml);
    gravarArquivo(args.output, gerarMarkdown(dados));
    gravarArquivo(outputJson, JSON.stringify(dados, null, 2));

    imprimirResumoConsole(dados);
    console.log(`Relatorio Markdown gerado em: ${args.output}`);
    console.log(`Relatorio JSON gerado em: ${outputJson}`);
}

main().catch(error => {
    console.error(`Erro ao analisar testes: ${error.message}`);
    process.exit(1);
});
