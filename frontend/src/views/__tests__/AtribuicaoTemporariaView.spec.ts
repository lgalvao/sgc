import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import AtribuicaoTemporariaView from '../AtribuicaoTemporariaView.vue';
import {buscarUnidadePorCodigo} from '@/services/unidadeService';
import {criarAtribuicaoTemporaria} from '@/services/atribuicaoTemporariaService';
import {createMemoryHistory, createRouter} from 'vue-router';
import {createPinia, setActivePinia} from 'pinia';
import type {Unidade} from '@/types/tipos';

const unidadeMinima: Unidade = {codigo: 1, sigla: 'TESTE', nome: 'Unidade de Teste'};

vi.mock('@/services/unidadeService', async (importOriginal) => {
    const actual = await importOriginal<typeof import('@/services/unidadeService')>();
    return {
        ...actual,
        buscarUnidadePorCodigo: vi.fn(),
    };
});

vi.mock('@/services/usuarioService', () => ({
    pesquisarUsuarios: vi.fn(),
}));

vi.mock('@/services/atribuicaoTemporariaService', () => ({
    criarAtribuicaoTemporaria: vi.fn(),
}));

const mockNotify = vi.fn();
const mockClear = vi.fn();
vi.mock('@/composables/useNotification', () => ({
    useNotification: () => ({
        notificacao: {value: null},
        notify: mockNotify,
        clear: mockClear,
    }),
}));

const router = createRouter({
    history: createMemoryHistory(),
    routes: [
        {path: '/', component: {template: '<div></div>'}},
        {path: '/unidade/:codigo', component: {template: '<div></div>'}}
    ],
});

const mountOptions = {
    props: {codUnidade: 1},
    global: {
        plugins: [router, createPinia()],
        stubs: {
            LayoutPadrao: {
                template: '<div><slot></slot></div>',
            },
            PageHeader: {
                template: '<div><slot></slot><slot name="actions"></slot></div>',
            },
            InputData: {
                name: 'InputData',
                template: '<input type="date" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
                props: ['modelValue']
            },
            LoadingButton: {
                props: ['disabled', 'loading', 'text'],
                template: '<button :disabled="disabled" @click="$emit(\'click\')">LoadingButton</button>'
            },
            BuscadorUsuarios: {
                name: 'BuscadorUsuarios',
                props: ['termo', 'selecionado'],
                template: '<div></div>',
                methods: {
                    limparResultadosPesquisaUsuarios() {
                    }
                }
            },
            BFormTextarea: {
                name: 'BFormTextarea',
                props: ['modelValue', 'state'],
                template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>'
            },
            AppAlert: {
                name: 'AppAlert',
                template: '<div><button @click="$emit(\'dismissed\')">x</button></div>'
            },
            CarregamentoPagina: {
                template: '<div data-testid="loading">Carregando...</div>'
            }
        },
    },
};

