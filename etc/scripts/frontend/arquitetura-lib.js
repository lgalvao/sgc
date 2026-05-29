import fs from "node:fs/promises";
import path from "node:path";
import {Node, Project, SyntaxKind, ts} from "ts-morph";
import {DIRETORIO_RAIZ, resolverNaRaiz} from "../lib/caminhos.js";

const VERSAO_SCHEMA = "3.0.0";
const DIRETORIO_SAIDA_PADRAO = resolverNaRaiz("etc", "qualidade", "frontend-arquitetura", "latest");
const CAMINHO_SNAPSHOT_PADRAO = path.join(DIRETORIO_SAIDA_PADRAO, "ultimo-snapshot.json");
const CAMINHO_RESUMO_PADRAO = path.join(DIRETORIO_SAIDA_PADRAO, "ultimo-resumo.md");
const EXTENSOES_SUPORTADAS = new Set([".ts", ".vue"]);
const EXTENSOES_RESOLUCAO = [".ts", ".vue", ".js", "/index.ts", "/index.vue", "/index.js"];
const CATEGORIAS_ACOPLAMENTO = ["store", "composable", "service", "router"];
const HUBS_CENTRAIS = new Set([
    "frontend/src/App.vue",
    "frontend/src/stores/perfil.ts",
    "frontend/src/stores/unidade.ts",
    "frontend/src/stores/mapas.ts",
    "frontend/src/composables/useInvalidacaoNavegacao.ts",
    "frontend/src/composables/useCacheSync.ts",
]);
const NOMES_BOLSAS_LARGAS = /^(Dependencias|Estado|Contexto)(?:[A-Z][A-Za-z0-9_]*)?$/;
const NOMES_CHAMADAS_ESTRATEGIA = /^(invalidar|recarregar|sincronizar|marcar[A-Z].*Atualizacao|dados[A-Z].*Validos|limparContextoAtual|resetar)$/;
const NOMES_CHAMADAS_INVALIDACAO = /^(invalidar|marcar[A-Z].*Atualizacao|limparContextoAtual|resetar)$/;
const NOMES_ESTADO_ASSINCRONO_MANUAL = /^(carregando|carregado|ultimoErro|erro|promessa|promessas|carregamentoInicialConcluido|dados[A-Z].*Validos|contexto[A-Z].*Invalido)$/;
const NOMES_COORDENACAO_SERVER_STATE_CASEIRO = /^(garantirDados|dados[A-Z].*Validos|recarregarContexto[A-Z].*|carregarContexto[A-Z].*|carregarDados[A-Z].*|sincronizar[A-Z].*|marcar[A-Z].*Atualizacao|invalidar|resetar)$/;
const CHAMADAS_COLADA_QUERY = new Set(["useQuery", "useMutation", "useInfiniteQuery"]);
const CHAMADAS_COLADA_CACHE = new Set(["useQueryCache"]);
const PADROES = {
    palavraStale: /\bstale\b/g,
    palavraSnapshot: /\bsnapshot\b/g,
};
const LIMITE_LINHAS_FACHADA_PURA = 25;
const LIMITE_LINHAS_ARQUIVO_MINUSCULO = 30;
const LIMITE_FAMILIA_PULVERIZADA = 4;

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
        usaDefineStore: false,
    };
}

function obterNomeChamada(expressao) {
    if (Node.isIdentifier(expressao)) {
        return expressao.getText();
    }
    if (Node.isPropertyAccessExpression(expressao)) {
        return expressao.getName();
    }
    return null;
}

function adicionarSet(destino, valor) {
    if (valor) {
        destino.add(valor);
    }
}

function contarMembrosBolsa(noTipo) {
    if (noTipo && Node.isTypeLiteral(noTipo)) {
        return noTipo.getMembers().length;
    }
    return 0;
}

function ehFuncaoLike(no) {
    return Node.isFunctionDeclaration(no)
        || Node.isArrowFunction(no)
        || Node.isFunctionExpression(no)
        || Node.isMethodDeclaration(no);
}

