import fs from "node:fs/promises";
import path from "node:path";
import process from "node:process";
import {pathToFileURL} from "node:url";
import {execa} from "execa";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {NOME_ARQUIVO_FOTOGRAFIA, obterDiretorioArtefatos} from "../lib/qualidade.js";
import {extrairCoberturaJacoco} from "../lib/dominios/cobertura-java.js";
import {extrairCoberturaFrontend} from "../lib/dominios/cobertura-web.js";

const DIRETORIO_RAIZ = resolverNaRaiz();
const DIRETORIO_ARTEFATOS = obterDiretorioArtefatos(DIRETORIO_RAIZ);
const DIRETORIO_EXECUCOES = path.join(DIRETORIO_ARTEFATOS, "execucoes");
const DIRETORIO_MAIS_RECENTE = path.join(DIRETORIO_ARTEFATOS, "mais-recente");
const VERSAO_SCHEMA = "1.0.0";

const PERFIS = {
    rapido: ["testesBackendUnitarios", "coberturaBackend", "coberturaFrontend", "lintFrontend", "tiposFrontend", "residuosFrontend", "arquiteturaFrontend", "identificadoresTesteFrontend"],
    completo: ["testesBackendUnitarios", "testesBackendIntegracao", "coberturaBackend", "coberturaFrontend", "lintFrontend", "tiposFrontend", "residuosFrontend", "arquiteturaFrontend", "testesIntegracaoPlaywright", "identificadoresTesteFrontend"],
    backend: ["testesBackendUnitarios", "testesBackendIntegracao", "coberturaBackend"],
    frontend: ["coberturaFrontend", "lintFrontend", "tiposFrontend", "residuosFrontend", "arquiteturaFrontend", "identificadoresTesteFrontend"]
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
            codigoSaida: resultado.exitCode,
            saida: resultado.stdout,
            erro: resultado.stderr,
            duracaoMs: Date.now() - inicio
        };
    } catch (erro) {
        return {
            codigoSaida: -1,
            saida: "",
            erro: erro.message,
            duracaoMs: Date.now() - inicio
        };
    }
}

