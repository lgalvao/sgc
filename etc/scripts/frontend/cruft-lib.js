import fs from "node:fs/promises";
import path from "node:path";
import {DIRETORIO_RAIZ, resolverNaRaiz} from "../lib/caminhos.js";

const VERSAO_SCHEMA = "1.0.0";
const CAMINHO_BUDGET_PADRAO = resolverNaRaiz("etc", "qualidade", "frontend-cruft-budget.json");
const CAMINHO_WAIVERS_PADRAO = resolverNaRaiz("etc", "qualidade", "frontend-cruft-waivers.json");
const DIRETORIO_SAIDA_PADRAO = resolverNaRaiz("etc", "qualidade", "frontend-cruft", "latest");
const CAMINHO_SNAPSHOT_PADRAO = path.join(DIRETORIO_SAIDA_PADRAO, "ultimo-snapshot.json");
const CAMINHO_RESUMO_PADRAO = path.join(DIRETORIO_SAIDA_PADRAO, "ultimo-resumo.md");

const EXTENSOES_SUPORTADAS = new Set([".ts", ".vue"]);

const DEFINICOES_CAMADA = [
    {camada: "service", prefixo: "frontend/src/services/"},
    {camada: "store", prefixo: "frontend/src/stores/"},
    {camada: "composable", prefixo: "frontend/src/composables/"},
    {camada: "view", prefixo: "frontend/src/views/"},
    {camada: "component", prefixo: "frontend/src/components/"},
    {camada: "router", prefixo: "frontend/src/router/"},
    {camada: "utils", prefixo: "frontend/src/utils/"},
];

