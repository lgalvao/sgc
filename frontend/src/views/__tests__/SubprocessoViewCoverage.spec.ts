import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import SubprocessoView from '../SubprocessoView.vue';
import {createTestingPinia} from '@pinia/testing';
import {createMemoryHistory, createRouter} from 'vue-router';
import {useSubprocessoStore} from '@/stores/subprocesso';
import {useToastStore} from '@/stores/toast';
import {SituacaoSubprocesso} from '@/types/tipos';
import * as processoService from '@/services/processoService';

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

vi.mock('@/services/processoService', () => ({
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

  const mountComponent = (props: Partial<{ codProcesso: number; siglaUnidade: string; codSubprocesso?: number }> = {}) => {
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
        siglaUnidade: 'U1',
        ...props
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
        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
        prazoEtapaAtual: '2024-01-01T00:00:00',
        ultimaDataLimiteSubprocesso: '2024-02-01T00:00:00',
        movimentacoes: [{codigo: 7, dataHora: '2024-01-02T00:00:00'}],
        titular: {nome: 'Titular', ramal: '123', email: 'titular@teste.com'},
        responsavel: {usuario: {nome: 'Resp', ramal: '456', email: 'resp@teste.com'}, tipo: 'Atribuição temporária', dataFim: '2024-03-01T00:00:00'}
      } as any
    };

    const vm = wrapper.vm as any;
    expect(vm.formatTipoResponsabilidade(null)).toBe('');
    expect(vm.formatTipoResponsabilidade({tipo: 'Substituição', dataFim: '2024-03-01T00:00:00'})).toContain('até 01/03/2024');
    expect(vm.formatTipoResponsabilidade({tipo: 'Atribuição temporária', dataFim: '2024-03-01T00:00:00'})).toContain('Atrib. temporária');
    expect(vm.formatTipoResponsabilidade({tipo: 'Outra'})).toBe('Outra');
    expect(vm.rowAttrMovimentacao(null)).toEqual({});
    expect(vm.rowAttrMovimentacao({codigo: 7})).toEqual({'data-testid': 'row-movimentacao-7'});
    vm.fecharModalAlterarDataLimite();
    vm.abrirModalAlterarDataLimite();
    expect(vm.mostrarModalAlterarDataLimite).toBe(true);
    vm.fecharModalAlterarDataLimite();
    expect(vm.mostrarModalAlterarDataLimite).toBe(false);
    vm.abrirModalReabrirCadastro();
    expect(vm.mostrarModalReabrir).toBe(true);
    vm.fecharModalReabrir();
    expect(vm.mostrarModalReabrir).toBe(false);
    vm.abrirModalReabrirRevisao();
    expect(vm.mostrarModalReabrir).toBe(true);

    vm.codigoSubprocesso = 10;
    await vm.atualizarSubprocessoAtual();
    expect((store as any).garantirContextoEdicao).toHaveBeenCalledWith(10, true);

    await vm.confirmarEnviarLembrete();
    expect(vm.modalLembreteAberto).toBe(true);
    await vm.enviarLembreteConfirmado();
    expect(processoService.enviarLembrete).toHaveBeenCalledWith(123, 2);

    (store as any).contextoEdicao = null;
    await vm.confirmarEnviarLembrete();
    vm.loadingLembrete = true;
    await vm.enviarLembreteConfirmado();

    await vm.confirmarReabertura();
    expect(vm.mostrarModalReabrir).toBe(true);
  });

  it('usa o carregamento direto quando a rota já traz o codigo do subprocesso', async () => {
    const wrapper = mountComponent({codSubprocesso: 10});
    const store = useSubprocessoStore();
    (store as any).garantirContextoEdicao.mockResolvedValue({
      detalhes: {
        codigo: 10,
        unidade: { sigla: 'U1', nome: 'Unidade 1', codigo: 2 },
        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO
      }
    });
    await flushPromises();

    expect((store as any).garantirContextoEdicao).toHaveBeenCalledWith(10, true);
    expect(wrapper.exists()).toBe(true);
  });

  it('cobre toast pendente, watch de props e retorno antecipado de ativação', async () => {
    const toastStore = useToastStore();
    (toastStore as any).setPending?.('Mensagem de teste');

    const wrapper = mountComponent();
    const vm = wrapper.vm as any;
    const hooks = ((wrapper.vm.$ as {a?: Array<() => unknown>} | undefined)?.a) ?? [];
    if (hooks[0]) {
      await hooks[0].call(wrapper.vm);
    }
    await flushPromises();

    await wrapper.setProps({codProcesso: 456});
    await flushPromises();

    vm.codigoSubprocesso = null;
    await vm.atualizarSubprocessoAtual();
  });
});
