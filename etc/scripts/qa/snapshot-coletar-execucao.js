import fs from "node:fs/promises";
import path from "node:path";
import process from "node:process";
import {execa} from "execa";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {extrairCoberturaJacoco} from "../lib/dominios/cobertura-java.js";
import {extrairCoberturaFrontend} from "../lib/dominios/cobertura-web.js";

const DIRETORIO_RAIZ = resolverNaRaiz();
const DIRETORIO_DASHBOARD = path.join(DIRETORIO_RAIZ, "etc", "qa-dashboard");
const DIRETORIO_RUNS = path.join(DIRETORIO_DASHBOARD, "runs");
const DIRETORIO_LATEST = path.join(DIRETORIO_DASHBOARD, "latest");
const VERSAO_SCHEMA = "1.0.0";

const PERFIS = {
    rapido: ["backendUnitario", "backendCobertura", "frontendCobertura", "frontendLint", "frontendTypecheck", "frontendCruft", "frontendArquitetura", "frontendTestIds", "sincroniaValidacoes"],
    completo: ["backendUnitario", "backendIntegracao", "backendCobertura", "frontendCobertura", "frontendLint", "frontendTypecheck", "frontendCruft", "frontendArquitetura", "e2ePlaywright", "frontendTestIds", "sincroniaValidacoes"],
    backend: ["backendUnitario", "backendIntegracao", "backendCobertura", "sincroniaValidacoes"],
    frontend: ["frontendCobertura", "frontendLint", "frontendTypecheck", "frontendCruft", "frontendArquitetura", "frontendTestIds", "sincroniaValidacoes"]
};

function caminhoRelativo(caminhoAbsoluto) {
    return path.relative(DIRETORIO_RAIZ, caminhoAbsoluto).replace(/\\/g, "/");
}

