import type {Ref} from 'vue';
import {ref} from 'vue';
import type {AxiosError} from 'axios';

// Define a type for the error structure we expect from the API
interface ApiError {
  message: string;
  // Add other fields if your API returns more structured errors
}

export function useApi<T>(
  apiCall: (...args: any[]) => Promise<T>
) {
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
        // Use the specific error message from the API response
        error.value = axiosError.response.data.message || 'Ocorreu um erro desconhecido.';
      } else {
        // Fallback for network errors or other issues
        error.value = 'Não foi possível conectar ao servidor.';
      }
      // Re-throw the error so that the component can optionally handle it further
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  // Helper to clear the error manually, e.g., for a dismiss button
  const clearError = () => {
    error.value = null;
  };

  return {
    data,
    isLoading,
    error,
    execute,
    clearError,
  };
}
