import {AxiosError} from 'axios';
import {describe, expect, it, vi, beforeEach} from 'vitest';
import {mount} from '@vue/test-utils';
import {computed, ref} from 'vue';
import DiagnosticoEquipePainel from '../DiagnosticoEquipePainel.vue';

const pushMock = vi.fn();
const backMock = vi.fn();
const concluirDiagnosticoMock = vi.fn();

vi.mock('vue-router', () => ({
    useRouter: () => ({
        push: pushMock,
        back: backMock,
    }),
}));

vi.mock('@/composables/useDiagnosticoPermissoes', () => ({
    useDiagnosticoPermissoes: () => ({
        podeCriarConsenso: computed(() => true),
        habilitarConcluirDiagnostico: computed(() => true),
        habilitarValidarDiagnostico: computed(() => false),
        habilitarDevolverDiagnostico: computed(() => false),
        habilitarHomologarDiagnostico: computed(() => false),
    }),
}));

vi.mock('@/composables/useDiagnosticoCache', () => ({
    useCacheDiagnostico: () => ({
        invalidarUnidade: vi.fn(),
    }),
}));

vi.mock('@/services/diagnosticoService', () => ({
    impossibilitarAvaliacao: vi.fn(),
}));

vi.mock('@/composables/useMonitoramentoDiagnostico', () => ({
    useMonitoramentoDiagnostico: () => ({
        unidade: ref({unidadeSigla: 'ASSESSORIA_12', unidadeNome: 'Assessoria 12'}),
        servidores: ref([
            {servidorTitulo: '242426', servidorNome: 'Servidor 1', situacaoServidor: 'CONSENSO_APROVADO'},
        ]),
    }),
}));

vi.mock('@/composables/useFluxoDiagnostico', () => ({
    useFluxoDiagnostico: () => ({
        concluindo: ref(false),
        validando: ref(false),
        devolvendo: ref(false),
        homologando: ref(false),
        erroConcluir: ref(null),
        erroValidar: ref(null),
        erroDevolver: ref(null),
        erroHomologar: ref(null),
        concluirDiagnostico: concluirDiagnosticoMock,
        validarDiagnostico: vi.fn(),
        devolverDiagnostico: vi.fn(),
        homologarDiagnostico: vi.fn(),
    }),
}));

describe('DiagnosticoEquipePainel', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('deve concluir diagnóstico e redirecionar para o painel', async () => {
        concluirDiagnosticoMock.mockResolvedValue(undefined);

        const wrapper = mount(DiagnosticoEquipePainel, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
            },
        });

        await wrapper.get('[data-testid="btn-concluir-diagnostico"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-concluir-diagnostico"]').trigger('click');

        expect(concluirDiagnosticoMock).toHaveBeenCalledTimes(1);
        expect(pushMock).toHaveBeenCalledWith({name: 'Painel'});
    });

    it('deve exibir erro retornado ao falhar conclusão', async () => {
        concluirDiagnosticoMock.mockRejectedValue(new AxiosError(
            'Request failed with status code 422',
            'ERR_BAD_REQUEST',
            undefined,
            undefined,
            {
                status: 422,
                statusText: 'Unprocessable Entity',
                headers: {},
                config: {headers: {} as never},
                data: {message: 'Ainda existem avaliações ou ocupações críticas pendentes.'},
            },
        ));

        const wrapper = mount(DiagnosticoEquipePainel, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
            },
        });

        await wrapper.get('[data-testid="btn-concluir-diagnostico"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-concluir-diagnostico"]').trigger('click');

        expect(wrapper.text()).toContain('Ainda existem avaliações ou ocupações críticas pendentes.');
        expect(pushMock).not.toHaveBeenCalled();
    });
});
