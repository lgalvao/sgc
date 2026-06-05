import {describe, expect, it, vi} from 'vitest';
import {computed, ref} from 'vue';
import {mount} from '@vue/test-utils';
import OcupacoesCriticasDiagnosticoView from '../OcupacoesCriticasDiagnosticoView.vue';

vi.mock('vue-router', () => ({
    useRouter: () => ({
        back: vi.fn(),
    }),
}));

vi.mock('@/composables/useDiagnosticoContexto', () => ({
    useDiagnosticoContexto: () => ({
        data: ref({
            competencias: [
                {competenciaCodigo: 10, descricao: 'Competência A'},
            ],
        }),
    }),
}));

vi.mock('@/composables/useOcupacoesCriticasDiagnostico', () => ({
    useOcupacoesCriticasDiagnostico: () => ({
        ocupacoesLocais: ref([
            {servidorTitulo: '242426', servidorNome: 'Duff McKagan', competenciaCodigo: 10, situacaoCapacitacao: null},
            {servidorTitulo: '242427', servidorNome: 'Izzy Stradlin', competenciaCodigo: 10, situacaoCapacitacao: 'EC'},
        ]),
        unidade: computed(() => ({
            unidadeSigla: 'ASSESSORIA_12',
            unidadeNome: 'Assessoria 12',
            situacaoSubprocesso: 'DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO',
            servidores: [
                {servidorTitulo: '242426', servidorNome: 'Duff McKagan'},
                {servidorTitulo: '242427', servidorNome: 'Izzy Stradlin'},
            ],
        })),
        servidores: computed(() => [
            {servidorTitulo: '242426', servidorNome: 'Duff McKagan'},
            {servidorTitulo: '242427', servidorNome: 'Izzy Stradlin'},
        ]),
        carregando: computed(() => false),
        salvandoAutomaticamente: ref(false),
        pendentes: computed(() => 1),
        atualizarCapacitacao: vi.fn(),
    }),
}));

describe('OcupacoesCriticasDiagnosticoView', () => {
    it('simplifica o cabeçalho e apresenta a matriz competência x servidor', () => {
        const wrapper = mount(OcupacoesCriticasDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    AppAlert: {template: '<div />'},
                    EmptyState: {template: '<div />'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section><slot /></section>'},
                    BFormSelect: {template: '<select v-bind="$attrs"></select>'},
                    BSpinner: {template: '<span />'},
                },
            },
        });

        expect(wrapper.text()).toContain('Situação de Capacitação');
        expect(wrapper.text()).toContain('ASSESSORIA_12');
        expect(wrapper.text()).toContain('Duff M.');
        expect(wrapper.text()).toContain('Izzy S.');
        expect(wrapper.text()).toContain('Competência A');
        expect(wrapper.text()).not.toContain('Assessoria 12');
        expect(wrapper.text()).not.toContain('Existem 1 situações de capacitação sem valor definido.');
        expect(wrapper.text()).not.toContain('242426');
        expect(wrapper.find('.bi-award').exists()).toBe(false);
        expect(wrapper.find('th[title="Duff McKagan"]').exists()).toBe(true);
        expect(wrapper.find('th[title="Izzy Stradlin"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="ocupacao-242426-10"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="ocupacao-242427-10"]').exists()).toBe(true);
        expect(wrapper.text()).not.toContain('Concluir diagnóstico');
    });
});
