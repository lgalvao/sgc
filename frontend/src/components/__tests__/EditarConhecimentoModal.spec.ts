import { describe, it, expect, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import EditarConhecimentoModal from '../EditarConhecimentoModal.vue';
import { setActivePinia, createPinia } from 'pinia';
import { BFormTextarea } from 'bootstrap-vue-next';

describe('EditarConhecimentoModal', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  const conhecimento = {
    id: 1,
    descricao: 'Conhecimento original',
  };

  const globalComponents = {
    global: {
      components: {
        BFormTextarea,
      },
    },
  };

  it('não deve renderizar o modal quando mostrar for falso', () => {
    const wrapper = mount(EditarConhecimentoModal, {
      props: {
        mostrar: false,
        conhecimento,
      },
      ...globalComponents,
    });
    expect(wrapper.find('[data-testid="input-conhecimento-modal"]').exists()).toBe(false);
  });

  it('deve renderizar o modal com a descrição do conhecimento', () => {
    const wrapper = mount(EditarConhecimentoModal, {
      props: {
        mostrar: true,
        conhecimento,
      },
      ...globalComponents,
    });

    const textarea = wrapper.find('[data-testid="input-conhecimento-modal"]');
    expect(wrapper.findComponent(BFormTextarea).props().modelValue).toBe(conhecimento.descricao);
  });

  it('deve desabilitar o botão de salvar quando a descrição estiver vazia', async () => {
    const wrapper = mount(EditarConhecimentoModal, {
      props: {
        mostrar: true,
        conhecimento,
      },
      ...globalComponents,
    });

    const textareaWrapper = wrapper.findComponent(BFormTextarea);
    const nativeTextarea = textareaWrapper.find('textarea');
    await nativeTextarea.setValue('');
    const salvarButton = wrapper.find('[data-testid="btn-salvar-conhecimento-modal"]');
    expect(salvarButton.attributes('disabled')).toBeDefined();
  });

  it('deve emitir o evento fechar ao clicar no botão de cancelar', async () => {
    const wrapper = mount(EditarConhecimentoModal, {
      props: {
        mostrar: true,
        conhecimento,
      },
      ...globalComponents,
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
      ...globalComponents,
    });

    const novaDescricao = 'Conhecimento atualizado';
    const textareaWrapper = wrapper.findComponent(BFormTextarea);
    const nativeTextarea = textareaWrapper.find('textarea');
    await nativeTextarea.setValue(novaDescricao);
    await wrapper.find('[data-testid="btn-salvar-conhecimento-modal"]').trigger('click');

    expect(wrapper.emitted('salvar')).toBeTruthy();
    expect(wrapper.emitted('salvar')?.[0]).toEqual([conhecimento.id, novaDescricao]);
  });
});
