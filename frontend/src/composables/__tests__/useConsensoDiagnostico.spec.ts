import {beforeEach, describe, expect, it, vi} from 'vitest';
import {effectScope, nextTick, ref} from 'vue';
import * as diagnosticoService from '@/services/diagnosticoService';
import {useConsensoDiagnostico} from '../useConsensoDiagnostico';

const invalidateQueriesMock = vi.fn();
const setQueryDataMock = vi.fn();
const mockQueryData = ref<any>(null);
const mockQueryStatus = ref<'pending' | 'success'>('success');
const mockQueryError = ref<Error | null>(null);
const useQueryMock = vi.fn();

vi.mock('@pinia/colada', () => ({
    useQuery: (...args: unknown[]) => useQueryMock(...args),
    useQueryCache: () => ({
        invalidateQueries: invalidateQueriesMock,
        setQueryData: setQueryDataMock,
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
    obterConsenso: vi.fn(),
    obterConsensoServidor: vi.fn(),
    salvarConsenso: vi.fn(),
    aprovarConsenso: vi.fn(),
}));

describe('useConsensoDiagnostico', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        setQueryDataMock.mockReset();
        vi.useFakeTimers();
        useQueryMock.mockImplementation((options: any) => ({
            data: mockQueryData,
            status: mockQueryStatus,
            error: mockQueryError,
            executarQuery: options.query,
        }));
        mockQueryStatus.value = 'success';
        mockQueryError.value = null;
        mockQueryData.value = {
            situacaoServidor: 'CONSENSO_CRIADO',
            competencias: [{competenciaCodigo: 10, importancia: 2, dominio: 2}],
            competenciasDetalhadas: [{
                competenciaCodigo: 10,
                autoimportancia: 4,
                autodominio: 3,
                chefiaImportancia: null,
                chefiaDominio: null,
                consensoImportancia: null,
                consensoDominio: null,
            }],
        };
    });

    it('deve consultar o consenso do usuário logado quando o título da rota coincide com o perfil', async () => {
        vi.mocked(diagnosticoService.obterConsenso).mockResolvedValue(mockQueryData.value);
        const scope = effectScope();

        scope.run(() => {
            useConsensoDiagnostico(55, '242426');
        });

        const chamada = useQueryMock.mock.calls[0][0];
        await chamada.query();

        expect(diagnosticoService.obterConsenso).toHaveBeenCalledWith(55);
        expect(diagnosticoService.obterConsensoServidor).not.toHaveBeenCalled();

        scope.stop();
    });

    it('deve consultar o consenso de servidor específico quando a chefia abre outro servidor', async () => {
        vi.mocked(diagnosticoService.obterConsensoServidor).mockResolvedValue(mockQueryData.value);
        const scope = effectScope();

        scope.run(() => {
            useConsensoDiagnostico(56, '242427');
        });

        const chamada = useQueryMock.mock.calls[0][0];
        await chamada.query();

        expect(diagnosticoService.obterConsensoServidor).toHaveBeenCalledWith(56, '242427');
        expect(diagnosticoService.obterConsenso).not.toHaveBeenCalled();

        scope.stop();
    });

    it('deve autopreencher o consenso quando a nota da chefia coincide com a autoavaliação', async () => {
        vi.mocked(diagnosticoService.salvarConsenso).mockResolvedValue();
        const scope = effectScope();
        let composable: ReturnType<typeof useConsensoDiagnostico> | undefined;

        scope.run(() => {
            composable = useConsensoDiagnostico(57, '242427');
        });
        await nextTick();

        composable!.atualizarNotaDetalhada(10, {origem: 'chefia', campo: 'importancia', valor: 4});
        composable!.atualizarNotaDetalhada(10, {origem: 'chefia', campo: 'dominio', valor: 3});

        expect(composable!.competenciasDetalhadasLocais.value[0].consensoImportancia).toBe(4);
        expect(composable!.competenciasDetalhadasLocais.value[0].consensoDominio).toBe(3);
        expect(composable!.competenciasLocais.value[0].importancia).toBe(4);
        expect(composable!.competenciasLocais.value[0].dominio).toBe(3);

        await vi.advanceTimersByTimeAsync(800);
        await Promise.resolve();

        expect(diagnosticoService.salvarConsenso).toHaveBeenCalledWith(57, '242427', expect.objectContaining({
            competenciasDetalhadas: [expect.objectContaining({
                competenciaCodigo: 10,
                consensoImportancia: 4,
                consensoDominio: 3,
            })],
        }));

        scope.stop();
    });

    it('deve disparar autosave ao editar o consenso final', async () => {
        vi.mocked(diagnosticoService.salvarConsenso).mockResolvedValue();
        const scope = effectScope();
        let composable: ReturnType<typeof useConsensoDiagnostico> | undefined;

        scope.run(() => {
            composable = useConsensoDiagnostico(60, '242427');
        });
        await nextTick();

        composable!.atualizarNotaDetalhada(10, {origem: 'consenso', campo: 'dominio', valor: 5});

        expect(composable!.salvandoAutomaticamente.value).toBe(true);
        await vi.advanceTimersByTimeAsync(800);
        await Promise.resolve();

        expect(diagnosticoService.salvarConsenso).toHaveBeenCalledWith(60, '242427', expect.objectContaining({
            competenciasDetalhadas: [expect.objectContaining({
                competenciaCodigo: 10,
                consensoDominio: 5,
            })],
        }));
        expect(setQueryDataMock).toHaveBeenCalledWith(
            ['diagnostico-competencias', 'consenso', '242426', 'sem-perfil', 'sem-unidade', 60, '242427'],
            expect.objectContaining({
                situacaoServidor: 'CONSENSO_CRIADO',
                competenciasDetalhadas: [expect.objectContaining({
                    competenciaCodigo: 10,
                    consensoDominio: 5,
                })],
            }),
        );
        expect(composable!.salvandoAutomaticamente.value).toBe(false);
        scope.stop();
    });

    it('não deve disparar autosave quando o consenso já está aprovado', async () => {
        mockQueryData.value = {
            situacaoServidor: 'CONSENSO_APROVADO',
            competencias: [{competenciaCodigo: 10, importancia: 2, dominio: 2}],
            competenciasDetalhadas: [],
        };
        vi.mocked(diagnosticoService.salvarConsenso).mockResolvedValue();
        const scope = effectScope();
        let composable: ReturnType<typeof useConsensoDiagnostico> | undefined;

        scope.run(() => {
            composable = useConsensoDiagnostico(58, '242427');
        });
        await nextTick();

        composable!.atualizarNota(10, 'importancia', 5);
        await vi.advanceTimersByTimeAsync(900);

        expect(diagnosticoService.salvarConsenso).not.toHaveBeenCalled();

        scope.stop();
    });

    it('deve invalidar consenso, equipe e autoavaliação ao aprovar', async () => {
        vi.mocked(diagnosticoService.aprovarConsenso).mockResolvedValue();
        const scope = effectScope();
        let composable: ReturnType<typeof useConsensoDiagnostico> | undefined;

        scope.run(() => {
            composable = useConsensoDiagnostico(59, '242426');
        });
        await nextTick();

        await composable!.aprovarConsenso();

        expect(diagnosticoService.aprovarConsenso).toHaveBeenCalledWith(59);
        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'consenso', '242426', 'sem-perfil', 'sem-unidade', 59, '242426'],
            exact: true,
        });
        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'equipe', '242426', 'sem-perfil', 'sem-unidade', 59],
            exact: true,
        });
        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'autoavaliacao', '242426', 'sem-perfil', 'sem-unidade', 59],
            exact: true,
        });

        scope.stop();
    });

    it('deve exercitar as opções do useQuery para chave, query e enabled', async () => {
        const scope = effectScope();
        scope.run(() => {
            useConsensoDiagnostico(80, '242426');
        });
        await nextTick();

        const chamada = useQueryMock.mock.calls[0][0];

        // 1. key()
        expect(chamada.key()).toEqual(['diagnostico-competencias', 'consenso', '242426', 'sem-perfil', 'sem-unidade', 80, '242426']);

        // 2. enabled()
        expect(chamada.enabled()).toBe(true);

        scope.stop();
    });

    it('deve disparar autosave e cobrir onSuccess quando competenciasDetalhadasLocais for vazio', async () => {
        mockQueryData.value.competenciasDetalhadas = [];
        mockQueryData.value.competencias = [{competenciaCodigo: 12, importancia: 3, dominio: 2}];
        vi.mocked(diagnosticoService.salvarConsenso).mockResolvedValue();

        const scope = effectScope();
        let composable: ReturnType<typeof useConsensoDiagnostico> | undefined;

        scope.run(() => {
            composable = useConsensoDiagnostico(61, '242426');
        });
        await nextTick();

        composable!.atualizarNota(12, 'importancia', 4);

        expect(composable!.salvandoAutomaticamente.value).toBe(true);
        await vi.advanceTimersByTimeAsync(800);
        await Promise.resolve();

        expect(diagnosticoService.salvarConsenso).toHaveBeenCalledWith(61, '242426', expect.objectContaining({
            competencias: [{competenciaCodigo: 12, importancia: 4, dominio: 2}],
            competenciasDetalhadas: undefined,
        }));
        expect(setQueryDataMock).toHaveBeenCalledWith(
            ['diagnostico-competencias', 'consenso', '242426', 'sem-perfil', 'sem-unidade', 61, '242426'],
            expect.objectContaining({
                competencias: [{competenciaCodigo: 12, importancia: 4, dominio: 2}],
                competenciasDetalhadas: undefined,
            }),
        );

        scope.stop();
    });
});