function obterProfundidadeFuncao(no) {
    return no.getAncestors().filter(ehFuncaoLike).length;
}

function analisarSuperficieExportada(sourceFile, camada, analiseAst, caminhoRelativo, ehComposableDeView) {
    const retornos = sourceFile.getDescendantsOfKind(SyntaxKind.ReturnStatement);
    for (const retorno of retornos) {
        const expressao = retorno.getExpression();
        if (!expressao || !Node.isObjectLiteralExpression(expressao)) {
            continue;
        }

        if (obterProfundidadeFuncao(retorno) > 1) {
            continue;
        }

        const ehFacadeDeStore = camada === "composable"
            && analiseAst.importsPorCategoria.store.size === 1
            && analiseAst.importsPorCategoria.composable.size === 0
            && analiseAst.importsPorCategoria.service.size === 0;
        if (ehFacadeDeStore) {
            continue;
        }

        const ehContratoDeTela = camada === "composable"
            && (
                /(Tela|Orquestracao|Mutacoes|Form)\.ts$/.test(caminhoRelativo ?? "")
                || /Fluxo/.test(caminhoRelativo ?? "")
            );
        if (ehContratoDeTela || ehComposableDeView) {
            continue;
        }

        const totalPropriedades = expressao.getProperties().length;
        const limite = camada === "store" ? 10 : 8;
        if (totalPropriedades > limite) {
            analiseAst.superficieExportadaAmpla += 1;
        }
    }
}

function analisarBolsasDependencias(sourceFile, analiseAst, ehComposableDeView) {
    if (ehComposableDeView) {
        return;
    }

    for (const declaracao of sourceFile.getInterfaces()) {
        if (NOMES_BOLSAS_LARGAS.test(declaracao.getName()) && declaracao.getMembers().length > 5) {
            analiseAst.bolsasDependenciasLargas += 1;
        }
    }

    for (const declaracao of sourceFile.getTypeAliases()) {
        if (!NOMES_BOLSAS_LARGAS.test(declaracao.getName())) {
            continue;
        }
        if (contarMembrosBolsa(declaracao.getTypeNode()) > 5) {
            analiseAst.bolsasDependenciasLargas += 1;
        }
    }

    for (const funcao of sourceFile.getFunctions()) {
        if (!funcao.getName()?.startsWith("use")) {
            continue;
        }
        for (const parametro of funcao.getParameters()) {
            const tipo = parametro.getTypeNode();
            if (tipo && Node.isTypeLiteral(tipo) && tipo.getMembers().length > 5) {
                analiseAst.bolsasDependenciasLargas += 1;
            }
        }
    }

    for (const declaracao of sourceFile.getVariableDeclarations()) {
        const nome = declaracao.getName();
        if (!nome.startsWith("use")) {
            continue;
        }
        const inicializador = declaracao.getInitializer();
        if (!inicializador || (!Node.isArrowFunction(inicializador) && !Node.isFunctionExpression(inicializador))) {
            continue;
        }
        for (const parametro of inicializador.getParameters()) {
            const tipo = parametro.getTypeNode();
            if (tipo && Node.isTypeLiteral(tipo) && tipo.getMembers().length > 5) {
                analiseAst.bolsasDependenciasLargas += 1;
            }
        }
    }
}

