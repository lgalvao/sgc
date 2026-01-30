import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import CadAtribuicao from '@/views/CadAtribuicao.vue';
import {criarAtribuicaoTemporaria} from '@/services/atribuicaoTemporariaService';
import {buscarUnidadePorCodigo} from '@/services/unidadeService';
import {buscarUsuariosPorUnidade} from '@/services/usuarioService';
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

// Mocks
const { mockPush, mockFeedbackShow } = vi.hoisted(() => {
    return {
        mockPush: vi.fn(),
        mockFeedbackShow: vi.fn(),
    };
});

vi.mock('vue-router', () => ({
    useRouter: () => ({
        push: mockPush,
    }),
    createRouter: vi.fn(() => ({
        push: mockPush,
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

vi.mock('@/stores/feedback', () => ({
    useFeedbackStore: () => ({
        show: mockFeedbackShow,
    }),
}));

vi.mock('@/services/atribuicaoTemporariaService', () => ({
    criarAtribuicaoTemporaria: vi.fn(),
}));
vi.mock('@/services/unidadeService', () => ({
    buscarUnidadePorCodigo: vi.fn(),
}));
vi.mock('@/services/usuarioService', () => ({
    buscarUsuariosPorUnidade: vi.fn(),
}));

describe('CadAtribuicao.vue', () => {
    const context = setupComponentTest();

    const mockUnidade = {
        codigo: 1,
        sigla: 'TEST',
        nome: 'Unidade Teste'
    };

    const mockUsuarios = [
        { codigo: '111', nome: 'Servidor 1', tituloEleitoral: '111' },
        { codigo: '222', nome: 'Servidor 2', tituloEleitoral: '222' }
    ];

    beforeEach(() => {
        vi.clearAllMocks();
        vi.spyOn(console, 'error').mockImplementation(() => {});

        (buscarUnidadePorCodigo as any).mockResolvedValue(mockUnidade);
        (buscarUsuariosPorUnidade as any).mockResolvedValue(mockUsuarios);

        context.wrapper = mount(CadAtribuicao, {
            ...getCommonMountOptions(
                {}, // no store needed, or empty state
                {
                    BContainer: { template: '<div><slot /></div>' },
                    BCard: { template: '<div><slot /></div>' },
                    BCardBody: { template: '<div><slot /></div>' },
                    BForm: { template: '<form @submit.prevent="$emit(\'submit\', { preventDefault: () => {} })"><slot /></form>' },
                    BFormSelect: {
                        template: '<select :value="modelValue" @change="$emit(\'update:modelValue\', $event.target.value)"><slot name="first" /><option v-for="opt in options" :key="opt.codigo" :value="opt.codigo">{{ opt.nome }}</option></select>',
                        props: ['modelValue', 'options']
                    },
                    BFormSelectOption: { template: '<option><slot /></option>' },
                    BFormInput: {
                        template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
                        props: ['modelValue']
                    },
                    BFormTextarea: {
                        template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>',
                        props: ['modelValue']
                    },
                    BButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
                    BAlert: { template: '<div role="alert"><slot /></div>' },
                }
            ),
            props: {
                codUnidade: 1
            },
        });
    });

    it('fetches data on mount', async () => {
        expect(buscarUnidadePorCodigo).toHaveBeenCalledWith(1);

        await flushPromises();

        expect(buscarUsuariosPorUnidade).toHaveBeenCalledWith(1);
        expect(context.wrapper!.vm.usuarios).toHaveLength(2);
    });

    it('submits the form successfully', async () => {
        await flushPromises();

        // Fill form
        const select = context.wrapper!.find('[data-testid="select-usuario"]');
        await select.setValue('111');

        const dateInput = context.wrapper!.find('[data-testid="input-data-termino"]');
        await dateInput.setValue('2023-12-31');

        const textarea = context.wrapper!.find('[data-testid="textarea-justificativa"]');
        await textarea.setValue('Justificativa de teste');

        await context.wrapper!.find('form').trigger('submit');
        await flushPromises();

        expect(criarAtribuicaoTemporaria).toHaveBeenCalledWith(1, {
            tituloEleitoralUsuario: '111',
            dataTermino: '2023-12-31',
            justificativa: 'Justificativa de teste'
        });

        expect(mockFeedbackShow).toHaveBeenCalledWith('Sucesso', 'Atribuição criada com sucesso!', 'success');
    });

    it('handles submission error', async () => {
        await flushPromises();

        (criarAtribuicaoTemporaria as any).mockRejectedValue(new Error('API Error'));

        // Fill form
        context.wrapper!.vm.usuarioSelecionado = '111';
        context.wrapper!.vm.dataTermino = '2023-12-31';
        context.wrapper!.vm.justificativa = 'Teste';

        await context.wrapper!.find('form').trigger('submit');
        await flushPromises();

        expect(mockFeedbackShow).toHaveBeenCalledWith('Erro', 'Falha ao criar atribuição. Tente novamente.', 'danger');
    });

    it('cancels and navigates back', async () => {
        const btn = context.wrapper!.find('[data-testid="btn-cancelar-atribuicao"]');
        await btn.trigger('click');

        expect(mockPush).toHaveBeenCalledWith('/unidade/1');
    });
});
