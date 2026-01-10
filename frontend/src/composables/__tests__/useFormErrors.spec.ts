import {describe, expect, it} from 'vitest';
import {useFormErrors} from '../useFormErrors';

describe('useFormErrors', () => {
  it('initializes with default values', () => {
    const { errors } = useFormErrors(['field1', 'field2']);
    expect(errors.value).toEqual({ field1: '', field2: '' });
  });

  it('sets errors from normalized error', () => {
    const { errors, setFromNormalizedError } = useFormErrors(['field1']);
    const normalizedError = {
      kind: 'validation' as const,
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

  it('handles null normalized error', () => {
    const { errors, setFromNormalizedError } = useFormErrors(['field1']);
    setFromNormalizedError(null);
    expect(errors.value.field1).toBe('');
  });

  it('handles normalized error without subErrors', () => {
    const { errors, setFromNormalizedError } = useFormErrors(['field1']);
    const normalizedError = {
      kind: 'validation' as const,
      message: 'Global error'
    };
    setFromNormalizedError(normalizedError);
    expect(errors.value.field1).toBe('');
  });

  it('ignores subErrors with missing fields', () => {
    const { errors, setFromNormalizedError } = useFormErrors(['field1']);
    const normalizedError = {
      kind: 'validation' as const,
      message: 'Global error',
      subErrors: [{ message: 'Error 1' }] // Missing field
    };
    setFromNormalizedError(normalizedError);
    expect(errors.value.field1).toBe('');
  });

  it('ignores subErrors for fields not in tracking list', () => {
    const { errors, setFromNormalizedError } = useFormErrors(['field1']);
    const normalizedError = {
      kind: 'validation' as const,
      message: 'Global error',
      subErrors: [{ field: 'otherField', message: 'Error 1' }]
    };
    setFromNormalizedError(normalizedError);
    expect(errors.value.field1).toBe('');
    expect(errors.value).not.toHaveProperty('otherField');
  });

  it('uses default message if subError message is missing', () => {
    const { errors, setFromNormalizedError } = useFormErrors(['field1']);
    const normalizedError = {
      kind: 'validation' as const,
      message: 'Global error',
      subErrors: [{ field: 'field1' }] // Missing message
    };
    setFromNormalizedError(normalizedError);
    expect(errors.value.field1).toBe('Campo inválido');
  });
});
