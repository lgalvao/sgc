import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount, RouterLinkStub} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import SubprocessoView from '@/views/SubprocessoView.vue';
import {reactive, ref} from 'vue';
import {SituacaoSubprocesso, TipoProcesso} from '@/types/tipos';
import * as processoService from '@/services/processo';
import * as useAcessoModule from '@/composables/acesso';
import {TEXTOS} from "@/constants/textos";

vi.mock('vue-router', () => ({
    useRoute: () => ({params: {codProcesso: '1', siglaUnidade: 'TEST'}, query: {}}),
}));

type SubprocessoViewVm = {
    mostrarModalAlterarDataLimite: boolean;
    mostrarModalReabrir: boolean;
    modalLembreteAberto: boolean;
    tipoReabertura: 'cadastro' | 'revisao' | null;
    notify: (mensagem: string, variante: string) => void;
    confirmarAlteracaoDataLimite: (novaData: string | null) => Promise<void>;
    abrirModalAlterarDataLimite: () => void;
    $nextTick: () => Promise<void>;
};

type SubprocessoDetalheMock = {
    codigo: number;
    situacao: SituacaoSubprocesso;
    situacaoLabel: string;
    processoDescricao: string;
    tipoProcesso: TipoProcesso;
    unidade: { codigo: number; sigla: string; nome: string };
    responsavel: {
        codigo: number;
        nome: string;
        tituloEleitoral: string;
        unidade: { codigo: number; sigla: string; nome: string };
        email: string;
        ramal: string;
    } | null;
    titular: {
        codigo: number;
        nome: string;
        tituloEleitoral: string;
        unidade: { codigo: number; sigla: string; nome: string };
        email: string;
        ramal: string;
    } | null;
    etapaAtual: number;
    prazoEtapaAtual: string;
    localizacaoAtual: string;
    isEmAndamento: boolean;
    elementosProcesso: unknown[];
    movimentacoes: unknown[];
    permissoes?: { podeEditarCadastro?: boolean };
};

const SubprocessoCardsStub = {
    template: '<div data-testid="subprocesso-cards"></div>',
    props: ['situacao', 'tipoProcesso', 'subprocesso']
};
const SubprocessoModalStub = {
    template: '<div data-testid="subprocesso-modal"></div>',
    props: ['mostrarModal'],
    emits: ['confirmar-alteracao', 'fechar-modal']
};

const toast = {
    create: vi.fn(),
    success: vi.fn(),
};
vi.mock('bootstrap-vue-next', async (importOriginal) => {
    const actual = await importOriginal<typeof import('bootstrap-vue-next')>();
    return {
        ...actual,
        useToast: () => ({
            show: vi.fn(),
            success: toast.success,
            create: toast.create,
        }),
    };
});

vi.mock('@/services/processo', () => ({
    reabrirCadastro: vi.fn(),
    enviarLembrete: vi.fn(),
}));

const fluxoSubprocessoMock = {
    alterarDataLimiteSubprocesso: vi.fn(),
    reabrirCadastro: vi.fn(),
    reabrirRevisaoCadastro: vi.fn(),
    ultimoErro: ref(null),
    limparErro: vi.fn(),
};
const subprocessoStoreMock = reactive({
    contextoEdicao: null as { detalhes: SubprocessoDetalheMock } | null,
    erroIntegracaoContexto: null as { message: string; details?: string } | null,
    obterContextoEdicaoPorProcessoEUnidade: vi.fn(),
    obterContextoEdicao: vi.fn(),
    dadosEdicaoValidos: vi.fn(),
    invalidar: vi.fn(),
    limparErroIntegracao: vi.fn(),
});

vi.mock('@/composables/useFluxoSubprocesso', () => ({
    useFluxoSubprocesso: () => fluxoSubprocessoMock
}));
vi.mock('@/stores/subprocesso', () => ({useSubprocessoStore: () => subprocessoStoreMock}));

