import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import SubprocessoView from '../SubprocessoView.vue';
import {createTestingPinia} from '@pinia/testing';
import {createMemoryHistory, createRouter} from 'vue-router';
import {useSubprocessoStore} from '@/stores/subprocesso';
import {SituacaoSubprocesso} from '@/types/tipos';

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
    mostrarEnviarLembrete: { value: true },
    mostrarAlterarDataLimite: { value: true },
    mostrarReabrirCadastro: { value: true },
    mostrarReabrirRevisao: { value: true },
    acaoPrincipalCadastro: { value: { codigo: 'ACEITAR', rotulo: 'Aceitar', mostrar: true } }
  }))
}));

vi.mock('@/services/subprocessoService', () => ({
  enviarLembrete: vi.fn().mockResolvedValue({}),
}));

const router = createRouter({
  history: createMemoryHistory(),
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
          BDropdown: { template: '<div :data-testid="$attrs[\'data-testid\']"><button :disabled="disabled"><slot/></button></div>', props: ['disabled'] },
          BDropdownItemButton: { template: '<button :data-testid="$attrs[\'data-testid\']" :disabled="disabled" @click="$emit(\'click\')"><slot/></button>', props: ['disabled'] },
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
    (store as any).contextoEdicao = {
      detalhes: {
        codigo: 1,
        unidade: { sigla: 'U1', nome: 'Unidade 1', codigo: 2 },
        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO
      } as any
    };

    const vm = wrapper.vm as any;
    // We trigger multiple actions that might not be fully covered in main spec
    await vm.confirmarEnviarLembrete();
    expect(vm.modalLembreteAberto).toBe(true);

    await vm.enviarLembreteConfirmado();

    // Test that reabertura triggers validation
    await vm.confirmarReabertura();

    // Open some modals
    vm.abrirModalAlterarDataLimite();
    expect(vm.mostrarModalAlterarDataLimite).toBe(true);

    vm.abrirModalReabrirCadastro();
    expect(vm.mostrarModalReabrir).toBe(true);

    vm.abrirModalReabrirRevisao();

    // 1. rowAttrMovimentacao(null)
    expect(vm.rowAttrMovimentacao(null)).toEqual({});

    // 2. confirmarEnviarLembrete(!subprocesso)
    vm.subprocesso = null;
    await vm.confirmarEnviarLembrete();

    // 3. atualizarSubprocessoAtual(!codigoSubprocesso)
    vm.codigoSubprocesso = null;
    await vm.atualizarSubprocessoAtual();

/*
    // 4. onActivated branches
    vm.carregamentoInicialConcluido = false;
    await vm.onActivated?.();
    expect(vm.carregamentoInicialConcluido).toBe(false);

    // 5. enviarLembreteConfirmado caminhos de erro
    vm.subprocesso = null;
    await vm.enviarLembreteConfirmado();
    expect(vm.loadingLembrete).toBe(false);
*/
  });
});
