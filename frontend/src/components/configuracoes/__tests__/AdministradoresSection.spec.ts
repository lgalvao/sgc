import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import AdministradoresSection from '@/components/configuracoes/AdministradoresSection.vue';
import {useNotificacoesStore} from '@/stores/feedback';
import * as administradorService from '@/services/administradorService';
import {flushPromises} from '@vue/test-utils';

// Mock dependencies
vi.mock('@/services/administradorService', () => ({
    listarAdministradores: vi.fn(),
    adicionarAdministrador: vi.fn(),
    removerAdministrador: vi.fn(),
}));

describe('AdministradoresSection', () => {
    let wrapper: any;
    let notificacoesStore: any;

    const setupWrapper = (mockAdmins: any = []) => {
        const pinia = createTestingPinia({
            stubActions: false,
        });

        notificacoesStore = useNotificacoesStore(pinia);

        if (mockAdmins instanceof Error) {
            (administradorService.listarAdministradores as any).mockRejectedValue(mockAdmins);
        } else {
            (administradorService.listarAdministradores as any).mockResolvedValue(mockAdmins);
        }

        wrapper = mount(AdministradoresSection, {
            global: {
                plugins: [pinia],
                stubs: {
                    EmptyState: {
                        template: '<div class="empty-state-stub">Nenhum administrador</div>'
                    },
                    ModalConfirmacao: {
                        template: '<div class="modal-stub" v-if="modelValue"><slot /><button class="confirm-btn" @click="$emit(\'confirmar\')">OK</button></div>',
                        props: ['modelValue']
                    },
                    LoadingButton: {
                        template: '<button class="loading-btn" @click="$emit(\'click\')"><slot /></button>',
                        props: ['loading', 'variant', 'size', 'icon', 'text']
                    },
                    BAlert: {
                        template: '<div class="alert-stub"><slot /></div>'
                    }
                }
            }
        });
    };

    beforeEach(async () => {
        vi.clearAllMocks();
    });

    it('deve listar administradores ao montar', async () => {
        const mockAdmins = [
            { tituloEleitoral: '123', nome: 'Admin 1', matricula: 'M001', unidadeSigla: 'UN1' }
        ];
        setupWrapper(mockAdmins);
        await flushPromises();

        expect(administradorService.listarAdministradores).toHaveBeenCalled();
        expect(wrapper.text()).toContain('Admin 1');
        expect(wrapper.text()).toContain('123');
    });

    it('deve mostrar EmptyState quando não houver administradores', async () => {
        setupWrapper([]);
        await flushPromises();
        expect(wrapper.find('.empty-state-stub').exists()).toBe(true);
    });

    it('deve mostrar erro ao falhar carregamento', async () => {
        setupWrapper(new Error('Erro ao carregar'));
        await flushPromises();
        expect(wrapper.find('.alert-stub').text()).toContain('Erro ao carregar');
    });

    it('deve abrir modal ao clicar em Adicionar', async () => {
        setupWrapper([]);
        await flushPromises();
        // O botão de adicionar está no card-header, é um BButton variant="light"
        // Como não dei stub para BButton, ele renderiza como <button>
        const btn = wrapper.find('.card-header button');
        await btn.trigger('click');
        expect(wrapper.vm.mostrarModalAdicionarAdmin).toBe(true);
    });

    it('deve chamar adicionarAdministrador ao confirmar no modal', async () => {
        setupWrapper([]);
        await flushPromises();
        wrapper.vm.mostrarModalAdicionarAdmin = true;
        wrapper.vm.novoAdminTitulo = '999';
        (administradorService.adicionarAdministrador as any).mockResolvedValue({});

        await wrapper.vm.adicionarAdmin();

        expect(administradorService.adicionarAdministrador).toHaveBeenCalledWith('999');
        expect(notificacoesStore.show).toHaveBeenCalledWith('Sucesso', expect.any(String), 'success');
    });

    it('deve chamar confirmarRemocao ao clicar no botão de remover', async () => {
        const mockAdmins = [
            { tituloEleitoral: '123', nome: 'Admin 1', matricula: 'M001', unidadeSigla: 'UN1' }
        ];
        setupWrapper(mockAdmins);
        await flushPromises();

        const btnRemover = wrapper.find('.loading-btn');
        await btnRemover.trigger('click');

        expect(wrapper.vm.mostrarModalRemoverAdmin).toBe(true);
        expect(wrapper.vm.adminParaRemover).toEqual(mockAdmins[0]);
    });

    it('deve chamar removerAdministrador ao confirmar exclusão', async () => {
        setupWrapper([]);
        await flushPromises();
        wrapper.vm.adminParaRemover = { tituloEleitoral: '123', nome: 'Admin 1' };
        wrapper.vm.mostrarModalRemoverAdmin = true;
        (administradorService.removerAdministrador as any).mockResolvedValue({});

        await wrapper.vm.removerAdmin();

        expect(administradorService.removerAdministrador).toHaveBeenCalledWith('123');
        expect(notificacoesStore.show).toHaveBeenCalledWith('Sucesso', expect.any(String), 'success');
        expect(wrapper.vm.mostrarModalRemoverAdmin).toBe(false);
    });

    it('deve exibir alerta se tentar adicionar administrador com título vazio', async () => {
        setupWrapper([]);
        await flushPromises();
        wrapper.vm.novoAdminTitulo = '   ';
        await wrapper.vm.adicionarAdmin();

        expect(notificacoesStore.show).toHaveBeenCalledWith('Erro', 'Digite um título eleitoral válido', 'warning');
        expect(administradorService.adicionarAdministrador).not.toHaveBeenCalled();
    });

    it('deve exibir erro ao falhar adição de administrador', async () => {
        setupWrapper([]);
        await flushPromises();
        wrapper.vm.novoAdminTitulo = '123';
        (administradorService.adicionarAdministrador as any).mockRejectedValue(new Error('Falha ao adicionar'));

        await wrapper.vm.adicionarAdmin();

        expect(notificacoesStore.show).toHaveBeenCalledWith('Erro', 'Falha ao adicionar', 'danger');
    });

    it('deve exibir erro ao falhar remoção de administrador', async () => {
        setupWrapper([]);
        await flushPromises();
        wrapper.vm.adminParaRemover = { tituloEleitoral: '123', nome: 'Admin' };
        (administradorService.removerAdministrador as any).mockRejectedValue(new Error('Falha ao remover'));

        await wrapper.vm.removerAdmin();

        expect(notificacoesStore.show).toHaveBeenCalledWith('Erro', 'Falha ao remover', 'danger');
    });
});
