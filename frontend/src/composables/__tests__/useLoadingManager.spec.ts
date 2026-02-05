import {describe, expect, it, vi} from 'vitest';
import {useLoadingManager, useSingleLoading} from '@/composables/useLoadingManager';
import {logger} from '@/utils';

vi.mock('@/utils', () => ({
    logger: {
        warn: vi.fn(),
    }
}));

describe('useLoadingManager', () => {
    it('deve inicializar os estados corretamente', () => {
        const { states } = useLoadingManager(['fetch', 'save']);
        expect(states.fetch.value).toBe(false);
        expect(states.save.value).toBe(false);
    });

    it('deve iniciar e parar um estado', () => {
        const { start, stop, isLoading } = useLoadingManager(['fetch']);

        start('fetch');
        expect(isLoading('fetch')).toBe(true);

        stop('fetch');
        expect(isLoading('fetch')).toBe(false);
    });

    it('deve identificar se qualquer estado está carregando', () => {
        const { start, anyLoading } = useLoadingManager(['m1', 'm2']);

        expect(anyLoading.value).toBe(false);

        start('m1');
        expect(anyLoading.value).toBe(true);
    });

    it('deve parar todos os estados', () => {
        const { start, stopAll, anyLoading } = useLoadingManager(['m1', 'm2']);
        start('m1');
        start('m2');

        stopAll();
        expect(anyLoading.value).toBe(false);
    });

    it('deve usar wrapper withLoading', async () => {
        const { withLoading, isLoading } = useLoadingManager(['m1']);

        const promise = withLoading('m1', async () => {
            expect(isLoading('m1')).toBe(true);
            return 'done';
        });

        const result = await promise;
        expect(result).toBe('done');
        expect(isLoading('m1')).toBe(false);
    });

    it('deve logar aviso se tentar usar estado não registrado', () => {
        const { start, stop } = useLoadingManager(['m1']);

        start('m2');
        expect(logger.warn).toHaveBeenCalledWith(expect.stringContaining('m2'));

        stop('m2');
        expect(logger.warn).toHaveBeenCalledTimes(2);
    });
});

describe('useSingleLoading', () => {
    it('deve gerenciar estado simples', () => {
        const { isLoading, start, stop, toggle } = useSingleLoading(true);

        expect(isLoading.value).toBe(true);

        stop();
        expect(isLoading.value).toBe(false);

        start();
        expect(isLoading.value).toBe(true);

        toggle();
        expect(isLoading.value).toBe(false);
    });

    it('deve usar wrapper withLoading simples', async () => {
        const { withLoading, isLoading } = useSingleLoading();

        await withLoading(async () => {
            expect(isLoading.value).toBe(true);
        });

        expect(isLoading.value).toBe(false);
    });
});
