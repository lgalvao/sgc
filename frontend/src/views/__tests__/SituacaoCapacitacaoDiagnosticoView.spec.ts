import {describe, expect, it, vi} from 'vitest';
import {ref} from 'vue';
import {mount} from '@vue/test-utils';
import SituacaoCapacitacaoDiagnosticoView from '../SituacaoCapacitacaoDiagnosticoView.vue';

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

const situacoesLocaisVal = ref<any[]>([
    {servidorTitulo: '242426', servidorNome: 'Duff McKagan', competenciaCodigo: 10, situacaoCapacitacao: null},
    {servidorTitulo: '242427', servidorNome: 'Izzy Stradlin', competenciaCodigo: 10, situacaoCapacitacao: 'EC'},
]);

const servidoresVal = ref<any[]>([
    {servidorTitulo: '242426', servidorNome: 'Duff McKagan'},
    {servidorTitulo: '242427', servidorNome: 'Izzy Stradlin'},
]);

const carregandoVal = ref(false);
const salvandoAutomaticamenteVal = ref(false);
const atualizarCapacitacaoMock = vi.fn();

const unidadeVal = ref<any>({
    unidadeSigla: 'ASSESSORIA_12',
    unidadeNome: 'Assessoria 12',
});

vi.mock('@/composables/useSituacaoCapacitacaoDiagnostico', () => ({
    useSituacaoCapacitacaoDiagnostico: () => ({
        situacoesLocais: situacoesLocaisVal,
        unidade: unidadeVal,
        servidores: servidoresVal,
        carregando: carregandoVal,
        salvandoAutomaticamente: salvandoAutomaticamenteVal,
        atualizarCapacitacao: atualizarCapacitacaoMock,
    }),
}));

describe('SituacaoCapacitacaoDiagnosticoView', () => {
    it('simplifica o cabeçalho e apresenta a matriz competência x servidor', () => {
        const wrapper = mount(SituacaoCapacitacaoDiagnosticoView, {
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
        expect(wrapper.find('[data-testid="situacao-242426-10"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="situacao-242427-10"]').exists()).toBe(true);
        expect(wrapper.text()).not.toContain('Concluir diagnóstico');
    });

    it('exercita casos especiais de abreviarNomeServidor e exibirTituloSecundario', () => {
        servidoresVal.value = [
            {servidorTitulo: '1', servidorNome: 'Axl'}, // Uma palavra só
            {servidorTitulo: '2', servidorNome: 'Slash'}, // Outra
            {servidorTitulo: '3', servidorNome: 'ChristopherR Wallace'}, // >= 10 letras no 1 nome + segundo nome
            {servidorTitulo: '4', servidorNome: 'Steven Adler'},
            {servidorTitulo: '5', servidorNome: 'Steven Adler'}, // Mesmo abreviado "Steven A.", forca exibirTituloSecundario
        ];

        const wrapper = mount(SituacaoCapacitacaoDiagnosticoView, {
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

        expect(wrapper.text()).toContain('Axl');
        expect(wrapper.text()).toContain('ChristopherR');
        expect(wrapper.text()).toContain('Steven A.');
        expect(wrapper.find('small.cabecalho-servidor-titulo').exists()).toBe(true);
    });

    it('exercita empty state e update capacitacao', async () => {
        situacoesLocaisVal.value = [];
        const wrapperEmpty = mount(SituacaoCapacitacaoDiagnosticoView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    AppAlert: {template: '<div />'},
                    EmptyState: {template: '<div class="empty-state">Vazio</div>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section><slot /></section>'},
                    BFormSelect: {template: '<select v-bind="$attrs"></select>'},
                    BSpinner: {template: '<span />'},
                },
            },
        });
        expect(wrapperEmpty.find('.empty-state').exists()).toBe(true);

        situacoesLocaisVal.value = [
            {servidorTitulo: '1', servidorNome: 'Axl', competenciaCodigo: 10, situacaoCapacitacao: null},
        ];
        servidoresVal.value = [
            {servidorTitulo: '1', servidorNome: 'Axl'},
        ];
        const wrapperSelect = mount(SituacaoCapacitacaoDiagnosticoView, {
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
                    BFormSelect: {
                        props: ['modelValue', 'options'],
                        emits: ['update:modelValue'],
                        template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><option v-for="opt in options" :key="opt.value" :value="opt.value">{{ opt.text }}</option></select>'
                    },
                    BSpinner: {template: '<span />'},
                },
            },
        });

        const select = wrapperSelect.find('select');
        await select.setValue('AC');
        expect(atualizarCapacitacaoMock).toHaveBeenCalledWith('1', 10, 'AC');
    });

    it('exercita unidade null', () => {
        unidadeVal.value = null; // Para forçar unidade = null pelo mock
        const wrapper = mount(SituacaoCapacitacaoDiagnosticoView, {
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
        expect(wrapper.text()).not.toContain('ASSESSORIA_12');
    });
});
