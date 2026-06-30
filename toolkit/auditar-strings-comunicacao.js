#!/usr/bin/env node

import fs from 'node:fs';
import path from 'node:path';
import {globby} from 'globby';

const raiz = process.cwd();
const mapaMensagens = carregarMapaMensagens(path.join(raiz, 'backend/src/main/java/sgc/comum/Mensagens.java'));
const mapaTextosAlerta = carregarMapaTextosAlerta(path.join(raiz, 'frontend/src/constants/textos.ts'));
const arquivos = [
    path.join(raiz, 'backend/src/main/java/sgc/subprocesso/service/SubprocessoNotificacaoService.java'),
    path.join(raiz, 'backend/src/main/java/sgc/comum/Mensagens.java'),
    path.join(raiz, 'backend/src/main/java/sgc/alerta/AssuntosNotificacao.java'),
    path.join(raiz, 'backend/src/main/java/sgc/alerta/EmailModelosService.java'),
    path.join(raiz, 'backend/src/main/java/sgc/processo/service/ProcessoService.java'),
    path.join(raiz, 'backend/src/main/java/sgc/organizacao/service/ResponsavelUnidadeService.java'),
    ...(await globby(path.join(raiz, 'backend/src/test/java/sgc/integracao/**/*').replace(/\\/g, '/'), {absolute: true}))
        .filter(c => /(CDU\d+IntegrationTest\.java|EmailModelosRenderIntegrationTest\.java)$/.test(c)),
    path.join(raiz, 'backend/src/test/java/sgc/alerta/notificacao/EmailModelosServiceTest.java'),
    ...(await globby(path.join(raiz, 'e2e/**/*').replace(/\\/g, '/'), {absolute: true}))
        .filter(c => /cdu-\d+\.spec\.ts$/.test(c)),
    ...(await globby(path.join(raiz, 'etc/reqs/**/*').replace(/\\/g, '/'), {absolute: true}))
        .filter(c => /^cdu-\d+\.md$/.test(c))
].filter(caminho => !caminho.includes(`${path.sep}etc${path.sep}reqs${path.sep}diagnostico${path.sep}`));

const entradas = [
    ...arquivos.flatMap(extrairEntradasArquivo),
    ...criarEntradasSinteticasAssuntosCatalogo()
];
const grupos = agruparPorFamilia(entradas);
const suspeitas = grupos.filter(temSuspeita);

if (process.argv.includes('--json')) {
    console.log(JSON.stringify((process.argv.includes('--suspeitas') ? suspeitas : grupos), null, 2));
    process.exit(0);
}

const filtroFamilia = argumentoOpcional('--familia');
const saida = (process.argv.includes('--suspeitas') ? suspeitas : grupos)
    .filter(grupo => !filtroFamilia || grupo.familia.includes(normalizar(filtroFamilia)));

console.log(`familias: ${grupos.length}`);
console.log(`suspeitas: ${suspeitas.length}`);

for (const grupo of saida) {
    console.log(`\n${grupo.titulo}`);
    console.log(`  familia: ${grupo.familia}`);
    console.log(`  resumo: ${grupo.resumo}`);
    console.log(`  origens: ${grupo.origens.join('; ')}`);
    if (grupo.variantes.length > 1) {
        console.log(`  variantes: ${grupo.variantes.map(variant => `"${variant}"`).join(' | ')}`);
    } else {
        console.log(`  variante: "${grupo.variantes[0]}"`);
    }
    if (grupo.suspeitas.length > 0) {
        console.log(`  suspeitas: ${grupo.suspeitas.join('; ')}`);
    }
}


