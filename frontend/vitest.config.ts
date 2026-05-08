import vue from "@vitejs/plugin-vue";
import tsconfigPaths from "vite-tsconfig-paths";
import type { Plugin } from "vite";
import { defineConfig } from "vitest/config";
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
        exclude: ["node_modules", "dist", "**/*.d.ts", "src/main.ts", "**/*.config.*", "**/*.stories.ts"],
        setupFiles: ["./vitest.setup.ts", "./src/test/a11y-setup.ts"]
      }
    }]
  }
});