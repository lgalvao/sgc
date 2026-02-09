import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import Configuracoes from '../Configuracoes.vue';
import {usePerfilStore} from '@/stores/perfil';
import * as administradorService from '@/services/administradorService';

// Mock dependencies
vi.mock('@/services/administradorService', () => ({
    listarAdministradores: vi.fn(),
    adicionarAdministrador: vi.fn(),
    removerAdministrador: vi.fn(),
}));

describe('Configuracoes', () => {
    let wrapper: any;
    let perfilStore: any;

    beforeEach(async () => {
        vi.clearAllMocks();

        const pinia = createTestingPinia({
            stubActions: false,
            initialState: {
                configuracoes: {
                    parametros: [],
                    loading: false,
                    error: null,
                },
                perfil: {
                    perfis: ['ADMIN'],
                    isAdmin: true
                }
            }
        });

        perfilStore = usePerfilStore(pinia);

        (administradorService.listarAdministradores as any).mockResolvedValue([]);

        wrapper = mount(Configuracoes, {
            global: {
                plugins: [pinia],
                stubs: {
                    PageHeader: true,
                    AdministradoresSection: { template: '<div>AdministradoresSection</div>' },
                    ParametrosSection: { template: '<div>ParametrosSection</div>' }
                }
            }
        });

        await wrapper.vm.$nextTick();
    });

    it('deve renderizar corretamente', () => {
        expect(wrapper.find('[data-testid="page-header"]').exists() || wrapper.text().length > 0).toBe(true);
    });

    it('deve exibir seção de administradores quando é admin', () => {
        expect(wrapper.text()).toContain('AdministradoresSection');
    });

    it('não deve exibir seção de administradores quando não é admin', async () => {
        perfilStore.isAdmin = false;
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).not.toContain('AdministradoresSection');
    });

    it('deve sempre exibir seção de parâmetros', () => {
        expect(wrapper.text()).toContain('ParametrosSection');
    });
});