describe('SubprocessoView.vue', () => {
    const mockSubprocesso: SubprocessoDetalheMock = {
        codigo: 10,
        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
        situacaoLabel: 'Em andamento',
        processoDescricao: 'Processo teste',
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        unidade: {
            codigo: 1,
            sigla: 'TEST',
            nome: 'Unidade teste'
        },
        responsavel: {
            codigo: 1,
            nome: 'Resp',
            tituloEleitoral: '123456789012',
            unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade teste'},
            email: 'resp@test.com',
            ramal: '123'
        },
        titular: {
            codigo: 2,
            nome: 'Titular',
            tituloEleitoral: '987654321012',
            unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade teste'},
            email: 'titular@test.com',
            ramal: '456'
        },
        etapaAtual: 1,
        prazoEtapaAtual: '2023-12-31T00:00:00',
        localizacaoAtual: 'Unidade teste',
        isEmAndamento: true,
        elementosProcesso: [],

        movimentacoes: []
    };

    const additionalStubs = {
        SubprocessoCards: SubprocessoCardsStub,
        SubprocessoModal: SubprocessoModalStub,
        ModalConfirmacao: {
            name: 'ModalConfirmacao',
            template: '<div><slot /><button :data-testid="testIdConfirmar" :disabled="okDisabled" @click="$emit(\'confirmar\')">OK</button></div>',
            props: ['modelValue', 'titulo', 'testIdConfirmar', 'okDisabled'],
            emits: ['update:modelValue', 'confirmar']
        },
        AppAlert: {
            name: 'AppAlert',
            template: '<div><button @click="$emit(\'dismissed\')">x</button></div>'
        },
        BModal: {
            template: '<div><slot /><slot name="footer" /></div>',
            props: ['modelValue', 'title'],
            emits: ['update:modelValue', 'ok']
        },
        BFormTextarea: {
            template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue'],
            emits: ['update:modelValue']
        },
        BButton: {template: '<button :disabled="disabled"><slot /></button>', props: ['disabled']},
        BDropdown: {
            template: '<div :data-testid="$attrs[\'data-testid\']"><button :disabled="disabled"><slot /></button></div>',
            props: ['disabled']
        },
        BDropdownItemButton: {
            template: '<button :data-testid="$attrs[\'data-testid\']" :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
            props: ['disabled']
        },
        EditorTextoRico: {
            props: ['modelValue'],
            emits: ['update:modelValue'],
            template: '<textarea :data-testid="$attrs[\'data-testid\']" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>'
        },
        BAlert: {
            name: 'BAlert',
            template: '<div><slot /><button @click="$emit(\'dismissed\')">x</button></div>',
            props: ['variant', 'dismissible', 'modelValue']
        },
        BSpinner: {template: '<div></div>'},
    };

    beforeEach(() => {
        vi.clearAllMocks();
        fluxoSubprocessoMock.alterarDataLimiteSubprocesso.mockResolvedValue({});
        fluxoSubprocessoMock.reabrirCadastro.mockResolvedValue(true);
        fluxoSubprocessoMock.reabrirRevisaoCadastro.mockResolvedValue(true);
        subprocessoStoreMock.contextoEdicao = null;
        subprocessoStoreMock.erroIntegracaoContexto = null;
        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade = vi.fn().mockImplementation(async () => {
            subprocessoStoreMock.contextoEdicao = {detalhes: mockSubprocesso};
            return {
                codigo: 10,
                contexto: {
                    detalhes: mockSubprocesso,
                }
            };
        });
        subprocessoStoreMock.obterContextoEdicao = vi.fn().mockImplementation(async () => {
            subprocessoStoreMock.contextoEdicao = {detalhes: mockSubprocesso};
            return {detalhes: mockSubprocesso};
        });
        subprocessoStoreMock.dadosEdicaoValidos = vi.fn().mockReturnValue(false);
        subprocessoStoreMock.limparErroIntegracao = vi.fn();
    });

    // Helper to mount component with specific setup
    const mountComponent = (
        overrideMockSubprocesso?: Partial<typeof mockSubprocesso>,
        accessOverrides: Partial<Record<string, unknown>> = {}
    ) => {
        const subprocessoToUse = overrideMockSubprocesso ? {...mockSubprocesso, ...overrideMockSubprocesso} : mockSubprocesso;

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            habilitarAlterarDataLimite: ref(true),
            habilitarReabrirCadastro: ref(true),
            habilitarReabrirRevisao: ref(true),
            habilitarEnviarLembrete: ref(true),
            mostrarAlterarDataLimite: ref(true),
            mostrarReabrirCadastro: ref(true),
            mostrarReabrirRevisao: ref(true),
            mostrarEnviarLembrete: ref(true),
            podeDisponibilizarCadastro: ref(true),
            podeEditarCadastro: ref(true),
            ...accessOverrides
        } as unknown as ReturnType<typeof useAcessoModule.useAcesso>);

        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                subprocessos: {
                    subprocessoDetalhe: null,
                },
                mapas: {
                    mapaCompleto: null,
                },
            },
            stubActions: false,
        });

        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade.mockImplementation(async () => {
            subprocessoStoreMock.contextoEdicao = {detalhes: subprocessoToUse};
            return {
                codigo: subprocessoToUse.codigo,
                contexto: {
                    detalhes: subprocessoToUse,
                }
            };
        });
        subprocessoStoreMock.obterContextoEdicao.mockResolvedValue({detalhes: subprocessoToUse});

        const wrapper = mount(SubprocessoView, {
            global: {
                plugins: [pinia],
                stubs: {
                    RouterLink: RouterLinkStub,
                    RouterView: true,
                    ...additionalStubs,
                },
            },
            props: {
                codProcesso: 1,
                siglaUnidade: 'TEST'
            }
        });

        return {wrapper, store: subprocessoStoreMock};
    };

    async function definirJustificativaReabertura(wrapper: ReturnType<typeof mount>, texto: string) {
        const editor = wrapper.find('[data-testid="inp-justificativa-reabrir"]');
        await editor.setValue(texto);
    }

    it('fetches data on mount', async () => {
        mountComponent();
        await flushPromises();

        expect(subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TEST', {recarregar: false});
    });

    it('recarrega dados ao reativar a view em keepAlive', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade.mockClear();
        subprocessoStoreMock.dadosEdicaoValidos.mockReturnValue(false);

        const hooks = ((wrapper.vm.$ as { a?: Array<() => unknown> } | undefined)?.a) ?? [];
        for (const hook of hooks) {
            await hook.call(wrapper.vm);
        }
        await flushPromises();
        expect(subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TEST', {recarregar: false});
    });

    it('não recarrega ao reativar quando o contexto atual ainda é válido', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade.mockClear();
        subprocessoStoreMock.dadosEdicaoValidos.mockReturnValue(true);

        const hooks = ((wrapper.vm.$ as { a?: Array<() => unknown> } | undefined)?.a) ?? [];
        for (const hook of hooks) {
            await hook.call(wrapper.vm);
        }
        await flushPromises();

        expect(subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade).not.toHaveBeenCalled();
    });

    it('mantem orçamento enxuto de chamadas no carregamento inicial do detalhe', async () => {
        mountComponent();
        await flushPromises();

        expect(subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledTimes(1);
    });

    it('limpa subprocessoDetalhe imediatamente ao montar para evitar dados desatualizados', async () => {
        // Simula dado desatualizado de uma visita anterior (ex: processo de mapeamento
        // com podeEditarCadastro=false), que poderia fazer SubprocessoCards carregar
        // dados incorretos na primeira renderização
        subprocessoStoreMock.contextoEdicao = {
            detalhes: {
                ...mockSubprocesso,
                codigo: 999,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                permissoes: {podeEditarCadastro: false}
            }
        };

        mountComponent();
        await flushPromises();

        // Após o carregamento, o subprocessoDetalhe deve refletir os dados corretos
        // (mockSubprocesso com codigo: 10), não os dados residuais do estado anterior (codigo: 999)
        expect(subprocessoStoreMock.contextoEdicao).not.toBeNull();
        expect(subprocessoStoreMock.contextoEdicao?.detalhes.codigo).toBe(10);
    });

    it('substitui permissões stale de ADMIN por permissões de CHEFE ao abrir o subprocesso na mesma sessão', async () => {
        subprocessoStoreMock.contextoEdicao = {
            detalhes: {
                ...mockSubprocesso,
                codigo: 999,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                permissoes: {podeEditarCadastro: false}
            }
        };

        const {wrapper} = mountComponent({
            codigo: 10,
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
            permissoes: {podeEditarCadastro: true}
        });
        await flushPromises();

        const cards = wrapper.findComponent(SubprocessoCardsStub);
        expect(cards.props('subprocesso')).toEqual(expect.objectContaining({
            codigo: 10,
            permissoes: expect.objectContaining({
                podeEditarCadastro: true
            })
        }));
    });

    it('renders components when data is available', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        await vm.$nextTick();

        expect(wrapper.find('[data-testid="header-subprocesso"]').exists()).toBe(true);
        expect(wrapper.findComponent(SubprocessoCardsStub).exists()).toBe(true);
        expect(wrapper.find('[data-testid="tbl-movimentacoes"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-subprocesso-acoes"]').exists()).toBe(true);
    });

    it('passa o subprocesso carregado ao componente de cards', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        await vm.$nextTick();

        const cards = wrapper.findComponent(SubprocessoCardsStub);
        expect(cards.props('subprocesso')).toEqual(expect.objectContaining({
            codigo: 10,
            tipoProcesso: TipoProcesso.MAPEAMENTO
        }));
    });

    it('opens date limit modal when allowed', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        await vm.$nextTick();

        await wrapper.find('[data-testid="btn-alterar-data-limite"]').trigger('click');
        await vm.$nextTick();

        expect(vm.mostrarModalAlterarDataLimite).toBe(true);
    });

    it('shows error when opening date limit modal is not allowed', async () => {
        const {wrapper} = mountComponent({}, {habilitarAlterarDataLimite: {value: false}});
        await flushPromises();
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        await vm.$nextTick();

        vm.abrirModalAlterarDataLimite();
        await vm.$nextTick();

        expect(vm.mostrarModalAlterarDataLimite).toBe(false);
    });

    it('handles date limit update confirmation', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        await vm.$nextTick();

        vm.mostrarModalAlterarDataLimite = true;
        await vm.$nextTick();

        const modal = wrapper.findComponent(SubprocessoModalStub);
        await modal.vm.$emit('confirmar-alteracao', '2024-01-01');

        await flushPromises();

        expect(fluxoSubprocessoMock.alterarDataLimiteSubprocesso).toHaveBeenCalledWith(10, {novaData: '2024-01-01'});
        expect(vm.mostrarModalAlterarDataLimite).toBe(false);
        expect(toast.create).toHaveBeenCalledWith(expect.objectContaining({
            props: expect.objectContaining({
                body: expect.stringContaining(TEXTOS.subprocesso.SUCESSO_DATA_ALTERADA)
            })
        }));
    });

    it('trata erro ao alterar data limite', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        fluxoSubprocessoMock.alterarDataLimiteSubprocesso.mockRejectedValue(new Error('Falha'));

        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        vm.mostrarModalAlterarDataLimite = true;
        const modal = wrapper.findComponent(SubprocessoModalStub);
        await modal.vm.$emit('confirmar-alteracao', '2024-01-01');
        await flushPromises();

        expect(fluxoSubprocessoMock.alterarDataLimiteSubprocesso).toHaveBeenCalled();
    });

    it('reabre cadastro com sucesso', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();

        await wrapper.find('[data-testid="btn-reabrir-cadastro"]').trigger('click');
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        await vm.$nextTick();

        expect(vm.tipoReabertura).toBe('cadastro');
        expect(vm.mostrarModalReabrir).toBe(true);

        await definirJustificativaReabertura(wrapper, 'Erro no preenchimento');

        const btn = wrapper.find('[data-testid="btn-confirmar-reabrir"]');
        await btn.trigger('click');
        await flushPromises();

        expect(fluxoSubprocessoMock.reabrirCadastro).toHaveBeenCalledWith(10, 'Erro no preenchimento');
    });

    it('reabre revisão com sucesso', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();

        await wrapper.find('[data-testid="btn-reabrir-revisao"]').trigger('click');
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        expect(vm.tipoReabertura).toBe('revisao');

        await definirJustificativaReabertura(wrapper, 'Revisão incompleta');

        const btn = wrapper.find('[data-testid="btn-confirmar-reabrir"]');
        await btn.trigger('click');
        await flushPromises();

        expect(fluxoSubprocessoMock.reabrirRevisaoCadastro).toHaveBeenCalledWith(10, 'Revisão incompleta');
    });

    it('impede reabertura se justificativa vazia e exibe pendência contextual', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();

        await wrapper.find('[data-testid="btn-reabrir-cadastro"]').trigger('click');

        const btn = wrapper.find('[data-testid="btn-confirmar-reabrir"]');
        await btn.trigger('click');
        expect(wrapper.find('[data-testid="txt-reabertura-pendencia-justificativa"]').text())
            .toContain('Informe a justificativa para reabrir.');
        expect(fluxoSubprocessoMock.reabrirCadastro).not.toHaveBeenCalled();
    });

    it('trata erro na API ao reabrir', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        fluxoSubprocessoMock.reabrirCadastro.mockResolvedValue(false);

        await wrapper.find('[data-testid="btn-reabrir-cadastro"]').trigger('click');

        await definirJustificativaReabertura(wrapper, 'Justificativa');

        const btn = wrapper.find('[data-testid="btn-confirmar-reabrir"]');
        await btn.trigger('click');
        await flushPromises();

        expect(fluxoSubprocessoMock.reabrirCadastro).toHaveBeenCalled();
    });

    it('envia lembrete com sucesso', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();

        await wrapper.find('[data-testid="btn-enviar-lembrete"]').trigger('click');
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        await vm.$nextTick();

        const btn = wrapper.find('[data-testid="btn-confirmar-enviar-lembrete"]');
        await btn.trigger('click');
        await flushPromises();

        expect(processoService.enviarLembrete).toHaveBeenCalledWith(1, 1);
        expect(toast.create).toHaveBeenCalledWith(expect.objectContaining({
            props: expect.objectContaining({
                body: expect.stringContaining(TEXTOS.subprocesso.SUCESSO_LEMBRETE_ENVIADO)
            })
        }));
    });

    it('trata erro ao enviar lembrete', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        vi.mocked(processoService.enviarLembrete).mockRejectedValue(new Error('Erro'));

        await wrapper.find('[data-testid="btn-enviar-lembrete"]').trigger('click');
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        await vm.$nextTick();

        const btn = wrapper.find('[data-testid="btn-confirmar-enviar-lembrete"]');
        await btn.trigger('click');
        await flushPromises();

        expect(processoService.enviarLembrete).toHaveBeenCalled();
    });

    it('deve gerenciar notificações de erro, alertas de sistema e estados de modais de confirmação', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        const vm = wrapper.vm as unknown as SubprocessoViewVm;

        // Gerenciamento de notificações
        vm.notify("Msg", "info");
        await vm.$nextTick();
        const appAlert = wrapper.findComponent({name: 'AppAlert'});
        if (appAlert.exists()) await appAlert.vm.$emit('dismissed');

        // Gerenciamento de erros de subprocesso
        subprocessoStoreMock.contextoEdicao = null;
        subprocessoStoreMock.erroIntegracaoContexto = {message: "Erro"};
        await vm.$nextTick();
        const bAlert = wrapper.findComponent({name: 'BAlert'});
        if (bAlert.exists()) await bAlert.vm.$emit('dismissed');
        expect(subprocessoStoreMock.limparErroIntegracao).toHaveBeenCalled();

        // Atualização de estados de v-model nos modais
        const modalsComp = wrapper.findAllComponents({name: 'ModalConfirmacao'});
        for (const modal of modalsComp) {
            await modal.vm.$emit('update:modelValue', true);
        }
        expect(vm.modalLembreteAberto).toBe(true);

        // Validação de entrada nula para alteração de data limite
        expect(await vm.confirmarAlteracaoDataLimite(null)).toBeUndefined();

        // Exibição de toast pendente ao montar componente
        const {useToastStore} = await import("@/stores/toast");
        const toastStore = useToastStore();
        toastStore.setPending("Msg");
        mountComponent();
    });

    it('formata corretamente o tipo de responsabilidade', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        const vm = wrapper.vm as any;

        expect(vm.formatTipoResponsabilidade(null)).toBe('');
        expect(vm.formatTipoResponsabilidade({tipo: 'Titular'})).toBe('Titular');
        expect(vm.formatTipoResponsabilidade({
            tipo: 'Substituição',
            dataFim: '2023-12-31'
        })).toContain('Substituição (até 31/12/2023)');
        expect(vm.formatTipoResponsabilidade({
            tipo: 'Atribuição temporária',
            dataFim: '2023-12-31'
        })).toContain('Atrib. temporária (até 31/12/2023)');
    });

    it('exibe estado de "Não Encontrado" quando erroNaoEncontrado é true', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        subprocessoStoreMock.contextoEdicao = null;
        subprocessoStoreMock.erroIntegracaoContexto = null;
        (wrapper.vm as unknown as { erroNaoEncontrado: boolean }).erroNaoEncontrado = true;
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).toContain(TEXTOS.subprocesso.NAO_ENCONTRADO_TITULO);
    });

    it('exibe erro de integração do store', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        subprocessoStoreMock.contextoEdicao = null;
        subprocessoStoreMock.erroIntegracaoContexto = {
            mensagem: 'Erro de Banco',
            detalhes: 'Connection timeout'
        } as any;
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).toContain('Erro de Banco');
        expect(wrapper.text()).toContain('Detalhes: Connection timeout');
    });

    it('renderiza EmptyState quando não há movimentações', async () => {
        const {wrapper} = mountComponent({movimentacoes: []});
        await flushPromises();

        expect(wrapper.find('[data-testid="empty-state-movimentacoes"]').exists()).toBe(true);
    });

    it('exibe erro de integração sem detalhes', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        subprocessoStoreMock.contextoEdicao = null;
        subprocessoStoreMock.erroIntegracaoContexto = {
            mensagem: 'Erro de Banco',
        } as any;
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).toContain('Erro de Banco');
        expect(wrapper.text()).not.toContain('Detalhes:');
    });

    it('formata data simples', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        const vm = wrapper.vm as any;
        expect(vm.formatDataSimples('2026-01-01')).toBe('01/01/2026');
        expect(vm.formatDataSimples(null)).toBe('');
    });

    it('reabre revisão com erro na API', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        fluxoSubprocessoMock.reabrirRevisaoCadastro.mockResolvedValue(false);

        await wrapper.find('[data-testid="btn-reabrir-revisao"]').trigger('click');
        await definirJustificativaReabertura(wrapper, 'Justificativa');
        const btn = wrapper.find('[data-testid="btn-confirmar-reabrir"]');
        await btn.trigger('click');
        await flushPromises();

        expect(fluxoSubprocessoMock.reabrirRevisaoCadastro).toHaveBeenCalled();
    });

    it('deve lidar com subprocesso sem tipoProcesso', async () => {
        const {wrapper} = mountComponent({
            tipoProcesso: undefined as any,
        });
        await flushPromises();
        const cards = wrapper.findComponent(SubprocessoCardsStub);
        expect(cards.props('tipoProcesso')).toBe(TipoProcesso.MAPEAMENTO);
    });
});
