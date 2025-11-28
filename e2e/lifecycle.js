const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');

// Configuration
const BACKEND_DIR = path.resolve(__dirname, '../backend');
const FRONTEND_DIR = path.resolve(__dirname, '../frontend');
const BACKEND_PORT = 10000;
const FRONTEND_PORT = 5173;

let backendProcess = null;
let frontendProcess = null;

function log(prefix, data) {
    const lines = data.toString().split('\n');
    lines.forEach(line => {
        if (line.trim()) {
            console.log(`[${prefix}] ${line}`);
        }
    });
}

function startBackend() {
    console.log('Starting Backend...');
    // Ensure gradlew is executable
    try {
        fs.chmodSync(path.join(BACKEND_DIR, 'gradlew'), '755');
    } catch (e) {
        // Ignore if fails, might already be executable or windows
    }

    const gradlewPath = path.resolve(BACKEND_DIR, '../gradlew');
    backendProcess = spawn(gradlewPath, ['bootRun', '--args=--spring.profiles.active=e2e'], {
        cwd: BACKEND_DIR,
        shell: false,
        stdio: ['ignore', 'pipe', 'pipe']
    });

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
    console.log('Starting Frontend...');
    frontendProcess = spawn('npm', ['run', 'dev'], {
        cwd: FRONTEND_DIR,
        shell: false,
        stdio: ['ignore', 'pipe', 'pipe']
    });

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
    console.log('Cleaning up processes...');
    if (backendProcess) {
        // On Windows/Shell spawned processes, we might need tree-kill, but for now try simple kill
        // Since we used shell: true, the PID is the shell, not the java process.
        // But let's try standard kill first.
        // Actually, for robust cleanup in CI/Dev, relying on Playwright's process group cleanup is often safer,
        // but explicit kill helps.
        // Using negative PID to kill process group if possible
        try {
            process.kill(-backendProcess.pid);
        } catch (e) {
            try {
                backendProcess.kill();
            } catch (e2) { /* ignore */ }
        }
    }
    if (frontendProcess) {
        try {
            process.kill(-frontendProcess.pid);
        } catch (e) {
            try {
                frontendProcess.kill();
            } catch (e2) { /* ignore */ }
        }
    }

    // Force kill any remaining on ports
    try {
        require('child_process').execSync(`lsof -ti:${BACKEND_PORT} | xargs kill -9 2>/dev/null`);
        require('child_process').execSync(`lsof -ti:${FRONTEND_PORT} | xargs kill -9 2>/dev/null`);
    } catch (e) { /* ignore */ }
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
// Kill existing first
try {
    require('child_process').execSync(`lsof -ti:${BACKEND_PORT} | xargs kill -9 2>/dev/null`);
    require('child_process').execSync(`lsof -ti:${FRONTEND_PORT} | xargs kill -9 2>/dev/null`);
} catch (e) { /* ignore */ }

function checkBackendHealth() {
    return new Promise((resolve) => {
        const check = () => {
            const req = require('http').get(`http://localhost:${BACKEND_PORT}/actuator/health`, (res) => {
                if (res.statusCode === 200) {
                    console.log('[LIFECYCLE] Backend is healthy!');
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
        console.log('[LIFECYCLE] Waiting for Backend to be healthy...');
        check();
    });
}

startBackend();
checkBackendHealth().then(() => {
    startFrontend();
});

// Keep alive
setInterval(() => { }, 1000);
