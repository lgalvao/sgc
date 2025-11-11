import { defineConfig } from 'vitest/config';
import baseConfig from './vitest.config';

export default defineConfig({
  ...baseConfig,
  test: {
    ...baseConfig.test,
    environment: 'node',
    include: ['src/**/*.integration.{test,spec}.{js,ts}'],
    setupFiles: ['./vitest.integration.setup.ts'],
  },
});