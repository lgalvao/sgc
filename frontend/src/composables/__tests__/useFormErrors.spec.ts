import { describe, it, expect } from 'vitest';
import { useFormErrors } from '../useFormErrors';

describe('useFormErrors', () => {
  it('initializes with default values', () => {
    const { errors } = useFormErrors(['field1', 'field2']);
    expect(errors.value).toEqual({ field1: '', field2: '' });
  });

  it('sets errors from normalized error', () => {
    const { errors, setFromNormalizedError } = useFormErrors(['field1']);
    const normalizedError = {
      message: 'Global error',
      subErrors: [{ field: 'field1', message: 'Error 1' }]
    };

    setFromNormalizedError(normalizedError);
    expect(errors.value.field1).toBe('Error 1');
  });

  it('clears errors', () => {
    const { errors, clearErrors } = useFormErrors(['field1']);
    errors.value.field1 = 'Error';
    clearErrors();
    expect(errors.value.field1).toBe('');
  });

  it('detects errors', () => {
      const { errors, hasErrors } = useFormErrors(['field1']);
      expect(hasErrors()).toBe(false);
      errors.value.field1 = 'Error';
      expect(hasErrors()).toBe(true);
  });
});
