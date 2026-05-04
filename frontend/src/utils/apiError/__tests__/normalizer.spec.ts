import {describe, expect, it} from 'vitest';
import {normalizarErro} from '../normalizer';

describe('normalizer.ts', () => {
    it('deve normalizar erro de requisição cancelada (ERR_CANCELED)', () => {
        const erro = {
            isAxiosError: true,
            code: 'ERR_CANCELED',
            name: 'CanceledError',
            stack: 'stack'
        };
        const resultado = normalizarErro(erro);
        expect(resultado.tipo).toBe('rede');
        expect(resultado.codigo).toBe('REQUEST_CANCELADA');
        expect(resultado.mensagem).toBe('Requisição cancelada.');
    });

    it('deve normalizar erro de rede sem resposta do servidor', () => {
        const erro = {
            isAxiosError: true,
            response: undefined,
            stack: 'stack'
        };
        const resultado = normalizarErro(erro);
        expect(resultado.tipo).toBe('rede');
        expect(resultado.mensagem).toContain('Não foi possível conectar');
    });

    it('deve normalizar erro 400 (Validação)', () => {
        const erro = {
            isAxiosError: true,
            response: {
                status: 400,
                data: {
                    message: 'Erro de validação customizado',
                    code: 'VAL_001',
                    details: 'algum detalhe'
                }
            }
        };
        const resultado = normalizarErro(erro);
        expect(resultado.tipo).toBe('validacao');
        expect(resultado.mensagem).toBe('Erro de validação customizado');
        expect(resultado.codigo).toBe('VAL_001');
        expect(resultado.detalhes).toBe('algum detalhe');
    });

    it('deve normalizar erro 401 (Não Autorizado)', () => {
        const erro = {
            isAxiosError: true,
            response: {
                status: 401,
                data: {message: 'Token expirado'}
            }
        };
        const resultado = normalizarErro(erro);
        expect(resultado.tipo).toBe('naoAutorizado');
        expect(resultado.status).toBe(401);
    });

    it('deve usar mensagem padrão quando o servidor não retorna message', () => {
        const erro = {
            isAxiosError: true,
            response: {
                status: 500,
                data: {}
            }
        };
        const resultado = normalizarErro(erro);
        expect(resultado.tipo).toBe('inesperado');
        expect(resultado.mensagem).toContain('Erro 500');
    });

    it('deve normalizar erro de instância genérica Error', () => {
        const erro = new Error('Erro genérico');
        const resultado = normalizarErro(erro);
        expect(resultado.tipo).toBe('inesperado');
        expect(resultado.mensagem).toBe('Erro genérico');
    });

    it('deve normalizar erro desconhecido (não objeto)', () => {
        const resultado = normalizarErro('string de erro');
        expect(resultado.tipo).toBe('inesperado');
        expect(resultado.mensagem).toContain('Erro desconhecido');
    });

    it('deve extrair traceId e erros do payload se presentes', () => {
        const erro = {
            isAxiosError: true,
            response: {
                status: 422,
                data: {
                    message: 'Unprocessable Entity',
                    traceId: 'trace-123',
                    erros: [{campo: 'nome', mensagem: 'obrigatório'}]
                }
            }
        };
        const resultado = normalizarErro(erro);
        expect(resultado.traceId).toBe('trace-123');
        expect(resultado.erros).toHaveLength(1);
    });
});
