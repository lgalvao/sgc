#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha} from "../lib/saida.js";

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

function lerArgumentos(argumentos) {
    const resultado = {
        entrada: lerOpcao(argumentos, "--input", "unit-test-report.md"),
        entradaExplicita: argumentos.includes("--input") || argumentos.some((argumento) => argumento.startsWith("--input=")),
        saida: lerOpcao(argumentos, "--output", "prioritized-tests.md"),
        ajuda: argumentos.includes("--help") || argumentos.includes("-h"),
    };
    return resultado;
}

function imprimirAjuda() {
    exibirAjudaComando({
        comandoSgc: "backend testes priorizar",
        scriptDireto: "backend/testes-priorizar.js",
        descricao: 'Prioriza o backlog de testes a partir do relatorio estruturado ou Markdown.',
        opcoes: [
            '--input <arquivo>   Arquivo de entrada em JSON ou Markdown',
            '--output <arquivo>  Arquivo de saida em Markdown',
            '--help, -h          Exibe esta ajuda'
        ],
        exemplos: [
            "npx tsx toolkit/sgc.js backend testes priorizar --input analise-testes.json --output priorizacao-testes.md"
        ]
    });
}

function resolverEntradaPadrao(caminhoMarkdown) {
    const diretorio = path.dirname(caminhoMarkdown);
    const nomeArquivo = `${path.parse(caminhoMarkdown).name}.json`;
    const jsonSidecar = diretorio === '.' ? nomeArquivo : path.join(diretorio, nomeArquivo);

    if (fs.existsSync(jsonSidecar)) {
        return jsonSidecar;
    }

    return caminhoMarkdown;
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
        (categoria.untested || []).forEach(item => {
            const ignorado = item.dto_ruido_ignorado || item.model_ruido_ignorado || item.other_ruido_ignorado;
            if (ignorado || item.evidencia_qualidade === 'fora_escopo_jacoco') {
                return;
            }
            pendencias.push({
                caminho_relativo: item.caminho_relativo,
                evidencia_qualidade: item.evidencia_qualidade
            });
        });
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
    const pendencias = caminhoEntrada.endsWith('.json')
        ? extrairPendenciasDeJson(caminhoEntrada)
        : extrairPendenciasDeMarkdown(caminhoEntrada);
    return pendencias.map(item => typeof item === 'string' ? {
        caminho_relativo: item,
        evidencia_qualidade: 'desconhecida'
    } : item);
}

function priorizar(caminhoEntrada) {
    const pendencias = carregarPendencias(caminhoEntrada);
    const priorizadas = {P1: [], P2: [], P3: []};

    pendencias.forEach(({caminho_relativo, evidencia_qualidade}) => {
        const prioridade = classificarArquivo(caminho_relativo);
        if (prioridade) {
            priorizadas[prioridade].push({
                caminho_relativo,
                evidencia_qualidade
            });
        }
    });

    Object.keys(priorizadas).forEach(chave => priorizadas[chave].sort((a, b) => {
        const pesoA = a.evidencia_qualidade === 'sem_evidencia_no_escopo' ? 0 : 1;
        const pesoB = b.evidencia_qualidade === 'sem_evidencia_no_escopo' ? 0 : 1;
        return pesoA - pesoB || a.caminho_relativo.localeCompare(b.caminho_relativo, 'pt-BR');
    }));
    return priorizadas;
}

function descricaoEvidencia(item) {
    if (item.evidencia_qualidade === 'cobertura_indireta') {
        return 'cobertura indireta';
    }
    if (item.evidencia_qualidade === 'sem_evidencia_no_escopo') {
        return 'sem evidência';
    }
    return 'pendência';
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
        priorizadas.P1.forEach(item => linhas.push(`- [ ] \`${item.caminho_relativo}\` (${descricaoEvidencia(item)})`));
    }

    linhas.push('\n## P2: Importantes (Integracao e Contratos)\n');
    linhas.push('Controladores e mappers. Importantes para garantir que a API respeite os contratos e que os dados sejam transformados corretamente.\n');
    if (priorizadas.P2.length === 0) {
        linhas.push('_Nenhum arquivo encontrado._');
    } else {
        priorizadas.P2.forEach(item => linhas.push(`- [ ] \`${item.caminho_relativo}\` (${descricaoEvidencia(item)})`));
    }

    linhas.push('\n## P3: Baixa Prioridade (Dados e Infraestrutura)\n');
    linhas.push('DTOs, modelos, repositorios e configuracoes. Geralmente cobertos por testes de integracao ou seguros por natureza.\n');
    if (priorizadas.P3.length === 0) {
        linhas.push('_Nenhum arquivo encontrado._');
    } else {
        priorizadas.P3.forEach(item => linhas.push(`- [ ] \`${item.caminho_relativo}\` (${descricaoEvidencia(item)})`));
    }

    return `${linhas.join('\n')}\n`;
}

function principal(argumentos = process.argv.slice(2)) {
    const opcoes = lerArgumentos(argumentos);
    if (opcoes.ajuda) {
        imprimirAjuda();
        return;
    }

    const caminhoEntrada = opcoes.entradaExplicita ? opcoes.entrada : resolverEntradaPadrao(opcoes.entrada);
    const priorizadas = priorizar(caminhoEntrada);
    fs.mkdirSync(path.dirname(opcoes.saida), {recursive: true});
    fs.writeFileSync(opcoes.saida, gerarMarkdown(priorizadas), 'utf-8');
    escreverLinha(`Entrada utilizada: ${caminhoEntrada}`);
    escreverLinha(`Priorizacao concluida. Encontrados ${priorizadas.P1.length} P1, ${priorizadas.P2.length} P2, ${priorizadas.P3.length} P3.`);
    escreverLinha(`Plano gerado em: ${opcoes.saida}`);
}

if (ehEntradaPrincipal(import.meta.url)) {
    try {
        principal();
    } catch (erro) {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(`Erro ao processar priorizacao: ${mensagem}`);
        process.exitCode = 1;
    }
}

export {
    principal,
};
