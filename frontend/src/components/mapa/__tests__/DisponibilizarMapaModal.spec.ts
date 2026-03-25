import {describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import DisponibilizarMapaModal from '../DisponibilizarMapaModal.vue';

vi.mock("@/utils/dateUtils", async () => {
    const actual = await vi.importActual("@/utils/dateUtils") as any;
    return {
        ...actual,
        obterAmanhaFormatado: () => '2026-03-25',
        isDateValidAndFuture: (d: string) => d > '2026-03-24'
    };
});

describe('DisponibilizarMapaModal.vue', () => {
    it('deve ter o atributo min correto no campo de data', () => {
        const wrapper = mount(DisponibilizarMapaModal, {
            props: { mostrar: true }
        });
        const input = wrapper.find('[data-testid="inp-disponibilizar-mapa-data"]');
        expect(input.attributes('min')).toBe('2026-03-25');
    });

    it('deve exibir erro se a data não for futura', async () => {
        const wrapper = mount(DisponibilizarMapaModal, {
            props: { mostrar: true }
        });
        const input = wrapper.find('[data-testid="inp-disponibilizar-mapa-data"]');
        
        await input.setValue('2026-03-24'); // hoje no mock
        
        expect(wrapper.text()).toContain('A data limite para validação deve ser uma data futura.');
        
        const btn = wrapper.find('[data-testid="btn-disponibilizar-mapa-confirmar"]');
        expect((btn.element as HTMLButtonElement).disabled).toBe(true);
    });
});
