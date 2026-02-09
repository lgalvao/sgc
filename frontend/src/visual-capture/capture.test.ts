import { describe, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { page } from '@vitest/browser/context';
import App from '../App.vue';
import { createPinia, setActivePinia } from 'pinia';
import router from '../router';

describe('Visual Capture', () => {
  it('captures the main app screen', async () => {
    setActivePinia(createPinia());
    
    // We mount the component
    mount(App, {
      global: {
        plugins: [router]
      }
    });

    // Wait for some rendering
    await new Promise(resolve => setTimeout(resolve, 1000));

    // Take screenshot
    await page.screenshot({ path: './screenshots-vitest/app-main.png' });
  });
});
