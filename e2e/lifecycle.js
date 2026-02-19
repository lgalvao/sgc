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
const BACKEND_PORT = 10000;
const FRONTEND_PORT = 5173;
const SMTP_PORT = 1025;

let backendProcess;
let frontendProcess;
let smtpServer;

// Criar/limpar arquivo de log ao iniciar
const LOG_FILE = path.resolve(__dirname, 'server.log');

// Local lightweight logger for this lifecycle script.
// Writes to console and appends to LOG_FILE.
const lifecycleLogger = {
    info: (msg) => {
        try { console.log(msg); } catch {}
        try { fs.appendFileSync(LOG_FILE, `[INFO] ${msg}\n`); } catch {}
    },
    warn: (msg) => {
        try { console.warn(msg); } catch {}
        try { fs.appendFileSync(LOG_FILE, `[WARN] ${msg}\n`); } catch {}
    },
    error: (msg) => {
        try { console.error(msg); } catch {}
        try { fs.appendFileSync(LOG_FILE, `[ERROR] ${msg}\n`); } catch {}
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

    // Gradle
    /^> Task :/, 
    /logStarted/,
    /UP-TO-DATE/, 
    /Starting a Gradle Daemon.*Daemons could not be reused/, 
    /Reusing configuration cache/, 
    /Starting/, 

    // Spring Boot - Inicialização
    /The following.*profile.*is active/, 
    /Initializing Spring/, 
    /Initializing Spring embedded WebApplicationContext/, 
    /Initializing Spring DispatcherServlet/, 
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
    /\[vite\] {2}connecting/,
    /\[vite\] {2}connected/,
    /debug:/,

    // Logs de serviços mockados
    /NotificacaoEmailServiceMock.*ATIVADO/, 
    /E-mails serão mockados/, 

    // Linhas vazias
    /^\s*$/
];

/**
 * Verifica se uma linha deve ser filtrada
 */
function shouldFilterLog(line) {
    return LOG_FILTERS.some(pattern => pattern.test(line));
}

/**
 * Loga dados do processo, filtrando mensagens indesejadas
 */
function log(prefix, data) {
    const lines = data.toString().split('\n');
    lines.forEach(line => {
        const trimmed = line.trim();
        if (trimmed && !shouldFilterLog(line)) {
            // Use lifecycleLogger to keep console + file in sync
            lifecycleLogger.info(`[${prefix}] ${line}`);
        }
    });
}

const isWindows = process.platform === 'win32';

function startBackend() {
    // Ensure gradlew is executable (Unix only)
    if (!isWindows) {
        fs.chmodSync(path.join(BACKEND_DIR, 'gradlew'), '755');
    }

    const gradlewExecutable = isWindows ? 'gradlew.bat' : './gradlew';
    const gradlewPath = path.resolve(BACKEND_DIR, `../${gradlewExecutable}`);

    const spawnOptions = {
        cwd: BACKEND_DIR,
        shell: isWindows,
        stdio: ['ignore', 'pipe', 'pipe']
    };

    backendProcess = spawn(gradlewPath, ['bootRun', '-PENV=e2e'], spawnOptions);

    backendProcess.stdout.on('data', data => log('BACKEND', data));
    backendProcess.stderr.on('data', data => log('BACKEND_ERR', data));

    backendProcess.on('exit', code => {
        if (code !== 0 && code !== null) {
            lifecycleLogger.error(`Backend exited with code ${code}`);
            process.exit(code);
        }
    });
}

function startFrontend() {
    const npmExecutable = isWindows ? 'npm.cmd' : 'npm';

    const spawnOptions = {
        cwd: FRONTEND_DIR,
        shell: isWindows,
        stdio: ['ignore', 'pipe', 'pipe']
    };

    frontendProcess = spawn(npmExecutable, ['run', 'dev'], spawnOptions);

    frontendProcess.stdout.on('data', data => log('FRONTEND', data));
    frontendProcess.stderr.on('data', data => log('FRONTEND_ERR', data));

    frontendProcess.on('exit', code => {
        if (code !== 0 && code !== null) {
            lifecycleLogger.error(`Frontend exited with code ${code}`);
            process.exit(code);
        }
    });
}

function startSmtpServer() {
    smtpServer = new SMTPServer({
        authOptional: true,
        onData(stream, session, callback) {
            let buffer = '';
            stream.on('data', (chunk) => {
                buffer += chunk;
            });
            stream.on('end', () => {
                log('SMTP', `E-mail recebido de ${session.envelope.mailFrom.address} para ${session.envelope.rcptTo.map(r => r.address).join(', ')}`);
                // Se precisar do corpo do e-mail para debug futuro, pode logar 'buffer' aqui
                callback();
            });
        }
    });

    smtpServer.on('error', err => {
        lifecycleLogger.error(`Erro no servidor SMTP: ${err && err.message ? err.message : err}`);
    });

    smtpServer.listen(SMTP_PORT, '0.0.0.0', () => {
        log('SMTP', `Servidor SMTP rodando na porta ${SMTP_PORT}`);
    });
}

function stopProcess(proc, isWindows) {
    if (!proc) return;
    try {
        if (isWindows) {
            execSync(`taskkill /pid ${proc.pid} /T /F`, {stdio: 'ignore'});
        } else {
            process.kill(-proc.pid);
        }
    } catch {
        proc.kill();
    }
}

function cleanup() {
    // Matar apenas processos iniciados, preservando Gradle Daemons
    stopProcess(backendProcess, isWindows);
    stopProcess(frontendProcess, isWindows);
    if (smtpServer) {
        smtpServer.close();
    }
}

// Handle exit signals
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

function checkBackendHealth() {
    return new Promise((resolve) => {
        const check = () => {
            // Uso de http intencional para health check local (localhost).
            // O ambiente de desenvolvimento local não utiliza HTTPS por padrão.
            const req = http.get(`http://localhost:${BACKEND_PORT}/`, (res) => {
                if (res.statusCode >= 200 && res.statusCode < 500) resolve(); else setTimeout(check, 1000);
            });
            req.on('error', () => setTimeout(check, 1000));
            req.end();
        };
        check();
    });
}

function checkFrontendHealth() {
    return new Promise((resolve) => {
        const check = () => {
            // Uso de http intencional para health check local (localhost).
            const req = http.get(`http://localhost:${FRONTEND_PORT}`, (res) => {
                if (res.statusCode >= 200 && res.statusCode < 400) resolve(); else setTimeout(check, 1000);
            });
            req.on('error', () => setTimeout(check, 1000));
            req.end();
        };
        check();
    });
}

startBackend();
startSmtpServer();
try {
    await checkBackendHealth();
    startFrontend();
    await checkFrontendHealth();
    lifecycleLogger.info('>>> Frontend, Backend e SMTP no ar!');
} catch (error) {
    lifecycleLogger.error(`Erro ao iniciar infraestrutura de testes: ${error && error.message ? error.message : error}`);
    process.exit(1);
}

// Keep alive
setInterval(() => {
}, 1000);