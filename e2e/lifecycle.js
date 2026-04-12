import {execSync, spawn} from 'node:child_process';
import path from 'node:path';
import fs from 'node:fs';
import http from 'node:http';
import {fileURLToPath} from 'node:url';
import {SMTPServer} from 'smtp-server';

// Recreate __dirname for ES modules
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const BACKEND_DIR = path.resolve(__dirname, '../backend');
const FRONTEND_DIR = path.resolve(__dirname, '../frontend');

const PERFIL_LIFECYCLE = process.env.SGC_LIFECYCLE_PROFILE || 'e2e';
const BACKEND_BASE_PORT = Number.parseInt(process.env.E2E_BACKEND_BASE_PORT || '10000', 10);
const FRONTEND_PORT = Number.parseInt(process.env.E2E_FRONTEND_PORT || '5173', 10);
const SMTP_PORT = Number.parseInt(process.env.E2E_SMTP_PORT || '1025', 10);
const DB_NAME_PREFIX = process.env.E2E_DB_NAME_PREFIX || 'sgc-e2e-w';
const MAX_BACKEND_PORT_SCAN = Number.parseInt(process.env.E2E_BACKEND_PORT_SCAN_LIMIT || '20', 10);
const MODO_MONITORAMENTO = process.env.SGC_MONITORAMENTO || 'off';
const REUTILIZAR_EXISTENTE = process.env.SGC_LIFECYCLE_REUTILIZAR_EXISTENTE || 'on';
const LIMITE_MONITORAMENTO_ALERTA_MS = Number.parseInt(process.env.SGC_MONITORAMENTO_ALERTA_MS || '500', 10);
const TAXA_AMOSTRAGEM_MONITORAMENTO = process.env.SGC_MONITORAMENTO_AMOSTRAGEM || '0.0';
const SILENT_LOGS = process.env.SGC_SILENT_LIFECYCLE === 'true';
const ANSI_RESET = '\u001b[0m';
const ANSI_AMARELO = '\u001b[33m';
const ANSI_VERMELHO = '\u001b[31m';

const backendProcessos = [];
const frontendProcessos = [];
let smtpServer;
let backendPortSelecionado = BACKEND_BASE_PORT;

// Criar/limpar arquivo de log ao iniciar
const LOG_FILE = path.resolve(__dirname, 'server.log');

// Local lightweight logger for this lifecycle script.
// Writes to console and appends to LOG_FILE.
const lifecycleLogger = {
    info: (msg) => {
        try {
            if (!SILENT_LOGS) console.log(msg);
        } catch {
            // Ignorar erro de log
        }
        try {
            fs.appendFileSync(LOG_FILE, `[INFO] ${msg}\n`);
        } catch {
            // Ignorar erro de escrita em arquivo
        }
    },
    warn: (msg) => {
        try {
            if (!SILENT_LOGS) console.warn(msg);
        } catch {
            // Ignorar erro de log
        }
        try {
            fs.appendFileSync(LOG_FILE, `[WARN] ${msg}\n`);
        } catch {
            // Ignorar erro de escrita em arquivo
        }
    },
    error: (msg) => {
        try {
            console.error(msg); // Error we always print
        } catch {
            // Ignorar erro de log
        }
        try {
            fs.appendFileSync(LOG_FILE, `[ERROR] ${msg}\n`);
        } catch {
            // Ignorar erro de escrita em arquivo
        }
    }
};

try {
    fs.writeFileSync(LOG_FILE, '');
} catch (e) {
    // If LOG_FILE creation fails, fallback to console
    console.error('Não foi possível criar o arquivo de log:', e && e.message ? e.message : e);
}

// Suprimir o DeprecationWarning DEP0190 - é necessário usar shell: true no Windows para .bat/.cmd
process.removeAllListeners('warning');
process.on('warning', (warning) => {
    if (warning.name === 'DeprecationWarning' && warning.code === 'DEP0190') return;
    lifecycleLogger.warn(`${warning.name} ${warning.message}`);
});

