import {test as base} from '@playwright/test';

// Extend base test to automatically log console messages
export const test = base.extend({
    page: async ({page}, use) => {
        // Listen to all console events
        page.on('console', msg => {
            const type = msg.type();
            const text = msg.text();
            
            // Only log console.log, console.error, console.warn
            if (['log', 'error', 'warn'].includes(type)) {
                console.log(`[Browser ${type.toUpperCase()}] ${text}`);
            }
        });

        // Listen to page errors
        page.on('pageerror', error => {
            console.error(`[Browser ERROR] ${error.message}`);
        });

        await use(page);
    },
});

export {expect} from '@playwright/test';
