import fs from "node:fs/promises";
import os from "node:os";
import path from "node:path";
import process from "node:process";
import {spawn} from "node:child_process";
import {parseStringPromise} from "xml2js";

const DIRETORIO_RAIZ = path.resolve(import.meta.dirname, "..", "..", "..");
const DIRETORIO_DASHBOARD = path.join(DIRETORIO_RAIZ, "etc", "qa-dashboard");
const DIRETORIO_RUNS = path.join(DIRETORIO_DASHBOARD, "runs");
const DIRETORIO_LATEST = path.join(DIRETORIO_DASHBOARD, "latest");
const VERSAO_SCHEMA = "1.0.0";

const PERFIS = {
    rapido: [
        "backendUnitario",
        "backendCobertura",
        "frontendCobertura",
        "frontendLint",
        "frontendTypecheck"
    ],
    completo: [
        "backendUnitario",
        "backendIntegracao",
        "backendCobertura",
        "frontendCobertura",
        "frontendLint",
        "frontendTypecheck",
        "e2ePlaywright"
    ],
    backend: [
        "backendUnitario",
        "backendIntegracao",
        "backendCobertura"
    ],
    frontend: [
        "frontendCobertura",
        "frontendLint",
        "frontendTypecheck"
    ]
};

function caminhoRelativo(caminhoAbsoluto) {
    return path.relative(DIRETORIO_RAIZ, caminhoAbsoluto).replaceAll("\\", "/");
}

function formatarTimestampArquivo(data = new Date()) {
    const texto = data.toISOString().replaceAll(":", "-").replace(/\.\d{3}Z$/, "Z");
    return texto;
}

function resumirComando(comando, args) {
    return [comando, ...args].join(" ");
}

