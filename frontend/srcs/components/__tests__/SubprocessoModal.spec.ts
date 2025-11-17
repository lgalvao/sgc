import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import SubprocessoModal from '../SubprocessoModal.vue';
import * as utils from '@/utils';

describe('SubprocessoModal', () => {
  const dataLimiteAtual = new Date('2024-10-10T00:00:00');

  it('não deve renderizar o modal quando mostrarModal for falso', () => {
    const wrapper = mount(SubprocessoModal, {
      props: { mostrarModal: false, dataLimiteAtual, etapaAtual: 1 },
    });
    expect(wrapper.find('b-modal-stub').exists()).toBe(false);
  });

  it('deve inicializar o campo de data com a data limite atual', () => {
    const wrapper = mount(SubprocessoModal, {
      props: { mostrarModal: true, dataLimiteAtual, etapaAtual: 1 },
    });
    const input = wrapper.find('[data-testid="input-nova-data-limite"]');
    expect((input.element as HTMLInputElement).value).toBe('2024-10-10');
  });

  it('deve desabilitar o botão de confirmar se a data for inválida', async () => {
    vi.spyOn(utils, 'isDateValidAndFuture').mockReturnValue(false);
    const wrapper = mount(SubprocessoModal, {
      props: { mostrarModal: true, dataLimiteAtual, etapaAtual: 1 },
    });

    const confirmButton = wrapper.find('[data-testid="btn-modal-confirmar"]');
    expect(confirmButton.attributes('disabled')).toBeDefined();
  });

  it('deve habilitar o botão de confirmar se a data for válida', async () => {
    vi.spyOn(utils, 'isDateValidAndFuture').mockReturnValue(true);
    const wrapper = mount(SubprocessoModal, {
      props: { mostrarModal: true, dataLimiteAtual, etapaAtual: 1 },
    });

    await wrapper.find('[data-testid="input-nova-data-limite"]').setValue('2025-01-01');

    const confirmButton = wrapper.find('[data-testid="btn-modal-confirmar"]');
    expect(confirmButton.attributes('disabled')).toBeUndefined();
  });

  it('deve emitir "fecharModal" ao clicar no botão de cancelar', async () => {
    const wrapper = mount(SubprocessoModal, {
      props: { mostrarModal: true, dataLimiteAtual, etapaAtual: 1 },
    });
    await wrapper.find('[data-testid="btn-modal-cancelar"]').trigger('click');
    expect(wrapper.emitted('fecharModal')).toBeTruthy();
  });

  it('deve emitir "confirmarAlteracao" com a nova data', async () => {
    vi.spyOn(utils, 'isDateValidAndFuture').mockReturnValue(true);
    const wrapper = mount(SubprocessoModal, {
      props: { mostrarModal: true, dataLimiteAtual, etapaAtual: 1 },
    });

    const novaData = '2025-01-01';
    await wrapper.find('[data-testid="input-nova-data-limite"]').setValue(novaData);
    await wrapper.find('[data-testid="btn-modal-confirmar"]').trigger('click');

    expect(wrapper.emitted('confirmarAlteracao')).toBeTruthy();
    expect(wrapper.emitted('confirmarAlteracao')?.[0]).toEqual([novaData]);
  });
});
