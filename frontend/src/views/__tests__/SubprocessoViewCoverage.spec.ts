import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createTestingPinia } from '@pinia/testing';
import SubprocessoView from '@/views/SubprocessoView.vue';
import { useSubprocessosStore } from '@/stores/subprocessos';
import { useFeedbackStore } from '@/stores/feedback';
import { BSpinner, BAlert } from 'bootstrap-vue-next';

// Mock child components to avoid rendering them and their dependencies
const SubprocessoHeaderStub = { template: '<div />' };
const SubprocessoCardsStub = { template: '<div />', props: ['permissoes'] };
const SubprocessoModalStub = { template: '<div />' };
const TabelaMovimentacoesStub = { template: '<div />' };
const ModalConfirmacaoStub = {
    template: '<div><slot /></div>',
    props: ['modelValue', 'titulo', 'loading', 'okDisabled'],
    emits: ['update:modelValue', 'confirmar']
};

describe('SubprocessoView Coverage', () => {
    it('renders loading state when no data and no error', () => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                subprocessos: {
                    subprocessoDetalhe: null,
                    lastError: null
                }
            }
        });

        const wrapper = mount(SubprocessoView, {
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
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                subprocessos: {
                    subprocessoDetalhe: null,
                    lastError: { message: 'Erro teste' }
                }
            }
        });

        const wrapper = mount(SubprocessoView, {
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
                        unidade: { codigo: 1 },
                        permissoes: { podeAlterarDataLimite: true }
                    }
                }
            }
        });
        const store = useSubprocessosStore(pinia);

        const wrapper = mount(SubprocessoView, {
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
                        permissoes: {
                            podeAlterarDataLimite: false,
                            podeReabrirCadastro: false,
                            podeReabrirRevisao: false,
                            podeEnviarLembrete: false
                        }
                    }
                }
            }
        });

        const store = useSubprocessosStore(pinia);
        // Mock returning an ID to set codSubprocesso
        (store.buscarSubprocessoPorProcessoEUnidade as any).mockResolvedValue(123);
        const feedbackStore = useFeedbackStore(pinia);

        const wrapper = mount(SubprocessoView, {
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
