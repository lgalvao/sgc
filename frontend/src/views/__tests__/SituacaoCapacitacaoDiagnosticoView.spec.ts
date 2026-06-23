import {beforeEach, describe, expect, it, vi} from 'vitest';
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
    {servidorTitulo: '242426', servidorNome: 'João Guilherme de Albuquerque Maranhão', competenciaCodigo: 10, situacaoCapacitacao: null},
    {servidorTitulo: '242427', servidorNome: 'Maria Eduarda Cavalcanti de Alencar', competenciaCodigo: 10, situacaoCapacitacao: 'EC'},
]);

const servidoresVal = ref<any[]>([
    {servidorTitulo: '242426', servidorNome: 'João Guilherme de Albuquerque Maranhão', situacaoServidor: 'CONSENSO_APROVADO'},
    {servidorTitulo: '242427', servidorNome: 'Maria Eduarda Cavalcanti de Alencar', situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA'},
]);

const carregandoVal = ref(false);
const salvandoAutomaticamenteVal = ref(false);
const atualizarCapacitacaoMock = vi.fn();
const habilitarCriarConsensoVal = ref(true);

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
        habilitarCriarConsenso: habilitarCriarConsensoVal,
        atualizarCapacitacao: atualizarCapacitacaoMock,
    }),
}));

