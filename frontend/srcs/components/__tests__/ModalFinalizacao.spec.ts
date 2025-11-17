import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import ModalFinalizacao from '../ModalFinalizacao.vue';

describe('ModalFinalizacao', () => {
  const processoDescricao = 'Processo de Teste';

  it('não deve renderizar o modal quando mostrar for falso', () => {
    const wrapper = mount(ModalFinalizacao, {
      props: { mostrar: false, processoDescricao },
    });
    expect(wrapper.find('b-modal-stub').exists()).toBe(false);
  });

  it('deve renderizar o modal com a descrição do processo', () => {
    const wrapper = mount(ModalFinalizacao, {
      props: { mostrar: true, processoDescricao },
    });
    expect(wrapper.text()).toContain(processoDescricao);
  });

  it('deve emitir "fechar" ao clicar no botão de cancelar', async () => {
    const wrapper = mount(ModalFinalizacao, {
      props: { mostrar: true, processoDescricao },
    });
    await wrapper.find('[data-testid="btn-cancelar-finalizacao"]').trigger('click');
    expect(wrapper.emitted('fechar')).toBeTruthy();
  });

  it('deve emitir "confirmar" ao clicar no botão de confirmar', async () => {
    const wrapper = mount(ModalFinalizacao, {
      props: { mostrar: true, processoDescricao },
    });
    await wrapper.find('[data-testid="btn-confirmar-finalizacao"]').trigger('click');
    expect(wrapper.emitted('confirmar')).toBeTruthy();
  });
});
