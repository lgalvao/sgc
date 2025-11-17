import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import DisponibilizarMapaModal from '../DisponibilizarMapaModal.vue';

describe('DisponibilizarMapaModal', () => {
  it('não deve renderizar o modal quando mostrar for falso', () => {
    const wrapper = mount(DisponibilizarMapaModal, {
      props: {
        mostrar: false,
      },
    });
    expect(wrapper.find('[data-testid="input-data-limite"]').exists()).toBe(false);
  });

  it('deve renderizar o modal com os campos iniciais', () => {
    const wrapper = mount(DisponibilizarMapaModal, {
      props: {
        mostrar: true,
      },
    });

    const dataInput = wrapper.find('[data-testid="input-data-limite"]');
    expect(dataInput.exists()).toBe(true);
    expect((dataInput.element as HTMLInputElement).value).toBe('');

    const disponibilizarButton = wrapper.find('[data-testid="btn-disponibilizar"]');
    expect(disponibilizarButton.attributes('disabled')).toBeDefined();
  });

  it('deve habilitar o botão de disponibilizar quando a data for selecionada', async () => {
    const wrapper = mount(DisponibilizarMapaModal, {
      props: {
        mostrar: true,
      },
    });

    await wrapper.find('[data-testid="input-data-limite"]').setValue('2024-12-31');
    const disponibilizarButton = wrapper.find('[data-testid="btn-disponibilizar"]');
    expect(disponibilizarButton.attributes('disabled')).toBeUndefined();
  });

  it('deve emitir o evento fechar ao clicar no botão de cancelar', async () => {
    const wrapper = mount(DisponibilizarMapaModal, {
      props: {
        mostrar: true,
      },
    });

    await wrapper.find('[data-testid="btn-modal-cancelar"]').trigger('click');
    expect(wrapper.emitted('fechar')).toBeTruthy();
  });

  it('deve emitir o evento disponibilizar com a data selecionada', async () => {
    const wrapper = mount(DisponibilizarMapaModal, {
      props: {
        mostrar: true,
      },
    });

    const dataLimite = '2024-12-31';
    await wrapper.find('[data-testid="input-data-limite"]').setValue(dataLimite);
    await wrapper.find('[data-testid="btn-disponibilizar"]').trigger('click');

    expect(wrapper.emitted('disponibilizar')).toBeTruthy();
    expect(wrapper.emitted('disponibilizar')?.[0]).toEqual([dataLimite]);
  });
});
