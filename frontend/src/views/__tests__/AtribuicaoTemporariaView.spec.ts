import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount, flushPromises} from '@vue/test-utils';
import AtribuicaoTemporariaView from '../AtribuicaoTemporariaView.vue';
import {buscarUnidadePorCodigo} from '@/services/unidadeService';
import {criarAtribuicaoTemporaria} from '@/services/atribuicaoTemporariaService';
import {createMemoryHistory, createRouter} from 'vue-router';
import {createPinia, setActivePinia} from 'pinia';
import type {Unidade} from '@/types/tipos';

type AtribuicaoTemporariaVm = {
  unidade: Unidade | null;
  erroUsuario: string;
  erroFormulario: string;
  criarAtribuicao: () => Promise<void>;
  usuarioSelecionado: string | null;
  termoUsuario: string;
  dataInicio: string;
  dataTermino: string;
  justificativa: string;
  notify: (mensagem: string, variante: string) => void;
  $nextTick: () => Promise<void>;
};

const unidadeMinima: Unidade = {codigo: 1, sigla: 'TESTE', nome: 'Unidade de Teste'};

vi.mock('@/services/unidadeService', () => ({
  buscarUnidadePorCodigo: vi.fn(),
}));

vi.mock('@/services/usuarioService', () => ({
  pesquisarUsuarios: vi.fn(),
}));

vi.mock('@/services/atribuicaoTemporariaService', () => ({
  criarAtribuicaoTemporaria: vi.fn(),
}));

const mockNotify = vi.fn();
const mockClear = vi.fn();
vi.mock('@/composables/useNotification', () => ({
  useNotification: () => ({
    notificacao: { value: null },
    notify: mockNotify,
    clear: mockClear,
  }),
}));

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/', component: { template: '<div></div>' } }],
});

const mountOptions = {
  props: { codUnidade: 1 },
  global: {
    plugins: [router, createPinia()],
    stubs: {
      LayoutPadrao: {
        template: '<div><slot></slot></div>',
      },
      PageHeader: {
        template: '<div><slot></slot><slot name="actions"></slot></div>',
      },
      InputData: {
        name: 'InputData',
        template: '<input type="date" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
        props: ['modelValue']
      },
      LoadingButton: {
        props: ['disabled'],
        template: '<button :disabled="disabled" @click="$emit(\'click\')">LoadingButton</button>'
      },
      BuscadorUsuarios: {
        name: 'BuscadorUsuarios',
        props: ['termo', 'selecionado'],
        template: '<div></div>',
        methods: {
            limparResultadosPesquisaUsuarios() {}
        }
      },
      BFormTextarea: {
        name: 'BFormTextarea',
        props: ['modelValue'],
        template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>'
      },
      AppAlert: {
        name: 'AppAlert',
        template: '<div><button @click="$emit(\'dismissed\')">x</button></div>'
      }
    },
  },
};

describe('AtribuicaoTemporariaView', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('deve carregar a unidade corretamente no onMounted', async () => {
    vi.mocked(buscarUnidadePorCodigo).mockResolvedValue({
      codigo: 1,
      sigla: 'TESTE-UNIDADE',
      nome: 'Unidade de Teste'
    });

    const wrapper = mount(AtribuicaoTemporariaView, mountOptions);
    const vm = wrapper.vm as unknown as AtribuicaoTemporariaVm;
    await flushPromises();
    
    expect(buscarUnidadePorCodigo).toHaveBeenCalledWith(1);
    expect(vm.unidade).toBeDefined();
    expect(vm.unidade?.sigla).toBe('TESTE-UNIDADE');
  });

  it('deve lidar com erro ao carregar unidade', async () => {
    vi.mocked(buscarUnidadePorCodigo).mockRejectedValue(new Error('Erro de API'));

    const wrapper = mount(AtribuicaoTemporariaView, mountOptions);
    const vm = wrapper.vm as unknown as AtribuicaoTemporariaVm;
    await flushPromises();

    expect(vm.erroUsuario).toContain('Falha ao carregar dados da unidade');
  });

  it('deve validar formulário vazio', async () => {
    vi.mocked(buscarUnidadePorCodigo).mockResolvedValue(unidadeMinima);
    const wrapper = mount(AtribuicaoTemporariaView, mountOptions);
    const vm = wrapper.vm as unknown as AtribuicaoTemporariaVm;
    await flushPromises();

    await vm.criarAtribuicao();

    expect(mockNotify).not.toHaveBeenCalledWith(expect.any(String), 'danger');
    expect(criarAtribuicaoTemporaria).not.toHaveBeenCalled();
  });

  it('deve manter o botão de criar habilitado para permitir validação contextual', async () => {
    vi.mocked(buscarUnidadePorCodigo).mockResolvedValue(unidadeMinima);
    const wrapper = mount(AtribuicaoTemporariaView, mountOptions);
    const vm = wrapper.vm as unknown as AtribuicaoTemporariaVm;
    await flushPromises();

    const botaoCriar = wrapper.find('[data-testid="cad-atribuicao__btn-criar-atribuicao"]');
    expect((botaoCriar.element as HTMLButtonElement).disabled).toBe(false);

    vm.usuarioSelecionado = '123';
    vm.dataInicio = '2025-01-01';
    vm.dataTermino = '2025-12-31';
    vm.justificativa = 'Teste de justificativa';
    await flushPromises();

    expect((botaoCriar.element as HTMLButtonElement).disabled).toBe(false);
  });

  it('deve criar atribuicao com sucesso', async () => {
    vi.mocked(buscarUnidadePorCodigo).mockResolvedValue(unidadeMinima);
    vi.mocked(criarAtribuicaoTemporaria).mockResolvedValue();

    const wrapper = mount(AtribuicaoTemporariaView, mountOptions);
    const vm = wrapper.vm as unknown as AtribuicaoTemporariaVm;
    await flushPromises();

    // Preenchendo o formulário
    vm.usuarioSelecionado = '999';
    vm.dataInicio = '2025-01-01';
    vm.dataTermino = '2025-12-31';
    vm.justificativa = 'Teste de justificativa';

    await vm.criarAtribuicao();
    await flushPromises();

    expect(criarAtribuicaoTemporaria).toHaveBeenCalledWith(1, {
      tituloEleitoralUsuario: '999',
      dataInicio: '2025-01-01',
      dataTermino: '2025-12-31',
      justificativa: 'Teste de justificativa'
    });
    expect(mockNotify).toHaveBeenCalledWith(expect.any(String), 'success');
    expect(vm.usuarioSelecionado).toBeNull();
    expect(vm.justificativa).toBe('');
  });

  it('deve lidar com erro ao criar atribuicao', async () => {
    vi.mocked(buscarUnidadePorCodigo).mockResolvedValue(unidadeMinima);
    vi.mocked(criarAtribuicaoTemporaria).mockRejectedValue(new Error('Erro no servidor'));

    const wrapper = mount(AtribuicaoTemporariaView, mountOptions);
    const vm = wrapper.vm as unknown as AtribuicaoTemporariaVm;
    await flushPromises();

    vm.usuarioSelecionado = '999';
    vm.dataInicio = '2025-01-01';
    vm.dataTermino = '2025-12-31';
    vm.justificativa = 'Teste de justificativa';

    await vm.criarAtribuicao();
    await flushPromises();

    expect(criarAtribuicaoTemporaria).toHaveBeenCalled();
    expect(vm.erroFormulario).toContain('Erro no servidor');
    expect(mockNotify).not.toHaveBeenCalledWith(expect.any(String), 'danger');
  });

});