function extrairEntradasArquivo(caminho) {
    const texto = fs.readFileSync(caminho, 'utf8');
    const relativas = path.relative(raiz, caminho);
    const linhas = texto.split('\n');
    const entradasExtraidas = [];

    linhas.forEach((linha, indice) => {
        const numeroLinha = indice + 1;
        const textoRequisito = extrairAssuntoRequisito(linha);
        if (textoRequisito) {
            entradasExtraidas.push(criarEntrada(relativas, numeroLinha, 'requisito', 'assunto', textoRequisito));
        }

        if (relativas.endsWith('Mensagens.java')) {
            const constante = extrairConstanteMensagens(linha);
            if (constante) {
                entradasExtraidas.push(criarEntrada(relativas, numeroLinha, 'producao', constante.tipo, constante.valor, constante.nome));
            }
        }

        for (const nomeConstante of extrairReferenciasMensagens(linha)) {
            const constante = mapaMensagens.get(nomeConstante);
            if (!constante) continue;
            entradasExtraidas.push(criarEntrada(relativas, numeroLinha, tipoOrigem(relativas), constante.tipo, constante.valor, nomeConstante));
        }

        for (const nomeConstante of extrairReferenciasTextosAlerta(linha)) {
            const valor = mapaTextosAlerta.get(nomeConstante);
            if (!valor) continue;
            entradasExtraidas.push(criarEntrada(relativas, numeroLinha, tipoOrigem(relativas), 'alerta', valor, `TEXTOS.alerta.${nomeConstante}`));
        }

        for (const literal of extrairLiteraisLinha(linha)) {
            if (!pareceComunicacao(literal)) continue;
            entradasExtraidas.push(criarEntrada(relativas, numeroLinha, tipoOrigem(relativas), 'literal', literal));
        }
    });

    return deduplicarEntradasMesmaLinha(entradasExtraidas);
}

function extrairAssuntoRequisito(linha) {
    const match = linha.match(/^\s*Assunto:\s*(.+)$/);
    return match?.[1]?.trim() || null;
}

function extrairConstanteMensagens(linha) {
    const match = linha.match(/public static final String\s+((?:ALERTA|HIST|ASSUNTO)_[A-Z0-9_]+)\s*=\s*"([^"]+)"/);
    if (!match) return null;
    const nome = match[1];
    const valor = match[2].trim();
    const tipo = nome.startsWith('ALERTA_') ? 'alerta' : nome.startsWith('HIST_') ? 'historico' : 'assunto';
    return {nome, valor, tipo};
}

function extrairReferenciasMensagens(linha) {
    return [...linha.matchAll(/Mensagens\.((?:ALERTA|HIST|ASSUNTO)_[A-Z0-9_]+)/g)].map(match => match[1]);
}

function extrairReferenciasTextosAlerta(linha) {
    return [...linha.matchAll(/TEXTOS\.alerta\.([A-Z0-9_]+)/g)].map(match => match[1]);
}

