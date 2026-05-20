import {describe, expect, it} from 'vitest';
import {useFormErrors} from '../useFormErrors';

describe('useFormErrors', () => {
    it('initializes with default values', () => {
        const {erros} = useFormErrors(['field1', 'field2']);
        expect(erros.value).toEqual({field1: '', field2: ''});
    });

    it('sets erros from normalized error', () => {
        const {erros, aplicarErroNormalizado} = useFormErrors(['field1']);
        const normalizedError = {
            tipo: 'validacao' as const,
            mensagem: 'Global error',
            erros: [{campo: 'field1', mensagem: 'Error 1'}]
        };

        aplicarErroNormalizado(normalizedError);
        expect(erros.value.field1).toBe('Error 1');
    });

    it('clears erros', () => {
        const {erros, limparErros} = useFormErrors(['field1']);
        erros.value.field1 = 'Error';
        limparErros();
        expect(erros.value.field1).toBe('');
    });

    it('detects erros', () => {
        const {erros, temErros} = useFormErrors(['field1']);
        expect(temErros()).toBe(false);
        erros.value.field1 = 'Error';
        expect(temErros()).toBe(true);
    });

    it('handles null normalized error', () => {
        const {erros, aplicarErroNormalizado} = useFormErrors(['field1']);
        aplicarErroNormalizado(null);
        expect(erros.value.field1).toBe('');
    });

    it('handles normalized error without erros', () => {
        const {erros, aplicarErroNormalizado} = useFormErrors(['field1']);
        const normalizedError = {
            tipo: 'validacao' as const,
            mensagem: 'Global error'
        };
        aplicarErroNormalizado(normalizedError);
        expect(erros.value.field1).toBe('');
    });

    it('ignores erros with missing fields', () => {
        const {erros, aplicarErroNormalizado} = useFormErrors(['field1']);
        const normalizedError = {
            tipo: 'validacao' as const,
            mensagem: 'Global error',
            erros: [{mensagem: 'Error 1'}] // Missing campo
        };
        aplicarErroNormalizado(normalizedError);
        expect(erros.value.field1).toBe('');
    });

    it('ignores erros for fields not in tracking list', () => {
        const {erros, aplicarErroNormalizado} = useFormErrors(['field1']);
        const normalizedError = {
            tipo: 'validacao' as const,
            mensagem: 'Global error',
            erros: [{campo: 'otherField', mensagem: 'Error 1'}]
        };
        aplicarErroNormalizado(normalizedError);
        expect(erros.value.field1).toBe('');
        expect(erros.value).not.toHaveProperty('otherField');
    });

    it('uses default message if erro message is missing', () => {
        const {erros, aplicarErroNormalizado} = useFormErrors(['field1']);
        const normalizedError = {
            tipo: 'validacao' as const,
            mensagem: 'Global error',
            erros: [{campo: 'field1'}] // Missing message
        };
        aplicarErroNormalizado(normalizedError);
        expect(erros.value.field1).toBe('Campo inválido');
    });
});
