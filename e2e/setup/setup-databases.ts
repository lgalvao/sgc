import { spawn, ChildProcess } from 'child_process';
import axios from 'axios';
import { FullConfig } from '@playwright/test';
import { debug as debugLog, logger } from '../helpers/utils/logger';
import fs from 'fs';
import path from 'path';

// Use IPv4 explicitly to avoid node/java resolution mismatches (localhost vs ::1)
const BACKEND_URL = process.env.BACKEND_URL || 'http://127.0.0.1:10000';
const BACKEND_HEALTH_URL = `${BACKEND_URL}/actuator/health`;
const LOG_FILE = path.join(process.cwd(), 'e2e-backend.log');

let backendProcess: ChildProcess | undefined;

// Function to filter out lines we consider noise
function shouldDisplayLine(line: string): boolean {
    const currentTrimmedLine = line.trim();

    // Hide lines starting with '>'
    if (currentTrimmedLine.startsWith('> ')) {
        return false;
    }
    // Hide Node.js deprecation warnings
    if (currentTrimmedLine.startsWith('(node:') && currentTrimmedLine.includes('DeprecationWarning')) {
        return false;
    }
    // Hide Lombok warnings related to sun.misc.Unsafe
    if (currentTrimmedLine.startsWith('WARNING:') && currentTrimmedLine.includes('lombok.permit.Permit') && currentTrimmedLine.includes('sun.misc.Unsafe')) {
        return false;
    }
    if (currentTrimmedLine.startsWith('WARNING:') && currentTrimmedLine.includes('sun.misc.Unsafe::objectFieldOffset')) {
        return false;
    }

    // Filter Playwright 'list' reporter redundant start lines
    // A test pass/fail line starts with '✓' or '×'
    // A test start line (redundant) looks like "     1 … Test name" (has leading spaces, number, ellipsis, and doesn't start with ✓ or ×)
    // The summary line looks like "  5 passed (16.8s)" or "  1 failed, 4 passed (16.8s)"

    // If the line matches the pattern of a test progress line (e.g., "     1 …" or "  ✓  1 …")
    // but it does *not* start with '✓' or '×', then it's a redundant test start line.
    if (currentTrimmedLine.match(/^\d+ \…/) && !currentTrimmedLine.startsWith('✓') && !currentTrimmedLine.startsWith('×')) {
        return false; // Filter out the redundant test start line
    }

    // Keep the final summary line, which doesn't start with '✓' or '×' but contains "passed" or "failed"
    if (currentTrimmedLine.match(/^\d+ (passed|failed)/)) {
        return true;
    }

    return true; // Keep all other lines (including actual test results starting with ✓ or ×, and other logs)
}

async function globalSetup(config: FullConfig) {
    debugLog('Starting E2E backend server...');
    
    // Create a write stream for the log file
    const logStream = fs.createWriteStream(LOG_FILE, { flags: 'w' });
    
    debugLog(`Backend logs will be written to: ${LOG_FILE}`);

    try {
        if (process.platform === 'win32') {
            backendProcess = spawn('cmd.exe', ['/c', 'gradlew :backend:bootRunE2E --quiet --args=--spring.profiles.active=e2e'], {
                stdio: 'pipe', // Capture streams so we can tee them
                shell: false, // shell: false since we're explicitly calling cmd.exe
                cwd: process.cwd()
            });
        } else {
            backendProcess = spawn('./gradlew', [':backend:bootRunE2E', '--quiet', '--args=--spring.profiles.active=e2e'], {
                stdio: 'pipe', // Capture streams so we can tee them
                shell: true,
                cwd: process.cwd()
            });
        }

        if (backendProcess.stdout) {
            backendProcess.stdout.on('data', (data) => {
                const lines = data.toString().split('\n');
                lines.forEach(line => {
                    const trimmedLine = line.trim();
                    if (trimmedLine.length > 0 && shouldDisplayLine(trimmedLine)) {
                        process.stdout.write(trimmedLine + '\n'); // Write to console
                        logStream.write(trimmedLine + '\n');      // Write to file
                    }
                });
            });
        }

        if (backendProcess.stderr) {
            backendProcess.stderr.on('data', (data) => {
                const lines = data.toString().split('\n');
                lines.forEach(line => {
                    const trimmedLine = line.trim();
                    if (trimmedLine.length > 0 && shouldDisplayLine(trimmedLine)) {
                        process.stderr.write(trimmedLine + '\n'); // Write to console
                        logStream.write(trimmedLine + '\n');      // Write to file
                    }
                });
            });
        }

        let backendExited = false;
        let exitCode: number | null = null;

        backendProcess.on('exit', (code) => {
            backendExited = true;
            exitCode = code;
            if (code !== 0 && code !== 1 && code !== null) {
                logger.error(`Backend process exited prematurely with code ${code}`);
            }
        });

        // Wait for the backend to be healthy
        let retries = 0;
        const maxRetries = 60; // 60 retries * 1 second = 60 seconds timeout
        while (retries < maxRetries) {
            if (backendExited) {
                logger.error(`Backend failed to start (exited with code ${exitCode}). Check logs above.`);
                process.exit(1);
            }

            try {
                const response = await axios.get(BACKEND_HEALTH_URL);
                if (response.status === 200 && response.data.status === 'UP') {
                    logger.success('Backend server is up and healthy.');
                    break;
                }
            } catch (error) {
                // Ignore connection errors while waiting for server to start
            }
            retries++;
            await new Promise(resolve => setTimeout(resolve, 1000));
        }

        if (retries === maxRetries) {
            logger.error('Backend server did not start within the expected time.');
            console.log('\n--- BACKEND LOGS (TAIL) ---');
            // Read the last 50 lines of the log file
            try {
                const logs = fs.readFileSync(LOG_FILE, 'utf8');
                const lines = logs.split('\n');
                console.log(lines.slice(-50).join('\n'));
            } catch (e) {
                console.log('Could not read log file.');
            }
            console.log('---------------------------\n');
            
            if (backendProcess) {
                backendProcess.kill();
            }
            process.exit(1);
        }

        debugLog('✓ Infraestrutura de testes E2E configurada');
        debugLog('  - Backend URL:', BACKEND_URL);
        debugLog('  - Modo de execução: SEQUENCIAL (Banco compartilhado)');

    } catch (e) {
        logger.error('Failed to spawn backend process', e);
        process.exit(1);
    }
}

