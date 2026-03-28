import {describe, expect, it, vi} from 'vitest';
import {useGerenciadorCarregamento, useCarregamentoSimples} from '@/composables/useLoadingManager';
import {logger} from '@/utils';

vi.mock('@/utils', () => ({
    logger: {
        warn: vi.fn(),
    }
}));

describe('useGerenciadorCarregamento', () => {
    it('deve inicializar os estados corretamente', () => {
        const {estados} = useGerenciadorCarregamento(['buscar', 'salvar']);
        expect(estados.buscar.value).toBe(false);
        expect(estados.salvar.value).toBe(false);
    });

    it('deve iniciar e parar um estado', () => {
        const {iniciar, parar, estaCarregando} = useGerenciadorCarregamento(['buscar']);

        iniciar('buscar');
        expect(estaCarregando('buscar')).toBe(true);

        parar('buscar');
        expect(estaCarregando('buscar')).toBe(false);
    });

    it('deve identificar se qualquer estado está carregando', () => {
        const {iniciar, qualquerCarregando} = useGerenciadorCarregamento(['m1', 'm2']);

        expect(qualquerCarregando.value).toBe(false);

        iniciar('m1');
        expect(qualquerCarregando.value).toBe(true);
    });

    it('deve parar todos os estados', () => {
        const {iniciar, pararTodos, qualquerCarregando} = useGerenciadorCarregamento(['m1', 'm2']);
        iniciar('m1');
        iniciar('m2');

        pararTodos();
        expect(qualquerCarregando.value).toBe(false);
    });

    it('deve usar wrapper comCarregamento', async () => {
        const {comCarregamento, estaCarregando} = useGerenciadorCarregamento(['m1']);

        const promise = comCarregamento('m1', async () => {
            expect(estaCarregando('m1')).toBe(true);
            return 'done';
        });

        const result = await promise;
        expect(result).toBe('done');
        expect(estaCarregando('m1')).toBe(false);
    });

    it('deve logar aviso se tentar usar estado não registrado', () => {
        const {iniciar, parar, estaCarregando} = useGerenciadorCarregamento(['m1']);

        iniciar('m2');
        expect(logger.warn).toHaveBeenCalledWith(expect.stringContaining('m2'));

        parar('m2');
        expect(logger.warn).toHaveBeenCalledTimes(2);

        expect(estaCarregando('m2')).toBe(false);
    });
});

describe('useCarregamentoSimples', () => {
    it('deve gerenciar estado simples', () => {
        const {estaCarregando, iniciar, parar, alternar} = useCarregamentoSimples(true);

        expect(estaCarregando.value).toBe(true);

        parar();
        expect(estaCarregando.value).toBe(false);

        iniciar();
        expect(estaCarregando.value).toBe(true);

        alternar();
        expect(estaCarregando.value).toBe(false);
    });

    it('deve usar wrapper comCarregamento simples', async () => {
        const {comCarregamento, estaCarregando} = useCarregamentoSimples();

        await comCarregamento(async () => {
            expect(estaCarregando.value).toBe(true);
        });

        expect(estaCarregando.value).toBe(false);
    });
});
