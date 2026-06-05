import {describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {computed, ref} from 'vue';
import MonitoramentoDiagnosticoView from '../MonitoramentoDiagnosticoView.vue';

const pushMock = vi.fn();
const backMock = vi.fn();

vi.mock('vue-router', () => ({
    useRouter: () => ({
        push: pushMock,
        back: backMock,
    }),
}));

vi.mock('@/composables/useDiagnosticoPermissoes', () => ({
    useDiagnosticoPermissoes: () => ({
        podeCriarConsenso: computed(() => true),
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
            {servidorTitulo: '242426', servidorNome: 'Servidor 1', situacaoServidor: 'AUTOAVALIACAO_NAO_INICIADA'},
        ]),
        ocupacoesCriticas: ref([{cargoNome: 'Cargo crítico'}]),
        movimentacoes: ref([
            {descricao: 'Movimentação teste', unidadeOrigem: 'A', unidadeDestino: 'B', dataHora: '2026-06-05 10:00'},
        ]),
        carregando: ref(false),
        situacao: ref('EM_ANDAMENTO'),
        totalPendentes: ref(1),
    }),
}));

vi.mock('@/composables/useFluxoDiagnostico', () => ({
    useFluxoDiagnostico: () => ({
        validando: ref(false),
        devolvendo: ref(false),
        homologando: ref(false),
        erroValidar: ref(null),
        erroDevolver: ref(null),
        erroHomologar: ref(null),
        validarDiagnostico: vi.fn(),
        devolverDiagnostico: vi.fn(),
        homologarDiagnostico: vi.fn(),
    }),
}));

describe('MonitoramentoDiagnosticoView', () => {
    it('simplifica o cabeçalho e não renderiza cards numéricos nem bloco de movimentações', () => {
        const wrapper = mount(MonitoramentoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    DiagnosticoEquipePainel: {template: '<div data-testid="diagnostico-equipe-painel" />'},
                },
            },
        });

        expect(wrapper.find('[data-testid="diagnostico-equipe-painel"]').exists()).toBe(true);
    });
});