const LOG_FILTERS = [
    // Warnings do Lombok
    /WARNING:/,

    /^> Task :/,
    /Calculating task graph as no cached configuration is available/,
    /logStarted/,
    /UP-TO-DATE/,
    /Starting a Gradle daemon.*Daemons could not be reused/,
    /Reusing configuration cache/,
    /Starting/,
    /Perfil Spring ativado:/,
    /Carregando configurações de:/,
    /Arquivo \.env\..*não encontrado, usando configurações padrão/,

    // Spring Boot - Inicialização
    /The following.*profile.*is active/,
    /Initializing spring/,
    /Initializing Spring DispatcherServlet/,
    /Initializing spring embedded WebApplicationContext/,
    /Initializing spring DispatcherServlet/,
    /Started .* in .* seconds/,
    /Tomcat started on port/,
    /Tomcat initialized with port/,

    // Vite - Inicialização
    /^> sgc@.*dev$/,
    /^> vite$/,
    /VITE v.* ready in/,
    /➜ {2}Local:/,
    /➜ {2}Network:/,
    /use --host to expose/,
    // eslint-disable-next-line no-control-regex
    /\u001b\[vite] {2}connecting/,
    // eslint-disable-next-line no-control-regex
    /\u001b\[vite] {2}connected/,
    /debug:/,

    // Logs de serviços mockados
    /NotificacaoEmailServiceMock.*ATIVADO/,
    /E-mails serão mockados/,

     
    /^\s*$/
];

function shouldFilterLog(line) {
    return LOG_FILTERS.some(pattern => pattern.test(line));
}

function normalizarLinhaBackend(line) {
    const semPrefixoLogger = line.replace(
        /^(INFO|WARN|ERROR|DEBUG)\s+[^:]+(?::[^:]+)?:(\d+):\s+/,
        (_match, nivel) => nivel === 'INFO' ? '' : `${nivel} `
    ).trim();
    return semPrefixoLogger;
}

function colorirLinha(line) {
    if (line.startsWith('HTTP-MUITO-LENTO')) {
        return `${ANSI_VERMELHO}${line}${ANSI_RESET}`;
    }

    if (line.startsWith('HTTP-LENTO') || line.startsWith('WARN ')) {
        return `${ANSI_AMARELO}${line}${ANSI_RESET}`;
    }

    if (line.startsWith('ERROR ')) {
        return `${ANSI_VERMELHO}${line}${ANSI_RESET}`;
    }

    return line;
}

function formatarLinha(prefix, line) {
    const trimmed = line.trim();
    if (!trimmed) {
        return null;
    }

    if (prefix === 'BACKEND' || prefix === 'BACKEND_ERR') {
        return colorirLinha(normalizarLinhaBackend(trimmed));
    }

    if (prefix === 'FRONTEND' || prefix === 'FRONTEND_ERR') {
        return trimmed;
    }

    return `[${prefix}] ${trimmed}`;
}

function log(prefix, data) {
    const lines = data.toString().split('\n');
    lines.forEach(line => {
        const trimmed = line.trim();
        if (trimmed && !shouldFilterLog(line)) {
            const linhaFormatada = formatarLinha(prefix, line);
            if (linhaFormatada) {
                lifecycleLogger.info(linhaFormatada);
            }
        }
    });
}

const isWindows = process.platform === 'win32';

function normalizarEnv(baseEnv = process.env) {
    const env = {...baseEnv};
    if (env.FORCE_COLOR && env.NO_COLOR) {
        delete env.FORCE_COLOR;
    }
    return env;
}

function portaBackend() {
    return backendPortSelecionado;
}

function modoHomologacao() {
    return PERFIL_LIFECYCLE === 'hom';
}

function modoE2e() {
    return PERFIL_LIFECYCLE === 'e2e';
}

