import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import CadMapa from '../CadMapa.vue';
import {useMapasStore} from '@/stores/mapas';
import {useUnidadesStore} from '@/stores/unidades';
import {useAtividadesStore} from '@/stores/atividades';
import {useProcessosStore} from '@/stores/processos';
import {beforeEach, describe, expect, it, vi} from 'vitest';

// Mock de unidades.json
vi.mock('@/mocks/unidades.json', () => ({
  default: [
    { sigla: 'UN1', nome: 'Unidade Teste 1', filhas: [], titular: 1, responsavel: null, tipo: 'OPERACIONAL' },
  ],
}));

// Mock de atividades.json
vi.mock('@/mocks/atividades.json', () => ({
  default: [
    { id: 10, descricao: 'Atividade A', idSubprocesso: 101, conhecimentos: [] },
    { id: 11, descricao: 'Atividade B', idSubprocesso: 101, conhecimentos: [] },
  ],
}));

// Mock de mapas.json
vi.mock('@/mocks/mapas.json', () => ({
  default: [
    { id: 1, unidade: 'UN1', idProcesso: 1, situacao: 'em_andamento', competencias: [], dataCriacao: '2025-01-01', dataDisponibilizacao: null, dataFinalizacao: null },
  ],
}));

describe('CadMapa.vue', () => {
  let mapasStore: ReturnType<typeof useMapasStore>;
  let unidadesStore: ReturnType<typeof useUnidadesStore>;
  let atividadesStore: ReturnType<typeof useAtividadesStore>;
  let processosStore: ReturnType<typeof useProcessosStore>;

  beforeEach(() => {
    setActivePinia(createPinia());
    mapasStore = useMapasStore();
    unidadesStore = useUnidadesStore();
    atividadesStore = useAtividadesStore();
    processosStore = useProcessosStore();

    vi.clearAllMocks();

    // Resetar o estado das stores para garantir isolamento entre os testes
    mapasStore.$reset();
    unidadesStore.$reset();
    atividadesStore.$reset();
    processosStore.$reset();

    // Configurar dados iniciais para os testes
    processosStore.processosUnidade = [
      { id: 101, idProcesso: 1, unidade: 'UN1', situacao: 'Em andamento', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), dataFimEtapa1: null, dataFimEtapa2: null, unidadeAtual: 'UN1', unidadeAnterior: null },
    ];
  });

  it('deve renderizar corretamente com os dados iniciais da unidade', async () => {
    const wrapper = mount(CadMapa, {
      props: {
        sigla: 'UN1',
        idProcesso: 1,
      },
      global: {
        plugins: [createPinia()],
      },
    });
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain('UN1 - Unidade Teste 1');
    expect(wrapper.find('h2').text()).toBe('Mapa de competências técnicas');
    expect(wrapper.find('[data-testid="btn-abrir-criar-competencia"]').exists()).toBe(true);
    expect(wrapper.find('button').text()).toContain('Disponibilizar');
  });

  it('deve abrir e fechar o modal de criação de competência', async () => {
    const wrapper = mount(CadMapa, {
      props: { sigla: 'UN1', idProcesso: 1 },
      global: { plugins: [createPinia()] },
    });
    await wrapper.vm.$nextTick();

    await wrapper.find('[data-testid="btn-abrir-criar-competencia"]').trigger('click');
    await wrapper.vm.$nextTick();
    expect(wrapper.find('.modal-dialog').exists()).toBe(true);
    expect(wrapper.find('h5.modal-title').text()).toBe('Criação de competência');

    await wrapper.find('.modal-header button.btn-close').trigger('click');
    await wrapper.vm.$nextTick();
    expect(wrapper.find('.modal-dialog').exists()).toBe(false);
  });

  it('deve adicionar uma nova competência', async () => {
    const wrapper = mount(CadMapa, {
      props: { sigla: 'UN1', idProcesso: 1 },
      global: { plugins: [createPinia()] },
    });
    await wrapper.vm.$nextTick();

    await wrapper.find('[data-testid="btn-abrir-criar-competencia"]').trigger('click');
    await wrapper.vm.$nextTick();
    await wrapper.find('[data-testid="input-nova-competencia"]').setValue('Nova Competência');
    await wrapper.find('[data-testid="atividade-checkbox"]').trigger('click');
    await wrapper.vm.$nextTick();

    await wrapper.find('[data-testid="btn-criar-competencia"]').trigger('click');
    await wrapper.vm.$nextTick();

    expect(mapasStore.adicionarMapa).toHaveBeenCalled();
    expect(wrapper.findAll('[data-testid="competencia-item"]').length).toBe(2);
    expect(wrapper.findAll('[data-testid="competencia-descricao"]')[1].text()).toBe('Nova Competência');
  });

  it('deve editar uma competência existente', async () => {
    mapasStore.mapas = [
      {
        id: 1,
        unidade: 'UN1',
        idProcesso: 1,
        competencias: [{ id: 100, descricao: 'Competência Original', atividadesAssociadas: [10] }],
        situacao: 'em_andamento',
        dataCriacao: new Date(),
        dataDisponibilizacao: null,
        dataFinalizacao: null,
      },
    ];

    const wrapper = mount(CadMapa, {
      props: {
        sigla: 'UN1',
        idProcesso: 1,
      },
      global: {
        plugins: [createPinia()],
      },
    });

    await wrapper.vm.$nextTick();

    const editarBtn = wrapper.find('[data-testid="btn-editar-competencia"]');
    expect(editarBtn.exists()).toBe(true);
    await editarBtn.trigger('click');
    await wrapper.vm.$nextTick();
    expect(wrapper.find('h5.modal-title').text()).toBe('Edição de competência');

    const inputEditar = wrapper.find('[data-testid="input-nova-competencia"]');
    await inputEditar.setValue('Competência Editada');
    const salvarBtn = wrapper.find('[data-testid="btn-criar-competencia"]');
    expect(salvarBtn.exists()).toBe(true);
    await salvarBtn.trigger('click');
    await wrapper.vm.$nextTick();

    expect(mapasStore.editarMapa).toHaveBeenCalledWith(
      1,
      expect.objectContaining({
        competencias: expect.arrayContaining([
          expect.objectContaining({
            id: 100,
            descricao: 'Competência Editada',
          }),
        ]),
      })
    );
    expect(wrapper.find('[data-testid="competencia-descricao"]').text()).toBe('Competência Editada');
  });

  it('deve excluir uma competência', async () => {
    mapasStore.mapas = [
      {
        id: 1,
        unidade: 'UN1',
        idProcesso: 1,
        competencias: [{ id: 100, descricao: 'Competência a Excluir', atividadesAssociadas: [] }],
        situacao: 'em_andamento',
        dataCriacao: new Date(),
        dataDisponibilizacao: null,
        dataFinalizacao: null,
      },
    ];

    const wrapper = mount(CadMapa, {
      props: { sigla: 'UN1', idProcesso: 1 },
      global: { plugins: [createPinia()] },
    });
    await wrapper.vm.$nextTick();

    const excluirBtn = wrapper.find('[data-testid="btn-excluir-competencia"]');
    expect(excluirBtn.exists()).toBe(true);
    await excluirBtn.trigger('click');
    await wrapper.vm.$nextTick();

    expect(mapasStore.editarMapa).toHaveBeenCalledWith(
      1,
      expect.objectContaining({
        competencias: [],
      })
    );
    expect(wrapper.findAll('[data-testid="competencia-item"]').length).toBe(0);
  });

  it('deve remover uma atividade associada a uma competência', async () => {
    mapasStore.mapas = [
      {
        id: 1,
        unidade: 'UN1',
        idProcesso: 1,
        competencias: [{ id: 100, descricao: 'Competência', actividadesAssociadas: [10, 11] }],
        situacao: 'em_andamento',
        dataCriacao: new Date(),
        dataDisponibilizacao: null,
        dataFinalizacao: null,
      },
    ];

    const wrapper = mount(CadMapa, {
      props: { sigla: 'UN1', idProcesso: 1 },
      global: { plugins: [createPinia()] },
    });
    await wrapper.vm.$nextTick();

    const removerAtividadeBtn = wrapper.find('.botao-acao-inline');
    expect(removerAtividadeBtn.exists()).toBe(true);
    await removerAtividadeBtn.trigger('click');
    await wrapper.vm.$nextTick();

    expect(mapasStore.editarMapa).toHaveBeenCalledWith(
      1,
      expect.objectContaining({
        competencias: expect.arrayContaining([
          expect.objectContaining({
            id: 100,
            atividadesAssociadas: [11],
          }),
        ]),
      })
    );
  });

  it('deve abrir o modal de disponibilização e preencher a data limite', async () => {
    mapasStore.mapas = [
      {
        id: 1,
        unidade: 'UN1',
        idProcesso: 1,
        competencias: [{ id: 100, descricao: 'Competência', atividadesAssociadas: [10] }],
        situacao: 'em_andamento',
        dataCriacao: new Date(),
        dataDisponibilizacao: null,
        dataFinalizacao: null,
      },
    ];

    const wrapper = mount(CadMapa, {
      props: { sigla: 'UN1', idProcesso: 1 },
      global: { plugins: [createPinia()] },
    });
    await wrapper.vm.$nextTick();

    const disponibilizarBtn = wrapper.find('button.btn-outline-success');
    expect(disponibilizarBtn.exists()).toBe(true);
    await disponibilizarBtn.trigger('click');
    await wrapper.vm.$nextTick();
    expect(wrapper.find('.modal-dialog').exists()).toBe(true);
    expect(wrapper.find('h5.modal-title').text()).toBe('Disponibilizar Mapa');

    const inputDataLimite = wrapper.find('#dataLimite');
    expect(inputDataLimite.exists()).toBe(true);
    await inputDataLimite.setValue('2025-12-31');
    expect((inputDataLimite.element as HTMLInputElement).value).toBe('2025-12-31');
  });

  it('deve disponibilizar o mapa e exibir notificação', async () => {
    mapasStore.mapas = [
      {
        id: 1,
        unidade: 'UN1',
        idProcesso: 1,
        competencias: [{ id: 100, descricao: 'Competência', atividadesAssociadas: [10] }],
        situacao: 'em_andamento',
        dataCriacao: new Date(),
        dataDisponibilizacao: null,
        dataFinalizacao: null,
      },
    ];

    const wrapper = mount(CadMapa, {
      props: { sigla: 'UN1', idProcesso: 1 },
      global: {
        plugins: [createPinia()],
      },
    });
    await wrapper.vm.$nextTick();

    const disponibilizarBtn = wrapper.find('button.btn-outline-success');
    expect(disponibilizarBtn.exists()).toBe(true);
    await disponibilizarBtn.trigger('click');
    await wrapper.vm.$nextTick();

    const inputDataLimite = wrapper.find('#dataLimite');
    expect(inputDataLimite.exists()).toBe(true);
    await inputDataLimite.setValue('2025-12-31');
    const confirmarDisponibilizarBtn = wrapper.find('button.btn-success');
    expect(confirmarDisponibilizarBtn.exists()).toBe(true);
    await confirmarDisponibilizarBtn.trigger('click');
    await wrapper.vm.$nextTick();

    expect(mapasStore.editarMapa).toHaveBeenCalledWith(
      1,
      expect.objectContaining({
        situacao: 'disponivel_validacao',
      })
    );
    expect(wrapper.find('.alert-info').text()).toContain('O mapa de competências da unidade UN1 foi disponibilizado para validação até 31/12/2025. (Simulação)');
  });
});