import { describe, it, expect, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import CriarCompetenciaModal from '../CriarCompetenciaModal.vue';
import { setActivePinia, createPinia } from 'pinia';

describe('CriarCompetenciaModal', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  const atividades = [
    { codigo: 1, descricao: 'Atividade 1', conhecimentos: [] },
    { codigo: 2, descricao: 'Atividade 2', conhecimentos: [{ id: 1, descricao: 'Conhecimento 1' }] },
  ];

  it('não deve renderizar o modal quando mostrar for falso', () => {
    const wrapper = mount(CriarCompetenciaModal, {
      props: {
        mostrar: false,
        atividades: [],
      },
    });
    expect(wrapper.find('[data-testid="input-descricao-competencia"]').exists()).toBe(false);
  });

  it('deve renderizar o modal no modo de criação', () => {
    const wrapper = mount(CriarCompetenciaModal, {
      props: {
        mostrar: true,
        atividades,
      },
    });

    expect(wrapper.find('textarea').element.value).toBe('');
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
    });

    await wrapper.vm.$nextTick();

    expect(wrapper.find('textarea').element.value).toBe('Competência existente');
  });

  it('deve habilitar o botão de salvar quando a descrição e pelo menos uma atividade forem selecionadas', async () => {
    const wrapper = mount(CriarCompetenciaModal, {
      props: {
        mostrar: true,
        atividades,
      },
    });

    await wrapper.find('textarea').setValue('Nova competência');
    await wrapper.find('.atividade-card-item').trigger('click');
    expect(wrapper.find('[data-testid="btn-modal-confirmar"]').attributes('disabled')).toBeUndefined();
  });

  it('deve emitir o evento fechar ao clicar no botão de cancelar', async () => {
    const wrapper = mount(CriarCompetenciaModal, {
      props: {
        mostrar: true,
        atividades,
      },
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
    });

    const descricao = 'Competência de teste';
    await wrapper.find('textarea').setValue(descricao);
    await wrapper.find('.atividade-card-item').trigger('click');
    await wrapper.find('[data-testid="btn-modal-confirmar"]').trigger('click');

    expect(wrapper.emitted('salvar')).toBeTruthy();
    expect(wrapper.emitted('salvar')?.[0]).toEqual([{
      descricao,
      atividadesSelecionadas: [atividades[0].codigo],
    }]);
  });
});
