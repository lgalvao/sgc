import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ImpactoMapaModal from '../ImpactoMapaModal.vue';
import { TipoMudanca } from '@/stores/revisao';

// Mock data
const mockUnidade = { nome: 'Unidade Teste', sigla: 'UT' };
const mockProcesso = { id: 1, nome: 'Processo Teste' };
const mockMapa = {
  competencias: [
    { codigo: 101, descricao: 'Competência A', atividadesAssociadas: [201] },
    { codigo: 102, descricao: 'Competência B', atividadesAssociadas: [202] },
  ],
  atividades: [
    { codigo: 201, descricao: 'Atividade X' },
    { codigo: 202, descricao: 'Atividade Y' },
  ],
};
const mockMudancas = [
  { id: 1, tipo: TipoMudanca.AtividadeAdicionada, idAtividade: 203, descricaoAtividade: 'Nova Atividade' },
  { id: 2, tipo: TipoMudanca.ConhecimentoAdicionado, idAtividade: 203, descricaoConhecimento: 'Novo Conhecimento' },
  { id: 3, tipo: TipoMudanca.AtividadeRemovida, idAtividade: 202, descricaoAtividade: 'Atividade Y' },
];

// Mock stores
vi.mock('@/stores/unidades', () => ({
  useUnidadesStore: () => ({
    pesquisarUnidade: () => mockUnidade,
  }),
}));
vi.mock('@/stores/processos', () => ({
  useProcessosStore: () => ({
    processoDetalhe: mockProcesso,
    fetchProcessoDetalhe: vi.fn().mockResolvedValue(true),
  }),
}));
vi.mock('@/stores/mapas', () => ({
  useMapasStore: () => ({
    mapaCompleto: mockMapa,
    fetchMapaCompleto: vi.fn().mockResolvedValue(true),
  }),
}));
vi.mock('@/stores/revisao', () => {
  const TipoMudanca = {
    AtividadeAdicionada: 0,
    AtividadeRemovida: 1,
    AtividadeAlterada: 2,
    ConhecimentoAdicionado: 3,
    ConhecimentoRemovido: 4,
    ConhecimentoAlterado: 5,
  };

  return {
    useRevisaoStore: () => ({
      mudancasParaImpacto: mockMudancas,
    }),
    TipoMudanca,
  };
});

describe('ImpactoMapaModal', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('não deve renderizar o modal quando mostrar for falso', () => {
    const wrapper = mount(ImpactoMapaModal, {
      props: { mostrar: false, idProcesso: 1, siglaUnidade: 'UT' },
    });
    expect(wrapper.find('[data-testid="impacto-mapa-modal"]').exists()).toBe(false);
  });

  it('deve renderizar a seção de atividades inseridas corretamente', () => {
    const wrapper = mount(ImpactoMapaModal, {
      props: { mostrar: true, idProcesso: 1, siglaUnidade: 'UT' },
    });

    const secao = wrapper.find('[data-testid="secao-atividades-inseridas"]');
    expect(secao.exists()).toBe(true);
    expect(secao.text()).toContain('Nova Atividade');
    expect(secao.text()).toContain('Novo Conhecimento');
  });

  it('deve renderizar a seção de competências impactadas corretamente', () => {
    const wrapper = mount(ImpactoMapaModal, {
      props: { mostrar: true, idProcesso: 1, siglaUnidade: 'UT' },
    });

    const secao = wrapper.find('[data-testid="titulo-competencias-impactadas"]');
    expect(secao.exists()).toBe(true);

    const competencias = wrapper.findAll('.card');
    expect(competencias.length).toBe(1);
    expect(competencias[0].text()).toContain('Competência B');
    expect(competencias[0].text()).toContain('Atividade removida: Atividade Y');
  });

  it('deve emitir o evento fechar ao clicar no botão de fechar', async () => {
    const wrapper = mount(ImpactoMapaModal, {
      props: { mostrar: true, idProcesso: 1, siglaUnidade: 'UT' },
    });

    await wrapper.find('[data-testid="fechar-impactos-mapa-button"]').trigger('click');
    expect(wrapper.emitted('fechar')).toBeTruthy();
  });
});