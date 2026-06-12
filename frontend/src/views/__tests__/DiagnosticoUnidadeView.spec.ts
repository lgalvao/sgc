import {beforeEach, describe, expect, it, vi} from 'vitest';
import {computed, ref} from 'vue';
import {mount} from '@vue/test-utils';
import DiagnosticoUnidadeView from '../DiagnosticoUnidadeView.vue';
import {Perfil} from '@/types/comum';

const backMock = vi.fn();
const validarDiagnosticoMock = vi.fn();
const devolverDiagnosticoMock = vi.fn();
const homologarDiagnosticoMock = vi.fn();
const COD_SUBPROCESSO = 400;
const SIGLA_UNIDADE = 'ASSESSORIA_12';
const NOME_UNIDADE = 'Assessoria 12';
const TITULO_SERVIDOR = '242426';
const NOME_SERVIDOR = 'Duff McKagan';
const CODIGO_COMPETENCIA_BASE = 10;

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

const contextoVal = ref<any>({
    competencias: [
        {competenciaCodigo: CODIGO_COMPETENCIA_BASE, descricao: 'Competência A'},
    ],
});

vi.mock('@/composables/useDiagnosticoContexto', () => ({
    useDiagnosticoContexto: () => ({
        data: contextoVal,
    }),
}));

const unidadeVal = ref<any>({
    unidadeSigla: SIGLA_UNIDADE,
    unidadeNome: NOME_UNIDADE,
    situacaoSubprocesso: 'DIAGNOSTICO_CONCLUIDO',
});
const servidoresVal = ref<any[]>([
    {
        servidorTitulo: TITULO_SERVIDOR,
        servidorNome: NOME_SERVIDOR,
        situacaoServidor: 'CONSENSO_APROVADO',
        consenso: [
            {competenciaCodigo: CODIGO_COMPETENCIA_BASE, importancia: 4, dominio: 2},
            {competenciaCodigo: CODIGO_COMPETENCIA_BASE + 1, importancia: null, dominio: null},
        ],
    },
]);
const situacoesCapacitacaoVal = ref<any[]>([
    {servidorTitulo: TITULO_SERVIDOR, competenciaCodigo: CODIGO_COMPETENCIA_BASE, situacaoCapacitacao: 'EC'},
]);