const PADROES = {
    anyExplicito: [/\bas any\b/g, /:\s*any\b/g, /\bArray<any>\b/g, /\bPromise<any>\b/g, /\bref<any>\b/g, /\bRecord<[^>]+,\s*any>\b/g, /\[key:\s*string\]:\s*any\b/g],
    checksNull: [/(?:===|!==|==|!=)\s*null/g, /null\s*(?:===|!==|==|!=)/g],
    fallbacksDefensivos: [/\|\|\s*(?:\[]|\{}|["'`]{2}|false|true|0)(?![\w$])/g, /\?\?\s*(?:\[]|\{}|["'`]{2}|0)(?![\w$])/g],
    catchBlocks: [/catch\s*(?:\([^)]*\))?\s*\{/g],
    castsDuplos: [/\bas unknown as\b/g],
    storageDireto: [/\blocalStorage\.(?:getItem|setItem|removeItem|clear)\s*\(/g, /\bsessionStorage\.(?:getItem|setItem|removeItem|clear)\s*\(/g],
};

const PESOS_SCORE = {
    linhasAcimaTarget: 0.5,
    linhasAcimaHard: 1.0,
    anyExplicito: 12,
    checksNull: 2,
    fallbacksDefensivos: 3,
    catchBlocks: 2,
    castsDuplos: 8,
    storageDireto: 10,
    exportsSuspeitos: 4,
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

function ehArquivoFrontend(caminhoRelativo) {
    return caminhoRelativo.startsWith("frontend/src/");
}

function ehArquivoProducaoFrontend(caminhoRelativo) {
    return ehArquivoFrontend(caminhoRelativo) && !ehArquivoTesteOuStory(caminhoRelativo);
}

function classificarCamada(caminhoRelativo) {
    const definicao = DEFINICOES_CAMADA.find((item) => caminhoRelativo.startsWith(item.prefixo));
    return definicao?.camada ?? "outro";
}

function contarOcorrencias(conteudo, regexes) {
    return regexes.reduce((total, regex) => total + (conteudo.match(regex)?.length ?? 0), 0);
}

function criarContagensZeradas() {
    return {
        anyExplicito: 0,
        checksNull: 0,
        fallbacksDefensivos: 0,
        catchBlocks: 0,
        castsDuplos: 0,
        storageDireto: 0,
        exportsSuspeitos: 0,
        arquivosAcimaTarget: {},
        arquivosAcimaHard: {},
    };
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

function extrairExportsRuntime(conteudo) {
    const encontrados = new Set();
    const regexes = [
        /export\s+async\s+function\s+([A-Za-z_][A-Za-z0-9_]*)/g,
        /export\s+function\s+([A-Za-z_][A-Za-z0-9_]*)/g,
        /export\s+const\s+([A-Za-z_][A-Za-z0-9_]*)/g,
        /export\s+class\s+([A-Za-z_][A-Za-z0-9_]*)/g,
    ];

    for (const regex of regexes) {
        let match = regex.exec(conteudo);
        while (match) {
            encontrados.add(match[1]);
            match = regex.exec(conteudo);
        }
    }

    const exportListas = [...conteudo.matchAll(/export\s*\{\s*([^}]+)\s*\}/g)];
    for (const match of exportListas) {
        const nomes = match[1]
            .split(",")
            .map((item) => item.trim())
            .map((item) => item.split(/\s+as\s+/i)[0]?.trim())
            .filter(Boolean);
        nomes.forEach((nome) => encontrados.add(nome));
    }

    return [...encontrados];
}

function somarCamada(mapa, camada) {
    mapa[camada] = (mapa[camada] ?? 0) + 1;
}

function calcularFaixa(score) {
    if (score <= 80) {
        return "bom";
    }
    if (score <= 180) {
        return "atencao";
    }
    return "critico";
}

function calcularScoreArquivo(arquivo, budgetCamada) {
    const linhasAcimaTarget = Math.max(arquivo.linhas - budgetCamada.target, 0);
    const linhasAcimaHard = Math.max(arquivo.linhas - budgetCamada.hard, 0);

    return (linhasAcimaTarget * PESOS_SCORE.linhasAcimaTarget)
        + (linhasAcimaHard * PESOS_SCORE.linhasAcimaHard)
        + (arquivo.contagens.anyExplicito * PESOS_SCORE.anyExplicito)
        + (arquivo.contagens.checksNull * PESOS_SCORE.checksNull)
        + (arquivo.contagens.fallbacksDefensivos * PESOS_SCORE.fallbacksDefensivos)
        + (arquivo.contagens.catchBlocks * PESOS_SCORE.catchBlocks)
        + (arquivo.contagens.castsDuplos * PESOS_SCORE.castsDuplos)
        + (arquivo.contagens.storageDireto * PESOS_SCORE.storageDireto)
        + (arquivo.contagens.exportsSuspeitos * PESOS_SCORE.exportsSuspeitos);
}

async function lerJsonOpcional(caminhoArquivo, fallback) {
    try {
        return JSON.parse(await fs.readFile(caminhoArquivo, "utf8"));
    } catch {
        return fallback;
    }
}

async function carregarBudget(caminhoBudget = CAMINHO_BUDGET_PADRAO) {
    return lerJsonOpcional(caminhoBudget, {
        versaoSchema: VERSAO_SCHEMA,
        camadas: {},
        metricas: {
            maximosProducao: {},
        }
    });
}

async function carregarWaivers(caminhoWaivers = CAMINHO_WAIVERS_PADRAO) {
    const conteudo = await lerJsonOpcional(caminhoWaivers, {versaoSchema: VERSAO_SCHEMA, waivers: []});
    return Array.isArray(conteudo.waivers) ? conteudo : {versaoSchema: VERSAO_SCHEMA, waivers: []};
}

async function analisarCruftFrontend({base = DIRETORIO_RAIZ, caminhoBudget = CAMINHO_BUDGET_PADRAO} = {}) {
    const baseResolvida = path.resolve(base ?? DIRETORIO_RAIZ);
    const budget = await carregarBudget(caminhoBudget);
    const arquivos = await listarArquivosFrontend(baseResolvida);
    const arquivosAnalisados = [];
    const exportsMap = new Map();
    const conteudos = new Map();

    for (const arquivo of arquivos) {
        const conteudo = await fs.readFile(arquivo, "utf8");
        const caminhoRelativo = normalizarCaminho(path.relative(baseResolvida, arquivo));
        conteudos.set(caminhoRelativo, conteudo);

        const ehProducao = ehArquivoProducaoFrontend(caminhoRelativo);
        const camada = classificarCamada(caminhoRelativo);
        const definicaoBudget = budget.camadas?.[camada] ?? budget.camadas?.outro ?? {
            target: Number.POSITIVE_INFINITY,
            hard: Number.POSITIVE_INFINITY
        };
        const contagens = {
            anyExplicito: contarOcorrencias(conteudo, PADROES.anyExplicito),
            checksNull: contarOcorrencias(conteudo, PADROES.checksNull),
            fallbacksDefensivos: contarOcorrencias(conteudo, PADROES.fallbacksDefensivos),
            catchBlocks: contarOcorrencias(conteudo, PADROES.catchBlocks),
            castsDuplos: contarOcorrencias(conteudo, PADROES.castsDuplos),
            storageDireto: (caminhoRelativo.endsWith("useLocalStorage.ts")
                || caminhoRelativo.endsWith("useSessionStorage.ts")
                || caminhoRelativo.endsWith("useWebStorage.ts"))
                ? 0
                : contarOcorrencias(conteudo, PADROES.storageDireto),
            exportsSuspeitos: 0,
        };

        const registro = {
            arquivo: caminhoRelativo,
            camada,
            categoriaArquivo: ehProducao ? "producao" : "teste",
            linhas: conteudo.split(/\r?\n/).length,
            imports: conteudo.match(/^\s*import\s/mg)?.length ?? 0,
            exports: 0,
            exportsRuntime: [],
            contagens,
            limites: definicaoBudget,
            score: 0,
            violacoes: [],
        };

        if (ehProducao && path.extname(caminhoRelativo) === ".ts") {
            registro.exportsRuntime = extrairExportsRuntime(conteudo);
            registro.exports = registro.exportsRuntime.length;
            for (const exportNome of registro.exportsRuntime) {
                exportsMap.set(`${caminhoRelativo}::${exportNome}`, {
                    arquivo: caminhoRelativo,
                    nome: exportNome,
                    consumidoresProducao: 0,
                    consumidoresTeste: 0,
                });
            }
        }

        arquivosAnalisados.push(registro);
    }

    for (const exportInfo of exportsMap.values()) {
        const regexUso = new RegExp(`\\b${exportInfo.nome}\\b`);
        for (const [arquivo, conteudo] of conteudos.entries()) {
            if (arquivo === exportInfo.arquivo || !regexUso.test(conteudo)) {
                continue;
            }
            if (ehArquivoProducaoFrontend(arquivo)) {
                exportInfo.consumidoresProducao += 1;
            } else if (ehArquivoTesteOuStory(arquivo)) {
                exportInfo.consumidoresTeste += 1;
            }
        }
    }

    const contagensProducao = criarContagensZeradas();
    const contagensTeste = criarContagensZeradas();
    const exportsSuspeitos = [];

    for (const arquivo of arquivosAnalisados) {
        if (arquivo.categoriaArquivo === "producao") {
            for (const exportNome of arquivo.exportsRuntime) {
                const chave = `${arquivo.arquivo}::${exportNome}`;
                const exportInfo = exportsMap.get(chave);
                if (!exportInfo || exportInfo.consumidoresProducao > 0) {
                    continue;
                }
                arquivo.contagens.exportsSuspeitos += 1;
                exportsSuspeitos.push({
                    arquivo: arquivo.arquivo,
                    exportNome,
                    consumidoresTeste: exportInfo?.consumidoresTeste ?? 0,
                });
            }
        }

        const bucket = arquivo.categoriaArquivo === "producao" ? contagensProducao : contagensTeste;
        bucket.anyExplicito += arquivo.contagens.anyExplicito;
        bucket.checksNull += arquivo.contagens.checksNull;
        bucket.fallbacksDefensivos += arquivo.contagens.fallbacksDefensivos;
        bucket.catchBlocks += arquivo.contagens.catchBlocks;
        bucket.castsDuplos += arquivo.contagens.castsDuplos;
        bucket.storageDireto += arquivo.contagens.storageDireto;
        bucket.exportsSuspeitos += arquivo.contagens.exportsSuspeitos;

        if (arquivo.categoriaArquivo !== "producao") {
            continue;
        }

        if (arquivo.linhas > arquivo.limites.target) {
            somarCamada(contagensProducao.arquivosAcimaTarget, arquivo.camada);
            arquivo.violacoes.push({
                tipo: "acima_target",
                mensagem: `${arquivo.linhas} linhas > target ${arquivo.limites.target}`,
            });
        }

        if (arquivo.linhas > arquivo.limites.hard) {
            somarCamada(contagensProducao.arquivosAcimaHard, arquivo.camada);
            arquivo.violacoes.push({
                tipo: "acima_hard",
                mensagem: `${arquivo.linhas} linhas > hard ${arquivo.limites.hard}`,
            });
        }

        arquivo.score = calcularScoreArquivo(arquivo, arquivo.limites);
    }

    const arquivosProducao = arquivosAnalisados.filter((item) => item.categoriaArquivo === "producao");
    const hotspots = [...arquivosProducao]
        .filter((item) => item.score > 0)
        .sort((a, b) => b.score - a.score)
        .slice(0, 20)
        .map((item) => ({
            arquivo: item.arquivo,
            camada: item.camada,
            linhas: item.linhas,
            score: Number(item.score.toFixed(1)),
            contagens: item.contagens,
            violacoes: item.violacoes,
        }));

    const scoreTotal = arquivosProducao.reduce((total, item) => total + item.score, 0);

    return {
        versaoSchema: VERSAO_SCHEMA,
        geradoEm: new Date().toISOString(),
        base: baseResolvida,
        budget,
        resumo: {
            arquivosFrontend: arquivosAnalisados.length,
            arquivosProducao: arquivosProducao.length,
            arquivosTeste: arquivosAnalisados.length - arquivosProducao.length,
            scoreTotal: Number(scoreTotal.toFixed(1)),
            faixa: calcularFaixa(scoreTotal),
        },
        contagens: {
            producao: contagensProducao,
            testes: contagensTeste,
        },
        exportsSuspeitos,
        hotspots,
        arquivos: arquivosAnalisados
            .sort((a, b) => b.score - a.score || b.linhas - a.linhas)
            .map((item) => ({
                ...item,
                score: Number(item.score.toFixed(1)),
            })),
    };
}

function gerarMarkdownAuditoria(snapshot) {
    const linhas = [];
    linhas.push("# Auditoria de cruft do frontend");
    linhas.push("");
    linhas.push(`Gerado em: ${snapshot.geradoEm}`);
    linhas.push(`Score total: ${snapshot.resumo.scoreTotal} (${snapshot.resumo.faixa})`);
    linhas.push("");
    linhas.push("## Resumo");
    linhas.push("");
    linhas.push(`- Arquivos de producao: ${snapshot.resumo.arquivosProducao}`);
    linhas.push(`- Arquivos de teste/story: ${snapshot.resumo.arquivosTeste}`);
    linhas.push(`- any explicito em producao: ${snapshot.contagens.producao.anyExplicito}`);
    linhas.push(`- checks de null em producao: ${snapshot.contagens.producao.checksNull}`);
    linhas.push(`- fallbacks defensivos em producao: ${snapshot.contagens.producao.fallbacksDefensivos}`);
    linhas.push(`- blocos catch em producao: ${snapshot.contagens.producao.catchBlocks}`);
    linhas.push(`- casts duplos em producao: ${snapshot.contagens.producao.castsDuplos}`);
    linhas.push(`- acessos diretos a storage em producao: ${snapshot.contagens.producao.storageDireto}`);
    linhas.push(`- exports suspeitos em producao: ${snapshot.contagens.producao.exportsSuspeitos}`);
    linhas.push("");
    linhas.push("## Arquivos acima do budget");
    linhas.push("");
    linhas.push("| Camada | Acima do target | Acima do hard |");
    linhas.push("|---|---:|---:|");

    const camadas = Object.keys(snapshot.budget.camadas ?? {}).filter((camada) => camada !== "outro");
    for (const camada of camadas) {
        linhas.push(`| ${camada} | ${snapshot.contagens.producao.arquivosAcimaTarget[camada] ?? 0} | ${snapshot.contagens.producao.arquivosAcimaHard[camada] ?? 0} |`);
    }

    linhas.push("");
    linhas.push("## Top hotspots");
    linhas.push("");
    linhas.push("| Arquivo | Camada | Linhas | Score | Sinais |");
    linhas.push("|---|---|---:|---:|---|");
    for (const hotspot of snapshot.hotspots) {
        const sinais = Object.entries(hotspot.contagens)
            .filter(([, valor]) => typeof valor === "number" && valor > 0)
            .map(([chave, valor]) => `${chave}: ${valor}`)
            .join(", ");
        linhas.push(`| ${hotspot.arquivo} | ${hotspot.camada} | ${hotspot.linhas} | ${hotspot.score} | ${sinais || "-"} |`);
    }

    return `${linhas.join("\n")}\n`;
}

async function gravarSnapshotAuditoria(snapshot, diretorioSaida = DIRETORIO_SAIDA_PADRAO) {
    await fs.mkdir(diretorioSaida, {recursive: true});
    await fs.writeFile(path.join(diretorioSaida, "ultimo-snapshot.json"), JSON.stringify(snapshot, null, 2));
    await fs.writeFile(path.join(diretorioSaida, "ultimo-resumo.md"), gerarMarkdownAuditoria(snapshot));
}

export {
    CAMINHO_BUDGET_PADRAO,
    CAMINHO_RESUMO_PADRAO,
    CAMINHO_SNAPSHOT_PADRAO,
    CAMINHO_WAIVERS_PADRAO,
    DIRETORIO_SAIDA_PADRAO,
    VERSAO_SCHEMA,
    analisarCruftFrontend,
    carregarBudget,
    carregarWaivers,
    ehArquivoTesteOuStory,
    gerarMarkdownAuditoria,
    gravarSnapshotAuditoria,
};
