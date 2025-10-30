import {defineConfig} from 'vite'
import tsconfigPaths from 'vite-tsconfig-paths'
import vue from "@vitejs/plugin-vue";

export default defineConfig({
    plugins: [vue(), tsconfigPaths()],
    test: {
        globals: true,
        environment: 'jsdom',
    },
})
