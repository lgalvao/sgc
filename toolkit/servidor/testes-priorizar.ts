// Priorizador de testes do servidor.
import fs from "node:fs";
import path from "node:path";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverErro, escreverLinha, imprimirJson} from "../biblioteca/saida.js";
import {VERSAO_PRIORIZACAO_TESTES, VERSAO_RELATORIO_TESTES} from "./biblioteca/testes-contrato.js";

type Prioridade = "P1" | "P2" | "P3";

interface OpcoesPriorizar {
    entrada: string;
    entradaExplicita: boolean;
    saida: string;
    ajuda: boolean;
    gravar: boolean;
    emitirJson: boolean;
}

interface Pendencia {
    caminhoRelativo: string;
    evidenciaQualidade: string;
}

type PendenciaEntrada = Pendencia | string;
type PendenciasPriorizadas = Record<Prioridade, Pendencia[]>;

interface ResultadoPriorizacao {
    versao: typeof VERSAO_PRIORIZACAO_TESTES;
    prioridades: PendenciasPriorizadas;
}

interface RegistroJson {
    [chave: string]: unknown;
}

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

function lerArgumentos(argumentos: string[]): OpcoesPriorizar {
    const resultado = {
        entrada: lerOpcao(argumentos, "--entrada", "analise-testes.md") ?? "analise-testes.md",
        entradaExplicita: argumentos.includes("--entrada") || argumentos.some((argumento) => argumento.startsWith("--entrada=")),
        saida: lerOpcao(argumentos, "--saida", "priorizacao-testes.md") ?? "priorizacao-testes.md",
        ajuda: argumentos.includes("--help") || argumentos.includes("-h"),
        gravar: argumentos.includes("--gravar"),
        emitirJson: argumentos.includes("--json"),
    };
    return resultado;
}

function imprimirAjuda() {
    exibirAjudaComando({
        comandoSgc: "servidor testes priorizar",
        scriptDireto: "servidor/testes-priorizar.ts",
        descricao: 'Prioriza o backlog de testes a partir do relatorio estruturado ou Markdown.',
        opcoes: [
            '--entrada <arquivo> Arquivo de entrada em JSON ou Markdown',
            '--saida <arquivo>   Arquivo de saida em Markdown',
            '--gravar             Persiste o relatorio Markdown',
            '--json               Emite a priorizacao estruturada no stdout',
            '--help, -h          Exibe esta ajuda'
        ],
        exemplos: [
            "npx tsx toolkit/sgc.ts servidor testes priorizar --entrada analise-testes.json --saida priorizacao-testes.md"
        ]
    });
}

function resolverEntradaPadrao(caminhoMarkdown: string): string {
    const diretorio = path.dirname(caminhoMarkdown);
    const nomeArquivo = `${path.parse(caminhoMarkdown).name}.json`;
    const jsonSidecar = diretorio === '.' ? nomeArquivo : path.join(diretorio, nomeArquivo);

    if (fs.existsSync(jsonSidecar)) {
        return jsonSidecar;
    }

    return caminhoMarkdown;
}

