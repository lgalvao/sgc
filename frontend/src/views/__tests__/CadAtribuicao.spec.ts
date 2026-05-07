import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import CadAtribuicao from '@/views/AtribuicaoTemporariaView.vue';
import {criarAtribuicaoTemporaria} from '@/services/atribuicaoTemporariaService';
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import {TEXTOS} from "@/constants/textos";

const {mockPush, mockBuscarUnidade, mockPesquisarUsuarios} = vi.hoisted(() => {
    return {
        mockPush: vi.fn(),
        mockBuscarUnidade: vi.fn(),
        mockPesquisarUsuarios: vi.fn(),
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

vi.mock('@/services/atribuicaoTemporariaService', () => ({
    criarAtribuicaoTemporaria: vi.fn(),
    buscarTodasAtribuicoes: vi.fn().mockResolvedValue([]),
}));

vi.mock('@/services/unidadeService', async (importOriginal) => {
    const actual = await importOriginal<typeof import('@/services/unidadeService')>();
    return {
        ...actual,
        buscarArvoreUnidade: mockBuscarUnidade,
        buscarDiagnosticoOrganizacional: vi.fn().mockResolvedValue({
            possuiViolacoes: false,
            resumo: '',
            quantidadeTiposViolacao: 0,
            quantidadeOcorrencias: 0,
            grupos: [],
        }),
    };
});

vi.mock('@/services/usuarioService', () => ({
    pesquisarUsuarios: mockPesquisarUsuarios,
}));

vi.mock('@/composables/usePerfil', () => ({
    usePerfil: () => ({
        mostrarDiagnosticoOrganizacional: {value: true}
    })
}));

describe('CadAtribuicao.vue', () => {
    const context = setupComponentTest();

    const mockUnidade = {
        codigo: 1,
        sigla: 'TEST',
        nome: 'Unidade teste'
    };

    const mockUsuarios = [
        {codigo: 111, nome: 'Servidor 1', tituloEleitoral: '111'},
        {codigo: 222, nome: 'Servidor 2', tituloEleitoral: '222'}
    ];

    function criarWrapper() {
        return mount(CadAtribuicao, {
            ...getCommonMountOptions(
                {
                    unidade: {
                        cacheUnidades: new Map()
                    }
                },
                {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    PageHeader: {template: '<div><slot /><slot name="actions" /></div>'},
                    CarregamentoPagina: {template: '<div data-testid="carregamento-pagina"></div>'},
                    BContainer: {template: '<div><slot /></div>'},
                    BForm: {template: '<form @submit.prevent="$emit(\'submit\', { preventDefault: () => {} })"><slot /></form>'},
                    BFormInput: {
                        template: '<input v-bind="$attrs" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
                        props: ['modelValue']
                    },
                    BListGroup: {template: '<div><slot /></div>'},
                    BListGroupItem: {template: '<button @click="$emit(\'click\')"><slot /></button>'},
                    BFormTextarea: {
                        template: '<textarea v-bind="$attrs" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>',
                        props: ['modelValue']
                    },
                    BButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                    BAlert: {template: '<div role="alert"><slot /></div>'},
                    InputData: {
                        props: ['modelValue'],
                        emits: ['update:modelValue'],
                        template: '<input v-bind="$attrs" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
                    },
                    BuscadorUsuarios: {
                        props: ['selecionado', 'termo'],
                        emits: ['update:selecionado', 'update:termo'],
                        template: '<input v-bind="$attrs" :value="termo" @input="$emit(\'update:termo\', $event.target.value)" />'
                    },
                    LoadingButton: {template: '<button v-bind="$attrs" @click="$emit(\'click\')"><slot /></button>'},
                },
                {stubActions: false} // Permite que as stores chamem os serviços mockados
            ),
            props: {
                codUnidade: 1
            },
        });
    }

    beforeEach(() => {
        vi.clearAllMocks();
        vi.spyOn(console, 'error').mockImplementation(() => {
        });

        mockBuscarUnidade.mockResolvedValue(mockUnidade);
        mockPesquisarUsuarios.mockResolvedValue(mockUsuarios);

        context.wrapper = criarWrapper();
    });

    it('busca dados no mount', async () => {
        await flushPromises();

        expect(mockBuscarUnidade).toHaveBeenCalledWith(1);
        expect(mockPesquisarUsuarios).not.toHaveBeenCalled();
    });

    it('submete o formulário com sucesso', async () => {
        // Resetar e configurar o mock apenas para este teste
        (criarAtribuicaoTemporaria as any).mockReset();
        (criarAtribuicaoTemporaria as any).mockResolvedValue({});

        // Remontar o componente para este teste
        context.wrapper = criarWrapper();
        await flushPromises();

        context.wrapper.vm.usuarioSelecionado = '111';

        const dateInput = context.wrapper.find('[data-testid="input-data-termino"]');
        await dateInput.setValue('2023-12-31');

        const dateInicioInput = context.wrapper.find('[data-testid="input-data-inicio"]');
        await dateInicioInput.setValue('2023-01-01');

        const textarea = context.wrapper.find('[data-testid="textarea-justificativa"]');
        await textarea.setValue('Justificativa de teste');

        await context.wrapper.find('form').trigger('submit');
        await flushPromises();

        expect(criarAtribuicaoTemporaria).toHaveBeenCalledWith(1, {
            tituloEleitoralUsuario: '111',
            dataInicio: '2023-01-01',
            dataTermino: '2023-12-31',
            justificativa: 'Justificativa de teste'
        });
    });

    it('lida com erro de submissão', async () => {
        // Resetar e configurar o mock apenas para este teste
        (criarAtribuicaoTemporaria as any).mockReset();
        (criarAtribuicaoTemporaria as any).mockRejectedValue(new Error('API Error'));

        // Remontar o componente para este teste
        context.wrapper = criarWrapper();
        await flushPromises();

        context.wrapper.vm.usuarioSelecionado = '111';
        context.wrapper.vm.dataInicio = '2023-01-01';
        context.wrapper.vm.dataTermino = '2023-12-31';
        context.wrapper.vm.justificativa = 'Teste';

        await context.wrapper.find('form').trigger('submit');
        await flushPromises();

        expect(criarAtribuicaoTemporaria).toHaveBeenCalled();
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
        await context.wrapper.find('form').trigger('submit');
        expect(context.wrapper.text()).toContain(TEXTOS.atribuicaoTemporaria.ERRO_SELECIONE_USUARIO);

        // Com usuario, sem justificativa
        context.wrapper.vm.usuarioSelecionado = '111';
        await context.wrapper.find('form').trigger('submit');
        expect(criarAtribuicaoTemporaria).not.toHaveBeenCalled();
    });

    it('lida com erro no mount', async () => {
        mockBuscarUnidade.mockRejectedValueOnce(new Error('Fetch error'));
        context.wrapper = criarWrapper();
        await flushPromises();

        expect(context.wrapper.vm.erroUsuario).toBe(TEXTOS.atribuicaoTemporaria.ERRO_CARREGAR);
    });
});
