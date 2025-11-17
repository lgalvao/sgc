import { describe, it, expect, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import CriarCompetenciaModal from '../CriarCompetenciaModal.vue';
import { setActivePinia, createPinia } from 'pinia';
import { BFormTextarea, BFormCheckbox } from 'bootstrap-vue-next';

describe('CriarCompetenciaModal', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  const atividades = [
    { codigo: 1, descricao: 'Atividade 1', conhecimentos: [] },
    { codigo: 2, descricao: 'Atividade 2', conhecimentos: [{ id: 1, descricao: 'Conhecimento 1' }] },
  ];

  const globalComponents = {
    global: {
      components: {
        BFormTextarea,
        BFormCheckbox,
      },
    },
  };

  it('não deve renderizar o modal quando mostrar for falso', () => {
    const wrapper = mount(CriarCompetenciaModal, {
      props: {
        mostrar: false,
        atividades: [],
      },
      ...globalComponents,
    });
    expect(wrapper.find('[data-testid="input-descricao-competencia"]').exists()).toBe(false);
  });

  it('deve renderizar o modal no modo de criação', () => {
    const wrapper = mount(CriarCompetenciaModal, {
      props: {
        mostrar: true,
        atividades,
      },
      ...globalComponents,
    });

    expect(wrapper.findComponent(BFormTextarea).props().modelValue).toBe('');
    expect(wrapper.find('[data-testid="btn-modal-confirmar"]').attributes('disabled')).toBeDefined();
  });

  it('deve renderizar o modal no modo de edição', async () => {
    const competenciaParaEditar = {
      codigo: 1,
      descricao: 'Competência existente',
      atividadesAssociadas: [1],
    };

    const wrapper = mount(CriarCompetenciaModal, {
      props: {
        mostrar: true,
        atividades,
        competenciaParaEditar,
      },
      ...globalComponents,
    });

    await wrapper.vm.$nextTick();

    expect(wrapper.findComponent(BFormTextarea).props().modelValue).toBe('Competência existente');
  });

  it('deve habilitar o botão de salvar quando a descrição e pelo menos uma atividade forem selecionadas', async () => {
    const wrapper = mount(CriarCompetenciaModal, {
      props: {
        mostrar: true,
        atividades,
      },
      ...globalComponents,
    });

    const textareaWrapper = wrapper.findComponent(BFormTextarea);
    const nativeTextarea = textareaWrapper.find('textarea');
    await nativeTextarea.setValue('Nova competência');
    await wrapper.findComponent(BFormCheckbox).trigger('click');
    expect(wrapper.find('[data-testid="btn-modal-confirmar"]').attributes('disabled')).toBeUndefined();
  });

  it('deve emitir o evento fechar ao clicar no botão de cancelar', async () => {
    const wrapper = mount(CriarCompetenciaModal, {
      props: {
        mostrar: true,
        atividades,
      },
      ...globalComponents,
    });

    await wrapper.find('[data-testid="btn-modal-cancelar"]').trigger('click');
    expect(wrapper.emitted('fechar')).toBeTruthy();
  });

  it('deve emitir o evento salvar com os dados corretos', async () => {
    const wrapper = mount(CriarCompetenciaModal, {
      props: {
        mostrar: true,
        atividades,
      },
      ...globalComponents,
    });

    const descricao = 'Competência de teste';
    const textareaWrapper = wrapper.findComponent(BFormTextarea);
    const nativeTextarea = textareaWrapper.find('textarea');
    await nativeTextarea.setValue(descricao);
    await wrapper.findComponent(BFormCheckbox).trigger('click');
    await wrapper.find('[data-testid="btn-modal-confirmar"]').trigger('click');

    expect(wrapper.emitted('salvar')).toBeTruthy();
    expect(wrapper.emitted('salvar')?.[0]).toEqual([{
      descricao,
      atividadesSelecionadas: [atividades[0].codigo],
    }]);
  });
});
