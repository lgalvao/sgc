import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import Painel from '../Painel.vue';
import {usePerfilStore} from '@/stores/perfil';
import {useProcessosStore} from '@/stores/processos';
import {useAlertasStore} from '@/stores/alertas';
import {useRouter} from 'vue-router';
import {beforeEach, describe, expect, it, vi} from 'vitest';

// Mock do useRouter
vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({
    push: vi.fn(),
  })),
  RouterLink: {
    template: '<a :href="to"><slot /></a>',
    props: ['to'],
  },
}));

// Mock de alertas.json
vi.mock('@/mocks/alertas.json', () => ({
  default: [
    { id: 1, unidadeOrigem: 'COSIS', unidadeDestino: 'SESEL', dataHora: '2025-07-02T10:00:00', idProcesso: 1, descricao: 'Cadastro devolvido para ajustes' },
    { id: 2, unidadeOrigem: 'SEDOC', unidadeDestino: 'SEDESENV', dataHora: '2025-07-03T14:30:00', idProcesso: 2, descricao: 'Prazo próximo para validação do mapa de competências' },
    { id: 3, unidadeOrigem: 'SEDOC', unidadeDestino: 'SEDESENV', dataHora: '2025-07-04T09:15:00', idProcesso: 2, descricao: 'Nova atribuição temporária: Bruno Silva (10/10/2025 a 10/11/2025)' },
  ],
}));

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
    { id: 1, idProcesso: 1, unidade: 'STIC', dataLimiteEtapa1: '2025-06-30', dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'STIC', unidadeAnterior: null },
    { id: 3, idProcesso: 1, unidade: 'SESEL', dataLimiteEtapa1: '2025-06-30', dataLimiteEtapa2: '2025-07-30', dataFimEtapa1: '2025-06-30', dataFimEtapa2: null, situacao: 'Mapa disponibilizado', unidadeAtual: 'SESEL', unidadeAnterior: 'STIC' },
    { id: 4, idProcesso: 1, unidade: 'SEDESENV', dataLimiteEtapa1: '2025-06-30', dataLimiteEtapa2: null, dataFimEtapa1: '2025-06-30', dataFimEtapa2: null, situacao: 'Cadastro homologado', unidadeAtual: 'SEDESENV', unidadeAnterior: 'SESEL' },
    { id: 5, idProcesso: 2, unidade: 'STIC', dataLimiteEtapa1: '2025-07-15', dataLimiteEtapa2: '2025-08-15', dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'STIC', unidadeAnterior: null },
    { id: 6, idProcesso: 2, unidade: 'COINF', dataLimiteEtapa1: '2025-07-20', dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'COINF', unidadeAnterior: 'STIC' },
    { id: 7, idProcesso: 2, unidade: 'SENIC', dataLimiteEtapa1: '2025-07-20', dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'SENIC', unidadeAnterior: 'COINF' },
    { id: 8, idProcesso: 4, unidade: 'STIC', dataLimiteEtapa1: '2024-06-10', dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'STIC', unidadeAnterior: null },
    { id: 10, idProcesso: 4, unidade: 'SEDESENV', dataLimiteEtapa1: '2024-05-10', dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'SEDESENV', unidadeAnterior: 'STIC' },
    { id: 11, idProcesso: 4, unidade: 'SEDIA', dataLimiteEtapa1: '2024-07-10', dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'SEDIA', unidadeAnterior: 'SEDESENV' },
    { id: 12, idProcesso: 4, unidade: 'SESEL', dataLimiteEtapa1: '2024-05-10', dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'SESEL', unidadeAnterior: 'SEDIA' },
    { id: 13, idProcesso: 5, unidade: 'STIC', dataLimiteEtapa1: '2025-08-10', dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'STIC', unidadeAnterior: null },
    { id: 15, idProcesso: 5, unidade: 'SEDIA', dataLimiteEtapa1: '2025-08-10', dataLimiteEtapa2: null, dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Cadastro em andamento', unidadeAtual: 'SEDIA', unidadeAnterior: 'STIC' },
  ],
}));

