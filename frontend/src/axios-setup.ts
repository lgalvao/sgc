import axios from "axios";
import router from "./router";
import {useFeedbackStore} from "@/stores/feedback";
import {normalizeError, notifyError, shouldNotifyGlobally} from '@/utils/apiError';
import {logger} from "@/utils";

export const apiClient = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:10000/api",
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
        if (router.currentRoute?.value?.path !== '/login') {
            feedbackStore.show(
                'Não Autorizado',
                'Sua sessão expirou ou você não está autenticado. Faça login novamente.',
                'danger'
            );
            router.push('/login').catch(e => logger.error("Erro ao redirecionar:", e));
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
