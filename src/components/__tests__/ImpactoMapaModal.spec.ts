import {mount} from '@vue/test-utils';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import ImpactoMapaModal from '../ImpactoMapaModal.vue';
import {createPinia, setActivePinia} from 'pinia';
import {TipoMudanca} from '@/stores/revisao';
import type {Atividade, Competencia, Conhecimento, Mapa, Processo, Unidade} from '@/types/tipos';

// Mock de dados simplificados
const mockUnidade: Unidade = {
  id: 1,
  sigla: 'UNID1',
  nome: 'Unidade 1',
} as Unidade; // Type assertion para evitar a necessidade de todas as propriedades

const mockProcesso: Processo = {
  id: 100,
  descricao: 'Processo de Teste',
} as Processo;

const mockCompetencia1: Competencia = {
  id: 1,
  descricao: 'Competencia 1',
  atividadesAssociadas: [10, 11],
} as Competencia;

const mockCompetencia2: Competencia = {
  id: 2,
  descricao: 'Competencia 2',
  atividadesAssociadas: [12],
} as Competencia;

const mockMapa: Mapa = {
  id: 1,
  idUnidade: 1,
  idProcesso: 100,
  competencias: [mockCompetencia1, mockCompetencia2],
  atividades: [
    { id: 10, descricao: 'Atividade Existente 1' } as Atividade,
    { id: 11, descricao: 'Atividade Existente 2' } as Atividade,
  ],
} as unknown as Mapa;

const mockAtividadeAdicionada: Atividade = {
  id: 20,
  descricao: 'Nova Atividade',
} as Atividade;

const mockConhecimentoAdicionado: Conhecimento = {
  id: 30,
  descricao: 'Novo Conhecimento',
  idAtividade: 20,
} as Conhecimento;

// Mocks das stores
const mockUnidadesStore = {
  pesquisarUnidade: vi.fn(),
};

const mockProcessosStore = {
  processos: [],
};

const mockMapasStore = {
  getMapaByUnidadeId: vi.fn(),
};

const mockRevisaoStore = {
  mudancasParaImpacto: [],
  TipoMudanca: {
    AtividadeAdicionada: 'AtividadeAdicionada',
    AtividadeRemovida: 'AtividadeRemovida',
    ConhecimentoAdicionado: 'ConhecimentoAdicionado',
  },
};

vi.mock('@/stores/unidades', () => ({
  useUnidadesStore: () => mockUnidadesStore,
}));

vi.mock('@/stores/processos', () => ({
  useProcessosStore: () => mockProcessosStore,
}));

vi.mock('@/stores/mapas', () => ({
  useMapasStore: () => mockMapasStore,
}));

vi.mock('@/stores/revisao', async (importOriginal) => {
  const mod = await importOriginal();
  return {
    ...(mod as object),
    useRevisaoStore: () => mockRevisaoStore,
  };
});

