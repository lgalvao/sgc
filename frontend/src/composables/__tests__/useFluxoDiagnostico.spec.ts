import {beforeEach, describe, expect, it, vi} from 'vitest';
import {ref} from 'vue';
import * as diagnosticoService from '@/services/diagnosticoService';
import {useFluxoDiagnostico} from '../useFluxoDiagnostico';

const invalidateQueriesMock = vi.fn();
const pushMock = vi.fn();

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

vi.mock('@/services/diagnosticoService', () => ({
    concluirDiagnostico: vi.fn(),
    validarDiagnostico: vi.fn(),
    devolverDiagnostico: vi.fn(),
    homologarDiagnostico: vi.fn(),
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
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ['diagnostico-competencias', 'contexto', 41]});
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ['diagnostico-competencias', 'equipe', 41]});
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ['diagnostico-competencias', 'unidade', 41]});
    });

    it('deve enviar payload correto ao validar, devolver e homologar', async () => {
        vi.mocked(diagnosticoService.validarDiagnostico).mockResolvedValue();
        vi.mocked(diagnosticoService.devolverDiagnostico).mockResolvedValue();
        vi.mocked(diagnosticoService.homologarDiagnostico).mockResolvedValue();
        const composable = useFluxoDiagnostico(42);

        await composable.validarDiagnostico('Observação');
        await composable.devolverDiagnostico('Justificativa');
        await composable.homologarDiagnostico('Homologado');

        expect(diagnosticoService.validarDiagnostico).toHaveBeenCalledWith(42, {texto: 'Observação'});
        expect(diagnosticoService.devolverDiagnostico).toHaveBeenCalledWith(42, {justificativa: 'Justificativa'});
        expect(diagnosticoService.homologarDiagnostico).toHaveBeenCalledWith(42, {texto: 'Homologado'});
    });

    it('deve navegar de volta para o subprocesso', () => {
        const composable = useFluxoDiagnostico(43);

        composable.voltarParaSubprocesso(100, 'ASSESSORIA_12');

        expect(pushMock).toHaveBeenCalledWith({
            name: 'Subprocesso',
            params: {codProcesso: 100, siglaUnidade: 'ASSESSORIA_12'},
        });
    });
});
