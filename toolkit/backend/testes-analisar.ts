// Analisador de cobertura de testes do backend.
import fs from "node:fs";
import path from "node:path";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverErro, escreverLinha, imprimirJson} from "../biblioteca/saida.js";
import {extrairCoberturaJacoco, type ClasseCobertura} from "../biblioteca/dominios/cobertura-java.js";
import {VERSAO_RELATORIO_TESTES} from "./biblioteca/testes-contrato.js";
import {
    CATEGORIAS_PRIORITARIAS,
    CATEGORIAS_SECUNDARIAS,
    classificarPerfilDto,
    classificarPerfilModelo,
    classificarPerfilOutro,
    construirNomeClasseCompleto,
    criarItemRelatorio,
    EXTENSAO_JAVA,
    inferirCategoria,
    lerConteudoFonte,
    normalizarCaminho,
    SUFIXOS_TESTE
} from "./biblioteca/testes-analisar-regras.js";

type Categoria = (typeof CATEGORIAS_PRIORITARIAS | typeof CATEGORIAS_SECUNDARIAS)[number];
type PerfilFonte = "comportamental" | "estruturalContrato" | "estruturalPuro";
type EstrategiaCorrespondencia = "mesmoPacote" | "nomeCorrespondenteOutroPacote" | "nenhum";
type ItemRelatorioBase = ReturnType<typeof criarItemRelatorio>;
type ItemRelatorio = ItemRelatorioBase & {
    perfilModelo: PerfilFonte | null;
    ruidoModeloIgnorado: boolean;
    perfilOutro: PerfilFonte | null;
    ruidoOutroIgnorado: boolean;
};

interface ArquivoFonte {
    caminhoRelativo: string;
    nomeClasse: string;
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
    comTeste: ItemRelatorio[];
    semTeste: ItemRelatorio[];
}

type CategoriasRelatorio = Record<Categoria, GrupoRelatorio>;

interface EstatisticasRelatorio {
    totalClasses: number;
    classesComTesteDedicado: number;
    classesComCoberturaIndireta: number;
    classesSemEvidenciaNoEscopo: number;
    classesForaEscopoJacoco: number;
    classesRuidoIgnorado: number;
    classesSemTesteDedicado: number;
    coberturaArquivosPercentual: number;
    coberturaBacklogRealPercentual: number;
    coberturaObservadaPercentual: number;
    correspondenciasAmbiguas: number;
    jacocoDisponivel: boolean;
    dtosComportamentais: number;
    dtosEstruturais: number;
    dtosEstruturaisContratuais: number;
    modelosComportamentais: number;
    modelosEstruturais: number;
    modelosEstruturaisContratuais: number;
    outrosComportamentais: number;
    outrosEstruturais: number;
    outrosEstruturaisContratuais: number;
}

