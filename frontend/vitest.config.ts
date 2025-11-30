import vue from "@vitejs/plugin-vue";
import tsconfigPaths from "vite-tsconfig-paths";
import {defineConfig} from "vitest/config";

export default defineConfig({
  plugins: [vue(), tsconfigPaths()],
  test: {
    globals: true,
      reporters: "dot",
      environment: "jsdom",
      include: ["src/**/*.{test,spec}.{js,ts}"],
      exclude: ["node_modules"],
      setupFiles: ["./vitest.setup.ts"],
    coverage: {
        provider: "v8",
        reporter: ["json", "text"],
        reportsDirectory: "./coverage",
    },
  },
});
