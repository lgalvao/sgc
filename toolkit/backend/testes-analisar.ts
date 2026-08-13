#!/usr/bin/env node
// Analisador de cobertura de testes do backend.
import fs from "node:fs";
import path from "node:path";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha} from "../lib/saida.js";
import {extrairCoberturaJacoco, type ClasseCobertura} from "../lib/dominios/cobertura-java.js";
import {
    CATEGORIAS_PRIORITARIAS,
    CATEGORIAS_SECUNDARIAS,
    classificarPerfilDto,
    classificarPerfilModel,
    classificarPerfilOther,
    construirNomeClasseCompleto,
    criarItemRelatorio,
    EXTENSAO_JAVA,
    inferirCategoria,
    lerConteudoFonte,
    normalizarCaminho,
    SUFIXOS_TESTE
} from "./lib/testes-analisar-regras.js";

type Categoria = (typeof CATEGORIAS_PRIORITARIAS | typeof CATEGORIAS_SECUNDARIAS)[number];
type PerfilFonte = "comportamental" | "estrutural_contrato" | "estrutural_puro";
type EstrategiaCorrespondencia = "mesmo_pacote" | "nome_correspondente_outro_pacote" | "nenhum";
type ItemRelatorioBase = ReturnType<typeof criarItemRelatorio>;
type ItemRelatorio = ItemRelatorioBase & {
    perfil_model: PerfilFonte | null;
    model_ruido_ignorado: boolean;
    perfil_other: PerfilFonte | null;
    other_ruido_ignorado: boolean;
};

interface ArquivoFonte {
    caminho_relativo: string;
    nome_classe: string;
    pacote: string;
    categoria: Categoria;
}

interface IndicesTestes {
    indicePorNome: Map<string, string[]>;
    indicePorPacote: Map<string, string[]>;
}

interface LocalizacaoTestes {
    caminhos: string[];
    estrategia: EstrategiaCorrespondencia;
}

interface GrupoRelatorio {
    tested: ItemRelatorio[];
    untested: ItemRelatorio[];
}

type CategoriasRelatorio = Record<Categoria, GrupoRelatorio>;

interface EstatisticasRelatorio {
    total_classes: number;
    classes_com_teste_dedicado: number;
    classes_com_cobertura_indireta: number;
    classes_sem_evidencia_no_escopo: number;
    classes_fora_escopo_jacoco: number;
    classes_ruido_ignorado: number;
    classes_sem_teste_dedicado: number;
    cobertura_arquivos_percentual: number;
    cobertura_backlog_real_percentual: number;
    cobertura_observada_percentual: number;
    correspondencias_ambiguas: number;
    jacoco_disponivel: boolean;
    dtos_comportamentais: number;
    dtos_estruturais: number;
    dtos_estruturais_contratuais: number;
    models_comportamentais: number;
    models_estruturais: number;
    models_estruturais_contratuais: number;
    others_comportamentais: number;
    others_estruturais: number;
    others_estruturais_contratuais: number;
}

interface RelatorioTestes {
    gerado_em: string;
    backend_dir: string;
    estatisticas: EstatisticasRelatorio;
    categorias: CategoriasRelatorio;
}

interface OpcoesAnalisar {
    base: string;
    diretorio: string;
    diretorioTestes: string;
    saida: string;
    saidaJson: string | null;
    arquivoJacoco: string | null;
    ajuda: boolean;
}

interface OpcoesAnaliseTestes {
    diretorioFonte: string;
    diretorioTestes: string;
    caminhoJacocoXml?: string | null;
    base?: string;
}

function lerArgumentos(argumentos: string[]): OpcoesAnalisar {
    const base = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const diretorioInformado = lerOpcao(argumentos, "--diretorio", undefined);
    const raizBackendInformada = diretorioInformado ? path.resolve(base, diretorioInformado) : null;
    const resultado = {
        base,
        diretorio: raizBackendInformada
            ? path.join(raizBackendInformada, "src", "main", "java")
            : resolverCaminhoConfigurado("backendCodigo", base),
        diretorioTestes: raizBackendInformada
            ? path.join(raizBackendInformada, "src", "test", "java")
            : resolverCaminhoConfigurado("backendTestes", base),
        saida: lerOpcao(argumentos, "--saida", "analise-testes.md") ?? "analise-testes.md",
        saidaJson: lerOpcao(argumentos, "--saida-json", undefined) ?? null,
        arquivoJacoco: lerOpcao(argumentos, "--arquivo-jacoco", undefined) ?? null,
        ajuda: argumentos.includes("--help") || argumentos.includes("-h"),
    };
    return resultado;
}

