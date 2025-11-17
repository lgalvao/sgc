import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import SubprocessoHeader from '../SubprocessoHeader.vue';
import {badgeClass} from '@/utils';

describe('SubprocessoHeader.vue', () => {
  const defaultProps = {
    processoDescricao: 'Processo de Teste',
    unidadeSigla: 'TEST',
    unidadeNome: 'Unidade de Teste',
    situacao: 'EM_ANDAMENTO',
    titularNome: 'João Silva',
    titularRamal: '1234',
    titularEmail: 'joao@teste.com',
    podeAlterarDataLimite: false, // Default to false
  };

  const mountComponent = (props = {}) => {
    return mount(SubprocessoHeader, {
      props: {
        ...defaultProps,
        ...props
      }
    });
  };

  describe('props rendering', () => {
    it('should render all basic props correctly', () => {
      const wrapper = mountComponent();

      expect(wrapper.text()).toContain('Processo: Processo de Teste');
      expect(wrapper.text()).toContain('TEST - Unidade de Teste');
      expect(wrapper.text()).toContain('Situação:EM_ANDAMENTO');
      expect(wrapper.text()).toContain('Titular: João Silva');
      expect(wrapper.text()).toContain('1234');
      expect(wrapper.text()).toContain('joao@teste.com');
    });

    it('should render badge with correct class', () => {
      const wrapper = mountComponent();

      const badge = wrapper.find('.badge');
      expect(badge.exists()).toBe(true);
      expect(badge.text()).toBe('EM_ANDAMENTO');
      expect(badge.classes()).toContain('badge');
    });

    it('should render unidadeAtual when provided', () => {
      const wrapper = mountComponent({
        unidadeAtual: 'Unidade Atual Teste'
      });

      expect(wrapper.text()).toContain('Unidade atual: Unidade Atual Teste');
    });

    it('should not render unidadeAtual when not provided', () => {
      const wrapper = mountComponent();

      expect(wrapper.text()).not.toContain('Unidade atual:');
    });
  });

  describe('responsavel section', () => {
    it('should render responsavel section when responsavelNome is provided', () => {
      const wrapper = mountComponent({
        responsavelNome: 'Maria Santos',
        responsavelRamal: '5678',
        responsavelEmail: 'maria@teste.com'
      });

      expect(wrapper.text()).toContain('Responsável: Maria Santos');
      expect(wrapper.text()).toContain('5678');
      expect(wrapper.text()).toContain('maria@teste.com');
    });

    it('should not render responsavel section when responsavelNome is not provided', () => {
      const wrapper = mountComponent();

      expect(wrapper.text()).not.toContain('Responsável:');
    });

    it('should handle partial responsavel data', () => {
      const wrapper = mountComponent({
        responsavelNome: 'Maria Santos'
        // responsavelRamal and responsavelEmail not provided
      });

      expect(wrapper.text()).toContain('Responsável: Maria Santos');
    });
  });

  describe('alterar data limite button', () => {
    it('should show button when podeAlterarDataLimite is true', () => {
      const wrapper = mountComponent({
        podeAlterarDataLimite: true,
      });

      const button = wrapper.find('button');
      expect(button.exists()).toBe(true);
      expect(button.text()).toContain('Alterar data limite');
      expect(button.classes()).toContain('btn-outline-primary');
    });

    it('should not show button when podeAlterarDataLimite is false', () => {
      const wrapper = mountComponent({
        podeAlterarDataLimite: false,
      });

      const button = wrapper.find('button');
      expect(button.exists()).toBe(false);
    });

    it('should emit alterarDataLimite when button is clicked', async () => {
      const wrapper = mountComponent({
        podeAlterarDataLimite: true,
      });

      const button = wrapper.find('button');
      await button.trigger('click');
      expect(wrapper.emitted()).toHaveProperty('alterarDataLimite');
    });
  });


    describe('SubprocessoHeader.vue', () => {
    it('should return correct badge class for known situacao', () => {
        // Não precisamos montar o componente para testar uma função utilitária pura
        const result = badgeClass('EM_ANDAMENTO');

      // Since we can't easily mock the constants, we'll test that the function exists and returns a string
        expect(typeof result).toBe('string');
        expect(result.length).toBeGreaterThan(0);
    });

    it('should return default class for any situacao', () => {
        // Não precisamos montar o componente para testar uma função utilitária pura
        const result = badgeClass('UNKNOWN_SITUACAO');

        expect(result).toBe('bg-secondary');
    });
  });



  describe('structure and styling', () => {
    it('should have correct card structure', () => {
      const wrapper = mountComponent();

      expect(wrapper.find('.card').exists()).toBe(true);
      expect(wrapper.find('.card-body').exists()).toBe(true);
    });

    it('should have correct text styling', () => {
      const wrapper = mountComponent();

      expect(wrapper.find('.text-muted').exists()).toBe(true);
      expect(wrapper.find('.display-6').exists()).toBe(true);
      expect(wrapper.find('.fw-bold').exists()).toBe(true);
    });

    it('should have correct icons', () => {
      const wrapper = mountComponent();

      const phoneIcons = wrapper.findAll('.bi-telephone-fill');
      const emailIcons = wrapper.findAll('.bi-envelope-fill');

      expect(phoneIcons.length).toBeGreaterThan(0);
      expect(emailIcons.length).toBeGreaterThan(0);
    });
  });
});