import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import ModalAcaoBloco from '../ModalAcaoBloco.vue';
import type { UnidadeSelecao } from '../ModalAcaoBloco.vue';
import { BFormCheckbox } from 'bootstrap-vue-next';

describe('ModalAcaoBloco', () => {
  const unidades: UnidadeSelecao[] = [
    { sigla: 'U1', nome: 'Unidade 1', situacao: 'Pendente', selecionada: false },
    { sigla: 'U2', nome: 'Unidade 2', situacao: 'Pendente', selecionada: true },
  ];

  const globalComponents = {
    global: {
      components: {
        BFormCheckbox,
      },
    },
  };

  it('não deve renderizar o modal quando mostrar for falso', () => {
    const wrapper = mount(ModalAcaoBloco, {
      props: { mostrar: false, tipo: 'aceitar', unidades },
      ...globalComponents,
    });
    expect(wrapper.find('.table').exists()).toBe(false);
  });

  it('deve renderizar o título e o botão corretos para o tipo "aceitar"', () => {
    const wrapper = mount(ModalAcaoBloco, {
      props: { mostrar: true, tipo: 'aceitar', unidades },
      ...globalComponents,
    });
    expect(wrapper.find('.btn-primary').text()).toContain('Aceitar');
  });

  it('deve renderizar o título e o botão corretos para o tipo "homologar"', () => {
    const wrapper = mount(ModalAcaoBloco, {
      props: { mostrar: true, tipo: 'homologar', unidades },
      ...globalComponents,
    });
    expect(wrapper.find('.btn-success').text()).toContain('Homologar');
  });

  it('deve renderizar a lista de unidades', () => {
    const wrapper = mount(ModalAcaoBloco, {
      props: { mostrar: true, tipo: 'aceitar', unidades },
      ...globalComponents,
    });
    const rows = wrapper.findAll('tbody tr');
    expect(rows.length).toBe(unidades.length);
    expect(rows[0].text()).toContain('Unidade 1');
    expect(rows[1].findComponent(BFormCheckbox).props().modelValue).toBe(true);
  });

  it('deve emitir "fechar" ao clicar no botão de cancelar', async () => {
    const wrapper = mount(ModalAcaoBloco, {
      props: { mostrar: true, tipo: 'aceitar', unidades },
      ...globalComponents,
    });
    await wrapper.find('.btn-secondary').trigger('click');
    expect(wrapper.emitted('fechar')).toBeTruthy();
  });

  it('deve emitir "confirmar" com as unidades selecionadas', async () => {
    const wrapper = mount(ModalAcaoBloco, {
      props: { mostrar: true, tipo: 'aceitar', unidades },
      ...globalComponents,
    });
    await wrapper.find('.btn-primary').trigger('click');
    expect(wrapper.emitted('confirmar')).toBeTruthy();
    const emittedUnidades = wrapper.emitted('confirmar')?.[0][0] as UnidadeSelecao[];
    expect(emittedUnidades.length).toBe(unidades.length);
    expect(emittedUnidades[1].selecionada).toBe(true);
  });
});
