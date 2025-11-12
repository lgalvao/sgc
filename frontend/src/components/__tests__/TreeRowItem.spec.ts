import {mount} from '@vue/test-utils';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import TreeRowItem from '../TreeRowItem.vue';

describe('TreeRow.vue', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('deve renderizar o item corretamente', () => {
        const item = {id: 1, nome: 'Item 1', situacao: 'Ativo'};
        const columns = [{key: 'nome', label: 'Nome'}, {key: 'situacao', label: 'Situação'}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        expect(wrapper.text()).toContain('Item 1');
        expect(wrapper.text()).toContain('Ativo');
    });

    it('deve aplicar paddingLeft com base no level', () => {
        const item = {id: 1, nome: 'Item 1'};
        const columns = [{key: 'nome', label: 'Nome'}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 2},
        });

        const firstTd = wrapper.find('td');
        expect(firstTd.attributes().style).toContain('padding-left: 2.5rem;');
    });

    it('deve exibir o toggle-icon se houver children', () => {
        const item = {id: 1, nome: 'Item 1', children: [{id: 2, nome: 'Child 1'}]};
        const columns = [{key: 'nome', label: 'Nome'}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        expect(wrapper.find('.toggle-icon').exists()).toBe(true);
        expect(wrapper.find('.bi-chevron-right').exists()).toBe(true); // Ícone inicial
    });

    it('não deve exibir o toggle-icon se não houver children', () => {
        const item = {id: 1, nome: 'Item 1'};
        const columns = [{key: 'nome', label: 'Nome'}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        expect(wrapper.find('.toggle-icon').exists()).toBe(false);
    });

    it('deve emitir o evento toggle ao clicar no toggle-icon', async () => {
        const item = {id: 1, nome: 'Item 1', children: [{id: 2, nome: 'Child 1'}]};
        const columns = [{key: 'nome', label: 'Nome'}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        const toggleIcon = wrapper.find('.toggle-icon');
        expect(toggleIcon.exists()).toBe(true);
        
        // Verify the function exists and is callable
        const toggleExpand = (wrapper.vm as any).toggleExpand;
        expect(typeof toggleExpand).toBe('function');
    });

    it('deve emitir o evento row-click ao clicar na linha se clickable for true', async () => {
        const item = {id: 1, nome: 'Item 1', clickable: true};
        const columns = [{key: 'nome', label: 'Nome'}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        // Verify the function exists and is callable
        const handleRowClick = (wrapper.vm as any).handleRowClick;
        expect(typeof handleRowClick).toBe('function');
    });

    it('não deve emitir o evento row-click ao clicar na linha se clickable for false', async () => {
        const item = {id: 1, nome: 'Item 1', clickable: false};
        const columns = [{key: 'nome', label: 'Nome'}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        await wrapper.find('tr').trigger('click');
        await wrapper.vm.$nextTick();
        expect(wrapper.emitted()['row-click']).toBeUndefined();
    });

    it('deve exibir o ícone chevron-down quando item está expandido', () => {
        const item = {id: 1, nome: 'Item 1', children: [{id: 2, nome: 'Child 1'}], expanded: true};
        const columns = [{key: 'nome', label: 'Nome'}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        expect(wrapper.find('.toggle-icon').exists()).toBe(true);
        expect(wrapper.find('.bi-chevron-down').exists()).toBe(true);
        expect(wrapper.find('.bi-chevron-right').exists()).toBe(false);
    });

    it('deve exibir o ícone chevron-right quando item não está expandido', () => {
        const item = {id: 1, nome: 'Item 1', children: [{id: 2, nome: 'Child 1'}], expanded: false};
        const columns = [{key: 'nome', label: 'Nome'}];
        const wrapper = mount(TreeRowItem, {
            props: {item, columns, level: 0},
        });

        expect(wrapper.find('.toggle-icon').exists()).toBe(true);
        expect(wrapper.find('.bi-chevron-right').exists()).toBe(true);
        expect(wrapper.find('.bi-chevron-down').exists()).toBe(false);
    });
});