import type {AxiosError} from "axios";
import type {Ref} from "vue";
import {ref} from "vue";

interface ApiError {
    message: string;
}

export function useApi<T>(apiCall: (...args: any[]) => Promise<T>) {
    const data: Ref<T | null> = ref(null);
    const isLoading = ref(false);
    const error: Ref<string | null> = ref(null);

    const execute = async (...args: any[]): Promise<void> => {
        isLoading.value = true;
        error.value = null;
        data.value = null;

        try {
            data.value = await apiCall(...args);
        } catch (err) {
            const axiosError = err as AxiosError<ApiError>;
            if (axiosError.response && axiosError.response.data) {
                error.value = axiosError.response.data.message || "Erro desconhecido.";
            } else {
                error.value = "Não foi possível conectar ao servidor.";
            }
            throw err;
        } finally {
            isLoading.value = false;
        }
    };

    const clearError = () => error.value = null;
    return {data, isLoading, error, execute, clearError};
}
