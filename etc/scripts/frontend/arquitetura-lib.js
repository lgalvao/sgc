import fs from "node:fs/promises";
import path from "node:path";
import {DIRETORIO_RAIZ, resolverNaRaiz} from "../lib/caminhos.js";

const VERSAO_SCHEMA = "1.0.0";
const DIRETORIO_SAIDA_PADRAO = resolverNaRaiz("etc", "qualidade", "frontend-arquitetura", "latest");
const CAMINHO_SNAPSHOT_PADRAO = path.join(DIRETORIO_SAIDA_PADRAO, "ultimo-snapshot.json");
const CAMINHO_RESUMO_PADRAO = path.join(DIRETORIO_SAIDA_PADRAO, "ultimo-resumo.md");
const EXTENSOES_SUPORTADAS = new Set([".ts", ".vue"]);

const PADROES = {
    acessoDiretoCache: /\.\s*cache[A-Z][A-Za-z0-9_]*/g,
    metodoEmCache: /\b(?:tem|obter|reaplicar)[A-Za-z0-9_]*EmCache\s*\(/g,
    invalidacaoExplicita: /\.\s*invalidar[A-Za-z0-9_]*\s*\(/g,
    booleanoPosicional: /\b[A-Za-z_$][\w$]*\s*\([^)\n]*,\s*(?:true|false)\s*(?:[,)\n])/g,
    palavraForcar: /\bforcar\b/g,
    palavraStale: /\bstale\b/g,
    palavraSnapshot: /\bsnapshot\b/g,
};

const HUBS_CENTRAIS = new Set([
    "frontend/src/stores/perfil.ts",
    "frontend/src/stores/unidade.ts",
    "frontend/src/stores/mapas.ts",
    "frontend/src/composables/useInvalidacaoNavegacao.ts",
    "frontend/src/composables/useCacheSync.ts",
]);

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
    if (caminhoRelativo.startsWith("frontend/src/views/")) return "view";
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

function calcularScoreArquivo(sinais) {
    return (sinais.acessoDiretoCache * 8)
        + (sinais.metodoEmCache * 6)
        + (sinais.invalidacaoExplicita * 5)
        + (sinais.booleanoPosicional * 4)
        + (sinais.palavraForcar * 3)
        + (sinais.palavraStale * 3)
        + (sinais.palavraSnapshot * 2);
}

function calcularFaixa(score) {
    if (score <= 20) return "bom";
    if (score <= 60) return "atencao";
    return "critico";
}

function criarResumoMarkdown(snapshot) {
    const linhas = [
        "# Auditoria Arquitetural do Frontend",
        "",
        `- Score total: **${snapshot.resumo.scoreTotal}** (${snapshot.resumo.faixa})`,
        `- Arquivos de producao: **${snapshot.resumo.arquivosProducao}**`,
        `- Views com vazamento de estrategia de cache: **${snapshot.resumo.metricas.viewsComVazamentoCache}**`,
        `- Acessos diretos a cache de store: **${snapshot.resumo.metricas.acessosDiretosCache}**`,
        `- Chamadas com booleano posicional: **${snapshot.resumo.metricas.booleanosPosicionais}**`,
        `- Ocorrencias de \`forcar\` em producao: **${snapshot.resumo.metricas.ocorrenciasForcar}**`,
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
        });
    }

    linhas.push("");
    linhas.push("## Diretrizes acompanhadas");
    linhas.push("");
    linhas.push("- views nao devem conhecer estrategia de cache;");
    linhas.push("- contratos de view devem ser orientados a caso de uso;");
    linhas.push("- evitar `forcar`, `stale`, `snapshot`, `invalidar` e `xxxEmCache` na borda consumida pela view;");
    linhas.push("- reduzir hubs centrais antes de expandir APIs locais.");
    linhas.push("");

    return `${linhas.join("\n")}\n`;
}

async function gravarSnapshotArquitetura(snapshot, diretorioSaida = DIRETORIO_SAIDA_PADRAO) {
    await fs.mkdir(diretorioSaida, {recursive: true});
    await fs.writeFile(path.join(diretorioSaida, path.basename(CAMINHO_SNAPSHOT_PADRAO)), JSON.stringify(snapshot, null, 2));
    await fs.writeFile(path.join(diretorioSaida, path.basename(CAMINHO_RESUMO_PADRAO)), criarResumoMarkdown(snapshot));
}

async function analisarArquiteturaFrontend({base = DIRETORIO_RAIZ} = {}) {
    const baseResolvida = path.resolve(base ?? DIRETORIO_RAIZ);
    const arquivos = await listarArquivosFrontend(baseResolvida);
    const analisados = [];
    const metricas = {
        viewsComVazamentoCache: 0,
        acessosDiretosCache: 0,
        metodosEmCache: 0,
        invalidacoesExplicitasEmViews: 0,
        booleanosPosicionais: 0,
        ocorrenciasForcar: 0,
        ocorrenciasStale: 0,
        ocorrenciasSnapshot: 0,
        hubsCentraisComSinais: 0,
    };

    for (const arquivo of arquivos) {
        const caminhoRelativo = normalizarCaminho(path.relative(baseResolvida, arquivo));
        if (!ehArquivoProducaoFrontend(caminhoRelativo)) {
            continue;
        }

        const conteudo = await fs.readFile(arquivo, "utf8");
        const camada = classificarCamada(caminhoRelativo);
        const sinais = {
            acessoDiretoCache: contarOcorrencias(conteudo, PADROES.acessoDiretoCache),
            metodoEmCache: contarOcorrencias(conteudo, PADROES.metodoEmCache),
            invalidacaoExplicita: contarOcorrencias(conteudo, PADROES.invalidacaoExplicita),
            booleanoPosicional: contarOcorrencias(conteudo, PADROES.booleanoPosicional),
            palavraForcar: contarOcorrencias(conteudo, PADROES.palavraForcar),
            palavraStale: contarOcorrencias(conteudo, PADROES.palavraStale),
            palavraSnapshot: contarOcorrencias(conteudo, PADROES.palavraSnapshot),
        };
        const score = calcularScoreArquivo(sinais);
        const sinaisAtivos = Object.entries(sinais).filter(([, valor]) => valor > 0).map(([nome]) => nome);
        const temSinal = sinaisAtivos.length > 0;

        if (camada === "view" && temSinal) {
            metricas.viewsComVazamentoCache += 1;
        }
        metricas.acessosDiretosCache += sinais.acessoDiretoCache;
        metricas.metodosEmCache += sinais.metodoEmCache;
        if (camada === "view") {
            metricas.invalidacoesExplicitasEmViews += sinais.invalidacaoExplicita;
        }
        metricas.booleanosPosicionais += sinais.booleanoPosicional;
        metricas.ocorrenciasForcar += sinais.palavraForcar;
        metricas.ocorrenciasStale += sinais.palavraStale;
        metricas.ocorrenciasSnapshot += sinais.palavraSnapshot;
        if (HUBS_CENTRAIS.has(caminhoRelativo) && temSinal) {
            metricas.hubsCentraisComSinais += 1;
        }

        analisados.push({
            arquivo: caminhoRelativo,
            camada,
            linhas: conteudo.split(/\r?\n/).length,
            score,
            sinais,
            sinaisAtivos,
            hubCentral: HUBS_CENTRAIS.has(caminhoRelativo),
        });
    }

    const hotspots = analisados
        .filter((arquivo) => arquivo.score > 0)
        .sort((a, b) => b.score - a.score || b.linhas - a.linhas || a.arquivo.localeCompare(b.arquivo));

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
