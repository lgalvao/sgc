import {beforeEach, describe, expect, it, vi} from 'vitest';
import {effectScope, nextTick, ref} from 'vue';
import {useSituacaoCapacitacaoDiagnostico} from '../useSituacaoCapacitacaoDiagnostico';

const mockQueryData = ref<any>(null);
const mockQueryStatus = ref<'pending' | 'success'>('success');
const mockQueryError = ref<Error | null>(null);
const invalidateQueriesMock = vi.fn();
const mutateAsyncMock = vi.fn();
const mutateMock = vi.fn();

vi.mock('@pinia/colada', () => ({
    useQuery: vi.fn(() => ({
        data: mockQueryData,
        status: mockQueryStatus,
        error: mockQueryError,
    })),
    useQueryCache: () => ({
        invalidateQueries: invalidateQueriesMock,
    }),
    useMutation: vi.fn((options: any) => ({
        isLoading: ref(false),
        error: ref(null),
        mutate: vi.fn((arg?: unknown) => {
            mutateMock(arg);
            return options.mutation(arg);
        }),
        mutateAsync: vi.fn(async (arg?: unknown) => {
            mutateAsyncMock(arg);
            const resultado = await options.mutation(arg);
            options.onSuccess?.(resultado, arg, undefined);
            options.onSettled?.();
            return resultado;
        }),
    })),
}));

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({
        usuarioCodigo: '151515',
        perfilSelecionado: 'CHEFE',
        unidadeSelecionada: 12,
    }),
}));

vi.mock('@/services/diagnosticoService', () => ({
    obterDiagnosticoUnidade: vi.fn(),
    salvarSituacoesCapacitacao: vi.fn().mockResolvedValue(undefined),
}));

describe('useSituacaoCapacitacaoDiagnostico', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        mockQueryStatus.value = 'success';
        mockQueryError.value = null;
        mockQueryData.value = {
            unidade: {unidadeSigla: 'ASSESSORIA_12'},
            movimentacoes: [],
            situacoesCapacitacao: [],
            servidores: [
                {
                    servidorTitulo: '242426',
                    servidorNome: 'Servidor A',
                    situacaoServidor: 'CONSENSO_APROVADO',
                    consenso: [
                        {competenciaCodigo: 10, importancia: 5, dominio: 3},
                        {competenciaCodigo: 20, importancia: 4, dominio: 2},
                    ],
                },
                {
                    servidorTitulo: '242427',
                    servidorNome: 'Servidor B',
                    situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA',
                    consenso: [
                        {competenciaCodigo: 10, importancia: 5, dominio: 3},
                    ],
                },
            ],
        };
    });

    it('materializa situações pendentes para servidores com consenso aprovado mesmo sem registros salvos', async () => {
        const scope = effectScope();
        let composable: ReturnType<typeof useSituacaoCapacitacaoDiagnostico> | undefined;

        scope.run(() => {
            composable = useSituacaoCapacitacaoDiagnostico(400);
        });
        await nextTick();

        expect(composable!.situacoesLocais.value).toEqual([
            {
                servidorTitulo: '242426',
                servidorNome: 'Servidor A',
                competenciaCodigo: 10,
                situacaoCapacitacao: null,
            },
            {
                servidorTitulo: '242426',
                servidorNome: 'Servidor A',
                competenciaCodigo: 20,
                situacaoCapacitacao: null,
            },
        ]);

        scope.stop();
    });
});
