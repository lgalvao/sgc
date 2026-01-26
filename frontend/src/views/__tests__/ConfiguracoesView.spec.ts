import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import ConfiguracoesView from '../ConfiguracoesView.vue';
import {useConfiguracoesStore} from '@/stores/configuracoes';
import {useNotificacoesStore} from '@/stores/feedback';
import {usePerfilStore} from '@/stores/perfil';
import * as administradorService from '@/services/administradorService';
import {apiClient} from '@/axios-setup';

// Mock dependencies
vi.mock('@/services/administradorService', () => ({
    listarAdministradores: vi.fn(),
    adicionarAdministrador: vi.fn(),
    removerAdministrador: vi.fn(),
}));

// Mock API Client
vi.mock('@/axios-setup', () => ({
    apiClient: {
        get: vi.fn(),
        post: vi.fn()
    }
}));

describe('ConfiguracoesView', () => {
    let wrapper: any;
    let configuracoesStore: any;
    let notificacoesStore: any;
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

        configuracoesStore = useConfiguracoesStore(pinia);
        vi.spyOn(configuracoesStore, 'carregarConfiguracoes');
        vi.spyOn(configuracoesStore, 'salvarConfiguracoes');

        perfilStore = usePerfilStore(pinia);
        notificacoesStore = useNotificacoesStore(pinia);

        (apiClient.get as any).mockResolvedValue({ data: [] });
        (apiClient.post as any).mockResolvedValue({ data: [] });

        wrapper = mount(ConfiguracoesView, {
            global: {
                plugins: [pinia],
                stubs: {
                    BModal: true
                }
            }
        });

        // IMPORTANT: Manually set administradores ref in component to empty array if needed?
        // No, the error `administradores.length` undefined means `administradores` ref is undefined?
        // But it's initialized as `ref<AdministradorDto[]>([])`.
        // The issue is likely that `carregarAdministradores` sets it to undefined?
        // Or `listarAdministradores` mock returns undefined by default?
        // In beforeEach of `describe` block I set a mock return value, but NOT in the global `beforeEach`.
        // So `listarAdministradores` returns undefined, and `administradores.value = await ...` sets it to undefined.
        // I must ensure mock returns an array by default.
        (administradorService.listarAdministradores as any).mockResolvedValue([]);

        await wrapper.vm.$nextTick();
        await new Promise(resolve => setTimeout(resolve, 10));
        await wrapper.vm.$nextTick();
    });

    it('deve carregar configurações ao montar', () => {
        expect(configuracoesStore.carregarConfiguracoes).toHaveBeenCalled();
    });

    it('deve exibir spinner quando carregando', async () => {
        configuracoesStore.loading = true;
        await wrapper.vm.$nextTick();

        expect(wrapper.find('.spinner-border').exists()).toBe(true);
        expect(wrapper.find('form').exists()).toBe(false);
    });

    it('deve exibir erro quando houver falha', async () => {
        configuracoesStore.error = 'Erro teste';
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).toContain('Erro teste');
    });

    it('deve chamar carregarConfiguracoes ao clicar em Recarregar', async () => {
        configuracoesStore.carregarConfiguracoes.mockClear();

        const btn = wrapper.findAll('button').find((b: any) => b.text().includes('Recarregar'));
        await btn.trigger('click');

        expect(configuracoesStore.carregarConfiguracoes).toHaveBeenCalled();
    });

    it('deve chamar salvarConfiguracoes ao submeter formulário', async () => {
        configuracoesStore.salvarConfiguracoes.mockResolvedValue(true);
        const form = wrapper.find('form');
        await form.trigger('submit');
        await wrapper.vm.$nextTick();
        await new Promise(resolve => setTimeout(resolve, 0));

        expect(configuracoesStore.salvarConfiguracoes).toHaveBeenCalled();
        expect(notificacoesStore.show).toHaveBeenCalledWith(
            'Sucesso',
            'Configurações salvas com sucesso!',
            'success'
        );
    });

    it('deve exibir erro se falhar ao salvar', async () => {
        configuracoesStore.salvarConfiguracoes.mockResolvedValueOnce(false);
        await wrapper.find('form').trigger('submit');
        // Need to wait for async save to complete
        await wrapper.vm.$nextTick(); // Wait for promise resolution (mockResolvedValue is mostly sync but Vue update is async)
        // Wait for next tick inside component
        await new Promise(resolve => setTimeout(resolve, 0));

        expect(notificacoesStore.show).toHaveBeenCalledWith(
            'Erro',
            'Erro ao salvar configurações.',
            'danger'
        );
    });

    describe('Seção de Administradores', () => {
        beforeEach(async () => {
            // Mock setup for this block
             (administradorService.listarAdministradores as any).mockResolvedValue([
                { nome: 'Admin 1', tituloEleitoral: '123', matricula: '001', unidadeSigla: 'TEST' }
            ]);

            // Re-trigger load if needed, or update manually
            // Since onMounted already ran in global beforeEach with empty list,
            // we can call `carregarAdministradores` manually or re-mount.
            // Re-mounting is safer.

            // Re-setup Pinia to avoid shared state pollution
             const pinia = createTestingPinia({
                stubActions: false,
                initialState: {
                    configuracoes: { parametros: [] },
                    perfil: { perfis: ['ADMIN'], isAdmin: true }
                }
            });

            configuracoesStore = useConfiguracoesStore(pinia);
            vi.spyOn(configuracoesStore, 'carregarConfiguracoes');
            perfilStore = usePerfilStore(pinia);
            notificacoesStore = useNotificacoesStore(pinia);

            wrapper = mount(ConfiguracoesView, {
                global: { plugins: [pinia], stubs: { BModal: true } }
            });

            await wrapper.vm.$nextTick();
            await new Promise(resolve => setTimeout(resolve, 10));
            await wrapper.vm.$nextTick();
        });

        it('não deve exibir seção se não for admin', async () => {
            perfilStore.perfis = ['SERVIDOR'];
            // Need to force re-evaluation of computed property or re-render
            await wrapper.vm.$nextTick();

            expect(wrapper.text()).not.toContain('Administradores do Sistema');
        });

        it('deve listar administradores', async () => {
             expect(administradorService.listarAdministradores).toHaveBeenCalled();
             expect(wrapper.text()).toContain('Admin 1');
             expect(wrapper.text()).toContain('123');
        });

        it('deve abrir modal ao clicar em Adicionar', async () => {
            const btn = wrapper.findAll('button').find((b: any) => b.text().includes('Adicionar administrador'));
            await btn.trigger('click');
            expect(wrapper.vm.mostrarModalAdicionarAdmin).toBe(true);
        });

        it('deve chamar adicionarAdministrador ao confirmar no modal', async () => {
            wrapper.vm.mostrarModalAdicionarAdmin = true;
            wrapper.vm.novoAdminTitulo = '999';
            (administradorService.adicionarAdministrador as any).mockResolvedValue({});

            await wrapper.vm.adicionarAdmin();

            expect(administradorService.adicionarAdministrador).toHaveBeenCalledWith('999');
            expect(notificacoesStore.show).toHaveBeenCalledWith(expect.anything(), expect.stringContaining('sucesso'), 'success');
        });

        it('deve chamar removerAdministrador ao confirmar exclusão', async () => {
            wrapper.vm.adminParaRemover = { tituloEleitoral: '123', nome: 'Admin 1' };
            (administradorService.removerAdministrador as any).mockResolvedValue({});

            await wrapper.vm.removerAdmin();

            expect(administradorService.removerAdministrador).toHaveBeenCalledWith('123');
            expect(notificacoesStore.show).toHaveBeenCalledWith(expect.anything(), expect.stringContaining('sucesso'), 'success');
        });

        it('deve exibir erro ao falhar listagem de administradores', async () => {
            const error = new Error('Erro API');
            (administradorService.listarAdministradores as any).mockRejectedValue(error);

            await wrapper.vm.carregarAdministradores();

            expect(wrapper.text()).toContain('Erro API');
        });

        it('deve exibir alerta se tentar adicionar administrador com título vazio', async () => {
            wrapper.vm.novoAdminTitulo = '   ';
            await wrapper.vm.adicionarAdmin();

            expect(notificacoesStore.show).toHaveBeenCalledWith('Erro', 'Digite um título eleitoral válido', 'warning');
            expect(administradorService.adicionarAdministrador).not.toHaveBeenCalled();
        });

        it('deve exibir erro ao falhar adição de administrador', async () => {
            wrapper.vm.novoAdminTitulo = '123';
            (administradorService.adicionarAdministrador as any).mockRejectedValue(new Error('Falha ao adicionar'));

            await wrapper.vm.adicionarAdmin();

            expect(notificacoesStore.show).toHaveBeenCalledWith('Erro', 'Falha ao adicionar', 'danger');
        });

        it('deve exibir erro ao falhar remoção de administrador', async () => {
            wrapper.vm.adminParaRemover = { tituloEleitoral: '123', nome: 'Admin' };
            (administradorService.removerAdministrador as any).mockRejectedValue(new Error('Falha ao remover'));

            await wrapper.vm.removerAdmin();

            expect(notificacoesStore.show).toHaveBeenCalledWith('Erro', 'Falha ao remover', 'danger');
        });
    });
});
