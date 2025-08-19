import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import Unidade from '../Unidade.vue';
import {useUnidadesStore} from '@/stores/unidades';
import {usePerfilStore} from '@/stores/perfil';
import {useServidoresStore} from '@/stores/servidores';
import {useMapasStore} from '@/stores/mapas';
import {useRouter} from 'vue-router';
import {beforeEach, describe, expect, it, MockInstance, vi} from 'vitest';

// Mock do useRouter
vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({
    push: vi.fn(),
  })),
}));

// Mock do componente TreeTable
const mockTreeTable = {
  template: '<div><slot></slot></div>',
  props: ['data', 'columns', 'title', 'hideHeaders'],
  emits: ['row-click'],
};

// Mock de servidores.json
vi.mock('@/mocks/servidores.json', () => ({
  default: [
    { id: 1, nome: 'Servidor A', unidade: 'UN1', email: 'a@email.com', ramal: '1111' },
    { id: 2, nome: 'Servidor B', unidade: 'SUB1', email: 'b@email.com', ramal: '2222' },
    { id: 3, nome: 'Servidor C', unidade: 'UN2', email: 'c@email.com', ramal: '3333' },
  ],
}));

// Mock de mapas.json
vi.mock('@/mocks/mapas.json', () => ({
  default: [
    { id: 1, unidade: 'UN1', idProcesso: 10, situacao: 'em_andamento', competencias: [], dataCriacao: '2025-01-01', dataDisponibilizacao: null, dataFinalizacao: null },
  ],
}));

// Mock de unidades.json
vi.mock('@/mocks/unidades.json', () => ({
  default: [
    { sigla: 'UN1', nome: 'Unidade Teste 1', titular: 1, responsavel: null, tipo: 'OPERACIONAL', filhas: [
      { sigla: 'SUB1', nome: 'Subunidade 1', titular: 2, responsavel: null, tipo: 'OPERACIONAL', filhas: [] },
    ]},
    { sigla: 'UN2', nome: 'Unidade Teste 2', titular: 3, responsavel: null, tipo: 'OPERACIONAL', filhas: [] },
  ],
}));

