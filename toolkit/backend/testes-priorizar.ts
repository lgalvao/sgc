// Priorizador de testes do backend.
import fs from "node:fs";
import path from "node:path";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverErro, escreverLinha, imprimirJson} from "../lib/saida.js";

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
    caminho_relativo: string;
    evidencia_qualidade: string;
}

type PendenciaEntrada = Pendencia | string;
type PendenciasPriorizadas = Record<Prioridade, Pendencia[]>;

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
        comandoSgc: "backend testes priorizar",
        scriptDireto: "backend/testes-priorizar.ts",
        descricao: 'Prioriza o backlog de testes a partir do relatorio estruturado ou Markdown.',
        opcoes: [
            '--entrada <arquivo> Arquivo de entrada em JSON ou Markdown',
            '--saida <arquivo>   Arquivo de saida em Markdown',
            '--gravar             Persiste o relatorio Markdown',
            '--json               Emite a priorizacao estruturada no stdout',
            '--help, -h          Exibe esta ajuda'
        ],
        exemplos: [
            "npx tsx toolkit/sgc.ts backend testes priorizar --entrada analise-testes.json --saida priorizacao-testes.md"
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
    if (!ehRegistroJson(dados) || !ehRegistroJson(dados.categorias)) {
        return [];
    }

    const pendencias: Pendencia[] = [];
    for (const categoria of Object.values(dados.categorias)) {
        if (!ehRegistroJson(categoria) || !Array.isArray(categoria.untested)) {
            continue;
        }

        for (const item of categoria.untested) {
            if (!ehRegistroJson(item) || typeof item.caminho_relativo !== "string") {
                continue;
            }

            const ignorado = item.dto_ruido_ignorado || item.model_ruido_ignorado || item.other_ruido_ignorado;
            if (ignorado || item.evidencia_qualidade === "fora_escopo_jacoco") {
                continue;
            }

            pendencias.push({
                caminho_relativo: item.caminho_relativo,
                evidencia_qualidade: typeof item.evidencia_qualidade === "string"
                    ? item.evidencia_qualidade
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
        caminho_relativo: item,
        evidencia_qualidade: "desconhecida"
    } : item);
}

function priorizar(caminhoEntrada: string): PendenciasPriorizadas {
    const pendencias = carregarPendencias(caminhoEntrada);
    const priorizadas: PendenciasPriorizadas = {P1: [], P2: [], P3: []};

    pendencias.forEach(({caminho_relativo, evidencia_qualidade}) => {
        const prioridade = classificarArquivo(caminho_relativo);
        if (prioridade) {
            priorizadas[prioridade].push({
                caminho_relativo,
                evidencia_qualidade
            });
        }
    });

    (Object.keys(priorizadas) as Prioridade[]).forEach(chave => priorizadas[chave].sort((a, b) => {
        const pesoA = a.evidencia_qualidade === 'sem_evidencia_no_escopo' ? 0 : 1;
        const pesoB = b.evidencia_qualidade === 'sem_evidencia_no_escopo' ? 0 : 1;
        return pesoA - pesoB || a.caminho_relativo.localeCompare(b.caminho_relativo, 'pt-BR');
    }));
    return priorizadas;
}

function descricaoEvidencia(item: Pendencia): string {
    if (item.evidencia_qualidade === 'cobertura_indireta') {
        return 'cobertura indireta';
    }
    if (item.evidencia_qualidade === 'sem_evidencia_no_escopo') {
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

function principal(argumentos: string[] = process.argv.slice(2)): void {
    const opcoes = lerArgumentos(argumentos);
    if (opcoes.ajuda) {
        imprimirAjuda();
        return;
    }

    const caminhoEntrada = opcoes.entradaExplicita ? opcoes.entrada : resolverEntradaPadrao(opcoes.entrada);
    const priorizadas = priorizar(caminhoEntrada);
    if (opcoes.emitirJson) {
        imprimirJson(priorizadas);
    } else if (!opcoes.gravar) {
        escreverLinha(gerarMarkdown(priorizadas).trimEnd());
    }

    if (!opcoes.gravar) {
        return;
    }

    fs.mkdirSync(path.dirname(opcoes.saida), {recursive: true});
    fs.writeFileSync(opcoes.saida, gerarMarkdown(priorizadas), 'utf-8');
    const escreverStatus = opcoes.emitirJson ? escreverErro : escreverLinha;
    escreverStatus(`Entrada utilizada: ${caminhoEntrada}`);
    escreverStatus(`Priorizacao concluida. Encontrados ${priorizadas.P1.length} P1, ${priorizadas.P2.length} P2, ${priorizadas.P3.length} P3.`);
    escreverStatus(`Plano gerado em: ${opcoes.saida}`);
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
