import {describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import DisponibilizarMapaModal from '../DisponibilizarMapaModal.vue';

vi.mock("@/utils/dateUtils", async () => {
    const actual = await vi.importActual<typeof import("@/utils/dateUtils")>("@/utils/dateUtils");
    return {
        ...actual,
        obterAmanhaFormatado: () => '2026-03-25',
        isDateStrictlyFuture: (d: string) => d > '2026-03-24',
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
    });

    it('deve usar a última data limite do subprocesso como mínimo quando ela for maior que amanhã', () => {
        const wrapper = mount(DisponibilizarMapaModal, {
            props: { mostrar: true, ultimaDataLimiteSubprocesso: '2026-03-30T00:00:00' }
        });
        const input = wrapper.find('[data-testid="inp-disponibilizar-mapa-data"]');
        expect(input.attributes('min')).toBe('2026-03-30');
    });

    it('deve exigir data maior ou igual à última data limite do subprocesso', async () => {
        const wrapper = mount(DisponibilizarMapaModal, {
            props: { mostrar: true, ultimaDataLimiteSubprocesso: '2026-03-30T00:00:00' }
        });
        const input = wrapper.find('[data-testid="inp-disponibilizar-mapa-data"]');

        await input.setValue('2026-03-29');

        expect(wrapper.text()).toContain('A data limite deve ser maior ou igual à última data limite do subprocesso.');
    });

    it('deve exigir data limite obrigatória no submit e focar o input', async () => {
        const wrapper = mount(DisponibilizarMapaModal, {
            props: { mostrar: true }
        });
        
        const btn = wrapper.find('[data-testid="btn-disponibilizar-mapa-confirmar"]');
        await btn.trigger('click');
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).toContain('A data limite é obrigatória.');
    });
});
