import fs from "node:fs/promises";
import path from "node:path";
import process from "node:process";
import {execa} from "execa";
import {DIRETORIO_RAIZ, DIRETORIO_TOOLKIT} from "../lib/caminhos.js";
import {ehEntradaPrincipal, resolverCaminhoTsx} from "../lib/execucao.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";
import {NOME_ARQUIVO_FOTOGRAFIA, obterDiretorioArtefatos} from "../lib/qualidade.js";
import {extrairCoberturaJacoco} from "../lib/dominios/cobertura-java.js";
import {extrairCoberturaFrontend} from "../lib/dominios/cobertura-web.js";
import {escreverLinha} from "../lib/saida.js";

const CAMINHO_SGC = path.join(DIRETORIO_TOOLKIT, "sgc.ts");
const VERSAO_SCHEMA = "1.0.0" as const;

type PerfilQualidade = "rapido" | "completo" | "backend" | "frontend";
type NomeAdaptador =
    | "testesBackendUnitarios"
    | "testesBackendIntegracao"
    | "coberturaBackend"
    | "coberturaFrontend"
    | "lintFrontend"
    | "tiposFrontend"
    | "residuosFrontend"
    | "arquiteturaFrontend"
    | "testesIntegracaoPlaywright"
    | "identificadoresTesteFrontend";
type CategoriaExecucao = "teste" | "cobertura" | "qualidade";
type StatusExecucao = "nao_executado" | "sucesso" | "falha";

interface ContextoColeta {
    base: string;
    diretorioArtefatos: string;
    diretorioExecucoes: string;
    diretorioMaisRecente: string;
    diretorioBackend: string;
    diretorioFrontend: string;
    diretorioFrontendCodigo: string;
}

interface ExecucaoQualidade {
    codigo: string;
    nome: string;
    categoria: CategoriaExecucao;
    status: StatusExecucao;
    duracaoMs: number;
    comando: string;
    diretorio: string;
    sumario: string;
    metricas: unknown;
    erros: string[];
    artefatos: string[];
}

interface ResultadoComando {
    codigoSaida: number;
    saida: string;
    erro: string;
    duracaoMs: number;
}

interface OpcoesComando {
    comando: string;
    args: string[];
    cwd: string;
    env?: Record<string, string>;
}

interface ResultadoJUnit extends Record<string, unknown> {
    testes: number;
    falhas: number;
    ignorados: number;
    tempoSegundos: number;
    sucessos: number;
    arquivosXml: string[];
}

interface ResultadoResiduos {
    status?: string;
    resumo?: {
        scoreTotal?: number;
        faixa?: string;
    };
    violacoes?: unknown[];
    avisos?: unknown[];
    hotspots?: HotspotQualidade[];
}

interface ResultadoArquitetura {
    resumo?: {
        scoreTotal?: number;
        faixa?: string;
        metricas?: Record<string, unknown>;
    };
    hotspots?: unknown[];
}

interface ResultadoPlaywright extends Record<string, unknown> {
    stats?: Record<string, unknown> & {
        expected?: number;
    };
}

interface HotspotQualidade {
    arquivo: string;
    score: number;
}

interface FotografiaColeta {
    versaoSchema: string;
    metadados: {
        geradoEm: string;
        perfilExecucao: PerfilQualidade;
        duracaoTotalMs: number;
        git: Record<string, string>;
    };
    verificacoes: ExecucaoQualidade[];
    resumo: {
        statusGeral: "verde" | "vermelho";
        totais: {
            verificacoes: number;
            sucesso: number;
            falha: number;
        };
    };
    hotspots: Array<{
        nome: string;
        risco: number;
        origem: string;
    }>;
}

const PERFIS: Record<PerfilQualidade, NomeAdaptador[]> = {
    rapido: ["testesBackendUnitarios", "coberturaBackend", "coberturaFrontend", "lintFrontend", "tiposFrontend", "residuosFrontend", "arquiteturaFrontend", "identificadoresTesteFrontend"],
    completo: ["testesBackendUnitarios", "testesBackendIntegracao", "coberturaBackend", "coberturaFrontend", "lintFrontend", "tiposFrontend", "residuosFrontend", "arquiteturaFrontend", "testesIntegracaoPlaywright", "identificadoresTesteFrontend"],
    backend: ["testesBackendUnitarios", "testesBackendIntegracao", "coberturaBackend"],
    frontend: ["coberturaFrontend", "lintFrontend", "tiposFrontend", "residuosFrontend", "arquiteturaFrontend", "identificadoresTesteFrontend"]
};

