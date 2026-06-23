import {beforeEach, describe, expect, it, vi} from 'vitest';
import {ref} from 'vue';
import * as diagnosticoService from '@/services/diagnosticoService';
import {useFluxoDiagnostico} from '../useFluxoDiagnostico';

const invalidateQueriesMock = vi.fn();
const pushMock = vi.fn();
const invalidarSubprocessoMock = vi.fn();

vi.mock('@pinia/colada', () => ({
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
            }
        });

        return {
            isLoading,
            error,
            mutateAsync,
        };
    }),
}));

vi.mock('vue-router', () => ({
    useRouter: () => ({
        push: pushMock,
    }),
}));

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({
        usuarioCodigo: '151515',
        perfilSelecionado: 'CHEFE',
        unidadeSelecionada: 12,
    }),
}));

vi.mock('@/stores/subprocesso', () => ({
    useSubprocessoStore: () => ({
        invalidar: invalidarSubprocessoMock,
    }),
}));

vi.mock('@/services/diagnosticoService', () => ({
    concluirDiagnostico: vi.fn(),
    validarConclusaoDiagnostico: vi.fn(),
    validarDiagnostico: vi.fn(),
    validarAcaoValidarDiagnostico: vi.fn(),
    devolverDiagnostico: vi.fn(),
    validarAcaoDevolverDiagnostico: vi.fn(),
    homologarDiagnostico: vi.fn(),
    validarAcaoHomologarDiagnostico: vi.fn(),
    impossibilitarAvaliacao: vi.fn(),
}));

describe('useFluxoDiagnostico', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('deve invalidar contexto, equipe e unidade ao concluir diagnóstico', async () => {
        vi.mocked(diagnosticoService.concluirDiagnostico).mockResolvedValue();
        const composable = useFluxoDiagnostico(41);

        await composable.concluirDiagnostico();

        expect(diagnosticoService.concluirDiagnostico).toHaveBeenCalledWith(41);
        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'contexto', '151515', 'CHEFE', '12', 41],
            exact: true,
        });
        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'equipe', '151515', 'CHEFE', '12', 41],
            exact: true,
        });
        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'unidade', '151515', 'CHEFE', '12', 41],
            exact: true,
        });
        expect(invalidarSubprocessoMock).toHaveBeenCalled();
    });

    it('deve enviar payload correto ao validar, devolver e homologar', async () => {
        vi.mocked(diagnosticoService.validarAcaoValidarDiagnostico).mockResolvedValue();
        vi.mocked(diagnosticoService.validarDiagnostico).mockResolvedValue();
        vi.mocked(diagnosticoService.validarAcaoDevolverDiagnostico).mockResolvedValue();
        vi.mocked(diagnosticoService.devolverDiagnostico).mockResolvedValue();
        vi.mocked(diagnosticoService.validarAcaoHomologarDiagnostico).mockResolvedValue();
        vi.mocked(diagnosticoService.homologarDiagnostico).mockResolvedValue();
        const composable = useFluxoDiagnostico(42);

        await composable.validarAcaoValidarDiagnostico();
        await composable.validarDiagnostico('Observação');
        await composable.validarAcaoDevolverDiagnostico();
        await composable.devolverDiagnostico('Justificativa');
        await composable.validarAcaoHomologarDiagnostico();
        await composable.homologarDiagnostico('Homologado');

        expect(diagnosticoService.validarAcaoValidarDiagnostico).toHaveBeenCalledWith(42);
        expect(diagnosticoService.validarDiagnostico).toHaveBeenCalledWith(42, {texto: 'Observação'});
        expect(diagnosticoService.validarAcaoDevolverDiagnostico).toHaveBeenCalledWith(42);
        expect(diagnosticoService.devolverDiagnostico).toHaveBeenCalledWith(42, {justificativa: 'Justificativa'});
        expect(diagnosticoService.validarAcaoHomologarDiagnostico).toHaveBeenCalledWith(42);
        expect(diagnosticoService.homologarDiagnostico).toHaveBeenCalledWith(42, {texto: 'Homologado'});
    });

    it('deve impossibilitar avaliação e invalidar equipe e unidade', async () => {
        vi.mocked(diagnosticoService.impossibilitarAvaliacao).mockResolvedValue();
        const composable = useFluxoDiagnostico(44);

        await composable.impossibilitarAvaliacao('242426', 'Servidor afastado.');

        expect(diagnosticoService.impossibilitarAvaliacao).toHaveBeenCalledWith(44, '242426', {
            justificativa: 'Servidor afastado.',
        });
        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'equipe', '151515', 'CHEFE', '12', 44],
            exact: true,
        });
        expect(invalidateQueriesMock).toHaveBeenCalledWith({
            key: ['diagnostico-competencias', 'unidade', '151515', 'CHEFE', '12', 44],
            exact: true,
        });
    });

    it('deve navegar de volta para o subprocesso', () => {
        const composable = useFluxoDiagnostico(43);

        composable.voltarParaSubprocesso(100, 'ASSESSORIA_12');

        expect(pushMock).toHaveBeenCalledWith({
            name: 'Subprocesso',
            params: {codProcesso: 100, siglaUnidade: 'ASSESSORIA_12'},
        });
    });

    it('deve avaliar propriedades computadas de estado', () => {
        const composable = useFluxoDiagnostico(41);
        expect(composable.concluindo.value).toBe(false);
        expect(composable.validando.value).toBe(false);
        expect(composable.devolvendo.value).toBe(false);
        expect(composable.homologando.value).toBe(false);
        expect(composable.impossibilitando.value).toBe(false);
        expect(composable.erroConcluir.value).toBeNull();
        expect(composable.erroValidacaoConcluir.value).toBeNull();
        expect(composable.erroValidar.value).toBeNull();
        expect(composable.erroValidacaoValidar.value).toBeNull();
        expect(composable.erroDevolver.value).toBeNull();
        expect(composable.erroValidacaoDevolver.value).toBeNull();
        expect(composable.erroHomologar.value).toBeNull();
        expect(composable.erroValidacaoHomologar.value).toBeNull();
        expect(composable.erroImpossibilitar.value).toBeNull();
    });
});