function registrarResultadoExecucao(execucao, resultado) {
    execucao.duracaoMs = resultado.duracaoMs;
    if (resultado.codigoSaida !== 0) {
        execucao.erros = [resultado.erro || resultado.saida || `Comando terminou com codigo ${resultado.codigoSaida}.`];
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
    async testesBackendUnitarios() {
        const execucao = criarExecucao("backend-unitario", "Backend unitario", "teste", "./gradlew :backend:unitTest", "backend");
        const saida = await executarComando({
            comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
            args: [":backend:unitTest"],
            cwd: DIRETORIO_RAIZ
        });
        const relatorio = await consolidarJUnit(path.join(DIRETORIO_RAIZ, "backend", "build", "test-results", "unitTest"));
        execucao.status = saida.codigoSaida === 0 && relatorio.falhas === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.metricas = relatorio;
        execucao.sumario = `${relatorio.sucessos}/${relatorio.testes} testes aprovados.`;
        return execucao;
    },
    async testesBackendIntegracao() {
        const execucao = criarExecucao("backend-integracao", "Backend integracao", "teste", "./gradlew :backend:integrationTest", "backend");
        const saida = await executarComando({
            comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
            args: [":backend:integrationTest"],
            cwd: DIRETORIO_RAIZ
        });
        const relatorio = await consolidarJUnit(path.join(DIRETORIO_RAIZ, "backend", "build", "test-results", "integrationTest"));
        execucao.status = saida.codigoSaida === 0 && relatorio.falhas === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.metricas = relatorio;
        execucao.sumario = `${relatorio.sucessos}/${relatorio.testes} testes aprovados.`;
        return execucao;
    },
    async coberturaBackend() {
        const execucao = criarExecucao("backend-cobertura", "Backend cobertura", "cobertura", "./gradlew :backend:jacocoTestReport", "backend");
        const saida = await executarComando({
            comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
            args: [":backend:jacocoTestReport"],
            cwd: DIRETORIO_RAIZ
        });
        const cobertura = await extrairCoberturaJacoco();
        execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.metricas = cobertura;
        execucao.sumario = `Cobertura: ${cobertura.linhas.percentual}% linhas, ${cobertura.branches.percentual}% branches.`;
        return execucao;
    },
    async coberturaFrontend() {
        const execucao = criarExecucao("frontend-cobertura", "Frontend cobertura", "cobertura", "npm --prefix frontend run coverage:unit:collect", "frontend");
        const saida = await executarComando({
            comando: "npm",
            args: ["--prefix", "frontend", "run", "coverage:unit:collect"],
            cwd: DIRETORIO_RAIZ
        });
        const cobertura = await extrairCoberturaFrontend();
        execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.metricas = cobertura;
        execucao.sumario = `Cobertura: ${cobertura.lines.percentual}% linhas.`;
        return execucao;
    },
    async lintFrontend() {
        const execucao = criarExecucao("frontend-lint", "Frontend lint", "qualidade", "npx eslint .", "frontend");
        const saida = await executarComando({
            comando: "npx",
            args: ["eslint", ".", "--max-warnings", "0"],
            cwd: path.join(DIRETORIO_RAIZ, "frontend")
        });
        execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.sumario = saida.codigoSaida === 0 ? "Lint sem problemas." : "Problemas de lint encontrados.";
        return execucao;
    },
    async tiposFrontend() {
        const execucao = criarExecucao("frontend-typecheck", "Frontend typecheck", "qualidade", "npm --prefix frontend run typecheck", "frontend");
        const saida = await executarComando({
            comando: "npm",
            args: ["--prefix", "frontend", "run", "typecheck"],
            cwd: DIRETORIO_RAIZ
        });
        execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.sumario = saida.codigoSaida === 0 ? "Typecheck sem erros." : "Erros de tipagem encontrados.";
        return execucao;
    },
    async residuosFrontend() {
        const execucao = criarExecucao("frontend-residuos", "Residuos do frontend", "qualidade", "node toolkit/sgc.js frontend residuos validar --json-resumido", ".");
        const saida = await executarComando({
            comando: "node",
            args: ["toolkit/sgc.js", "frontend", "residuos", "validar", "--json-resumido"],
            cwd: DIRETORIO_RAIZ
        });
        const resultado = parseJsonSeguro(saida.saida, {});
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
    async arquiteturaFrontend() {
        const execucao = criarExecucao("frontend-arquitetura", "Frontend arquitetura", "qualidade", "node toolkit/sgc.js frontend arquitetura auditar --json", ".");
        const saida = await executarComando({
            comando: "node",
            args: ["toolkit/sgc.js", "frontend", "arquitetura", "auditar", "--json"],
            cwd: DIRETORIO_RAIZ
        });
        const resultado = parseJsonSeguro(saida.saida, {});
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
    async identificadoresTesteFrontend() {
        const execucao = criarExecucao("frontend-identificadores-teste", "Identificadores de teste do frontend", "qualidade", "node toolkit/sgc.js frontend identificadores-teste listar-duplicados", ".");
        const saida = await executarComando({
            comando: "node",
            args: ["toolkit/sgc.js", "frontend", "identificadores-teste", "listar-duplicados"],
            cwd: DIRETORIO_RAIZ
        });
        execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
        execucao.sumario = saida.codigoSaida === 0 ? "Nenhum identificador de teste duplicado." : "Identificadores de teste duplicados encontrados.";
        return execucao;
    },
    async testesIntegracaoPlaywright() {
        const execucao = criarExecucao("e2e-playwright", "E2E Playwright", "teste", "npx playwright test --config=e2e/playwright.config.ts", ".");
        const saida = await executarComando({
            comando: "npx",
            args: ["playwright", "test", "--config=e2e/playwright.config.ts", "--reporter=json"],

            cwd: DIRETORIO_RAIZ,
            env: {CI: "1"}
        });
        const stats = JSON.parse(saida.saida || "{}").stats || {};
        execucao.status = saida.codigoSaida === 0 ? "sucesso" : "falha";
        registrarResultadoExecucao(execucao, saida);
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
    if (!PERFIS[perfil]) {
        throw new Error(`Perfil de qualidade invalido: ${perfil}`);
    }

    const diretorioExecucao = path.join(DIRETORIO_EXECUCOES, timestamp);

    await fs.mkdir(diretorioExecucao, {recursive: true});
    await fs.mkdir(DIRETORIO_MAIS_RECENTE, {recursive: true});

    const verificacoes = [];
    for (const adaptador of PERFIS[perfil]) {
        console.log(`Executando ${adaptador}...`);
        verificacoes.push(await ADAPTADORES[adaptador]());
    }

    const hotspotsResiduos = verificacoes
        .filter((item) => Array.isArray(item.metricas?.hotspots))
        .flatMap((item) => item.metricas.hotspots.map((hotspot) => ({
            nome: hotspot.arquivo,
            risco: hotspot.score,
            origem: item.codigo
        })))
        .toSorted((a, b) => b.risco - a.risco)
        .slice(0, 20);

    const fotografia = {
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
        hotspots: hotspotsResiduos
    };

    const caminhoFotografia = path.join(diretorioExecucao, NOME_ARQUIVO_FOTOGRAFIA);
    await fs.writeFile(caminhoFotografia, JSON.stringify(fotografia, null, 2));
    await fs.writeFile(path.join(DIRETORIO_MAIS_RECENTE, NOME_ARQUIVO_FOTOGRAFIA), JSON.stringify(fotografia, null, 2));
    console.log(`Fotografia gerada em ${caminhoRelativo(caminhoFotografia)}`);

    return fotografia;
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
    main().catch((erro) => {
        console.error(`Erro ao coletar qualidade: ${erro.message}`);
        process.exitCode = 1;
    });
}

export {
    ADAPTADORES,
    PERFIS,
    main
};