describe('SituacaoCapacitacaoDiagnosticoView', () => {
    beforeEach(() => {
        situacoesLocaisVal.value = [
            {servidorTitulo: '242426', servidorNome: 'João Guilherme de Albuquerque Maranhão', competenciaCodigo: 10, situacaoCapacitacao: null},
            {servidorTitulo: '242427', servidorNome: 'Maria Eduarda Cavalcanti de Alencar', competenciaCodigo: 10, situacaoCapacitacao: 'EC'},
        ];
        servidoresVal.value = [
            {servidorTitulo: '242426', servidorNome: 'João Guilherme de Albuquerque Maranhão', situacaoServidor: 'CONSENSO_APROVADO', consenso: [{competenciaCodigo: 10, importancia: 5, dominio: 3}]},
            {servidorTitulo: '242427', servidorNome: 'Maria Eduarda Cavalcanti de Alencar', situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA', consenso: []},
        ];
        carregandoVal.value = false;
        salvandoAutomaticamenteVal.value = false;
        unidadeVal.value = {
            unidadeSigla: 'ASSESSORIA_12',
            unidadeNome: 'Assessoria 12',
        };
        habilitarCriarConsensoVal.value = true;
        atualizarCapacitacaoMock.mockReset();
    });

    it('simplifica o cabeçalho e apresenta competências para o servidor selecionado', async () => {
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
                    BBadge: {template: '<span><slot /></span>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section><slot /></section>'},
                    BListGroup: {template: '<div><slot /></div>'},
                    BListGroupItem: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BFormSelect: {
                        props: ['modelValue', 'options'],
                        template: '<select :value="modelValue" v-bind="$attrs"><option v-for="opt in options" :key="opt.value" :value="opt.value">{{ opt.text }}</option></select>'
                    },
                    BSpinner: {template: '<span />'},
                },
            },
        });

        expect(wrapper.text()).toContain('Situação de Capacitação');
        expect(wrapper.text()).toContain('ASSESSORIA_12');
        expect(wrapper.text()).toContain('João Guilherme de Albuquerque Maranhão');
        expect(wrapper.text()).not.toContain('Competência A');
        await wrapper.find('[data-testid="btn-selecionar-servidor-situacao-capacitacao-242426"]').trigger('click');
        expect(wrapper.text()).toContain('Competência A');
        expect(wrapper.text()).not.toContain('Assessoria 12');
        expect(wrapper.text()).not.toContain('Existem 1 situações de capacitação sem valor definido.');
        expect(wrapper.find('[data-testid="lista-servidores-situacao-capacitacao"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="detalhes-servidor-situacao-capacitacao"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="situacao-242426-10"]').exists()).toBe(true);
        expect(wrapper.text()).not.toContain('Concluir diagnóstico');
    });

    it('troca o servidor selecionado e reaproveita a mesma lista de competências', async () => {
        servidoresVal.value = [
            {servidorTitulo: '1', servidorNome: 'Ana Beatriz de Albuquerque e Souza', situacaoServidor: 'CONSENSO_APROVADO', consenso: []},
            {servidorTitulo: '2', servidorNome: 'Luiz Fernando Cavalcanti de Moura', situacaoServidor: 'CONSENSO_APROVADO', consenso: []},
        ];
        situacoesLocaisVal.value = [
            {servidorTitulo: '1', servidorNome: 'Ana Beatriz de Albuquerque e Souza', competenciaCodigo: 10, situacaoCapacitacao: 'AC'},
            {servidorTitulo: '2', servidorNome: 'Luiz Fernando Cavalcanti de Moura', competenciaCodigo: 10, situacaoCapacitacao: 'I'},
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
                    BBadge: {template: '<span><slot /></span>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section><slot /></section>'},
                    BListGroup: {template: '<div><slot /></div>'},
                    BListGroupItem: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BFormSelect: {
                        props: ['modelValue', 'options'],
                        emits: ['update:modelValue'],
                        template: '<select :value="modelValue" v-bind="$attrs" @change="$emit(\'update:modelValue\', $event.target.value)"><option v-for="opt in options" :key="opt.value" :value="opt.value">{{ opt.text }}</option></select>'
                    },
                    BSpinner: {template: '<span />'},
                },
            },
        });

        expect(wrapper.text()).toContain('Ana Beatriz de Albuquerque e Souza');
        await wrapper.get('[data-testid="btn-selecionar-servidor-situacao-capacitacao-2"]').trigger('click');
        expect(wrapper.text()).toContain('Luiz Fernando Cavalcanti de Moura');
        expect(wrapper.find('[data-testid="situacao-2-10"]').exists()).toBe(true);
    });

    it('exercita empty state e update capacitacao', async () => {
        situacoesLocaisVal.value = [];
        servidoresVal.value = [];
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
                    BBadge: {template: '<span><slot /></span>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section><slot /></section>'},
                    BListGroup: {template: '<div><slot /></div>'},
                    BListGroupItem: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
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
            {servidorTitulo: '1', servidorNome: 'Axl', situacaoServidor: 'CONSENSO_APROVADO', consenso: []},
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
                    BBadge: {template: '<span><slot /></span>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section><slot /></section>'},
                    BListGroup: {template: '<div><slot /></div>'},
                    BListGroupItem: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BFormSelect: {
                        props: ['modelValue', 'options'],
                        emits: ['update:modelValue'],
                        template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><option v-for="opt in options" :key="opt.value" :value="opt.value">{{ opt.text }}</option></select>'
                    },
                    BSpinner: {template: '<span />'},
                },
            },
        });

        await wrapperSelect.find('[data-testid="btn-selecionar-servidor-situacao-capacitacao-1"]').trigger('click');
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
                    BBadge: {template: '<span><slot /></span>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section><slot /></section>'},
                    BListGroup: {template: '<div><slot /></div>'},
                    BListGroupItem: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BFormSelect: {template: '<select v-bind="$attrs"></select>'},
                    BSpinner: {template: '<span />'},
                },
            },
        });
        expect(wrapper.text()).not.toContain('ASSESSORIA_12');
    });

    it('ordena servidores com consenso aprovado no topo', () => {
        servidoresVal.value = [
            {servidorTitulo: '1', servidorNome: 'Servidor B', situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA', consenso: []},
            {servidorTitulo: '2', servidorNome: 'Servidor A', situacaoServidor: 'CONSENSO_APROVADO', consenso: []},
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
                    BBadge: {template: '<span><slot /></span>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section><slot /></section>'},
                    BListGroup: {template: '<div><slot /></div>'},
                    BListGroupItem: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BFormSelect: {template: '<select v-bind="$attrs"></select>'},
                    BSpinner: {template: '<span />'},
                },
            },
        });

        const itens = wrapper.findAll('[data-testid^="btn-selecionar-servidor-situacao-capacitacao-"]');
        expect(itens[0].text()).toContain('Servidor A');
        expect(itens[1].text()).toContain('Servidor B');
    });

    it('exibe EmptyState informativo se o servidor selecionado não tiver consenso aprovado', async () => {
        servidoresVal.value = [
            {servidorTitulo: '1', servidorNome: 'Servidor A', situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA', consenso: []},
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
                    EmptyState: {
                        props: ['title', 'description'],
                        template: '<div class="empty-state"><h3>{{ title }}</h3><p>{{ description }}</p></div>'
                    },
                    BBadge: {template: '<span><slot /></span>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section><slot /></section>'},
                    BListGroup: {template: '<div><slot /></div>'},
                    BListGroupItem: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BFormSelect: {template: '<select v-bind="$attrs"></select>'},
                    BSpinner: {template: '<span />'},
                },
            },
        });

        await wrapper.find('[data-testid="btn-selecionar-servidor-situacao-capacitacao-1"]').trigger('click');

        const emptyState = wrapper.find('.empty-state');
        expect(emptyState.exists()).toBe(true);
        expect(emptyState.text()).toContain('Aguardando aprovação de consenso');
        expect(emptyState.text()).toContain('A situação de capacitação só pode ser preenchida após o servidor aprovar a avaliação de consenso.');
        expect(wrapper.find('table').exists()).toBe(false);
    });

    it('formata NA na grade de competências quando importância e domínio forem zero', async () => {
        servidoresVal.value = [
            {
                servidorTitulo: '1',
                servidorNome: 'Servidor A',
                situacaoServidor: 'CONSENSO_APROVADO',
                consenso: [{competenciaCodigo: 10, importancia: 0, dominio: 0}],
            },
        ];
        situacoesLocaisVal.value = [
            {servidorTitulo: '1', servidorNome: 'Servidor A', competenciaCodigo: 10, situacaoCapacitacao: 'NA'},
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
                    BBadge: {template: '<span><slot /></span>'},
                    BButton: {template: '<button v-bind="$attrs"><slot /></button>'},
                    BCard: {template: '<section><slot /></section>'},
                    BListGroup: {template: '<div><slot /></div>'},
                    BListGroupItem: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BFormSelect: {template: '<select v-bind="$attrs"></select>'},
                    BSpinner: {template: '<span />'},
                },
            },
        });

        await wrapper.find('[data-testid="btn-selecionar-servidor-situacao-capacitacao-1"]').trigger('click');

        expect(wrapper.text()).toContain('NA');
    });
});
