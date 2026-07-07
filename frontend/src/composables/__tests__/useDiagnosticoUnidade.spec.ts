import {beforeEach, describe, expect, it, vi} from 'vitest';
import {effectScope, nextTick, ref} from 'vue';
import {useDiagnosticoUnidade} from '../useDiagnosticoUnidade';
import {STALE_TIME_CONTROLADO_POR_INVALIDACAO} from '../cachePolicy';

const mockQueryData = ref<any>(null);
const mockQueryStatus = ref<'pending' | 'success'>('success');
const mockQueryError = ref<Error | null>(null);
const contextoData = ref<any>(null);
const {useQueryMock} = vi.hoisted(() => ({
    useQueryMock: vi.fn(),
}));

vi.mock('@pinia/colada', () => ({
    useQuery: useQueryMock,
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
}));

vi.mock('@/composables/useDiagnosticoContexto', async () => {
    const moduloAtual = await vi.importActual<typeof import('../useDiagnosticoContexto')>('../useDiagnosticoContexto');
    return {
        ...moduloAtual,
        useDiagnosticoContexto: () => ({
            data: contextoData,
        }),
    };
});

describe('useDiagnosticoUnidade', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        useQueryMock.mockImplementation(() => ({
            data: mockQueryData,
            status: mockQueryStatus,
            error: mockQueryError,
        }));
        mockQueryStatus.value = 'success';
        mockQueryError.value = null;
        contextoData.value = null;
        mockQueryData.value = {
            unidade: {
                unidadeSigla: 'ASSESSORIA_12',
                unidadeNome: 'Assessoria 12',
                situacaoSubprocesso: 'DIAGNOSTICO_CONCLUIDO',
            },
            situacaoDiagnostico: 'CONCLUIDO',
            servidores: [
                {servidorTitulo: '242426', situacaoServidor: 'CONSENSO_APROVADO'},
                {servidorTitulo: '242427', situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA'},
                {servidorTitulo: '242428', situacaoServidor: 'AVALIACAO_IMPOSSIBILITADA'},
            ],
            situacoesCapacitacao: [{competenciaCodigo: 10}],
            movimentacoes: [{descricao: 'Teste'}],
        };
    });

    it('expõe unidade, situação e pendências com base na query principal', async () => {
        const scope = effectScope();
        let composable: ReturnType<typeof useDiagnosticoUnidade> | undefined;

        scope.run(() => {
            composable = useDiagnosticoUnidade(55);
        });
        await nextTick();

        expect(composable!.unidade.value?.unidadeSigla).toBe('ASSESSORIA_12');
        expect(composable!.situacao.value).toBe('CONCLUIDO');
        expect(composable!.situacaoSubprocesso.value).toBe('DIAGNOSTICO_CONCLUIDO');
        expect(composable!.situacoesCapacitacao.value).toHaveLength(1);
        expect(composable!.movimentacoes.value).toHaveLength(1);
        expect(composable!.totalPendentes.value).toBe(1);
        expect(useQueryMock).toHaveBeenCalledWith(expect.objectContaining({
            staleTime: STALE_TIME_CONTROLADO_POR_INVALIDACAO,
        }));

        scope.stop();
    });

    it('usa o contexto como fallback da situação e sinaliza carregamento/erro', async () => {
        mockQueryStatus.value = 'pending';
        const erro = new Error('Falha no monitoramento');
        mockQueryError.value = erro;
        contextoData.value = {situacaoDiagnostico: 'VALIDADO'};
        mockQueryData.value = {
            unidade: null,
            servidores: [],
            situacoesCapacitacao: [],
            movimentacoes: [],
            situacaoDiagnostico: null,
        };

        const scope = effectScope();
        let composable: ReturnType<typeof useDiagnosticoUnidade> | undefined;

        scope.run(() => {
            composable = useDiagnosticoUnidade(77);
        });
        await nextTick();

        expect(composable!.carregando.value).toBe(true);
        expect(composable!.erro.value).toBe(erro);
        expect(composable!.situacao.value).toBe('VALIDADO');
        expect(composable!.situacaoSubprocesso.value).toBe('');
        expect(composable!.totalPendentes.value).toBe(0);

        scope.stop();
    });
});
