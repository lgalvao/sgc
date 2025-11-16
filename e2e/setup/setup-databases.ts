import { spawn, ChildProcessWithoutNullStreams } from 'child_process';
import axios from 'axios';
import { FullConfig } from '@playwright/test';
import logger from '../../frontend/src/utils/logger';

const debugLog = (...args: any[]) => { if (process.env.E2E_DEBUG === '1') logger.debug(...args); };

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:10000';
const BACKEND_HEALTH_URL = `${BACKEND_URL}/actuator/health`;

let backendProcess: ChildProcessWithoutNullStreams | undefined;

async function globalSetup(config: FullConfig) {
    debugLog('Starting E2E backend server...');

    backendProcess = spawn('./gradlew', [':backend:bootRun', "--args='--spring.profiles.active=e2e'"], {
        stdio: 'inherit',
        shell: true,
        cwd: process.cwd()
    });

    // Wait for the backend to be healthy
    let retries = 0;
    const maxRetries = 60; // 60 retries * 1 second = 60 seconds timeout
    while (retries < maxRetries) {
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
        if (backendProcess) {
            backendProcess.kill();
        }
        process.exit(1);
    }

    debugLog('✓ Infraestrutura de testes E2E configurada');
    debugLog('  - Backend URL:', BACKEND_URL);
    debugLog('  - Isolamento de banco: ATIVO (por teste)');
}

async function globalTeardown() {
    debugLog('Stopping E2E backend server gracefully...');
    try {
        await axios.post(`${BACKEND_URL}/actuator/shutdown`);
        logger.info('Backend shutdown request sent. Waiting for process to exit...');
        // Give some time for the process to shut down
        await new Promise(resolve => setTimeout(resolve, 5000));
    } catch (error: any) {
        logger.error(`Error sending shutdown request to backend: ${error.message}`);
        if (backendProcess) {
            logger.warn('Force killing backend process...');
            backendProcess.kill();
        }
    } finally {
        if (backendProcess && !backendProcess.killed) {
            logger.warn('Backend process still running, force killing...');
            backendProcess.kill();
        }
        logger.success('Backend server stopped.');
    }
}

export default globalSetup;
export { globalTeardown };