function imprimirAjuda(): void {
    exibirAjudaComando({
        comandoSgc: "backend testes analisar",
        scriptDireto: "backend/testes-analisar.ts",
        descricao: 'Analisa classes sem testes correspondentes e gera relatorios em Markdown e JSON com resumo por categoria.',
        opcoes: [
            '--base <diretorio>     Base do projeto para resolver configuracao.',
            '--diretorio <caminho>   Diretorio de fontes Java; substitui backendCodigo.',
            '--saida <arquivo>       Arquivo de saida em Markdown',
            '--saida-json <arquivo>  Arquivo de saida estruturado em JSON (padrao: sidecar do Markdown)',
            '--arquivo-jacoco <arquivo> Relatorio XML do JaCoCo para classificar cobertura indireta',
            '--help, -h              Exibe esta ajuda'
        ],
        exemplos: [
            "npx tsx toolkit/sgc.ts backend testes analisar --diretorio backend --saida analise-testes.md --saida-json analise-testes.json"
        ]
    });
}

function resolverSaidaJsonPadrao(caminhoMarkdown: string): string {
    if (caminhoMarkdown.toLowerCase().endsWith('.md')) {
        return caminhoMarkdown.replace(/\.md$/i, '.json');
    }
    return `${caminhoMarkdown}.json`;
}

function listarFontes(backendSrc: string): ArquivoFonte[] {
    const arquivos: ArquivoFonte[] = [];

    function visitar(diretorio: string): void {
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

function indexarTestes(backendTest: string): IndicesTestes {
    const indicePorNome = new Map<string, string[]>();
    const indicePorPacote = new Map<string, string[]>();

    if (!fs.existsSync(backendTest)) {
        return {indicePorNome, indicePorPacote};
    }

    function visitar(diretorio: string): void {
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

            const testesPorNome = indicePorNome.get(entrada.name) ?? [];
            testesPorNome.push(caminhoRelativo);
            indicePorNome.set(entrada.name, testesPorNome);

            const chavePacote = `${pacote}::${entrada.name}`;
            const testesPorPacote = indicePorPacote.get(chavePacote) ?? [];
            testesPorPacote.push(caminhoRelativo);
            indicePorPacote.set(chavePacote, testesPorPacote);
        });
    }

    visitar(backendTest);
    return {indicePorNome, indicePorPacote};
}


function localizarTestes(
    nomeClasse: string,
    pacote: string,
    indicePorNome: Map<string, string[]>,
    indicePorPacote: Map<string, string[]>
): LocalizacaoTestes {
    const candidatos = SUFIXOS_TESTE.map(sufixo => `${nomeClasse}${sufixo}${EXTENSAO_JAVA}`);
    const encontradosMesmoPacote: string[] = [];

    candidatos.forEach(candidato => {
        const chavePacote = `${pacote}::${candidato}`;
        (indicePorPacote.get(chavePacote) || []).forEach(item => encontradosMesmoPacote.push(item));
    });

    if (encontradosMesmoPacote.length > 0) {
        return {
            caminhos: [...new Set(encontradosMesmoPacote)].toSorted(),
            estrategia: 'mesmo_pacote'
        };
    }

    const encontradosPorNome: string[] = [];
    candidatos.forEach(candidato => {
        (indicePorNome.get(candidato) || []).forEach(item => encontradosPorNome.push(item));
    });

    if (encontradosPorNome.length > 0) {
        return {
            caminhos: [...new Set(encontradosPorNome)].toSorted(),
            estrategia: 'nome_correspondente_outro_pacote'
        };
    }

    return {
        caminhos: [],
        estrategia: 'nenhum'
    };
}

