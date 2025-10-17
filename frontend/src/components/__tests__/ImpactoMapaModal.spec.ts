import {mount} from '@vue/test-utils';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import ImpactoMapaModal from '../ImpactoMapaModal.vue';
import {initPinia} from '@/test-utils/helpers';
import {TipoMudanca} from '@/stores/revisao';
import type {Atividade} from '@/models/atividade';
import type {Competencia} from '@/models/competencia';
import type {Conhecimento} from '@/models/conhecimento';
import type {Mapa} from '@/models/mapa';
import type {Processo} from '@/models/processo';
import type {Unidade} from '@/models/unidade';


// Mock de dados simplificados
const mockUnidade: Unidade = {
  codigo: 1,
  sigla: 'UNID1',
  nome: 'Unidade 1',
} as Unidade; // Type assertion para evitar a necessidade de todas as propriedades

const mockProcesso: Processo = {
  codigo: 100,
  descricao: 'Processo de Teste',
} as Processo;

const mockCompetencia1: Competencia = {
  codigo: 1,
  descricao: 'Competencia 1',
  atividadesAssociadas: [10, 11],
} as Competencia;

const mockCompetencia2: Competencia = {
  codigo: 2,
  descricao: 'Competencia 2',
  atividadesAssociadas: [12],
} as Competencia;

const mockMapa: Mapa = {
  codigo: 1,
  idUnidade: 1,
  idProcesso: 100,
  competencias: [mockCompetencia1, mockCompetencia2],
  atividades: [
    { codigo: 10, descricao: 'Atividade Existente 1' } as Atividade,
    { codigo: 11, descricao: 'Atividade Existente 2' } as Atividade,
  ],
} as unknown as Mapa;

const mockAtividadeAdicionada: Atividade = {
  codigo: 20,
  descricao: 'Nova Atividade',
} as Atividade;

const mockConhecimentoAdicionado: Conhecimento = {
  codigo: 30,
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
    initPinia();

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
        codigo: 1,
        tipo: TipoMudanca.AtividadeAdicionada,
        descricaoAtividade: mockAtividadeAdicionada.descricao,
        idAtividade: mockAtividadeAdicionada.codigo,
        competenciasImpactadasIds: [],
      },
      {
        codigo: 2,
        tipo: TipoMudanca.ConhecimentoAdicionado,
        descricaoConhecimento: mockConhecimentoAdicionado.descricao,
        idAtividade: mockAtividadeAdicionada.codigo,
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
        codigo: 3,
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
        codigo: 4,
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

  it('exibe atividades alteradas corretamente', async () => {
    mockRevisaoStore.mudancasParaImpacto = [
      {
        codigo: 5,
        tipo: TipoMudanca.AtividadeAlterada,
        descricaoAtividade: 'Atividade Alterada',
        idAtividade: 10,
        valorAntigo: 'Valor Anterior',
        valorNovo: 'Valor Novo',
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

    expect(wrapper.html()).toContain('Atividade alterada: <strong>Atividade Alterada</strong>');
    expect(wrapper.html()).toContain('De "Valor Anterior" para "Valor Novo"');
  });

  it('exibe conhecimentos alterados corretamente', async () => {
    mockRevisaoStore.mudancasParaImpacto = [
      {
        codigo: 6,
        tipo: TipoMudanca.ConhecimentoAlterado,
        descricaoConhecimento: 'Conhecimento Alterado',
        idAtividade: 10,
        valorAntigo: 'Valor Anterior',
        valorNovo: 'Valor Novo',
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

    expect(wrapper.html()).toContain('Conhecimento alterado: <strong>Conhecimento Alterado</strong>');
    expect(wrapper.html()).toContain('De "Valor Anterior" para "Valor Novo"');
  });

  it('exibe competências impactadas por IDs explícitos', async () => {
    mockRevisaoStore.mudancasParaImpacto = [
      {
        codigo: 7,
        tipo: TipoMudanca.AtividadeRemovida,
        descricaoAtividade: 'Atividade Removida',
        idAtividade: 10,
        competenciasImpactadasIds: [1], // Explicitamente marca competência 1 como impactada
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
    expect(wrapper.html()).toContain('Atividade removida: <strong>Atividade Removida</strong>');
  });

  it('retorna mapa vazio quando não há mapa para a unidade/processo', async () => {
    mockMapasStore.getMapaByUnidadeId.mockReturnValue(null);

    const wrapper = mount(ImpactoMapaModal, {
      props: {
        mostrar: true,
        idProcesso: 100,
        siglaUnidade: 'UNID1',
      },
    });
    await wrapper.vm.$nextTick();

    // Verificar que não há competências impactadas quando não há mapa
    expect(wrapper.find('[data-testid="msg-nenhuma-competencia"]').exists()).toBe(true);
  });

  it('processa competências impactadas por atividades associadas', async () => {
    mockRevisaoStore.mudancasParaImpacto = [
      {
        codigo: 8,
        tipo: TipoMudanca.ConhecimentoAdicionado,
        descricaoConhecimento: 'Novo Conhecimento',
        idAtividade: 10, // Atividade 10 está associada à Competencia 1
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
    expect(wrapper.html()).toContain('Conhecimento adicionado: <strong>Novo Conhecimento</strong>');
  });
});