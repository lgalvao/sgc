import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import UnidadeView from '@/views/UnidadeView.vue';
import {useUnidadesStore} from '@/stores/unidades';
import {useAtribuicaoTemporariaStore} from '@/stores/atribuicoes';
import {usePerfilStore} from '@/stores/perfil';
import {useUsuariosStore} from '@/stores/usuarios';
import {useMapasStore} from '@/stores/mapas';
import {buscarUsuarioPorTitulo} from '@/services/usuarioService';
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import {logger} from "@/utils";

// Mocks
const { mockPush } = vi.hoisted(() => {
    return { mockPush: vi.fn() };
});

vi.mock("@/utils", () => ({
    logger: {
        error: vi.fn(),
    }
}));

vi.mock('vue-router', async (importOriginal) => {
    const actual: any = await importOriginal();
    return {
        ...actual,
        useRouter: () => ({
            push: mockPush,
        }),
    };
});

vi.mock('@/services/usuarioService', async (importOriginal) => {
    const actual: any = await importOriginal();
    return {
        ...actual,
        buscarUsuarioPorTitulo: vi.fn(),
    }
});

const TreeTableStub = {
    template: '<div data-testid="tree-table"></div>',
    props: ['data', 'columns', 'title'],
    emits: ['row-click']
};

describe('UnidadeView.vue', () => {
    const context = setupComponentTest();
    let unidadesStore: any;
    let atribuicaoStore: any;
    let perfilStore: any;
    let usuariosStore: any;
    let mapasStore: any;

    const mockUnidade = {
        codigo: 1,
        sigla: 'TEST',
        nome: 'Unidade Teste',
        usuarioCodigo: 10,
        tituloTitular: '123456',
        filhas: [
            { codigo: 2, sigla: 'SUB1', nome: 'Subordinada 1', filhas: [] },
            { codigo: 3, sigla: 'SUB2', nome: 'Subordinada 2', filhas: [] }
        ]
    };

    const mockUsuario = {
        codigo: 10,
        nome: 'Titular Teste',
        email: 'titular@test.com',
        ramal: '123'
    };

    const mockUsuarioResponsavel = {
        codigo: 20,
        nome: 'Responsavel Teste',
        email: 'resp@test.com',
        ramal: '456'
    }

    const createWrapper = (initialStateOverride = {}) => {
        context.wrapper = mount(UnidadeView, {
            ...getCommonMountOptions(
                {
                    unidades: {
                        unidade: null,
                    },
                    atribuicoes: {
                        atribuicoes: [],
                    },
                    perfil: {
                        perfilSelecionado: 'USER',
                    },
                    usuarios: {
                        usuarios: [],
                    },
                    mapas: {
                        mapaCompleto: null,
                    },
                    ...initialStateOverride
                },
                {
                    BContainer: { template: '<div><slot /></div>' },
                    BCard: { template: '<div><slot /></div>' },
                    BCardBody: { template: '<div><slot /></div>' },
                    BButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
                    TreeTable: TreeTableStub,
                }
            ),
            props: {
                codUnidade: 1
            },
        });

        unidadesStore = useUnidadesStore();
        atribuicaoStore = useAtribuicaoTemporariaStore();
        perfilStore = usePerfilStore();
        usuariosStore = useUsuariosStore();
        mapasStore = useMapasStore();

        unidadesStore.buscarArvoreUnidade.mockResolvedValue(null);
        atribuicaoStore.buscarAtribuicoes.mockResolvedValue(null);
        atribuicaoStore.obterAtribuicoesPorUnidade = vi.fn().mockReturnValue([]);
        usuariosStore.obterUsuarioPorId = vi.fn().mockImplementation((codigo: number) => {
            if (codigo === 10) return mockUsuario;
            if (codigo === 20) return mockUsuarioResponsavel;
            return null;
        });

        return { wrapper: context.wrapper, unidadesStore, atribuicaoStore, perfilStore, usuariosStore, mapasStore };
    };

    beforeEach(() => {
        vi.clearAllMocks();
        (buscarUsuarioPorTitulo as any).mockResolvedValue(mockUsuario);
    });

    it('fetches data on mount', async () => {
        const { unidadesStore, atribuicaoStore } = createWrapper();
        expect(unidadesStore.buscarArvoreUnidade).toHaveBeenCalledWith(1);
        expect(atribuicaoStore.buscarAtribuicoes).toHaveBeenCalled();
    });

    it('renders unit details correctly', async () => {
        const { wrapper } = createWrapper({
            unidades: {
                unidade: mockUnidade
            }
        });

        await wrapper.vm.$nextTick();
        await flushPromises();

        expect(wrapper.text()).toContain('TEST - Unidade Teste');
        expect(wrapper.text()).toContain('Titular: Titular Teste');
    });

    it('renders "Criar atribuição" button only for ADMIN', async () => {
        const { wrapper, perfilStore } = createWrapper({
            unidades: {
                unidade: mockUnidade
            }
        });
        perfilStore.perfilSelecionado = 'ADMIN';
        await wrapper.vm.$nextTick();
        expect(wrapper.find('[data-testid="unidade-view__btn-criar-atribuicao"]').exists()).toBe(true);

        perfilStore.perfilSelecionado = 'USER';
        await wrapper.vm.$nextTick();
        expect(wrapper.find('[data-testid="unidade-view__btn-criar-atribuicao"]').exists()).toBe(false);
    });

    it('navigates to create assignment', async () => {
        const { wrapper, perfilStore } = createWrapper({
            unidades: {
                unidade: mockUnidade
            }
        });
        perfilStore.perfilSelecionado = 'ADMIN';
        await wrapper.vm.$nextTick();

        await wrapper.find('[data-testid="unidade-view__btn-criar-atribuicao"]').trigger('click');
        expect(mockPush).toHaveBeenCalledWith({ path: '/unidade/1/atribuicao' });
    });

    it('calculates dynamic responsible person correctly', async () => {
        const { wrapper, atribuicaoStore } = createWrapper({
            unidades: {
                unidade: mockUnidade
            }
        });

        const today = new Date();
        const tomorrow = new Date(today);
        tomorrow.setDate(today.getDate() + 1);
        const yesterday = new Date(today);
        yesterday.setDate(today.getDate() - 1);

        const mockAtribuicao = {
            usuario: { ...mockUsuarioResponsavel, unidade: { codigo: 1 } },
            unidade: { codigo: 1 },
            dataInicio: yesterday.toISOString(),
            dataTermino: tomorrow.toISOString(),
        };

        atribuicaoStore.obterAtribuicoesPorUnidade.mockReturnValue([mockAtribuicao]);

        // Force re-computation
        await wrapper.vm.$nextTick();
        await flushPromises();

        expect(wrapper.text()).toContain('Responsável: Responsavel Teste');
    });

    it('renders subordinate units tree table', async () => {
        const { wrapper } = createWrapper({
            unidades: {
                unidade: mockUnidade
            }
        });
        await wrapper.vm.$nextTick();

        const treeTable = wrapper.findComponent(TreeTableStub);
        expect(treeTable.exists()).toBe(true);
    });

    it('navigates to subordinate unit on row click', async () => {
        const { wrapper } = createWrapper({
            unidades: {
                unidade: mockUnidade
            }
        });
        await wrapper.vm.$nextTick();

        const treeTable = wrapper.findComponent(TreeTableStub);
        treeTable.vm.$emit('row-click', { codigo: 2 });

        expect(mockPush).toHaveBeenCalledWith({ path: '/unidade/2' });
    });

    it('renders and clicks "Mapa vigente" button when map exists', async () => {
        const { wrapper, mapasStore } = createWrapper({
            unidades: {
                unidade: mockUnidade
            }
        });
        mapasStore.mapaCompleto = { subprocessoCodigo: 99 };
        await wrapper.vm.$nextTick();

        const btn = wrapper.find('[data-testid="btn-mapa-vigente"]');
        expect(btn.exists()).toBe(true);

        await btn.trigger('click');
        expect(mockPush).toHaveBeenCalledWith({
            name: 'SubprocessoVisMapa',
            params: { codProcesso: 99, siglaUnidade: 'TEST' }
        });
    });

    it('displays error alert when unidadesStore has error', async () => {
        const { wrapper, unidadesStore } = createWrapper();
        // Since createTestingPinia is used, we can directly modify state or use patch
        unidadesStore.lastError = { message: 'Erro ao carregar unidade' };
        await wrapper.vm.$nextTick();

        expect(wrapper.find('.alert').text()).toContain('Erro ao carregar unidade');
    });

    it('logs error when fetching titular fails', async () => {
        (buscarUsuarioPorTitulo as any).mockRejectedValue(new Error('Fetch error'));

        const { wrapper } = createWrapper({
            unidades: {
                unidade: { ...mockUnidade, tituloTitular: '123' }
            }
        });
        await flushPromises();

        expect(logger.error).toHaveBeenCalledWith('Erro ao buscar titular:', expect.any(Error));
    });
});