function classificarArquivo(caminhoArquivo: string): Prioridade | null {
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

function ehRegistroJson(valor: unknown): valor is RegistroJson {
    return typeof valor === "object" && valor !== null && !Array.isArray(valor);
}

function extrairPendenciasDeJson(caminhoEntrada: string): Pendencia[] {
    const dados: unknown = JSON.parse(fs.readFileSync(caminhoEntrada, "utf-8"));
    if (!ehRegistroJson(dados)) {
        throw new Error("Relatorio de analise de testes invalido: o JSON deve conter um objeto na raiz.");
    }
    if (dados.versao !== VERSAO_RELATORIO_TESTES) {
        throw new Error(`Relatorio de analise de testes possui versao ausente ou incompativel: esperado ${VERSAO_RELATORIO_TESTES}.`);
    }
    if (!ehRegistroJson(dados.categorias)) {
        throw new Error("Relatorio de analise de testes invalido: campo categorias ausente ou invalido.");
    }

    const pendencias: Pendencia[] = [];
    for (const categoria of Object.values(dados.categorias)) {
        if (!ehRegistroJson(categoria) || !Array.isArray(categoria.semTeste)) {
            throw new Error("Relatorio de analise de testes invalido: cada categoria deve conter o grupo semTeste.");
        }

        for (const item of categoria.semTeste) {
            if (!ehRegistroJson(item) || typeof item.caminhoRelativo !== "string") {
                continue;
            }

            const ignorado = item.ruidoDtoIgnorado || item.ruidoModeloIgnorado || item.ruidoOutroIgnorado;
            if (ignorado || item.evidenciaQualidade === "foraEscopoJacoco") {
                continue;
            }

            pendencias.push({
                caminhoRelativo: item.caminhoRelativo,
                evidenciaQualidade: typeof item.evidenciaQualidade === "string"
                    ? item.evidenciaQualidade
                    : "desconhecida"
            });
        }
    }
    return pendencias;
}

function extrairPendenciasDeMarkdown(caminhoEntrada: string): string[] {
    const linhas = fs.readFileSync(caminhoEntrada, 'utf-8').split(/\r?\n/);
    return linhas
        .filter(linha => linha.trim().startsWith('- `'))
        .map(linha => linha.trim().replace('- `', '').replace(/`/g, ''));
}

function carregarPendencias(caminhoEntrada: string): Pendencia[] {
    if (!fs.existsSync(caminhoEntrada)) {
        throw new Error(`Arquivo de entrada nao encontrado: ${caminhoEntrada}`);
    }
    const pendencias = caminhoEntrada.endsWith('.json')
        ? extrairPendenciasDeJson(caminhoEntrada)
        : extrairPendenciasDeMarkdown(caminhoEntrada);
    return pendencias.map((item: PendenciaEntrada): Pendencia => typeof item === 'string' ? {
        caminhoRelativo: item,
        evidenciaQualidade: "desconhecida"
    } : item);
}

function priorizar(caminhoEntrada: string): PendenciasPriorizadas {
    const pendencias = carregarPendencias(caminhoEntrada);
    const priorizadas: PendenciasPriorizadas = {P1: [], P2: [], P3: []};

    pendencias.forEach(({caminhoRelativo, evidenciaQualidade}) => {
        const prioridade = classificarArquivo(caminhoRelativo);
        if (prioridade) {
            priorizadas[prioridade].push({
                caminhoRelativo,
                evidenciaQualidade
            });
        }
    });

    (Object.keys(priorizadas) as Prioridade[]).forEach(chave => priorizadas[chave].sort((a, b) => {
        const pesoA = a.evidenciaQualidade === 'semEvidenciaNoEscopo' ? 0 : 1;
        const pesoB = b.evidenciaQualidade === 'semEvidenciaNoEscopo' ? 0 : 1;
        return pesoA - pesoB || a.caminhoRelativo.localeCompare(b.caminhoRelativo, 'pt-BR');
    }));
    return priorizadas;
}

function descricaoEvidencia(item: Pendencia): string {
    if (item.evidenciaQualidade === 'coberturaIndireta') {
        return 'cobertura indireta';
    }
    if (item.evidenciaQualidade === 'semEvidenciaNoEscopo') {
        return 'sem evidência';
    }
    return 'pendência';
}

function gerarMarkdown(priorizadas: PendenciasPriorizadas): string {
    const linhas = [
        '# Plano de Priorizacao de Testes Unitarios\n',
        '## P1: Criticos (Logica de Negocio e Seguranca)\n',
        'Estas classes contem regras de negocio, validacoes, seguranca ou orquestracao complexa. A falta de testes aqui representa alto risco.\n'
    ];

    if (priorizadas.P1.length === 0) {
        linhas.push('Nenhuma pendencia critica de logica encontrada.\n');
    } else {
        priorizadas.P1.forEach(item => linhas.push(`- [ ] \`${item.caminhoRelativo}\` (${descricaoEvidencia(item)})`));
    }

    linhas.push('\n## P2: Importantes (Integracao e Contratos)\n');
    linhas.push('Controladores e mapeadores. Importantes para garantir que a API respeite os contratos e que os dados sejam transformados corretamente.\n');
    if (priorizadas.P2.length === 0) {
        linhas.push('_Nenhum arquivo encontrado._');
    } else {
        priorizadas.P2.forEach(item => linhas.push(`- [ ] \`${item.caminhoRelativo}\` (${descricaoEvidencia(item)})`));
    }

    linhas.push('\n## P3: Baixa Prioridade (Dados e Infraestrutura)\n');
    linhas.push('dtos, modelos, repositorios e configuracoes. Geralmente cobertos por testes de integracao ou seguros por natureza.\n');
    if (priorizadas.P3.length === 0) {
        linhas.push('_Nenhum arquivo encontrado._');
    } else {
        priorizadas.P3.forEach(item => linhas.push(`- [ ] \`${item.caminhoRelativo}\` (${descricaoEvidencia(item)})`));
    }

    return `${linhas.join('\n')}\n`;
}

function criarResultado(prioridades: PendenciasPriorizadas): ResultadoPriorizacao {
    return {
        versao: VERSAO_PRIORIZACAO_TESTES,
        prioridades
    };
}

function principal(argumentos: string[] = process.argv.slice(2)): void {
    const opcoes = lerArgumentos(validarArgumentosEntradaDireta(import.meta.url, argumentos));
    if (opcoes.ajuda) {
        imprimirAjuda();
        return;
    }

    const caminhoEntrada = opcoes.entradaExplicita ? opcoes.entrada : resolverEntradaPadrao(opcoes.entrada);
    const resultado = criarResultado(priorizar(caminhoEntrada));
    if (opcoes.emitirJson) {
        imprimirJson(resultado);
    } else if (!opcoes.gravar) {
        escreverLinha(gerarMarkdown(resultado.prioridades).trimEnd());
    }

    if (!opcoes.gravar) {
        return;
    }

    fs.mkdirSync(path.dirname(opcoes.saida), {recursive: true});
    fs.writeFileSync(opcoes.saida, gerarMarkdown(resultado.prioridades), 'utf-8');
    const escreverStatus = opcoes.emitirJson ? escreverErro : escreverLinha;
    escreverStatus(`Entrada utilizada: ${caminhoEntrada}`);
    escreverStatus(`Priorizacao concluida. Encontrados ${resultado.prioridades.P1.length} P1, ${resultado.prioridades.P2.length} P2, ${resultado.prioridades.P3.length} P3.`);
    escreverStatus(`Plano gerado em: ${opcoes.saida}`);
}

if (ehEntradaPrincipal(import.meta.url)) {
    try {
        principal();
    } catch (erro) {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverErro(`Erro ao processar priorizacao: ${mensagem}`);
        process.exitCode = 1;
    }
}

export {
    principal,
};
