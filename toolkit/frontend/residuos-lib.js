import fs from "node:fs/promises";
import path from "node:path";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";

const VERSAO_SCHEMA = "1.0.0";
const CAMINHO_ORCAMENTO_PADRAO = resolverCaminhoConfigurado("orcamentoResiduosFrontend");
const CAMINHO_EXCECOES_PADRAO = resolverCaminhoConfigurado("excecoesResiduosFrontend");
const DIRETORIO_SAIDA_PADRAO = path.join(resolverCaminhoConfigurado("artefatosQualidade"), "frontend-residuos", "mais-recente");

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
    linhasAcimaMeta: 0.5,
    linhasAcimaLimite: 1.0,
    anyExplicito: 12,
    checksNull: 2,
    fallbacksDefensivos: 3,
    catchBlocks: 2,
    castsDuplos: 8,
    storageDireto: 10,
    exportacoesSuspeitas: 4,
};

function normalizarCaminho(caminhoArquivo) {
    return caminhoArquivo.split(path.sep).join("/");
}

function ehArquivoTesteOuHistoria(caminhoRelativo) {
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
    return ehArquivoFrontend(caminhoRelativo) && !ehArquivoTesteOuHistoria(caminhoRelativo);
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
        exportacoesSuspeitas: 0,
        arquivosAcimaMeta: {},
        arquivosAcimaLimite: {},
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

function extrairExportacoesTempoExecucao(conteudo) {
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

function calcularScoreArquivo(arquivo, limitesCamada) {
    const linhasAcimaMeta = Math.max(arquivo.linhas - limitesCamada.meta, 0);
    const linhasAcimaLimite = Math.max(arquivo.linhas - limitesCamada.limite, 0);

    return (linhasAcimaMeta * PESOS_SCORE.linhasAcimaMeta)
        + (linhasAcimaLimite * PESOS_SCORE.linhasAcimaLimite)
        + (arquivo.contagens.anyExplicito * PESOS_SCORE.anyExplicito)
        + (arquivo.contagens.checksNull * PESOS_SCORE.checksNull)
        + (arquivo.contagens.fallbacksDefensivos * PESOS_SCORE.fallbacksDefensivos)
        + (arquivo.contagens.catchBlocks * PESOS_SCORE.catchBlocks)
        + (arquivo.contagens.castsDuplos * PESOS_SCORE.castsDuplos)
        + (arquivo.contagens.storageDireto * PESOS_SCORE.storageDireto)
        + (arquivo.contagens.exportacoesSuspeitas * PESOS_SCORE.exportacoesSuspeitas);
}

async function lerJsonOpcional(caminhoArquivo, fallback) {
    try {
        return JSON.parse(await fs.readFile(caminhoArquivo, "utf8"));
    } catch {
        return fallback;
    }
}

async function carregarOrcamento(caminhoOrcamento = CAMINHO_ORCAMENTO_PADRAO) {
    return lerJsonOpcional(caminhoOrcamento, {
        versaoSchema: VERSAO_SCHEMA,
        camadas: {},
        metricas: {
            maximosProducao: {},
        }
    });
}

async function carregarExcecoes(caminhoExcecoes = CAMINHO_EXCECOES_PADRAO) {
    const conteudo = await lerJsonOpcional(caminhoExcecoes, {versaoSchema: VERSAO_SCHEMA, excecoes: []});
    return Array.isArray(conteudo.excecoes) ? conteudo : {versaoSchema: VERSAO_SCHEMA, excecoes: []};
}

async function analisarResiduosFrontend({base = DIRETORIO_RAIZ, caminhoOrcamento = CAMINHO_ORCAMENTO_PADRAO} = {}) {
    const baseResolvida = path.resolve(base ?? DIRETORIO_RAIZ);
    const orcamento = await carregarOrcamento(caminhoOrcamento);
    const arquivos = await listarArquivosFrontend(baseResolvida);
    const arquivosAnalisados = [];
    const mapaExportacoes = new Map();
    const conteudos = new Map();

    for (const arquivo of arquivos) {
        const conteudo = await fs.readFile(arquivo, "utf8");
        const caminhoRelativo = normalizarCaminho(path.relative(baseResolvida, arquivo));
        conteudos.set(caminhoRelativo, conteudo);

        const ehProducao = ehArquivoProducaoFrontend(caminhoRelativo);
        const camada = classificarCamada(caminhoRelativo);
        const limitesCamada = orcamento.camadas?.[camada] ?? orcamento.camadas?.outro ?? {
            meta: Number.POSITIVE_INFINITY,
            limite: Number.POSITIVE_INFINITY
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
            exportacoesSuspeitas: 0,
        };

        const registro = {
            arquivo: caminhoRelativo,
            camada,
            categoriaArquivo: ehProducao ? "producao" : "teste",
            linhas: conteudo.split(/\r?\n/).length,
            imports: conteudo.match(/^\s*import\s/mg)?.length ?? 0,
            exportacoes: 0,
            exportacoesTempoExecucao: [],
            contagens,
            limites: limitesCamada,
            score: 0,
            violacoes: [],
        };

        if (ehProducao && path.extname(caminhoRelativo) === ".ts") {
            registro.exportacoesTempoExecucao = extrairExportacoesTempoExecucao(conteudo);
            registro.exportacoes = registro.exportacoesTempoExecucao.length;
            for (const nomeExportacao of registro.exportacoesTempoExecucao) {
                mapaExportacoes.set(`${caminhoRelativo}::${nomeExportacao}`, {
                    arquivo: caminhoRelativo,
                    nome: nomeExportacao,
                    consumidoresProducao: 0,
                    consumidoresTeste: 0,
                });
            }
        }

        arquivosAnalisados.push(registro);
    }

    for (const exportacao of mapaExportacoes.values()) {
        const regexUso = new RegExp(`\\b${exportacao.nome}\\b`);
        for (const [arquivo, conteudo] of conteudos.entries()) {
            if (arquivo === exportacao.arquivo || !regexUso.test(conteudo)) {
                continue;
            }
            if (ehArquivoProducaoFrontend(arquivo)) {
                exportacao.consumidoresProducao += 1;
            } else if (ehArquivoTesteOuHistoria(arquivo)) {
                exportacao.consumidoresTeste += 1;
            }
        }
    }

    const contagensProducao = criarContagensZeradas();
    const contagensTeste = criarContagensZeradas();
    const exportacoesSuspeitas = [];

    for (const arquivo of arquivosAnalisados) {
        if (arquivo.categoriaArquivo === "producao") {
            for (const nomeExportacao of arquivo.exportacoesTempoExecucao) {
                const chave = `${arquivo.arquivo}::${nomeExportacao}`;
                const exportacao = mapaExportacoes.get(chave);
                if (!exportacao || exportacao.consumidoresProducao > 0) {
                    continue;
                }
                arquivo.contagens.exportacoesSuspeitas += 1;
                exportacoesSuspeitas.push({
                    arquivo: arquivo.arquivo,
                    nomeExportacao,
                    consumidoresTeste: exportacao?.consumidoresTeste ?? 0,
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
        bucket.exportacoesSuspeitas += arquivo.contagens.exportacoesSuspeitas;

        if (arquivo.categoriaArquivo !== "producao") {
            continue;
        }

        if (arquivo.linhas > arquivo.limites.meta) {
            somarCamada(contagensProducao.arquivosAcimaMeta, arquivo.camada);
            arquivo.violacoes.push({
                tipo: "acima_meta",
                mensagem: `${arquivo.linhas} linhas > meta ${arquivo.limites.meta}`,
            });
        }

        if (arquivo.linhas > arquivo.limites.limite) {
            somarCamada(contagensProducao.arquivosAcimaLimite, arquivo.camada);
            arquivo.violacoes.push({
                tipo: "acima_limite",
                mensagem: `${arquivo.linhas} linhas > limite ${arquivo.limites.limite}`,
            });
        }

        arquivo.score = calcularScoreArquivo(arquivo, arquivo.limites);
    }

    const arquivosProducao = arquivosAnalisados.filter((item) => item.categoriaArquivo === "producao");
    const hotspots = [...arquivosProducao]
        .filter((item) => item.score > 0)
        .toSorted((a, b) => b.score - a.score)
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
        orcamento,
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
        exportacoesSuspeitas,
        hotspots,
        arquivos: arquivosAnalisados
            .toSorted((a, b) => b.score - a.score || b.linhas - a.linhas)
            .map((item) => ({
                ...item,
                score: Number(item.score.toFixed(1)),
            })),
    };
}

function gerarMarkdownAuditoria(fotografia) {
    const linhas = [];
    linhas.push("# Auditoria de residuos do frontend");
    linhas.push("");
    linhas.push(`Gerado em: ${fotografia.geradoEm}`);
    linhas.push(`Score total: ${fotografia.resumo.scoreTotal} (${fotografia.resumo.faixa})`);
    linhas.push("");
    linhas.push("## Resumo");
    linhas.push("");
    linhas.push(`- Arquivos de producao: ${fotografia.resumo.arquivosProducao}`);
    linhas.push(`- Arquivos de teste/story: ${fotografia.resumo.arquivosTeste}`);
    linhas.push(`- any explicito em producao: ${fotografia.contagens.producao.anyExplicito}`);
    linhas.push(`- checks de null em producao: ${fotografia.contagens.producao.checksNull}`);
    linhas.push(`- fallbacks defensivos em producao: ${fotografia.contagens.producao.fallbacksDefensivos}`);
    linhas.push(`- blocos catch em producao: ${fotografia.contagens.producao.catchBlocks}`);
    linhas.push(`- casts duplos em producao: ${fotografia.contagens.producao.castsDuplos}`);
    linhas.push(`- acessos diretos a storage em producao: ${fotografia.contagens.producao.storageDireto}`);
    linhas.push(`- exportacoes suspeitas em producao: ${fotografia.contagens.producao.exportacoesSuspeitas}`);
    linhas.push("");
    linhas.push("## Arquivos acima do orcamento");
    linhas.push("");
    linhas.push("| Camada | Acima da meta | Acima do limite |");
    linhas.push("|---|---:|---:|");

    const camadas = Object.keys(fotografia.orcamento.camadas ?? {}).filter((camada) => camada !== "outro");
    for (const camada of camadas) {
        linhas.push(`| ${camada} | ${fotografia.contagens.producao.arquivosAcimaMeta[camada] ?? 0} | ${fotografia.contagens.producao.arquivosAcimaLimite[camada] ?? 0} |`);
    }

    linhas.push("");
    linhas.push("## Top hotspots");
    linhas.push("");
    linhas.push("| Arquivo | Camada | Linhas | Score | Sinais |");
    linhas.push("|---|---|---:|---:|---|");
    for (const hotspot of fotografia.hotspots) {
        const sinais = Object.entries(hotspot.contagens)
            .filter(([, valor]) => typeof valor === "number" && valor > 0)
            .map(([chave, valor]) => `${chave}: ${valor}`)
            .join(", ");
        linhas.push(`| ${hotspot.arquivo} | ${hotspot.camada} | ${hotspot.linhas} | ${hotspot.score} | ${sinais || "-"} |`);
    }

    return `${linhas.join("\n")}\n`;
}

async function gravarFotografiaAuditoria(fotografia, diretorioSaida = DIRETORIO_SAIDA_PADRAO) {
    await fs.mkdir(diretorioSaida, {recursive: true});
    await fs.writeFile(path.join(diretorioSaida, "fotografia.json"), JSON.stringify(fotografia, null, 2));
    await fs.writeFile(path.join(diretorioSaida, "resumo.md"), gerarMarkdownAuditoria(fotografia));
}

export {
    CAMINHO_ORCAMENTO_PADRAO,
    CAMINHO_EXCECOES_PADRAO,
    DIRETORIO_SAIDA_PADRAO,
    analisarResiduosFrontend,
    carregarExcecoes,
    gravarFotografiaAuditoria,
};
