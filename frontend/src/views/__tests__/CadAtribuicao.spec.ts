import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import CadAtribuicao from '@/views/CadAtribuicao.vue';
import {criarAtribuicaoTemporaria} from '@/services/atribuicaoTemporariaService';
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

// Mocks
const { mockPush, mockFeedbackShow, mockBuscarUnidade, mockBuscarUsuarios } = vi.hoisted(() => {
    return {
        mockPush: vi.fn(),
        mockFeedbackShow: vi.fn(),
        mockBuscarUnidade: vi.fn(),
        mockBuscarUsuarios: vi.fn(),
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
    buscarTodasAtribuicoes: vi.fn().mockResolvedValue([]),
}));

vi.mock('@/services/unidadeService', () => ({
    buscarUnidadePorCodigo: mockBuscarUnidade,
}));

vi.mock('@/services/usuarioService', () => ({
    buscarUsuariosPorUnidade: mockBuscarUsuarios,
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

    function criarWrapper() {
        return mount(CadAtribuicao, {
            ...getCommonMountOptions(
                {
                    unidades: {
                        unidadeSelecionada: mockUnidade
                    }
                },
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
                },
                { stubActions: false } // Permite que as stores chamem os serviços mockados
            ),
            props: {
                codUnidade: 1
            },
        });
    }

    beforeEach(() => {
        vi.clearAllMocks();
        vi.spyOn(console, 'error').mockImplementation(() => {});

        mockBuscarUnidade.mockResolvedValue(mockUnidade);
        mockBuscarUsuarios.mockResolvedValue(mockUsuarios);

        context.wrapper = criarWrapper();
    });

    it('busca dados no mount', async () => {
        await flushPromises();

        expect(mockBuscarUnidade).toHaveBeenCalledWith(1);
        expect(mockBuscarUsuarios).toHaveBeenCalledWith(1);
    });

    it('submete o formulário com sucesso', async () => {
        // Resetar e configurar o mock apenas para este teste
        (criarAtribuicaoTemporaria as any).mockReset();
        (criarAtribuicaoTemporaria as any).mockResolvedValue({});
        
        // Remontar o componente para este teste
        context.wrapper = criarWrapper();
        await flushPromises();

        // Preencher formulário
        const select = context.wrapper!.find('[data-testid="select-usuario"]');
        await select.setValue('111');

        const dateInput = context.wrapper!.find('[data-testid="input-data-termino"]');
        await dateInput.setValue('2023-12-31');

        const dateInicioInput = context.wrapper!.find('[data-testid="input-data-inicio"]');
        await dateInicioInput.setValue('2023-01-01');

        const textarea = context.wrapper!.find('[data-testid="textarea-justificativa"]');
        await textarea.setValue('Justificativa de teste');

        await context.wrapper!.find('form').trigger('submit');
        await flushPromises();

        expect(criarAtribuicaoTemporaria).toHaveBeenCalledWith(1, {
            tituloEleitoralUsuario: '111',
            dataInicio: '2023-01-01',
            dataTermino: '2023-12-31',
            justificativa: 'Justificativa de teste'
        });

        expect(mockFeedbackShow).toHaveBeenCalledWith('Sucesso', 'Atribuição criada com sucesso!', 'success');
    });

    it('lida com erro de submissão', async () => {
        // Resetar e configurar o mock apenas para este teste
        (criarAtribuicaoTemporaria as any).mockReset();
        (criarAtribuicaoTemporaria as any).mockRejectedValue(new Error('API Error'));
        
        // Remontar o componente para este teste
        context.wrapper = criarWrapper();
        await flushPromises();

        // Preencher formulário
        context.wrapper!.vm.usuarioSelecionado = '111';
        context.wrapper!.vm.dataInicio = '2023-01-01';
        context.wrapper!.vm.dataTermino = '2023-12-31';
        context.wrapper!.vm.justificativa = 'Teste';

        await context.wrapper!.find('form').trigger('submit');
        await flushPromises();

        expect(mockFeedbackShow).toHaveBeenCalledWith('Erro', 'Falha ao criar atribuição. Tente novamente.', 'danger');
    });

    it('cancela e navega de volta', async () => {
        const btn = context.wrapper!.find('[data-testid="btn-cancelar-atribuicao"]');
        await btn.trigger('click');

        expect(mockPush).toHaveBeenCalledWith('/unidade/1');
    });

    it('valida campos obrigatorios', async () => {
        context.wrapper = criarWrapper();
        await flushPromises();

        // Sem usuario
        await context.wrapper!.find('form').trigger('submit');
        expect(mockFeedbackShow).toHaveBeenCalledWith('Erro', expect.stringContaining('Selecione um usuário'), 'danger');

        // Com usuario, sem justificativa
        context.wrapper!.vm.usuarioSelecionado = '111';
        await context.wrapper!.find('form').trigger('submit');
        expect(mockFeedbackShow).toHaveBeenCalledWith('Erro', expect.stringContaining('Preencha data de início'), 'danger');
    });

    it('lida com erro no mount', async () => {
        mockBuscarUnidade.mockRejectedValueOnce(new Error('Fetch Error'));
        context.wrapper = criarWrapper();
        await flushPromises();

        expect(context.wrapper!.vm.erroUsuario).toBe("Falha ao carregar dados da unidade ou usuários.");
    });
});
