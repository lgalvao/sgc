import {beforeEach, describe, expect, it, vi} from 'vitest';
import {effectScope, nextTick, ref} from 'vue';
import * as diagnosticoService from '@/services/diagnosticoService';
import {useSituacaoCapacitacaoDiagnostico} from '../useSituacaoCapacitacaoDiagnostico';

const invalidateQueriesMock = vi.fn();
const mockQueryData = ref<any>(null);
const mockQueryStatus = ref<'pending' | 'success'>('success');
const mockQueryError = ref<Error | null>(null);

let queryOptions: any = null;
vi.mock('@pinia/colada', () => ({
    useQuery: vi.fn((options: any) => {
        queryOptions = options;
        return {
            data: mockQueryData,
            status: mockQueryStatus,
            error: mockQueryError,
        };
    }),
    useQueryCache: () => ({
        invalidateQueries: invalidateQueriesMock,
    }),
    useMutation: vi.fn((options: any) => {
        const isLoading = ref(false);
        const error = ref<Error | null>(null);

        const mutateAsync = vi.fn(async (arg?: unknown) => {
            isLoading.value = true;
            try {
                const resultado = await options.mutation(arg);
                options.onSuccess?.(resultado, arg, undefined);
                return resultado;
            } catch (erro) {
                error.value = erro as Error;
                throw erro;
            } finally {
                isLoading.value = false;
                options.onSettled?.();
            }
        });

        return {
            isLoading,
            error,
            mutate: (arg?: unknown) => {
                void mutateAsync(arg);
            },
            mutateAsync,
        };
    }),
}));

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({
        usuarioCodigo: '151515',
    }),
}));

vi.mock('@/services/diagnosticoService', () => ({
    obterDiagnosticoUnidade: vi.fn(),
    salvarSituacoesCapacitacao: vi.fn(),
}));

describe('useSituacaoCapacitacaoDiagnostico', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.useFakeTimers();
        mockQueryStatus.value = 'success';
        mockQueryError.value = null;
        mockQueryData.value = {
            unidade: {unidadeSigla: 'ASSESSORIA_12'},
            servidores: [{servidorTitulo: '242426'}],
            movimentacoes: [],
            situacoesCapacitacao: [
                {servidorTitulo: '242426', competenciaCodigo: 10, situacaoCapacitacao: null},
                {servidorTitulo: '242427', competenciaCodigo: 20, situacaoCapacitacao: 'AC'},
            ],
        };
    });

    it('deve hidratar situações locais e calcular pendentes', async () => {
        const scope = effectScope();
        let composable: ReturnType<typeof useSituacaoCapacitacaoDiagnostico> | undefined;

        scope.run(() => {
            composable = useSituacaoCapacitacaoDiagnostico(70);
        });
        await nextTick();

        expect(composable!.situacoesLocais.value).toEqual(mockQueryData.value.situacoesCapacitacao);
        expect(composable!.pendentes.value).toBe(1);

        scope.stop();
    });

    it('não deve alterar situacoesLocais se novas for undefined', async () => {
        mockQueryData.value = { situacoesCapacitacao: undefined };
        const scope = effectScope();
        let composable: ReturnType<typeof useSituacaoCapacitacaoDiagnostico> | undefined;

        scope.run(() => {
            composable = useSituacaoCapacitacaoDiagnostico(70);
        });
        await nextTick();

        expect(composable!.situacoesLocais.value).toEqual([]);
        scope.stop();
    });

    it('deve usar valores default para as props da query (servidores e movimentacoes)', async () => {
        mockQueryData.value = { servidores: undefined, movimentacoes: undefined, unidade: undefined };
        const scope = effectScope();
        let composable: ReturnType<typeof useSituacaoCapacitacaoDiagnostico> | undefined;

        scope.run(() => {
            composable = useSituacaoCapacitacaoDiagnostico(70);
        });
        await nextTick();

        expect(composable!.servidores.value).toEqual([]);
        expect(composable!.movimentacoes.value).toEqual([]);
        expect(composable!.unidade.value).toBeUndefined();
        scope.stop();
    });

    it('deve calcular 0 pendentes se não houver situações ou todas estiverem preenchidas', async () => {
        mockQueryData.value = {
            situacoesCapacitacao: [
                {servidorTitulo: '242426', competenciaCodigo: 10, situacaoCapacitacao: 'AC'},
            ],
        };
        const scope = effectScope();
        let composable: ReturnType<typeof useSituacaoCapacitacaoDiagnostico> | undefined;

        scope.run(() => {
            composable = useSituacaoCapacitacaoDiagnostico(70);
        });
        await nextTick();

        expect(composable!.pendentes.value).toBe(0);
        scope.stop();
    });

    it('deve fazer autosave com debounce e invalidar a unidade', async () => {
        vi.mocked(diagnosticoService.salvarSituacoesCapacitacao).mockResolvedValue();
        const scope = effectScope();
        let composable: ReturnType<typeof useSituacaoCapacitacaoDiagnostico> | undefined;

        scope.run(() => {
            composable = useSituacaoCapacitacaoDiagnostico(71);
        });
        await nextTick();

        // Duas chamadas para cobrir a limpeza do timer
        composable!.atualizarCapacitacao('242426', 10, 'EC');
        composable!.atualizarCapacitacao('242426', 10, 'AC');

        expect(composable!.salvandoAutomaticamente.value).toBe(true);
        expect(composable!.pendentes.value).toBe(0);

        await vi.advanceTimersByTimeAsync(800);
        await Promise.resolve();

        expect(diagnosticoService.salvarSituacoesCapacitacao).toHaveBeenCalledWith(71, {
            situacoes: [
                {servidorTitulo: '242426', competenciaCodigo: 10, situacaoCapacitacao: 'AC'},
                {servidorTitulo: '242427', competenciaCodigo: 20, situacaoCapacitacao: 'AC'},
            ],
        });
        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'unidade', '151515', 'sem-perfil', 'sem-unidade', 71],
            exact: true,
        });
        expect(composable!.salvandoAutomaticamente.value).toBe(false);
        scope.stop();
    });

    it('deve ignorar atualização para situação inexistente', async () => {
        vi.mocked(diagnosticoService.salvarSituacoesCapacitacao).mockResolvedValue();
        const scope = effectScope();
        let composable: ReturnType<typeof useSituacaoCapacitacaoDiagnostico> | undefined;

        scope.run(() => {
            composable = useSituacaoCapacitacaoDiagnostico(72);
        });
        await nextTick();

        composable!.atualizarCapacitacao('999999', 999, 'I');
        await vi.advanceTimersByTimeAsync(900);

        expect(diagnosticoService.salvarSituacoesCapacitacao).not.toHaveBeenCalled();

        scope.stop();
    });

    it('deve exercitar as opções do useQuery para chave, query e enabled', async () => {
        const scope = effectScope();
        scope.run(() => {
            useSituacaoCapacitacaoDiagnostico(80);
        });
        await nextTick();

        expect(queryOptions).toBeDefined();

        // 1. key()
        expect(queryOptions.key()).toEqual(['diagnostico-competencias', 'unidade', '151515', 'sem-perfil', 'sem-unidade', 80]);

        // 2. query()
        vi.mocked(diagnosticoService.obterDiagnosticoUnidade).mockResolvedValue({} as any);
        await queryOptions.query();
        expect(diagnosticoService.obterDiagnosticoUnidade).toHaveBeenCalledWith(80);

        // 3. enabled()
        expect(queryOptions.enabled()).toBe(true);

        scope.stop();
    });
});
