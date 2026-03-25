import {describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import ModalAcaoBloco from '../ModalAcaoBloco.vue';

vi.mock("@/utils/dateUtils", async () => {
    const actual = await vi.importActual("@/utils/dateUtils") as any;
    return {
        ...actual,
        obterAmanhaFormatado: () => '2026-03-25',
        isDateValidAndFuture: (d: string) => d > '2026-03-24'
    };
});

describe('ModalAcaoBloco.vue', () => {
    const propsPadrao = {
        id: 'modal-teste',
        titulo: 'Título Teste',
        texto: 'Texto Teste',
        rotuloBotao: 'Confirmar',
        unidades: [{ codigo: 1, sigla: 'U1', nome: 'Unidade 1', situacao: 'OK' }],
        unidadesPreSelecionadas: [1],
        mostrarDataLimite: true,
        modelValue: true // Forçar exibição para o teste encontrar o conteúdo
    };

    it('deve ter o atributo min correto no campo de data', async () => {
        const wrapper = mount(ModalAcaoBloco, {
            props: propsPadrao
        });
        const input = wrapper.find('[data-testid="inp-data-limite-bloco"]');
        expect(input.attributes('min')).toBe('2026-03-25');
    });

    it('deve exibir erro se a data não for futura', async () => {
        const wrapper = mount(ModalAcaoBloco, {
            props: propsPadrao
        });
        const input = wrapper.find('[data-testid="inp-data-limite-bloco"]');
        
        await input.setValue('2026-03-24'); // hoje no mock
        
        expect(wrapper.text()).toContain('A data limite para validação deve ser uma data futura.');
    });
});
