const { spawn, exec } = require('child_process');
const path = require('path');
const fs = require('fs');
const os = require('os');

// Helper to find Chrome from Playwright
function findChrome() {
    const playwrightCache = path.join(os.homedir(), '.cache/ms-playwright');
    if (!fs.existsSync(playwrightCache)) return null;

    const dirs = fs.readdirSync(playwrightCache);
    // Find the latest chromium folder
    const chromiumDir = dirs
        .filter(d => d.startsWith('chromium-'))
        .sort().reverse()[0];

    if (!chromiumDir) return null;

    const baseDir = path.join(playwrightCache, chromiumDir);
    let chromePath = null;

    if (process.platform === 'linux') {
        chromePath = path.join(baseDir, 'chrome-linux', 'chrome');
    } else if (process.platform === 'darwin') {
        chromePath = path.join(baseDir, 'chrome-mac', 'Chromium.app', 'Contents', 'MacOS', 'Chromium');
    } else if (process.platform === 'win32') {
        chromePath = path.join(baseDir, 'chrome-win', 'chrome.exe');
    }

    if (chromePath && fs.existsSync(chromePath)) return chromePath;

    return null;
}

// Try to find chrome, but don't fail immediately if not found.
// Lighthouse's chrome-launcher might be able to find a system installation.
const chromePath = process.env.CHROME_PATH || findChrome();

if (chromePath) {
    console.log(`Using Chrome at: ${chromePath}`);
    // Lighthouse uses CHROME_PATH env var
    process.env.CHROME_PATH = chromePath;
} else {
    console.warn('Warning: Could not find Playwright Chrome. Relying on system Chrome or Lighthouse defaults.');
}

// Start the application
const lifecycleScript = path.resolve(__dirname, '../e2e/lifecycle.js');
console.log('Starting application...');
const appProcess = spawn('node', [lifecycleScript], { stdio: ['ignore', 'pipe', 'pipe'] });

let appReady = false;

appProcess.stdout.on('data', (data) => {
    const output = data.toString();
    // Log app output to see progress if needed
    // console.log(`[APP] ${output.trim()}`);
    if (output.includes('>>> Frontend e Backend no ar!')) {
        console.log('[APP] Application is ready!');
        if (!appReady) {
            appReady = true;
            runLighthouse();
        }
    }
});

appProcess.stderr.on('data', (data) => {
    // Only log errors
    console.error(`[APP ERROR] ${data.toString().trim()}`);
});

function runLighthouse() {
    console.log('Starting Lighthouse accessibility check...');
    const lighthouseBin = path.resolve(__dirname, '../node_modules/.bin/lighthouse');
    const reportPath = path.resolve(__dirname, '../accessibility-report.html');

    // Lighthouse command arguments
    const args = [
        'http://localhost:5173',
        '--chrome-flags="--headless --no-sandbox --disable-gpu"',
        '--output=html',
        `--output-path=${reportPath}`,
        '--only-categories=accessibility'
    ];

    const cmd = `"${lighthouseBin}" ${args.join(' ')}`;
    console.log(`Executing: ${cmd}`);

    exec(cmd, { env: process.env }, (error, stdout, stderr) => {
        if (stdout) console.log(stdout);
        // Lighthouse logs to stderr often for status
        if (stderr) console.error(stderr);

        if (error) {
            console.error('Lighthouse failed:', error);
            cleanup(1);
        } else {
            console.log(`Lighthouse check complete. Report saved to ${reportPath}`);
            cleanup(0);
        }
    });
}

function cleanup(code) {
    console.log('Stopping application...');
    if (!appProcess.killed) {
        // Send SIGINT to trigger lifecycle.js cleanup
        appProcess.kill('SIGINT');

        // Force kill if it doesn't exit in 5 seconds
        setTimeout(() => {
            if (!appProcess.killed) {
                appProcess.kill('SIGKILL');
            }
            process.exit(code);
        }, 5000);

        // Wait for exit
        appProcess.on('exit', () => {
            process.exit(code);
        });
    } else {
        process.exit(code);
    }
}

// Handle signals
process.on('SIGINT', () => cleanup(0));
process.on('SIGTERM', () => cleanup(0));
