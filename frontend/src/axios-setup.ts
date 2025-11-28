import axios from "axios";
import router from "./router";
import { ToastService } from "@/services/toastService";

const apiClient = axios.create({
    baseURL: "http://localhost:10000/api",
  headers: {
      "Content-type": "application/json",
  },
});

const handleResponseError = (error: any) => {
  try {
      if (error && typeof error === "object" && "response" in error) {
      const { status, data } = (error as any).response;
      // Do not show global popups for these statuses, they will be handled locally
      const isHandledInline = [400, 404, 409, 422].includes(status);

      if (isHandledInline) {
        // Just forward the error to the local handler
        return Promise.reject(error);
      }

      if (status === 401) {
          ToastService.erro(
              "Não Autorizado",
              "Sua sessão expirou ou você não está autenticado. Faça login novamente.",
          );
          router.push("/login");
      } else if (data && data.message) {
        // For other errors (like 500), show a generic popup
          ToastService.erro("Erro Inesperado", data.message);
      } else {
          ToastService.erro(
              "Erro Inesperado",
              "Ocorreu um erro. Tente novamente mais tarde.",
          );
      }
      } else if (error && typeof error === "object" && "request" in error) {
          ToastService.erro(
              "Erro de Rede",
              "Não foi possível conectar ao servidor. Verifique sua conexão com a internet.",
          );
      } else if (error && typeof error === "object" && "message" in error) {
          ToastService.erro("Erro", (error as any).message);
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
