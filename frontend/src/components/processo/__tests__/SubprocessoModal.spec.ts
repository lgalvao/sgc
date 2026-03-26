import {describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import SubprocessoModal from '../SubprocessoModal.vue';

vi.mock("@/utils/dateUtils", async () => {
    const actual = await vi.importActual("@/utils/dateUtils") as any;
    return {
        ...actual,
        obterAmanhaFormatado: () => '2026-03-25',
        isDateValidAndFuture: (d: any) => {
            if (!d) return false;
            // Simplificado para o teste
            const dateStr = typeof d === 'string' ? d : d.toISOString().split('T')[0];
            return dateStr > '2026-03-24';
        },
        parseDate: (d: string) => new Date(d + 'T12:00:00')
    };
});

describe('SubprocessoModal.vue', () => {
    const propsPadrao = {
        mostrarModal: true,
        dataLimiteAtual: new Date('2026-03-24'),
        ultimaDataLimiteSubprocesso: new Date('2026-03-24'),
        etapaAtual: 1
    };

    it('deve ter o atributo min correto no campo de data', () => {
        const wrapper = mount(SubprocessoModal, {
            props: propsPadrao
        });
        const input = wrapper.find('[data-testid="input-nova-data-limite"]');
        expect(input.attributes('min')).toBe('2026-03-25');
    });

    it('deve exibir erro se a nova data não for futura', async () => {
        const wrapper = mount(SubprocessoModal, {
            props: propsPadrao
        });
        const input = wrapper.find('[data-testid="input-nova-data-limite"]');
        
        await input.setValue('2026-03-24'); // hoje no mock
        
        expect(wrapper.text()).toContain('A data limite para validação deve ser uma data futura.');
        
        const btn = wrapper.find('[data-testid="btn-modal-confirmar"]');
        expect((btn.element as HTMLButtonElement).disabled).toBe(true);
    });

    it('deve usar a última data limite do subprocesso como mínimo quando ela for maior que amanhã', () => {
        const wrapper = mount(SubprocessoModal, {
            props: {
                ...propsPadrao,
                ultimaDataLimiteSubprocesso: new Date('2026-03-30T12:00:00')
            }
        });
        const input = wrapper.find('[data-testid="input-nova-data-limite"]');
        expect(input.attributes('min')).toBe('2026-03-30');
    });
});
