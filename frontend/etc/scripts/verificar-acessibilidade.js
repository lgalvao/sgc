/* eslint-disable */
import {spawn} from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import * as chromeLauncher from 'chrome-launcher';

async function run() {
    console.log('Starting e2e/lifecycle.js...');
    const lifecycle = spawn('node', ['e2e/lifecycle.js'], {
        stdio: ['ignore', 'pipe', 'pipe'],
        cwd: process.cwd(),
    });

    let ready = false;

    lifecycle.stdout.on('data', (data) => {
        const output = data.toString().trim();
        console.log(`[Lifecycle]: ${output}`);
        if (output.includes('>>> Frontend e Backend no ar!') && !ready) {
            ready = true;
            onReady();
        }
    });

    lifecycle.stderr.on('data', (data) => {
        console.error(`[Lifecycle Err]: ${data.toString().trim()}`);
    });

    const cleanup = () => {
        if (!lifecycle.killed) {
            console.log('Stopping lifecycle...');
            lifecycle.kill('SIGINT');
            setTimeout(() => {
                try {
                    if (process.kill(lifecycle.pid, 0)) {
                        lifecycle.kill('SIGKILL');
                    }
                } catch (e) {
                    console.log('Informação: Processo de limpeza já encerrado ou sem permissão:', e.message);
                }
            }, 2000);
        }
    };

    process.on('SIGINT', () => {
        cleanup();
        process.exit();
    });

    process.on('SIGTERM', () => {
        cleanup();
        process.exit();
    });

    async function onReady() {
        console.log('Application ready. Starting accessibility check...');

        let chromePath;
        try {
            const {chromium} = await import('@playwright/test');
            const candidatePath = chromium.executablePath();

            if (fs.existsSync(candidatePath)) {
                chromePath = candidatePath;
                console.log(`Found valid Playwright Chrome at: ${chromePath}`);
            } else {
                console.warn(`Playwright reported Chrome at ${candidatePath}, but it does not exist.`);
                // Fallback search in cache directory

                const homeDir = process.env.HOME || process.env.USERPROFILE;
                const playwrightCache = path.join(homeDir, '.cache', 'ms-playwright');

                if (fs.existsSync(playwrightCache)) {
                    console.log(`Searching in ${playwrightCache}...`);
                    const entries = fs.readdirSync(playwrightCache);
                    // Look for chromium-* folders
                    const chromiumDirs = entries.filter(e => e.startsWith('chromium-')).sort().reverse(); // Use latest version found

                    for (const dir of chromiumDirs) {
                        // Common paths: chrome-linux/chrome or chrome-linux64/chrome or chrome-win/chrome.exe etc.
                        // Based on ls output: chromium-1187/chrome-linux/chrome
                        const potentialPath = path.join(playwrightCache, dir, 'chrome-linux', 'chrome');
                        if (fs.existsSync(potentialPath)) {
                            chromePath = potentialPath;
                            console.log(`Found fallback Chrome at: ${chromePath}`);
                            break;
                        }
                    }
                }
            }

        } catch (e) {
            console.warn('Error locating Playwright Chrome:', e.message);
        }

        let chrome;
        try {
            chrome = await chromeLauncher.launch({
                chromeFlags: ['--headless', '--disable-gpu', '--no-sandbox'],
                chromePath: chromePath
            });
        } catch (err) {
            console.error('Failed to launch Chrome:', err);
            cleanup();
            process.exit(1);
        }

        const PORT = chrome.port;
        const TARGET_URL = 'http://localhost:5173';

        console.log(`Running Lighthouse on ${TARGET_URL} port ${PORT}...`);

        try {
            const lighthouseModule = await import('lighthouse');
            const lighthouse = lighthouseModule.default;

            const options = {
                logLevel: 'info',
                output: 'html',
                onlyCategories: ['accessibility'],
                port: PORT,
            };

            const runnerResult = await lighthouse(TARGET_URL, options);

            const reportHtml = runnerResult.report;
            fs.writeFileSync('accessibility-report.html', reportHtml);

            console.log('Report is done for', runnerResult.lhr.finalUrl);
            console.log('Accessibility score was', runnerResult.lhr.categories.accessibility.score * 100);
        } catch (err) {
            console.error('Lighthouse execution failed:', err);
        } finally {
            chrome.kill();
            cleanup();
            process.exit(0);
        }
    }
}

run().catch(err => {
    console.error('Erro inesperado no accessibility-check:', err);
    process.exit(1);
});
