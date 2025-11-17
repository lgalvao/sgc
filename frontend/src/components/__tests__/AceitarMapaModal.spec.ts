import { describe, it, expect, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import AceitarMapaModal from '../AceitarMapaModal.vue';
import { setActivePinia, createPinia } from 'pinia';

describe('AceitarMapaModal', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('não deve renderizar o modal quando mostrarModal for falso', () => {
    const wrapper = mount(AceitarMapaModal, {
      props: {
        mostrarModal: false,
      },
    });
    expect(wrapper.find('[data-testid="modal-aceite-body"]').exists()).toBe(false);
  });

  it('deve renderizar o modal com o perfil padrão (não ADMIN)', async () => {
    const wrapper = mount(AceitarMapaModal, {
      props: {
        mostrarModal: true,
      },
    });

    const corpoModal = wrapper.find('[data-testid="modal-aceite-body"]');
    expect(corpoModal.exists()).toBe(true);
    expect(corpoModal.text()).toContain('Observações (opcional)');
    expect(wrapper.find('[data-testid="observacao-aceite-textarea"]').exists()).toBe(true);
  });

  it('deve renderizar o modal com o perfil ADMIN', () => {
    const wrapper = mount(AceitarMapaModal, {
      props: {
        mostrarModal: true,
        perfil: 'ADMIN',
      },
    });

    const corpoModal = wrapper.find('[data-testid="modal-aceite-body"]');
    expect(corpoModal.exists()).toBe(true);
    expect(corpoModal.text()).toContain('Confirma a homologação do mapa de competências?');
    expect(wrapper.find('[data-testid="observacao-aceite-textarea"]').exists()).toBe(false);
  });

  it('deve emitir o evento fecharModal ao clicar no botão de cancelar', async () => {
    const wrapper = mount(AceitarMapaModal, {
      props: {
        mostrarModal: true,
      },
    });

    await wrapper.find('[data-testid="modal-aceite-cancelar"]').trigger('click');
    expect(wrapper.emitted('fecharModal')).toBeTruthy();
  });

  it('deve emitir o evento confirmarAceitacao com a observação', async () => {
    const wrapper = mount(AceitarMapaModal, {
      props: {
        mostrarModal: true,
      },
    });

    const observacao = 'Mapa de competências está de acordo com o esperado.';
    await wrapper.find('[data-testid="observacao-aceite-textarea"]').setValue(observacao);
    await wrapper.find('[data-testid="modal-aceite-confirmar"]').trigger('click');

    expect(wrapper.emitted('confirmarAceitacao')).toBeTruthy();
    expect(wrapper.emitted('confirmarAceitacao')?.[0]).toEqual([observacao]);
  });

  it('deve emitir o evento confirmarAceitacao com uma observação vazia', async () => {
    const wrapper = mount(AceitarMapaModal, {
      props: {
        mostrarModal: true,
      },
    });

    await wrapper.find('[data-testid="modal-aceite-confirmar"]').trigger('click');

    expect(wrapper.emitted('confirmarAceitacao')).toBeTruthy();
    expect(wrapper.emitted('confirmarAceitacao')?.[0]).toEqual(['']);
  });
});
