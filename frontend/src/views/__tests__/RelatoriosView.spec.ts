import {describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import RelatoriosView from '@/views/RelatoriosView.vue';
import {getCommonMountOptions, setupComponentTest} from '@/test-utils/componentTestHelpers';

const mockPush = vi.fn();
vi.mock("vue-router", () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}));

describe('RelatoriosView.vue', () => {
    const ctx = setupComponentTest();

    const stubs = {
        LayoutPadrao: {template: '<div><slot /></div>'},
        BCard: {template: '<div class="card" @click="$emit(\'click\')"><slot /></div>'},
        BCardTitle: {template: '<h3><slot /></h3>'},
        BCardText: {template: '<p><slot /></p>'},
        BRow: {template: '<div><slot /></div>'},
        BCol: {template: '<div><slot /></div>'},
        PageHeader: {template: '<div><slot /></div>'},
    };

    it('deve renderizar os cards de relatórios', () => {
        const mountOptions = getCommonMountOptions({}, stubs);
        ctx.wrapper = mount(RelatoriosView, mountOptions);

        expect(ctx.wrapper.find('[data-testid="card-relatorio-andamento"]').exists()).toBe(true);
        expect(ctx.wrapper.find('[data-testid="card-relatorio-mapas"]').exists()).toBe(true);
    });

    it('deve navegar para relatório de andamento ao clicar no card', async () => {
        const mountOptions = getCommonMountOptions({}, stubs);
        ctx.wrapper = mount(RelatoriosView, mountOptions);

        await ctx.wrapper.find('[data-testid="card-relatorio-andamento"]').trigger('click');
        expect(mockPush).toHaveBeenCalledWith('/relatorios/andamento');
    });

    it('deve navegar para relatório de mapas vigentes ao clicar no card', async () => {
        const mountOptions = getCommonMountOptions({}, stubs);
        ctx.wrapper = mount(RelatoriosView, mountOptions);

        await ctx.wrapper.find('[data-testid="card-relatorio-mapas"]').trigger('click');
        expect(mockPush).toHaveBeenCalledWith('/relatorios/mapas-vigentes');
    });
});
