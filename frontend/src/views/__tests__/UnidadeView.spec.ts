import {describe, expect, it, vi, beforeEach} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import UnidadeView from '@/views/UnidadeView.vue';
import {useAtribuicaoTemporariaStore} from '@/stores/atribuicoes';
import {createTestingPinia} from '@pinia/testing';
import {setupComponentTest} from "@/test-utils/componentTestHelpers";

// Mocks
const { mockPush, mockUnidadeData, mockUsuario } = vi.hoisted(() => {
    return { 
        mockPush: vi.fn(),
        mockUnidadeData: {
            codigo: 1,
            sigla: 'TEST',
            nome: 'Unidade Teste',
            usuarioCodigo: 10,
            tituloTitular: '123456',
            filhas: [
                { codigo: 2, sigla: 'SUB1', nome: 'Subordinada 1', filhas: [] }
            ]
        },
        mockUsuario: { codigo: 10, nome: 'Titular' }
    };
});

vi.mock('vue-router', async (importOriginal) => {
    const actual: any = await importOriginal();
    return {
        ...actual,
        useRouter: () => ({ push: mockPush }),
    };
});

vi.mock('@/services/usuarioService', () => ({
    buscarUsuarioPorTitulo: vi.fn().mockResolvedValue(mockUsuario),
}));

vi.mock('@/services/unidadeService', () => ({
    buscarArvoreUnidade: vi.fn().mockResolvedValue(mockUnidadeData),
    buscarUnidadePorSigla: vi.fn().mockResolvedValue(mockUnidadeData),
}));

describe('UnidadeView.vue', () => {
    const context = setupComponentTest();

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renderiza detalhes e navegações', async () => {
        const pinia = createTestingPinia({
            stubActions: true,
            initialState: {
                unidades: {
                    unidade: mockUnidadeData,
                },
                perfil: {
                    perfilSelecionado: 'ADMIN'
                },
                mapas: {
                    mapaCompleto: { subprocessoCodigo: 99, unidade: { sigla: 'TEST' } }
                }
            }
        });

        const atStore = useAtribuicaoTemporariaStore();
        // @ts-expect-error - mocking store
        atStore.obterAtribuicoesPorUnidade.mockReturnValue([]);

        context.wrapper = mount(UnidadeView, {
            global: {
                plugins: [pinia],
                stubs: {
                    BContainer: { template: '<div><slot /></div>' },
                    BAlert: { template: '<div class="alert-stub"><slot /><button @click="$emit(\'dismissed\')">x</button></div>', emits: ['dismissed'] },
                    BButton: {
                        template: '<button @click="$emit(\'click\')" v-bind="$attrs"><slot /></button>',
                        inheritAttrs: false
                    },
                    PageHeader: { template: '<div>{{ title }}<slot name="actions" /></div>', props: ['title'] },
                    TreeTable: { template: '<div id="tree-table-stub" @click="$emit(\'row-click\', {codigo: 2})">Tree</div>', emits: ['row-click'] }
                }
            },
            props: { codUnidade: 1 }
        });
        await flushPromises();

        expect(context.wrapper.text()).toContain('TEST - Unidade Teste');

        await context.wrapper.find('[data-testid="btn-mapa-vigente"]').trigger('click');
        expect(mockPush).toHaveBeenCalledWith(expect.objectContaining({ name: 'SubprocessoVisMapa' }));
    });

    it('cobre lógica de responsável dinâmico com dataFim', async () => {
        const pinia = createTestingPinia({
            stubActions: true,
            initialState: {
                unidades: { unidade: mockUnidadeData }
            }
        });

        const atStore = useAtribuicaoTemporariaStore();
        // @ts-expect-error - mocking store
        atStore.obterAtribuicoesPorUnidade.mockReturnValue([
            {
                dataInicio: '2020-01-01',
                dataFim: '2099-12-31',
                usuario: { nome: 'Substituto Fim' }
            }
        ]);

        context.wrapper = mount(UnidadeView, {
            global: {
                plugins: [pinia],
                stubs: {
                    BContainer: { template: '<div><slot /></div>' },
                    BAlert: { template: '<div />' },
                    PageHeader: { template: '<div>{{ title }}</div>', props: ['title'] },
                    TreeTable: { template: '<div />' }
                }
            },
            props: { codUnidade: 1 }
        });
        await flushPromises();

        expect((context.wrapper.vm as any).unidadeComResponsavelDinamico.responsavel.nome).toBe('Substituto Fim');
    });
});
