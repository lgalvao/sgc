import vue from "@vitejs/plugin-vue";
import tsconfigPaths from "vite-tsconfig-paths";
import type { Plugin } from "vite";
import { defineConfig } from "vitest/config";
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { storybookTest } from '@storybook/addon-vitest/vitest-plugin';
import { playwright } from '@vitest/browser-playwright';
const dirname = typeof __dirname !== 'undefined' ? __dirname : path.dirname(fileURLToPath(import.meta.url));

// More info at: https://storybook.js.org/docs/next/writing-tests/integrations/vitest-addon
export default defineConfig({
  plugins: [vue() as Plugin, tsconfigPaths() as Plugin],
  test: {
    reporters: "dot",
    onConsoleLog(log: string) {
      if (log.includes('decodeEntities')) return false;
    },
    coverage: {
      provider: "v8",
      reporter: ["json", "text", "html", "lcov"],
      reportsDirectory: "./coverage",
      include: ["src/**/*.{js,ts,vue}"],
      exclude: ["node_modules", "dist", "**/*.d.ts", "src/main.ts", "**/*.config.*", "src/constants/**", "src/types/**", "src/test-utils/**", "**/*.stories.ts"],
      thresholds: {
        lines: 90,
        functions: 90,
        statements: 90,
        branches: 85
      }
    },
    projects: [{
      extends: true,
      test: {
        globals: true,
        environment: "jsdom",
        testTimeout: 15000,
        include: ["src/**/*.{test,spec}.{js,ts}"],
        exclude: ["node_modules", "dist", "**/*.d.ts", "src/main.ts", "**/*.config.*"],
        setupFiles: ["./vitest.setup.ts", "./src/test/a11y-setup.ts"]
      }
    }, {
      extends: true,
      plugins: [
      // The plugin will run tests for the stories defined in your Storybook config
      // See options at: https://storybook.js.org/docs/next/writing-tests/integrations/vitest-addon#storybooktest
      storybookTest({
        configDir: path.join(dirname, '.storybook')
      })],
      test: {
        name: 'storybook',
        browser: {
          enabled: true,
          headless: true,
          provider: playwright({}),
          instances: [{
            browser: 'chromium'
          }]
        }
      }
    }]
  }
});