function computarSinaisLexicais(sourceFile, conteudoOriginal) {
    const propertyAccesses = sourceFile.getDescendantsOfKind(SyntaxKind.PropertyAccessExpression);
    const callExpressions = sourceFile.getDescendantsOfKind(SyntaxKind.CallExpression);

    let acessoDiretoCache = 0;
    for (const acesso of propertyAccesses) {
        if (/^cache[A-Z]/.test(acesso.getName())) {
            acessoDiretoCache += 1;
        }
    }

    let metodoEmCache = 0;
    let invalidacaoExplicita = 0;
    let booleanoPosicional = 0;
    for (const chamada of callExpressions) {
        const expressao = chamada.getExpression();
        if (Node.isPropertyAccessExpression(expressao)) {
            const nomeMetodo = expressao.getName();
            if (/EmCache$/.test(nomeMetodo)) {
                metodoEmCache += 1;
            }
            if (/^invalidar/.test(nomeMetodo)) {
                invalidacaoExplicita += 1;
            }
        }

        const argumentos = chamada.getArguments();
        if (argumentos.length < 2) {
            continue;
        }
        for (let indice = 1; indice < argumentos.length; indice += 1) {
            const kind = argumentos[indice].getKind();
            if (kind === SyntaxKind.TrueKeyword || kind === SyntaxKind.FalseKeyword) {
                booleanoPosicional += 1;
                break;
            }
        }
    }

    let palavraForcar = 0;
    sourceFile.getDescendantsOfKind(SyntaxKind.Identifier).forEach((id) => {
        if (id.getText() === "forcar") {
            palavraForcar += 1;
        }
    });

    return {
        acessoDiretoCache,
        metodoEmCache,
        invalidacaoExplicita,
        booleanoPosicional,
        palavraForcar,
        palavraStale: contarOcorrencias(conteudoOriginal, PADROES.palavraStale),
        palavraSnapshot: contarOcorrencias(conteudoOriginal, PADROES.palavraSnapshot),
    };
}

