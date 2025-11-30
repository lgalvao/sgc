const { spawn } = require('child_process');
const path = require('path');
const fs = require('fs');

const BACKEND_DIR = path.resolve(__dirname, '../backend');
const FRONTEND_DIR = path.resolve(__dirname, '../frontend');
const BACKEND_PORT = 10000;
const FRONTEND_PORT = 5173;

let backendProcess = null;
let frontendProcess = null;

function log(prefix, data) {
    const lines = data.toString().split('\n');
    lines.forEach(line => {
        if (line.trim()) console.log(`${line}`);
    });
}

function startBackend() {
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
    if (backendProcess) {
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

// Start services, Kill existing first
try {
    require('child_process').execSync(`lsof -ti:${BACKEND_PORT} | xargs kill -9 2>/dev/null`);
    require('child_process').execSync(`lsof -ti:${FRONTEND_PORT} | xargs kill -9 2>/dev/null`);
} catch (e) { /* ignore */ }

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
