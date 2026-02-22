import {describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import Subprocesso from '@/views/processo/SubprocessoDetalheView.vue';
import {useSubprocessosStore} from '@/stores/subprocessos';
import {useFeedbackStore} from '@/stores/feedback';
import {BAlert, BSpinner} from 'bootstrap-vue-next';
import * as useAcessoModule from '@/composables/useAcesso';

// Mock child components to avoid rendering them and their dependencies
const SubprocessoHeaderStub = { template: '<div />' };
const SubprocessoCardsStub = { template: '<div />' };
const SubprocessoModalStub = { template: '<div />' };
const TabelaMovimentacoesStub = { template: '<div />' };
const ModalConfirmacaoStub = {
    template: '<div><slot /></div>',
    props: ['modelValue', 'titulo', 'loading', 'okDisabled'],
    emits: ['update:modelValue', 'confirmar']
};

describe('Subprocesso Coverage', () => {
    it('renders loading state when no data and no error', () => {
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeAlterarDataLimite: { value: false },
            podeReabrirCadastro: { value: false },
            podeReabrirRevisao: { value: false },
            podeEnviarLembrete: { value: false },
            podeDisponibilizarCadastro: { value: false },
            podeEditarCadastro: { value: false },
            podeVisualizarMapa: { value: true },
        } as any);

        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                subprocessos: {
                    subprocessoDetalhe: null,
                    lastError: null
                }
            }
        });

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeVisualizarMapa: { value: true },
        } as any);

        const wrapper = mount(Subprocesso, {
            global: {
                plugins: [pinia],
                stubs: {
                    SubprocessoHeader: SubprocessoHeaderStub,
                    SubprocessoCards: SubprocessoCardsStub,
                    TabelaMovimentacoes: TabelaMovimentacoesStub,
                    SubprocessoModal: SubprocessoModalStub,
                    ModalConfirmacao: ModalConfirmacaoStub
                }
            },
            props: {
                codProcesso: 1,
                siglaUnidade: 'TEST'
            }
        });

        expect(wrapper.findComponent(BSpinner).exists()).toBe(true);
        expect(wrapper.text()).toContain('Carregando informações da unidade...');
    });

    it('renders error state when lastError is present', () => {
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeAlterarDataLimite: { value: false },
            podeReabrirCadastro: { value: false },
            podeReabrirRevisao: { value: false },
            podeEnviarLembrete: { value: false },
            podeDisponibilizarCadastro: { value: false },
            podeEditarCadastro: { value: false },
            podeVisualizarMapa: { value: true },
        } as any);

        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                subprocessos: {
                    subprocessoDetalhe: null,
                    lastError: { message: 'Erro teste' }
                }
            }
        });

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeVisualizarMapa: { value: true },
        } as any);

        const wrapper = mount(Subprocesso, {
            global: {
                plugins: [pinia],
                stubs: {
                    SubprocessoHeader: SubprocessoHeaderStub,
                    SubprocessoCards: SubprocessoCardsStub,
                    TabelaMovimentacoes: TabelaMovimentacoesStub,
                    SubprocessoModal: SubprocessoModalStub,
                    ModalConfirmacao: ModalConfirmacaoStub
                }
            },
            props: {
                codProcesso: 1,
                siglaUnidade: 'TEST'
            }
        });

        expect(wrapper.findComponent(BAlert).exists()).toBe(true);
        expect(wrapper.text()).toContain('Erro teste');
    });

    it('confirmarAlteracaoDataLimite returns early if novaData is empty', async () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                subprocessos: {
                    subprocessoDetalhe: {
                        unidade: { codigo: 1 }
                    }
                }
            }
        });
        const store = useSubprocessosStore(pinia);

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeAlterarDataLimite: { value: true },
            podeReabrirCadastro: { value: false },
            podeReabrirRevisao: { value: false },
            podeEnviarLembrete: { value: false },
            podeDisponibilizarCadastro: { value: false },
            podeEditarCadastro: { value: false },
            podeVisualizarMapa: { value: true },
        } as any);

        const wrapper = mount(Subprocesso, {
            global: {
                plugins: [pinia],
                stubs: {
                    SubprocessoHeader: SubprocessoHeaderStub,
                    SubprocessoCards: SubprocessoCardsStub,
                    TabelaMovimentacoes: TabelaMovimentacoesStub,
                    SubprocessoModal: SubprocessoModalStub,
                    ModalConfirmacao: ModalConfirmacaoStub
                }
            },
            props: { codProcesso: 1, siglaUnidade: 'TEST' }
        });

        // Trigger manually
        await (wrapper.vm as any).confirmarAlteracaoDataLimite('');

        expect(store.alterarDataLimiteSubprocesso).not.toHaveBeenCalled();
    });

    it('confirmarReabertura returns early if justification is empty', async () => {
         const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                subprocessos: {
                    subprocessoDetalhe: {
                        unidade: { codigo: 1 },
                        movimentacoes: [],
                    }
                }
            }
        });

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeAlterarDataLimite: { value: false },
            podeReabrirCadastro: { value: false },
            podeReabrirRevisao: { value: false },
            podeEnviarLembrete: { value: false },
            podeDisponibilizarCadastro: { value: false },
            podeEditarCadastro: { value: false },
            podeVisualizarMapa: { value: true },
        } as any);

        const store = useSubprocessosStore(pinia);
        // Mock returning an ID to set codSubprocesso
        (store.buscarSubprocessoPorProcessoEUnidade as any).mockResolvedValue(123);
        const feedbackStore = useFeedbackStore(pinia);

        const wrapper = mount(Subprocesso, {
            global: {
                plugins: [pinia],
                stubs: {
                    SubprocessoHeader: SubprocessoHeaderStub,
                    SubprocessoCards: SubprocessoCardsStub,
                    TabelaMovimentacoes: TabelaMovimentacoesStub,
                    SubprocessoModal: SubprocessoModalStub,
                    ModalConfirmacao: ModalConfirmacaoStub,
                    BFormTextarea: { template: '<textarea />' }
                }
            },
            props: { codProcesso: 1, siglaUnidade: 'TEST' }
        });

        // Wait for onMounted to set codSubprocesso
        await (wrapper.vm as any).$nextTick();

        // Ensure codSubprocesso is set (though mocked return above should handle it, we can force it if needed)
        // (wrapper.vm as any).codSubprocesso = 123;

        // Call with empty justification
        (wrapper.vm as any).justificativaReabertura = '';
        await (wrapper.vm as any).confirmarReabertura();

        expect(feedbackStore.show).toHaveBeenCalledWith("Erro", "Justificativa é obrigatória.", "danger");
    });
});
