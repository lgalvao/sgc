import fs from "node:fs/promises";
import path from "node:path";
import ts from "typescript";
import {DIRETORIO_RAIZ, resolverNaRaiz} from "../lib/caminhos.js";

const VERSAO_SCHEMA = "2.0.0";
const DIRETORIO_SAIDA_PADRAO = resolverNaRaiz("etc", "qualidade", "frontend-arquitetura", "latest");
const CAMINHO_SNAPSHOT_PADRAO = path.join(DIRETORIO_SAIDA_PADRAO, "ultimo-snapshot.json");
const CAMINHO_RESUMO_PADRAO = path.join(DIRETORIO_SAIDA_PADRAO, "ultimo-resumo.md");
const EXTENSOES_SUPORTADAS = new Set([".ts", ".vue"]);
const EXTENSOES_RESOLUCAO = [".ts", ".vue", ".js", "/index.ts", "/index.vue", "/index.js"];
const CATEGORIAS_ACOPLAMENTO = ["store", "composable", "service", "router"];
const HUBS_CENTRAIS = new Set([
    "frontend/src/stores/perfil.ts",
    "frontend/src/stores/unidade.ts",
    "frontend/src/stores/mapas.ts",
    "frontend/src/composables/useInvalidacaoNavegacao.ts",
    "frontend/src/composables/useCacheSync.ts",
]);
const NOMES_BOLSAS_LARGAS = /(Dependencias|Estado|Contexto)(?:[A-Z][A-Za-z0-9_]*)?$/;
const NOMES_CHAMADAS_ESTRATEGIA = /^(invalidar|recarregar|sincronizar|marcar[A-Z].*Atualizacao|dados[A-Z].*Validos|limparContextoAtual|resetar)$/;
const NOMES_CHAMADAS_INVALIDACAO = /^(invalidar|marcar[A-Z].*Atualizacao|limparContextoAtual|resetar)$/;
const NOMES_ESTADO_ASSINCRONO_MANUAL = /^(carregando|carregado|ultimoErro|erro|promessa|promessas|carregamentoInicialConcluido|dados[A-Z].*Validos|contexto[A-Z].*Invalido)$/;
const NOMES_COORDENACAO_SERVER_STATE_CASEIRO = /^(garantirDados|dados[A-Z].*Validos|recarregarContexto[A-Z].*|carregarContexto[A-Z].*|carregarDados[A-Z].*|sincronizar[A-Z].*|marcar[A-Z].*Atualizacao|invalidar|resetar)$/;
const CHAMADAS_COLADA_QUERY = new Set(["useQuery", "useMutation", "useInfiniteQuery"]);
const CHAMADAS_COLADA_CACHE = new Set(["useQueryCache"]);

