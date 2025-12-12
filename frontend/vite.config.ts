import vue from "@vitejs/plugin-vue";
import {defineConfig} from "vite";
import tsconfigPaths from "vite-tsconfig-paths";

export default defineConfig({
    plugins: [vue(), tsconfigPaths()],
    server: {
        proxy: {
            "/api": {
                target: "http://localhost:10000",
                changeOrigin: true,
            },
        },
    },
});
