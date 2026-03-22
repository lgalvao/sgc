import axios from "axios";
import type {Router} from "vue-router";
import {normalizeError, shouldNotifyGlobally} from '@/utils/apiError';
import {logger} from "@/utils";

let routerInstance: Router | null = null;

export function setRouter(router: Router) {
    routerInstance = router;
}

export const apiClient = axios.create({
    baseURL: import.meta.env?.VITE_API_BASE_URL || "/api",
    withCredentials: true,
    xsrfCookieName: 'XSRF-TOKEN',
    xsrfHeaderName: 'X-XSRF-TOKEN',
    headers: {
        "Content-type": "application/json",
    },
});

const handleResponseError = (error: any) => {
    const normalized = normalizeError(error);

    // Caso especial: 401 - redirecionar para login
    if (normalized.kind === 'unauthorized') {
        const currentPath = routerInstance?.currentRoute?.value?.path;
        if (currentPath !== '/login') {
            routerInstance?.push('/login').catch(e => logger.error("Erro ao redirecionar:", e));
        }
        return Promise.reject(error);
    }

    // Loga globalmente erros de rede e inesperados para diagnóstico
    if (shouldNotifyGlobally(normalized)) {
        logger.error("[axios] Erro global:", normalized.message);
    }

    // Sempre rejeitar para permitir tratamento local
    return Promise.reject(error);
};

apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("jwtToken");
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    },
);

apiClient.interceptors.response.use(
    (response) => response,
    handleResponseError,
);

export default apiClient;
