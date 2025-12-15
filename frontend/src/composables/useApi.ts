import type {Ref} from "vue";
import {ref} from "vue";
import {type NormalizedError, normalizeError} from "@/utils/apiError";

export function useApi<T>(apiCall: (...args: any[]) => Promise<T>) {
  const data: Ref<T | null> = ref(null);
  const isLoading = ref(false);
  const error: Ref<string | null> = ref(null);
  const normalizedError: Ref<NormalizedError | null> = ref(null);

  const execute = async (...args: any[]): Promise<void> => {
    isLoading.value = true;
    error.value = null;
    normalizedError.value = null;
    data.value = null;

    try {
      data.value = await apiCall(...args);
    } catch (err) {
      normalizedError.value = normalizeError(err);
      error.value = normalizedError.value.message; // Retrocompatibilidade
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const clearError = () => {
    error.value = null;
    normalizedError.value = null;
  };

  return { data, isLoading, error, normalizedError, execute, clearError };
}
