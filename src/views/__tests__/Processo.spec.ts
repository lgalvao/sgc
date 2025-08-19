import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import Processo from '../Processo.vue';
import {useProcessosStore} from '@/stores/processos';
import {useUnidadesStore} from '@/stores/unidades';
import {usePerfilStore} from '@/stores/perfil';
import {useRoute, useRouter} from 'vue-router';
import {beforeEach, describe, expect, it, vi} from 'vitest';

// Mock do useRouter e useRoute
vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({
    push: vi.fn(),
  })),
  useRoute: vi.fn(() => ({
    params: { idProcesso: 1 }, // Valor padrão para idProcesso
  })),
}));

// Mock do componente TreeTable
const mockTreeTable = {
  template: '<div><slot></slot></div>',
  props: ['data', 'columns', 'title', 'hideHeaders'],
  emits: ['row-click'],
};

// Mock de processos.json
vi.mock('@/mocks/processos.json', () => ({
  default: [
    { id: 1, descricao: 'Mapeamento de competências - 2025', tipo: 'Mapeamento', dataLimite: '2025-06-30', dataFinalizacao: '2025-10-15', situacao: 'Finalizado' },
    { id: 2, descricao: 'Revisão de mapeamento STIC/COINF - 2025', tipo: 'Revisão', dataLimite: '2025-07-15', situacao: 'Em andamento' },
    { id: 4, descricao: 'Revisão de mapa de competências STIC - 2024', tipo: 'Revisão', dataLimite: '2024-05-10', dataFinalizacao: '2024-06-15', situacao: 'Finalizado' },
    { id: 5, descricao: 'Revisão de mapa de competências - 2024', tipo: 'Diagnóstico', dataLimite: '2025-08-10', dataFinalizacao: '2025-08-15', situacao: 'Finalizado' },
  ],
}));

// Mock de subprocessos.json
vi.mock('@/mocks/subprocessos.json', () => ({
  default: [
    { id: 1, idProcesso: 1, unidade: 'STIC', dataLimiteEtapa1: new Date('2025-06-30'), dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'STIC', unidadeAnterior: null },
    { id: 3, idProcesso: 1, unidade: 'SESEL', dataLimiteEtapa1: new Date('2025-06-30'), dataLimiteEtapa2: new Date('2025-07-30'), dataFimEtapa1: new Date('2025-06-30'), dataFimEtapa2: null, situacao: 'Mapa disponibilizado', unidadeAtual: 'SESEL', unidadeAnterior: 'STIC' },
    { id: 4, idProcesso: 1, unidade: 'SEDESENV', dataLimiteEtapa1: new Date('2025-06-30'), dataLimiteEtapa2: null, dataFimEtapa1: new Date('2025-06-30'), dataFimEtapa2: null, situacao: 'Cadastro homologado', unidadeAtual: 'SEDESENV', unidadeAnterior: 'SESEL' },
    { id: 5, idProcesso: 2, unidade: 'STIC', dataLimiteEtapa1: new Date('2025-07-15'), dataLimiteEtapa2: new Date('2025-08-15'), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'STIC', unidadeAnterior: null },
    { id: 6, idProcesso: 2, unidade: 'COINF', dataLimiteEtapa1: new Date('2025-07-20'), dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'COINF', unidadeAnterior: 'STIC' },
    { id: 7, idProcesso: 2, unidade: 'SENIC', dataLimiteEtapa1: new Date('2025-07-20'), dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'SENIC', unidadeAnterior: 'COINF' },
    { id: 8, idProcesso: 4, unidade: 'STIC', dataLimiteEtapa1: new Date('2024-06-10'), dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'STIC', unidadeAnterior: null },
    { id: 10, idProcesso: 4, unidade: 'SEDESENV', dataLimiteEtapa1: new Date('2024-05-10'), dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'SEDESENV', unidadeAnterior: 'STIC' },
    { id: 11, idProcesso: 4, unidade: 'SEDIA', dataLimiteEtapa1: new Date('2024-07-10'), dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'SEDIA', unidadeAnterior: 'SEDESENV' },
    { id: 12, idProcesso: 4, unidade: 'SESEL', dataLimiteEtapa1: new Date('2024-05-10'), dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'SESEL', unidadeAnterior: 'SEDIA' },
    { id: 13, idProcesso: 5, unidade: 'STIC', dataLimiteEtapa1: new Date('2025-08-10'), dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'STIC', unidadeAnterior: null },
    { id: 15, idProcesso: 5, unidade: 'SEDIA', dataLimiteEtapa1: new Date('2025-08-10'), dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'SEDIA', unidadeAnterior: 'STIC' },
  ],
}));