function validarPerfilLifecycle() {
    const perfisSuportados = new Set(['e2e', 'hom']);
    if (!perfisSuportados.has(PERFIL_LIFECYCLE)) {
        throw new Error(
            `Perfil de lifecycle inválido: ${PERFIL_LIFECYCLE}. ` +
            'Use SGC_LIFECYCLE_PROFILE=e2e ou SGC_LIFECYCLE_PROFILE=hom.'
        );
    }
}

function validarModoMonitoramento() {
    const modosSuportados = new Set(['off', 'on']);
    if (!modosSuportados.has(MODO_MONITORAMENTO)) {
        throw new Error(
            `Modo de monitoramento inválido: ${MODO_MONITORAMENTO}. ` +
            'Use SGC_MONITORAMENTO=off ou on.'
        );
    }
}

function validarReutilizacaoExistente() {
    const modosSuportados = new Set(['off', 'on']);
    if (!modosSuportados.has(REUTILIZAR_EXISTENTE)) {
        throw new Error(
            `Modo de reutilização inválido: ${REUTILIZAR_EXISTENTE}. ` +
            'Use SGC_LIFECYCLE_REUTILIZAR_EXISTENTE=off ou on.'
        );
    }
}

function monitoramentoAtivo() {
    return MODO_MONITORAMENTO !== 'off';
}

function monitoramentoTraceCompleto() {
    return MODO_MONITORAMENTO === 'on';
}

function monitoramentoPermiteHeader() {
    return MODO_MONITORAMENTO === 'on';
}

function requestStatus(url, method = 'GET') {
    return new Promise((resolve) => {
        const req = http.request(url, {method}, (res) => {
            const status = res.statusCode ?? null;
            res.resume();
            resolve(status);
        });
        req.on('error', () => resolve(null));
        req.end();
    });
}

async function resolverBackendExistenteOuPortaLivre() {
    if (!modoE2e()) {
        throw new Error('resolverBackendExistenteOuPortaLivre só pode ser usado no modo e2e.');
    }

    for (let offset = 0; offset < MAX_BACKEND_PORT_SCAN; offset++) {
        const porta = BACKEND_BASE_PORT + offset;
        const status = await requestStatus(`http://localhost:${porta}/e2e/reset-database`);

        if (status === null) {
            return {porta, reutilizar: false};
        }

        if (status === 405) {
            if (REUTILIZAR_EXISTENTE === 'off') {
                throw new Error(
                    `Backend E2E já está ativo na porta ${porta}. ` +
                    'Finalize a instância anterior antes de iniciar o lifecycle novamente.'
                );
            }
            lifecycleLogger.warn(`Backend E2E já ativo na porta ${porta}; reutilizando instância existente.`);
            return {porta, reutilizar: true};
        }

        lifecycleLogger.warn(
            `Porta ${porta} ocupada por serviço incompatível com E2E ` +
            `(status ${status} em /e2e/reset-database). Tentando próxima porta.`
        );
    }

    throw new Error(
        `Nenhuma porta disponível para backend E2E a partir de ${BACKEND_BASE_PORT} ` +
        `(limite de varredura: ${MAX_BACKEND_PORT_SCAN}).`
    );
}

function dbUrl() {
    return `jdbc:h2:mem:${DB_NAME_PREFIX};DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`;
}