function criarContextoColeta(base: string = DIRETORIO_RAIZ): ContextoColeta {
    const baseResolvida = path.resolve(base ?? DIRETORIO_RAIZ);
    const diretorioArtefatos = obterDiretorioArtefatos(baseResolvida);

    return {
        base: baseResolvida,
        diretorioArtefatos,
        diretorioExecucoes: path.join(diretorioArtefatos, "execucoes"),
        diretorioMaisRecente: path.join(diretorioArtefatos, "mais-recente"),
        diretorioBackend: resolverCaminhoConfigurado("backend", baseResolvida),
        diretorioFrontend: resolverCaminhoConfigurado("frontend", baseResolvida),
        diretorioFrontendCodigo: resolverCaminhoConfigurado("frontendCodigo", baseResolvida),
    };
}

function caminhoRelativo(caminhoAbsoluto: string, base: string): string {
    return path.relative(base, caminhoAbsoluto).replace(/\\/g, "/");
}

function formatarTimestampArquivo(data: Date = new Date()): string {
    return data.toISOString().replaceAll(":", "-").replace(/\.\d{3}Z$/, "Z");
}


function criarExecucao(
    codigo: string,
    nome: string,
    categoria: CategoriaExecucao,
    comando: string,
    diretorio: string
): ExecucaoQualidade {
    return {
        codigo,
        nome,
        categoria,
        status: "nao_executado",
        duracaoMs: 0,
        comando,
        diretorio,
        sumario: "",
        metricas: {},
        erros: [],
        artefatos: []
    };
}

async function executarComando({comando, args, cwd, env}: OpcoesComando): Promise<ResultadoComando> {
    const inicio = Date.now();
    try {
        const resultado = await execa(comando, args, {
            cwd,
            env: {...process.env, ...env},
            shell: process.platform === "win32",
            reject: false
        });
        return {
            codigoSaida: resultado.exitCode ?? -1,
            saida: resultado.stdout,
            erro: resultado.stderr,
            duracaoMs: Date.now() - inicio
        };
    } catch (erro: unknown) {
        return {
            codigoSaida: -1,
            saida: "",
            erro: erro instanceof Error ? erro.message : String(erro),
            duracaoMs: Date.now() - inicio
        };
    }
}

async function executarComandoSgc(contexto: ContextoColeta, argumentos: string[], incluirBase: boolean = true): Promise<ResultadoComando> {
    return executarComando({
        comando: resolverCaminhoTsx(),
        args: [CAMINHO_SGC, ...argumentos, ...(incluirBase ? ["--base", contexto.base] : [])],
        cwd: contexto.base,
    });
}

function registrarResultadoExecucao(execucao: ExecucaoQualidade, resultado: ResultadoComando): void {
    execucao.duracaoMs = resultado.duracaoMs;
    if (resultado.codigoSaida !== 0) {
        execucao.erros = [resultado.erro || resultado.saida || `Comando terminou com codigo ${resultado.codigoSaida}.`];
    }
}

function parseJsonSeguro<T>(conteudo: string, fallback: T): T {
    try {
        return JSON.parse(conteudo);
    } catch {
        return fallback;
    }
}

async function consolidarJUnit(diretorioRelatorio: string, base: string): Promise<ResultadoJUnit> {
    const entries = await fs.readdir(diretorioRelatorio, {withFileTypes: true}).catch(() => []);
    const arquivos = entries.filter(e => e.isFile() && e.name.endsWith(".xml")).map(e => path.join(diretorioRelatorio, e.name));

    const totais: ResultadoJUnit = {testes: 0, falhas: 0, ignorados: 0, tempoSegundos: 0, sucessos: 0, arquivosXml: []};
    for (const arquivo of arquivos) {
        const conteudo = await fs.readFile(arquivo, "utf-8");
        totais.testes += Number(conteudo.match(/tests="(\d+)"/)?.[1] ?? 0);
        totais.falhas += Number(conteudo.match(/failures="(\d+)"/)?.[1] ?? 0) + Number(conteudo.match(/errors="(\d+)"/)?.[1] ?? 0);
        totais.ignorados += Number(conteudo.match(/skipped="(\d+)"/)?.[1] ?? 0);
        totais.tempoSegundos += Number(conteudo.match(/time="([0-9.]+)"/)?.[1] ?? 0);
    }
    totais.sucessos = Math.max(totais.testes - totais.falhas - totais.ignorados, 0);
    totais.arquivosXml = arquivos.map((arquivo) => caminhoRelativo(arquivo, base));
    return totais;
}