describe('Painel.vue', () => {
  let perfilStore: ReturnType<typeof usePerfilStore>;
  let processosStore: ReturnType<typeof useProcessosStore>;
  let alertasStore: ReturnType<typeof useAlertasStore>;
  let routerPushMock: ReturnType<typeof useRouter>['push'];

  beforeEach(() => {
    setActivePinia(createPinia());
    perfilStore = usePerfilStore();
    processosStore = useProcessosStore();
    alertasStore = useAlertasStore();
    routerPushMock = useRouter().push;

    vi.clearAllMocks();

    // Resetar o estado das stores
    perfilStore.$reset();
    processosStore.$reset();
    alertasStore.$reset();
  });

  it('deve renderizar corretamente os títulos e tabelas', async () => {
    const wrapper = mount(Painel, {
      global: {
        plugins: [createPinia()],
      },
    });
    await wrapper.vm.$nextTick();

    expect(wrapper.find('[data-testid="titulo-processos"]').text()).toBe('Processos');
    expect(wrapper.find('[data-testid="titulo-alertas"]').text()).toBe('Alertas');
    expect(wrapper.find('[data-testid="tabela-processos"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="tabela-alertas"]').exists()).toBe(true);
  });

  it('deve exibir o botão "Criar processo" para o perfil ADMIN', async () => {
    perfilStore.perfilSelecionado = 'ADMIN';
    const wrapper = mount(Painel, {
      global: {
        plugins: [createPinia()],
      },
    });
    await wrapper.vm.$nextTick();
    expect(wrapper.find('[data-testid="btn-criar-processo"]').exists()).toBe(true);
  });

  it('não deve exibir o botão "Criar processo" para outros perfis', async () => {
    perfilStore.perfilSelecionado = 'GESTOR';
    const wrapper = mount(Painel, {
      global: {
        plugins: [createPinia()],
      },
    });
    await wrapper.vm.$nextTick();
    expect(wrapper.find('[data-testid="btn-criar-processo"]').exists()).toBe(false);
  });

  it('deve exibir os processos na tabela', async () => {
    const wrapper = mount(Painel, {
      global: {
        plugins: [createPinia()],
      },
    });

    await wrapper.vm.$nextTick();
    const rows = wrapper.findAll('[data-testid="tabela-processos"] tbody tr');
    expect(rows.length).toBe(4);
    expect(rows[0].text()).toContain('Mapeamento de competências - 2025');
    expect(rows[0].text()).toContain('Mapeamento');
    expect(rows[0].text()).toContain('STIC, SESEL, SEDESENV');
    expect(rows[0].text()).toContain('Finalizado');
  });

  it('deve ordenar os processos por descrição (ascendente e descendente)', async () => {
    const wrapper = mount(Painel, {
      global: {
        plugins: [createPinia()],
      },
    });
    await wrapper.vm.$nextTick();

    let rows = wrapper.findAll('[data-testid="tabela-processos"] tbody tr');
    // A ordem padrão é por descrição ascendente. Ajustar as expectativas com base nos mocks.
    // Processos mockados: 1 (Mapeamento), 2 (Revisão), 4 (Revisão), 5 (Diagnóstico)
    // Ordem alfabética: 1, 5, 2, 4
    expect(rows[0].text()).toContain('Diagnóstico de Gaps - 2025');
    expect(rows[1].text()).toContain('Mapeamento de competências - 2025');

    await wrapper.find('[data-testid="coluna-descricao"]').trigger('click');
    await wrapper.vm.$nextTick();
    rows = wrapper.findAll('[data-testid="tabela-processos"] tbody tr');
    // Ordem alfabética descendente: 4, 2, 5, 1
    expect(rows[0].text()).toContain('Revisão de mapa de competências STIC - 2024');
    expect(rows[1].text()).toContain('Revisão de mapeamento STIC/COINF - 2025');
  });

  it('deve redirecionar para os detalhes do processo ao clicar na linha', async () => {
    const wrapper = mount(Painel, {
      global: {
        plugins: [createPinia()],
      },
    });

    await wrapper.vm.$nextTick();
    const firstRow = wrapper.findAll('[data-testid="tabela-processos"] tbody tr')[0];
    await firstRow.trigger('click');

    expect(routerPushMock).toHaveBeenCalledWith({ name: 'Processo', params: { idProcesso: 1 } });
  });

  it('deve exibir os alertas na tabela', async () => {
    const wrapper = mount(Painel, {
      global: {
        plugins: [createPinia()],
      },
    });

    await wrapper.vm.$nextTick();
    const rows = wrapper.findAll('[data-testid="tabela-alertas"] tbody tr');
    expect(rows.length).toBe(3);
    expect(rows[0].text()).toContain('Cadastro devolvido para ajustes');
    expect(rows[0].text()).toContain('COSIS');
    expect(rows[0].text()).toContain('Mapeamento de competências - 2025');
  });

  it('deve exibir "Nenhum alerta no momento." quando não há alertas', async () => {
    alertasStore.alertas = [];
    const wrapper = mount(Painel, {
      global: {
        plugins: [createPinia()],
      },
    });

    await wrapper.vm.$nextTick();
    expect(wrapper.find('[data-testid="tabela-alertas"]').text()).toContain('Nenhum alerta no momento.');
  });

  it('deve redirecionar para a página de criação de processo ao clicar no botão', async () => {
    perfilStore.perfilSelecionado = 'ADMIN';
    const wrapper = mount(Painel, {
      global: {
        plugins: [createPinia()],
      },
    });
    await wrapper.vm.$nextTick();

    const criarProcessoBtn = wrapper.find('[data-testid="btn-criar-processo"]');
    expect(criarProcessoBtn.exists()).toBe(true);
    await criarProcessoBtn.trigger('click');
    expect(routerPushMock).toHaveBeenCalledWith({ name: 'CadProcesso' });
  });
});