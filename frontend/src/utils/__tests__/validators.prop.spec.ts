import {describe, expect, it} from 'vitest';
import fc from 'fast-check';
import {validarEmail, validarSenha} from '../validators';

describe('Propriedades de Validação (validators.ts)', () => {

    describe('validarEmail', () => {
        it('deve rejeitar strings que não contêm @', () => {
            fc.assert(
                fc.property(
                    fc.string().filter(s => !s.includes('@')),
                    (s) => {
                        return !validarEmail(s);
                    }
                )
            );
        });

        it('deve aceitar emails válidos gerados', () => {
             fc.assert(
                fc.property(
                    fc.stringMatching(/^[a-zA-Z0-9]+@[a-zA-Z0-9]+\.[a-zA-Z]{2,4}$/),
                    (email) => {
                         return validarEmail(email);
                    }
                )
             );
        });

        it('deve rejeitar nulos ou undefined', () => {
            expect(validarEmail(null)).toBe(false);
            expect(validarEmail(undefined)).toBe(false);
        });
    });

    describe('validarSenha', () => {
        it('senhas válidas devem ter pelo menos 8 caracteres', () => {
            fc.assert(
                fc.property(
                    fc.string().filter(s => s.length < 8),
                    (s) => {
                        return !validarSenha(s);
                    }
                )
            );
        });

        it('senhas válidas devem ter letras', () => {
             fc.assert(
                fc.property(
                    fc.string(),
                    (s) => {
                        // Se não tem letra, deve ser falso
                        if (!/[A-Za-z]/.test(s)) {
                            return !validarSenha(s);
                        }
                        return true; // Se tem letra, pode ser verdadeiro ou falso dependendo de outras regras
                    }
                )
             );
        });
    });
});
