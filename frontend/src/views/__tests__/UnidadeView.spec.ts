import {describe, expect, it, vi} from 'vitest';
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
const { mockPush, mockUnidadeData, mockUsuario, mockUsuarioResponsavel } = vi.hoisted(() => {
    const u = {
        codigo: 10,
        nome: 'Titular Teste',
        unidade: { codigo: 1, sigla: 'TEST' }
    };
    const ur = {
        codigo: 20,
        nome: 'Responsavel Teste',
        unidade: { codigo: 1, sigla: 'TEST' }
    };
    return { 
        mockPush: vi.fn(),
        mockUnidadeData: {
            codigo: 1,
            sigla: 'TEST',
            nome: 'Unidade Teste',
            usuarioCodigo: 10,
            tituloTitular: '123456',
            filhas: [
                { codigo: 2, sigla: 'SUB1', nome: 'Subordinada 1', filhas: [] },
                { codigo: 3, sigla: 'SUB2', nome: 'Subordinada 2', filhas: [] }
            ]
        },
        mockUsuario: u,
        mockUsuarioResponsavel: ur
    };
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
        buscarUsuarioPorTitulo: vi.fn().mockResolvedValue(mockUsuario),
    }
});

vi.mock('@/services/unidadeService', () => ({
    buscarArvoreUnidade: vi.fn().mockResolvedValue(mockUnidadeData),
    buscarUnidadePorSigla: vi.fn().mockResolvedValue(mockUnidadeData),
}));

vi.mock('@/services/atribuicaoTemporariaService', () => ({
    buscarTodasAtribuicoes: vi.fn().mockResolvedValue([]),
}));

vi.mock('@/services/mapaService', () => ({
    obterMapaCompleto: vi.fn().mockResolvedValue(null),
}));

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

    const mockUnidade = mockUnidadeData;


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
                },
                { stubActions: false }
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

        // Spies are already created by Pinia for actions if stubActions: true, 
        // but since we used false, we should only mock those we want to change behavior
        vi.spyOn(unidadesStore, 'buscarArvoreUnidade').mockResolvedValue(null);
        vi.spyOn(atribuicaoStore, 'buscarAtribuicoes').mockResolvedValue(null);
        
        usuariosStore.obterUsuarioPorId = vi.fn().mockImplementation((codigo: number) => {
            if (codigo === 10) return mockUsuario;
            if (codigo === 20) return mockUsuarioResponsavel;
            return null;
        });

        return { wrapper: context.wrapper, unidadesStore, atribuicaoStore, perfilStore, usuariosStore, mapasStore };
    };

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
        const today = new Date();
        const tomorrow = new Date(today);
        tomorrow.setDate(today.getDate() + 1);
        const yesterday = new Date(today);
        yesterday.setDate(today.getDate() - 1);

        const mockAtribuicao = {
            usuario: { ...mockUsuarioResponsavel, unidade: { codigo: 1 } },
            unidade: { ...mockUnidade },
            dataInicio: yesterday.toISOString(),
            dataTermino: tomorrow.toISOString(),
        };

        const { wrapper, atribuicaoStore } = createWrapper({
            unidades: {
                unidade: mockUnidade
            },
            atribuicoes: {
                atribuicoes: [mockAtribuicao]
            }
        });

        // Mock obterAtribuicoesPorUnidade para retornar a atribuição
        vi.spyOn(atribuicaoStore, 'obterAtribuicoesPorUnidade').mockReturnValue([mockAtribuicao]);

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

        createWrapper({
            unidades: {
                unidade: { ...mockUnidade, tituloTitular: '123' }
            }
        });
        await flushPromises();

        expect(logger.error).toHaveBeenCalledWith('Erro ao buscar titular:', expect.any(Error));
    });

    it('renders clickable contact links for titular', async () => {
        // Setup mock user with contact info
        const mockUserWithContact = {
            ...mockUsuario,
            ramal: '1234',
            email: 'test@example.com'
        };
        (buscarUsuarioPorTitulo as any).mockResolvedValue(mockUserWithContact);

        const { wrapper } = createWrapper({
            unidades: {
                unidade: mockUnidade
            }
        });

        await wrapper.vm.$nextTick();
        await flushPromises();

        // Check Ramal Link
        const ramalLink = wrapper.find('a[href="tel:1234"]');
        expect(ramalLink.exists()).toBe(true);
        expect(ramalLink.text()).toBe('1234');
        expect(ramalLink.attributes('aria-label')).toBe('Ligar para 1234');

        // Check Email Link
        const emailLink = wrapper.find('a[href="mailto:test@example.com"]');
        expect(emailLink.exists()).toBe(true);
        expect(emailLink.text()).toBe('test@example.com');
        expect(emailLink.attributes('aria-label')).toBe('Enviar e-mail para test@example.com');
    });
});