async function globalTeardown() {
    debugLog('Stopping E2E backend server gracefully...');
    try {
        await axios.post(`${BACKEND_URL}/actuator/shutdown`, {}, {
            headers: { 'Content-Type': 'application/json' }
        });
        logger.info('Backend shutdown request sent. Waiting for process to exit...');
        // Give some time for the process to shut down
        await new Promise(resolve => setTimeout(resolve, 5000));
    } catch (error: any) {
        // It's possible the server is already down or unreachable, which is fine.
        debugLog(`Shutdown request info: ${error.message}`);
    } finally {
        if (backendProcess) {
            // Try to kill the gradle wrapper process
            try {
                backendProcess.kill();
            } catch (e) {
                // ignore
            }
        }

        // Ensure the process on the port is killed
        try {
            const port = new URL(BACKEND_URL).port;
            if (port) {
                const { execSync } = require('child_process');
                if (process.platform === 'win32') {
                    try {
                        const output = execSync(`netstat -ano | findstr :${port}`).toString();
                        const lines = output.split('\n');
                        for (const line of lines) {
                            const parts = line.trim().split(/\s+/);
                            // Find the PID in the last column
                            // Check for 'LISTENING' state and IPv4/IPv6 address.
                            // Example for IPv4: TCP    127.0.0.1:10000        0.0.0.0:0              LISTENING       1234
                            // Example for IPv6: TCP    [::]:10000             [::]:0                 LISTENING       5678
                            if (parts.length >= 5 && parts[4] === 'LISTENING' && (parts[1].includes(`:${port}`) || parts[2].includes(`:${port}`))) {
                                const pid = parts[parts.length - 1];
                                if (pid) {
                                    logger.warn(`Force killing backend process on port ${port} (PID: ${pid}) using taskkill...`);
                                    execSync(`taskkill /PID ${pid} /F`);
                                    // Do not break here. netstat might return multiple lines for a single port, e.g., both IPv4 and IPv6.
                                    // We want to make sure all processes related to this port are killed.
                                }
                            }
                        }
                    } catch (e: any) {
                        // Ignore if no process found or other error
                        debugLog(`Error while trying to kill process on port ${port} (Windows): ${e.message}`);
                    }
                } else {
                    // Original lsof logic for Unix-like systems
                    try {
                        // Check if lsof is available
                        try {
                            execSync('lsof -v', { stdio: 'ignore' });
                        } catch (e) {
                            // lsof not available
                        }

                        // Use lsof to check if port is still in use
                        try {
                            const pid = execSync(`lsof -t -i:${port}`).toString().trim();
                            if (pid) {
                                logger.warn(`Force killing backend process on port ${port} (PID: ${pid})...`);
                                process.kill(parseInt(pid), 'SIGKILL');
                            }
                        } catch (e) {
                            // No process on port, all good
                        }
                    } catch (e) {
                        // Ignore
                    }
                }
            }
        } catch (e) {
            // Ignore URL parsing errors
        }
        
        logger.success('Backend server stopped.');
    }
}

export default globalSetup;
export { globalTeardown };