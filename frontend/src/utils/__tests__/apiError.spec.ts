import {describe, expect, it} from 'vitest';
import {deveNotificarGlobalmente, ehErroAxios, normalizarErro} from '@/utils/apiError';
import logger from '@/utils/logger';

describe('apiError utils', () => {
    it('ehErroAxios deve identificar erros do axios', () => {
        expect(ehErroAxios({isAxiosError: true})).toBe(true);
        expect(ehErroAxios({isAxiosError: false})).toBe(false);
        expect(ehErroAxios({})).toBe(false);
        expect(ehErroAxios(null)).toBe(false);
    });

    describe('normalizarErro', () => {
        it('deve normalizar erro de rede', () => {
            const err = {isAxiosError: true, response: undefined};
            const normalized = normalizarErro(err);
            expect(normalized.tipo).toBe('rede');
            expect(normalized.mensagem).toContain('conexão');
        });

        it('deve normalizar requisicao cancelada', () => {
            const err = {isAxiosError: true, code: 'ERR_CANCELED', response: undefined};
            const normalized = normalizarErro(err);
            expect(normalized.tipo).toBe('rede');
            expect(normalized.codigo).toBe('REQUEST_CANCELADA');
            expect(normalized.mensagem).toBe('Requisição cancelada.');
        });

        it('deve normalizar erro HTTP com resposta', () => {
            const err = {
                isAxiosError: true,
                response: {
                    status: 404,
                    data: {message: 'Não achei', code: 'E001'}
                }
            };
            const normalized = normalizarErro(err);
            expect(normalized.tipo).toBe('naoEncontrado');
            expect(normalized.mensagem).toBe('Não achei');
            expect(normalized.codigo).toBe('E001');
            expect(normalized.status).toBe(404);
        });

        it('deve preservar erros estruturados em português sem alias legado', () => {
            const err = {
                isAxiosError: true,
                response: {
                    status: 400,
                    data: {
                        message: 'Dados inválidos',
                        erros: [{campo: 'descricao', mensagem: 'Descrição obrigatória'}],
                        subErrors: [{field: 'descricao', message: 'Alias legado'}]
                    }
                }
            };


            const normalized = normalizarErro(err);

            expect(normalized.erros).toEqual([{campo: 'descricao', mensagem: 'Descrição obrigatória'}]);
            expect(normalized).not.toHaveProperty('subErrors');
        });

        it('deve lidar com resposta sem dados', () => {
            const err = {
                isAxiosError: true,
                response: {status: 500}
            };
            const normalized = normalizarErro(err);
            expect(normalized.tipo).toBe('inesperado');
            expect(normalized.mensagem).toBe('Erro 500: O servidor não retornou uma mensagem detalhada.');
        });

        it('deve normalizar erro genérico do JS', () => {
            const err = new Error('Erro custom');
            const normalized = normalizarErro(err);
            expect(normalized.tipo).toBe('inesperado');
            expect(normalized.mensagem).toBe('Erro custom');
            expect(normalized.stackTrace).toBeDefined();
        });

        it('deve normalizar erro desconhecido (string)', () => {
            const normalized = normalizarErro('string error');
            expect(logger.error).toHaveBeenCalledWith("[normalizarErro] Erro não mapeado:", "string error");
            expect(normalized.tipo).toBe('inesperado');
            expect(normalized.mensagem).toBe('Erro desconhecido ou não mapeado pela aplicação.');
        });

        it('mapeia outros status corretamente', () => {
            expect(normalizarErro({isAxiosError: true, response: {status: 400}}).tipo).toBe('validacao');
            expect(normalizarErro({isAxiosError: true, response: {status: 401}}).tipo).toBe('naoAutorizado');
            expect(normalizarErro({isAxiosError: true, response: {status: 403}}).tipo).toBe('proibido');
            expect(normalizarErro({isAxiosError: true, response: {status: 409}}).tipo).toBe('conflito');
            expect(normalizarErro({isAxiosError: true, response: {status: 503}}).tipo).toBe('inesperado');
        });
    });

    it('deveNotificarGlobalmente deve decidir corretamente', () => {
        expect(deveNotificarGlobalmente({tipo: 'naoAutorizado'} as any)).toBe(true);
        expect(deveNotificarGlobalmente({tipo: 'validacao'} as any)).toBe(false);
        expect(deveNotificarGlobalmente({tipo: 'rede'} as any)).toBe(true);
        expect(deveNotificarGlobalmente({tipo: 'proibido'} as any)).toBe(false);
    });
});
