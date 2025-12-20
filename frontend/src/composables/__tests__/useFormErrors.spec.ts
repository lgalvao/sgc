import { describe, it, expect } from 'vitest';
import { useFormErrors } from '../useFormErrors';
import type { NormalizedError } from '@/utils/apiError';

describe('useFormErrors', () => {
  it('should initialize with empty errors for given fields', () => {
    const { errors } = useFormErrors(['field1', 'field2']);

    expect(errors.value).toEqual({
      field1: '',
      field2: ''
    });
  });

  it('should set errors from NormalizedError', () => {
    const { errors, setFromNormalizedError } = useFormErrors(['name', 'email']);

    const mockError: NormalizedError = {
      kind: 'validation',
      message: 'Validation failed',
      subErrors: [
        { field: 'name', message: 'Name is required' },
        { field: 'email', message: 'Invalid email' }
      ]
    };

    setFromNormalizedError(mockError);

    expect(errors.value.name).toBe('Name is required');
    expect(errors.value.email).toBe('Invalid email');
  });

  it('should clear existing errors before setting new ones', () => {
    const { errors, setFromNormalizedError } = useFormErrors(['field1']);

    // Set initial error
    errors.value.field1 = 'Initial error';

    // Set new error with empty subErrors
    const mockError: NormalizedError = {
      kind: 'validation',
      message: 'Other error',
      subErrors: []
    };

    setFromNormalizedError(mockError);

    expect(errors.value.field1).toBe('');
  });

  it('should ignore subErrors for fields not in initial list', () => {
    const { errors, setFromNormalizedError } = useFormErrors(['field1']);

    const mockError: NormalizedError = {
      kind: 'validation',
      message: 'Validation failed',
      subErrors: [
        { field: 'field1', message: 'Error 1' },
        { field: 'unknownField', message: 'Error 2' }
      ]
    };

    setFromNormalizedError(mockError);

    expect(errors.value.field1).toBe('Error 1');
    expect(errors.value).not.toHaveProperty('unknownField');
  });

  it('should detect if has errors', () => {
    const { errors, hasErrors } = useFormErrors(['field1']);

    expect(hasErrors()).toBe(false);

    errors.value.field1 = 'Error';
    expect(hasErrors()).toBe(true);
  });
});
