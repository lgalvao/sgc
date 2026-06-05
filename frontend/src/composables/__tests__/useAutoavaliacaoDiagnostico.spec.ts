import {beforeEach, describe, expect, it, vi} from 'vitest';
import {effectScope, nextTick, ref} from 'vue';
import * as diagnosticoService from '@/services/diagnosticoService';
import {useAutoavaliacaoDiagnostico} from '../useAutoavaliacaoDiagnostico';

const invalidateQueriesMock = vi.fn();
const mockQueryData = ref<any>(null);
const mockQueryStatus = ref<'pending' | 'success'>('success');
const mockQueryError = ref<Error | null>(null);

vi.mock('@pinia/colada', () => ({
    useQuery: vi.fn(() => ({
        data: mockQueryData,
        status: mockQueryStatus,
        error: mockQueryError,
    })),
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
        usuarioCodigo: '242426',
    }),
}));

vi.mock('@/services/diagnosticoService', () => ({
    obterAutoavaliacao: vi.fn(),
    salvarAutoavaliacao: vi.fn(),
    concluirAutoavaliacao: vi.fn(),
}));

describe('useAutoavaliacaoDiagnostico', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.useFakeTimers();
        mockQueryStatus.value = 'success';
        mockQueryError.value = null;
        mockQueryData.value = {
            situacaoServidor: 'AUTOAVALIACAO_NAO_INICIADA',
            competencias: [
                {competenciaCodigo: 10, importancia: 1, dominio: 2},
                {competenciaCodigo: 20, importancia: 3, dominio: 4},
            ],
        };
    });

    it('deve hidratar as competências locais com os dados da query', async () => {
        const scope = effectScope();
        let composable: ReturnType<typeof useAutoavaliacaoDiagnostico> | undefined;

        scope.run(() => {
            composable = useAutoavaliacaoDiagnostico(99);
        });
        await nextTick();

        expect(composable!.competenciasLocais.value).toEqual([
            {competenciaCodigo: 10, importancia: 1, dominio: 2},
            {competenciaCodigo: 20, importancia: 3, dominio: 4},
        ]);

        scope.stop();
    });

    it('deve fazer autosave com debounce e exibir autoguardado', async () => {
        vi.mocked(diagnosticoService.salvarAutoavaliacao).mockResolvedValue();
        const scope = effectScope();
        let composable: ReturnType<typeof useAutoavaliacaoDiagnostico> | undefined;

        scope.run(() => {
            composable = useAutoavaliacaoDiagnostico(99);
        });
        await nextTick();

        composable!.atualizarNota(10, 'importancia', 5);

        expect(composable!.salvandoAutomaticamente.value).toBe(true);
        expect(composable!.autoguardado.value).toBe(false);

        await vi.advanceTimersByTimeAsync(800);
        await Promise.resolve();

        expect(diagnosticoService.salvarAutoavaliacao).toHaveBeenCalledWith(99, {
            competencias: [
                {competenciaCodigo: 10, importancia: 5, dominio: 2},
                {competenciaCodigo: 20, importancia: 3, dominio: 4},
            ],
        });
        expect(composable!.salvandoAutomaticamente.value).toBe(false);
        expect(composable!.autoguardado.value).toBe(true);

        await vi.advanceTimersByTimeAsync(2000);
        expect(composable!.autoguardado.value).toBe(false);

        scope.stop();
    });

    it('deve invalidar autoavaliação e equipe ao concluir', async () => {
        vi.mocked(diagnosticoService.concluirAutoavaliacao).mockResolvedValue();
        const scope = effectScope();
        let composable: ReturnType<typeof useAutoavaliacaoDiagnostico> | undefined;

        scope.run(() => {
            composable = useAutoavaliacaoDiagnostico(77);
        });
        await nextTick();

        await composable!.concluirAutoavaliacao();

        expect(diagnosticoService.concluirAutoavaliacao).toHaveBeenCalledWith(77);
        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'autoavaliacao', '242426', 'sem-perfil', 'sem-unidade', 77],
            exact: true,
        });
        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'equipe', '242426', 'sem-perfil', 'sem-unidade', 77],
            exact: true,
        });

        scope.stop();
    });
});
