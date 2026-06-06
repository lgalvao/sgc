import {describe, expect, it} from 'vitest';
import {deveNotificarGlobalmente, ehErroAxios, normalizarErro} from '@/utils/apiError';
import {extrairErrosGenericos} from '@/utils/apiError/helpers';
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
            expect(normalized.stackTrace).toContain('Error: Erro custom');
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

        it('deve extrair traceId e erros do payload se presentes', () => {
            const err = {
                isAxiosError: true,
                response: {
                    status: 422,
                    data: {
                        message: 'Entidade não processável',
                        traceId: 'trace-123',
                        erros: [{campo: 'nome', mensagem: 'obrigatório'}],
                    },
                },
            };
            const normalized = normalizarErro(err);
            expect(normalized.traceId).toBe('trace-123');
            expect(normalized.erros).toHaveLength(1);
            expect(normalized.erros![0]).toEqual({campo: 'nome', mensagem: 'obrigatório'});
        });

        it('deve mapear o campo details do payload para detalhes no erro normalizado', () => {
            const err = {
                isAxiosError: true,
                response: {
                    status: 400,
                    data: {
                        message: 'Erro de validação customizado',
                        code: 'VAL_001',
                        details: 'algum detalhe',
                    },
                },
            };
            const normalized = normalizarErro(err);
            expect(normalized.tipo).toBe('validacao');
            expect(normalized.codigo).toBe('VAL_001');
            expect(normalized.detalhes).toBe('algum detalhe');
        });
    });

    it('deveNotificarGlobalmente deve decidir corretamente', () => {
        expect(deveNotificarGlobalmente({tipo: 'naoAutorizado'} as any)).toBe(true);
        expect(deveNotificarGlobalmente({tipo: 'validacao'} as any)).toBe(false);
        expect(deveNotificarGlobalmente({tipo: 'rede'} as any)).toBe(true);
        expect(deveNotificarGlobalmente({tipo: 'proibido'} as any)).toBe(false);
    });

    describe('extrairErrosGenericos', () => {
        it('deve retornar vazio se não houver erros', () => {
            expect(extrairErrosGenericos({} as any)).toEqual([]);
        });

        it('deve retornar apenas erros globais e sem campo especifico', () => {
            const err = {
                erros: [
                    {campo: 'nome', mensagem: 'Invalido'},
                    {mensagem: 'Erro global'},
                    {campo: null, mensagem: 'Outro erro'},
                    {campo: undefined, mensagem: ''}
                ]
            } as any;
            expect(extrairErrosGenericos(err)).toEqual(['Erro global', 'Outro erro']);
        });
    });

    describe('branches restantes no normalizer', () => {
        it('deve normalizar Error sem message', () => {
            const err = new Error();
            err.message = ''; // simular sem message
            const normalized = normalizarErro(err);
            expect(normalized.mensagem).toBe('Erro inesperado.');
        });

        it('deve normalizar Erro com data null', () => {
            const err = {
                isAxiosError: true,
                response: {
                    status: 400,
                    data: null,
                }
            };
            const normalized = normalizarErro(err);
            expect(normalized.tipo).toBe('validacao');
            expect(normalized.mensagem).toBe('Erro 400: O servidor não retornou uma mensagem detalhada.');
        });
        
        it('deve testar typeof erro === number', () => {
            const normalized = normalizarErro(123);
            expect(normalized.mensagem).toBe('Erro desconhecido ou não mapeado pela aplicação.');
        });
    });
});