function ehHotspotQualidade(valor: unknown): valor is HotspotQualidade {
    if (!valor || typeof valor !== "object") {
        return false;
    }
    const hotspot = valor as Record<string, unknown>;
    return typeof hotspot.arquivo === "string" && typeof hotspot.score === "number";
}

function extrairHotspotsQualidade(metricas: unknown): HotspotQualidade[] {
    if (!metricas || typeof metricas !== "object") {
        return [];
    }
    const hotspots = (metricas as Record<string, unknown>).hotspots;
    return Array.isArray(hotspots) ? hotspots.filter(ehHotspotQualidade) : [];
}

const ADAPTADORES: Record<NomeAdaptador, (contexto: ContextoColeta) => Promise<ExecucaoQualidade>> = {
    async testesBackendUnitarios(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
        const execucao = criarExecucao("backend-unitario", "Backend unitario", "teste", "./gradlew :backend:unitTest", "backend");
        const saida = await executarComando({
            comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
            args: [":backend:unitTest"],
            cwd: contexto.base
        });
        const relatorio = await consolidarJUnit(path.join(contexto.diretorioBackend, "build", "test-results", "unitTest"), contexto.base);
        execucao.status = saida.codigoSaida === 0 && relatorio.falhas === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.metricas = relatorio;
        execucao.sumario = `${relatorio.sucessos}/${relatorio.testes} testes aprovados.`;
        return execucao;
    },
    async testesBackendIntegracao(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
        const execucao = criarExecucao("backend-integracao", "Backend integracao", "teste", "./gradlew :backend:integrationTest", "backend");
        const saida = await executarComando({
            comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
            args: [":backend:integrationTest"],
            cwd: contexto.base
        });
        const relatorio = await consolidarJUnit(path.join(contexto.diretorioBackend, "build", "test-results", "integrationTest"), contexto.base);
        execucao.status = saida.codigoSaida === 0 && relatorio.falhas === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.metricas = relatorio;
        execucao.sumario = `${relatorio.sucessos}/${relatorio.testes} testes aprovados.`;
        return execucao;
    },
    async coberturaBackend(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
        const execucao = criarExecucao("backend-cobertura", "Backend cobertura", "cobertura", "./gradlew :backend:jacocoTestReport", "backend");
        const saida = await executarComando({
            comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
            args: [":backend:jacocoTestReport"],
            cwd: contexto.base
        });
        const cobertura = await extrairCoberturaJacoco(null, {diretorioBase: contexto.base});
        execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.metricas = cobertura;
        execucao.sumario = `Cobertura: ${cobertura.linhas.percentual}% linhas, ${cobertura.branches.percentual}% branches.`;
        return execucao;
    },
    async coberturaFrontend(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
        const execucao = criarExecucao("frontend-cobertura", "Frontend cobertura", "cobertura", "npm --prefix frontend run coverage:unit:collect", "frontend");
        const saida = await executarComando({
            comando: "npm",
            args: ["run", "coverage:unit:collect"],
            cwd: contexto.diretorioFrontend
        });
        const cobertura = await extrairCoberturaFrontend(null, {diretorioBase: contexto.base});
        execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.metricas = cobertura;
        execucao.sumario = `Cobertura: ${cobertura.lines.percentual}% linhas.`;
        return execucao;
    },
    async lintFrontend(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
        const execucao = criarExecucao("frontend-lint", "Frontend lint", "qualidade", "npx eslint .", "frontend");
        const saida = await executarComando({
            comando: "npx",
            args: ["eslint", ".", "--max-warnings", "0"],
            cwd: contexto.diretorioFrontend
        });
        execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.sumario = saida.codigoSaida === 0 ? "Lint sem problemas." : "Problemas de lint encontrados.";
        return execucao;
    },
    async tiposFrontend(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
        const execucao = criarExecucao("frontend-typecheck", "Frontend typecheck", "qualidade", "npm --prefix frontend run typecheck", "frontend");
        const saida = await executarComando({
            comando: "npm",
            args: ["run", "typecheck"],
            cwd: contexto.diretorioFrontend
        });
        execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.sumario = saida.codigoSaida === 0 ? "Typecheck sem erros." : "Erros de tipagem encontrados.";
        return execucao;
    },
    async residuosFrontend(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
        const execucao = criarExecucao("frontend-residuos", "Residuos do frontend", "qualidade", "npx tsx toolkit/sgc.ts frontend residuos validar --json-resumido --gravar", ".");
        const saida = await executarComandoSgc(contexto, ["frontend", "residuos", "validar", "--json-resumido", "--gravar"]);
        const resultado = parseJsonSeguro<ResultadoResiduos>(saida.saida, {});
        execucao.status = saida.codigoSaida === 0 && resultado.status === "ok" ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.metricas = {
            scoreTotal: resultado.resumo?.scoreTotal ?? null,
            faixa: resultado.resumo?.faixa ?? null,
            violacoes: resultado.violacoes ?? [],
            avisos: resultado.avisos ?? [],
            hotspots: resultado.hotspots ?? []
        };
        execucao.sumario = resultado.resumo
            ? `Pontuacao de residuos: ${resultado.resumo.scoreTotal} (${resultado.resumo.faixa}).`
            : "Validacao de residuos executada.";
        return execucao;
    },
    async arquiteturaFrontend(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
        const execucao = criarExecucao("frontend-arquitetura", "Frontend arquitetura", "qualidade", "npx tsx toolkit/sgc.ts frontend arquitetura auditar --json --gravar", ".");
        const saida = await executarComandoSgc(contexto, ["frontend", "arquitetura", "auditar", "--json", "--gravar"]);
        const resultado = parseJsonSeguro<ResultadoArquitetura>(saida.saida, {});
        execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.metricas = {
            scoreTotal: resultado.resumo?.scoreTotal ?? null,
            faixa: resultado.resumo?.faixa ?? null,
            viewsComVazamentoCache: resultado.resumo?.metricas?.viewsComVazamentoCache ?? null,
            viewsComServiceDireto: resultado.resumo?.metricas?.viewsComServiceDireto ?? null,
            viewsComFanoutAlto: resultado.resumo?.metricas?.viewsComFanoutAlto ?? null,
            acessosDiretosCache: resultado.resumo?.metricas?.acessosDiretosCache ?? null,
            booleanosPosicionais: resultado.resumo?.metricas?.booleanosPosicionais ?? null,
            ocorrenciasForcar: resultado.resumo?.metricas?.ocorrenciasForcar ?? null,
            arquivosComBolsaDependenciasLarga: resultado.resumo?.metricas?.arquivosComBolsaDependenciasLarga ?? null,
            arquivosComSuperficieAmpla: resultado.resumo?.metricas?.arquivosComSuperficieAmpla ?? null,
            arquivosComMisturaCamadas: resultado.resumo?.metricas?.arquivosComMisturaCamadas ?? null,
            hubsCentraisComSinais: resultado.resumo?.metricas?.hubsCentraisComSinais ?? null,
            hotspots: resultado.hotspots ?? [],
        };
        execucao.sumario = resultado.resumo
            ? `Score arquitetural: ${resultado.resumo.scoreTotal} (${resultado.resumo.faixa}).`
            : "Auditoria arquitetural executada.";
        return execucao;
    },
    async identificadoresTesteFrontend(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
        const execucao = criarExecucao("frontend-identificadores-teste", "Identificadores de teste do frontend", "qualidade", "npx tsx toolkit/sgc.ts frontend identificadores-teste listar-duplicados", ".");
        const saida = await executarComandoSgc(contexto, [
            "frontend",
            "identificadores-teste",
            "listar-duplicados",
            "--base",
            contexto.base,
        ], false);
        execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.sumario = saida.codigoSaida === 0 ? "Nenhum identificador de teste duplicado." : "Identificadores de teste duplicados encontrados.";
        return execucao;
    },
    async testesIntegracaoPlaywright(contexto: ContextoColeta): Promise<ExecucaoQualidade> {
        const execucao = criarExecucao("e2e-playwright", "E2E Playwright", "teste", "npx playwright test --config=e2e/playwright.config.ts", ".");
        const saida = await executarComando({
            comando: "npx",
            args: ["playwright", "test", "--config=e2e/playwright.config.ts", "--reporter=json"],

            cwd: contexto.base,
            env: {CI: "1"}
        });
        const resultado = parseJsonSeguro<ResultadoPlaywright>(saida.saida, {});
        const stats = resultado.stats ?? {};
        execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.metricas = stats;
        execucao.sumario = `${stats.expected ?? 0} testes E2E aprovados.`;
        return execucao;
    }
};