const PADROES = {
    acessoDiretoCache: /\.\s*cache[A-Z][A-Za-z0-9_]*/g,
    metodoEmCache: /\b(?:tem|obter|reaplicar)[A-Za-z0-9_]*EmCache\s*\(/g,
    invalidacaoExplicita: /\.\s*invalidar[A-Za-z0-9_]*\s*\(/g,
    booleanoPosicional: /\b[A-Za-z_$][\w$]*\s*\([^)\n]*,\s*(?:true|false)\s*(?:[,)\n])/g,
    palavraForcar: /\bforcar\b/g,
    palavraStale: /\bstale\b/g,
    palavraSnapshot: /\bsnapshot\b/g,
};

function normalizarCaminho(caminhoArquivo) {
    return caminhoArquivo.split(path.sep).join("/");
}

function ehArquivoTesteOuStory(caminhoRelativo) {
    return caminhoRelativo.includes("/__tests__/")
        || caminhoRelativo.includes("/__mocks__/")
        || caminhoRelativo.includes("/src/test/")
        || caminhoRelativo.includes("/test-utils/")
        || caminhoRelativo.endsWith(".spec.ts")
        || caminhoRelativo.endsWith(".test.ts")
        || caminhoRelativo.endsWith(".stories.ts");
}

function ehArquivoProducaoFrontend(caminhoRelativo) {
    return caminhoRelativo.startsWith("frontend/src/") && !ehArquivoTesteOuStory(caminhoRelativo);
}

function classificarCamada(caminhoRelativo) {
    if (caminhoRelativo.startsWith("frontend/src/views/")) {
        // Apenas arquivos .vue são views de verdade; helpers .ts co-localizados são composables
        return caminhoRelativo.endsWith(".vue") ? "view" : "composable";
    }
    if (caminhoRelativo.startsWith("frontend/src/stores/")) return "store";
    if (caminhoRelativo.startsWith("frontend/src/composables/")) return "composable";
    if (caminhoRelativo.startsWith("frontend/src/components/")) return "component";
    if (caminhoRelativo.startsWith("frontend/src/services/")) return "service";
    if (caminhoRelativo.startsWith("frontend/src/router/")) return "router";
    return "outro";
}

function contarOcorrencias(conteudo, regex) {
    return conteudo.match(regex)?.length ?? 0;
}

async function listarArquivosFrontend(base) {
    const diretorioFrontend = path.join(base, "frontend", "src");
    const arquivos = [];

    async function percorrer(diretorioAtual) {
        const entradas = await fs.readdir(diretorioAtual, {withFileTypes: true}).catch(() => []);
        for (const entrada of entradas) {
            const caminhoCompleto = path.join(diretorioAtual, entrada.name);
            const caminhoRelativo = normalizarCaminho(path.relative(base, caminhoCompleto));

            if (caminhoRelativo.includes("/node_modules/")
                || caminhoRelativo.includes("/dist/")
                || caminhoRelativo.includes("/coverage/")) {
                continue;
            }

            if (entrada.isDirectory()) {
                await percorrer(caminhoCompleto);
                continue;
            }

            if (EXTENSOES_SUPORTADAS.has(path.extname(entrada.name))) {
                arquivos.push(caminhoCompleto);
            }
        }
    }

    await percorrer(diretorioFrontend);
    return arquivos;
}

function extrairCodigoScript(caminhoRelativo, conteudoOriginal) {
    if (!caminhoRelativo.endsWith(".vue")) {
        return conteudoOriginal;
    }

    const blocos = [...conteudoOriginal.matchAll(/<script\b[^>]*>([\s\S]*?)<\/script>/gi)]
        .map((match) => match[1]?.trim() ?? "")
        .filter(Boolean);

    return blocos.join("\n\n");
}

function resolverImportacao(caminhoRelativo, especificador) {
    if (especificador.startsWith("@/")) {
        return normalizarCaminho(path.posix.join("frontend/src", especificador.slice(2)));
    }

    if (!especificador.startsWith(".")) {
        return null;
    }

    const baseDiretorio = path.posix.dirname(caminhoRelativo);
    const caminhoBase = path.posix.normalize(path.posix.join(baseDiretorio, especificador));
    for (const extensao of EXTENSOES_RESOLUCAO) {
        const candidato = normalizarCaminho(`${caminhoBase}${extensao}`);
        if (ehArquivoProducaoFrontend(candidato) || candidato.startsWith("frontend/src/")) {
            return candidato;
        }
    }

    return normalizarCaminho(caminhoBase);
}

function classificarImportacaoResolvida(caminhoImportado) {
    if (!caminhoImportado) return "externo";
    if (caminhoImportado.startsWith("frontend/src/stores/")) return "store";
    if (caminhoImportado.startsWith("frontend/src/composables/")) return "composable";
    if (caminhoImportado.startsWith("frontend/src/services/")) return "service";
    if (caminhoImportado.startsWith("frontend/src/router/")) return "router";
    if (caminhoImportado.startsWith("frontend/src/views/")) return "view";
    if (caminhoImportado.startsWith("frontend/src/components/")) return "component";
    return "outro";
}

function criarAnaliseAst() {
    return {
        importsPorCategoria: {
            store: new Set(),
            composable: new Set(),
            service: new Set(),
            router: new Set(),
            view: new Set(),
            component: new Set(),
            outro: new Set(),
        },
        aliasesServicosNamespace: new Set(),
        aliasesServicosNomeados: new Set(),
        aliasesColadaNomeados: new Set(),
        variaveisStore: new Set(),
        membrosStoreDesestruturados: new Set(),
        chamadasStore: 0,
        chamadasServiceDiretas: 0,
        chamadasEstrategiaCache: 0,
        chamadasInvalidacao: 0,
        chamadasColadaQuery: 0,
        chamadasColadaCache: 0,
        estadosAssincronosManuais: 0,
        coordenacaoServerStateCaseiro: 0,
        mapasPromessasDedupe: 0,
        bolsasDependenciasLargas: 0,
        superficieExportadaAmpla: 0,
    };
}

function obterNomeChamada(expressao) {
    if (ts.isIdentifier(expressao)) {
        return expressao.text;
    }
    if (ts.isPropertyAccessExpression(expressao)) {
        return expressao.name.text;
    }
    return null;
}

function adicionarSet(destino, valor) {
    if (valor) {
        destino.add(valor);
    }
}

function contarMembrosBolsa(noTipo) {
    if (ts.isTypeLiteralNode(noTipo)) {
        return noTipo.members.length;
    }
    return 0;
}

function analisarSuperficieExportada(no, camada, analiseAst, caminhoRelativo) {
    if (!ts.isReturnStatement(no) || !no.expression || !ts.isObjectLiteralExpression(no.expression)) {
        return;
    }

    // Composables de Tela e de Orquestracao são contratos de tela por design; superfície ampla é intencional
    const ehContratoDeTela = camada === "composable"
        && /(Tela|Orquestracao)\.ts$/.test(caminhoRelativo ?? "");
    if (ehContratoDeTela) return;

    const totalPropriedades = no.expression.properties.length;
    const limite = camada === "store" ? 10 : 8;
    if (totalPropriedades > limite) {
        analiseAst.superficieExportadaAmpla += 1;
    }
}

function analisarArquivoAst(caminhoRelativo, conteudoOriginal, camada) {
    const codigo = extrairCodigoScript(caminhoRelativo, conteudoOriginal);
    const analiseAst = criarAnaliseAst();

    if (!codigo.trim()) {
        return analiseAst;
    }

    const arquivo = ts.createSourceFile(caminhoRelativo, codigo, ts.ScriptTarget.Latest, true, ts.ScriptKind.TS);

    function visitar(no) {
        if (ts.isImportDeclaration(no) && ts.isStringLiteral(no.moduleSpecifier)) {
            const especificador = no.moduleSpecifier.text;
            const resolvido = resolverImportacao(caminhoRelativo, especificador);
            const categoria = classificarImportacaoResolvida(resolvido);
            adicionarSet(analiseAst.importsPorCategoria[categoria] ?? analiseAst.importsPorCategoria.outro, resolvido ?? especificador);

            const clausula = no.importClause;
            if (categoria === "service" && clausula?.namedBindings) {
                if (ts.isNamespaceImport(clausula.namedBindings)) {
                    analiseAst.aliasesServicosNamespace.add(clausula.namedBindings.name.text);
                }
                if (ts.isNamedImports(clausula.namedBindings)) {
                    clausula.namedBindings.elements.forEach((elemento) => {
                        analiseAst.aliasesServicosNomeados.add(elemento.name.text);
                    });
                }
            }
            if (especificador === "@pinia/colada" && clausula?.namedBindings && ts.isNamedImports(clausula.namedBindings)) {
                clausula.namedBindings.elements.forEach((elemento) => {
                    analiseAst.aliasesColadaNomeados.add(elemento.name.text);
                });
            }
        }

        if (ts.isInterfaceDeclaration(no) && NOMES_BOLSAS_LARGAS.test(no.name.text) && no.members.length > 5) {
            analiseAst.bolsasDependenciasLargas += 1;
        }

        if (ts.isTypeAliasDeclaration(no) && NOMES_BOLSAS_LARGAS.test(no.name.text)) {
            const totalMembros = contarMembrosBolsa(no.type);
            if (totalMembros > 5) {
                analiseAst.bolsasDependenciasLargas += 1;
            }
        }

        if ((ts.isFunctionDeclaration(no) || ts.isArrowFunction(no) || ts.isFunctionExpression(no)) && no.parameters) {
            no.parameters.forEach((parametro) => {
                if (!ts.isIdentifier(parametro.name)) {
                    return;
                }
                if (!["dependencias", "estado", "contexto"].includes(parametro.name.text)) {
                    return;
                }
                const totalMembros = parametro.type ? contarMembrosBolsa(parametro.type) : 0;
                if (totalMembros > 5) {
                    analiseAst.bolsasDependenciasLargas += 1;
                }
            });
        }

        if (ts.isVariableDeclaration(no) && no.initializer && ts.isCallExpression(no.initializer)) {
            const nomeChamada = obterNomeChamada(no.initializer.expression);
            if (nomeChamada && /^use[A-Z].*Store$/.test(nomeChamada)) {
                if (ts.isIdentifier(no.name)) {
                    analiseAst.variaveisStore.add(no.name.text);
                }
                if (ts.isObjectBindingPattern(no.name)) {
                    no.name.elements.forEach((elemento) => {
                        if (ts.isIdentifier(elemento.name)) {
                            analiseAst.membrosStoreDesestruturados.add(elemento.name.text);
                        }
                    });
                }
            }
        }

        if (ts.isVariableDeclaration(no) && ts.isIdentifier(no.name) && NOMES_ESTADO_ASSINCRONO_MANUAL.test(no.name.text)) {
            if (
                (no.initializer && ts.isCallExpression(no.initializer) && ["ref", "computed"].includes(obterNomeChamada(no.initializer.expression) ?? ""))
                || (no.initializer && ts.isNewExpression(no.initializer) && ts.isIdentifier(no.initializer.expression) && no.initializer.expression.text === "Map")
            ) {
                analiseAst.estadosAssincronosManuais += 1;
            }
        }

        if (ts.isVariableDeclaration(no) && ts.isIdentifier(no.name) && /promessa|promessas/i.test(no.name.text)) {
            if (no.initializer && ts.isNewExpression(no.initializer) && ts.isIdentifier(no.initializer.expression) && no.initializer.expression.text === "Map") {
                analiseAst.mapasPromessasDedupe += 1;
            }
        }

        if ((ts.isFunctionDeclaration(no) || ts.isMethodDeclaration(no)) && no.name && ts.isIdentifier(no.name)) {
            if (NOMES_COORDENACAO_SERVER_STATE_CASEIRO.test(no.name.text)) {
                analiseAst.coordenacaoServerStateCaseiro += 1;
            }
        }

        if (ts.isPropertyAccessExpression(no) && ts.isIdentifier(no.expression)) {
            const alvo = no.expression.text;
            const propriedade = no.name.text;

            if (analiseAst.variaveisStore.has(alvo)) {
                analiseAst.chamadasStore += 1;
                if (NOMES_CHAMADAS_ESTRATEGIA.test(propriedade)) {
                    analiseAst.chamadasEstrategiaCache += 1;
                }
                if (NOMES_CHAMADAS_INVALIDACAO.test(propriedade)) {
                    analiseAst.chamadasInvalidacao += 1;
                }
            }

            if (analiseAst.aliasesServicosNamespace.has(alvo) && ts.isCallExpression(no.parent) && no.parent.expression === no) {
                analiseAst.chamadasServiceDiretas += 1;
            }
        }

        if (ts.isCallExpression(no)) {
            const nomeChamada = obterNomeChamada(no.expression);
            if (nomeChamada && analiseAst.membrosStoreDesestruturados.has(nomeChamada)) {
                analiseAst.chamadasStore += 1;
                if (NOMES_CHAMADAS_ESTRATEGIA.test(nomeChamada)) {
                    analiseAst.chamadasEstrategiaCache += 1;
                }
                if (NOMES_CHAMADAS_INVALIDACAO.test(nomeChamada)) {
                    analiseAst.chamadasInvalidacao += 1;
                }
            }
            if (nomeChamada && analiseAst.aliasesServicosNomeados.has(nomeChamada)) {
                analiseAst.chamadasServiceDiretas += 1;
            }
            if (nomeChamada && analiseAst.aliasesColadaNomeados.has(nomeChamada)) {
                if (CHAMADAS_COLADA_QUERY.has(nomeChamada)) {
                    analiseAst.chamadasColadaQuery += 1;
                }
                if (CHAMADAS_COLADA_CACHE.has(nomeChamada)) {
                    analiseAst.chamadasColadaCache += 1;
                }
            }
        }

        analisarSuperficieExportada(no, camada, analiseAst, caminhoRelativo);
        ts.forEachChild(no, visitar);
    }

    visitar(arquivo);
    return analiseAst;
}

function computarSinaisLexicais(conteudo) {
    return {
        acessoDiretoCache: contarOcorrencias(conteudo, PADROES.acessoDiretoCache),
        metodoEmCache: contarOcorrencias(conteudo, PADROES.metodoEmCache),
        invalidacaoExplicita: contarOcorrencias(conteudo, PADROES.invalidacaoExplicita),
        booleanoPosicional: contarOcorrencias(conteudo, PADROES.booleanoPosicional),
        palavraForcar: contarOcorrencias(conteudo, PADROES.palavraForcar),
        palavraStale: contarOcorrencias(conteudo, PADROES.palavraStale),
        palavraSnapshot: contarOcorrencias(conteudo, PADROES.palavraSnapshot),
    };
}

function contarCategoriasAcoplamento(importsPorCategoria) {
    return CATEGORIAS_ACOPLAMENTO.filter((categoria) => importsPorCategoria[categoria].size > 0).length;
}

function contarImportacoesArquiteturais(importsPorCategoria) {
    return CATEGORIAS_ACOPLAMENTO.reduce((total, categoria) => total + importsPorCategoria[categoria].size, 0);
}

function pesoServiceDireto(camada) {
    if (camada === "view") return 10;
    if (camada === "component") return 6;
    if (camada === "store") return 4;
    return 0;
}

function detectarServerStateCaseiro({camada, analiseAst}) {
    const usaColadaServerState = analiseAst.chamadasColadaQuery > 0;
    const coordenaServerStateNaMao = analiseAst.estadosAssincronosManuais >= 2
        || analiseAst.coordenacaoServerStateCaseiro >= 2
        || analiseAst.mapasPromessasDedupe > 0
        || analiseAst.chamadasEstrategiaCache >= 2
        || analiseAst.chamadasInvalidacao >= 1;

    if (usaColadaServerState) {
        return false;
    }

    if (camada === "view") {
        return analiseAst.chamadasServiceDiretas > 0 && coordenaServerStateNaMao;
    }

    if (camada === "store" || camada === "composable") {
        return analiseAst.chamadasServiceDiretas > 0 && coordenaServerStateNaMao;
    }

    return false;
}

function calcularScoreArquivo({camada, sinaisLexicais, analiseAst, hubCentral}) {
    let total = 0;
    total += (sinaisLexicais.acessoDiretoCache * 8)
        + (sinaisLexicais.metodoEmCache * 6)
        + (sinaisLexicais.booleanoPosicional * 4)
        + (sinaisLexicais.palavraForcar * 3)
        + (sinaisLexicais.palavraStale * 3)
        + (sinaisLexicais.palavraSnapshot * 2);

    // Penalidades de coordenação de cache: isentas em hubs centrais (cuja função É exatamente essa)
    if (!hubCentral) {
        total += sinaisLexicais.invalidacaoExplicita * 5;
        total += analiseAst.chamadasEstrategiaCache * (camada === "view" ? 8 : 5);
        total += analiseAst.chamadasInvalidacao * (camada === "view" ? 10 : 6);
        if (analiseAst.chamadasStore >= 8) {
            total += 8 + (Math.floor((analiseAst.chamadasStore - 8) / 4) * 3);
        }
    }

    const categoriasAcoplamento = contarCategoriasAcoplamento(analiseAst.importsPorCategoria);
    const importacoesArquiteturais = contarImportacoesArquiteturais(analiseAst.importsPorCategoria);

    // Mistura de camadas e fan-out: só problemáticos em views e componentes
    // Composables podem e devem importar de múltiplas camadas arquiteturais
    if (camada === "view" || camada === "component") {
        if (categoriasAcoplamento >= 3) {
            total += 10 + ((categoriasAcoplamento - 3) * 4);
        }
        if (importacoesArquiteturais >= 5) {
            total += (importacoesArquiteturais - 4) * 3;
        }
    }

    total += analiseAst.chamadasServiceDiretas * pesoServiceDireto(camada);
    if (detectarServerStateCaseiro({camada, analiseAst})) {
        total += camada === "view" ? 18 : 14;
    }
    total += analiseAst.bolsasDependenciasLargas * 9;
    if (camada === "store" || camada === "composable" || camada === "view" || camada === "component") {
        total += analiseAst.superficieExportadaAmpla * 9;
    }

    return total;
}

function calcularFaixa(score) {
    if (score === 0) return "excelente";
    if (score <= 15) return "bom";
    if (score <= 50) return "atencao";
    return "critico";
}

function criarResumoMarkdown(snapshot) {
    const linhas = [
        "# Auditoria Arquitetural do Frontend",
        "",
        `- Score total: **${snapshot.resumo.scoreTotal}** (${snapshot.resumo.faixa})`,
        `- Arquivos de producao: **${snapshot.resumo.arquivosProducao}**`,
        `- Views com vazamento de estrategia de cache: **${snapshot.resumo.metricas.viewsComVazamentoCache}**`,
        `- Views com chamadas diretas a service: **${snapshot.resumo.metricas.viewsComServiceDireto}**`,
        `- Views com server state caseiro: **${snapshot.resumo.metricas.viewsComServerStateCaseiro}**`,
        `- Views com fan-out arquitetural alto: **${snapshot.resumo.metricas.viewsComFanoutAlto}**`,
        `- Acessos diretos a cache de store: **${snapshot.resumo.metricas.acessosDiretosCache}**`,
        `- Chamadas com booleano posicional: **${snapshot.resumo.metricas.booleanosPosicionais}**`,
        `- Bolsas de dependencias/estado largas: **${snapshot.resumo.metricas.arquivosComBolsaDependenciasLarga}**`,
        `- Superficies exportadas amplas: **${snapshot.resumo.metricas.arquivosComSuperficieAmpla}**`,
        `- Arquivos com mistura de camadas arquiteturais: **${snapshot.resumo.metricas.arquivosComMisturaCamadas}**`,
        `- Arquivos com server state caseiro: **${snapshot.resumo.metricas.arquivosComServerStateCaseiro}**`,
        `- Hubs centrais com sinais: **${snapshot.resumo.metricas.hubsCentraisComSinais}**`,
        "",
        "## Hotspots",
        "",
    ];

    if (snapshot.hotspots.length === 0) {
        linhas.push("Nenhum hotspot arquitetural detectado.");
    } else {
        snapshot.hotspots.slice(0, 10).forEach((hotspot, indice) => {
            linhas.push(`${indice + 1}. \`${hotspot.arquivo}\` [${hotspot.camada}]`);
            linhas.push(`   - score: ${hotspot.score}`);
            linhas.push(`   - sinais: ${hotspot.sinaisAtivos.join(", ")}`);
            linhas.push(`   - fan-out: ${hotspot.metricasAst.categoriasAcoplamento} categorias / ${hotspot.metricasAst.importacoesArquiteturais} imports arquiteturais`);
        });
    }

    linhas.push("");
    linhas.push("## Diretrizes acompanhadas");
    linhas.push("");
    linhas.push("- views nao devem conhecer estrategia de cache;");
    linhas.push("- views nao devem chamar services diretamente quando existir borda de dominio/coordenacao;");
    linhas.push("- contratos de view devem ser orientados a caso de uso;");
    linhas.push("- evitar bolsas largas de `dependencias` e `estado`;");
    linhas.push("- reduzir hubs centrais antes de expandir APIs locais.");
    linhas.push("");

    return `${linhas.join("\n")}\n`;
}

async function gravarSnapshotArquitetura(snapshot, diretorioSaida = DIRETORIO_SAIDA_PADRAO) {
    await fs.mkdir(diretorioSaida, {recursive: true});
    await fs.writeFile(path.join(diretorioSaida, path.basename(CAMINHO_SNAPSHOT_PADRAO)), JSON.stringify(snapshot, null, 2));
    await fs.writeFile(path.join(diretorioSaida, path.basename(CAMINHO_RESUMO_PADRAO)), criarResumoMarkdown(snapshot));
}

function criarMetricasResumo() {
    return {
        viewsComVazamentoCache: 0,
        viewsComServiceDireto: 0,
        viewsComServerStateCaseiro: 0,
        viewsComFanoutAlto: 0,
        acessosDiretosCache: 0,
        metodosEmCache: 0,
        invalidacoesExplicitasEmViews: 0,
        booleanosPosicionais: 0,
        ocorrenciasForcar: 0,
        ocorrenciasStale: 0,
        ocorrenciasSnapshot: 0,
        arquivosComBolsaDependenciasLarga: 0,
        arquivosComSuperficieAmpla: 0,
        arquivosComMisturaCamadas: 0,
        arquivosComServerStateCaseiro: 0,
        arquivosComAcoplamentoStoreAlto: 0,
        arquivosComServiceDireto: 0,
        chamadasEstrategiaCache: 0,
        chamadasInvalidacao: 0,
        hubsCentraisComSinais: 0,
    };
}

function obterSinaisAtivos(camada, sinaisLexicais, analiseAst, categoriasAcoplamento, importacoesArquiteturais, hubCentral) {
    const sinais = [];
    for (const [nome, valor] of Object.entries(sinaisLexicais)) {
        // Hubs centrais: invalidacaoExplicita é esperada e correta — não sinalizar
        if (nome === "invalidacaoExplicita" && hubCentral) continue;
        if (valor > 0) {
            sinais.push(nome);
        }
    }
    if (analiseAst.chamadasServiceDiretas > 0) sinais.push("serviceDireto");
    // Sinais de estratégia/invalidação: só relevantes fora de hubs centrais
    if (!hubCentral && analiseAst.chamadasEstrategiaCache > 0) sinais.push("estrategiaCache");
    if (!hubCentral && analiseAst.chamadasInvalidacao > 0) sinais.push("invalidacaoArquitetural");
    if (analiseAst.bolsasDependenciasLargas > 0) sinais.push("bolsaDependenciasLarga");
    if (analiseAst.superficieExportadaAmpla > 0) sinais.push("superficieAmpla");
    if (detectarServerStateCaseiro({camada, analiseAst})) sinais.push("serverStateCaseiro");
    // Mistura de camadas e fan-out: só problemáticos em views e componentes
    if (categoriasAcoplamento >= 3 && (camada === "view" || camada === "component")) sinais.push("misturaCamadas");
    if (importacoesArquiteturais >= 5 && (camada === "view" || camada === "component")) sinais.push("fanoutAlto");
    // Acoplamento alto a stores: só problemático em views e componentes (e fora de hubs)
    if (!hubCentral && analiseAst.chamadasStore >= 8 && (camada === "view" || camada === "component")) sinais.push("acoplamentoStoreAlto");
    return sinais;
}

async function analisarArquiteturaFrontend({base = DIRETORIO_RAIZ} = {}) {
    const baseResolvida = path.resolve(base ?? DIRETORIO_RAIZ);
    const arquivos = await listarArquivosFrontend(baseResolvida);
    const analisados = [];
    const metricas = criarMetricasResumo();

    for (const arquivo of arquivos) {
        const caminhoRelativo = normalizarCaminho(path.relative(baseResolvida, arquivo));
        if (!ehArquivoProducaoFrontend(caminhoRelativo)) {
            continue;
        }

        const conteudo = await fs.readFile(arquivo, "utf8");
        const camada = classificarCamada(caminhoRelativo);
        const sinaisLexicais = computarSinaisLexicais(conteudo);
        const analiseAst = analisarArquivoAst(caminhoRelativo, conteudo, camada);
        const categoriasAcoplamento = contarCategoriasAcoplamento(analiseAst.importsPorCategoria);
        const importacoesArquiteturais = contarImportacoesArquiteturais(analiseAst.importsPorCategoria);
        const hubCentral = HUBS_CENTRAIS.has(caminhoRelativo);
        const score = calcularScoreArquivo({camada, sinaisLexicais, analiseAst, hubCentral});
        const sinaisAtivos = obterSinaisAtivos(camada, sinaisLexicais, analiseAst, categoriasAcoplamento, importacoesArquiteturais, hubCentral);
        const temSinal = sinaisAtivos.length > 0;
        const serverStateCaseiro = detectarServerStateCaseiro({camada, analiseAst});

        const vazamentoCacheView = camada === "view" && (
            sinaisLexicais.acessoDiretoCache > 0
            || sinaisLexicais.metodoEmCache > 0
            || sinaisLexicais.invalidacaoExplicita > 0
            || analiseAst.chamadasEstrategiaCache > 0
            || analiseAst.chamadasInvalidacao > 0
        );

        if (vazamentoCacheView) {
            metricas.viewsComVazamentoCache += 1;
        }
        if (camada === "view" && analiseAst.chamadasServiceDiretas > 0) {
            metricas.viewsComServiceDireto += 1;
        }
        if (camada === "view" && serverStateCaseiro) {
            metricas.viewsComServerStateCaseiro += 1;
        }
        if (camada === "view" && (categoriasAcoplamento >= 3 || importacoesArquiteturais >= 5 || analiseAst.chamadasStore >= 8)) {
            metricas.viewsComFanoutAlto += 1;
        }

        metricas.acessosDiretosCache += sinaisLexicais.acessoDiretoCache;
        metricas.metodosEmCache += sinaisLexicais.metodoEmCache;
        if (camada === "view") {
            metricas.invalidacoesExplicitasEmViews += sinaisLexicais.invalidacaoExplicita;
        }
        metricas.booleanosPosicionais += sinaisLexicais.booleanoPosicional;
        metricas.ocorrenciasForcar += sinaisLexicais.palavraForcar;
        metricas.ocorrenciasStale += sinaisLexicais.palavraStale;
        metricas.ocorrenciasSnapshot += sinaisLexicais.palavraSnapshot;
        metricas.chamadasEstrategiaCache += analiseAst.chamadasEstrategiaCache;
        metricas.chamadasInvalidacao += analiseAst.chamadasInvalidacao;
        if (analiseAst.bolsasDependenciasLargas > 0) {
            metricas.arquivosComBolsaDependenciasLarga += 1;
        }
        if (analiseAst.superficieExportadaAmpla > 0) {
            metricas.arquivosComSuperficieAmpla += 1;
        }
        if (categoriasAcoplamento >= 3 && (camada === "view" || camada === "component")) {
            metricas.arquivosComMisturaCamadas += 1;
        }
        if (serverStateCaseiro) {
            metricas.arquivosComServerStateCaseiro += 1;
        }
        if (!hubCentral && analiseAst.chamadasStore >= 8 && (camada === "view" || camada === "component")) {
            metricas.arquivosComAcoplamentoStoreAlto += 1;
        }
        if (analiseAst.chamadasServiceDiretas > 0) {
            metricas.arquivosComServiceDireto += 1;
        }
        if (hubCentral && temSinal) {
            metricas.hubsCentraisComSinais += 1;
        }

        analisados.push({
            arquivo: caminhoRelativo,
            camada,
            linhas: conteudo.split(/\r?\n/).length,
            score,
            sinaisLexicais,
            metricasAst: {
                chamadasStore: analiseAst.chamadasStore,
                chamadasServiceDiretas: analiseAst.chamadasServiceDiretas,
                chamadasEstrategiaCache: analiseAst.chamadasEstrategiaCache,
                chamadasInvalidacao: analiseAst.chamadasInvalidacao,
                chamadasColadaQuery: analiseAst.chamadasColadaQuery,
                chamadasColadaCache: analiseAst.chamadasColadaCache,
                estadosAssincronosManuais: analiseAst.estadosAssincronosManuais,
                coordenacaoServerStateCaseiro: analiseAst.coordenacaoServerStateCaseiro,
                mapasPromessasDedupe: analiseAst.mapasPromessasDedupe,
                bolsasDependenciasLargas: analiseAst.bolsasDependenciasLargas,
                superficieExportadaAmpla: analiseAst.superficieExportadaAmpla,
                categoriasAcoplamento,
                importacoesArquiteturais,
            },
            sinaisAtivos,
            hubCentral,
        });
    }

    const hotspots = analisados
        .filter((arquivo) => arquivo.score > 0)
        .sort((a, b) => b.score - a.score || b.metricasAst.importacoesArquiteturais - a.metricasAst.importacoesArquiteturais || b.linhas - a.linhas || a.arquivo.localeCompare(b.arquivo));

    const scoreTotal = hotspots.reduce((total, item) => total + item.score, 0);

    return {
        versaoSchema: VERSAO_SCHEMA,
        geradoEm: new Date().toISOString(),
        resumo: {
            arquivosProducao: analisados.length,
            scoreTotal,
            faixa: calcularFaixa(scoreTotal),
            metricas,
        },
        hotspots,
    };
}

export {
    analisarArquiteturaFrontend,
    DIRETORIO_SAIDA_PADRAO,
    gravarSnapshotArquitetura,
};
