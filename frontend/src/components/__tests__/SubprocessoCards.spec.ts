import { describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import SubprocessoCards from '../SubprocessoCards.vue';
import { type Mapa, TipoProcesso, type Unidade, SubprocessoPermissoes } from '@/types/tipos';
import { situacaoLabel } from '@/utils';

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
    currentRoute: {
      value: {
        params: {
          codSubprocesso: 1,
        },
      },
    },
  }),
}));

describe('SubprocessoCards.vue', () => {
  const defaultPermissoes: SubprocessoPermissoes = {
    podeVerPagina: true,
    podeEditarMapa: true,
    podeVisualizarMapa: true,
    podeDisponibilizarCadastro: true,
    podeDevolverCadastro: true,
    podeAceitarCadastro: true,
    podeVisualizarDiagnostico: true,
    podeAlterarDataLimite: true,
  };

  const mountComponent = (props: { tipoProcesso: TipoProcesso; mapa: Mapa | null; situacao?: string; permissoes?: SubprocessoPermissoes }) => {
    return mount(SubprocessoCards, {
      props: {
        permissoes: defaultPermissoes,
        ...props,
      },
    });
  };

  const mockMapa: Mapa = {
    codigo: 1,
    descricao: 'mapa de teste',
    unidade: { sigla: 'UNID_TESTE' } as Unidade,
    situacao: 'em_andamento',
    codProcesso: 1,
    competencias: [],
    dataCriacao: new Date().toISOString(),
    dataDisponibilizacao: null,
    dataFinalizacao: null,
  };

  describe('MAPEAMENTO and REVISAO process types', () => {
    it.each([TipoProcesso.MAPEAMENTO, TipoProcesso.REVISAO])('should render both cards for %s', tipoProcesso => {
      const wrapper = mountComponent({
        tipoProcesso,
        mapa: null,
        situacao: 'Mapa disponibilizado',
      });

      expect(wrapper.findAll('.card')).toHaveLength(2);
      expect(wrapper.text()).toContain('Atividades e conhecimentos');
      expect(wrapper.text()).toContain('Mapa de Competências');
    });

    it('should emit irParaAtividades when atividades card is clicked', async () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null,
        situacao: 'Mapa disponibilizado',
      });

      const atividadesCard = wrapper.find('[data-testid="atividades-card"]');
      expect(atividadesCard.exists()).toBe(true);
    });

    it('should emit navegarParaMapa when mapa card is clicked and mapa exists', async () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: mockMapa,
        situacao: 'Mapa em andamento',
      });

      const mapaCard = wrapper.find('[data-testid="mapa-card"]');
      expect(mapaCard.exists()).toBe(true);
    });
  });

  describe('handleMapaClick function', () => {
    it('should emit navegarParaMapa when handleMapaClick is called', async () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: mockMapa,
        situacao: 'Mapa em andamento',
      });

      const mapaCard = wrapper.find('[data-testid="mapa-card"]');
      expect(mapaCard.exists()).toBe(true);
    });

    it('should handle mapa click for different process types', async () => {
      const testCases = [
        { tipoProcesso: TipoProcesso.MAPEAMENTO },
        { tipoProcesso: TipoProcesso.REVISAO },
      ];

      for (const { tipoProcesso } of testCases) {
        const wrapper = mountComponent({
          tipoProcesso,
          mapa: mockMapa,
          situacao: 'Mapa em andamento',
        });

        const mapaCard = wrapper.find('[data-testid="mapa-card"]');
        expect(mapaCard.exists()).toBe(true);
      }
    });
  });

  describe('mapa card states', () => {
    it('should disable mapa card when mapa is null', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null,
        situacao: 'Mapa disponibilizado',
        permissoes: { ...defaultPermissoes, podeVisualizarMapa: false, podeEditarMapa: false },
      });

      const mapaCard = wrapper.find('[data-testid="mapa-card"]');
      expect(mapaCard.exists()).toBe(false);
    });

    it('should enable mapa card when mapa exists', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: mockMapa,
        situacao: 'Mapa em andamento',
      });

      const mapaCard = wrapper.findAll('.card')[1];
      expect(mapaCard.classes()).not.toContain('disabled-card');
    });

    it('should show EM_ANDAMENTO badge when mapa situacao is EM_ANDAMENTO', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: { ...mockMapa, situacao: 'EM_ANDAMENTO' },
        situacao: 'EM_ANDAMENTO',
      });

      expect(wrapper.find('[data-testid="mapa-card"]').text()).toContain(situacaoLabel('EM_ANDAMENTO'));
    });

    it('should show DISPONIVEL_VALIDACAO badge when mapa situacao is DISPONIVEL_VALIDACAO', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: { ...mockMapa, situacao: 'DISPONIVEL_VALIDACAO' },
        situacao: 'DISPONIVEL_VALIDACAO',
      });

      expect(wrapper.find('[data-testid="mapa-card"]').text()).toContain(situacaoLabel('DISPONIVEL_VALIDACAO'));
    });

    it('should show custom situacao badge when situacao has a custom value', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: { ...mockMapa, situacao: 'CUSTOM' },
        situacao: 'CUSTOM',
      });

      expect(wrapper.find('[data-testid="mapa-card"]').text()).toContain('CUSTOM');
    });
  });

  describe('DIAGNOSTICO process type', () => {
    it('should render diagnostico cards for DIAGNOSTICO type', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.DIAGNOSTICO,
        mapa: null,
      });

      expect(wrapper.findAll('.card')).toHaveLength(2);
      expect(wrapper.text()).toContain('Diagnóstico da Equipe');
      expect(wrapper.text()).toContain('Ocupações Críticas');
    });

    it('should show NAO_DISPONIBILIZADO badges for diagnostico cards', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.DIAGNOSTICO,
        mapa: null,
        permissoes: { ...defaultPermissoes, podeVisualizarDiagnostico: true },
      });

      expect(wrapper.find('[data-testid="diagnostico-card"]').text()).toContain('Não disponibilizado');
      expect(wrapper.find('[data-testid="ocupacoes-card"]').text()).toContain('Não disponibilizado');
    });
  });

  describe('card content', () => {
    it('should render atividades card with correct content', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null,
      });

      const atividadesCard = wrapper.find('[data-testid="atividades-card"]');
      expect(atividadesCard.text()).toContain('Atividades e conhecimentos');
      expect(atividadesCard.text()).toContain('Cadastro de atividades e conhecimentos da unidade');
    });

    it('should render mapa card with correct content', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null,
      });

      const mapaCard = wrapper.findAll('.card')[1];
      expect(mapaCard.text()).toContain('Mapa de Competências');
      expect(mapaCard.text()).toContain('Mapa de competências técnicas da unidade');
    });

    it('should render diagnostico card with correct content', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.DIAGNOSTICO,
        mapa: null,
      });

      const diagnosticoCard = wrapper.findAll('.card')[0];
      expect(diagnosticoCard.text()).toContain('Diagnóstico da Equipe');
      expect(diagnosticoCard.text()).toContain('Diagnóstico das competências pelos servidores da unidade');
    });

    it('should render ocupacoes card with correct content', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.DIAGNOSTICO,
        mapa: null,
      });

      const ocupacoesCard = wrapper.findAll('.card')[1];
      expect(ocupacoesCard.text()).toContain('Ocupações Críticas');
      expect(ocupacoesCard.text()).toContain('Identificação das ocupações críticas da unidade');
    });
  });

  describe('styling and structure', () => {
    it('should have correct card classes', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null,
      });

      const cards = wrapper.findAll('.card');
      cards.forEach(card => {
        expect(card.classes()).toContain('card');
        expect(card.classes()).toContain('h-100');
        expect(card.classes()).toContain('card-actionable');
      });
    });

    it('should have correct row structure', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null,
      });

      expect(wrapper.find('.row').exists()).toBe(true);
      expect(wrapper.findAll('.col-md-4')).toHaveLength(2);
    });

    it('should have correct badge structure', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null,
      });

      const badges = wrapper.findAll('.badge');
      badges.forEach(badge => {
        expect(badge.classes()).toContain('badge');
      });
    });
  });

  describe('edge cases', () => {
    it('should handle null mapa prop', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null,
      });

      expect(wrapper.findAll('.card')).toHaveLength(2);
      const mapaCard = wrapper.findAll('.card')[1];
      expect(mapaCard.classes()).toContain('disabled-card');
    });

    it('should handle empty mapa object', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: mockMapa,
      });

      const mapaCard = wrapper.findAll('.card')[1];
      // Empty object is truthy, so card should not be disabled
      expect(mapaCard.classes()).not.toContain('disabled-card');
    });

    it('should handle undefined situacao prop', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: mockMapa,
        // situacao is undefined
      });

      // Should show default "Disponibilizado" badge when situacao is undefined
      expect(wrapper.find('[data-testid="mapa-card"]').text()).toContain(situacaoLabel(undefined));
    });

    it('should handle empty string situacao', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: mockMapa,
        situacao: '', // Empty string
      });

      // Should show default "Disponibilizado" badge when situacao is empty string
      expect(wrapper.find('[data-testid="mapa-card"]').text()).toContain(situacaoLabel(''));
    });
  });
});