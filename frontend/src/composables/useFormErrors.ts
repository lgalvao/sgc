import {ref} from 'vue';
import type {NormalizedError} from '@/utils/apiError';

export function useFormErrors(initialFields: string[] = []) {
  const errors = ref<Record<string, string>>(
    Object.fromEntries(initialFields.map(f => [f, '']))
  );

  function clearErrors() {
    Object.keys(errors.value).forEach(key => {
      errors.value[key] = '';
    });
  }

  function setFromNormalizedError(normalizedError: NormalizedError | null) {
    clearErrors();

    if (!normalizedError?.subErrors) return;

    normalizedError.subErrors.forEach(subError => {
      const field = subError.field;
      if (field && field in errors.value) {
        errors.value[field] = subError.message || 'Campo inválido';
      }
    });
  }

  function hasErrors(): boolean {
    return Object.values(errors.value).some(e => e !== '');
  }

  return {
    errors,
    clearErrors,
    setFromNormalizedError,
    hasErrors
  };
}
