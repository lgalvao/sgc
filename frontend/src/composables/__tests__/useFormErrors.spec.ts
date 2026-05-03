import {describe, expect, it} from 'vitest';
import {useFormErrors} from '../useFormErrors';

describe('useFormErrors', () => {
    it('initializes with default values', () => {
        const {errors} = useFormErrors(['field1', 'field2']);
        expect(errors.value).toEqual({field1: '', field2: ''});
    });

    it('sets errors from normalized error', () => {
        const {errors, setFromErroNormalizado} = useFormErrors(['field1']);
        const normalizedError = {
            tipo: 'validacao' as const,
            mensagem: 'Global error',
            erros: [{campo: 'field1', mensagem: 'Error 1'}]
        };

        setFromErroNormalizado(normalizedError);
        expect(errors.value.field1).toBe('Error 1');
    });

    it('clears errors', () => {
        const {errors, clearErrors} = useFormErrors(['field1']);
        errors.value.field1 = 'Error';
        clearErrors();
        expect(errors.value.field1).toBe('');
    });

    it('detects errors', () => {
        const {errors, hasErrors} = useFormErrors(['field1']);
        expect(hasErrors()).toBe(false);
        errors.value.field1 = 'Error';
        expect(hasErrors()).toBe(true);
    });

    it('handles null normalized error', () => {
        const {errors, setFromErroNormalizado} = useFormErrors(['field1']);
        setFromErroNormalizado(null);
        expect(errors.value.field1).toBe('');
    });

    it('handles normalized error without erros', () => {
        const {errors, setFromErroNormalizado} = useFormErrors(['field1']);
        const normalizedError = {
            tipo: 'validacao' as const,
            mensagem: 'Global error'
        };
        setFromErroNormalizado(normalizedError);
        expect(errors.value.field1).toBe('');
    });

    it('ignores erros with missing fields', () => {
        const {errors, setFromErroNormalizado} = useFormErrors(['field1']);
        const normalizedError = {
            tipo: 'validacao' as const,
            mensagem: 'Global error',
            erros: [{mensagem: 'Error 1'}] // Missing campo
        };
        setFromErroNormalizado(normalizedError);
        expect(errors.value.field1).toBe('');
    });

    it('ignores erros for fields not in tracking list', () => {
        const {errors, setFromErroNormalizado} = useFormErrors(['field1']);
        const normalizedError = {
            tipo: 'validacao' as const,
            mensagem: 'Global error',
            erros: [{campo: 'otherField', mensagem: 'Error 1'}]
        };
        setFromErroNormalizado(normalizedError);
        expect(errors.value.field1).toBe('');
        expect(errors.value).not.toHaveProperty('otherField');
    });

    it('uses default message if erro message is missing', () => {
        const {errors, setFromErroNormalizado} = useFormErrors(['field1']);
        const normalizedError = {
            tipo: 'validacao' as const,
            mensagem: 'Global error',
            erros: [{campo: 'field1'}] // Missing message
        };
        setFromErroNormalizado(normalizedError);
        expect(errors.value.field1).toBe('Campo inválido');
    });
});
