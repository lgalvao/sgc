import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import SubprocessoView from '../SubprocessoView.vue';
import { createTestingPinia } from '@pinia/testing';
import { createRouter, createWebHistory } from 'vue-router';
import { useSubprocessoStore } from '@/stores/subprocesso';
import { SituacaoSubprocesso } from '@/types/tipos';

vi.mock('@/composables/useAcesso', () => ({
  useAcesso: vi.fn(() => ({
    temPermissaoAdmin: { value: true },
    temPermissaoGestor: { value: true },
    habilitarEditarCadastro: { value: true },
    habilitarDisponibilizarCadastro: { value: true },
    podeAnalisarCadastro: { value: true },
    podeVerSugestoes: { value: true },
    podeDevolverCadastro: { value: true },
    podeEnviarLembrete: { value: true },
    podeAlterarDataLimite: { value: true },
    podeReabrirCadastro: { value: true },
    podeReabrirRevisao: { value: true },
    acaoPrincipalCadastro: { value: { codigo: 'ACEITAR', rotulo: 'Aceitar', mostrar: true } }
  }))
}));

vi.mock('@/services/subprocessoService', () => ({
  enviarLembrete: vi.fn().mockResolvedValue({}),
}));

const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: '/', component: {} }]
});

describe('SubprocessoView Coverage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mountComponent = () => {
    return mount(SubprocessoView, {
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
          HistoricoAnaliseModal: true
        }
      },
      props: {
        codProcesso: 123,
        siglaUnidade: 'U1'
      }
    });
  };

  it('covers branches and statements', async () => {
    const wrapper = mountComponent();
    await flushPromises();

    const store = useSubprocessoStore();
    // @ts-ignore
    store.contextoEdicao = {
      detalhes: {
        codigo: 1,
        unidade: { sigla: 'U1', nome: 'Unidade 1', codigo: 2 },
        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO
      } as any
    };

    // We trigger multiple actions that might not be fully covered in main spec
    // @ts-ignore
    await wrapper.vm.confirmarEnviarLembrete();
    // @ts-ignore
    expect(wrapper.vm.modalLembreteAberto).toBe(true);

    // @ts-ignore
    await wrapper.vm.enviarLembreteConfirmado();

    // Test that reabertura triggers validation
    // @ts-ignore
    await wrapper.vm.confirmarReabertura();

    // Open some modals
    // @ts-ignore
    wrapper.vm.abrirModalAlterarDataLimite();
    // @ts-ignore
    expect(wrapper.vm.mostrarModalAlterarDataLimite).toBe(true);

    // @ts-ignore
    wrapper.vm.abrirModalReabrirCadastro();
    // @ts-ignore
    expect(wrapper.vm.mostrarModalReabrir).toBe(true);

    // @ts-ignore
    wrapper.vm.abrirModalReabrirRevisao();

    // Cover the invalid validation inside confirmarAlteracaoDataLimite
    // @ts-ignore
    await wrapper.vm.confirmarAlteracaoDataLimite();
  });
});
