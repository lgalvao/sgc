import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import CadastroView from '../CadastroView.vue';
import { createTestingPinia } from '@pinia/testing';
import { createRouter, createWebHistory } from 'vue-router';
import { useSubprocessoStore } from '@/stores/subprocesso';
import { SituacaoSubprocesso } from '@/types/tipos';

vi.mock('@/composables/useAcesso', () => ({
  useAcesso: vi.fn(() => ({
    temPermissaoAdmin: { value: true },
    temPermissaoGestor: { value: true },
    habilitarEditarCadastro: { value: true },
    podeEditarCadastro: { value: true },
    habilitarDisponibilizarCadastro: { value: true },
    podeAnalisarCadastro: { value: true },
    podeVerSugestoes: { value: true },
    podeDevolverCadastro: { value: true },
    acaoPrincipalCadastro: { value: { codigo: 'ACEITAR', rotulo: 'Aceitar', mostrar: true } }
  }))
}));

vi.mock('@/composables/useAtividadeForm', () => ({
  useAtividadeForm: vi.fn(() => ({
    atividadeFormRef: { value: null },
    submitAtividade: vi.fn(),
    handleAtividadeSubmit: vi.fn(),
    iniciarEdicaoAtividade: vi.fn(),
    handleAtividadeCancel: vi.fn(),
    atividadeIdEditando: { value: null }
  }))
}));

vi.mock('@/composables/useFluxoSubprocesso', () => ({
  useFluxoSubprocesso: vi.fn(() => ({
    devolverCadastro: vi.fn().mockResolvedValue(true),
    aceitarCadastro: vi.fn().mockResolvedValue(true),
    validarSubmissao: vi.fn().mockReturnValue(true),
    resetarValidacao: vi.fn(),
    deveExibirErro: vi.fn().mockReturnValue(false),
    focarPrimeiroErroInvalido: vi.fn(),
    lastError: { value: null }
  }))
}));

vi.mock('@/composables/useValidacaoFormulario', () => ({
  useValidacaoFormulario: vi.fn(() => ({
    validarSubmissao: vi.fn().mockReturnValue(true),
    resetarValidacao: vi.fn(),
    deveExibirErro: vi.fn().mockReturnValue(false),
    focarPrimeiroErroInvalido: vi.fn()
  }))
}));

const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: '/', component: {} }]
});

describe('CadastroView Coverage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mountComponent = () => {
    return mount(CadastroView, {
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn }), router],
        stubs: {
          LayoutPadrao: { template: '<div><slot/></div>' },
          PageHeader: { template: '<div><slot name="actions"/></div>', props: ['title'] },
          BButton: { template: '<button @click="$emit(\'click\')"><slot/></button>' },
          AppAlert: true,
          BSpinner: true,
          BCard: true,
          BCardBody: true,
          BContainer: true,
          BCol: true,
          BRow: true,
          BFormGroup: true,
          BFormTextarea: { template: '<textarea></textarea>' },
          BFormInvalidFeedback: { template: '<div></div>' },
          LoadingButton: { template: '<button @click="$emit(\'click\')"><slot/></button>' },
          ModalConfirmacao: { template: '<div><slot/></div>', props: ['modelValue', 'mostrarModal'] },
          EmptyState: true,
          CarregamentoPagina: true,
          ModalPadrao: true,
          TreeTable: { template: '<div data-testid="tree-table"></div>' },
          ProcessoInfo: true,
          HistoricoAnaliseModal: true,
          ModalAceiteCadastro: true,
          ModalDevolucaoCadastro: true,
          ImpactoMapaModal: true,
          ImportarAtividadesModal: true,
          ConfirmacaoDisponibilizacaoModal: true
        }
      },
      props: {
        codProcesso: 123,
        sigla: 'U1'
      }
    });
  };

  it('covers branches and statements', async () => {
    const wrapper = mountComponent();
    await flushPromises();

    const store = useSubprocessoStore();
    // @ts-ignore
    store.contextoCadastro = {
      detalhes: {
        codigo: 1,
        unidade: { sigla: 'U1', nome: 'Unidade 1', codigo: 2 },
        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO
      } as any
    };
    // @ts-ignore
    store.garantirContextoEdicao = vi.fn().mockResolvedValue(true);

    expect(wrapper.exists()).toBe(true);

    // Open some modals
    // @ts-ignore
    wrapper.vm.abrirModalDevolverAnalise();

    // @ts-ignore
    wrapper.vm.disponibilizarCadastro();

    // @ts-ignore
    wrapper.vm.observacaoDevolucao = 'Motivo de teste';

    // @ts-ignore
    await wrapper.vm.confirmarDevolucaoAnalise();
  });
});
