// Suprimir o DeprecationWarning DEP0190 - é necessário usar shell: true no Windows para .bat/.cmd
process.removeAllListeners('warning');
process.on('warning', (warning) => {
    if (warning.name === 'DeprecationWarning' && warning.code === 'DEP0190') {
        return; // Ignorar este warning específico
    }
    console.warn(warning.name, warning.message);
});

const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');

const BACKEND_DIR = path.resolve(__dirname, '../backend');
const FRONTEND_DIR = path.resolve(__dirname, '../frontend');
const BACKEND_PORT = 10000;
const FRONTEND_PORT = 5173;

let backendProcess = null;
let frontendProcess = null;

const LOG_FILTERS = [
    /WARNING:.*sun\.misc\.Unsafe/,
    /WARNING:.*lombok\.permit\.Permit/,
    /WARNING:.*will be removed in a future release/,
    /WARNING:.*Please consider reporting this to the maintainers/,
    /^> Task :/,
    /logStarted/,
    /UP-TO-DATE/,
    /Starting a Gradle Daemon.*Daemons could not be reused/,
    /Reusing configuration cache/,
    /Starting/,
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
            // Ignore if fails
        }
    }

    const gradlewExecutable = isWindows ? 'gradlew.bat' : './gradlew';
    const gradlewPath = path.resolve(BACKEND_DIR, `../${gradlewExecutable}`);
    
    // No Windows, .bat precisa de shell, mas passamos argumentos separadamente para evitar warning
    // No Unix, não precisa de shell
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
    // Matar apenas os processos que iniciamos, preservando Gradle Daemons
    if (backendProcess) {
        try {
            if (isWindows) {
                // /T mata a árvore de processos (Spring Boot), mas não afeta Daemons
                require('child_process').execSync(`taskkill /pid ${backendProcess.pid} /T /F`, { stdio: 'ignore' });
            } else {
                process.kill(-backendProcess.pid);
            }
        } catch (e) {
            try {
                backendProcess.kill();
            } catch (e2) { /* ignore */ }
        }
    }
    if (frontendProcess) {
        try {
             if (isWindows) {
                require('child_process').execSync(`taskkill /pid ${frontendProcess.pid} /T /F`, { stdio: 'ignore' });
            } else {
                process.kill(-frontendProcess.pid);
            }
        } catch (e) {
            try {
                frontendProcess.kill();
            } catch (e2) { /* ignore */ }
        }
    }
    
    // NÃO matar portas - isso mataria os Gradle Daemons!
    // Os processos Spring Boot e Vite já foram finalizados acima
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

// Start services
// NÃO matar portas aqui - preserva Gradle Daemons para reutilização
// Se houver conflito de porta, o processo falhará e será tratado

function checkBackendHealth() {
    return new Promise((resolve) => {
        const check = () => {
            const req = require('http').get(`http://localhost:${BACKEND_PORT}/`, (res) => {
                // Accept any response (200, 404, etc) - just need server to be responding
                if (res.statusCode >= 200 && res.statusCode < 500) {
                    resolve();
                } else {
                    setTimeout(check, 1000);
                }
            });
            req.on('error', () => {
                setTimeout(check, 1000);
            });
            req.end();
        };
        check();
    });
}

function checkFrontendHealth() {
    return new Promise((resolve) => {
        const check = () => {
            const req = require('http').get(`http://localhost:${FRONTEND_PORT}`, (res) => {
                // Accept any 2xx or 3xx response
                if (res.statusCode >= 200 && res.statusCode < 400) {
                    resolve();
                } else {
                    setTimeout(check, 1000);
                }
            });
            req.on('error', () => {
                setTimeout(check, 1000);
            });
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
    .then(() => {
        console.log('[LIFECYCLE] Frontend e Backend no ar!');
    });

// Keep alive
setInterval(() => { }, 1000);
