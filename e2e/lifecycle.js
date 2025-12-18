// Suprimir o DeprecationWarning DEP0190 - é necessário usar shell: true no Windows para .bat/.cmd
process.removeAllListeners('warning');
process.on('warning', (warning) => {
    if (warning.name === 'DeprecationWarning' && warning.code === 'DEP0190') return;
    console.warn(warning.name, warning.message);
});

const {spawn} = require('child_process');
const path = require('path');
const fs = require('fs');

const BACKEND_DIR = path.resolve(__dirname, '../backend');
const FRONTEND_DIR = path.resolve(__dirname, '../frontend');
const BACKEND_PORT = 10000;
const FRONTEND_PORT = 5173;

// Criar/limpar arquivo de log ao iniciar
const LOG_FILE = path.resolve(__dirname, 'server.log');
try {
    fs.writeFileSync(LOG_FILE, '');
} catch (e) {
    console.error('Não foi possível criar o arquivo de log:', e.message);
}

let backendProcess = null;
let frontendProcess = null;

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
    /➜  Local:/,
    /➜  Network:/,
    /use --host to expose/,
    /\[vite\] connecting/,
    /\[vite\] connected/,

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
            console.log(line);
            try {
                fs.appendFileSync(LOG_FILE, `[${prefix}] ${line}\n`);
            } catch (e) { /* ignore */
            }
        }
    });
}

const isWindows = process.platform === 'win32';

function startBackend() {
    // Ensure gradlew is executable (Unix only)
    if (!isWindows) {
        try {
            fs.chmodSync(path.join(BACKEND_DIR, 'gradlew'), '755');
        } catch (e) {
            // Ignorar se falhar
        }
    }

    const gradlewExecutable = isWindows ? 'gradlew.bat' : './gradlew';
    const gradlewPath = path.resolve(BACKEND_DIR, `../${gradlewExecutable}`);

    const spawnOptions = {
        cwd: BACKEND_DIR,
        shell: isWindows,
        stdio: ['ignore', 'pipe', 'pipe']
    };

    backendProcess = spawn(gradlewPath, ['bootRun', '--args=--spring.profiles.active=e2e'], spawnOptions);

    backendProcess.stdout.on('data', data => log('BACKEND', data));
    backendProcess.stderr.on('data', data => log('BACKEND_ERR', data));

    backendProcess.on('exit', code => {
        if (code !== 0 && code !== null) {
            console.error(`Backend exited with code ${code}`);
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
            console.error(`Frontend exited with code ${code}`);
            process.exit(code);
        }
    });
}

function cleanup() {
    // Matar apenas processos iniciados, preservando Gradle Daemons
    if (backendProcess) {
        try {
            if (isWindows) {
                // /T mata a árvore de processos (Spring Boot), mas não afeta Daemons
                require('child_process').execSync(`taskkill /pid ${backendProcess.pid} /T /F`, {stdio: 'ignore'});
            } else {
                process.kill(-backendProcess.pid);
            }
        } catch (e) {
            try {
                backendProcess.kill();
            } catch (e2) { /* ignore */
            }
        }
    }

    if (frontendProcess) {
        try {
            if (isWindows) {
                require('child_process').execSync(`taskkill /pid ${frontendProcess.pid} /T /F`, {stdio: 'ignore'});
            } else {
                process.kill(-frontendProcess.pid);
            }
        } catch (e) {
            try {
                frontendProcess.kill();
            } catch (e2) { /* ignore */
            }
        }
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
            const req = require('http').get(`http://localhost:${BACKEND_PORT}/`, (res) => {
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
            const req = require('http').get(`http://localhost:${FRONTEND_PORT}`, (res) => {
                if (res.statusCode >= 200 && res.statusCode < 400) resolve(); else setTimeout(check, 1000);
            });
            req.on('error', () => setTimeout(check, 1000));
            req.end();
        };
        check();
    });
}

startBackend();
checkBackendHealth()
    .then(() => {
        startFrontend();
        return checkFrontendHealth();
    })
    .then(() => console.log('>>> Frontend e Backend no ar!'));

// Keep alive
setInterval(() => {
}, 1000);
