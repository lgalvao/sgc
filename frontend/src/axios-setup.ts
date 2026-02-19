import axios from "axios";
import type {Router} from "vue-router";
import {useFeedbackStore} from "@/stores/feedback";
import {normalizeError, notifyError, shouldNotifyGlobally} from '@/utils/apiError';
import {logger} from "@/utils";

let routerInstance: Router | null = null;

export function setRouter(router: Router) {
    routerInstance = router;
}

export const apiClient = axios.create({
    baseURL: import.meta.env?.VITE_API_BASE_URL || "http://localhost:10000/api",
    withCredentials: true,
    xsrfCookieName: 'XSRF-TOKEN',
    xsrfHeaderName: 'X-XSRF-TOKEN',
    headers: {
        "Content-type": "application/json",
    },
});

const handleResponseError = (error: any) => {
    const feedbackStore = useFeedbackStore();
    const normalized = normalizeError(error);

    // Caso especial: 401 - redirecionar para login
    if (normalized.kind === 'unauthorized') {
        const currentPath = routerInstance?.currentRoute?.value?.path;
        if (currentPath !== '/login') {
            feedbackStore.show(
                'Não Autorizado',
                'Sua sessão expirou ou você não está autenticado. Faça login novamente.',
                'danger'
            );
            routerInstance?.push('/login').catch(e => logger.error("Erro ao redirecionar:", e));
        }
        return Promise.reject(error);
    }

    // Decidir se mostra toast global baseado no kind
    if (shouldNotifyGlobally(normalized)) {
        try {
            notifyError(normalized);
        } catch (storeError) {
            logger.error("Erro ao exibir notificação:", storeError);
        }
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