// Mock de unidades.json
vi.mock('@/mocks/unidades.json', () => ({
  default: [
    { sigla: 'SEDOC', nome: 'SEDOC', tipo: 'ADMINISTRATIVA', titular: 7, responsavel: null, filhas: [
      { sigla: 'STIC', nome: 'STIC', tipo: 'INTEROPERACIONAL', titular: 8, responsavel: null, filhas: [
        { sigla: 'COSIS', nome: 'COSIS', tipo: 'INTERMEDIARIA', titular: 9, responsavel: null, filhas: [
          { sigla: 'SESEL', nome: 'SESEL', tipo: 'OPERACIONAL', titular: 11, responsavel: null, filhas: [] },
          { sigla: 'SEDESENV', nome: 'SEDESENV', tipo: 'OPERACIONAL', titular: 10, responsavel: null, filhas: [] },
        ]},
      ]},
    ]},
  ],
}));

describe('Processo.vue', () => {
  let processosStore: ReturnType<typeof useProcessosStore>;
  let unidadesStore: ReturnType<typeof useUnidadesStore>;
  let perfilStore: ReturnType<typeof usePerfilStore>;
  let routerPushMock: ReturnType<typeof useRouter>['push'];
  let useRouteMock: ReturnType<typeof useRoute>;

  beforeEach(() => {
    setActivePinia(createPinia());
    processosStore = useProcessosStore();
    unidadesStore = useUnidadesStore();
    perfilStore = usePerfilStore();
    routerPushMock = useRouter().push;
    useRouteMock = useRoute();

    vi.clearAllMocks();

    // Resetar o estado das stores
    processosStore.$reset();
    unidadesStore.$reset();
    perfilStore.$reset();

    // Mock de dados iniciais para as stores
    // Os dados de processos e processosUnidade agora vêm dos mocks globais
    // unidadesStore.unidades agora vem do mock global
    perfilStore.perfilSelecionado = 'ADMIN';

    // Espionar métodos
    vi.spyOn(processosStore, 'finalizarProcesso');
    // @ts-ignore
    vi.spyOn(window, 'confirm').mockReturnValue(true);
  });

  it('deve renderizar corretamente os detalhes do processo', async () => {
    const wrapper = mount(Processo, {
      props: { idProcesso: 1 }, // Garantir que o processo 1 esteja selecionado
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();

    expect(wrapper.find('h2').text()).toBe('Mapeamento de competências - 2025');
    expect(wrapper.text()).toContain('Tipo: Mapeamento');
    expect(wrapper.text()).toContain('Situação: Finalizado');
    expect(wrapper.findComponent(mockTreeTable).exists()).toBe(true);
  });

  it('deve exibir o botão "Finalizar processo" para o perfil ADMIN', async () => {
    perfilStore.perfilSelecionado = 'ADMIN';
    const wrapper = mount(Processo, {
      props: { idProcesso: 1 }, // Garantir que o processo 1 esteja selecionado
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();
    expect(wrapper.find('button.btn-danger').exists()).toBe(true);
  });

  it('não deve exibir o botão "Finalizar processo" para outros perfis', async () => {
    perfilStore.perfilSelecionado = 'GESTOR';
    const wrapper = mount(Processo, {
      props: { idProcesso: 1 }, // Garantir que o processo 1 esteja selecionado
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();
    expect(wrapper.find('button.btn-danger').exists()).toBe(false);
  });

  it('deve chamar finalizarProcesso e redirecionar ao clicar no botão', async () => {
    const wrapper = mount(Processo, {
      props: { idProcesso: 1 }, // Garantir que o processo 1 esteja selecionado
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();

    const finalizarBtn = wrapper.find('button.btn-danger');
    expect(finalizarBtn.exists()).toBe(true);
    await finalizarBtn.trigger('click');

    expect(window.confirm).toHaveBeenCalledWith('Tem certeza que deseja finalizar este processo?');
    expect(processosStore.finalizarProcesso).toHaveBeenCalledWith(1);
    expect(routerPushMock).toHaveBeenCalledWith('/painel');
  });

  it('não deve finalizar o processo se o usuário cancelar o confirm', async () => {
    // @ts-ignore
    (window.confirm as vi.Mock).mockReturnValue(false);

    const wrapper = mount(Processo, {
      props: { idProcesso: 1 }, // Garantir que o processo 1 esteja selecionado
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();

    const finalizarBtn = wrapper.find('button.btn-danger');
    expect(finalizarBtn.exists()).toBe(true);
    await finalizarBtn.trigger('click');

    expect(window.confirm).toHaveBeenCalledWith('Tem certeza que deseja finalizar este processo?');
    expect(processosStore.finalizarProcesso).not.toHaveBeenCalled();
    expect(routerPushMock).not.toHaveBeenCalled();
  });

  it('deve formatar corretamente os dados para a TreeTable', async () => {
    const wrapper = mount(Processo, {
      props: { idProcesso: 1 }, // Garantir que o processo 1 esteja selecionado
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();

    const treeTableProps = wrapper.findComponent(mockTreeTable).props();
    const data = treeTableProps.data;

    expect(data.length).toBe(1);
    expect(data[0].nome).toBe('STIC - STIC'); // Corrigido: O primeiro nó é STIC, não SEDOC
    expect(data[0].children.length).toBe(1);
    expect(data[0].children[0].nome).toBe('COSIS - COSIS');

    const cosis = data[0].children[0];
    expect(cosis.nome).toBe('COSIS - COSIS');
    expect(cosis.situacao).toBe('');
    expect(cosis.dataLimite).toBe('');
    expect(cosis.unidadeAtual).toBe('');
    expect(cosis.clickable).toBe(false);

    const sesel = cosis.children.find((c: any) => c.id === 'SESEL');
    expect(sesel.nome).toBe('SESEL - SESEL');
    expect(sesel.situacao).toBe('Mapa disponibilizado');
    expect(sesel.dataLimite).toBe('30/06/2025');
    expect(sesel.unidadeAtual).toBe('SESEL');
    expect(sesel.clickable).toBe(true);
  });

  it('deve navegar para subprocesso ao clicar em unidade participante', async () => {
    const wrapper = mount(Processo, {
      props: { idProcesso: 1 }, // Garantir que o processo 1 esteja selecionado
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();

    const seselItem = {
      id: 'SESEL',
      nome: 'SESEL - SESEL',
      situacao: 'Mapa disponibilizado',
      dataLimite: '30/06/2025',
      unidadeAtual: 'SESEL',
      clickable: true,
      children: [],
    };
    wrapper.findComponent(mockTreeTable).vm.$emit('row-click', seselItem);

    expect(routerPushMock).toHaveBeenCalledWith({
      name: 'Subprocesso',
      params: { idProcesso: 1, siglaUnidade: 'SESEL' },
    });
  });

  it('não deve navegar ao clicar em unidade intermediária', async () => {
    const wrapper = mount(Processo, {
      props: { idProcesso: 1 }, // Garantir que o processo 1 esteja selecionado
      global: {
        plugins: [createPinia()],
        stubs: { TreeTable: mockTreeTable },
      },
    });
    await wrapper.vm.$nextTick();

    const cosisItem = {
      id: 'COSIS',
      nome: 'COSIS - COSIS',
      situacao: '',
      dataLimite: '',
      unidadeAtual: '',
      clickable: false,
      children: [],
    };
    wrapper.findComponent(mockTreeTable).vm.$emit('row-click', cosisItem);

    expect(routerPushMock).not.toHaveBeenCalled();
  });
});