function extrairLiteraisLinha(linha) {
    const literais = [
        ...linha.matchAll(/"([^"\n]{6,})"/g),
        ...linha.matchAll(/'([^'\n]{6,})'/g),
        ...linha.matchAll(/`([^`\n]{6,})`/g)
    ].map(match => match[1].trim());
    return literais.filter(Boolean);
}

function pareceComunicacao(valor) {
    if (valor.length < 12) return false;
    if (/^[A-Z0-9_ -]+$/.test(valor) && !valor.includes('SGC:')) return false;
    return /(SGC:|cadastro.+(analise|ajustes|reabert|homolog|disponibil)|revis(?:[aã]o|[õo]es).+(analise|ajustes|reabert|disponibil)|mapa.+(analise|ajustes|homolog|disponibil|sugest|valida)|lembrete de prazo|finaliza[cç][aã]o do processo|in[ií]cio de processo|atribui[cç][aã]o de perfil|submetid[oa]s? para an[aá]lise|devolvid[oa]s? para ajustes|reabert[oa]s? para ajustes)/i.test(valor);
}


function criarEntrada(arquivo, linha, origem, tipo, valor, chave = null) {
    return {
        arquivo,
        linha,
        origem,
        tipo,
        valor,
        chave,
        familia: normalizarFamilia(valor)
    };
}

function deduplicarEntradasMesmaLinha(listaEntradas) {
    const vistos = new Set();
    return listaEntradas.filter(entrada => {
        const chave = `${entrada.arquivo}:${entrada.linha}:${entrada.tipo}:${entrada.valor}`;
        if (vistos.has(chave)) return false;
        vistos.add(chave);
        return true;
    });
}

function agruparPorFamilia(listaEntradas) {
    const mapa = new Map();
    for (const entrada of listaEntradas) {
        if (!entrada.familia) continue;
        if (!mapa.has(entrada.familia)) {
            mapa.set(entrada.familia, []);
        }
        mapa.get(entrada.familia).push(entrada);
    }

    return [...mapa.entries()]
        .map(([familia, itens]) => resumirGrupo(familia, itens))
        .sort((a, b) => b.suspeitas.length - a.suspeitas.length || a.titulo.localeCompare(b.titulo, 'pt-BR'));
}

function resumirGrupo(familia, itens) {
    const variantes = [...new Set(itens.map(item => item.valor))].sort();
    const arquivosProducao = new Set(itens.filter(item => item.origem === 'producao').map(item => item.arquivo));
    const arquivosTeste = new Set(itens.filter(item => item.origem === 'teste').map(item => item.arquivo));
    const arquivosRequisito = new Set(itens.filter(item => item.origem === 'requisito').map(item => item.arquivo));
    const tipos = [...new Set(itens.map(item => item.tipo))].sort();
    const listaSuspeitas = [];

    if (variantes.length > 1 && arquivosProducao.size > 1) {
        listaSuspeitas.push('variantes em producao');
    }
    if (arquivosProducao.size > 1) {
        listaSuspeitas.push('duplicada em multiplos arquivos de producao');
    }
    if (arquivosProducao.size > 0 && arquivosRequisito.size === 0 && tipos.includes('assunto')) {
        listaSuspeitas.push('assunto sem lastro explicito in requisito');
    }
    if (arquivosProducao.size > 0 && arquivosTeste.size === 0 && (tipos.includes('assunto') || tipos.includes('alerta'))) {
        listaSuspeitas.push('sem cobertura textual em teste');
    }
    if (arquivosRequisito.size > 0 && arquivosProducao.size === 0 && tipos.includes('assunto')) {
        listaSuspeitas.push('assunto so em requisito');
    }

    const amostra = itens[0];
    const titulo = amostra.chave ?? variantes[0];
    const resumo = `producao=${arquivosProducao.size} teste=${arquivosTeste.size} requisito=${arquivosRequisito.size} tipos=${tipos.join(',')}`;
    const origens = itens
        .slice()
        .sort((a, b) => a.arquivo.localeCompare(b.arquivo, 'pt-BR') || a.linha - b.linha)
        .map(item => `${item.origem}:${item.arquivo}:${item.linha}`);

    return {
        titulo,
        familia,
        resumo,
        variantes,
        origens,
        suspeitas: listaSuspeitas
    };
}

function temSuspeita(grupo) {
    const temRelevanciaForaTeste = grupo.origens.some(origem => !origem.startsWith('teste:'));
    return grupo.suspeitas.length > 0 && temRelevanciaForaTeste;
}

function normalizarFamilia(valor) {
    return normalizar(
        valor
            .replace(/^Assunto:\s*/i, '')
            .replace(/^SGC:\s*/i, '')
            .replace(/<[^>]+>/g, ' ')
            .replace(/%[sd]/g, '<var>')
            .replace(/\[[^\]]+\]/g, '<var>')
            .replace(/\$\{[^}]+\}/g, '<var>')
            .replace(/\b[A-ZÁÉÍÓÚÇ][A-ZÁÉÍÓÚÇ0-9_-]{1,}\b/g, '<var>')
            .replace(/\b\d{2}\/\d{2}\/\d{4}\b/g, '<data>')
            .replace(/\b\d+\b/g, '<num>')
    );
}

function normalizar(valor) {
    return valor
        .normalize('NFD')
        .replace(/\p{Diacritic}/gu, '')
        .toLowerCase()
        .replace(/\s+/g, ' ')
        .trim();
}

function tipoOrigem(relativo) {
    if (relativo.startsWith('backend/src/main/java/')) return 'producao';
    if (relativo.startsWith('backend/src/test/java/') || relativo.startsWith('e2e/')) return 'teste';
    if (relativo.startsWith('etc/reqs/')) return 'requisito';
    return 'outro';
}

function argumentoOpcional(flag) {
    const indice = process.argv.indexOf(flag);
    if (indice === -1) return null;
    return process.argv[indice + 1] ?? null;
}

function criarEntradasSinteticasAssuntosCatalogo() {
    const arquivo = 'backend/src/main/java/sgc/alerta/AssuntosNotificacao.java';
    const entradasSinteticas = [];
    const assuntos = [
        'SGC: Início de processo de mapeamento de competências',
        'SGC: Início de processo de mapeamento de competências em unidades subordinadas',
        'SGC: Início de processo de revisão do mapa de competências',
        'SGC: Início de processo de revisão do mapa de competências em unidades subordinadas',
        'SGC: Finalização do processo %s',
        'SGC: Finalização do processo %s em unidades subordinadas',
        'SGC: Lembrete de prazo - %s',
        'SGC: Data limite alterada',
        'SGC: Atribuição de perfil CHEFE na unidade %s',
        'SGC: Cadastro de atividades e conhecimentos da %s submetido para análise',
        'SGC: Cadastro de atividades e conhecimentos da %s devolvido para ajustes',
        'SGC: Cadastro de atividades e conhecimentos disponibilizado - %s',
        'SGC: Reabertura de cadastro de atividades - %s',
        'SGC: Mapa de competências homologado',
        'SGC: Mapa de competências disponibilizado',
        'SGC: Mapa de competências disponibilizado - %s',
        'SGC: Sugestões apresentadas para o mapa de competências da %s',
        'SGC: Validação do mapa de competências da %s submetida para análise',
        'SGC: Validação do mapa da %s devolvida para ajustes',
        'SGC: Revisão do cadastro de atividades e conhecimentos da %s submetido para análise',
        'SGC: Revisão do cadastro de atividades e conhecimentos da %s devolvida para ajustes',
        'SGC: Revisão do cadastro de atividades e conhecimentos disponibilizada: %s',
        'SGC: Reabertura de revisão de cadastro - %s',
        'SGC: Cadastros de atividades e conhecimentos submetidos para análise',
        'SGC: Revisões de cadastro de atividades e conhecimentos submetidas para análise',
        'SGC: Mapas de competências disponibilizados',
        'SGC: Validação de mapas de competências submetida para análise'
    ];

    assuntos.forEach((assunto, indice) => {
        entradasSinteticas.push(criarEntrada(arquivo, indice + 1, 'producao', 'assunto', assunto, 'CATALOGO_ASSUNTO'));
    });
    return entradasSinteticas;
}

function carregarMapaMensagens(caminho) {
    const mapa = new Map();
    if (!fs.existsSync(caminho)) return mapa;
    const texto = fs.readFileSync(caminho, 'utf8');
    for (const linha of texto.split('\n')) {
        const constante = extrairConstanteMensagens(linha);
        if (constante) mapa.set(constante.nome, constante);
    }
    return mapa;
}

function carregarMapaTextosAlerta(caminho) {
    const mapa = new Map();
    if (!fs.existsSync(caminho)) return mapa;
    const texto = fs.readFileSync(caminho, 'utf8');
    const bloco = texto.match(/alerta:\s*\{([\s\S]*?)\n\s*},\n\s*[a-zA-Z]/);
    if (!bloco) return mapa;
    for (const linha of bloco[1].split('\n')) {
        const func = linha.match(/^\s*([A-Z0-9_]+):\s*\([^)]*\)\s*=>\s*`([^`]+)`/);
        if (func) {
            mapa.set(func[1], func[2]);
            continue;
        }
        const literal = linha.match(/^\s*([A-Z0-9_]+):\s*"([^"]+)"/);
        if (literal) {
            mapa.set(literal[1], literal[2]);
        }
    }
    return mapa;
}