async function coletarGit(base: string): Promise<Record<string, string>> {
    const branch = (await execa("git", ["rev-parse", "--abbrev-ref", "HEAD"], {cwd: base})).stdout.trim();
    const commit = (await execa("git", ["rev-parse", "HEAD"], {cwd: base})).stdout.trim();
    return {branch, commit};
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<FotografiaColeta> {
    const indicePerfil = argumentos.indexOf("--perfil");
    const perfilPorOpcao = indicePerfil >= 0 ? argumentos[indicePerfil + 1] : null;
    const perfilPorAtribuicao = argumentos.find(argumento => argumento.startsWith("--perfil="))?.split("=")[1] ?? null;
    const perfilInformado = perfilPorOpcao || perfilPorAtribuicao || "rapido";
    const base = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const contexto = criarContextoColeta(base);
    const inicio = Date.now();
    const timestamp = formatarTimestampArquivo();
    if (!(perfilInformado in PERFIS)) {
        throw new Error(`Perfil de qualidade invalido: ${perfilInformado}`);
    }
    const perfil = perfilInformado as PerfilQualidade;

    const diretorioExecucao = path.join(contexto.diretorioExecucoes, timestamp);

    await fs.mkdir(diretorioExecucao, {recursive: true});
    await fs.mkdir(contexto.diretorioMaisRecente, {recursive: true});

    const verificacoes: ExecucaoQualidade[] = [];
    for (const adaptador of PERFIS[perfil]) {
        escreverLinha(`Executando ${adaptador}...`);
        verificacoes.push(await ADAPTADORES[adaptador](contexto));
    }

    const hotspotsResiduos = verificacoes
        .flatMap((item) => extrairHotspotsQualidade(item.metricas).map((hotspot) => ({
            nome: hotspot.arquivo,
            risco: hotspot.score,
            origem: item.codigo
        })))
        .toSorted((a, b) => b.risco - a.risco)
        .slice(0, 20);

    const fotografia: FotografiaColeta = {
        versaoSchema: VERSAO_SCHEMA,
        metadados: {
            geradoEm: new Date().toISOString(),
            perfilExecucao: perfil,
            duracaoTotalMs: Date.now() - inicio,
            git: await coletarGit(contexto.base).catch(() => ({}))
        },
        verificacoes,
        resumo: {
            statusGeral: verificacoes.some(v => v.status === "falha") ? "vermelho" : "verde",
            totais: {
                verificacoes: verificacoes.length,
                sucesso: verificacoes.filter(v => v.status === "sucesso").length,
                falha: verificacoes.filter(v => v.status === "falha").length
            }
        },
        hotspots: hotspotsResiduos
    };

    const caminhoFotografia = path.join(diretorioExecucao, NOME_ARQUIVO_FOTOGRAFIA);
    await fs.writeFile(caminhoFotografia, JSON.stringify(fotografia, null, 2));
    await fs.writeFile(path.join(contexto.diretorioMaisRecente, NOME_ARQUIVO_FOTOGRAFIA), JSON.stringify(fotografia, null, 2));
    escreverLinha(`Fotografia gerada em ${caminhoRelativo(caminhoFotografia, contexto.base)}`);

    return fotografia;
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        process.stderr.write(`Erro ao coletar qualidade: ${mensagem}\n`);
        process.exitCode = 1;
    });
}

export {
    ADAPTADORES,
    PERFIS,
    principal
};
