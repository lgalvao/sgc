import {beforeEach, describe, expect, it, vi} from 'vitest';
import {computed, ref} from 'vue';
import {mount} from '@vue/test-utils';
import DiagnosticoUnidadeView from '../DiagnosticoUnidadeView.vue';
import {Perfil} from '@/types/comum';

const backMock = vi.fn();
const validarDiagnosticoMock = vi.fn();
const devolverDiagnosticoMock = vi.fn();
const homologarDiagnosticoMock = vi.fn();
const validarAcaoValidarDiagnosticoMock = vi.fn();
const validarAcaoDevolverDiagnosticoMock = vi.fn();
const validarAcaoHomologarDiagnosticoMock = vi.fn();
const COD_SUBPROCESSO = 400;
const SIGLA_UNIDADE = 'ASSESSORIA_12';
const NOME_UNIDADE = 'Assessoria 12';
const TITULO_SERVIDOR = '242426';
const NOME_SERVIDOR = 'João Guilherme de Albuquerque Maranhão';
const CODIGO_COMPETENCIA_BASE = 10;
const PROCESSO_DESCRICAO = 'Processo de Diagnóstico 2026';
const LOCALIZACAO_ATUAL = 'Assessoria 12';
const NOME_TITULAR = 'Ana Paula Titular';
const NOME_RESPONSAVEL = 'Carlos Responsável';

const perfilSelecionado = ref<Perfil>(Perfil.GESTOR);
const situacaoDiagnostico = ref<'EM_ANDAMENTO' | 'CONCLUIDO' | 'VALIDADO' | 'HOMOLOGADO'>('CONCLUIDO');
const erroValidar = ref<Error | null>(null);
const erroDevolver = ref<Error | null>(null);
const erroHomologar = ref<Error | null>(null);
const erroValidacaoValidar = ref<Error | null>(null);
const erroValidacaoDevolver = ref<Error | null>(null);
const erroValidacaoHomologar = ref<Error | null>(null);

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
    processoCodigo: 910,
    subprocessoCodigo: COD_SUBPROCESSO,
    unidadeSigla: SIGLA_UNIDADE,
    unidadeNome: NOME_UNIDADE,
    competencias: [
        {competenciaCodigo: CODIGO_COMPETENCIA_BASE, descricao: 'Competência A'},
    ],
});

