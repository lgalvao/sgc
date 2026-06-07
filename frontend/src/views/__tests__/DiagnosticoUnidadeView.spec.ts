import {beforeEach, describe, expect, it, vi} from 'vitest';
import {computed, ref} from 'vue';
import {mount} from '@vue/test-utils';
import DiagnosticoUnidadeView from '../DiagnosticoUnidadeView.vue';
import {Perfil} from '@/types/comum';

const backMock = vi.fn();
const validarDiagnosticoMock = vi.fn();
const devolverDiagnosticoMock = vi.fn();
const homologarDiagnosticoMock = vi.fn();

const perfilSelecionado = ref<Perfil>(Perfil.GESTOR);
const situacaoDiagnostico = ref<'EM_ANDAMENTO' | 'CONCLUIDO' | 'VALIDADO' | 'HOMOLOGADO'>('CONCLUIDO');
const erroValidar = ref<Error | null>(null);
const erroDevolver = ref<Error | null>(null);
const erroHomologar = ref<Error | null>(null);

vi.mock('vue-router', () => ({
    useRouter: () => ({
        back: backMock,
    }),
}));

vi.mock('@/stores/perfil', () => ({
    usePerfilStore: () => ({
        perfilSelecionado: perfilSelecionado.value,
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

const unidadeVal = ref<any>({
    unidadeSigla: 'ASSESSORIA_12',
    unidadeNome: 'Assessoria 12',
    situacaoSubprocesso: 'DIAGNOSTICO_CONCLUIDO',
});
const servidoresVal = ref<any[]>([
    {
        servidorTitulo: '242426',
        servidorNome: 'Duff McKagan',
        situacaoServidor: 'CONSENSO_APROVADO',
        consenso: [
            {competenciaCodigo: 10, importancia: 4, dominio: 2},
            {competenciaCodigo: 11, importancia: null, dominio: null},
        ],
    },
]);
const ocupacoesCriticasVal = ref<any[]>([
    {servidorTitulo: '242426', competenciaCodigo: 10, situacaoCapacitacao: 'EC'},
]);

vi.mock('@/composables/useMonitoramentoDiagnostico', () => ({
    useMonitoramentoDiagnostico: () => ({
        unidade: unidadeVal,
        servidores: servidoresVal,
        ocupacoesCriticas: ocupacoesCriticasVal,
        movimentacoes: ref([
            {descricao: 'Diagnóstico concluído', unidadeOrigem: 'A', unidadeDestino: 'B', dataHora: '2026-06-06 09:00'},
        ]),
        carregando: ref(false),
        situacao: situacaoDiagnostico,
        totalPendentes: ref(1),
    }),
}));

vi.mock('@/composables/useFluxoDiagnostico', () => ({
    useFluxoDiagnostico: () => ({
        validando: ref(false),
        devolvendo: ref(false),
        homologando: ref(false),
        erroValidar,
        erroDevolver,
        erroHomologar,
        validarDiagnostico: validarDiagnosticoMock,
        devolverDiagnostico: devolverDiagnosticoMock,
        homologarDiagnostico: homologarDiagnosticoMock,
    }),
}));

describe('DiagnosticoUnidadeView', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        perfilSelecionado.value = Perfil.GESTOR;
        situacaoDiagnostico.value = 'CONCLUIDO';
        erroValidar.value = null;
        erroDevolver.value = null;
        erroHomologar.value = null;
        unidadeVal.value = {
            unidadeSigla: 'ASSESSORIA_12',
            unidadeNome: 'Assessoria 12',
            situacaoSubprocesso: 'DIAGNOSTICO_CONCLUIDO',
        };
        servidoresVal.value = [
            {
                servidorTitulo: '242426',
                servidorNome: 'Duff McKagan',
                situacaoServidor: 'CONSENSO_APROVADO',
                consenso: [
                    {competenciaCodigo: 10, importancia: 4, dominio: 2},
                    {competenciaCodigo: 11, importancia: null, dominio: null},
                ],
            },
        ];
    });

    function montar() {
        return mount(DiagnosticoUnidadeView, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
            },
            global: {
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    AppAlert: {
                        props: ['mensagem'],
                        template: '<div class="app-alert">{{ mensagem }}</div>',
                    },
                    EmptyState: {template: '<div data-testid="empty-state" />'},
                    BButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BBadge: {template: '<span><slot /></span>'},
                    BCard: {template: '<section><slot /></section>'},
                    BCardHeader: {template: '<header><slot /></header>'},
                    BRow: {template: '<div><slot /></div>'},
                    BCol: {template: '<div><slot /></div>'},
                    BAccordion: {template: '<div><slot /></div>'},
                    BAccordionItem: {template: '<div><slot name="title" /><slot /></div>'},
                    BListGroup: {template: '<div><slot /></div>'},
                    BListGroupItem: {template: '<div><slot /></div>'},
                    BSpinner: {template: '<span />'},
                    BFormText: {template: '<small><slot /></small>'},
                    BFormTextarea: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
                    },
                    BModal: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: '<div v-if="modelValue"><slot /><slot name="footer" /></div>',
                    },
                    BTable: {
                        props: ['items'],
                        template: `
                          <div>
                            <div v-for="item in items" :key="item.competenciaCodigo ?? item.servidorTitulo">
                              <slot name="cell(importancia)" :item="item" />
                              <slot name="cell(dominio)" :item="item" />
                              <slot name="cell(gap)" :item="item" />
                              <slot name="cell(situacaoCapacitacao)" :item="item" />
                            </div>
                          </div>
                        `,
                    },
                },
            },
        });
    }

    it('renderiza métricas, gaps, ocupações e histórico do diagnóstico', () => {
        const wrapper = montar();

        expect(wrapper.text()).toContain('ASSESSORIA_12');
        expect(wrapper.text()).toContain('Assessoria 12');
        expect(wrapper.text()).toContain('Servidores');
        expect(wrapper.text()).toContain('Pendentes');
        expect(wrapper.text()).toContain('Ocupações Críticas');
        expect(wrapper.text()).toContain('+2');
        expect(wrapper.text()).toContain('---');
        expect(wrapper.text()).toContain('Em capacitação');
        expect(wrapper.text()).toContain('Diagnóstico concluído');
        expect(wrapper.find('[data-testid="btn-validar-diagnostico-unidade"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-devolver-diagnostico-unidade"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-homologar-diagnostico-unidade"]').exists()).toBe(false);
    });

    it('valida, devolve com justificativa obrigatória e homologa como admin', async () => {
        validarDiagnosticoMock.mockResolvedValue(undefined);
        devolverDiagnosticoMock.mockResolvedValue(undefined);
        homologarDiagnosticoMock.mockResolvedValue(undefined);
        perfilSelecionado.value = Perfil.ADMIN;
        situacaoDiagnostico.value = 'VALIDADO';

        const wrapper = montar();

        await wrapper.get('[data-testid="btn-homologar-diagnostico-unidade"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-homologar-unidade"]').trigger('click');
        expect(homologarDiagnosticoMock).toHaveBeenCalledWith(undefined);
        expect(wrapper.text()).toContain('Diagnóstico homologado');

        situacaoDiagnostico.value = 'CONCLUIDO';
        const wrapperConcluido = montar();
        await wrapperConcluido.get('[data-testid="btn-validar-diagnostico-unidade"]').trigger('click');
        let textarea = wrapperConcluido.get('textarea');
        await textarea.setValue('Observações');
        await wrapperConcluido.get('[data-testid="btn-confirmar-validar-unidade"]').trigger('click');
        expect(validarDiagnosticoMock).toHaveBeenCalledWith('Observações');

        await wrapperConcluido.get('[data-testid="btn-devolver-diagnostico-unidade"]').trigger('click');
        await wrapperConcluido.get('[data-testid="btn-confirmar-devolver-unidade"]').trigger('click');
        expect(wrapperConcluido.text()).toContain('A justificativa é obrigatória.');

        textarea = wrapperConcluido.get('textarea');
        await textarea.setValue('Ajustar consenso');
        await wrapperConcluido.get('[data-testid="btn-confirmar-devolver-unidade"]').trigger('click');
        expect(devolverDiagnosticoMock).toHaveBeenCalledWith('Ajustar consenso');
    });

    it('exibe erro quando validar falha', async () => {
        erroValidar.value = new Error('Falha ao validar');
        validarDiagnosticoMock.mockRejectedValue(erroValidar.value);

        const wrapper = montar();
        await wrapper.get('[data-testid="btn-validar-diagnostico-unidade"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-validar-unidade"]').trigger('click');

        expect(wrapper.text()).toContain('Falha ao validar');
    });

    it('exibe erro quando devolver falha', async () => {
        erroDevolver.value = new Error('Falha ao devolver');
        devolverDiagnosticoMock.mockRejectedValue(erroDevolver.value);

        const wrapper = montar();
        await wrapper.get('[data-testid="btn-devolver-diagnostico-unidade"]').trigger('click');
        await wrapper.get('textarea').setValue('Justificativa');
        await wrapper.get('[data-testid="btn-confirmar-devolver-unidade"]').trigger('click');

        expect(wrapper.text()).toContain('Falha ao devolver');
    });

    it('exibe erro quando homologar falha', async () => {
        perfilSelecionado.value = Perfil.ADMIN;
        situacaoDiagnostico.value = 'VALIDADO';
        erroHomologar.value = new Error('Falha ao homologar');
        homologarDiagnosticoMock.mockRejectedValue(erroHomologar.value);

        const wrapper = montar();
        await wrapper.get('[data-testid="btn-homologar-diagnostico-unidade"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-homologar-unidade"]').trigger('click');

        expect(wrapper.text()).toContain('Falha ao homologar');
    });

    it('cobre ramos default e todos os status de variantes em DiagnosticoUnidadeView', () => {
        situacaoDiagnostico.value = 'OUTRO_STATUS' as any;
        servidoresVal.value[0].situacaoServidor = 'OUTRO_STATUS' as any;
        const wrapper = montar();
        expect(wrapper.text()).toContain('ASSESSORIA_12');

        // Outros status de situacao do diagnostico
        situacaoDiagnostico.value = 'HOMOLOGADO';
        const wrapperHomologado = montar();
        expect(wrapperHomologado.text()).toContain('HOMOLOGADO');

        situacaoDiagnostico.value = 'VALIDADO';
        const wrapperValidado = montar();
        expect(wrapperValidado.text()).toContain('VALIDADO');

        // Status de servidores
        servidoresVal.value = [
            { servidorTitulo: '1', servidorNome: 'A', situacaoServidor: 'AUTOAVALIACAO_NAO_INICIADA', consenso: [] },
            { servidorTitulo: '2', servidorNome: 'B', situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA', consenso: [] },
            { servidorTitulo: '3', servidorNome: 'C', situacaoServidor: 'CONSENSO_CRIADO', consenso: [] },
            { servidorTitulo: '4', servidorNome: 'D', situacaoServidor: 'AVALIACAO_IMPOSSIBILITADA', consenso: [] },
        ];
        const wrapperServidores = montar();
        expect(wrapperServidores.text()).toContain('Autoavaliação não iniciada');
        expect(wrapperServidores.text()).toContain('Autoavaliação concluída');
        expect(wrapperServidores.text()).toContain('Avaliação de consenso criada');
        expect(wrapperServidores.text()).toContain('Avaliação impossibilitada');
    });

    it('exercita todas as variantes de capacitacao, gap e formatacao de notas', () => {
        unidadeVal.value = {
            unidadeSigla: 'ASSESSORIA_12',
            unidadeNome: 'Assessoria 12',
            situacaoSubprocesso: 'DIAGNOSTICO_CONCLUIDO',
        };
        servidoresVal.value = [
            {
                servidorTitulo: '242426',
                servidorNome: 'Duff McKagan',
                situacaoServidor: 'CONSENSO_APROVADO',
                consenso: [
                    {competenciaCodigo: 10, importancia: 3, dominio: 3},
                    {competenciaCodigo: 11, importancia: 2, dominio: 4},
                    {competenciaCodigo: 12, importancia: 0, dominio: 2},
                    {competenciaCodigo: 13, importancia: 4, dominio: 0},
                ],
            },
        ];
        ocupacoesCriticasVal.value = [
            {servidorTitulo: '1', competenciaCodigo: 10, situacaoCapacitacao: 'NA'},
            {servidorTitulo: '2', competenciaCodigo: 10, situacaoCapacitacao: 'AC'},
            {servidorTitulo: '3', competenciaCodigo: 10, situacaoCapacitacao: 'EC'},
            {servidorTitulo: '4', competenciaCodigo: 10, situacaoCapacitacao: 'C'},
            {servidorTitulo: '5', competenciaCodigo: 10, situacaoCapacitacao: 'I'},
            {servidorTitulo: '6', competenciaCodigo: 10, situacaoCapacitacao: null},
            {servidorTitulo: '7', competenciaCodigo: 10, situacaoCapacitacao: 'OUTRO_VALOR' as any},
        ];

        const wrapper = mount(DiagnosticoUnidadeView, {
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
                    BButton: {template: '<button><slot /></button>'},
                    BBadge: {template: '<span><slot /></span>'},
                    BCard: {template: '<section><slot /></section>'},
                    BCardHeader: {template: '<header><slot /></header>'},
                    BRow: {template: '<div><slot /></div>'},
                    BCol: {template: '<div><slot /></div>'},
                    BAccordion: {template: '<div><slot /></div>'},
                    BAccordionItem: {template: '<div><slot name="title" /><slot /></div>'},
                    BListGroup: {template: '<div><slot /></div>'},
                    BListGroupItem: {template: '<div><slot /></div>'},
                    BSpinner: {template: '<span />'},
                    BFormText: {template: '<small><slot /></small>'},
                    BFormTextarea: {template: '<textarea />'},
                    BModal: {template: '<div />'},
                    BTable: {
                        props: ['items', 'fields'],
                        template: `
                          <div>
                            <div v-for="item in items" :key="item.competenciaCodigo ?? item.servidorTitulo">
                              <slot name="cell(gap)" :item="item" />
                              <slot name="cell(importancia)" :item="item" />
                              <slot name="cell(dominio)" :item="item" />
                              <slot name="cell(situacaoCapacitacao)" :item="item" />
                            </div>
                          </div>
                        `,
                    },
                },
            },
        });

        expect(wrapper.text()).toContain('0');
        expect(wrapper.text()).not.toContain('+0');
        expect(wrapper.text()).toContain('-2');
        expect(wrapper.text()).toContain('-');
        expect(wrapper.text()).toContain('Em capacitação');
        expect(wrapper.text()).toContain('Não se aplica');
        expect(wrapper.text()).toContain('A capacitar');
        expect(wrapper.text()).toContain('Capacitado');
        expect(wrapper.text()).toContain('Instrutor');
    });

    it('exercita tratamento de erros sem mensagem nos modais de DiagnosticoUnidadeView', async () => {
        erroValidar.value = {} as any;
        erroDevolver.value = {} as any;
        erroHomologar.value = {} as any;
        validarDiagnosticoMock.mockRejectedValue(erroValidar.value);
        devolverDiagnosticoMock.mockRejectedValue(erroDevolver.value);
        homologarDiagnosticoMock.mockRejectedValue(erroHomologar.value);

        const wrapper = montar();

        // 1. Validar falha
        await wrapper.get('[data-testid="btn-validar-diagnostico-unidade"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-validar-unidade"]').trigger('click');
        expect(wrapper.text()).toContain('Não foi possível salvar. Tente novamente.');

        // 2. Devolver falha
        await wrapper.get('[data-testid="btn-devolver-diagnostico-unidade"]').trigger('click');
        await wrapper.get('textarea').setValue('Justificativa');
        await wrapper.get('[data-testid="btn-confirmar-devolver-unidade"]').trigger('click');
        expect(wrapper.text()).toContain('Não foi possível salvar. Tente novamente.');

        // 3. Homologar falha (precisa de perfil admin e situacao validado)
        perfilSelecionado.value = Perfil.ADMIN;
        situacaoDiagnostico.value = 'VALIDADO';
        const wrapperAdmin = montar();
        await wrapperAdmin.get('[data-testid="btn-homologar-diagnostico-unidade"]').trigger('click');
        await wrapperAdmin.get('[data-testid="btn-confirmar-homologar-unidade"]').trigger('click');
        expect(wrapperAdmin.text()).toContain('Não foi possível salvar. Tente novamente.');
    });
});