describe('AtribuicaoTemporariaView', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
    });

    it('deve carregar a unidade corretamente no onMounted', async () => {
        vi.mocked(buscarUnidadePorCodigo).mockResolvedValue({
            codigo: 1,
            sigla: 'TESTE-UNIDADE',
            nome: 'Unidade de Teste'
        });

        const wrapper = mount(AtribuicaoTemporariaView, mountOptions);
        await flushPromises();

        expect(buscarUnidadePorCodigo).toHaveBeenCalledWith(1);
        expect(wrapper.text()).toContain('TESTE-UNIDADE');
    });

    it('deve lidar com erro ao carregar unidade', async () => {
        vi.mocked(buscarUnidadePorCodigo).mockRejectedValue(new Error('Erro de API'));

        const wrapper = mount(AtribuicaoTemporariaView, mountOptions);
        await flushPromises();

        // O erroUsuario é exibido no PageHeader ou num alerta?
        // Na real, no código ele atribui a erroUsuario que não é usado diretamente no template para exibição de texto de erro,
        // mas o PageHeader exibe a sigla da unidade se carregada.
        expect(wrapper.text()).not.toContain('TESTE-UNIDADE');
    });

    it('deve validar formulário vazio', async () => {
        vi.mocked(buscarUnidadePorCodigo).mockResolvedValue(unidadeMinima);
        const wrapper = mount(AtribuicaoTemporariaView, mountOptions);
        await flushPromises();

        await wrapper.find('[data-testid="cad-atribuicao__btn-criar-atribuicao"]').trigger('click');
        await flushPromises();

        expect(mockNotify).not.toHaveBeenCalledWith(expect.any(String), 'success');
        expect(criarAtribuicaoTemporaria).not.toHaveBeenCalled();
    });

    it('deve manter o botão de criar habilitado para permitir validação contextual', async () => {
        vi.mocked(buscarUnidadePorCodigo).mockResolvedValue(unidadeMinima);
        const wrapper = mount(AtribuicaoTemporariaView, mountOptions);
        await flushPromises();

        const botaoCriar = wrapper.find('[data-testid="cad-atribuicao__btn-criar-atribuicao"]');
        expect((botaoCriar.element as HTMLButtonElement).disabled).toBe(false);
    });

    it('deve criar atribuicao com sucesso', async () => {
        vi.mocked(buscarUnidadePorCodigo).mockResolvedValue(unidadeMinima);
        vi.mocked(criarAtribuicaoTemporaria).mockResolvedValue();

        const wrapper = mount(AtribuicaoTemporariaView, mountOptions);
        await flushPromises();

        // Preenchendo o formulário via DOM/Componentes
        await wrapper.findComponent({name: 'BuscadorUsuarios'}).vm.$emit('update:selecionado', '999');
        await wrapper.find('[data-testid="input-data-inicio"]').setValue('2025-01-01');
        await wrapper.find('[data-testid="input-data-termino"]').setValue('2025-12-31');
        await wrapper.find('[data-testid="textarea-justificativa"]').setValue('Teste de justificativa');

        await wrapper.find('[data-testid="cad-atribuicao__btn-criar-atribuicao"]').trigger('click');
        await flushPromises();

        expect(criarAtribuicaoTemporaria).toHaveBeenCalledWith(1, {
            tituloEleitoralUsuario: '999',
            dataInicio: '2025-01-01',
            dataTermino: '2025-12-31',
            justificativa: 'Teste de justificativa'
        });
        expect(mockNotify).toHaveBeenCalledWith(expect.any(String), 'success');

        // Verifica se limpou (opcional, mas bom para garantir integridade)
        expect(wrapper.findComponent({name: 'BuscadorUsuarios'}).props('selecionado')).toBeNull();
        expect((wrapper.find('[data-testid="textarea-justificativa"]').element as HTMLTextAreaElement).value).toBe('');
    });

    it('deve lidar com erro ao criar atribuicao', async () => {
        vi.mocked(buscarUnidadePorCodigo).mockResolvedValue(unidadeMinima);
        vi.mocked(criarAtribuicaoTemporaria).mockRejectedValue(new Error('Erro no servidor'));

        const wrapper = mount(AtribuicaoTemporariaView, mountOptions);
        await flushPromises();

        await wrapper.findComponent({name: 'BuscadorUsuarios'}).vm.$emit('update:selecionado', '999');
        await wrapper.find('[data-testid="input-data-inicio"]').setValue('2025-01-01');
        await wrapper.find('[data-testid="input-data-termino"]').setValue('2025-12-31');
        await wrapper.find('[data-testid="textarea-justificativa"]').setValue('Teste de justificativa');

        await wrapper.find('[data-testid="cad-atribuicao__btn-criar-atribuicao"]').trigger('click');
        await flushPromises();

        expect(criarAtribuicaoTemporaria).toHaveBeenCalled();
        // Verifica exibição do erro no BAlert
        expect(wrapper.text()).toContain('Erro no servidor');
        expect(mockNotify).not.toHaveBeenCalledWith(expect.any(String), 'danger');
    });

});
