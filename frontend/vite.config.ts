import vue from "@vitejs/plugin-vue";
import {defineConfig} from "vite";
import tsconfigPaths from "vite-tsconfig-paths";

const backendBasePort = Number.parseInt(process.env.E2E_BACKEND_BASE_PORT || "10000", 10);
const workerCount = Number.parseInt(process.env.E2E_WORKER_COUNT || "1", 10);

function extrairWorker(req: { headers?: Record<string, string | string[] | undefined> }): number | null {
    const header = req.headers?.["x-e2e-worker"];
    const workerHeader = Array.isArray(header) ? header[0] : header;
    const cookieHeader = Array.isArray(req.headers?.cookie) ? req.headers?.cookie[0] : req.headers?.cookie;
    const cookieMatch = cookieHeader?.match(/(?:^|;\s*)e2e_worker=(\d+)/);
    const workerCookie = cookieMatch?.[1];
    const workerRaw = workerHeader ?? workerCookie;
    const workerIndex = Number.parseInt(String(workerRaw ?? ""), 10);
    if (!Number.isFinite(workerIndex) || workerIndex < 0) return null;
    return workerIndex;
}

function construirProxyPorWorker() {
    const proxy: Record<string, { target: string; changeOrigin: boolean; rewrite: (path: string) => string }> = {};
    for (let i = 0; i < workerCount; i++) {
        const alvo = `http://localhost:${backendBasePort + i}`;
        proxy[`/__w${i}/api`] = {
            target: alvo,
            changeOrigin: true,
            rewrite: (path) => path.replace(new RegExp(`^/__w${i}`), ""),
        };
        proxy[`/__w${i}/e2e`] = {
            target: alvo,
            changeOrigin: true,
            rewrite: (path) => path.replace(new RegExp(`^/__w${i}`), ""),
        };
    }
    // Fallback para execução local sem header/cookie de worker.
    proxy["/api"] = {
        target: `http://localhost:${backendBasePort}`,
        changeOrigin: true,
        rewrite: (path) => path,
    };
    proxy["/e2e"] = {
        target: `http://localhost:${backendBasePort}`,
        changeOrigin: true,
        rewrite: (path) => path,
    };
    return proxy;
}

export default defineConfig({
    plugins: [
        vue(),
        tsconfigPaths(),
        {
            name: "e2e-worker-rewrite",
            configureServer(server) {
                server.middlewares.use((req, _res, next) => {
                    const url = req.url || "";
                    if (url.startsWith("/api") || url.startsWith("/e2e")) {
                        const workerIndex = extrairWorker(req);
                        if (workerIndex !== null && workerIndex < workerCount) {
                            req.url = `/__w${workerIndex}${url}`;
                        }
                    }
                    next();
                });
            },
        },
    ],
    server: {
        watch: {
            usePolling: true,
        },
        proxy: construirProxyPorWorker(),
    },
    preview: {
        port: 4173,
        proxy: construirProxyPorWorker(),
    },
});