vi.mock('@/composables/useDiagnosticoContexto', () => ({
    useDiagnosticoContexto: () => ({
        data: contextoVal,
        isPending: ref(false),
        isLoading: ref(false),
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
const subprocessoDetalheVal = ref<any>({
    codigo: COD_SUBPROCESSO,
    unidade: {
        codigo: 12,
        sigla: SIGLA_UNIDADE,
        nome: NOME_UNIDADE,
    },
    titular: {
        titulo: '111111',
        nome: NOME_TITULAR,
        email: 'titular@example.com',
        ramal: '1234',
    },
    responsavel: {
        usuario: {
            titulo: '222222',
            nome: NOME_RESPONSAVEL,
            email: 'responsavel@example.com',
            ramal: '5678',
        },
        tipo: 'Substituição',
        dataInicio: '2026-06-01',
        dataFim: '2026-06-30',
    },
    situacao: 'DIAGNOSTICO_CONCLUIDO',
    localizacaoAtual: LOCALIZACAO_ATUAL,
    processoDescricao: PROCESSO_DESCRICAO,
    prazoEtapaAtual: '2026-06-30',
    movimentacoes: [],
    permissoes: {},
});

vi.mock('@pinia/colada', () => ({
    useQuery: vi.fn(() => ({
        data: subprocessoDetalheVal,
        isPending: ref(false),
        isLoading: ref(false),
    })),
}));

vi.mock('@/composables/useDiagnosticoUnidade', () => ({
    useDiagnosticoUnidade: () => ({
        unidade: unidadeVal,
        servidores: servidoresVal,
        situacoesCapacitacao: situacoesCapacitacaoVal,
        movimentacoes: ref([
            {codigo: 1, dataHora: '2026-06-06 09:00', unidadeOrigemCodigo: 1, unidadeOrigemSigla: 'A', unidadeOrigemNome: 'Unidade A', unidadeDestinoCodigo: 2, unidadeDestinoSigla: 'B', unidadeDestinoNome: 'Unidade B', usuarioTitulo: '1234567890', usuarioNome: 'Usuário Teste', descricao: 'Diagnóstico concluído'},
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
        erroValidacaoValidar,
        erroValidar,
        erroValidacaoDevolver,
        erroDevolver,
        erroValidacaoHomologar,
        erroHomologar,
        validarAcaoValidarDiagnostico: validarAcaoValidarDiagnosticoMock,
        validarAcaoDevolverDiagnostico: validarAcaoDevolverDiagnosticoMock,
        validarAcaoHomologarDiagnostico: validarAcaoHomologarDiagnosticoMock,
        validarDiagnostico: validarDiagnosticoMock,
        devolverDiagnostico: devolverDiagnosticoMock,
        homologarDiagnostico: homologarDiagnosticoMock,
    }),
}));

vi.mock('@/composables/useDiagnosticoPermissoes', () => ({
    useDiagnosticoPermissoes: () => ({
        queryPermissoes: {
            isPending: ref(false),
            isLoading: ref(false),
        },
        podeValidarDiagnostico: computed(() => perfilSelecionado.value === Perfil.GESTOR),
        podeDevolverDiagnostico: computed(() => perfilSelecionado.value === Perfil.GESTOR || perfilSelecionado.value === Perfil.ADMIN),
        podeHomologarDiagnostico: computed(() => perfilSelecionado.value === Perfil.ADMIN),
        habilitarValidarDiagnostico: computed(() => situacaoDiagnostico.value === 'CONCLUIDO'),
        habilitarDevolverDiagnostico: computed(() => situacaoDiagnostico.value === 'CONCLUIDO'),
        habilitarHomologarDiagnostico: computed(() => perfilSelecionado.value === Perfil.ADMIN && situacaoDiagnostico.value === 'VALIDADO'),
    }),
}));

vi.mock('@/services/subprocessoServiceContexto', () => ({
    buscarSubprocessoDetalhe: vi.fn(),
}));

vi.mock('@/composables/useToast', () => ({
    useToast: () => ({
        exibirSucesso: vi.fn(),
        exibirErro: vi.fn(),
        exibirToast: vi.fn(),
        registrarPendente: vi.fn(),
        exibirPendente: vi.fn(),
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
        erroValidacaoValidar.value = null;
        erroValidacaoDevolver.value = null;
        erroValidacaoHomologar.value = null;
        validarAcaoValidarDiagnosticoMock.mockResolvedValue(undefined);
        validarAcaoDevolverDiagnosticoMock.mockResolvedValue(undefined);
        validarAcaoHomologarDiagnosticoMock.mockResolvedValue(undefined);
        unidadeVal.value = {
            unidadeSigla: SIGLA_UNIDADE,
            unidadeNome: NOME_UNIDADE,
            situacaoSubprocesso: 'DIAGNOSTICO_CONCLUIDO',
        };
        contextoVal.value = {
            processoCodigo: 910,
            subprocessoCodigo: COD_SUBPROCESSO,
            unidadeSigla: SIGLA_UNIDADE,
            unidadeNome: NOME_UNIDADE,
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
                    PageHeader: {
                        props: ['title', 'subtitle'],
                        template: '<div><h1>{{ title }}</h1><p>{{ subtitle }}</p><slot /><slot name="actions" /></div>',
                    },
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    Alerta: {
                        props: ['mensagem'],
                        template: '<div class="app-alert">{{ mensagem }}</div>',
                    },
                    EmptyState: {
                        props: ['title', 'description'],
                        template: '<div data-testid="empty-state">{{ title }}{{ description }}</div>',
                    },
                    BButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BBadge: {template: '<span><slot /></span>'},
                    BCard: {template: '<section><slot /></section>'},
                    BCardHeader: {template: '<header><slot /></header>'},
                    BDropdown: {template: '<div><slot /></div>'},
                    BDropdownItemButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BRow: {template: '<div><slot /></div>'},
                    BCol: {template: '<div><slot /></div>'},
                    BSpinner: {template: '<span />'},
                    BFormText: {template: '<small><slot /></small>'},
                    BFormTextarea: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
                    },
                    BListGroup: {template: '<div><slot /></div>'},
                    BListGroupItem: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    DiagnosticoFluxoModais: {
                        props: [
                            'modalHistoricoAberto',
                            'modalValidarAberto',
                            'modalDevolverAberto',
                            'modalHomologarAberto',
                            'testIdConfirmarValidar',
                            'testIdConfirmarDevolver',
                            'testIdConfirmarHomologar',
                            'observacoesValidar',
                            'justificativaDevolver',
                            'feedbackJustificativaDevolver',
                            'erroValidar',
                            'erroDevolver',
                            'erroHomologar'
                        ],
                        emits: [
                            'confirmar-validar',
                            'confirmar-devolver',
                            'confirmar-homologar',
                            'update:modal-validar-aberto',
                            'update:modal-devolver-aberto',
                            'update:modal-homologar-aberto',
                            'update:observacoes-validar',
                            'update:justificativa-devolver'
                        ],
                        template: `
                          <div>
                            <div v-if="modalHistoricoAberto" data-testid="modal-historico-analise" />
                            <div v-if="modalValidarAberto">
                              <textarea
                                :value="observacoesValidar"
                                @input="$emit('update:observacoes-validar', $event.target.value)"
                              />
                              <button :data-testid="testIdConfirmarValidar" @click="$emit('confirmar-validar')">Validar</button>
                              <div v-if="erroValidar">{{ erroValidar }}</div>
                            </div>
                            <div v-if="modalDevolverAberto">
                              <textarea
                                :value="justificativaDevolver"
                                @input="$emit('update:justificativa-devolver', $event.target.value)"
                              />
                              <button :data-testid="testIdConfirmarDevolver" @click="$emit('confirmar-devolver')">Devolver</button>
                              <div v-if="feedbackJustificativaDevolver">{{ feedbackJustificativaDevolver }}</div>
                              <div v-if="erroDevolver">{{ erroDevolver }}</div>
                            </div>
                            <div v-if="modalHomologarAberto">
                              <button :data-testid="testIdConfirmarHomologar" @click="$emit('confirmar-homologar')">Homologar</button>
                              <div v-if="erroHomologar">{{ erroHomologar }}</div>
                            </div>
                          </div>
                        `,
                    },
                    SubprocessoMovimentacoes: {
                        props: ['movimentacoes'],
                        template: '<div data-testid="movimentacoes">{{ movimentacoes.map((item) => `${item.unidadeOrigemSigla}->${item.unidadeDestinoSigla}:${item.descricao}`).join(" | ") }}</div>',
                    },
                    BTable: {
                        props: ['items'],
                        template: `
                          <div>
                            <div v-for="item in items" :key="item.competenciaCodigo ?? item.servidorTitulo">
                              <span v-for="(valor, chave) in item" :key="chave">{{ valor }}</span>
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
    }

    it('renderiza o cabeçalho contextual, lista de servidores, empty state inicial e histórico do diagnóstico', async () => {
        const wrapper = montar();

        expect(wrapper.text()).toContain(SIGLA_UNIDADE);
        expect(wrapper.text()).toContain(NOME_UNIDADE);
        expect(wrapper.text()).toContain(PROCESSO_DESCRICAO);
        expect(wrapper.text()).toContain(LOCALIZACAO_ATUAL);
        expect(wrapper.text()).toContain(NOME_TITULAR);
        expect(wrapper.text()).toContain(NOME_RESPONSAVEL);
        expect(wrapper.text()).toContain(NOME_SERVIDOR);
        expect(wrapper.text()).toContain('Diagnóstico concluído');
        expect(wrapper.find('[data-testid="btn-historico-analise-unidade"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="lista-servidores-diagnostico-unidade"]').exists()).toBe(true);
        expect(wrapper.find(`[data-testid="btn-selecionar-servidor-diagnostico-unidade-${TITULO_SERVIDOR}"]`).exists()).toBe(true);
        expect(wrapper.find('[data-testid="tbl-competencias-servidor-diagnostico-unidade"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="detalhes-servidor-diagnostico-unidade"]').exists()).toBe(false);
        expect(wrapper.text()).toContain('Selecione um servidor');

        await wrapper.get(`[data-testid="btn-selecionar-servidor-diagnostico-unidade-${TITULO_SERVIDOR}"]`).trigger('click');

        expect(wrapper.text()).toContain('EC - Em capacitação');
        expect(wrapper.find('[data-testid="tbl-competencias-servidor-diagnostico-unidade"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="detalhes-servidor-diagnostico-unidade"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="movimentacoes"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="movimentacoes"]').text()).toContain('A->B:Diagnóstico concluído');
        expect(wrapper.text()).toContain('Avaliações de competências');
        expect(wrapper.text()).toContain('Avaliação de consenso aprovada');
        expect(wrapper.text()).not.toContain(TITULO_SERVIDOR);
        expect(wrapper.text()).not.toContain('CONCLUIDO');
        expect(wrapper.find('[data-testid="btn-validar-diagnostico-unidade"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-devolver-diagnostico-unidade"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-homologar-diagnostico-unidade"]').exists()).toBe(false);
    });

    it('troca o servidor selecionado sem duplicar a lista de competências da unidade', async () => {
        const outroServidorTitulo = '242427';
        const outroServidorNome = 'Maria Eduarda Cavalcanti de Alencar';
        servidoresVal.value = [
            {
                servidorTitulo: TITULO_SERVIDOR,
                servidorNome: NOME_SERVIDOR,
                situacaoServidor: 'CONSENSO_APROVADO',
                consenso: [
                    {competenciaCodigo: CODIGO_COMPETENCIA_BASE, importancia: 4, dominio: 2},
                ],
            },
            {
                servidorTitulo: outroServidorTitulo,
                servidorNome: outroServidorNome,
                situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA',
                consenso: [
                    {competenciaCodigo: CODIGO_COMPETENCIA_BASE, importancia: 2, dominio: 5},
                ],
            },
        ];
        situacoesCapacitacaoVal.value = [
            {servidorTitulo: TITULO_SERVIDOR, competenciaCodigo: CODIGO_COMPETENCIA_BASE, situacaoCapacitacao: 'EC'},
            {servidorTitulo: outroServidorTitulo, competenciaCodigo: CODIGO_COMPETENCIA_BASE, situacaoCapacitacao: 'AC'},
        ];

        const wrapper = montar();

        expect(wrapper.text()).toContain(NOME_SERVIDOR);
        expect(wrapper.text()).toContain('Avaliação de consenso aprovada');
        expect(wrapper.text()).not.toContain('EC');

        await wrapper.get(`[data-testid="btn-selecionar-servidor-diagnostico-unidade-${TITULO_SERVIDOR}"]`).trigger('click');
        expect(wrapper.text()).toContain('EC - Em capacitação');

        await wrapper.get(`[data-testid="btn-selecionar-servidor-diagnostico-unidade-${outroServidorTitulo}"]`).trigger('click');

        expect(wrapper.text()).toContain(outroServidorNome);
        expect(wrapper.text()).toContain('Autoavaliação concluída');
        expect(wrapper.text()).toContain('AC');
    });

    it('mostra empty state em vez da tabela quando o servidor nao possui dados de importancia/dominio', async () => {
        const outroServidorTitulo = '242427';
        const outroServidorNome = 'Maria Eduarda Cavalcanti de Alencar';
        servidoresVal.value = [
            {
                servidorTitulo: outroServidorTitulo,
                servidorNome: outroServidorNome,
                situacaoServidor: 'CONSENSO_CRIADO',
                consenso: [],
            },
        ];

        const wrapper = montar();

        await wrapper.get(`[data-testid="btn-selecionar-servidor-diagnostico-unidade-${outroServidorTitulo}"]`).trigger('click');

        expect(wrapper.text()).toContain('Autoavaliação não iniciada');
        expect(wrapper.find('[data-testid="tbl-competencias-servidor-diagnostico-unidade"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="detalhes-servidor-diagnostico-unidade"]').exists()).toBe(false);
    });

    it('mostra a tabela quando existem dados de importancia/dominio independentemente da situacao do servidor', async () => {
        const outroServidorTitulo = '242427';
        const outroServidorNome = 'Maria Eduarda Cavalcanti de Alencar';
        servidoresVal.value = [
            {
                servidorTitulo: outroServidorTitulo,
                servidorNome: outroServidorNome,
                situacaoServidor: 'AUTOAVALIACAO_NAO_INICIADA',
                consenso: [
                    {competenciaCodigo: CODIGO_COMPETENCIA_BASE, importancia: 3, dominio: 1},
                ],
            },
        ];

        const wrapper = montar();

        await wrapper.get(`[data-testid="btn-selecionar-servidor-diagnostico-unidade-${outroServidorTitulo}"]`).trigger('click');

        expect(wrapper.find('[data-testid="tbl-competencias-servidor-diagnostico-unidade"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="detalhes-servidor-diagnostico-unidade"]').exists()).toBe(true);
        expect(wrapper.text()).not.toContain('As competências do servidor serão exibidas após o início da autoavaliação.');
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
        expect(wrapper.text()).not.toContain('Diagnóstico homologado');

        perfilSelecionado.value = Perfil.GESTOR;
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
        erroValidacaoValidar.value = new Error('Falha ao validar');
        validarAcaoValidarDiagnosticoMock.mockRejectedValue(erroValidacaoValidar.value);

        const wrapper = montar();
        await wrapper.get('[data-testid="btn-validar-diagnostico-unidade"]').trigger('click');

        expect(wrapper.text()).toContain('Falha ao validar');
    });

    it('exibe erro quando devolver falha', async () => {
        erroValidacaoDevolver.value = new Error('Falha ao devolver');
        validarAcaoDevolverDiagnosticoMock.mockRejectedValue(erroValidacaoDevolver.value);

        const wrapper = montar();
        await wrapper.get('[data-testid="btn-devolver-diagnostico-unidade"]').trigger('click');

        expect(wrapper.text()).toContain('Falha ao devolver');
    });

    it('exibe erro quando homologar falha', async () => {
        perfilSelecionado.value = Perfil.ADMIN;
        situacaoDiagnostico.value = 'VALIDADO';
        erroValidacaoHomologar.value = new Error('Falha ao homologar');
        validarAcaoHomologarDiagnosticoMock.mockRejectedValue(erroValidacaoHomologar.value);

        const wrapper = montar();
        await wrapper.get('[data-testid="btn-homologar-diagnostico-unidade"]').trigger('click');

        expect(wrapper.text()).toContain('Falha ao homologar');
    });

    it('mantem o menu de acoes visivel e desabilita os itens quando o gestor nao pode mais executar', () => {
        situacaoDiagnostico.value = 'EM_ANDAMENTO';

        const wrapper = montar();

        expect(wrapper.find('[data-testid="dropdown-acoes-diagnostico-unidade"]').exists()).toBe(true);
        expect(wrapper.get('[data-testid="btn-validar-diagnostico-unidade"]').attributes('disabled')).toBeDefined();
        expect(wrapper.get('[data-testid="btn-devolver-diagnostico-unidade"]').attributes('disabled')).toBeDefined();
        expect(wrapper.find('[data-testid="btn-homologar-diagnostico-unidade"]').exists()).toBe(false);
    });

    it('remove a situacao geral do card principal', () => {
        situacaoDiagnostico.value = 'OUTRO_STATUS' as any;
        const wrapper = montar();
        expect(wrapper.text()).toContain('ASSESSORIA_12');
        expect(wrapper.text()).not.toContain('OUTRO_STATUS');
        expect(wrapper.text()).not.toContain('HOMOLOGADO');
        expect(wrapper.text()).not.toContain('VALIDADO');
    });

    it('exercita as variantes de capacitação e formatação de notas na lista do servidor selecionado', async () => {
        unidadeVal.value = {
            unidadeSigla: SIGLA_UNIDADE,
            unidadeNome: NOME_UNIDADE,
            situacaoSubprocesso: 'DIAGNOSTICO_CONCLUIDO',
        };
        contextoVal.value = {
            processoCodigo: 910,
            subprocessoCodigo: COD_SUBPROCESSO,
            unidadeSigla: SIGLA_UNIDADE,
            unidadeNome: NOME_UNIDADE,
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
                    PageHeader: {
                        props: ['title', 'subtitle'],
                        template: '<div><h1>{{ title }}</h1><p>{{ subtitle }}</p><slot /><slot name="actions" /></div>',
                    },
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina" />'},
                    Alerta: {template: '<div />'},
                    EmptyState: {
                        props: ['title', 'description'],
                        template: '<div>{{ title }}{{ description }}</div>',
                    },
                    BButton: {template: '<button><slot /></button>'},
                    BBadge: {template: '<span><slot /></span>'},
                    BCard: {template: '<section><slot /></section>'},
                    BCardHeader: {template: '<header><slot /></header>'},
                    BDropdown: {template: '<div><slot /></div>'},
                    BDropdownItemButton: {template: '<button><slot /></button>'},
                    BRow: {template: '<div><slot /></div>'},
                    BCol: {template: '<div><slot /></div>'},
                    BSpinner: {template: '<span />'},
                    BFormText: {template: '<small><slot /></small>'},
                    BFormTextarea: {template: '<textarea />'},
                    BListGroup: {template: '<div><slot /></div>'},
                    BListGroupItem: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    DiagnosticoFluxoModais: {template: '<div />'},
                    SubprocessoMovimentacoes: {template: '<div />'},
                    BTable: {
                        props: ['items', 'fields'],
                        template: `
                          <div>
                            <div v-for="item in items" :key="item.competenciaCodigo ?? item.servidorTitulo">
                              <span v-for="(valor, chave) in item" :key="chave">{{ valor }}</span>
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

        await wrapper.get(`[data-testid="btn-selecionar-servidor-diagnostico-unidade-${TITULO_SERVIDOR}"]`).trigger('click');

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
