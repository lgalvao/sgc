import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import AdministradoresSection from '../AdministradoresSection.vue';
import {createTestingPinia} from '@pinia/testing';
import * as adminService from '@/services/administradorService';

vi.mock('@/services/administradorService', () => ({
    listarAdministradores: vi.fn(),
    adicionarAdministrador: vi.fn(),
    removerAdministrador: vi.fn(),
}));

describe('AdministradoresSection.vue Coverage', () => {
    const commonStubs = {
        EmptyState: { template: '<div />' },
        ModalConfirmacao: {
            template: '<div><slot /><button class="btn-confirmar" @click="$emit(\'confirmar\')">Confirm</button></div>',
            props: ['modelValue']
        },
        LoadingButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
        BAlert: { template: '<div><slot /></div>', props: ['modelValue'] },
        BButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('trata erro ao carregar administradores', async () => {
        vi.mocked(adminService.listarAdministradores).mockRejectedValue(new Error('Load Fail'));
        const pinia = createTestingPinia();

        const wrapper = mount(AdministradoresSection, {
            global: { plugins: [pinia], stubs: commonStubs }
        });

        await flushPromises();
        expect((wrapper.vm as any).erroAdmins).toBe('Load Fail');
    });

    it('trata erro ao adicionar administrador', async () => {
        vi.mocked(adminService.listarAdministradores).mockResolvedValue([]);
        const pinia = createTestingPinia();

        const wrapper = mount(AdministradoresSection, {
            global: { plugins: [pinia], stubs: commonStubs }
        });

        await flushPromises();

        (wrapper.vm as any).novoAdminTitulo = '12345';
        vi.mocked(adminService.adicionarAdministrador).mockRejectedValue(new Error('Add Fail'));

        await (wrapper.vm as any).adicionarAdmin();
        await flushPromises();

        expect((wrapper.vm as any).adicionandoAdmin).toBe(false);
    });

    it('adicionarAdmin retorna se título vazio', async () => {
        const pinia = createTestingPinia();
        const wrapper = mount(AdministradoresSection, {
            global: { plugins: [pinia], stubs: commonStubs }
        });

        (wrapper.vm as any).novoAdminTitulo = '  ';
        await (wrapper.vm as any).adicionarAdmin();

        expect(adminService.adicionarAdministrador).not.toHaveBeenCalled();
    });

    it('trata erro ao remover administrador', async () => {
        const mockAdmin = { tituloEleitoral: '1', nome: 'Admin', matricula: 'A1', unidadeSigla: 'U', unidadeCodigo: 1 };
        vi.mocked(adminService.listarAdministradores).mockResolvedValue([mockAdmin]);
        const pinia = createTestingPinia();

        const wrapper = mount(AdministradoresSection, {
            global: { plugins: [pinia], stubs: commonStubs }
        });

        await flushPromises();

        (wrapper.vm as any).adminParaRemover = mockAdmin;
        vi.mocked(adminService.removerAdministrador).mockRejectedValue(new Error('Rem Fail'));

        await (wrapper.vm as any).removerAdmin();
        await flushPromises();

        expect((wrapper.vm as any).removendoAdmin).toBeNull();
    });

    it('removerAdmin retorna se nenhum admin selecionado', async () => {
        const pinia = createTestingPinia();
        const wrapper = mount(AdministradoresSection, {
            global: { plugins: [pinia], stubs: commonStubs }
        });

        (wrapper.vm as any).adminParaRemover = null;
        await (wrapper.vm as any).removerAdmin();

        expect(adminService.removerAdministrador).not.toHaveBeenCalled();
    });

    it('adiciona administrador com sucesso', async () => {
        vi.mocked(adminService.listarAdministradores).mockResolvedValue([]);
        vi.mocked(adminService.adicionarAdministrador).mockResolvedValue({} as any);
        const pinia = createTestingPinia();

        const wrapper = mount(AdministradoresSection, {
            global: { plugins: [pinia], stubs: commonStubs }
        });

        (wrapper.vm as any).abrirModalAdicionarAdmin();
        (wrapper.vm as any).novoAdminTitulo = '12345';

        await (wrapper.vm as any).adicionarAdmin();
        await flushPromises();

        expect(adminService.adicionarAdministrador).toHaveBeenCalledWith('12345');
        expect((wrapper.vm as any).mostrarModalAdicionarAdmin).toBe(false);
    });

    it('confirma e remove administrador com sucesso', async () => {
        const mockAdmin = { tituloEleitoral: '1', nome: 'Admin' };
        vi.mocked(adminService.listarAdministradores).mockResolvedValue([mockAdmin] as any);
        vi.mocked(adminService.removerAdministrador).mockResolvedValue({} as any);
        const pinia = createTestingPinia();

        const wrapper = mount(AdministradoresSection, {
            global: { plugins: [pinia], stubs: commonStubs }
        });

        await flushPromises();

        await (wrapper.vm as any).confirmarRemocao(mockAdmin);
        expect((wrapper.vm as any).mostrarModalRemoverAdmin).toBe(true);
        expect((wrapper.vm as any).adminParaRemover).toEqual(mockAdmin);

        await (wrapper.vm as any).removerAdmin();
        await flushPromises();

        expect(adminService.removerAdministrador).toHaveBeenCalledWith('1');
        expect((wrapper.vm as any).mostrarModalRemoverAdmin).toBe(false);
    });

    it('modal close handlers', async () => {
        const pinia = createTestingPinia();
        const wrapper = mount(AdministradoresSection, {
            global: { plugins: [pinia], stubs: commonStubs }
        });

        (wrapper.vm as any).fecharModalAdicionarAdmin();
        expect((wrapper.vm as any).mostrarModalAdicionarAdmin).toBe(false);
    });
});
