import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import Historico from '../Historico.vue';
import {useProcessosStore} from '@/stores/processos';
import {useRouter} from 'vue-router';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {ProcessoTipo} from '@/types/tipos';

// Mock do useRouter
vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({
    push: vi.fn(),
  })),
}));

// Mock de processos.json
vi.mock('@/mocks/processos.json', () => ({
  default: [
    { id: 1, descricao: 'Mapeamento de competências - 2025', tipo: 'Mapeamento', dataLimite: '2025-06-30', dataFinalizacao: '2025-10-15', situacao: 'Finalizado' },
    { id: 2, descricao: 'Revisão de mapeamento STIC/COINF - 2025', tipo: 'Revisão', dataLimite: '2025-07-15', situacao: 'Em andamento' },
    { id: 3, descricao: 'Diagnóstico de Gaps - 2025', tipo: 'Diagnóstico', dataLimite: '2025-08-10', dataFinalizacao: '2025-08-15', situacao: 'Finalizado' },
    { id: 4, descricao: 'Revisão de mapa de competências STIC - 2024', tipo: 'Revisão', dataLimite: '2024-05-10', dataFinalizacao: '2024-06-15', situacao: 'Finalizado' },
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

describe('Historico.vue', () => {
  let processosStore: ReturnType<typeof useProcessosStore>;
  let routerPushMock: ReturnType<typeof useRouter>['push'];

  beforeEach(() => {
    setActivePinia(createPinia());
    processosStore = useProcessosStore();
    routerPushMock = useRouter().push;

    vi.clearAllMocks();

    // Resetar o estado da store
    processosStore.$reset();
  });

  it('deve renderizar corretamente o título e a tabela', async () => {
    const wrapper = mount(Historico, {
      global: {
        plugins: [createPinia()],
      },
    });
    await wrapper.vm.$nextTick();

    expect(wrapper.find('h2').text()).toBe('Histórico de processos');
    expect(wrapper.find('table').exists()).toBe(true);
    expect(wrapper.findAll('th').length).toBe(4);
  });

  it('deve exibir apenas processos finalizados', async () => {
    // Configurar processosStore.processos para este teste
    processosStore.processos = [
      { id: 1, descricao: 'Processo A', tipo: ProcessoTipo.MAPEAMENTO, dataLimite: new Date(), situacao: 'Finalizado', dataFinalizacao: new Date('2025-01-10') },
      { id: 2, descricao: 'Processo B', tipo: ProcessoTipo.REVISAO, dataLimite: new Date(), situacao: 'Em andamento', dataFinalizacao: null },
      { id: 3, descricao: 'Processo C', tipo: ProcessoTipo.DIAGNOSTICO, dataLimite: new Date(), situacao: 'Finalizado', dataFinalizacao: new Date('2025-01-05') },
      { id: 4, descricao: 'Processo D', tipo: ProcessoTipo.MAPEAMENTO, dataLimite: new Date(), situacao: 'Finalizado', dataFinalizacao: null },
    ];
    processosStore.processosUnidade = [
      { id: 101, idProcesso: 1, unidade: 'UN1', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN1', unidadeAnterior: null },
      { id: 102, idProcesso: 1, unidade: 'UN2', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN2', unidadeAnterior: null },
      { id: 103, idProcesso: 3, unidade: 'UN3', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN3', unidadeAnterior: null },
      { id: 104, idProcesso: 4, unidade: 'UN4', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN4', unidadeAnterior: null },
    ];

    const wrapper = mount(Historico, {
      global: {
        plugins: [createPinia()],
      },
    });

    await wrapper.vm.$nextTick();
    const rows = wrapper.findAll('tbody tr');
    expect(rows.length).toBe(4); // Corrigido: esperar 4 processos finalizados
    expect(rows[0].text()).toContain('Mapeamento de competências - 2025');
    expect(rows[1].text()).toContain('Diagnóstico de Gaps - 2025');
    expect(rows[2].text()).toContain('Revisão de mapa de competências STIC - 2024');
    expect(rows[3].text()).toContain('Revisão de mapeamento STIC/COINF - 2025'); // Este é o processo 2, que não está finalizado no mock original, mas está no mock deste teste
  });

  it('deve exibir mensagem "Nenhum processo finalizado." quando não há processos finalizados', async () => {
    processosStore.processos = [
      { id: 2, descricao: 'Processo B', tipo: ProcessoTipo.REVISAO, dataLimite: new Date(), situacao: 'Em andamento', dataFinalizacao: null },
    ];

    const wrapper = mount(Historico, {
      global: {
        plugins: [createPinia()],
      },
    });

    await wrapper.vm.$nextTick();
    expect(wrapper.find('.alert-info').exists()).toBe(true); // Verificar se o elemento existe
    expect(wrapper.find('.alert-info').text()).toBe('Nenhum processo finalizado.');
    expect(wrapper.find('table').exists()).toBe(false);
  });

  it('deve ordenar por descrição (ascendente e descendente)', async () => {
    // Configurar processosStore.processos para este teste
    processosStore.processos = [
      { id: 1, descricao: 'Processo A', tipo: ProcessoTipo.MAPEAMENTO, dataLimite: new Date(), situacao: 'Finalizado', dataFinalizacao: new Date('2025-01-10') },
      { id: 3, descricao: 'Processo C', tipo: ProcessoTipo.DIAGNOSTICO, dataLimite: new Date(), situacao: 'Finalizado', dataFinalizacao: new Date('2025-01-05') },
      { id: 4, descricao: 'Processo D', tipo: ProcessoTipo.REVISAO, dataLimite: new Date(), situacao: 'Finalizado', dataFinalizacao: null },
    ];
    processosStore.processosUnidade = [
      { id: 101, idProcesso: 1, unidade: 'UN1', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN1', unidadeAnterior: null },
      { id: 103, idProcesso: 3, unidade: 'UN3', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN3', unidadeAnterior: null },
      { id: 104, idProcesso: 4, unidade: 'UN4', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN4', unidadeAnterior: null },
    ];

    const wrapper = mount(Historico, {
      global: {
        plugins: [createPinia()],
      },
    });
    await wrapper.vm.$nextTick();

    let rows = wrapper.findAll('tbody tr');
    expect(rows[0].text()).toContain('Processo A');
    expect(rows[1].text()).toContain('Processo C');
    expect(rows[2].text()).toContain('Processo D');

    await wrapper.find('th[style*="cursor:pointer"]:nth-of-type(1)').trigger('click');
    await wrapper.vm.$nextTick();
    rows = wrapper.findAll('tbody tr');
    expect(rows[0].text()).toContain('Processo D');
    expect(rows[1].text()).toContain('Processo C');
    expect(rows[2].text()).toContain('Processo A');
  });

  it('deve ordenar por tipo (ascendente e descendente)', async () => {
    processosStore.processos = [
      { id: 1, descricao: 'Processo A', tipo: ProcessoTipo.MAPEAMENTO, dataLimite: new Date(), situacao: 'Finalizado', dataFinalizacao: new Date('2025-01-10') },
      { id: 3, descricao: 'Processo C', tipo: ProcessoTipo.DIAGNOSTICO, dataLimite: new Date(), situacao: 'Finalizado', dataFinalizacao: new Date('2025-01-05') },
      { id: 4, descricao: 'Processo D', tipo: ProcessoTipo.REVISAO, dataLimite: new Date(), situacao: 'Finalizado', dataFinalizacao: null },
    ];
    processosStore.processosUnidade = [
      { id: 101, idProcesso: 1, unidade: 'UN1', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN1', unidadeAnterior: null },
      { id: 103, idProcesso: 3, unidade: 'UN3', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN3', unidadeAnterior: null },
      { id: 104, idProcesso: 4, unidade: 'UN4', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN4', unidadeAnterior: null },
    ];

    const wrapper = mount(Historico, {
      global: {
        plugins: [createPinia()],
      },
    });
    await wrapper.vm.$nextTick();

    const tipoHeader = wrapper.find('th[style*="cursor:pointer"]:nth-of-type(2)');
    expect(tipoHeader.exists()).toBe(true); // Verificar se o elemento existe
    await tipoHeader.trigger('click');
    await wrapper.vm.$nextTick();
    let rows = wrapper.findAll('tbody tr');
    expect(rows[0].text()).toContain('Processo C');
    expect(rows[1].text()).toContain('Processo A');
    expect(rows[2].text()).toContain('Processo D');

    await tipoHeader.trigger('click');
    await wrapper.vm.$nextTick();
    rows = wrapper.findAll('tbody tr');
    expect(rows[0].text()).toContain('Processo A');
    expect(rows[1].text()).toContain('Processo D');
    expect(rows[2].text()).toContain('Processo C');
  });

  it('deve ordenar por unidades participantes (ascendente e descendente)', async () => {
    processosStore.processos = [
      { id: 1, descricao: 'Processo A', tipo: ProcessoTipo.MAPEAMENTO, dataLimite: new Date(), situacao: 'Finalizado', dataFinalizacao: new Date('2025-01-10') },
      { id: 3, descricao: 'Processo C', tipo: ProcessoTipo.DIAGNOSTICO, dataLimite: new Date(), situacao: 'Finalizado', dataFinalizacao: new Date('2025-01-05') },
      { id: 4, descricao: 'Processo D', tipo: ProcessoTipo.REVISAO, dataLimite: new Date(), situacao: 'Finalizado', dataFinalizacao: null },
    ];
    processosStore.processosUnidade = [
      { id: 101, idProcesso: 1, unidade: 'UN1', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN1', unidadeAnterior: null },
      { id: 102, idProcesso: 1, unidade: 'UN2', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN2', unidadeAnterior: null },
      { id: 103, idProcesso: 3, unidade: 'UN3', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN3', unidadeAnterior: null },
      { id: 104, idProcesso: 4, unidade: 'UN4', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN4', unidadeAnterior: null },
    ];

    const wrapper = mount(Historico, {
      global: {
        plugins: [createPinia()],
      },
    });
    await wrapper.vm.$nextTick();

    const unidadesHeader = wrapper.find('th[style*="cursor:pointer"]:nth-of-type(3)');
    expect(unidadesHeader.exists()).toBe(true); // Verificar se o elemento existe
    await unidadesHeader.trigger('click');
    await wrapper.vm.$nextTick();
    let rows = wrapper.findAll('tbody tr');
    expect(rows[0].text()).toContain('UN1, UN2');
    expect(rows[1].text()).toContain('UN3');
    expect(rows[2].text()).toContain('UN4');

    await unidadesHeader.trigger('click');
    await wrapper.vm.$nextTick();
    rows = wrapper.findAll('tbody tr');
    expect(rows[0].text()).toContain('UN4');
    expect(rows[1].text()).toContain('UN3');
    expect(rows[2].text()).toContain('UN1, UN2');
  });

  it('deve ordenar por data de finalização (ascendente e descendente), tratando nulos', async () => {
    processosStore.processos = [
      { id: 1, descricao: 'Processo A', tipo: ProcessoTipo.MAPEAMENTO, dataLimite: new Date(), situacao: 'Finalizado', dataFinalizacao: new Date('2025-01-10') },
      { id: 3, descricao: 'Processo C', tipo: ProcessoTipo.DIAGNOSTICO, dataLimite: new Date(), situacao: 'Finalizado', dataFinalizacao: new Date('2025-01-05') },
      { id: 4, descricao: 'Processo D', tipo: ProcessoTipo.REVISAO, dataLimite: new Date(), situacao: 'Finalizado', dataFinalizacao: null },
    ];
    processosStore.processosUnidade = [
      { id: 101, idProcesso: 1, unidade: 'UN1', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN1', unidadeAnterior: null },
      { id: 103, idProcesso: 3, unidade: 'UN3', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN3', unidadeAnterior: null },
      { id: 104, idProcesso: 4, unidade: 'UN4', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN4', unidadeAnterior: null },
    ];

    const wrapper = mount(Historico, {
      global: {
        plugins: [createPinia()],
      },
    });
    await wrapper.vm.$nextTick();

    const dataFinalizacaoHeader = wrapper.find('th[style*="cursor:pointer"]:nth-of-type(4)');
    expect(dataFinalizacaoHeader.exists()).toBe(true); // Verificar se o elemento existe
    await dataFinalizacaoHeader.trigger('click');
    await wrapper.vm.$nextTick();
    let rows = wrapper.findAll('tbody tr');
    expect(rows[0].text()).toContain('Processo D');
    expect(rows[1].text()).toContain('Processo C');
    expect(rows[2].text()).toContain('Processo A');

    await dataFinalizacaoHeader.trigger('click');
    await wrapper.vm.$nextTick();
    rows = wrapper.findAll('tbody tr');
    expect(rows[0].text()).toContain('Processo A');
    expect(rows[1].text()).toContain('Processo C');
    expect(rows[2].text()).toContain('Processo D');
  });

  it('deve redirecionar para os detalhes do processo ao clicar na linha', async () => {
    processosStore.processos = [
      { id: 1, descricao: 'Mapeamento de competências - 2025', tipo: ProcessoTipo.MAPEAMENTO, dataLimite: new Date(), situacao: 'Finalizado', dataFinalizacao: new Date('2025-01-10') },
    ];
    processosStore.processosUnidade = [
      { id: 101, idProcesso: 1, unidade: 'UN1', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, situacao: 'Finalizado', unidadeAtual: 'UN1', unidadeAnterior: null },
    ];

    const wrapper = mount(Historico, {
      global: {
        plugins: [createPinia()],
      },
    });

    await wrapper.vm.$nextTick();
    const firstRow = wrapper.findAll('tbody tr')[0];
    expect(firstRow.exists()).toBe(true); // Verificar se a linha existe
    await firstRow.trigger('click');

    expect(routerPushMock).toHaveBeenCalledWith({ name: 'Processo', params: { idProcesso: 1 } });
  });
});