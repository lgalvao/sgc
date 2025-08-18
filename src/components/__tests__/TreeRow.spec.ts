import { mount } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import TreeRow from '../TreeRow.vue';

describe('TreeRow.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('deve renderizar o item corretamente', () => {
    const item = { id: 1, nome: 'Item 1', situacao: 'Ativo' };
    const columns = [{ key: 'nome', label: 'Nome' }, { key: 'situacao', label: 'Situação' }];
    const wrapper = mount(TreeRow, {
      props: { item, columns, level: 0 },
    });

    expect(wrapper.text()).toContain('Item 1');
    expect(wrapper.text()).toContain('Ativo');
  });

  it('deve aplicar paddingLeft com base no level', () => {
    const item = { id: 1, nome: 'Item 1' };
    const columns = [{ key: 'nome', label: 'Nome' }];
    const wrapper = mount(TreeRow, {
      props: { item, columns, level: 2 },
    });

    const firstTd = wrapper.find('td');
    expect(firstTd.attributes().style).toContain('padding-left: 40px;');
  });

  it('deve exibir o toggle-icon se houver children', () => {
    const item = { id: 1, nome: 'Item 1', children: [{ id: 2, nome: 'Child 1' }] };
    const columns = [{ key: 'nome', label: 'Nome' }];
    const wrapper = mount(TreeRow, {
      props: { item, columns, level: 0 },
    });

    expect(wrapper.find('.toggle-icon').exists()).toBe(true);
    expect(wrapper.find('.bi-chevron-right').exists()).toBe(true); // Ícone inicial
  });

  it('não deve exibir o toggle-icon se não houver children', () => {
    const item = { id: 1, nome: 'Item 1' };
    const columns = [{ key: 'nome', label: 'Nome' }];
    const wrapper = mount(TreeRow, {
      props: { item, columns, level: 0 },
    });

    expect(wrapper.find('.toggle-icon').exists()).toBe(false);
  });

  it('deve emitir o evento toggle ao clicar no toggle-icon', async () => {
    const item = { id: 1, nome: 'Item 1', children: [{ id: 2, nome: 'Child 1' }] };
    const columns = [{ key: 'nome', label: 'Nome' }];
    const wrapper = mount(TreeRow, {
      props: { item, columns, level: 0 },
    });

    await wrapper.find('.toggle-icon').trigger('click');
    expect(wrapper.emitted().toggle).toBeTruthy();
    expect(wrapper.emitted().toggle[0]).toEqual([1]);
  });

  it('deve emitir o evento row-click ao clicar na linha se clickable for true', async () => {
    const item = { id: 1, nome: 'Item 1', clickable: true };
    const columns = [{ key: 'nome', label: 'Nome' }];
    const wrapper = mount(TreeRow, {
      props: { item, columns, level: 0 },
    });

    await wrapper.find('tr').trigger('click');
    expect(wrapper.emitted()['row-click']).toBeTruthy();
    expect(wrapper.emitted()['row-click'][0]).toEqual([item]);
  });

  it('não deve emitir o evento row-click ao clicar na linha se clickable for false', async () => {
    const item = { id: 1, nome: 'Item 1', clickable: false };
    const columns = [{ key: 'nome', label: 'Nome' }];
    const wrapper = mount(TreeRow, {
      props: { item, columns, level: 0 },
    });

    await wrapper.find('tr').trigger('click');
    expect(wrapper.emitted()['row-click']).toBeUndefined();
  });

  it('deve renderizar children recursivamente', () => {
    const item = {
      id: 1, nome: 'Parent', expanded: true, children: [
        { id: 2, nome: 'Child 1' },
        { id: 3, nome: 'Child 2' },
      ],
    };
    const columns = [{ key: 'nome', label: 'Nome' }];
    const wrapper = mount(TreeRow, {
      props: { item, columns, level: 0 },
    });

    expect(wrapper.text()).toContain('Parent');
    expect(wrapper.text()).toContain('Child 1');
    expect(wrapper.text()).toContain('Child 2');

    // Verificar se os children são renderizados como TreeRow
    const childRows = wrapper.findAllComponents(TreeRow);
    // O findAllComponents encontra apenas os componentes filhos, não o componente raiz
    expect(childRows.length).toBe(2); // Apenas os 2 Children
  });
});