vi.mock('@/composables/useMonitoramentoDiagnostico', () => ({
    useMonitoramentoDiagnostico: () => ({
        unidade: unidadeVal,
        servidores: servidoresVal,
        situacoesCapacitacao: situacoesCapacitacaoVal,
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

vi.mock('@/composables/useDiagnosticoPermissoes', () => ({
    useDiagnosticoPermissoes: () => ({
        habilitarValidarDiagnostico: computed(() => situacaoDiagnostico.value === 'CONCLUIDO'),
        habilitarDevolverDiagnostico: computed(() => situacaoDiagnostico.value === 'CONCLUIDO'),
        habilitarHomologarDiagnostico: computed(() => perfilSelecionado.value === Perfil.ADMIN && situacaoDiagnostico.value === 'VALIDADO'),
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
            unidadeSigla: SIGLA_UNIDADE,
            unidadeNome: NOME_UNIDADE,
            situacaoSubprocesso: 'DIAGNOSTICO_CONCLUIDO',
        };
        contextoVal.value = {
            competencias: [
                {competenciaCodigo: CODIGO_COMPETENCIA_BASE, descricao: 'Competência A'},
            ],
        };
        servidoresVal.value = [
            {
                servidorTitulo: TITULO_SERVIDOR,
                servidorNome: NOME_SERVIDOR,
                situacaoServidor: 'CONSENSO_APROVADO',
                consenso: [
                    {competenciaCodigo: CODIGO_COMPETENCIA_BASE, importancia: 4, dominio: 2},
                    {competenciaCodigo: CODIGO_COMPETENCIA_BASE + 1, importancia: null, dominio: null},
                ],
            },
        ];
    });

    function montar() {
        return mount(DiagnosticoUnidadeView, {
            props: {
                codSubprocesso: COD_SUBPROCESSO,
                siglaUnidade: SIGLA_UNIDADE,
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
                    BDropdown: {template: '<div><slot /></div>'},
                    BDropdownItemButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
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
                    HistoricoAnaliseModal: {
                        props: ['mostrar'],
                        template: '<div v-if="mostrar" data-testid="modal-historico-analise" />',
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

    it('renderiza métricas, gaps, situações de capacitação e histórico do diagnóstico', () => {
        const wrapper = montar();

        expect(wrapper.text()).toContain(SIGLA_UNIDADE);
        expect(wrapper.text()).toContain(NOME_UNIDADE);
        expect(wrapper.text()).toContain('Servidores');
        expect(wrapper.text()).toContain('Pendentes');
        expect(wrapper.text()).toContain('Situações de Capacitação');
        expect(wrapper.text()).toContain('+2');
        expect(wrapper.text()).toContain('---');
        expect(wrapper.text()).toContain('EC');
        expect(wrapper.text()).toContain('Diagnóstico concluído');
        expect(wrapper.find('[data-testid="btn-historico-analise-unidade"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="matriz-diagnostico-unidade"]').exists()).toBe(true);
        expect(wrapper.text()).toContain('I');
        expect(wrapper.text()).toContain('D');
        expect(wrapper.text()).toContain('C');
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
            unidadeSigla: SIGLA_UNIDADE,
            unidadeNome: NOME_UNIDADE,
            situacaoSubprocesso: 'DIAGNOSTICO_CONCLUIDO',
        };
        contextoVal.value = {
            competencias: [
                {competenciaCodigo: CODIGO_COMPETENCIA_BASE, descricao: 'Competência A'},
                {competenciaCodigo: CODIGO_COMPETENCIA_BASE + 1, descricao: 'Competência B'},
                {competenciaCodigo: CODIGO_COMPETENCIA_BASE + 2, descricao: 'Competência C'},
                {competenciaCodigo: CODIGO_COMPETENCIA_BASE + 3, descricao: 'Competência D'},
                {competenciaCodigo: CODIGO_COMPETENCIA_BASE + 4, descricao: 'Competência E'},
            ],
        };
        servidoresVal.value = [
            {
                servidorTitulo: TITULO_SERVIDOR,
                servidorNome: NOME_SERVIDOR,
                situacaoServidor: 'CONSENSO_APROVADO',
                consenso: [
                    {competenciaCodigo: CODIGO_COMPETENCIA_BASE, importancia: 3, dominio: 3},
                    {competenciaCodigo: CODIGO_COMPETENCIA_BASE + 1, importancia: 2, dominio: 4},
                    {competenciaCodigo: CODIGO_COMPETENCIA_BASE + 2, importancia: 0, dominio: 2},
                    {competenciaCodigo: CODIGO_COMPETENCIA_BASE + 3, importancia: 4, dominio: 0},
                    {competenciaCodigo: CODIGO_COMPETENCIA_BASE + 4, importancia: 5, dominio: 1},
                ],
            },
        ];
        situacoesCapacitacaoVal.value = [
            {servidorTitulo: TITULO_SERVIDOR, competenciaCodigo: CODIGO_COMPETENCIA_BASE, situacaoCapacitacao: 'NA'},
            {servidorTitulo: TITULO_SERVIDOR, competenciaCodigo: CODIGO_COMPETENCIA_BASE + 1, situacaoCapacitacao: 'AC'},
            {servidorTitulo: TITULO_SERVIDOR, competenciaCodigo: CODIGO_COMPETENCIA_BASE + 2, situacaoCapacitacao: 'EC'},
            {servidorTitulo: TITULO_SERVIDOR, competenciaCodigo: CODIGO_COMPETENCIA_BASE + 3, situacaoCapacitacao: 'C'},
            {servidorTitulo: TITULO_SERVIDOR, competenciaCodigo: CODIGO_COMPETENCIA_BASE + 4, situacaoCapacitacao: 'I'},
        ];

        const wrapper = mount(DiagnosticoUnidadeView, {
            props: {
                codSubprocesso: COD_SUBPROCESSO,
                siglaUnidade: SIGLA_UNIDADE,
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
                    BDropdown: {template: '<div><slot /></div>'},
                    BDropdownItemButton: {template: '<button><slot /></button>'},
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
                    HistoricoAnaliseModal: {template: '<div />'},
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
        expect(wrapper.text()).toContain('NA');
        expect(wrapper.text()).toContain('AC');
        expect(wrapper.text()).toContain('EC');
        expect(wrapper.text()).toContain('C');
        expect(wrapper.text()).toContain('I');
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
