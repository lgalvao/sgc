import {describe, it, expect} from 'vitest';
import {mount} from '@vue/test-utils';
import VisAtividadeItem from '../VisAtividadeItem.vue';

describe('VisAtividadeItem.vue', () => {
  it('renderiza atividade e conhecimentos', () => {
    const atividade = {
        codigo: 1,
        descricao: 'Atividade Teste',
        conhecimentos: [
            { codigo: 10, descricao: 'Conhecimento 1' },
            { codigo: 11, descricao: 'Conhecimento 2' }
        ]
    };

    const wrapper = mount(VisAtividadeItem, {
      props: { atividade },
      global: {
          stubs: {
              BCard: { template: '<div><slot /></div>' },
              BCardBody: { template: '<div><slot /></div>' }
          }
      }
    });

    expect(wrapper.find('[data-testid="txt-atividade-descricao"]').text()).toBe('Atividade Teste');
    expect(wrapper.findAll('[data-testid="txt-conhecimento-descricao"]')).toHaveLength(2);
    expect(wrapper.text()).toContain('Conhecimento 1');
    expect(wrapper.text()).toContain('Conhecimento 2');
  });
});
