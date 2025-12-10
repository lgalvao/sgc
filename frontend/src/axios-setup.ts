import axios from "axios";
import router from "./router";
import {useFeedbackStore} from "@/stores/feedback";

const apiClient = axios.create({
    baseURL: "http://localhost:10000/api",
    headers: {
        "Content-type": "application/json",
    },
});

const handleResponseError = (error: any) => {
    const feedbackStore = useFeedbackStore();
    try {
        if (error && typeof error === "object" && "response" in error) {
            const {status, data} = (error as any).response;
            // Do not show global popups for these statuses, they will be handled locally
            const isHandledInline = [400, 404, 409, 422].includes(status);

            if (isHandledInline) {
                // Just forward the error to the local handler without showing a global toast
                return Promise.reject(error); // Correctly return the rejected promise
                // Removed `return;` as it's redundant after `return Promise.reject`
            }

            if (status === 401) {
                feedbackStore.show(
                    "Não Autorizado",
                    "Sua sessão expirou ou você não está autenticado. Faça login novamente.",
                    "danger"
                );
                router.push("/login");
            } else if (data && data.message) {
                // For other errors (like 500), show a generic popup
                feedbackStore.show("Erro Inesperado", data.message, "danger");
            } else {
                feedbackStore.show(
                    "Erro Inesperado",
                    "Ocorreu um erro. Tente novamente mais tarde.",
                    "danger"
                );
            }
        } else if (error && typeof error === "object" && "request" in error) {
            feedbackStore.show(
                "Erro de Rede",
                "Não foi possível conectar ao servidor. Verifique sua conexão com a internet.",
                "danger"
            );
        } else if (error && typeof error === "object" && "message" in error) {
            feedbackStore.show("Erro", (error as any).message, "danger");
        }
    } catch (storeError) {
        console.error("Erro ao exibir notificação:", storeError);
    }
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
