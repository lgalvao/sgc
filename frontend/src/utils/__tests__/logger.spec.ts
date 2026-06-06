import {describe, expect, it, vi} from 'vitest';
import logger, {getLogLevel} from '../logger';

vi.unmock("@/utils/logger");

describe('logger', () => {
    it('deve exportar uma instância do logger com os métodos esperados', () => {
        expect(typeof logger.info).toBe('function');
        expect(typeof logger.error).toBe('function');
        expect(typeof logger.warn).toBe('function');
        expect(typeof logger.success).toBe('function');
    });

    describe('getLogLevel', () => {
        it('deve retornar 1 para o modo test', () => {
            expect(getLogLevel('test')).toBe(1);
        });

        it('deve retornar 3 para o modo production', () => {
            expect(getLogLevel('production')).toBe(3);
        });

        it('deve retornar 4 para o modo development ou outros', () => {
            expect(getLogLevel('development')).toBe(4);
            expect(getLogLevel('other')).toBe(4);
        });

        it('deve retornar 4 para ambientes desconhecidos como staging e string vazia', () => {
            expect(getLogLevel('staging')).toBe(4);
            expect(getLogLevel('')).toBe(4);
        });

        it('deve detectar o modo ambiente quando não informado', () => {
            // Como estamos rodando em VITEST, ele deve retornar 1 (test)
            expect(getLogLevel()).toBe(1);
        });

        it('deve cair nos fallbacks do process.env e development quando import.meta não possuir MODE', () => {
            const importMetaComEnv = import.meta as any;
            const originalMode = importMetaComEnv.env?.MODE;
            if (importMetaComEnv.env) {
                importMetaComEnv.env.MODE = '';
            }

            // Fallback 1: process.env.VITEST
            expect(getLogLevel()).toBe(1);

            // Fallback 2: "development"
            const originalVitest = process.env.VITEST;
            delete process.env.VITEST;
            expect(getLogLevel()).toBe(4);

            // Restaura
            if (importMetaComEnv.env) {
                importMetaComEnv.env.MODE = originalMode;
            }
            process.env.VITEST = originalVitest;
        });
    });
});
