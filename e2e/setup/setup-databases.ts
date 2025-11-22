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
    // Hide lines starting with '>'
    if (line.startsWith('> ')) {
        return false;
    }
    // Hide Node.js deprecation warnings
    if (line.startsWith('(node:') && line.includes('DeprecationWarning')) {
        return false;
    }
    // Hide Lombok warnings related to sun.misc.Unsafe
    if (line.startsWith('WARNING:') && line.includes('lombok.permit.Permit') && line.includes('sun.misc.Unsafe')) {
        return false;
    }
    if (line.startsWith('WARNING:') && line.includes('sun.misc.Unsafe::objectFieldOffset')) {
        return false;
    }
    // You can add more filtering rules here if needed
    return true;
}

async function globalSetup(config: FullConfig) {
    debugLog('Starting E2E backend server...');
    
    // Create a write stream for the log file
    const logStream = fs.createWriteStream(LOG_FILE, { flags: 'w' });
    
    debugLog(`Backend logs will be written to: ${LOG_FILE}`);

    try {
        backendProcess = spawn('./gradlew', [':backend:bootRunE2E', '--quiet', '--args=--spring.profiles.active=e2e'], {
            stdio: 'pipe', // Capture streams so we can tee them
            shell: true,
            cwd: process.cwd()
        });

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
               // Find PID using lsof and kill it
               const { execSync } = require('child_process');
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
        } catch (e) {
            // Ignore URL parsing errors
        }
        
        logger.success('Backend server stopped.');
    }
}

export default globalSetup;
export { globalTeardown };