import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import AdministradoresSection from '@/components/configuracoes/AdministradoresSection.vue';
import {useNotificacoesStore} from '@/stores/feedback';
import * as administradorService from '@/services/administradorService';

// Mock dependencies
vi.mock('@/services/administradorService', () => ({
    listarAdministradores: vi.fn(),
    adicionarAdministrador: vi.fn(),
    removerAdministrador: vi.fn(),
}));

describe('AdministradoresSection', () => {
    let wrapper: any;
    let notificacoesStore: any;

    beforeEach(async () => {
        vi.clearAllMocks();

        const pinia = createTestingPinia({
            stubActions: false,
        });

        notificacoesStore = useNotificacoesStore(pinia);

        const mockAdmins = [
            { tituloEleitoral: '123', nome: 'Admin 1', matricula: 'M001', unidadeSigla: 'UN1' },
            { tituloEleitoral: '456', nome: 'Admin 2', matricula: 'M002', unidadeSigla: 'UN2' }
        ];

        (administradorService.listarAdministradores as any).mockResolvedValue(mockAdmins);

        wrapper = mount(AdministradoresSection, {
            global: {
                plugins: [pinia],
                stubs: {
                    EmptyState: true,
                    ModalConfirmacao: true,
                    LoadingButton: { template: '<button @click="$emit(\'click\')"><slot /></button>', props: ['loading', 'variant', 'size', 'icon', 'text'] }
                }
            }
        });

        await wrapper.vm.$nextTick();
        await new Promise(resolve => setTimeout(resolve, 50));
        await wrapper.vm.$nextTick();
    });

    it('deve listar administradores ao montar', () => {
        expect(administradorService.listarAdministradores).toHaveBeenCalled();
        expect(wrapper.text()).toContain('Admin 1');
        expect(wrapper.text()).toContain('123');
    });

    it('deve abrir modal ao clicar em Adicionar', async () => {
        const btn = wrapper.find('button');
        await btn.trigger('click');
        expect(wrapper.vm.mostrarModalAdicionarAdmin).toBe(true);
    });

    it('deve chamar adicionarAdministrador ao confirmar no modal', async () => {
        wrapper.vm.mostrarModalAdicionarAdmin = true;
        wrapper.vm.novoAdminTitulo = '999';
        (administradorService.adicionarAdministrador as any).mockResolvedValue({});

        await wrapper.vm.adicionarAdmin();

        expect(administradorService.adicionarAdministrador).toHaveBeenCalledWith('999');
        expect(notificacoesStore.show).toHaveBeenCalledWith('Sucesso', expect.any(String), 'success');
    });

    it('deve chamar removerAdministrador ao confirmar exclusão', async () => {
        wrapper.vm.adminParaRemover = { tituloEleitoral: '123', nome: 'Admin 1' };
        (administradorService.removerAdministrador as any).mockResolvedValue({});

        await wrapper.vm.removerAdmin();

        expect(administradorService.removerAdministrador).toHaveBeenCalledWith('123');
        expect(notificacoesStore.show).toHaveBeenCalledWith('Sucesso', expect.any(String), 'success');
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
