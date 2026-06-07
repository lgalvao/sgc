import {AxiosError} from 'axios';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {computed, nextTick, ref} from 'vue';
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
const concluindo = ref(false);
const validando = ref(false);
const devolvendo = ref(false);
const homologando = ref(false);
const impossibilitando = ref(false);
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

vi.mock('@/utils/apiError/normalizer', () => ({
    normalizarErro: vi.fn((err) => {
        if (err && typeof err === 'object' && 'response' in err && (err.response as any)?.data?.message) {
            return { mensagem: (err.response as any).data.message };
        }
        if (err instanceof Error) {
            return { mensagem: err.message };
        }
        return { mensagem: undefined };
    })
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
        concluindo,
        validando,
        devolvendo,
        homologando,
        impossibilitando,
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
        concluindo.value = false;
        validando.value = false;
        devolvendo.value = false;
        homologando.value = false;
        impossibilitando.value = false;
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
                        emits: ['dismissed'],
                        template: '<div class="app-alert">{{ mensagem }} <button data-testid="btn-dismiss-alert" @click="$emit(\'dismissed\')">X</button></div>',
                    },
                    EmptyState: {template: '<div data-testid="empty-state" />'},
                    BBadge: {
                        props: ['variant'],
                        template: '<span :class="variant ? `badge-${variant}` : \'\'"><slot /></span>',
                    },
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
                    'b-modal': {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: `
                          <div class="b-modal-stub">
                            <template v-if="modelValue">
                              <slot />
                              <slot name="footer" />
                            </template>
                          </div>
                        `,
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
    });

    it('deve exercitar fallbacks de erro sem mensagem e argumentos opcionais nos modais de fluxo', async () => {
        habilitarConcluirDiagnostico.value = true;
        habilitarValidarDiagnostico.value = true;
        habilitarHomologarDiagnostico.value = true;

        concluirDiagnosticoMock.mockRejectedValue({});
        validarDiagnosticoMock.mockRejectedValue({});
        homologarDiagnosticoMock.mockRejectedValue({});

        const wrapper = montar();

        // 1. Concluir sem mensagem de erro
        await wrapper.get('[data-testid="btn-concluir-diagnostico"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-concluir-diagnostico"]').trigger('click');
        expect(wrapper.text()).toContain('Não foi possível salvar. Tente novamente.');

        // 2. Validar com observacoes vazias e erro sem mensagem
        await wrapper.get('[data-testid="btn-validar-diagnostico"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-validar"]').trigger('click');
        expect(validarDiagnosticoMock).toHaveBeenCalledWith(undefined);
        expect(wrapper.text()).toContain('Não foi possível salvar. Tente novamente.');

        // 3. Homologar com observações vazias e erro sem mensagem
        await wrapper.get('[data-testid="btn-homologar-diagnostico"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-homologar"]').trigger('click');
        expect(homologarDiagnosticoMock).toHaveBeenCalledWith(undefined);
        expect(wrapper.text()).toContain('Não foi possível salvar. Tente novamente.');
    });

    it('exercita formatarSituacaoServidor e varianteSituacaoServidor para situacoes desconhecidas', () => {
        servidores.value = [
            {servidorTitulo: '242426', servidorNome: 'Duff McKagan', situacaoServidor: 'STATUS_DESCONHECIDO' as any},
        ];
        const wrapper = montar();
        expect(wrapper.text()).toContain('STATUS_DESCONHECIDO');
    });

    it('exercita todas as variantes de situacaoServidor e suas classes de badge correspondentes', () => {
        servidores.value = [
            {servidorTitulo: '1', servidorNome: 'Servidor 1', situacaoServidor: 'AUTOAVALIACAO_NAO_INICIADA'},
            {servidorTitulo: '2', servidorNome: 'Servidor 2', situacaoServidor: 'AUTOAVALIACAO_CONCLUIDA'},
            {servidorTitulo: '3', servidorNome: 'Servidor 3', situacaoServidor: 'CONSENSO_CRIADO'},
            {servidorTitulo: '4', servidorNome: 'Servidor 4', situacaoServidor: 'CONSENSO_APROVADO'},
            {servidorTitulo: '5', servidorNome: 'Servidor 5', situacaoServidor: 'AVALIACAO_IMPOSSIBILITADA'},
        ];

        const wrapper = montar();

        const badge1 = wrapper.find('span.badge-light');
        const badge2 = wrapper.find('span.badge-info');
        const badge3 = wrapper.find('span.badge-warning');
        const badge4 = wrapper.find('span.badge-success');
        const badge5 = wrapper.find('span.badge-secondary');

        expect(badge1.exists()).toBe(true);
        expect(badge2.exists()).toBe(true);
        expect(badge3.exists()).toBe(true);
        expect(badge4.exists()).toBe(true);
        expect(badge5.exists()).toBe(true);

        expect(badge1.text()).toBe('Autoavaliação não iniciada');
        expect(badge2.text()).toBe('Autoavaliação concluída');
        expect(badge3.text()).toBe('Avaliação de consenso criada');
        expect(badge4.text()).toBe('Avaliação de consenso aprovada');
        expect(badge5.text()).toBe('Avaliação impossibilitada');
    });

    it('exibe a mensagem de erro customizada do backend na falha ao devolver', async () => {
        habilitarDevolverDiagnostico.value = true;
        devolverDiagnosticoMock.mockRejectedValue(new Error('Justificativa inválida do backend'));
        erroDevolver.value = new Error('Erro: justificativa precisa de mais caracteres');

        const wrapper = montar();

        await wrapper.get('[data-testid="btn-devolver-diagnostico"]').trigger('click');
        await wrapper.get('textarea').setValue('Ajuste solicitado');
        await wrapper.get('[data-testid="btn-confirmar-devolver"]').trigger('click');

        expect(wrapper.text()).toContain('Erro: justificativa precisa de mais caracteres');
    });

    it('cobre o fechamento dos modais de fluxo pelo evento update:modelValue', async () => {
        const wrapper = montar();
        const modals = wrapper.findAllComponents('.b-modal-stub');

        // 1. Modal Impossibilitar
        await wrapper.get('[data-testid="btn-impossibilitar-242426"]').trigger('click');
        await nextTick();
        await (modals[0] as any).vm.$emit('update:modelValue', false);
        await nextTick();

        // 2. Modal Concluir
        await wrapper.get('[data-testid="btn-concluir-diagnostico"]').trigger('click');
        await nextTick();
        await (modals[1] as any).vm.$emit('update:modelValue', false);
        await nextTick();

        // 3. Modal Validar
        habilitarValidarDiagnostico.value = true;
        await nextTick();
        await wrapper.get('[data-testid="btn-validar-diagnostico"]').trigger('click');
        await nextTick();
        await (modals[2] as any).vm.$emit('update:modelValue', false);
        await nextTick();

        // 4. Modal Devolver
        habilitarDevolverDiagnostico.value = true;
        await nextTick();
        await wrapper.get('[data-testid="btn-devolver-diagnostico"]').trigger('click');
        await nextTick();
        await (modals[3] as any).vm.$emit('update:modelValue', false);
        await nextTick();

        // 5. Modal Homologar
        habilitarHomologarDiagnostico.value = true;
        await nextTick();
        await wrapper.get('[data-testid="btn-homologar-diagnostico"]').trigger('click');
        await nextTick();
        await (modals[4] as any).vm.$emit('update:modelValue', false);
        await nextTick();
    });

    it('navega de volta e fecha os alertas de erro e sucesso', async () => {
        const wrapper = montar({exibirBotaoVoltar: true});

        // Testar botão voltar
        const botaoVoltar = wrapper.findAll('button').find(b => b.text().includes('Voltar'));
        await botaoVoltar?.trigger('click');
        expect(backMock).toHaveBeenCalled();

        // Testar dismiss do alerta de erro
        devolverDiagnosticoMock.mockRejectedValue(new Error('Erro de rede'));
        habilitarDevolverDiagnostico.value = true;
        await nextTick();
        await wrapper.get('[data-testid="btn-devolver-diagnostico"]').trigger('click');
        await wrapper.get('textarea').setValue('Justificativa');
        await wrapper.get('[data-testid="btn-confirmar-devolver"]').trigger('click');
        await nextTick();

        expect(wrapper.text()).toContain('Não foi possível salvar.');
        await wrapper.get('[data-testid="btn-dismiss-alert"]').trigger('click');
        await nextTick();
        expect(wrapper.text()).not.toContain('Não foi possível salvar.');

        // Testar dismiss do alerta de sucesso
        devolverDiagnosticoMock.mockResolvedValue(undefined);
        await wrapper.get('[data-testid="btn-devolver-diagnostico"]').trigger('click');
        await wrapper.get('textarea').setValue('Justificativa');
        await wrapper.get('[data-testid="btn-confirmar-devolver"]').trigger('click');
        await nextTick();

        expect(wrapper.text()).toContain('Diagnóstico devolvido para ajustes');
        await wrapper.get('[data-testid="btn-dismiss-alert"]').trigger('click');
        await nextTick();
        expect(wrapper.text()).not.toContain('Diagnóstico devolvido para ajustes');
    });

    it('renderiza spinners de loading quando as operacoes estao em andamento', async () => {
        // Inicializa com loading desativado para permitir cliques e aberturas de modal
        concluindo.value = false;
        validando.value = false;
        devolvendo.value = false;
        homologando.value = false;
        impossibilitando.value = false;

        habilitarConcluirDiagnostico.value = true;
        habilitarValidarDiagnostico.value = true;
        habilitarDevolverDiagnostico.value = true;
        habilitarHomologarDiagnostico.value = true;

        const wrapper = montar();

        // Abre os modais primeiro
        await wrapper.get('[data-testid="btn-concluir-diagnostico"]').trigger('click');
        await wrapper.get('[data-testid="btn-validar-diagnostico"]').trigger('click');
        await wrapper.get('[data-testid="btn-devolver-diagnostico"]').trigger('click');
        await wrapper.get('[data-testid="btn-homologar-diagnostico"]').trigger('click');
        await wrapper.get('[data-testid="btn-impossibilitar-242426"]').trigger('click');
        await nextTick();

        // Ativa os estados de loading
        concluindo.value = true;
        validando.value = true;
        devolvendo.value = true;
        homologando.value = true;
        impossibilitando.value = true;
        await nextTick();

        // Verifica a presença de spinners no HTML compilado (BSpinner stubbed -> span)
        expect(wrapper.find('[data-testid="btn-confirmar-concluir-diagnostico"] span').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-confirmar-validar"] span').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-confirmar-devolver"] span').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-confirmar-homologar"] span').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-confirmar-impossibilitar"] span').exists()).toBe(true);
    });

    it('exercita o retorno antecipado de confirmarImpossibilitar quando servidorSelecionado for null', async () => {
        const wrapper = montar();

        // Encontra o stub do modal pelo class selector .b-modal-stub
        const modals = wrapper.findAllComponents('.b-modal-stub');
        const modalImpossibilitar = modals[0];
        await (modalImpossibilitar as any).vm.$emit('update:modelValue', true);
        await nextTick();

        // Digita justificativa para passar no check de string vazia
        await wrapper.get('[data-testid="textarea-justificativa-impossibilidade"]').setValue('Justificativa de teste');
        // Clica no botão de confirmar impossibilidade
        await wrapper.get('[data-testid="btn-confirmar-impossibilitar"]').trigger('click');

        // Como servidorSelecionado é null, o método retorna de imediato e impossibilitarAvaliacaoMock não deve ser chamado
        expect(impossibilitarAvaliacaoMock).not.toHaveBeenCalled();
    });

    it('exercita o fallback de erroConcluir.value?.message na falha ao concluir', async () => {
        habilitarConcluirDiagnostico.value = true;
        concluirDiagnosticoMock.mockRejectedValue({}); // Rejeita com objeto vazio sem propriedade .mensagem
        erroConcluir.value = new Error('Mensagem customizada do erroConcluir ref');

        const wrapper = montar();

        await wrapper.get('[data-testid="btn-concluir-diagnostico"]').trigger('click');
        await wrapper.get('[data-testid="btn-confirmar-concluir-diagnostico"]').trigger('click');
        await nextTick();

        expect(wrapper.text()).toContain('Mensagem customizada do erroConcluir ref');
    });
});