function normalizarTexto(texto) {
    return texto.replaceAll("\r\n", "\n");
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

function parseArgs(argv) {
    const resultado = {
        perfil: "rapido"
    };

    for (let i = 0; i < argv.length; i += 1) {
        const atual = argv[i];
        if (atual === "--perfil" && argv[i + 1]) {
            resultado.perfil = argv[i + 1];
            i += 1;
            continue;
        }

        if (atual === "--ajuda" || atual === "-h") {
            resultado.ajuda = true;
        }
    }

    return resultado;
}

function imprimirAjuda() {
    console.log(`
Uso:
  node etc/qa-dashboard/scripts/coletar-snapshot.mjs --perfil rapido

Perfis disponiveis:
  rapido
  completo
  backend
  frontend
`.trim());
}

async function garantirDiretorios(diretorios) {
    await Promise.all(diretorios.map((diretorio) => fs.mkdir(diretorio, {recursive: true})));
}

function executarComando({comando, args, cwd, env}) {
    return new Promise((resolve) => {
        const inicio = Date.now();
        let comandoFinal = comando;
        let argsFinais = args;

        if (process.platform === "win32" && /\.(cmd|bat)$/i.test(comando)) {
            comandoFinal = process.env.ComSpec || "cmd.exe";
            argsFinais = ["/d", "/s", "/c", `"${comando}" ${args.join(" ")}`];
        }

        const processoFilho = spawn(comandoFinal, argsFinais, {
            cwd,
            env: {
                ...process.env,
                ...env
            },
            shell: false
        });

        let stdout = "";
        let stderr = "";

        processoFilho.stdout.on("data", (chunk) => {
            stdout += chunk.toString();
        });

        processoFilho.stderr.on("data", (chunk) => {
            stderr += chunk.toString();
        });

        processoFilho.on("close", (code) => {
            resolve({
                code: code ?? -1,
                stdout: normalizarTexto(stdout),
                stderr: normalizarTexto(stderr),
                duracaoMs: Date.now() - inicio
            });
        });

        processoFilho.on("error", (erro) => {
            resolve({
                code: -1,
                stdout: normalizarTexto(stdout),
                stderr: `${normalizarTexto(stderr)}\n${erro.message}`.trim(),
                duracaoMs: Date.now() - inicio
            });
        });
    });
}

async function lerArquivoSeExistir(caminho) {
    try {
        return await fs.readFile(caminho, "utf-8");
    } catch {
        return null;
    }
}

async function listarArquivosXml(diretorio) {
    try {
        const entradas = await fs.readdir(diretorio, {withFileTypes: true});
        return entradas
            .filter((entrada) => entrada.isFile() && entrada.name.endsWith(".xml"))
            .map((entrada) => path.join(diretorio, entrada.name));
    } catch {
        return [];
    }
}

function extrairInteiro(texto, regex, padrao = 0) {
    const match = texto.match(regex);
    return match ? Number.parseInt(match[1], 10) : padrao;
}

function extrairDecimal(texto, regex, padrao = 0) {
    const match = texto.match(regex);
    return match ? Number.parseFloat(match[1].replace(",", ".")) : padrao;
}

async function consolidarJUnit(diretorioRelatorio) {
    const arquivos = await listarArquivosXml(diretorioRelatorio);
    const totais = {
        testes: 0,
        falhas: 0,
        ignorados: 0,
        tempoSegundos: 0
    };

    for (const arquivo of arquivos) {
        const conteudo = await lerArquivoSeExistir(arquivo);
        if (!conteudo) {
            continue;
        }

        totais.testes += extrairInteiro(conteudo, /tests="(\d+)"/);
        totais.falhas += extrairInteiro(conteudo, /failures="(\d+)"/) + extrairInteiro(conteudo, /errors="(\d+)"/);
        totais.ignorados += extrairInteiro(conteudo, /skipped="(\d+)"/);
        totais.tempoSegundos += extrairDecimal(conteudo, /time="([0-9.]+)"/);
    }

    totais.sucessos = Math.max(totais.testes - totais.falhas - totais.ignorados, 0);
    totais.arquivosXml = arquivos.map(caminhoRelativo);
    return totais;
}

function calcularPercentual(covered, missed) {
    const total = covered + missed;
    if (total <= 0) {
        return 0;
    }

    return Number(((covered / total) * 100).toFixed(2));
}

async function extrairCoberturaJacoco(caminhoXml) {
    const conteudo = await lerArquivoSeExistir(caminhoXml);
    if (!conteudo) {
        throw new Error(`Relatorio JaCoCo nao encontrado em ${caminhoRelativo(caminhoXml)}`);
    }

    const relatorio = await parseStringPromise(conteudo);
    const counters = relatorio.report.counter ?? [];
    const classes = [];

    for (const pacote of relatorio.report.package ?? []) {
        for (const classe of pacote.class ?? []) {
            const nomeClasse = classe.$.name.replaceAll("/", ".");
            const countersClasse = classe.counter ?? [];
            const line = countersClasse.find((counter) => counter.$.type === "LINE");
            const branch = countersClasse.find((counter) => counter.$.type === "BRANCH");
            const covered = Number.parseInt(line?.$.covered ?? "0", 10);
            const missed = Number.parseInt(line?.$.missed ?? "0", 10);
            const percentual = calcularPercentual(covered, missed);

            classes.push({
                nome: nomeClasse,
                linhasPercentual: percentual,
                linhasCobertas: covered,
                linhasPerdidas: missed,
                branchesPercentual: calcularPercentual(
                    Number.parseInt(branch?.$.covered ?? "0", 10),
                    Number.parseInt(branch?.$.missed ?? "0", 10)
                )
            });
        }
    }

    const resumo = {};
    for (const counter of counters) {
        resumo[counter.$.type] = {
            cobertos: Number.parseInt(counter.$.covered, 10),
            perdidos: Number.parseInt(counter.$.missed, 10),
            percentual: calcularPercentual(
                Number.parseInt(counter.$.covered, 10),
                Number.parseInt(counter.$.missed, 10)
            )
        };
    }

    return {
        linhas: resumo.LINE ?? {cobertos: 0, perdidos: 0, percentual: 0},
        branches: resumo.BRANCH ?? {cobertos: 0, perdidos: 0, percentual: 0},
        instrucoes: resumo.INSTRUCTION ?? {cobertos: 0, perdidos: 0, percentual: 0},
        metodos: resumo.METHOD ?? {cobertos: 0, perdidos: 0, percentual: 0},
        classes: classes.sort((a, b) => a.linhasPercentual - b.linhasPercentual).slice(0, 20)
    };
}

async function extrairCoberturaFrontend(caminhoJson) {
    const conteudo = await lerArquivoSeExistir(caminhoJson);
    if (!conteudo) {
        throw new Error(`Relatorio V8 nao encontrado em ${caminhoRelativo(caminhoJson)}`);
    }

    const cobertura = JSON.parse(conteudo);
    const arquivos = [];
    const totais = {
        statements: {cobertos: 0, total: 0},
        branches: {cobertos: 0, total: 0},
        functions: {cobertos: 0, total: 0},
        lines: {cobertos: 0, total: 0}
    };

    for (const [arquivo, dados] of Object.entries(cobertura)) {
        const statementTotal = Object.keys(dados.s ?? {}).length;
        const statementCobertos = Object.values(dados.s ?? {}).filter((valor) => valor > 0).length;
        const functionTotal = Object.keys(dados.f ?? {}).length;
        const functionCobertos = Object.values(dados.f ?? {}).filter((valor) => valor > 0).length;
        const branchTotal = Object.values(dados.b ?? {}).reduce((total, valores) => total + valores.length, 0);
        const branchCobertos = Object.values(dados.b ?? {}).reduce(
            (total, valores) => total + valores.filter((valor) => valor > 0).length,
            0
        );
        const lineTotal = Object.keys(dados.statementMap ?? {}).length;
        const lineCobertos = statementCobertos;

        totais.statements.total += statementTotal;
        totais.statements.cobertos += statementCobertos;
        totais.functions.total += functionTotal;
        totais.functions.cobertos += functionCobertos;
        totais.branches.total += branchTotal;
        totais.branches.cobertos += branchCobertos;
        totais.lines.total += lineTotal;
        totais.lines.cobertos += lineCobertos;

        arquivos.push({
            arquivo: caminhoRelativo(arquivo),
            statementsPercentual: statementTotal > 0 ? Number(((statementCobertos / statementTotal) * 100).toFixed(2)) : 0,
            branchesPercentual: branchTotal > 0 ? Number(((branchCobertos / branchTotal) * 100).toFixed(2)) : 0,
            functionsPercentual: functionTotal > 0 ? Number(((functionCobertos / functionTotal) * 100).toFixed(2)) : 0,
            linesPercentual: lineTotal > 0 ? Number(((lineCobertos / lineTotal) * 100).toFixed(2)) : 0
        });
    }

    return {
        statements: {
            cobertos: totais.statements.cobertos,
            total: totais.statements.total,
            percentual: totais.statements.total > 0
                ? Number(((totais.statements.cobertos / totais.statements.total) * 100).toFixed(2))
                : 0
        },
        branches: {
            cobertos: totais.branches.cobertos,
            total: totais.branches.total,
            percentual: totais.branches.total > 0
                ? Number(((totais.branches.cobertos / totais.branches.total) * 100).toFixed(2))
                : 0
        },
        functions: {
            cobertos: totais.functions.cobertos,
            total: totais.functions.total,
            percentual: totais.functions.total > 0
                ? Number(((totais.functions.cobertos / totais.functions.total) * 100).toFixed(2))
                : 0
        },
        lines: {
            cobertos: totais.lines.cobertos,
            total: totais.lines.total,
            percentual: totais.lines.total > 0
                ? Number(((totais.lines.cobertos / totais.lines.total) * 100).toFixed(2))
                : 0
        },
        arquivos: arquivos.sort((a, b) => a.linesPercentual - b.linesPercentual).slice(0, 20)
    };
}

function extrairResumoVitest(saida) {
    const texto = `${saida.stdout}\n${saida.stderr}`;
    const testes = extrairInteiro(texto, /Tests\s+(\d+)\s+passed\s+\((\d+)\)/, 0) || extrairInteiro(texto, /(\d+)\s+passed/, 0);
    const falhas = extrairInteiro(texto, /(\d+)\s+failed/, 0);
    const ignorados = extrairInteiro(texto, /(\d+)\s+skipped/, 0);

    return {
        testes: testes + falhas + ignorados,
        sucessos: testes,
        falhas,
        ignorados
    };
}

function extrairResumoLint(saida) {
    const texto = `${saida.stdout}\n${saida.stderr}`;
    const erros = extrairInteiro(texto, /(\d+)\s+problems?/, 0);
    const avisos = extrairInteiro(texto, /(\d+)\s+warnings?/, 0);
    return {erros, avisos};
}

function extrairResumoTypecheck(saida) {
    const texto = `${saida.stdout}\n${saida.stderr}`;
    const erros = texto.split("\n").filter((linha) => linha.includes(" error TS")).length;
    return {erros};
}

function resumirTextoErro(saida, limiteLinhas = 12) {
    const linhas = `${saida.stdout}\n${saida.stderr}`
        .split("\n")
        .map((linha) => linha.trim())
        .filter(Boolean);

    return linhas.slice(Math.max(linhas.length - limiteLinhas, 0));
}

function extrairResumoPlaywright(jsonTexto) {
    const relatorio = JSON.parse(jsonTexto);
    const stats = relatorio.stats ?? {};
    const esperado = Number(stats.expected ?? 0);
    const inesperado = Number(stats.unexpected ?? 0);
    const ignorados = Number(stats.skipped ?? 0);
    const flaky = Number(stats.flaky ?? 0);
    const total = esperado + inesperado + ignorados + flaky;

    return {
        testes: total,
        sucessos: esperado,
        falhas: inesperado,
        ignorados,
        flaky
    };
}

function calcularStatusPorCodigoSaida(code, metricas = {}) {
    if (code !== 0) {
        return "falha";
    }

    if ((metricas.falhas ?? 0) > 0 || (metricas.erros ?? 0) > 0) {
        return "falha";
    }

    if ((metricas.avisos ?? 0) > 0 || (metricas.flaky ?? 0) > 0) {
        return "alerta";
    }

    return "sucesso";
}

async function coletarGit() {
    const branch = await executarComando({
        comando: "git",
        args: ["rev-parse", "--abbrev-ref", "HEAD"],
        cwd: DIRETORIO_RAIZ
    });
    const commit = await executarComando({
        comando: "git",
        args: ["rev-parse", "HEAD"],
        cwd: DIRETORIO_RAIZ
    });
    const commitCurto = await executarComando({
        comando: "git",
        args: ["rev-parse", "--short", "HEAD"],
        cwd: DIRETORIO_RAIZ
    });
    const status = await executarComando({
        comando: "git",
        args: ["status", "--porcelain"],
        cwd: DIRETORIO_RAIZ
    });

    return {
        branch: branch.stdout.trim(),
        commit: commit.stdout.trim(),
        commitCurto: commitCurto.stdout.trim(),
        worktreeSujo: status.stdout.trim().length > 0
    };
}

const ADAPTADORES = {
    async backendUnitario() {
        const execucao = criarExecucao(
            "backend-unitario",
            "Backend unitario",
            "teste",
            resumirComando("gradlew.bat", [":backend:unitTest"]),
            "backend"
        );
        const saida = await executarComando({
            comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
            args: [":backend:unitTest"],
            cwd: DIRETORIO_RAIZ
        });
        const relatorio = await consolidarJUnit(path.join(DIRETORIO_RAIZ, "backend", "build", "test-results", "unitTest"));

        execucao.status = calcularStatusPorCodigoSaida(saida.code, {falhas: relatorio.falhas});
        execucao.duracaoMs = saida.duracaoMs;
        execucao.metricas = relatorio;
        execucao.sumario = `${relatorio.sucessos}/${relatorio.testes} testes aprovados no backend unitario.`;
        execucao.erros = saida.code === 0 ? [] : resumirTextoErro(saida);
        execucao.artefatos = relatorio.arquivosXml;
        return execucao;
    },

    async backendIntegracao() {
        const execucao = criarExecucao(
            "backend-integracao",
            "Backend integracao",
            "teste",
            resumirComando("gradlew.bat", [":backend:integrationTest"]),
            "backend"
        );
        const saida = await executarComando({
            comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
            args: [":backend:integrationTest"],
            cwd: DIRETORIO_RAIZ
        });
        const relatorio = await consolidarJUnit(path.join(DIRETORIO_RAIZ, "backend", "build", "test-results", "integrationTest"));

        execucao.status = calcularStatusPorCodigoSaida(saida.code, {falhas: relatorio.falhas});
        execucao.duracaoMs = saida.duracaoMs;
        execucao.metricas = relatorio;
        execucao.sumario = `${relatorio.sucessos}/${relatorio.testes} testes aprovados no backend integracao.`;
        execucao.erros = saida.code === 0 ? [] : resumirTextoErro(saida);
        execucao.artefatos = relatorio.arquivosXml;
        return execucao;
    },

    async backendCobertura() {
        const execucao = criarExecucao(
            "backend-cobertura",
            "Backend cobertura",
            "cobertura",
            resumirComando("gradlew.bat", [":backend:jacocoTestReport"]),
            "backend"
        );
        const saida = await executarComando({
            comando: process.platform === "win32" ? "gradlew.bat" : "./gradlew",
            args: [":backend:jacocoTestReport"],
            cwd: DIRETORIO_RAIZ
        });
        const caminhoXml = path.join(DIRETORIO_RAIZ, "backend", "build", "reports", "jacoco", "test", "jacocoTestReport.xml");
        const cobertura = await extrairCoberturaJacoco(caminhoXml);

        execucao.status = calcularStatusPorCodigoSaida(saida.code);
        execucao.duracaoMs = saida.duracaoMs;
        execucao.metricas = cobertura;
        execucao.sumario = `Cobertura backend: ${cobertura.linhas.percentual}% de linhas e ${cobertura.branches.percentual}% de branches.`;
        execucao.erros = saida.code === 0 ? [] : resumirTextoErro(saida);
        execucao.artefatos = [caminhoRelativo(caminhoXml)];
        return execucao;
    },

    async frontendCobertura() {
        const execucao = criarExecucao(
            "frontend-cobertura",
            "Frontend cobertura",
            "cobertura",
            resumirComando("npm", ["run", "coverage:unit", "--prefix", "frontend"]),
            "frontend"
        );
        const saida = await executarComando({
            comando: process.platform === "win32" ? "npm.cmd" : "npm",
            args: ["run", "coverage:unit", "--prefix", "frontend"],
            cwd: DIRETORIO_RAIZ
        });
        const caminhoJson = path.join(DIRETORIO_RAIZ, "frontend", "coverage", "coverage-final.json");
        const cobertura = await extrairCoberturaFrontend(caminhoJson);
        const resumoVitest = extrairResumoVitest(saida);

        execucao.status = calcularStatusPorCodigoSaida(saida.code, {falhas: resumoVitest.falhas});
        execucao.duracaoMs = saida.duracaoMs;
        execucao.metricas = {
            ...resumoVitest,
            cobertura
        };
        execucao.sumario = `Cobertura frontend: ${cobertura.lines.percentual}% de linhas com ${resumoVitest.sucessos} testes aprovados.`;
        execucao.erros = saida.code === 0 ? [] : resumirTextoErro(saida);
        execucao.artefatos = [caminhoRelativo(caminhoJson)];
        return execucao;
    },

    async frontendLint() {
        const execucao = criarExecucao(
            "frontend-lint",
            "Frontend lint",
            "qualidade",
            resumirComando("npx", [
                "eslint",
                ".",
                "--ext",
                ".vue,.js,.mjs,.ts",
                "--max-warnings",
                "0",
                "--ignore-pattern",
                "coverage",
                "--ignore-pattern",
                "dist"
            ]),
            "frontend"
        );
        const saida = await executarComando({
            comando: process.platform === "win32" ? "npx.cmd" : "npx",
            args: [
                "eslint",
                ".",
                "--ext",
                ".vue,.js,.mjs,.ts",
                "--max-warnings",
                "0",
                "--ignore-pattern",
                "coverage",
                "--ignore-pattern",
                "dist"
            ],
            cwd: path.join(DIRETORIO_RAIZ, "frontend")
        });
        const resumo = extrairResumoLint(saida);

        execucao.status = calcularStatusPorCodigoSaida(saida.code, resumo);
        execucao.duracaoMs = saida.duracaoMs;
        execucao.metricas = resumo;
        execucao.sumario = resumo.erros > 0
            ? `Lint frontend encontrou ${resumo.erros} problemas.`
            : "Lint frontend sem problemas.";
        execucao.erros = saida.code === 0 ? [] : resumirTextoErro(saida);
        return execucao;
    },

    async frontendTypecheck() {
        const execucao = criarExecucao(
            "frontend-typecheck",
            "Frontend typecheck",
            "qualidade",
            resumirComando("npm", ["run", "typecheck", "--prefix", "frontend"]),
            "frontend"
        );
        const saida = await executarComando({
            comando: process.platform === "win32" ? "npm.cmd" : "npm",
            args: ["run", "typecheck", "--prefix", "frontend"],
            cwd: DIRETORIO_RAIZ
        });
        const resumo = extrairResumoTypecheck(saida);

        execucao.status = calcularStatusPorCodigoSaida(saida.code, resumo);
        execucao.duracaoMs = saida.duracaoMs;
        execucao.metricas = resumo;
        execucao.sumario = resumo.erros > 0
            ? `Typecheck frontend encontrou ${resumo.erros} erros.`
            : "Typecheck frontend sem erros.";
        execucao.erros = saida.code === 0 ? [] : resumirTextoErro(saida);
        return execucao;
    },

    async e2ePlaywright() {
        const execucao = criarExecucao(
            "e2e-playwright",
            "E2E Playwright",
            "teste",
            resumirComando("npx", ["playwright", "test", "--reporter=json"]),
            "."
        );
        const saida = await executarComando({
            comando: process.platform === "win32" ? "npx.cmd" : "npx",
            args: ["playwright", "test", "--reporter=json"],
            cwd: DIRETORIO_RAIZ,
            env: {
                CI: "1"
            }
        });
        const textoJson = saida.stdout.trim();
        const resumo = textoJson ? extrairResumoPlaywright(textoJson) : {testes: 0, sucessos: 0, falhas: 0, ignorados: 0, flaky: 0};

        execucao.status = calcularStatusPorCodigoSaida(saida.code, {falhas: resumo.falhas, flaky: resumo.flaky});
        execucao.duracaoMs = saida.duracaoMs;
        execucao.metricas = resumo;
        execucao.sumario = `${resumo.sucessos}/${resumo.testes} testes E2E aprovados.`;
        execucao.erros = saida.code === 0 ? [] : resumirTextoErro(saida);
        return execucao;
    }
};

function consolidarCobertura(verificacoes) {
    const backend = verificacoes.find((item) => item.codigo === "backend-cobertura")?.metricas ?? {};
    const frontend = verificacoes.find((item) => item.codigo === "frontend-cobertura")?.metricas?.cobertura ?? {};

    return {
        backend: {
            linhas: backend.linhas ?? null,
            branches: backend.branches ?? null,
            itensCriticos: backend.classes ?? []
        },
        frontend: {
            lines: frontend.lines ?? null,
            branches: frontend.branches ?? null,
            functions: frontend.functions ?? null,
            statements: frontend.statements ?? null,
            itensCriticos: frontend.arquivos ?? []
        }
    };
}

function consolidarQualidade(verificacoes) {
    return {
        lint: verificacoes.find((item) => item.codigo === "frontend-lint")?.metricas ?? {},
        typecheck: verificacoes.find((item) => item.codigo === "frontend-typecheck")?.metricas ?? {}
    };
}

function consolidarConfiabilidade(verificacoes) {
    const backendUnitario = verificacoes.find((item) => item.codigo === "backend-unitario")?.metricas ?? {};
    const backendIntegracao = verificacoes.find((item) => item.codigo === "backend-integracao")?.metricas ?? {};
    const frontend = verificacoes.find((item) => item.codigo === "frontend-cobertura")?.metricas ?? {};
    const e2e = verificacoes.find((item) => item.codigo === "e2e-playwright")?.metricas ?? {};

    return {
        testesIgnorados: (backendUnitario.ignorados ?? 0) + (backendIntegracao.ignorados ?? 0) + (frontend.ignorados ?? 0) + (e2e.ignorados ?? 0),
        testesFlaky: e2e.flaky ?? 0,
        suitesLentas: verificacoes
            .map((item) => ({
                codigo: item.codigo,
                nome: item.nome,
                duracaoMs: item.duracaoMs
            }))
            .sort((a, b) => b.duracaoMs - a.duracaoMs)
            .slice(0, 5)
    };
}

function construirHotspots(cobertura) {
    const hotspots = [];

    for (const classe of cobertura.backend?.itensCriticos ?? []) {
        hotspots.push({
            codigo: `backend:${classe.nome}`,
            nome: classe.nome,
            risco: Number((100 - classe.linhasPercentual).toFixed(2)),
            motivos: [`Cobertura backend baixa: ${classe.linhasPercentual}%`]
        });
    }

    for (const arquivo of cobertura.frontend?.itensCriticos ?? []) {
        hotspots.push({
            codigo: `frontend:${arquivo.arquivo}`,
            nome: arquivo.arquivo,
            risco: Number((100 - arquivo.linesPercentual).toFixed(2)),
            motivos: [`Cobertura frontend baixa: ${arquivo.linesPercentual}%`]
        });
    }

    return hotspots
        .sort((a, b) => b.risco - a.risco)
        .slice(0, 15);
}

function consolidarResumo(verificacoes) {
    const totais = {
        verificacoes: verificacoes.length,
        sucesso: verificacoes.filter((item) => item.status === "sucesso").length,
        falha: verificacoes.filter((item) => item.status === "falha").length,
        alerta: verificacoes.filter((item) => item.status === "alerta").length,
        naoExecutado: verificacoes.filter((item) => item.status === "nao_executado").length
    };

    let statusGeral = "verde";
    if (totais.falha > 0) {
        statusGeral = "vermelho";
    } else if (totais.alerta > 0) {
        statusGeral = "amarelo";
    }

    const indiceSaude = Math.max(
        0,
        Number((((totais.sucesso + (totais.alerta * 0.5)) / Math.max(totais.verificacoes, 1)) * 100).toFixed(2))
    );

    return {
        statusGeral,
        indiceSaude,
        totais
    };
}

function gerarResumoMarkdown(snapshot) {
    const linhas = [];

    linhas.push("# Resumo do Dashboard de QA");
    linhas.push("");
    linhas.push(`- Gerado em: ${snapshot.metadados.geradoEm}`);
    linhas.push(`- Perfil: ${snapshot.metadados.perfilExecucao}`);
    linhas.push(`- Branch: ${snapshot.metadados.git.branch}`);
    linhas.push(`- Commit: ${snapshot.metadados.git.commitCurto || snapshot.metadados.git.commit}`);
    linhas.push(`- Status geral: ${snapshot.resumo.statusGeral}`);
    linhas.push(`- Indice de saude: ${snapshot.resumo.indiceSaude}`);
    linhas.push("");
    linhas.push("## Verificacoes");
    linhas.push("");
    linhas.push("| Verificacao | Status | Duracao (s) | Sumario |");
    linhas.push("| --- | --- | ---: | --- |");

    for (const verificacao of snapshot.verificacoes) {
        linhas.push(`| ${verificacao.nome} | ${verificacao.status} | ${(verificacao.duracaoMs / 1000).toFixed(2)} | ${verificacao.sumario} |`);
    }

    linhas.push("");
    linhas.push("## Hotspots");
    linhas.push("");

    if (snapshot.hotspots.length === 0) {
        linhas.push("Sem hotspots calculados.");
    } else {
        for (const hotspot of snapshot.hotspots.slice(0, 10)) {
            linhas.push(`- ${hotspot.nome}: risco ${hotspot.risco}`);
        }
    }

    return `${linhas.join("\n")}\n`;
}

async function escreverJson(caminho, dados) {
    await fs.writeFile(caminho, `${JSON.stringify(dados, null, 2)}\n`, "utf-8");
}

async function main() {
    const args = parseArgs(process.argv.slice(2));
    if (args.ajuda) {
        imprimirAjuda();
        return;
    }

    if (!PERFIS[args.perfil]) {
        throw new Error(`Perfil invalido: ${args.perfil}`);
    }

    const inicio = Date.now();
    const timestamp = formatarTimestampArquivo();
    const diretorioExecucao = path.join(DIRETORIO_RUNS, timestamp);

    await garantirDiretorios([DIRETORIO_RUNS, DIRETORIO_LATEST, diretorioExecucao]);

    const verificacoes = [];

    for (const nomeAdaptador of PERFIS[args.perfil]) {
        if (!ADAPTADORES[nomeAdaptador]) {
            throw new Error(`Adaptador nao encontrado: ${nomeAdaptador}`);
        }

        console.log(`Executando ${nomeAdaptador}...`);
        const resultado = await ADAPTADORES[nomeAdaptador]();
        verificacoes.push(resultado);
    }

    const cobertura = consolidarCobertura(verificacoes);
    const qualidade = consolidarQualidade(verificacoes);
    const confiabilidade = consolidarConfiabilidade(verificacoes);
    const hotspots = construirHotspots(cobertura);
    const resumo = consolidarResumo(verificacoes);

    const snapshot = {
        versaoSchema: VERSAO_SCHEMA,
        metadados: {
            geradoEm: new Date().toISOString(),
            perfilExecucao: args.perfil,
            duracaoTotalMs: Date.now() - inicio,
            maquina: os.hostname(),
            sistemaOperacional: `${os.platform()} ${os.release()}`,
            git: await coletarGit()
        },
        resumo,
        verificacoes,
        cobertura,
        qualidade,
        confiabilidade,
        hotspots
    };

    const caminhoSnapshot = path.join(diretorioExecucao, "snapshot.json");
    const caminhoResumo = path.join(diretorioExecucao, "resumo.md");
    const conteudoResumo = gerarResumoMarkdown(snapshot);

    await escreverJson(caminhoSnapshot, snapshot);
    await fs.writeFile(caminhoResumo, conteudoResumo, "utf-8");
    await escreverJson(path.join(DIRETORIO_LATEST, "ultimo-snapshot.json"), snapshot);
    await fs.writeFile(path.join(DIRETORIO_LATEST, "ultimo-resumo.md"), conteudoResumo, "utf-8");

    console.log(`Snapshot gerado em ${caminhoRelativo(caminhoSnapshot)}`);
    console.log(`Resumo gerado em ${caminhoRelativo(caminhoResumo)}`);
}

main().catch((erro) => {
    console.error(erro.message);
    process.exitCode = 1;
});
