import { describe, it, expect, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import EditarConhecimentoModal from '../EditarConhecimentoModal.vue';
import { setActivePinia, createPinia } from 'pinia';

describe('EditarConhecimentoModal', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  const conhecimento = {
    id: 1,
    descricao: 'Conhecimento original',
  };

  it('não deve renderizar o modal quando mostrar for falso', () => {
    const wrapper = mount(EditarConhecimentoModal, {
      props: {
        mostrar: false,
        conhecimento,
      },
    });
    expect(wrapper.find('[data-testid="input-conhecimento-modal"]').exists()).toBe(false);
  });

  it('deve renderizar o modal com a descrição do conhecimento', () => {
    const wrapper = mount(EditarConhecimentoModal, {
      props: {
        mostrar: true,
        conhecimento,
      },
    });

    const textarea = wrapper.find('[data-testid="input-conhecimento-modal"]');
    expect((textarea.element as HTMLTextAreaElement).value).toBe(conhecimento.descricao);
  });

  it('deve desabilitar o botão de salvar quando a descrição estiver vazia', async () => {
    const wrapper = mount(EditarConhecimentoModal, {
      props: {
        mostrar: true,
        conhecimento,
      },
    });

    await wrapper.find('[data-testid="input-conhecimento-modal"]').setValue('');
    const salvarButton = wrapper.find('[data-testid="btn-salvar-conhecimento-modal"]');
    expect(salvarButton.attributes('disabled')).toBeDefined();
  });

  it('deve emitir o evento fechar ao clicar no botão de cancelar', async () => {
    const wrapper = mount(EditarConhecimentoModal, {
      props: {
        mostrar: true,
        conhecimento,
      },
    });

    await wrapper.find('button.btn-secondary').trigger('click');
    expect(wrapper.emitted('fechar')).toBeTruthy();
  });

  it('deve emitir o evento salvar com a nova descrição', async () => {
    const wrapper = mount(EditarConhecimentoModal, {
      props: {
        mostrar: true,
        conhecimento,
      },
    });

    const novaDescricao = 'Conhecimento atualizado';
    await wrapper.find('[data-testid="input-conhecimento-modal"]').setValue(novaDescricao);
    await wrapper.find('[data-testid="btn-salvar-conhecimento-modal"]').trigger('click');

    expect(wrapper.emitted('salvar')).toBeTruthy();
    expect(wrapper.emitted('salvar')?.[0]).toEqual([conhecimento.id, novaDescricao]);
  });
});
