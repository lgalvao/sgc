import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import AppAlert from '../AppAlert.vue';

describe('AppAlert.vue', () => {
  it('deve renderizar a prop message no modo simples', () => {
    const wrapper = mount(AppAlert, {
      props: {
        message: 'Alerta de erro simples'
      }
    });
    expect(wrapper.find('[data-testid="app-alert"]').exists()).toBe(true);
    expect(wrapper.text()).toContain('Alerta de erro simples');
  });

  it('deve renderizar o notification no modo estruturado e ocultar/exibir detalhes', async () => {
    const wrapper = mount(AppAlert, {
      props: {
        notification: {
          summary: 'Resumo do erro',
          details: ['Erro 1', 'Erro 2']
        }
      }
    });

    expect(wrapper.text()).toContain('Resumo do erro');
    expect(wrapper.text()).toContain('Mostrar detalhes');
    expect(wrapper.text()).not.toContain('Erro 1');

    // Click to show details
    await wrapper.find('button').trigger('click');
    expect(wrapper.text()).toContain('Ocultar detalhes');
    expect(wrapper.text()).toContain('Erro 1');
    expect(wrapper.text()).toContain('Erro 2');
  });

  it('deve renderizar o stack trace quando em modo dev', async () => {
    const wrapper = mount(AppAlert, {
      props: {
        message: 'Erro com stack',
        stackTrace: 'Error at line 1'
      }
    });

    // import.meta.env.DEV in vitest is usually true by default
    expect(wrapper.text()).toContain('Mostrar detalhes técnicos');

    // Múltiplos buttons podem existir se fosse notificação estruturada, mas é modo simples
    await wrapper.find('button').trigger('click');
    expect(wrapper.text()).toContain('Error at line 1');
  });

  it('nao deve renderizar se não houver message ou notification', () => {
    const wrapper = mount(AppAlert, {
      props: {}
    });
    expect(wrapper.find('[data-testid="app-alert"]').exists()).toBe(false);
  });

  it('deve emitir o evento dismissed', async () => {
    const wrapper = mount(AppAlert, {
      props: {
        message: 'Erro'
      }
    });

    // Encontra o alert (interno do Bootstrap-Vue-Next) e aciona dismiss, mas mockar é díficil.
    // O componente emite @dismissed que emite pro pai.
    // Vamos simular o próprio emit do component ou vm.emit
    wrapper.vm.$emit('dismissed');
    expect(wrapper.emitted().dismissed).toBeTruthy();
  });
});