function startBackend() {
    if (!isWindows) {
        fs.chmodSync(path.join(BACKEND_DIR, 'gradlew'), '755');
    }

    const gradlewExecutable = isWindows ? 'gradlew.bat' : './gradlew';
    const gradlewPath = path.resolve(BACKEND_DIR, `../${gradlewExecutable}`);

    const backendPort = portaBackend();
    const argsAplicacao = modoHomologacao()
        ? [
            `--server.port=${backendPort}`,
            `--CORS_ALLOWED_ORIGINS=http://localhost:${FRONTEND_PORT},http://localhost:4173`,
            `--sgc.monitoramento.ativo=${monitoramentoAtivo()}`,
            `--sgc.monitoramento.trace-completo=${monitoramentoTraceCompleto()}`,
            `--sgc.monitoramento.limite-alerta-ms=${LIMITE_MONITORAMENTO_ALERTA_MS}`,
            `--sgc.monitoramento.permitir-ativacao-por-header=${monitoramentoPermiteHeader()}`,
            `--sgc.monitoramento.taxa-amostragem=${TAXA_AMOSTRAGEM_MONITORAMENTO}`
        ].join(' ')
        : [
            `--server.port=${backendPort}`,
            `--spring.datasource.url=${dbUrl()}`,
            `--CORS_ALLOWED_ORIGINS=http://localhost:${FRONTEND_PORT},http://localhost:4173`,
            `--sgc.monitoramento.ativo=${monitoramentoAtivo()}`,
            `--sgc.monitoramento.trace-completo=${monitoramentoTraceCompleto()}`,
            `--sgc.monitoramento.limite-alerta-ms=${LIMITE_MONITORAMENTO_ALERTA_MS}`,
            `--sgc.monitoramento.permitir-ativacao-por-header=${monitoramentoPermiteHeader()}`,
            `--sgc.monitoramento.taxa-amostragem=${TAXA_AMOSTRAGEM_MONITORAMENTO}`
        ].join(' ');
    const argsGradle = isWindows ? `--args="${argsAplicacao}"` : `--args=${argsAplicacao}`;

    const spawnOptions = {
        cwd: BACKEND_DIR,
        shell: isWindows,
        stdio: ['ignore', 'pipe', 'pipe'],
        env: normalizarEnv()
    };

    const backendProcess = spawn(gradlewPath, ['bootRun', `-PENV=${PERFIL_LIFECYCLE}`, argsGradle], spawnOptions);
    backendProcessos.push(backendProcess);

    backendProcess.stdout.on('data', data => log(`BACKEND`, data));
    backendProcess.stderr.on('data', data => log(`BACKEND_ERR`, data));

    backendProcess.on('exit', code => {
        if (code !== 0 && code !== null) {
            lifecycleLogger.error(`Backend saiu com código ${code}`);
            process.exit(code);
        }
    });
}

function startFrontend() {
    const npmExecutable = isWindows ? 'npm.cmd' : 'npm';

    const spawnOptions = {
        cwd: FRONTEND_DIR,
        shell: isWindows,
        stdio: ['ignore', 'pipe', 'pipe'],
        env: {
            ...normalizarEnv(),
            E2E_BACKEND_BASE_PORT: String(BACKEND_BASE_PORT),
            E2E_WORKER_COUNT: '1',
            VITE_MONITORAMENTO_MODO: MODO_MONITORAMENTO
        }
    };

    const frontendProcess = spawn(
        npmExecutable,
        ['run', 'dev', '--', '--port', String(FRONTEND_PORT)],
        spawnOptions
    );
    frontendProcessos.push(frontendProcess);

    frontendProcess.stdout.on('data', data => log('FRONTEND', data));
    frontendProcess.stderr.on('data', data => log('FRONTEND_ERR', data));

    frontendProcess.on('exit', code => {
        if (code !== 0 && code !== null) {
            lifecycleLogger.error(`Frontend saiu com código ${code}`);
            process.exit(code);
        }
    });
}

async function validarPortaFrontendLivre() {
    const status = await requestStatus(`http://localhost:${FRONTEND_PORT}`);
    if (status !== null) {
        throw new Error(
            `Frontend já está ativo na porta ${FRONTEND_PORT}. ` +
            'Finalize a instância anterior antes de iniciar o lifecycle novamente.'
        );
    }
}

