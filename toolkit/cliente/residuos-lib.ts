import fs from "node:fs/promises";
import path from "node:path";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import {
    carregarOrcamento,
    resolverCaminhoOrcamentoResiduos,
    type LimitesCamada,
    type OrcamentoResiduos,
} from "./residuos-politicas.js";

const EXTENSOES_SUPORTADAS = new Set([".ts", ".vue"]);
const VERSAO_SCHEMA_FOTOGRAFIA = "3.0.0" as const;
const LIMITE_RESUMO_RESIDUOS = 20;

const PADROES = {
    anyExplicito: [/\bas any\b/g, /:\s*any\b/g, /\bArray<any>\b/g, /\bPromise<any>\b/g, /\bref<any>\b/g, /\bRecord<[^>]+,\s*any>\b/g, /\[key:\s*string\]:\s*any\b/g],
    checksNull: [/(?:===|!==|==|!=)\s*null/g, /null\s*(?:===|!==|==|!=)/g],
    fallbacksDefensivos: [/\|\|\s*(?:\[]|\{}|["'`]{2}|false|true|0)(?![\w$])/g, /\?\?\s*(?:\[]|\{}|["'`]{2}|0)(?![\w$])/g],
    catchBlocks: [/catch\s*(?:\([^)]*\))?\s*\{/g],
    castsDuplos: [/\bas unknown as\b/g],
    storageDireto: [/\blocalStorage\.(?:getItem|setItem|removeItem|clear)\s*\(/g, /\bsessionStorage\.(?:getItem|setItem|removeItem|clear)\s*\(/g],
};

const PESOS_PONTUACAO = {
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

type Camada = "service" | "store" | "composable" | "view" | "component" | "router" | "utils" | "outro";
type CategoriaArquivo = "producao" | "teste";

interface ContagensSinais {
    anyExplicito: number;
    checksNull: number;
    fallbacksDefensivos: number;
    catchBlocks: number;
    castsDuplos: number;
    storageDireto: number;
    exportacoesSuspeitas: number;
}

type TipoSinalResiduo = keyof ContagensSinais;

interface SinalResiduo {
    tipo: TipoSinalResiduo;
    quantidade: number;
}

interface ContagensTotais extends ContagensSinais {
    arquivosAcimaMeta: Record<string, number>;
    arquivosAcimaLimite: Record<string, number>;
}

interface ViolacaoResiduo {
    tipo: "acima_meta" | "acima_limite";
    mensagem: string;
}

interface ArquivoResiduo {
    arquivo: string;
    camada: Camada;
    categoriaArquivo: CategoriaArquivo;
    linhas: number;
    imports: number;
    exportacoes: number;
    exportacoesTempoExecucao: string[];
    contagens: ContagensSinais;
    sinaisAtivos: SinalResiduo[];
    limites: LimitesCamada;
    pontuacao: number;
    violacoes: ViolacaoResiduo[];
}

interface ExportacaoIndexada {
    arquivo: string;
    nome: string;
    consumidoresProducao: number;
    consumidoresTeste: number;
}

interface ExportacaoSuspeita {
    arquivo: string;
    nomeExportacao: string;
    consumidoresTeste: number;
}

interface PontoCriticoResiduo {
    arquivo: string;
    camada: Camada;
    linhas: number;
    pontuacao: number;
    contagens: ContagensSinais;
    sinaisAtivos: SinalResiduo[];
    violacoes: ViolacaoResiduo[];
}

interface ResumoResiduos {
    arquivosCliente: number;
    arquivosProducao: number;
    arquivosTeste: number;
    pontuacaoTotal: number;
    classificacao: "inventario";
}

interface FotografiaResiduos {
    versaoSchema: typeof VERSAO_SCHEMA_FOTOGRAFIA;
    geradoEm: string;
    base: string;
    orcamento: OrcamentoResiduos;
    resumo: ResumoResiduos;
    contagens: {
        producao: ContagensTotais;
        testes: ContagensTotais;
    };
    exportacoesSuspeitas: ExportacaoSuspeita[];
    pontosCriticos: PontoCriticoResiduo[];
    arquivos: ArquivoResiduo[];
}

interface OpcoesAnaliseResiduos {
    base?: string;
    caminhoOrcamento?: string;
}

interface ResumoFotografiaResiduos {
    versaoResumo: 1;
    versaoSchema: FotografiaResiduos["versaoSchema"];
    base: string;
    truncado: true;
    limiteItens: number;
    resumo: FotografiaResiduos["resumo"];
    contagens: FotografiaResiduos["contagens"];
    exportacoesSuspeitas: FotografiaResiduos["exportacoesSuspeitas"];
    pontosCriticos: Array<{
        arquivo: string;
        camada: string;
        linhas: number;
        pontuacao: number;
        sinaisAtivos: FotografiaResiduos["pontosCriticos"][number]["sinaisAtivos"];
        violacoes: FotografiaResiduos["pontosCriticos"][number]["violacoes"];
    }>;
}

function criarResumoFotografiaResiduos(fotografia: FotografiaResiduos): ResumoFotografiaResiduos {
    return {
        versaoResumo: 1,
        versaoSchema: fotografia.versaoSchema,
        base: fotografia.base,
        truncado: true,
        limiteItens: LIMITE_RESUMO_RESIDUOS,
        resumo: fotografia.resumo,
        contagens: fotografia.contagens,
        exportacoesSuspeitas: fotografia.exportacoesSuspeitas.slice(0, LIMITE_RESUMO_RESIDUOS),
        pontosCriticos: fotografia.pontosCriticos.slice(0, LIMITE_RESUMO_RESIDUOS).map(ponto => ({
            arquivo: ponto.arquivo,
            camada: ponto.camada,
            linhas: ponto.linhas,
            pontuacao: ponto.pontuacao,
            sinaisAtivos: ponto.sinaisAtivos,
            violacoes: ponto.violacoes
        }))
    };
}

function resolverDiretorioSaidaResiduos(base: string = DIRETORIO_RAIZ): string {
    return path.join(resolverCaminhoConfigurado("artefatosQualidade", base), "cliente-residuos", "mais-recente");
}

function normalizarCaminho(caminhoArquivo: string): string {
    return caminhoArquivo.split(path.sep).join("/");
}

function resolverPrefixoCodigo(base: string): string {
    const diretorioCodigo = resolverCaminhoConfigurado("codigoCliente", base);
    const relativo = normalizarCaminho(path.relative(base, diretorioCodigo));
    return relativo ? `${relativo}/` : "";
}

function ehArquivoTesteOuHistoria(caminhoRelativo: string): boolean {
    return caminhoRelativo.includes("/__tests__/")
        || caminhoRelativo.includes("/__mocks__/")
        || caminhoRelativo.includes("/test/")
        || caminhoRelativo.includes("/test-utils/")
        || caminhoRelativo.endsWith(".spec.ts")
        || caminhoRelativo.endsWith(".test.ts")
        || caminhoRelativo.endsWith(".stories.ts");
}

function ehArquivoCliente(caminhoRelativo: string, prefixoCodigo: string): boolean {
    return prefixoCodigo === "" || caminhoRelativo.startsWith(prefixoCodigo);
}

function ehArquivoProducaoCliente(caminhoRelativo: string, prefixoCodigo: string): boolean {
    return ehArquivoCliente(caminhoRelativo, prefixoCodigo) && !ehArquivoTesteOuHistoria(caminhoRelativo);
}

function classificarCamada(caminhoRelativo: string, prefixoCodigo: string): Camada {
    const definicoesCamada: Array<{camada: Camada; diretorios: string[]}> = [
        {camada: "service", diretorios: ["services", "servicos"]},
        {camada: "store", diretorios: ["stores"]},
        {camada: "composable", diretorios: ["composables"]},
        {camada: "view", diretorios: ["views", "visoes"]},
        {camada: "component", diretorios: ["components", "componentes"]},
        {camada: "router", diretorios: ["router", "rotas"]},
        {camada: "utils", diretorios: ["utils", "utilitarios"]},
    ];
    const definicao = definicoesCamada.find((item) => item.diretorios.some(
        diretorio => caminhoRelativo.startsWith(`${prefixoCodigo}${diretorio}/`)
    ));
    return definicao?.camada ?? "outro";
}

function contarOcorrencias(conteudo: string, regexes: RegExp[]): number {
    return regexes.reduce((total, regex) => total + (conteudo.match(regex)?.length ?? 0), 0);
}

function criarContagensZeradas(): ContagensTotais {
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

async function listarArquivosCliente(base: string): Promise<string[]> {
    const diretorioCliente = resolverCaminhoConfigurado("codigoCliente", base);
    const arquivos: string[] = [];

    async function percorrer(diretorioAtual: string): Promise<void> {
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

    await percorrer(diretorioCliente);
    return arquivos;
}

function extrairExportacoesTempoExecucao(conteudo: string): string[] {
    const encontrados = new Set<string>();
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

function somarCamada(mapa: Record<string, number>, camada: string): void {
    mapa[camada] = (mapa[camada] ?? 0) + 1;
}

const TIPOS_SINAIS: TipoSinalResiduo[] = [
    "anyExplicito",
    "checksNull",
    "fallbacksDefensivos",
    "catchBlocks",
    "castsDuplos",
    "storageDireto",
    "exportacoesSuspeitas",
];

function extrairSinaisAtivos(contagens: ContagensSinais): SinalResiduo[] {
    return TIPOS_SINAIS
        .filter((tipo) => contagens[tipo] > 0)
        .map((tipo) => ({tipo, quantidade: contagens[tipo]}));
}

function calcularPontuacaoArquivo(arquivo: ArquivoResiduo, limitesCamada: LimitesCamada): number {
    const linhasAcimaMeta = Math.max(arquivo.linhas - limitesCamada.meta, 0);
    const linhasAcimaLimite = Math.max(arquivo.linhas - limitesCamada.limite, 0);

    return (linhasAcimaMeta * PESOS_PONTUACAO.linhasAcimaMeta)
        + (linhasAcimaLimite * PESOS_PONTUACAO.linhasAcimaLimite)
        + (arquivo.contagens.anyExplicito * PESOS_PONTUACAO.anyExplicito)
        + (arquivo.contagens.checksNull * PESOS_PONTUACAO.checksNull)
        + (arquivo.contagens.fallbacksDefensivos * PESOS_PONTUACAO.fallbacksDefensivos)
        + (arquivo.contagens.catchBlocks * PESOS_PONTUACAO.catchBlocks)
        + (arquivo.contagens.castsDuplos * PESOS_PONTUACAO.castsDuplos)
        + (arquivo.contagens.storageDireto * PESOS_PONTUACAO.storageDireto)
        + (arquivo.contagens.exportacoesSuspeitas * PESOS_PONTUACAO.exportacoesSuspeitas);
}

async function analisarResiduosCliente({base = DIRETORIO_RAIZ, caminhoOrcamento}: OpcoesAnaliseResiduos = {}): Promise<FotografiaResiduos> {
    const baseResolvida = path.resolve(base ?? DIRETORIO_RAIZ);
    const prefixoCodigo = resolverPrefixoCodigo(baseResolvida);
    const orcamento = await carregarOrcamento(caminhoOrcamento ?? resolverCaminhoOrcamentoResiduos(baseResolvida));
    const arquivos = await listarArquivosCliente(baseResolvida);
    const arquivosAnalisados: ArquivoResiduo[] = [];
    const mapaExportacoes = new Map<string, ExportacaoIndexada>();
    const conteudos = new Map<string, string>();

    for (const arquivo of arquivos) {
        const conteudo = await fs.readFile(arquivo, "utf8");
        const caminhoRelativo = normalizarCaminho(path.relative(baseResolvida, arquivo));
        conteudos.set(caminhoRelativo, conteudo);

        const ehProducao = ehArquivoProducaoCliente(caminhoRelativo, prefixoCodigo);
        const camada = classificarCamada(caminhoRelativo, prefixoCodigo);
        const limitesCamada = orcamento.camadas?.[camada] ?? orcamento.camadas?.outro ?? {
            meta: Number.POSITIVE_INFINITY,
            limite: Number.POSITIVE_INFINITY
        };
        const contagens: ContagensSinais = {
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

        const registro: ArquivoResiduo = {
            arquivo: caminhoRelativo,
            camada,
            categoriaArquivo: ehProducao ? "producao" : "teste",
            linhas: conteudo.split(/\r?\n/).length,
            imports: conteudo.match(/^\s*import\s/mg)?.length ?? 0,
            exportacoes: 0,
            exportacoesTempoExecucao: [],
            contagens,
            sinaisAtivos: [],
            limites: limitesCamada,
            pontuacao: 0,
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
            if (ehArquivoProducaoCliente(arquivo, prefixoCodigo)) {
                exportacao.consumidoresProducao += 1;
            } else if (ehArquivoTesteOuHistoria(arquivo)) {
                exportacao.consumidoresTeste += 1;
            }
        }
    }

    const contagensProducao = criarContagensZeradas();
    const contagensTeste = criarContagensZeradas();
    const exportacoesSuspeitas: ExportacaoSuspeita[] = [];

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
        arquivo.sinaisAtivos = extrairSinaisAtivos(arquivo.contagens);

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

        arquivo.pontuacao = calcularPontuacaoArquivo(arquivo, arquivo.limites);
    }

    const arquivosProducao = arquivosAnalisados.filter((item) => item.categoriaArquivo === "producao");
    const pontosCriticos: PontoCriticoResiduo[] = [...arquivosProducao]
        .filter((item) => item.pontuacao > 0)
        .toSorted((a, b) => b.pontuacao - a.pontuacao)
        .slice(0, 20)
        .map((item) => ({
            arquivo: item.arquivo,
            camada: item.camada,
            linhas: item.linhas,
            pontuacao: Number(item.pontuacao.toFixed(1)),
            contagens: item.contagens,
            sinaisAtivos: item.sinaisAtivos,
            violacoes: item.violacoes,
        }));

    const pontuacaoTotal = arquivosProducao.reduce((total, item) => total + item.pontuacao, 0);

    return {
        versaoSchema: VERSAO_SCHEMA_FOTOGRAFIA,
        geradoEm: new Date().toISOString(),
        base: baseResolvida,
        orcamento,
        resumo: {
            arquivosCliente: arquivosAnalisados.length,
            arquivosProducao: arquivosProducao.length,
            arquivosTeste: arquivosAnalisados.length - arquivosProducao.length,
            pontuacaoTotal: Number(pontuacaoTotal.toFixed(1)),
            classificacao: "inventario",
        },
        contagens: {
            producao: contagensProducao,
            testes: contagensTeste,
        },
        exportacoesSuspeitas,
        pontosCriticos,
        arquivos: arquivosAnalisados
            .toSorted((a, b) => b.pontuacao - a.pontuacao || b.linhas - a.linhas)
            .map((item) => ({
                ...item,
                pontuacao: Number(item.pontuacao.toFixed(1)),
            })),
    };
}

function gerarMarkdownAuditoria(fotografia: FotografiaResiduos): string {
    const linhas: string[] = [];
    linhas.push("# Auditoria de residuos do cliente");
    linhas.push("");
    linhas.push(`Gerado em: ${fotografia.geradoEm}`);
    linhas.push(`Pontuacao de ordenacao: ${fotografia.resumo.pontuacaoTotal} (inventario; nao e severidade)`);
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
    linhas.push("## Principais pontos criticos");
    linhas.push("");
    linhas.push("| Arquivo | Camada | Linhas | Pontuacao | Sinais de codigo | Violacoes de orcamento |");
    linhas.push("|---|---|---:|---:|---|---|");
    for (const pontoCritico of fotografia.pontosCriticos) {
        const sinais = pontoCritico.sinaisAtivos
            .map((sinal) => `${sinal.tipo}: ${sinal.quantidade}`)
            .join(", ");
        const violacoes = pontoCritico.violacoes
            .map((violacao) => violacao.mensagem)
            .join("; ");
        linhas.push(`| ${pontoCritico.arquivo} | ${pontoCritico.camada} | ${pontoCritico.linhas} | ${pontoCritico.pontuacao} | ${sinais || "-"} | ${violacoes || "-"} |`);
    }

    return `${linhas.join("\n")}\n`;
}

async function gravarFotografiaAuditoria(
    fotografia: FotografiaResiduos,
    diretorioSaida: string = resolverDiretorioSaidaResiduos(fotografia.base)
): Promise<void> {
    await fs.mkdir(diretorioSaida, {recursive: true});
    await fs.writeFile(path.join(diretorioSaida, "fotografia.json"), JSON.stringify(fotografia, null, 2));
    await fs.writeFile(path.join(diretorioSaida, "resumo.md"), gerarMarkdownAuditoria(fotografia));
}

export {
    analisarResiduosCliente,
    criarResumoFotografiaResiduos,
    gravarFotografiaAuditoria,
    resolverDiretorioSaidaResiduos,
    LIMITE_RESUMO_RESIDUOS,
    type FotografiaResiduos,
};