describe('ImpactoMapaModal.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia());

    // Inicializar e configurar os mocks
    mockUnidadesStore.pesquisarUnidade = vi.fn(() => mockUnidade);
    mockProcessosStore.processos = [mockProcesso];
    mockMapasStore.getMapaByUnidadeId = vi.fn(() => mockMapa);
    mockRevisaoStore.mudancasParaImpacto = [];
    mockRevisaoStore.TipoMudanca = {
      AtividadeAdicionada: 'AtividadeAdicionada',
      AtividadeRemovida: 'AtividadeRemovida',
      ConhecimentoAdicionado: 'ConhecimentoAdicionado',
    };
  });

  it('não renderiza o modal quando "mostrar" é falso', () => {
    const wrapper = mount(ImpactoMapaModal, {
      props: {
        mostrar: false,
        idProcesso: 100,
        siglaUnidade: 'UNID1',
      },
    });
    expect(wrapper.find('[data-testid="impacto-mapa-modal"]').exists()).toBe(false);
  });

  it('renderiza o modal quando "mostrar" é verdadeiro', async () => {
    const wrapper = mount(ImpactoMapaModal, {
      props: {
        mostrar: true,
        idProcesso: 100,
        siglaUnidade: 'UNID1',
      },
    });
    await wrapper.vm.$nextTick(); // Garante que o DOM foi atualizado
    expect(wrapper.find('[data-testid="impacto-mapa-modal"]').exists()).toBe(true);
  });

  it('exibe spinner de carregamento se unidade ou processo não estiverem disponíveis', async () => {
    mockUnidadesStore.pesquisarUnidade.mockReturnValue(undefined); // Sobrescreve o mock para este teste

    const wrapper = mount(ImpactoMapaModal, {
      props: {
        mostrar: true,
        idProcesso: 100,
        siglaUnidade: 'UNID1',
      },
    });
    await wrapper.vm.$nextTick();
    expect(wrapper.find('.spinner-border').exists()).toBe(true);
    expect(wrapper.find('p').text()).toBe('Carregando informações do processo e unidade...');
  });

  it('exibe atividades inseridas corretamente', async () => {
    mockRevisaoStore.mudancasParaImpacto = [
      {
        id: 1,
        tipo: TipoMudanca.AtividadeAdicionada,
        descricaoAtividade: mockAtividadeAdicionada.descricao,
        idAtividade: mockAtividadeAdicionada.id,
        competenciasImpactadasIds: [],
      },
      {
        id: 2,
        tipo: TipoMudanca.ConhecimentoAdicionado,
        descricaoConhecimento: mockConhecimentoAdicionado.descricao,
        idAtividade: mockAtividadeAdicionada.id,
        competenciasImpactadasIds: [],
      },
    ];

    const wrapper = mount(ImpactoMapaModal, {
      props: {
        mostrar: true,
        idProcesso: 100,
        siglaUnidade: 'UNID1',
      },
    });
    await wrapper.vm.$nextTick();

    expect(wrapper.find('[data-testid="secao-atividades-inseridas"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="titulo-atividades-inseridas"]').text()).toContain('Atividades inseridas');
    expect(wrapper.html()).toContain(mockAtividadeAdicionada.descricao);
    expect(wrapper.find('[data-testid="label-conhecimentos-adicionados"]').exists()).toBe(true);
    expect(wrapper.html()).toContain(mockConhecimentoAdicionado.descricao);
  });

  it('exibe mensagem de "Nenhuma competência foi impactada" quando não há mudanças', async () => {
    const wrapper = mount(ImpactoMapaModal, {
      props: {
        mostrar: true,
        idProcesso: 100,
        siglaUnidade: 'UNID1',
      },
    });
    await wrapper.vm.$nextTick();
    expect(wrapper.find('[data-testid="msg-nenhuma-competencia"]').exists()).toBe(true);
    expect(wrapper.find('[data-testid="msg-nenhuma-competencia"]').text()).toBe('Nenhuma competência foi impactada.');
  });

  it('exibe competências impactadas por atividade removida', async () => {
    mockRevisaoStore.mudancasParaImpacto = [
      {
        id: 3,
        tipo: TipoMudanca.AtividadeRemovida,
        descricaoAtividade: 'Atividade Removida',
        idAtividade: 10, // Atividade 10 está associada à Competencia 1 no mockMapa
        competenciasImpactadasIds: [],
      },
    ];

    const wrapper = mount(ImpactoMapaModal, {
      props: {
        mostrar: true,
        idProcesso: 100,
        siglaUnidade: 'UNID1',
      },
    });
    await wrapper.vm.$nextTick();

    expect(wrapper.find('[data-testid="titulo-competencias-impactadas"]').exists()).toBe(true);
    expect(wrapper.html()).toContain('Competencia 1');
    expect(wrapper.html()).toContain('Atividade removida: <strong>Atividade Removida</strong>');
  });

  it('exibe competências impactadas por conhecimento adicionado', async () => {
    mockRevisaoStore.mudancasParaImpacto = [
      {
        id: 4,
        tipo: TipoMudanca.ConhecimentoAdicionado,
        descricaoConhecimento: 'Conhecimento Adicionado',
        idAtividade: 10, // Atividade 10 está associada à Competencia 1 no mockMapa
        competenciasImpactadasIds: [],
      },
    ];

    const wrapper = mount(ImpactoMapaModal, {
      props: {
        mostrar: true,
        idProcesso: 100,
        siglaUnidade: 'UNID1',
      },
    });
    await wrapper.vm.$nextTick();

    expect(wrapper.html()).toContain('Competencia 1');
    expect(wrapper.html()).toContain('Conhecimento adicionado: <strong>Conhecimento Adicionado</strong>');
  });

  it('emite evento "fechar" ao clicar no botão de fechar', async () => {
    const wrapper = mount(ImpactoMapaModal, {
      props: {
        mostrar: true,
        idProcesso: 100,
        siglaUnidade: 'UNID1',
      },
    });
    await wrapper.vm.$nextTick();
    await wrapper.find('[data-testid="fechar-impactos-mapa-button"]').trigger('click');
    expect(wrapper.emitted('fechar')).toHaveLength(1);
  });

  it('emite evento "fechar" ao clicar no botão "x" do modal', async () => {
    const wrapper = mount(ImpactoMapaModal, {
      props: {
        mostrar: true,
        idProcesso: 100,
        siglaUnidade: 'UNID1',
      },
    });
    await wrapper.vm.$nextTick();
    await wrapper.find('.btn-close').trigger('click');
    expect(wrapper.emitted('fechar')).toHaveLength(1);
  });
});