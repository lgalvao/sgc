import vue from "@vitejs/plugin-vue";
import tsconfigPaths from "vite-tsconfig-paths";
import {defineConfig} from "vitest/config";

export default defineConfig({
    plugins: [vue() as any, tsconfigPaths() as any],
    test: {
        globals: true,
        reporters: "dot",
        environment: "jsdom",
        testTimeout: 15000,
        include: ["src/**/*.{test,spec}.{js,ts}"],
        exclude: ["node_modules", "dist", "**/*.d.ts", "src/main.ts", "**/*.config.*", "src/visual-capture/capture.test.ts", "**/*.pact.spec.ts"],
        setupFiles: ["./vitest.setup.ts"],
        coverage: {
            provider: "v8",
            reporter: ["json", "text", "html", "lcov"],
            reportsDirectory: "./coverage",
            include: ["src/**/*.{js,ts,vue}"],
            exclude: ["node_modules", "dist", "**/*.d.ts", "src/main.ts", "**/*.config.*", "src/constants/**", "src/types/**", "src/test-utils/**"],
            thresholds: {
                statements: 90,
                branches: 80,
                functions: 89,
                lines: 90,
            },
        },
    },
});