function startSmtpServer() {
    smtpServer = new SMTPServer({
        authOptional: true,
        onData(stream, _session, callback) {
            stream.on('data', () => {
            });
            stream.on('end', () => {
                callback();
            });
        }
    });

    smtpServer.on('error', err => {
        lifecycleLogger.error(`Erro no servidor SMTP: ${err && err.message ? err.message : err}`);
    });

    smtpServer.listen(SMTP_PORT, '0.0.0.0', () => {
        log('SMTP', `Servidor SMTP local executando na porta ${SMTP_PORT}`);
    });
}

function stopProcess(proc, windows) {
    if (!proc) return;
    try {
        if (windows) {
            execSync(`taskkill /pid ${proc.pid} /T /F`, {stdio: 'ignore'});
        } else {
            process.kill(-proc.pid);
        }
    } catch {
        proc.kill();
    }
}

function cleanup() {
    for (const p of backendProcessos) stopProcess(p, isWindows);
    for (const p of frontendProcessos) stopProcess(p, isWindows);
    if (smtpServer) {
        smtpServer.close();
    }
}

process.on('SIGINT', () => {
    cleanup();
    process.exit();
});

process.on('SIGTERM', () => {
    cleanup();
    process.exit();
});

process.on('exit', () => {
    cleanup();
});

function checkHttpHealth(url, expectedMin, expectedMax) {
    return new Promise((resolve) => {
        const check = () => {
            const req = http.get(url, (res) => {
                if (res.statusCode >= expectedMin && res.statusCode < expectedMax) {
                    resolve();
                } else {
                    setTimeout(check, 1000);
                }
            });
            req.on('error', () => setTimeout(check, 1000));
            req.end();
        };
        check();
    });
}

async function subirBackends() {
    if (modoE2e()) {
        const backendResolvido = await resolverBackendExistenteOuPortaLivre();
        backendPortSelecionado = backendResolvido.porta;

        if (!backendResolvido.reutilizar) {
            startBackend();
        }
    } else {
        backendPortSelecionado = BACKEND_BASE_PORT;
        const status = await requestStatus(`http://localhost:${portaBackend()}/swagger-ui.html`);
        if (status !== null) {
            throw new Error(
                `Porta ${portaBackend()} já está ocupada. ` +
                'No modo hom o lifecycle não reutiliza backend existente para evitar acoplamento a um serviço inesperado.'
            );
        }
        startBackend();
    }

    const urlHealth = modoHomologacao()
        ? `http://localhost:${portaBackend()}/swagger-ui.html`
        : `http://localhost:${portaBackend()}/`;
    const expectedMax = modoHomologacao() ? 400 : 500;
    await checkHttpHealth(urlHealth, 200, expectedMax);
}

async function subirFrontend() {
    await validarPortaFrontendLivre();
    startFrontend();
    await checkHttpHealth(`http://localhost:${FRONTEND_PORT}`, 200, 400);
}

async function subirInfra() {
    await subirBackends();
    await subirFrontend();
}

function descreverBackends() {
    return `Backend: ${portaBackend()} (perfil ${PERFIL_LIFECYCLE})`;
}

try {
    validarPerfilLifecycle();
    validarModoMonitoramento();
    validarReutilizacaoExistente();
    if (modoE2e() || modoHomologacao()) {
        startSmtpServer();
    } else {
        lifecycleLogger.warn(
            'Lifecycle iniciado em modo hom. Endpoints /e2e, seed.sql e rotinas de limpeza não devem estar ativos.'
        );
    }
    await subirInfra();
    lifecycleLogger.info(
        `>>> Infra ${PERFIL_LIFECYCLE.toUpperCase()} no ar. Frontend: ${FRONTEND_PORT}. ` +
        `${descreverBackends()}. Monitoramento: ${MODO_MONITORAMENTO}.`
    );
} catch (error) {
    lifecycleLogger.error(`Erro ao iniciar infra de testes: ${error && error.message ? error.message : error}`);
    process.exit(1);
}

setInterval(() => {
}, 1000);