describe('Unidade.vue', () => {
  let unidadesStore: ReturnType<typeof useUnidadesStore>;
  let perfilStore: ReturnType<typeof usePerfilStore>;
  let servidoresStore: ReturnType<typeof useServidoresStore>;
  let mapasStore: ReturnType<typeof useMapasStore>;
  let routerPushMock: MockInstance; // Tipar como MockInstance
  let consoleWarnSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    setActivePinia(createPinia());
    unidadesStore = useUnidadesStore();
    perfilStore = usePerfilStore();
    servidoresStore = useServidoresStore();
    mapasStore = useMapasStore();
    routerPushMock = useRouter().push as unknown as MockInstance;

    vi.clearAllMocks();
    consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});

    // Resetar o estado das stores
    unidadesStore.$reset();
    perfilStore.$reset();
    servidoresStore.$reset();
    mapasStore.$reset();
  });

  it('deve renderizar corretamente os detalhes da unidade', async () => {
    const wrapper = mount(Unidade, {
      props: { siglaUnidade: 'UN1' },
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();

    expect(wrapper.find('h2').text()).toBe('UN1 - Unidade Teste 1');
    expect(wrapper.text()).toContain('Responsável: Servidor A');
    expect(wrapper.text()).toContain('Contato: a@email.com');
    expect(wrapper.find('[data-testid="card-atividades-conhecimentos"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('Mapa de Competências');
  });

  it('deve exibir o botão "Criar atribuição" para o perfil ADMIN', async () => {
    perfilStore.perfilSelecionado = 'ADMIN';
    const wrapper = mount(Unidade, {
      props: { siglaUnidade: 'UN1' },
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();
    expect(wrapper.find('[data-testid="btn-criar-atribuicao"]').exists()).toBe(true);
  });

  it('não deve exibir o botão "Criar atribuição" para outros perfis', async () => {
    perfilStore.perfilSelecionado = 'GESTOR';
    const wrapper = mount(Unidade, {
      props: { siglaUnidade: 'UN1' },
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();
    expect(wrapper.find('[data-testid="btn-criar-atribuicao"]').exists()).toBe(false);
  });

  it('deve exibir o botão "Visualizar Mapa" se houver mapa vigente', async () => {
    const wrapper = mount(Unidade, {
      props: { siglaUnidade: 'UN1' },
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();
    expect(wrapper.find('button.btn-info').exists()).toBe(true);
    expect(wrapper.find('button.btn-info').text()).toBe('Visualizar Mapa');
  });

  it('não deve exibir o botão "Visualizar Mapa" se não houver mapa vigente', async () => {
    mapasStore.mapas = [];
    const wrapper = mount(Unidade, {
      props: { siglaUnidade: 'UN1' },
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();
    expect(wrapper.find('button.btn-info').exists()).toBe(false);
  });

  it('deve navegar para a criação de atribuição', async () => {
    perfilStore.perfilSelecionado = 'ADMIN';
    const wrapper = mount(Unidade, {
      props: { siglaUnidade: 'UN1' },
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();

    const criarAtribuicaoBtn = wrapper.find('[data-testid="btn-criar-atribuicao"]');
    expect(criarAtribuicaoBtn.exists()).toBe(true);
    await criarAtribuicaoBtn.trigger('click');
    expect(routerPushMock).toHaveBeenCalledWith({ path: '/unidade/UN1/atribuicao' });
  });

  it('deve navegar para atividades e conhecimentos se houver mapa vigente', async () => {
    const wrapper = mount(Unidade, {
      props: { siglaUnidade: 'UN1' },
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();

    const cardAtividades = wrapper.find('[data-testid="card-atividades-conhecimentos"]');
    expect(cardAtividades.exists()).toBe(true);
    await cardAtividades.trigger('click');
    expect(routerPushMock).toHaveBeenCalledWith({
      name: 'SubprocessoCadastro',
      params: { idProcesso: 10, siglaUnidade: 'UN1' },
    });
  });

  it('deve logar um aviso se tentar navegar para atividades sem mapa vigente', async () => {
    mapasStore.mapas = [];
    const wrapper = mount(Unidade, {
      props: { siglaUnidade: 'UN1' },
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();

    const cardAtividades = wrapper.find('[data-testid="card-atividades-conhecimentos"]');
    expect(cardAtividades.exists()).toBe(true);
    await cardAtividades.trigger('click');
    expect(routerPushMock).not.toHaveBeenCalled();
    expect(consoleWarnSpy).toHaveBeenCalledWith('Não há mapa vigente para navegar para atividades.');
  });

  it('deve navegar para visualização de mapa ao clicar no card ou botão', async () => {
    const wrapper = mount(Unidade, {
      props: { siglaUnidade: 'UN1' },
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();

    const visualizarMapaBtn = wrapper.find('button.btn-info');
    expect(visualizarMapaBtn.exists()).toBe(true);
    await visualizarMapaBtn.trigger('click');
    expect(routerPushMock).toHaveBeenCalledWith({
      name: 'SubprocessoVisMapa',
      params: { idProcesso: 10, siglaUnidade: 'UN1' },
    });
    routerPushMock.mockClear();

    const cardMapa = wrapper.findAll('.card.h-100.cursor-pointer')[1];
    expect(cardMapa.exists()).toBe(true);
    await cardMapa.trigger('click');
    expect(routerPushMock).toHaveBeenCalledWith({
      name: 'SubprocessoVisMapa',
      params: { idProcesso: 10, siglaUnidade: 'UN1' },
    });
  });

  it('deve exibir unidades subordinadas na TreeTable', async () => {
    const wrapper = mount(Unidade, {
      props: { siglaUnidade: 'UN1' },
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();

    const treeTableProps = wrapper.findComponent(mockTreeTable).props();
    expect(treeTableProps.data.length).toBe(1);
    expect(treeTableProps.data[0].nome).toBe('SUB1 - Subunidade 1');
  });

  it('deve navegar para unidade subordinada ao clicar na TreeTable', async () => {
    const wrapper = mount(Unidade, {
      props: { siglaUnidade: 'UN1' },
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();

    const subunidadeItem = { id: 'SUB1', nome: 'SUB1 - Subunidade 1' };
    wrapper.findComponent(mockTreeTable).vm.$emit('row-click', subunidadeItem);

    expect(routerPushMock).toHaveBeenCalledWith({ path: '/unidade/SUB1' });
  });
});