interface RelatorioTestes {
    versao: typeof VERSAO_RELATORIO_TESTES;
    geradoEm: string;
    diretorioBackend: string;
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
    gravar: boolean;
    emitirJson: boolean;
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
        gravar: argumentos.includes("--gravar"),
        emitirJson: argumentos.includes("--json"),
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
            '--gravar                Persiste os relatorios em Markdown e JSON',
            '--json                  Emite o relatorio estruturado no stdout',
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
                caminhoRelativo: caminhoRelativo,
                nomeClasse: nomeClasse,
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
            estrategia: 'mesmoPacote'
        };
    }

    const encontradosPorNome: string[] = [];
    candidatos.forEach(candidato => {
        (indicePorNome.get(candidato) || []).forEach(item => encontradosPorNome.push(item));
    });

    if (encontradosPorNome.length > 0) {
        return {
            caminhos: [...new Set(encontradosPorNome)].toSorted(),
            estrategia: 'nomeCorrespondenteOutroPacote'
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
        controladores: {comTeste: [], semTeste: []},
        servicos: {comTeste: [], semTeste: []},
        fachadas: {comTeste: [], semTeste: []},
        mapeadores: {comTeste: [], semTeste: []},
        modelos: {comTeste: [], semTeste: []},
        dtos: {comTeste: [], semTeste: []},
        repositorios: {comTeste: [], semTeste: []},
        outros: {comTeste: [], semTeste: []}
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
    let totalModelosComportamentais = 0;
    let totalModelosEstruturais = 0;
    let totalModelosEstruturaisContratuais = 0;
    let totalOutrosComportamentais = 0;
    let totalOutrosEstruturais = 0;
    let totalOutrosEstruturaisContratuais = 0;

    arquivosFonte.forEach(arquivo => {
        const conteudoFonte = lerConteudoFonte(backendSrc, arquivo.caminhoRelativo);
        const perfilDto = arquivo.categoria === 'dtos' ? classificarPerfilDto(conteudoFonte) : null;
        const perfilModelo = arquivo.categoria === 'modelos'
            ? classificarPerfilModelo({
                nomeClasse: arquivo.nomeClasse,
                conteudoFonte
            })
            : null;
        const perfilOutro = arquivo.categoria === 'outros'
            ? classificarPerfilOutro({
                nomeClasse: arquivo.nomeClasse,
                caminhoRelativo: arquivo.caminhoRelativo,
                conteudoFonte
            })
            : null;
        const dtoEstrutural = perfilDto === 'estruturalPuro' || perfilDto === 'estruturalContrato';
        const modeloEstrutural = perfilModelo === 'estruturalPuro' || perfilModelo === 'estruturalContrato';
        const outroEstrutural = perfilOutro === 'estruturalPuro' || perfilOutro === 'estruturalContrato';
        const {caminhos, estrategia} = localizarTestes(
            arquivo.nomeClasse,
            arquivo.pacote,
            indicePorNome,
            indicePorPacote
        );
        const possuiTeste = caminhos.length > 0;
        const nomeClasseCompleto = construirNomeClasseCompleto(arquivo.caminhoRelativo);
        const coberturaClasse = coberturaPorClasse.get(nomeClasseCompleto) || null;
        const estaNoEscopoJacoco = coberturaPorClasse.size === 0 || coberturaClasse !== null;
        const possuiCoberturaJacoco = coberturaClasse !== null && coberturaClasse.linhasCobertas > 0;
        const possuiCoberturaSomenteIndireta = !possuiTeste && possuiCoberturaJacoco;
        const estaForaEscopoJacoco = !possuiTeste && coberturaPorClasse.size > 0 && !estaNoEscopoJacoco;
        const item: ItemRelatorio = {
            ...criarItemRelatorio({
            arquivo,
            perfilDto,
            dtoEstrutural: dtoEstrutural || modeloEstrutural || outroEstrutural,
            possuiTeste,
            estaNoEscopoJacoco,
            possuiCoberturaJacoco,
            possuiCoberturaSomenteIndireta,
            estaForaEscopoJacoco,
            estrategia,
            caminhos,
            coberturaClasse
            }),
            perfilModelo,
            ruidoModeloIgnorado: modeloEstrutural,
            perfilOutro: perfilOutro,
            ruidoOutroIgnorado: outroEstrutural
        };

        if (arquivo.categoria === 'dtos') {
            if (perfilDto === 'comportamental') {
                totalDtosComportamentais++;
            } else {
                totalDtosEstruturais++;
                if (perfilDto === 'estruturalContrato') {
                    totalDtosEstruturaisContratuais++;
                }
            }
        }

        if (arquivo.categoria === 'modelos') {
            if (perfilModelo === 'comportamental') {
                totalModelosComportamentais++;
            } else {
                totalModelosEstruturais++;
                if (perfilModelo === 'estruturalContrato') {
                    totalModelosEstruturaisContratuais++;
                }
            }
        }

        if (arquivo.categoria === 'outros') {
            if (perfilOutro === 'comportamental') {
                totalOutrosComportamentais++;
            } else {
                totalOutrosEstruturais++;
                if (perfilOutro === 'estruturalContrato') {
                    totalOutrosEstruturaisContratuais++;
                }
            }
        }

        if (possuiTeste) {
            totalComTeste++;
            relatorio[arquivo.categoria].comTeste.push(item);
            if (estrategia === 'nomeCorrespondenteOutroPacote') {
                correspondenciasAmbiguas++;
            }
        } else {
            relatorio[arquivo.categoria].semTeste.push(item);
            if (dtoEstrutural || modeloEstrutural || outroEstrutural) {
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
        versao: VERSAO_RELATORIO_TESTES,
        geradoEm: new Date().toISOString(),
        diretorioBackend: backendSrc,
        estatisticas: {
            totalClasses: totalClasses,
            classesComTesteDedicado: totalComTeste,
            classesComCoberturaIndireta: totalComCoberturaIndireta,
            classesSemEvidenciaNoEscopo: totalSemEvidenciaNoEscopo,
            classesForaEscopoJacoco: totalForaEscopoJacoco,
            classesRuidoIgnorado: totalRuidoIgnorado,
            classesSemTesteDedicado: totalClasses - totalComTeste,
            coberturaArquivosPercentual: Number(cobertura.toFixed(2)),
            coberturaBacklogRealPercentual: Number(coberturaBacklogReal.toFixed(2)),
            coberturaObservadaPercentual: Number(coberturaObservada.toFixed(2)),
            correspondenciasAmbiguas: correspondenciasAmbiguas,
            jacocoDisponivel: coberturaPorClasse.size > 0,
            dtosComportamentais: totalDtosComportamentais,
            dtosEstruturais: totalDtosEstruturais,
            dtosEstruturaisContratuais: totalDtosEstruturaisContratuais,
            modelosComportamentais: totalModelosComportamentais,
            modelosEstruturais: totalModelosEstruturais,
            modelosEstruturaisContratuais: totalModelosEstruturaisContratuais,
            outrosComportamentais: totalOutrosComportamentais,
            outrosEstruturais: totalOutrosEstruturais,
            outrosEstruturaisContratuais: totalOutrosEstruturaisContratuais
        },
        categorias: relatorio
    };
}

function gerarMarkdown(dados: RelatorioTestes): string {
    const estatisticas = dados.estatisticas;
    const dataFormatada = new Date(dados.geradoEm).toLocaleString('pt-BR');
    const linhas = [
        '# Relatorio de Cobertura de Testes Unitarios (Backend)\n',
        `**Data:** ${dataFormatada}`,
        `**Total de Classes:** ${estatisticas.totalClasses}`,
        `**Com Teste Dedicado:** ${estatisticas.classesComTesteDedicado}`,
        `**Com Cobertura Indireta:** ${estatisticas.classesComCoberturaIndireta}`,
        `**Sem Evidencia no Escopo do JaCoCo:** ${estatisticas.classesSemEvidenciaNoEscopo}`,
        `**Fora do Escopo do JaCoCo:** ${estatisticas.classesForaEscopoJacoco}`,
        `**Ruido Ignorado no Backlog:** ${estatisticas.classesRuidoIgnorado}`,
        `**Sem Teste Dedicado:** ${estatisticas.classesSemTesteDedicado}`,
        `**Correspondencia Direta (Arquivos):** ${estatisticas.coberturaArquivosPercentual.toFixed(2)}%`,
        `**Correspondencia Direta no Backlog Real:** ${estatisticas.coberturaBacklogRealPercentual.toFixed(2)}%`,
        `**Cobertura Observada (Teste Dedicado + Indireta):** ${estatisticas.coberturaObservadaPercentual.toFixed(2)}%`
    ];

    if (estatisticas.correspondenciasAmbiguas > 0) {
        linhas.push(`**Aviso:** ${estatisticas.correspondenciasAmbiguas} classe(s) foram marcadas como cobertas apenas por nome de teste em outro pacote.`);
    }

    if (!estatisticas.jacocoDisponivel) {
        linhas.push('**Aviso:** relatório JaCoCo indisponível; a coluna de cobertura indireta não foi calculada.');
    }

    linhas.push('\n## Detalhamento por Categoria\n');

    [...CATEGORIAS_PRIORITARIAS, ...CATEGORIAS_SECUNDARIAS].forEach(categoria => {
        const itens = dados.categorias[categoria];
        const total = itens.comTeste.length + itens.semTeste.length;
        if (total === 0) {
            return;
        }

        const totalRelevanteCategoria = categoria === 'dtos'
            ? itens.comTeste.length + itens.semTeste.filter(item => !item.ruidoDtoIgnorado).length
            : (categoria === 'modelos'
                ? itens.comTeste.length + itens.semTeste.filter(item => !item.ruidoModeloIgnorado).length
                : (categoria === 'outros'
                    ? itens.comTeste.length + itens.semTeste.filter(item => !item.ruidoOutroIgnorado).length
                    : total));
        linhas.push(`### ${categoria} (${itens.comTeste.length}/${totalRelevanteCategoria} testados${categoria === 'dtos' || categoria === 'modelos' || categoria === 'outros' ? ' no backlog real' : ''})`);
        if (itens.semTeste.length > 0) {
            const candidatos = categoria === 'dtos'
                ? itens.semTeste.filter(item => !item.ruidoDtoIgnorado)
                : (categoria === 'modelos'
                    ? itens.semTeste.filter(item => !item.ruidoModeloIgnorado)
                    : (categoria === 'outros'
                        ? itens.semTeste.filter(item => !item.ruidoOutroIgnorado)
                        : itens.semTeste));
            const dtoRuido = categoria === 'dtos'
                ? itens.semTeste.filter(item => item.ruidoDtoIgnorado)
                : [];
            const ruidoModelo = categoria === 'modelos'
                ? itens.semTeste.filter(item => item.ruidoModeloIgnorado)
                : [];
            const ruidoOutro = categoria === 'outros'
                ? itens.semTeste.filter(item => item.ruidoOutroIgnorado)
                : [];
            const indiretos = candidatos.filter(item => item.cobertaSomenteIndiretamente);
            const foraEscopo = candidatos.filter(item => item.foraEscopoJacoco);
            const semEvidencia = candidatos.filter(item => !item.cobertaSomenteIndiretamente && !item.foraEscopoJacoco);

            linhas.push(`**Faltando Testes Dedicados (${candidatos.length}):**`);
            if (indiretos.length > 0) {
                linhas.push(`Cobertos apenas indiretamente (${indiretos.length}):`);
                indiretos
                    .slice()
                    .toSorted((a, b) => a.caminhoRelativo.localeCompare(b.caminhoRelativo, 'pt-BR'))
                    .forEach(item => linhas.push(`- \`${item.caminhoRelativo}\` (${item.cobertura?.coberturaLinhasPercentual.toFixed(2)}% linhas)`));
            }
            if (foraEscopo.length > 0) {
                linhas.push(`Fora do escopo do JaCoCo (${foraEscopo.length}):`);
                foraEscopo
                    .slice()
                    .toSorted((a, b) => a.caminhoRelativo.localeCompare(b.caminhoRelativo, 'pt-BR'))
                    .forEach(item => linhas.push(`- \`${item.caminhoRelativo}\``));
            }
            if (semEvidencia.length > 0) {
                linhas.push(`Sem evidencia de cobertura no escopo (${semEvidencia.length}):`);
                semEvidencia
                    .slice()
                    .toSorted((a, b) => a.caminhoRelativo.localeCompare(b.caminhoRelativo, 'pt-BR'))
                    .forEach(item => linhas.push(`- \`${item.caminhoRelativo}\``));
            }
            if (dtoRuido.length > 0) {
                linhas.push(`Ignorados como DTO estrutural/contratual (${dtoRuido.length}):`);
                dtoRuido
                    .slice()
                    .toSorted((a, b) => a.caminhoRelativo.localeCompare(b.caminhoRelativo, 'pt-BR'))
                    .forEach(item => linhas.push(`- \`${item.caminhoRelativo}\` (${item.perfilDto})`));
            }
            if (ruidoModelo.length > 0) {
                linhas.push(`Ignorados como modelo estrutural/contratual (${ruidoModelo.length}):`);
                ruidoModelo
                    .slice()
                    .toSorted((a, b) => a.caminhoRelativo.localeCompare(b.caminhoRelativo, 'pt-BR'))
                    .forEach(item => linhas.push(`- \`${item.caminhoRelativo}\` (${item.perfilModelo})`));
            }
            if (ruidoOutro.length > 0) {
                linhas.push(`Ignorados como outro estrutural/contratual (${ruidoOutro.length}):`);
                ruidoOutro
                    .slice()
                    .toSorted((a, b) => a.caminhoRelativo.localeCompare(b.caminhoRelativo, 'pt-BR'))
                    .forEach(item => linhas.push(`- \`${item.caminhoRelativo}\` (${item.perfilOutro})`));
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
    escreverLinha(`Resumo: ${estatisticas.classesComTesteDedicado}/${estatisticas.totalClasses} classes com teste dedicado (${estatisticas.coberturaArquivosPercentual.toFixed(2)}%).`);
    escreverLinha(`- Cobertura indireta: ${estatisticas.classesComCoberturaIndireta}`);
    escreverLinha(`- Sem evidencia no escopo: ${estatisticas.classesSemEvidenciaNoEscopo}`);
    escreverLinha(`- Fora do escopo do JaCoCo: ${estatisticas.classesForaEscopoJacoco}`);
    escreverLinha(`- Ruido ignorado no backlog: ${estatisticas.classesRuidoIgnorado}`);
    escreverLinha(`- Backlog real coberto por teste dedicado: ${estatisticas.coberturaBacklogRealPercentual.toFixed(2)}%`);
    if (estatisticas.jacocoDisponivel) {
        escreverLinha(`- Cobertura observada: ${estatisticas.coberturaObservadaPercentual.toFixed(2)}%`);
    } else {
        escreverLinha('- Cobertura observada: indisponivel (JaCoCo ausente)');
    }

    [...CATEGORIAS_PRIORITARIAS, ...CATEGORIAS_SECUNDARIAS].forEach(categoria => {
        const itens = categorias[categoria];
        const total = itens.comTeste.length + itens.semTeste.length;
        if (total === 0) {
            return;
        }
        if (categoria === 'dtos') {
            const totalRelevante = itens.comTeste.length + itens.semTeste.filter(item => !item.ruidoDtoIgnorado).length;
            const ignorados = itens.semTeste.filter(item => item.ruidoDtoIgnorado).length;
            escreverLinha(`- ${categoria}: ${itens.comTeste.length}/${totalRelevante} testados no backlog real (${ignorados} ignorados)`);
            return;
        }
        if (categoria === 'modelos') {
            const totalRelevante = itens.comTeste.length + itens.semTeste.filter(item => !item.ruidoModeloIgnorado).length;
            const ignorados = itens.semTeste.filter(item => item.ruidoModeloIgnorado).length;
            escreverLinha(`- ${categoria}: ${itens.comTeste.length}/${totalRelevante} testados no backlog real (${ignorados} ignorados)`);
            return;
        }
        if (categoria === 'outros') {
            const totalRelevante = itens.comTeste.length + itens.semTeste.filter(item => !item.ruidoOutroIgnorado).length;
            const ignorados = itens.semTeste.filter(item => item.ruidoOutroIgnorado).length;
            escreverLinha(`- ${categoria}: ${itens.comTeste.length}/${totalRelevante} testados no backlog real (${ignorados} ignorados)`);
            return;
        }
        escreverLinha(`- ${categoria}: ${itens.comTeste.length}/${total} testados`);
    });

    if (estatisticas.correspondenciasAmbiguas > 0) {
        escreverLinha(`- Correspondencias ambiguas: ${estatisticas.correspondenciasAmbiguas}`);
    }
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const opcoes = lerArgumentos(validarArgumentosEntradaDireta(import.meta.url, argumentos));
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
    if (opcoes.emitirJson) {
        imprimirJson(dados);
    } else {
        imprimirResumoConsole(dados);
    }

    if (!opcoes.gravar) {
        return;
    }

    gravarArquivo(opcoes.saida, gerarMarkdown(dados));
    gravarArquivo(caminhoSaidaJson, JSON.stringify(dados, null, 2));
    const escreverStatus = opcoes.emitirJson ? escreverErro : escreverLinha;
    escreverStatus(`Relatorio Markdown gerado em: ${opcoes.saida}`);
    escreverStatus(`Relatorio JSON gerado em: ${caminhoSaidaJson}`);
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverErro(`Erro ao analisar testes: ${mensagem}`);
        process.exitCode = 1;
    });
}

export {
    principal,
    type RelatorioTestes
};
