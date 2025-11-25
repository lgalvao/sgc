import { logger } from '../helpers/utils/logger';

async function globalTeardown() {
    logger.info('Global teardown completed. Playwright webServer should handle backend shutdown.');
}

export default globalTeardown;
export { globalTeardown };