function analisarArquivoAst(project, caminhoRelativo, conteudoOriginal, camada) {
    const codigo = extrairCodigoScript(caminhoRelativo, conteudoOriginal);
    const analiseAst = criarAnaliseAst();

    if (!codigo.trim()) {
        return {analiseAst, sourceFile: null};
    }

    const sourceFile = project.createSourceFile(caminhoRelativo, codigo, {overwrite: true, scriptKind: ts.ScriptKind.TS});
    const ehComposableDeView = camada === "composable"
        && (caminhoRelativo ?? "").startsWith("frontend/src/views/");

    for (const declaracao of sourceFile.getImportDeclarations()) {
        const especificador = declaracao.getModuleSpecifierValue();
        const resolvido = resolverImportacao(caminhoRelativo, especificador);
        const categoria = classificarImportacaoResolvida(resolvido);
        adicionarSet(analiseAst.importsPorCategoria[categoria] ?? analiseAst.importsPorCategoria.outro, resolvido ?? especificador);

        const namespaceImport = declaracao.getNamespaceImport();
        if (categoria === "service" && namespaceImport) {
            analiseAst.aliasesServicosNamespace.add(namespaceImport.getText());
        }

        if (categoria === "service") {
            declaracao.getNamedImports().forEach((elemento) => {
                analiseAst.aliasesServicosNomeados.add(elemento.getAliasNode()?.getText() ?? elemento.getNameNode().getText());
            });
        }

        if (especificador === "@pinia/colada") {
            declaracao.getNamedImports().forEach((elemento) => {
                analiseAst.aliasesColadaNomeados.add(elemento.getAliasNode()?.getText() ?? elemento.getNameNode().getText());
            });
        }
    }

    analisarBolsasDependencias(sourceFile, analiseAst, ehComposableDeView);

    for (const declaracao of sourceFile.getVariableDeclarations()) {
        const nome = declaracao.getNameNode();
        const inicializador = declaracao.getInitializer();

        if (inicializador && Node.isCallExpression(inicializador)) {
            const nomeChamada = obterNomeChamada(inicializador.getExpression());
            if (nomeChamada && /^use[A-Z].*Store$/.test(nomeChamada)) {
                if (Node.isIdentifier(nome)) {
                    analiseAst.variaveisStore.add(nome.getText());
                }
                if (Node.isObjectBindingPattern(nome)) {
                    nome.getElements().forEach((elemento) => {
                        analiseAst.membrosStoreDesestruturados.add(elemento.getNameNode().getText());
                    });
                }
            }
        }

        if (Node.isIdentifier(nome) && NOMES_ESTADO_ASSINCRONO_MANUAL.test(nome.getText())) {
            if (
                (inicializador && Node.isCallExpression(inicializador) && obterNomeChamada(inicializador.getExpression()) === "ref")
                || (inicializador && Node.isNewExpression(inicializador) && Node.isIdentifier(inicializador.getExpression()) && inicializador.getExpression().getText() === "Map")
            ) {
                analiseAst.estadosAssincronosManuais += 1;
            }
        }

        if (Node.isIdentifier(nome) && /promessa|promessas/i.test(nome.getText())) {
            if (inicializador && Node.isNewExpression(inicializador) && Node.isIdentifier(inicializador.getExpression()) && inicializador.getExpression().getText() === "Map") {
                analiseAst.mapasPromessasDedupe += 1;
            }
        }
    }

    for (const funcao of sourceFile.getDescendantsOfKind(SyntaxKind.FunctionDeclaration)) {
        const nome = funcao.getName();
        if (nome && NOMES_COORDENACAO_SERVER_STATE_CASEIRO.test(nome)) {
            analiseAst.coordenacaoServerStateCaseiro += 1;
        }
    }
    for (const metodo of sourceFile.getDescendantsOfKind(SyntaxKind.MethodDeclaration)) {
        const nome = metodo.getName();
        if (nome && NOMES_COORDENACAO_SERVER_STATE_CASEIRO.test(nome)) {
            analiseAst.coordenacaoServerStateCaseiro += 1;
        }
    }

    for (const acesso of sourceFile.getDescendantsOfKind(SyntaxKind.PropertyAccessExpression)) {
        const alvo = acesso.getExpression();
        if (!Node.isIdentifier(alvo)) {
            continue;
        }
        const nomeAlvo = alvo.getText();
        const propriedade = acesso.getName();

        if (analiseAst.variaveisStore.has(nomeAlvo)) {
            analiseAst.chamadasStore += 1;
            if (NOMES_CHAMADAS_ESTRATEGIA.test(propriedade)) {
                analiseAst.chamadasEstrategiaCache += 1;
            }
            if (NOMES_CHAMADAS_INVALIDACAO.test(propriedade)) {
                analiseAst.chamadasInvalidacao += 1;
            }
        }

        if (analiseAst.aliasesServicosNamespace.has(nomeAlvo)) {
            const pai = acesso.getParent();
            if (Node.isCallExpression(pai) && pai.getExpression() === acesso) {
                analiseAst.chamadasServiceDiretas += 1;
            }
        }
    }

    for (const chamada of sourceFile.getDescendantsOfKind(SyntaxKind.CallExpression)) {
        const nomeChamada = obterNomeChamada(chamada.getExpression());
        if (nomeChamada === "defineStore") {
            analiseAst.usaDefineStore = true;
        }
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

    analisarSuperficieExportada(sourceFile, camada, analiseAst, caminhoRelativo, ehComposableDeView);
    return {analiseAst, sourceFile};
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

/**
 * Detecta composables que são fachadas puras: apenas importam e re-exportam outros
 * composables, sem lógica própria (sem ref, computed, calls a services/stores).
 * Usa análise AST positiva — não regex negativa.
 */
function detectarFachadaPura(camada, analiseAst, sourceFile, linhas, caminhoRelativo, hubCentral) {
    if (camada !== "composable" || hubCentral || linhas >= LIMITE_LINHAS_FACHADA_PURA) return false;
    if (!sourceFile) return false;
    if (!caminhoRelativo.startsWith("frontend/src/composables/")) return false;
    if (analiseAst.importsPorCategoria.store.size > 0) return false;
    if (analiseAst.importsPorCategoria.service.size > 0) return false;
    if (analiseAst.importsPorCategoria.composable.size < 2) return false;

    const funcoesExportadas = sourceFile.getFunctions().filter(
        (f) => f.isExported() && f.getName()?.startsWith("use"),
    );
    if (funcoesExportadas.length !== 1) return false;

    const corpo = funcoesExportadas[0].getBody();
    if (!corpo) return false;

    let temReturn = false;
    for (const stmt of corpo.getStatements()) {
        if (Node.isVariableStatement(stmt)) {
            for (const decl of stmt.getDeclarationList().getDeclarations()) {
                const init = decl.getInitializer();
                if (init && Node.isCallExpression(init)) {
                    const nome = obterNomeChamada(init.getExpression());
                    if (!nome?.startsWith("use")) return false;
                }
            }
            continue;
        }
        if (Node.isReturnStatement(stmt)) {
            temReturn = true;
            const expr = stmt.getExpression();
            if (!expr || !Node.isObjectLiteralExpression(expr)) return false;
            for (const prop of expr.getProperties()) {
                if (!Node.isSpreadAssignment(prop) && !Node.isShorthandPropertyAssignment(prop)) return false;
            }
            continue;
        }
        return false;
    }
    return temReturn;
}

/**
 * Detecta composables pequenos demais para justificar arquivo próprio.
 * Restrito a `composables/use*.ts` — exclui views, tipos e utilitários sem prefixo `use`.
 */
function detectarArquivoMinusculo(caminhoRelativo, linhas, hubCentral) {
    return !hubCentral
        && linhas < LIMITE_LINHAS_ARQUIVO_MINUSCULO
        && caminhoRelativo.startsWith("frontend/src/composables/")
        && path.basename(caminhoRelativo).startsWith("use");
}

function calcularScoreArquivo({camada, sinaisLexicais, analiseAst, hubCentral, fachadaPura, arquivoMinusculo}) {
    const camadaEfetiva = (camada === "store" && !analiseAst.usaDefineStore) ? "outro" : camada;
    const ehFacadeDeStore = camadaEfetiva === "composable"
        && analiseAst.importsPorCategoria.store.size === 1
        && analiseAst.importsPorCategoria.composable.size === 0
        && analiseAst.importsPorCategoria.service.size === 0;

    let total = 0;
    total += (sinaisLexicais.acessoDiretoCache * 8)
        + (sinaisLexicais.metodoEmCache * 6)
        + (sinaisLexicais.booleanoPosicional * 4)
        + (sinaisLexicais.palavraStale * 3)
        + (sinaisLexicais.palavraSnapshot * 2);

    if (!hubCentral) {
        total += sinaisLexicais.invalidacaoExplicita * 5;
        total += analiseAst.chamadasEstrategiaCache * (camadaEfetiva === "view" ? 8 : 5);
        total += analiseAst.chamadasInvalidacao * (camadaEfetiva === "view" ? 10 : 6);
        if (!ehFacadeDeStore && analiseAst.chamadasStore >= 8) {
            total += 8 + (Math.floor((analiseAst.chamadasStore - 8) / 4) * 3);
        }
    }

    const categoriasAcoplamento = contarCategoriasAcoplamento(analiseAst.importsPorCategoria);
    const importacoesArquiteturais = contarImportacoesArquiteturais(analiseAst.importsPorCategoria);

    if (camadaEfetiva === "view" || camadaEfetiva === "component") {
        if (categoriasAcoplamento >= 3) {
            total += 10 + ((categoriasAcoplamento - 3) * 4);
        }
        if (importacoesArquiteturais >= 5) {
            total += (importacoesArquiteturais - 4) * 3;
        }
    }

    total += analiseAst.chamadasServiceDiretas * pesoServiceDireto(camadaEfetiva);
    if (detectarServerStateCaseiro({camada: camadaEfetiva, analiseAst})) {
        total += camadaEfetiva === "view" ? 18 : 14;
    }
    total += analiseAst.bolsasDependenciasLargas * 9;
    if (!hubCentral && (camadaEfetiva === "store" || camadaEfetiva === "composable" || camadaEfetiva === "view" || camadaEfetiva === "component")) {
        total += analiseAst.superficieExportadaAmpla * 9;
    }

    if (fachadaPura) total += 3;
    else if (arquivoMinusculo) total += 1;

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
        `- Fachadas puras (composables sem lógica): **${snapshot.resumo.metricas.fachadasPuras}**`,
        `- Composables minúsculos (< ${LIMITE_LINHAS_ARQUIVO_MINUSCULO}L): **${snapshot.resumo.metricas.composablesMinusculos}**`,
        `- Famílias pulverizadas (>= ${LIMITE_FAMILIA_PULVERIZADA} membros): **${snapshot.resumo.metricas.familiasPulverizadas}**`,
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

    if (snapshot.familias) {
        const familiasGrandes = Object.entries(snapshot.familias)
            .filter(([, f]) => f.arquivos.length >= LIMITE_FAMILIA_PULVERIZADA)
            .sort(([, a], [, b]) => b.arquivos.length - a.arquivos.length);
        if (familiasGrandes.length > 0) {
            linhas.push("");
            linhas.push("## Famílias de composables pulverizadas");
            linhas.push("");
            for (const [nome, familia] of familiasGrandes) {
                linhas.push(`### ${nome} (${familia.arquivos.length} arquivos, ${familia.totalLinhas} linhas)`);
                for (const arq of familia.arquivos.sort()) {
                    linhas.push(`- \`${arq}\``);
                }
                linhas.push("");
            }
        }
    }

    if (snapshot.excecoesDocumentadas?.length > 0) {
        linhas.push("");
        linhas.push("## Exceções documentadas");
        linhas.push("");
        linhas.push("Arquivos com sinais suprimidos via `@sgc-auditoria ignorar:` com motivo explícito:");
        linhas.push("");
        for (const excecao of snapshot.excecoesDocumentadas) {
            linhas.push(`- \`${excecao.arquivo}\``);
            linhas.push(`  - sinais ignorados: ${excecao.sinais.join(", ")}`);
            linhas.push(`  - motivo: ${excecao.motivo}`);
        }
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
        ocorrenciasStale: 0,
        ocorrenciasSnapshot: 0,
        ocorrenciasForcar: 0,
        arquivosComBolsaDependenciasLarga: 0,
        arquivosComSuperficieAmpla: 0,
        arquivosComMisturaCamadas: 0,
        arquivosComServerStateCaseiro: 0,
        arquivosComAcoplamentoStoreAlto: 0,
        arquivosComServiceDireto: 0,
        chamadasEstrategiaCache: 0,
        chamadasInvalidacao: 0,
        hubsCentraisComSinais: 0,
        composablesMinusculos: 0,
        fachadasPuras: 0,
        familiasPulverizadas: 0,
    };
}

function obterSinaisAtivos(camada, sinaisLexicais, analiseAst, categoriasAcoplamento, importacoesArquiteturais, hubCentral, fachadaPura, arquivoMinusculo) {
    const camadaEfetiva = (camada === "store" && !analiseAst.usaDefineStore) ? "outro" : camada;

    const sinais = [];
    for (const [nome, valor] of Object.entries(sinaisLexicais)) {
        if (nome === "invalidacaoExplicita" && hubCentral) continue;
        if (valor > 0) {
            sinais.push(nome);
        }
    }
    if (analiseAst.chamadasServiceDiretas > 0 && pesoServiceDireto(camadaEfetiva) > 0) sinais.push("serviceDireto");
    if (!hubCentral && analiseAst.chamadasEstrategiaCache > 0) sinais.push("estrategiaCache");
    if (!hubCentral && analiseAst.chamadasInvalidacao > 0) sinais.push("invalidacaoArquitetural");
    if (analiseAst.bolsasDependenciasLargas > 0) sinais.push("bolsaDependenciasLarga");
    if (!hubCentral && analiseAst.superficieExportadaAmpla > 0) sinais.push("superficieAmpla");
    if (detectarServerStateCaseiro({camada: camadaEfetiva, analiseAst})) sinais.push("serverStateCaseiro");
    if (categoriasAcoplamento >= 3 && (camadaEfetiva === "view" || camadaEfetiva === "component")) sinais.push("misturaCamadas");
    if (importacoesArquiteturais >= 5 && (camadaEfetiva === "view" || camadaEfetiva === "component")) sinais.push("fanoutAlto");
    if (!hubCentral && analiseAst.chamadasStore >= 8) sinais.push("acoplamentoStoreAlto");
    if (fachadaPura) sinais.push("fachadaPura");
    else if (arquivoMinusculo) sinais.push("arquivoMinusculo");
    return sinais;
}

function criarProjetoAnalise() {
    return new Project({
        useInMemoryFileSystem: true,
        compilerOptions: {
            allowJs: true,
            strict: false,
        },
    });
}

/**
 * Agrupa arquivos de composables por "família" — o primeiro token CamelCase após o prefixo `use`.
 * Ex.: useFluxoSubprocessoExecucao → família "Fluxo"; useBuscadorUsuarios → "Buscador".
 * Retorna objeto { [familia]: { arquivos: string[], totalLinhas: number } }.
 */
function calcularFamilias(analisados) {
    const familias = {};
    for (const arquivo of analisados) {
        if (!arquivo.arquivo.startsWith("frontend/src/composables/")) continue;
        const nome = path.basename(arquivo.arquivo, ".ts");
        if (!nome.startsWith("use")) continue;
        const semUse = nome.slice(3);
        const match = semUse.match(/^[A-Z][a-z]+/);
        if (!match) continue;
        const familia = match[0];
        if (!familias[familia]) familias[familia] = {arquivos: [], totalLinhas: 0};
        familias[familia].arquivos.push(arquivo.arquivo);
        familias[familia].totalLinhas += arquivo.linhas;
    }
    return familias;
}

/**
 * Lê anotações `@sgc-auditoria ignorar: <sinais> | <motivo>` do conteúdo do arquivo.
 * Formato: // @sgc-auditoria ignorar: superficieAmpla, fachadaPura | Motivo explícito aqui
 * Retorna um Set com os nomes dos sinais a ignorar para este arquivo.
 * Efeito colateral: registra cada exceção em `excecoesDocumentadas` para o relatório.
 */
function lerExcecoesAuditoria(conteudo, caminhoRelativo, excecoesDocumentadas) {
    const sinaisExcetos = new Set();
    const regex = /\/\/ @sgc-auditoria ignorar:\s*([^|]+)\|\s*(.+)/g;
    let match;
    while ((match = regex.exec(conteudo)) !== null) {
        const sinais = match[1].split(",").map((s) => s.trim()).filter(Boolean);
        const motivo = match[2].trim();
        for (const sinal of sinais) {
            sinaisExcetos.add(sinal);
        }
        excecoesDocumentadas.push({arquivo: caminhoRelativo, sinais, motivo});
    }
    return sinaisExcetos;
}

async function analisarArquiteturaFrontend({base = DIRETORIO_RAIZ} = {}) {
    const baseResolvida = path.resolve(base ?? DIRETORIO_RAIZ);
    const arquivos = await listarArquivosFrontend(baseResolvida);
    const analisados = [];
    const metricas = criarMetricasResumo();
    const project = criarProjetoAnalise();
    const excecoesDocumentadas = [];

    for (const arquivo of arquivos) {
        const caminhoRelativo = normalizarCaminho(path.relative(baseResolvida, arquivo));
        if (!ehArquivoProducaoFrontend(caminhoRelativo)) {
            continue;
        }

        const conteudo = await fs.readFile(arquivo, "utf8");
        const camada = classificarCamada(caminhoRelativo);
        const {analiseAst, sourceFile} = analisarArquivoAst(project, caminhoRelativo, conteudo, camada);
        const sinaisExcetos = lerExcecoesAuditoria(conteudo, caminhoRelativo, excecoesDocumentadas);

        // Aplicar exceções documentadas antes de qualquer cálculo
        if (sinaisExcetos.has("superficieAmpla")) analiseAst.superficieExportadaAmpla = 0;
        if (sinaisExcetos.has("bolsaDependenciasLarga")) analiseAst.bolsasDependenciasLargas = 0;

        const sinaisLexicais = sourceFile ? computarSinaisLexicais(sourceFile, conteudo) : {
            acessoDiretoCache: 0,
            metodoEmCache: 0,
            invalidacaoExplicita: 0,
            booleanoPosicional: 0,
            palavraForcar: 0,
            palavraStale: contarOcorrencias(conteudo, PADROES.palavraStale),
            palavraSnapshot: contarOcorrencias(conteudo, PADROES.palavraSnapshot),
        };
        const categoriasAcoplamento = contarCategoriasAcoplamento(analiseAst.importsPorCategoria);
        const importacoesArquiteturais = contarImportacoesArquiteturais(analiseAst.importsPorCategoria);
        const hubCentral = HUBS_CENTRAIS.has(caminhoRelativo);
        const linhas = conteudo.split(/\r?\n/).length;
        const fachadaPura = !sinaisExcetos.has("fachadaPura") && detectarFachadaPura(camada, analiseAst, sourceFile, linhas, caminhoRelativo, hubCentral);
        const arquivoMinusculo = !fachadaPura && !sinaisExcetos.has("arquivoMinusculo") && detectarArquivoMinusculo(caminhoRelativo, linhas, hubCentral);
        const score = calcularScoreArquivo({camada, sinaisLexicais, analiseAst, hubCentral, fachadaPura, arquivoMinusculo});
        const sinaisAtivos = obterSinaisAtivos(camada, sinaisLexicais, analiseAst, categoriasAcoplamento, importacoesArquiteturais, hubCentral, fachadaPura, arquivoMinusculo);
        const temSinal = sinaisAtivos.length > 0;
        const serverStateCaseiro = detectarServerStateCaseiro({camada, analiseAst});

        const vazamentoCacheView = camada === "view" && (
            sinaisLexicais.acessoDiretoCache > 0
            || sinaisLexicais.metodoEmCache > 0
            || sinaisLexicais.invalidacaoExplicita > 0
            || analiseAst.chamadasEstrategiaCache > 0
            || analiseAst.chamadasInvalidacao > 0
            || sinaisLexicais.palavraForcar > 0
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
        metricas.ocorrenciasStale += sinaisLexicais.palavraStale;
        metricas.ocorrenciasSnapshot += sinaisLexicais.palavraSnapshot;
        metricas.ocorrenciasForcar += sinaisLexicais.palavraForcar;
        metricas.chamadasEstrategiaCache += analiseAst.chamadasEstrategiaCache;
        metricas.chamadasInvalidacao += analiseAst.chamadasInvalidacao;
        if (analiseAst.bolsasDependenciasLargas > 0) {
            metricas.arquivosComBolsaDependenciasLarga += 1;
        }
        if (!hubCentral && analiseAst.superficieExportadaAmpla > 0) {
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

        if (fachadaPura) metricas.fachadasPuras += 1;
        else if (arquivoMinusculo) metricas.composablesMinusculos += 1;

        analisados.push({
            arquivo: caminhoRelativo,
            camada,
            linhas,
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

    const familias = calcularFamilias(analisados);
    metricas.familiasPulverizadas = Object.values(familias).filter(
        (f) => f.arquivos.length >= LIMITE_FAMILIA_PULVERIZADA,
    ).length;

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
        familias,
        excecoesDocumentadas,
    };
}

export {
    analisarArquiteturaFrontend,
    DIRETORIO_SAIDA_PADRAO,
    gravarSnapshotArquitetura,
};