function formatarTimestampArquivo(data = new Date()) {
    return data.toISOString().replaceAll(":", "-").replace(/\.\d{3}Z$/, "Z");
}

 
function criarExecucao(codigo, nome, categoria, comando, diretorio) {
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

async function executarComando({comando, args, cwd, env}) {
    const inicio = Date.now();
    try {
        const resultado = await execa(comando, args, {
            cwd,
            env: {...process.env, ...env},
            shell: process.platform === "win32",
            reject: false
        });
        return {
            code: resultado.exitCode,
            stdout: resultado.stdout,
            stderr: resultado.stderr,
            duracaoMs: Date.now() - inicio
        };
    } catch (erro) {
        return {
            code: -1,
            stdout: "",
            stderr: erro.message,
            duracaoMs: Date.now() - inicio
        };
    }
}

function parseJsonSeguro(conteudo, fallback = {}) {
    try {
        return JSON.parse(conteudo);
    } catch {
        return fallback;
    }
}

async function consolidarJUnit(diretorioRelatorio) {
    const entries = await fs.readdir(diretorioRelatorio, {withFileTypes: true}).catch(() => []);
    const arquivos = entries.filter(e => e.isFile() && e.name.endsWith(".xml")).map(e => path.join(diretorioRelatorio, e.name));

    const totais = {testes: 0, falhas: 0, ignorados: 0, tempoSegundos: 0};
    for (const arquivo of arquivos) {
        const conteudo = await fs.readFile(arquivo, "utf-8");
        totais.testes += Number(conteudo.match(/tests="(\d+)"/)?.[1] ?? 0);
        totais.falhas += Number(conteudo.match(/failures="(\d+)"/)?.[1] ?? 0) + Number(conteudo.match(/errors="(\d+)"/)?.[1] ?? 0);
        totais.ignorados += Number(conteudo.match(/skipped="(\d+)"/)?.[1] ?? 0);
        totais.tempoSegundos += Number(conteudo.match(/time="([0-9.]+)"/)?.[1] ?? 0);
    }
    totais.sucessos = Math.max(totais.testes - totais.falhas - totais.ignorados, 0);
    totais.arquivosXml = arquivos.map(caminhoRelativo);
    return totais;
}

const ADAPTADORES = {
    async backendUnitario() {
        const execucao = criarExecucao("backend-unitario", "Backend unitario", "teste", "./gradlew :backend:unitTest", "backend");
        const saida = await executarComando({
            comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
            args: [":backend:unitTest"],
            cwd: DIRETORIO_RAIZ
        });
        const relatorio = await consolidarJUnit(path.join(DIRETORIO_RAIZ, "backend", "build", "test-results", "unitTest"));
        execucao.status = saida.code === 0 && relatorio.falhas === 0 ? "sucesso" : "falha";
        execucao.duracaoMs = saida.duracaoMs;
        execucao.metricas = relatorio;
        execucao.sumario = `${relatorio.sucessos}/${relatorio.testes} testes aprovados.`;
        return execucao;
    },
    async backendIntegracao() {
        const execucao = criarExecucao("backend-integracao", "Backend integracao", "teste", "./gradlew :backend:integrationTest", "backend");
        const saida = await executarComando({
            comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
            args: [":backend:integrationTest"],
            cwd: DIRETORIO_RAIZ
        });
        const relatorio = await consolidarJUnit(path.join(DIRETORIO_RAIZ, "backend", "build", "test-results", "integrationTest"));
        execucao.status = saida.code === 0 && relatorio.falhas === 0 ? "sucesso" : "falha";
        execucao.duracaoMs = saida.duracaoMs;
        execucao.metricas = relatorio;
        execucao.sumario = `${relatorio.sucessos}/${relatorio.testes} testes aprovados.`;
        return execucao;
    },
    async backendCobertura() {
        const execucao = criarExecucao("backend-cobertura", "Backend cobertura", "cobertura", "./gradlew :backend:jacocoTestReport", "backend");
        const saida = await executarComando({
            comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
            args: [":backend:jacocoTestReport"],
            cwd: DIRETORIO_RAIZ
        });
        const cobertura = await extrairCoberturaJacoco();
        execucao.status = saida.code === 0 ? "sucesso" : "falha";
        execucao.duracaoMs = saida.duracaoMs;
        execucao.metricas = cobertura;
        execucao.sumario = `Cobertura: ${cobertura.linhas.percentual}% linhas, ${cobertura.branches.percentual}% branches.`;
        return execucao;
    },
    async frontendCobertura() {
        const execucao = criarExecucao("frontend-cobertura", "Frontend cobertura", "cobertura", "npm --prefix frontend run coverage:unit:collect", "frontend");
        const saida = await executarComando({
            comando: "npm",
            args: ["--prefix", "frontend", "run", "coverage:unit:collect"],
            cwd: DIRETORIO_RAIZ
        });
        const cobertura = await extrairCoberturaFrontend();
        execucao.status = saida.code === 0 ? "sucesso" : "falha";
        execucao.duracaoMs = saida.duracaoMs;
        execucao.metricas = cobertura;
        execucao.sumario = `Cobertura: ${cobertura.lines.percentual}% linhas.`;
        return execucao;
    },
    async frontendLint() {
        const execucao = criarExecucao("frontend-lint", "Frontend lint", "qualidade", "npx eslint .", "frontend");
        const saida = await executarComando({
            comando: "npx",
            args: ["eslint", ".", "--max-warnings", "0"],
            cwd: path.join(DIRETORIO_RAIZ, "frontend")
        });
        execucao.status = saida.code === 0 ? "sucesso" : "falha";
        execucao.duracaoMs = saida.duracaoMs;
        execucao.sumario = saida.code === 0 ? "Lint sem problemas." : "Problemas de lint encontrados.";
        return execucao;
    },
    async frontendTypecheck() {
        const execucao = criarExecucao("frontend-typecheck", "Frontend typecheck", "qualidade", "npm --prefix frontend run typecheck", "frontend");
        const saida = await executarComando({
            comando: "npm",
            args: ["--prefix", "frontend", "run", "typecheck"],
            cwd: DIRETORIO_RAIZ
        });
        execucao.status = saida.code === 0 ? "sucesso" : "falha";
        execucao.duracaoMs = saida.duracaoMs;
        execucao.sumario = saida.code === 0 ? "Typecheck sem erros." : "Erros de tipagem encontrados.";
        return execucao;
    },
    async frontendCruft() {
        const execucao = criarExecucao("frontend-cruft", "Frontend cruft", "qualidade", "node etc/scripts/sgc.js frontend cruft validar --json-resumido", ".");
        const saida = await executarComando({
            comando: "node",
            args: ["etc/scripts/sgc.js", "frontend", "cruft", "validar", "--json-resumido"],
            cwd: DIRETORIO_RAIZ
        });
        const resultado = parseJsonSeguro(saida.stdout, {});
        execucao.status = saida.code === 0 && resultado.status === "ok" ? "sucesso" : "falha";
        execucao.duracaoMs = saida.duracaoMs;
        execucao.metricas = {
            scoreTotal: resultado.resumo?.scoreTotal ?? null,
            faixa: resultado.resumo?.faixa ?? null,
            violacoes: resultado.violacoes ?? [],
            avisos: resultado.avisos ?? [],
            hotspots: resultado.hotspots ?? []
        };
        execucao.sumario = resultado.resumo
            ? `Score de cruft: ${resultado.resumo.scoreTotal} (${resultado.resumo.faixa}).`
            : "Validacao de cruft executada.";
        return execucao;
    },
    async frontendArquitetura() {
        const execucao = criarExecucao("frontend-arquitetura", "Frontend arquitetura", "qualidade", "node etc/scripts/sgc.js frontend arquitetura auditar --json", ".");
        const saida = await executarComando({
            comando: "node",
            args: ["etc/scripts/sgc.js", "frontend", "arquitetura", "auditar", "--json"],
            cwd: DIRETORIO_RAIZ
        });
        const resultado = parseJsonSeguro(saida.stdout, {});
        execucao.status = saida.code === 0 ? "sucesso" : "falha";
        execucao.duracaoMs = saida.duracaoMs;
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
    async frontendTestIds() {
        const execucao = criarExecucao("frontend-test-ids", "Frontend Test IDs", "qualidade", "node etc/scripts/sgc.js frontend test-ids listar-duplicados", ".");
        const saida = await executarComando({
            comando: "node",
            args: ["etc/scripts/sgc.js", "frontend", "test-ids", "listar-duplicados"],
            cwd: DIRETORIO_RAIZ
        });
        execucao.status = saida.code === 0 ? "sucesso" : "falha";
        execucao.duracaoMs = saida.duracaoMs;
        execucao.sumario = saida.code === 0 ? "Nenhum test-id duplicado." : "Test-ids duplicados encontrados.";
        return execucao;
    },
    async sincroniaValidacoes() {
        const execucao = criarExecucao("sincronia-validacoes", "Sincronia de Validações", "qualidade", "node etc/scripts/sgc.js frontend validacoes auditar", ".");
        const saida = await executarComando({
            comando: "node",
            args: ["etc/scripts/sgc.js", "frontend", "validacoes", "auditar"],
            cwd: DIRETORIO_RAIZ
        });
        execucao.status = saida.code === 0 ? "sucesso" : "falha";
        execucao.duracaoMs = saida.duracaoMs;
        execucao.sumario = saida.code === 0 ? "Auditoria de validações concluída." : "Divergências de validação encontradas.";
        return execucao;
    },
    async e2ePlaywright() {
        const execucao = criarExecucao("e2e-playwright", "E2E Playwright", "teste", "npx playwright test", ".");
        const saida = await executarComando({
            comando: "npx",
            args: ["playwright", "test", "--reporter=json"],
            cwd: DIRETORIO_RAIZ,
            env: {CI: "1"}
        });
        const stats = JSON.parse(saida.stdout || "{}").stats || {};
        execucao.status = saida.code === 0 ? "sucesso" : "falha";
        execucao.duracaoMs = saida.duracaoMs;
        execucao.metricas = stats;
        execucao.sumario = `${stats.expected ?? 0} testes E2E aprovados.`;
        return execucao;
    }
};

async function coletarGit() {
    const branch = (await execa("git", ["rev-parse", "--abbrev-ref", "HEAD"])).stdout.trim();
    const commit = (await execa("git", ["rev-parse", "HEAD"])).stdout.trim();
    return {branch, commit};
}

async function main() {
    const indicePerfil = process.argv.indexOf("--perfil");
    const perfilPorOpcao = indicePerfil >= 0 ? process.argv[indicePerfil + 1] : null;
    const perfilPorAtribuicao = process.argv.find(a => a.startsWith("--perfil="))?.split("=")[1] ?? null;
    const perfil = perfilPorOpcao || perfilPorAtribuicao || "rapido";
    const inicio = Date.now();
    const timestamp = formatarTimestampArquivo();
    const diretorioExecucao = path.join(DIRETORIO_RUNS, timestamp);

    await fs.mkdir(diretorioExecucao, {recursive: true});
    await fs.mkdir(DIRETORIO_LATEST, {recursive: true});

    const verificacoes = [];
    for (const adaptador of PERFIS[perfil]) {
        console.log(`Executando ${adaptador}...`);
        verificacoes.push(await ADAPTADORES[adaptador]());
    }

    const hotspotsCruft = verificacoes
        .filter((item) => Array.isArray(item.metricas?.hotspots))
        .flatMap((item) => item.metricas.hotspots.map((hotspot) => ({
            nome: hotspot.arquivo,
            risco: hotspot.score,
            origem: item.codigo
        })))
        .sort((a, b) => b.risco - a.risco)
        .slice(0, 20);

    const snapshot = {
        versaoSchema: VERSAO_SCHEMA,
        metadados: {
            geradoEm: new Date().toISOString(),
            perfilExecucao: perfil,
            duracaoTotalMs: Date.now() - inicio,
            git: await coletarGit().catch(() => ({}))
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
        hotspots: hotspotsCruft
    };

    await fs.writeFile(path.join(diretorioExecucao, "snapshot.json"), JSON.stringify(snapshot, null, 2));
    await fs.writeFile(path.join(DIRETORIO_LATEST, "ultimo-snapshot.json"), JSON.stringify(snapshot, null, 2));
    console.log(`Snapshot gerado em ${caminhoRelativo(path.join(diretorioExecucao, "snapshot.json"))}`);
}

main().catch(console.error);