async function carregarCoberturaPorClasse(
    caminhoJacocoXml: string | null = null,
    diretorioBase: string = DIRETORIO_RAIZ
): Promise<Map<string, ClasseCobertura>> {
    try {
        const coleta = await extrairCoberturaJacoco(caminhoJacocoXml || undefined, {
            diretorioBase,
            incluirSemLacunas: true,
            aplicarExclusoes: false
        });

        return new Map(coleta.classes.map(arquivo => [arquivo.nomeClasse, arquivo] as const));
    } catch (error) {
        const mensagem = error instanceof Error ? error.message : String(error);
        if (/JaCoCo nao encontrado|JaCoCo não encontrado/i.test(mensagem)) {
            return new Map();
        }
        throw error;
    }
}

async function analisarTestes({
    diretorioFonte,
    diretorioTestes,
    caminhoJacocoXml = null,
    base = DIRETORIO_RAIZ
}: OpcoesAnaliseTestes): Promise<RelatorioTestes> {
    const backendSrc = path.resolve(diretorioFonte);
    const backendTest = path.resolve(diretorioTestes);

    if (!fs.existsSync(backendSrc)) {
        throw new Error(`Diretorio de origem nao encontrado: ${backendSrc}`);
    }

    const arquivosFonte = listarFontes(backendSrc);
    const {indicePorNome, indicePorPacote} = indexarTestes(backendTest);
    const coberturaPorClasse = await carregarCoberturaPorClasse(caminhoJacocoXml, base);

    const relatorio: CategoriasRelatorio = {
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
    let totalModelsComportamentais = 0;
    let totalModelsEstruturais = 0;
    let totalModelsEstruturaisContratuais = 0;
    let totalOthersComportamentais = 0;
    let totalOthersEstruturais = 0;
    let totalOthersEstruturaisContratuais = 0;

    arquivosFonte.forEach(arquivo => {
        const conteudoFonte = lerConteudoFonte(backendSrc, arquivo.caminho_relativo);
        const perfilDto = arquivo.categoria === 'DTOs' ? classificarPerfilDto(conteudoFonte) : null;
        const perfilModel = arquivo.categoria === 'Models'
            ? classificarPerfilModel({
                nomeClasse: arquivo.nome_classe,
                conteudoFonte
            })
            : null;
        const perfilOther = arquivo.categoria === 'Others'
            ? classificarPerfilOther({
                nomeClasse: arquivo.nome_classe,
                caminhoRelativo: arquivo.caminho_relativo,
                conteudoFonte
            })
            : null;
        const dtoEstrutural = perfilDto === 'estrutural_puro' || perfilDto === 'estrutural_contrato';
        const modelEstrutural = perfilModel === 'estrutural_puro' || perfilModel === 'estrutural_contrato';
        const otherEstrutural = perfilOther === 'estrutural_puro' || perfilOther === 'estrutural_contrato';
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
        const item: ItemRelatorio = {
            ...criarItemRelatorio({
            arquivo,
            perfilDto,
            dtoEstrutural: dtoEstrutural || modelEstrutural || otherEstrutural,
            possuiTeste,
            estaNoEscopoJacoco,
            possuiCoberturaJacoco,
            possuiCoberturaSomenteIndireta,
            estaForaEscopoJacoco,
            estrategia,
            caminhos,
            coberturaClasse
            }),
            perfil_model: perfilModel,
            model_ruido_ignorado: modelEstrutural,
            perfil_other: perfilOther,
            other_ruido_ignorado: otherEstrutural
        };

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

        if (arquivo.categoria === 'Models') {
            if (perfilModel === 'comportamental') {
                totalModelsComportamentais++;
            } else {
                totalModelsEstruturais++;
                if (perfilModel === 'estrutural_contrato') {
                    totalModelsEstruturaisContratuais++;
                }
            }
        }

        if (arquivo.categoria === 'Others') {
            if (perfilOther === 'comportamental') {
                totalOthersComportamentais++;
            } else {
                totalOthersEstruturais++;
                if (perfilOther === 'estrutural_contrato') {
                    totalOthersEstruturaisContratuais++;
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
            if (dtoEstrutural || modelEstrutural || otherEstrutural) {
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
        backend_dir: backendSrc,
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
            dtos_estruturais_contratuais: totalDtosEstruturaisContratuais,
            models_comportamentais: totalModelsComportamentais,
            models_estruturais: totalModelsEstruturais,
            models_estruturais_contratuais: totalModelsEstruturaisContratuais,
            others_comportamentais: totalOthersComportamentais,
            others_estruturais: totalOthersEstruturais,
            others_estruturais_contratuais: totalOthersEstruturaisContratuais
        },
        categorias: relatorio
    };
}

function gerarMarkdown(dados: RelatorioTestes): string {
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
            : (categoria === 'Models'
                ? itens.tested.length + itens.untested.filter(item => !item.model_ruido_ignorado).length
                : (categoria === 'Others'
                    ? itens.tested.length + itens.untested.filter(item => !item.other_ruido_ignorado).length
                    : total));
        linhas.push(`### ${categoria} (${itens.tested.length}/${totalRelevanteCategoria} testados${categoria === 'DTOs' || categoria === 'Models' || categoria === 'Others' ? ' no backlog real' : ''})`);
        if (itens.untested.length > 0) {
            const candidatos = categoria === 'DTOs'
                ? itens.untested.filter(item => !item.dto_ruido_ignorado)
                : (categoria === 'Models'
                    ? itens.untested.filter(item => !item.model_ruido_ignorado)
                    : (categoria === 'Others'
                        ? itens.untested.filter(item => !item.other_ruido_ignorado)
                        : itens.untested));
            const dtoRuido = categoria === 'DTOs'
                ? itens.untested.filter(item => item.dto_ruido_ignorado)
                : [];
            const modelRuido = categoria === 'Models'
                ? itens.untested.filter(item => item.model_ruido_ignorado)
                : [];
            const otherRuido = categoria === 'Others'
                ? itens.untested.filter(item => item.other_ruido_ignorado)
                : [];
            const indiretos = candidatos.filter(item => item.coberta_somente_indiretamente);
            const foraEscopo = candidatos.filter(item => item.fora_escopo_jacoco);
            const semEvidencia = candidatos.filter(item => !item.coberta_somente_indiretamente && !item.fora_escopo_jacoco);

            linhas.push(`**Faltando Testes Dedicados (${candidatos.length}):**`);
            if (indiretos.length > 0) {
                linhas.push(`Cobertos apenas indiretamente (${indiretos.length}):`);
                indiretos
                    .slice()
                    .toSorted((a, b) => a.caminho_relativo.localeCompare(b.caminho_relativo, 'pt-BR'))
                    .forEach(item => linhas.push(`- \`${item.caminho_relativo}\` (${item.cobertura?.cobertura_linhas_percentual.toFixed(2)}% linhas)`));
            }
            if (foraEscopo.length > 0) {
                linhas.push(`Fora do escopo do JaCoCo (${foraEscopo.length}):`);
                foraEscopo
                    .slice()
                    .toSorted((a, b) => a.caminho_relativo.localeCompare(b.caminho_relativo, 'pt-BR'))
                    .forEach(item => linhas.push(`- \`${item.caminho_relativo}\``));
            }
            if (semEvidencia.length > 0) {
                linhas.push(`Sem evidencia de cobertura no escopo (${semEvidencia.length}):`);
                semEvidencia
                    .slice()
                    .toSorted((a, b) => a.caminho_relativo.localeCompare(b.caminho_relativo, 'pt-BR'))
                    .forEach(item => linhas.push(`- \`${item.caminho_relativo}\``));
            }
            if (dtoRuido.length > 0) {
                linhas.push(`Ignorados como DTO estrutural/contratual (${dtoRuido.length}):`);
                dtoRuido
                    .slice()
                    .toSorted((a, b) => a.caminho_relativo.localeCompare(b.caminho_relativo, 'pt-BR'))
                    .forEach(item => linhas.push(`- \`${item.caminho_relativo}\` (${item.perfil_dto})`));
            }
            if (modelRuido.length > 0) {
                linhas.push(`Ignorados como model estrutural/contratual (${modelRuido.length}):`);
                modelRuido
                    .slice()
                    .toSorted((a, b) => a.caminho_relativo.localeCompare(b.caminho_relativo, 'pt-BR'))
                    .forEach(item => linhas.push(`- \`${item.caminho_relativo}\` (${item.perfil_model})`));
            }
            if (otherRuido.length > 0) {
                linhas.push(`Ignorados como others estrutural/contratual (${otherRuido.length}):`);
                otherRuido
                    .slice()
                    .toSorted((a, b) => a.caminho_relativo.localeCompare(b.caminho_relativo, 'pt-BR'))
                    .forEach(item => linhas.push(`- \`${item.caminho_relativo}\` (${item.perfil_other})`));
            }
        } else {
            linhas.push('Todos cobertos.');
        }
        linhas.push('');
    });

    return `${linhas.join('\n')}\n`;
}

function gravarArquivo(caminho: string, conteudo: string): void {
    fs.mkdirSync(path.dirname(caminho), {recursive: true});
    fs.writeFileSync(caminho, conteudo, 'utf-8');
}

function imprimirResumoConsole(dados: RelatorioTestes): void {
    const {estatisticas, categorias} = dados;
    escreverLinha(`Resumo: ${estatisticas.classes_com_teste_dedicado}/${estatisticas.total_classes} classes com teste dedicado (${estatisticas.cobertura_arquivos_percentual.toFixed(2)}%).`);
    escreverLinha(`- Cobertura indireta: ${estatisticas.classes_com_cobertura_indireta}`);
    escreverLinha(`- Sem evidencia no escopo: ${estatisticas.classes_sem_evidencia_no_escopo}`);
    escreverLinha(`- Fora do escopo do JaCoCo: ${estatisticas.classes_fora_escopo_jacoco}`);
    escreverLinha(`- Ruido ignorado no backlog: ${estatisticas.classes_ruido_ignorado}`);
    escreverLinha(`- Backlog real coberto por teste dedicado: ${estatisticas.cobertura_backlog_real_percentual.toFixed(2)}%`);
    if (estatisticas.jacoco_disponivel) {
        escreverLinha(`- Cobertura observada: ${estatisticas.cobertura_observada_percentual.toFixed(2)}%`);
    } else {
        escreverLinha('- Cobertura observada: indisponivel (JaCoCo ausente)');
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
            escreverLinha(`- ${categoria}: ${itens.tested.length}/${totalRelevante} testados no backlog real (${ignorados} ignorados)`);
            return;
        }
        if (categoria === 'Models') {
            const totalRelevante = itens.tested.length + itens.untested.filter(item => !item.model_ruido_ignorado).length;
            const ignorados = itens.untested.filter(item => item.model_ruido_ignorado).length;
            escreverLinha(`- ${categoria}: ${itens.tested.length}/${totalRelevante} testados no backlog real (${ignorados} ignorados)`);
            return;
        }
        if (categoria === 'Others') {
            const totalRelevante = itens.tested.length + itens.untested.filter(item => !item.other_ruido_ignorado).length;
            const ignorados = itens.untested.filter(item => item.other_ruido_ignorado).length;
            escreverLinha(`- ${categoria}: ${itens.tested.length}/${totalRelevante} testados no backlog real (${ignorados} ignorados)`);
            return;
        }
        escreverLinha(`- ${categoria}: ${itens.tested.length}/${total} testados`);
    });

    if (estatisticas.correspondencias_ambiguas > 0) {
        escreverLinha(`- Correspondencias ambiguas: ${estatisticas.correspondencias_ambiguas}`);
    }
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const opcoes = lerArgumentos(argumentos);
    if (opcoes.ajuda) {
        imprimirAjuda();
        return;
    }

    const caminhoSaidaJson = opcoes.saidaJson ?? resolverSaidaJsonPadrao(opcoes.saida);
    const dados = await analisarTestes({
        diretorioFonte: opcoes.diretorio,
        diretorioTestes: opcoes.diretorioTestes,
        caminhoJacocoXml: opcoes.arquivoJacoco,
        base: opcoes.base,
    });
    gravarArquivo(opcoes.saida, gerarMarkdown(dados));
    gravarArquivo(caminhoSaidaJson, JSON.stringify(dados, null, 2));

    imprimirResumoConsole(dados);
    escreverLinha(`Relatorio Markdown gerado em: ${opcoes.saida}`);
    escreverLinha(`Relatorio JSON gerado em: ${caminhoSaidaJson}`);
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(`Erro ao analisar testes: ${mensagem}`);
        process.exitCode = 1;
    });
}

export {
    principal,
};
