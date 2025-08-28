import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import SubprocessoCards from '../SubprocessoCards.vue';
import { TipoProcesso } from '@/types/tipos';

describe('SubprocessoCards.vue', () => {
  const mountComponent = (props: { tipoProcesso: TipoProcesso; mapa: any; situacao?: string }) => {
    return mount(SubprocessoCards, {
      props
    });
  };

  describe('MAPEAMENTO and REVISAO process types', () => {
    it.each([TipoProcesso.MAPEAMENTO, TipoProcesso.REVISAO])('should render both cards for %s', (tipoProcesso) => {
      const wrapper = mountComponent({
        tipoProcesso,
        mapa: null,
        situacao: 'Mapa disponibilizado'
      });

      expect(wrapper.findAll('.card')).toHaveLength(2);
      expect(wrapper.text()).toContain('Atividades e conhecimentos');
      expect(wrapper.text()).toContain('Mapa de Competências');
    });

    it('should emit irParaAtividades when atividades card is clicked', async () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null,
        situacao: 'Mapa disponibilizado'
      });

      const atividadesCard = wrapper.findAll('.card')[0];
      await atividadesCard.trigger('click');

      expect(wrapper.emitted()).toHaveProperty('irParaAtividades');
      expect(wrapper.emitted('irParaAtividades')).toHaveLength(1);
    });

    it('should emit navegarParaMapa when mapa card is clicked and mapa exists', async () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: { situacao: 'em_andamento' },
        situacao: 'Mapa em andamento'
      });

      const mapaCard = wrapper.findAll('.card')[1];
      await mapaCard.trigger('click');

      expect(wrapper.emitted()).toHaveProperty('navegarParaMapa');
      expect(wrapper.emitted('navegarParaMapa')).toHaveLength(1);
    });
  });

  describe('mapa card states', () => {
    it('should disable mapa card when mapa is null', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null,
        situacao: 'Mapa disponibilizado'
      });

      const mapaCard = wrapper.findAll('.card')[1];
      expect(mapaCard.classes()).toContain('disabled-card');
      expect(wrapper.text()).toContain('Não disponibilizado');
    });

    it('should enable mapa card when mapa exists', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: { situacao: 'em_andamento' },
        situacao: 'Mapa em andamento'
      });

      const mapaCard = wrapper.findAll('.card')[1];
      expect(mapaCard.classes()).not.toContain('disabled-card');
    });

    it('should show EM_ANDAMENTO badge when mapa situacao is EM_ANDAMENTO', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: { situacao: 'em_andamento' },
        situacao: 'Mapa em andamento'
      });

      expect(wrapper.text()).toContain('Em andamento');
      expect(wrapper.find('.badge.bg-warning').exists()).toBe(true);
    });

    it('should show DISPONIVEL_VALIDACAO badge when mapa situacao is DISPONIVEL_VALIDACAO', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: { situacao: 'disponivel_validacao' },
        situacao: 'Mapa disponibilizado'
      });

      expect(wrapper.text()).toContain('Disponibilizado');
      expect(wrapper.find('.badge.bg-success').exists()).toBe(true);
    });
  });

  describe('DIAGNOSTICO process type', () => {
    it('should render diagnostico cards for DIAGNOSTICO type', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.DIAGNOSTICO,
        mapa: null
      });

      expect(wrapper.findAll('.card')).toHaveLength(2);
      expect(wrapper.text()).toContain('Diagnóstico da Equipe');
      expect(wrapper.text()).toContain('Ocupações Críticas');
    });

    it('should show NAO_DISPONIBILIZADO badges for diagnostico cards', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.DIAGNOSTICO,
        mapa: null
      });

      const badges = wrapper.findAll('.badge.bg-secondary');
      expect(badges).toHaveLength(2);
      expect(wrapper.text()).toContain('Não disponibilizado');
    });
  });

  describe('card content', () => {
    it('should render atividades card with correct content', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null
      });

      const atividadesCard = wrapper.findAll('.card')[0];
      expect(atividadesCard.text()).toContain('Atividades e conhecimentos');
      expect(atividadesCard.text()).toContain('Cadastro de atividades e conhecimentos da unidade');
      expect(atividadesCard.text()).toContain('Disponibilizado');
    });

    it('should render mapa card with correct content', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null
      });

      const mapaCard = wrapper.findAll('.card')[1];
      expect(mapaCard.text()).toContain('Mapa de Competências');
      expect(mapaCard.text()).toContain('Mapa de competências técnicas da unidade');
    });

    it('should render diagnostico card with correct content', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.DIAGNOSTICO,
        mapa: null
      });

      const diagnosticoCard = wrapper.findAll('.card')[0];
      expect(diagnosticoCard.text()).toContain('Diagnóstico da Equipe');
      expect(diagnosticoCard.text()).toContain('Diagnóstico das competências pelos servidores da unidade');
    });

    it('should render ocupacoes card with correct content', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.DIAGNOSTICO,
        mapa: null
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
        mapa: null
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
        mapa: null
      });

      expect(wrapper.find('.row').exists()).toBe(true);
      expect(wrapper.findAll('.col-md-4')).toHaveLength(2);
    });

    it('should have correct badge structure', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null
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
        mapa: null
      });

      expect(wrapper.findAll('.card')).toHaveLength(2);
      const mapaCard = wrapper.findAll('.card')[1];
      expect(mapaCard.classes()).toContain('disabled-card');
    });

    it('should handle empty mapa object', () => {
      const wrapper = mountComponent({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: {}
      });

      const mapaCard = wrapper.findAll('.card')[1];
      // Empty object is truthy, so card should not be disabled
      expect(mapaCard.classes()).not.toContain('disabled-card');
    });
  });
});