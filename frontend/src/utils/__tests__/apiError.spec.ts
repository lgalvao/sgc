import {describe, expect, it, vi} from 'vitest';
import {
    existsOrFalse,
    getOrNull,
    isAxiosError,
    normalizeError,
    notifyError,
    shouldNotifyGlobally
} from '@/utils/apiError';
import {useFeedbackStore} from '@/stores/feedback';
import {createTestingPinia} from '@pinia/testing';
import logger from '@/utils/logger';

describe('apiError utils', () => {
    it('isAxiosError deve identificar erros do axios', () => {
        expect(isAxiosError({ isAxiosError: true })).toBe(true);
        expect(isAxiosError({ isAxiosError: false })).toBe(false);
        expect(isAxiosError({})).toBe(false);
        expect(isAxiosError(null)).toBe(false);
    });

    describe('normalizeError', () => {
        it('deve normalizar erro de rede', () => {
            const err = { isAxiosError: true, response: undefined };
            const normalized = normalizeError(err);
            expect(normalized.kind).toBe('network');
            expect(normalized.message).toContain('conexão');
        });

        it('deve normalizar erro HTTP com resposta', () => {
            const err = {
                isAxiosError: true,
                response: {
                    status: 404,
                    data: { message: 'Não achei', code: 'E001' }
                }
            };
            const normalized = normalizeError(err);
            expect(normalized.kind).toBe('notFound');
            expect(normalized.message).toBe('Não achei');
            expect(normalized.code).toBe('E001');
            expect(normalized.status).toBe(404);
        });

        it('deve lidar com resposta sem dados', () => {
            const err = {
                isAxiosError: true,
                response: { status: 500 }
            };
            const normalized = normalizeError(err);
            expect(normalized.kind).toBe('unexpected');
            expect(normalized.message).toBe('Erro 500: O servidor não retornou uma mensagem detalhada.');
        });

        it('deve normalizar erro genérico do JS', () => {
            const err = new Error('Erro custom');
            const normalized = normalizeError(err);
            expect(normalized.kind).toBe('unexpected');
            expect(normalized.message).toBe('Erro custom');
            expect(normalized.stackTrace).toBeDefined();
        });

        it('deve normalizar erro desconhecido (string)', () => {
            const normalized = normalizeError('string error');
            expect(logger.error).toHaveBeenCalledWith("[normalizeError] Erro não mapeado:", "string error");
            expect(normalized.kind).toBe('unexpected');
            expect(normalized.message).toBe('Erro desconhecido ou não mapeado pela aplicação.');
        });

        it('mapeia outros status corretamente', () => {
            expect(normalizeError({ isAxiosError: true, response: { status: 400 } }).kind).toBe('validation');
            expect(normalizeError({ isAxiosError: true, response: { status: 401 } }).kind).toBe('unauthorized');
            expect(normalizeError({ isAxiosError: true, response: { status: 403 } }).kind).toBe('forbidden');
            expect(normalizeError({ isAxiosError: true, response: { status: 409 } }).kind).toBe('conflict');
            expect(normalizeError({ isAxiosError: true, response: { status: 503 } }).kind).toBe('unexpected');
        });
    });

    it('shouldNotifyGlobally deve decidir corretamente', () => {
        expect(shouldNotifyGlobally({ kind: 'unauthorized' } as any)).toBe(true);
        expect(shouldNotifyGlobally({ kind: 'validation' } as any)).toBe(false);
        expect(shouldNotifyGlobally({ kind: 'network' } as any)).toBe(true);
        expect(shouldNotifyGlobally({ kind: 'forbidden' } as any)).toBe(false);
    });

    it('notifyError deve chamar o feedbackStore', () => {
        const pinia = createTestingPinia();
        const feedbackStore = useFeedbackStore(pinia);

        notifyError({ kind: 'notFound', message: 'Op' } as any);

        expect(feedbackStore.show).toHaveBeenCalledWith('Não Encontrado', 'Op', 'danger', 7000);
    });

    describe('helper functions', () => {
        it('existsOrFalse retorna true se sucesso', async () => {
            const call = vi.fn().mockResolvedValue('ok');
            const res = await existsOrFalse(call);
            expect(res).toBe(true);
        });

        it('existsOrFalse retorna false se 404', async () => {
            const call = vi.fn().mockRejectedValue({ isAxiosError: true, response: { status: 404 } });
            const res = await existsOrFalse(call);
            expect(res).toBe(false);
        });

        it('existsOrFalse propaga outros erros', async () => {
            const call = vi.fn().mockRejectedValue(new Error('Other'));
            await expect(existsOrFalse(call)).rejects.toThrow('Other');
        });

        it('getOrNull retorna dado se sucesso', async () => {
            const call = vi.fn().mockResolvedValue('data');
            const res = await getOrNull(call);
            expect(res).toBe('data');
        });

        it('getOrNull retorna null se 404', async () => {
            const call = vi.fn().mockRejectedValue({ isAxiosError: true, response: { status: 404 } });
            const res = await getOrNull(call);
            expect(res).toBeNull();
        });
    });
});
