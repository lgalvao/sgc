import { describe, it, expect } from 'vitest';
import { validarEmail, validarSenha } from '../validators';

describe('validators', () => {
    describe('validarEmail', () => {
        it('deve retornar false para email vazio', () => {
            expect(validarEmail('')).toBe(false);
            expect(validarEmail(null)).toBe(false);
            expect(validarEmail(undefined)).toBe(false);
        });
        it('deve retornar true para email válido', () => {
            expect(validarEmail('teste@teste.com')).toBe(true);
            expect(validarEmail('a@b.c')).toBe(true);
        });
        it('deve retornar false para email inválido', () => {
            expect(validarEmail('teste')).toBe(false);
            expect(validarEmail('teste@')).toBe(false);
            expect(validarEmail('@teste.com')).toBe(false);
            expect(validarEmail('teste@.com')).toBe(false);
        });
    });

    describe('validarSenha', () => {
        it('deve retornar false para senha vazia', () => {
            expect(validarSenha('')).toBe(false);
        });
        it('deve retornar false para senha curta', () => {
            expect(validarSenha('1234567')).toBe(false);
        });
        it('deve retornar false se não tiver letra', () => {
            expect(validarSenha('12345678')).toBe(false);
        });
        it('deve retornar false se não tiver número', () => {
            expect(validarSenha('abcdefgh')).toBe(false);
        });
        it('deve retornar true para senha válida', () => {
            expect(validarSenha('1234abcd')).toBe(true);
            expect(validarSenha('Senh@123')).toBe(true);
        });
    });
});
