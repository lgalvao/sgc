import {test as base} from '@playwright/test';
// Note: import uses .js extension because tests run with Node's ESM resolution (nodenext)
import logger from '../../frontend/src/utils/logger.js';

// Extend base test to automatically log console messages
export const test = base.extend({
    page: async ({page}, use) => {
        // Listen to all console events from the browser and forward to project logger
        page.on('console', msg => {
            // Coerce to string to avoid strict Playwright ConsoleMessageType union mismatches
            const type = String(msg.type());
            const text = msg.text();

            // Only forward common types to reduce noise
            if (!['log', 'error', 'warning', 'warn', 'info'].includes(type)) {
                return;
            }

            if (type === 'error') {
                logger.error(`${text}`);
            } else if (type === 'warning' || type === 'warn') {
                logger.warn(`${text}`);
            } else {
                logger.info(`${text}`);
            }
        });

        // Listen to page errors
        page.on('pageerror', error => {
            logger.error(`[Browser PAGE ERROR] ${error.message}`);
        });

        await use(page);
    },
});

export {expect} from '@playwright/test';
