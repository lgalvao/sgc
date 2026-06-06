import {AxiosError} from 'axios';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {computed, ref} from 'vue';
import DiagnosticoEquipePainel from '../DiagnosticoEquipePainel.vue';

const pushMock = vi.fn();
const backMock = vi.fn();
const concluirDiagnosticoMock = vi.fn();
const validarDiagnosticoMock = vi.fn();
const devolverDiagnosticoMock = vi.fn();
const homologarDiagnosticoMock = vi.fn();
const impossibilitarAvaliacaoMock = vi.fn();

const podeCriarConsenso = ref(true);
const habilitarConcluirDiagnostico = ref(true);
const habilitarValidarDiagnostico = ref(false);
const habilitarDevolverDiagnostico = ref(false);
const habilitarHomologarDiagnostico = ref(false);
const servidores = ref<any[]>([
    {servidorTitulo: '242426', servidorNome: 'Duff McKagan', situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA'},
    {servidorTitulo: '242427', servidorNome: 'Slash', situacaoServidor: 'AVALIACAO_IMPOSSIBILITADA'},
]);

const erroConcluir = ref<Error | null>(null);
const erroValidar = ref<Error | null>(null);
const erroDevolver = ref<Error | null>(null);
const erroHomologar = ref<Error | null>(null);

vi.mock('vue-router', () => ({
    useRouter: () => ({
        push: pushMock,
        back: backMock,
    }),
}));

vi.mock('@/composables/useDiagnosticoPermissoes', () => ({
    useDiagnosticoPermissoes: () => ({
        podeCriarConsenso: computed(() => podeCriarConsenso.value),
        habilitarConcluirDiagnostico: computed(() => habilitarConcluirDiagnostico.value),
        habilitarValidarDiagnostico: computed(() => habilitarValidarDiagnostico.value),
        habilitarDevolverDiagnostico: computed(() => habilitarDevolverDiagnostico.value),
        habilitarHomologarDiagnostico: computed(() => habilitarHomologarDiagnostico.value),
    }),
}));

vi.mock('@/composables/useMonitoramentoDiagnostico', () => ({
    useMonitoramentoDiagnostico: () => ({
        unidade: ref({unidadeSigla: 'ASSESSORIA_12', unidadeNome: 'Assessoria 12'}),
        servidores,
    }),
}));

vi.mock('@/composables/useFluxoDiagnostico', () => ({
    useFluxoDiagnostico: () => ({
        concluindo: ref(false),
        validando: ref(false),
        devolvendo: ref(false),
        homologando: ref(false),
        impossibilitando: ref(false),
        erroConcluir,
        erroValidar,
        erroDevolver,
        erroHomologar,
        erroImpossibilitar: ref(null),
        concluirDiagnostico: concluirDiagnosticoMock,
        validarDiagnostico: validarDiagnosticoMock,
        devolverDiagnostico: devolverDiagnosticoMock,
        homologarDiagnostico: homologarDiagnosticoMock,
        impossibilitarAvaliacao: impossibilitarAvaliacaoMock,
    }),
}));

describe('DiagnosticoEquipePainel', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        podeCriarConsenso.value = true;
        habilitarConcluirDiagnostico.value = true;
        habilitarValidarDiagnostico.value = false;
        habilitarDevolverDiagnostico.value = false;
        habilitarHomologarDiagnostico.value = false;
        servidores.value = [
            {servidorTitulo: '242426', servidorNome: 'Duff McKagan', situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA'},
            {servidorTitulo: '242427', servidorNome: 'Slash', situacaoServidor: 'AVALIACAO_IMPOSSIBILITADA'},
        ];
        erroConcluir.value = null;
        erroValidar.value = null;
        erroDevolver.value = null;
        erroHomologar.value = null;
    });

    function montar(props?: Record<string, unknown>) {
        return mount(DiagnosticoEquipePainel, {
            props: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                ...props,
            },
            global: {
                stubs: {
                    AppAlert: {
                        props: ['mensagem'],
                        template: '<div class="app-alert">{{ mensagem }}</div>',
                    },
                    EmptyState: {template: '<div data-testid="empty-state" />'},
                    BBadge: {template: '<span><slot /></span>'},
                    BCard: {template: '<section><slot /></section>'},
                    BSpinner: {template: '<span />'},
                    BButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BDropdown: {template: '<div v-bind="$attrs"><button>Ações</button><slot /></div>'},
                    BDropdownItemButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
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
                            <div v-for="item in items" :key="item.servidorTitulo">
                              <slot name="cell(situacaoServidor)" :item="item" />
                              <slot name="cell(acoes)" :item="item" />
                            </div>
                          </div>
                        `,
                    },
                },
            },
        });
    }

    it('renderiza estado vazio e permite ocultar cabeçalho', () => {
        servidores.value = [];
        const wrapper = montar({exibirCabecalho: false, exibirBotaoVoltar: false});

        expect(wrapper.find('[data-testid="empty-state"]').exists()).toBe(true);
        expect(wrapper.text()).not.toContain('Monitoramento do Diagnóstico');
        expect(wrapper.text()).not.toContain('Voltar');
    });

    it('navega para consenso e situação de capacitação pelas ações da chefia', async () => {
        const wrapper = montar();

        await wrapper.get('[data-testid="btn-manter-consenso-242426"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'ConsensoDiagnostico',
            params: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
                servidorTitulo: '242426',
            },
        });

        await wrapper.get('[data-testid="btn-manter-capacitacao-242426"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'OcupacoesCriticasDiagnostico',
            params: {
                codSubprocesso: 400,
                siglaUnidade: 'ASSESSORIA_12',
            },
            query: {
                servidorTitulo: '242426',
            },
        });

        expect(wrapper.get('[data-testid="btn-impossibilitar-242427"]').attributes('disabled')).toBeDefined();
    });

    it('valida justificativa obrigatória e registra impossibilidade com sucesso', async () => {
        impossibilitarAvaliacaoMock.mockResolvedValue(undefined);
        const wrapper = montar();

        await wrapper.get('[data-testid="btn-impossibilitar-242426"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-impossibilitar"]').trigger('click');
        expect(wrapper.text()).toContain('A justificativa é obrigatória.');

        await wrapper.get('textarea').setValue('Servidor afastado.');
        await wrapper.get('[data-testid="btn-confirmar-impossibilitar"]').trigger('click');

        expect(impossibilitarAvaliacaoMock).toHaveBeenCalledWith('242426', 'Servidor afastado.');
        expect(wrapper.text()).toContain('Impossibilidade registrada');
    });

    it('conclui diagnóstico e redireciona para o painel', async () => {
        concluirDiagnosticoMock.mockResolvedValue(undefined);
        const wrapper = montar();

        await wrapper.get('[data-testid="btn-concluir-diagnostico"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-concluir-diagnostico"]').trigger('click');

        expect(concluirDiagnosticoMock).toHaveBeenCalled();
        expect(pushMock).toHaveBeenCalledWith({name: 'Painel'});
    });

    it('exibe erro retornado ao falhar conclusão', async () => {
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

        const wrapper = montar();
        await wrapper.get('[data-testid="btn-concluir-diagnostico"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-concluir-diagnostico"]').trigger('click');

        expect(wrapper.text()).toContain('Ainda existem avaliações ou ocupações críticas pendentes.');
    });

    it('valida, devolve e homologa conforme permissões do fluxo', async () => {
        validarDiagnosticoMock.mockResolvedValue(undefined);
        devolverDiagnosticoMock.mockResolvedValue(undefined);
        homologarDiagnosticoMock.mockResolvedValue(undefined);
        habilitarConcluirDiagnostico.value = false;
        habilitarValidarDiagnostico.value = true;
        habilitarDevolverDiagnostico.value = true;
        habilitarHomologarDiagnostico.value = true;

        const wrapper = montar();

        await wrapper.get('[data-testid="btn-validar-diagnostico"]').trigger('click');
        await wrapper.get('textarea').setValue('Observações');
        await wrapper.get('[data-testid="btn-confirmar-validar"]').trigger('click');
        expect(validarDiagnosticoMock).toHaveBeenCalledWith('Observações');
        expect(wrapper.text()).toContain('Diagnóstico validado');

        await wrapper.get('[data-testid="btn-devolver-diagnostico"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-devolver"]').trigger('click');
        expect(wrapper.text()).toContain('A justificativa é obrigatória.');

        await wrapper.get('textarea').setValue('Ajustes');
        await wrapper.get('[data-testid="btn-confirmar-devolver"]').trigger('click');
        expect(devolverDiagnosticoMock).toHaveBeenCalledWith('Ajustes');
        expect(wrapper.text()).toContain('Diagnóstico devolvido para ajustes');

        await wrapper.get('[data-testid="btn-homologar-diagnostico"]').trigger('click');
        await wrapper.get('textarea').setValue('Homologado');
        await wrapper.get('[data-testid="btn-confirmar-homologar"]').trigger('click');
        expect(homologarDiagnosticoMock).toHaveBeenCalledWith('Homologado');
        expect(wrapper.text()).toContain('Diagnóstico homologado');
    });

    it('oculta ações e ajusta colunas se não for chefe', () => {
        podeCriarConsenso.value = false;
        const wrapper = montar();

        expect(wrapper.find('[data-testid="dropdown-acoes-242426"]').exists()).toBe(false);
    });

    it('exibe erro caso as ações falhem', async () => {
        habilitarConcluirDiagnostico.value = false;
        habilitarValidarDiagnostico.value = true;
        habilitarDevolverDiagnostico.value = true;
        habilitarHomologarDiagnostico.value = true;

        impossibilitarAvaliacaoMock.mockRejectedValue(new Error('Erro de rede'));
        validarDiagnosticoMock.mockRejectedValue(new Error('Erro de rede'));
        devolverDiagnosticoMock.mockRejectedValue(new Error('Erro de rede'));
        homologarDiagnosticoMock.mockRejectedValue(new Error('Erro de rede'));

        const wrapper = montar();

        // 1. Falha ao impossibilitar
        await wrapper.get('[data-testid="btn-impossibilitar-242426"]').trigger('click');
        await wrapper.get('textarea').setValue('Servidor afastado.');
        await wrapper.get('[data-testid="btn-confirmar-impossibilitar"]').trigger('click');
        expect(wrapper.text()).toContain('Não foi possível salvar.');

        // 2. Falha ao validar
        await wrapper.get('[data-testid="btn-validar-diagnostico"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-validar"]').trigger('click');
        expect(wrapper.text()).toContain('Não foi possível salvar.');

        // 3. Falha ao devolver
        await wrapper.get('[data-testid="btn-devolver-diagnostico"]').trigger('click');
        await wrapper.get('textarea').setValue('Justificativa');
        await wrapper.get('[data-testid="btn-confirmar-devolver"]').trigger('click');
        expect(wrapper.text()).toContain('Não foi possível salvar.');

        // 4. Falha ao homologar
        await wrapper.get('[data-testid="btn-homologar-diagnostico"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-homologar"]').trigger('click');
        expect(wrapper.text()).toContain('Não foi possível salvar.');
    });
});
