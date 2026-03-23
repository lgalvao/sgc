import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import AdministradoresView from "@/views/AdministradoresView.vue";
import * as administradorService from "@/services/administradorService";
import {createTestingPinia} from "@pinia/testing";

vi.mock("@/services/administradorService", () => ({
    listarAdministradores: vi.fn(),
    adicionarAdministrador: vi.fn(),
    removerAdministrador: vi.fn()
}));

describe("AdministradoresView.vue", () => {
    const mockAdmins = [
        {nome: "Admin 1", tituloEleitoral: "111", matricula: "M1", unidadeSigla: "U1", unidadeCodigo: 1},
        {nome: "Admin 2", tituloEleitoral: "222", matricula: "M2", unidadeSigla: "U2", unidadeCodigo: 2}
    ];

    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(administradorService.listarAdministradores).mockResolvedValue(mockAdmins);
    });

    const createWrapper = () => {
        return mount(AdministradoresView, {
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    PageHeader: {
                        props: ['title'],
                        template: '<div><h1>{{ title }}</h1><slot name="actions" /></div>'
                    },
                    BButton: {template: `<button @click="$emit('click')"><slot /></button>`},
                    BAlert: {template: '<div><slot /></div>'},
                    EmptyState: true,
                    ModalConfirmacao: {
                        name: 'ModalConfirmacao',
                        props: ['modelValue', 'loading'],
                        template: `<div v-if="modelValue"><slot /><button class="confirm" @click="$emit('confirmar')">OK</button></div>`,
                        emits: ['confirmar', 'shown', 'update:modelValue']
                    },
                    LoadingButton: {
                        props: ['loading', 'text'],
                        template: `<button @click="$emit('click')">{{ text }}<slot /></button>`,
                        emits: ['click']
                    }
                }
            }
        });
    };

    it("deve carregar e exibir administradores", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        expect(wrapper.find('h1').text()).toBe('Administradores');
        expect(administradorService.listarAdministradores).toHaveBeenCalled();
        expect(wrapper.text()).toContain("Admin 1");
        expect(wrapper.text()).toContain("Admin 2");
    });

    it("deve tratar erro ao carregar", async () => {
        vi.mocked(administradorService.listarAdministradores).mockRejectedValue(new Error("Erro ao carregar"));
        const wrapper = createWrapper();
        await flushPromises();

        expect(wrapper.text()).toContain("Erro ao carregar");
    });

    it("deve abrir modal e adicionar administrador", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        const addButton = wrapper.find('button[data-testid="btn-abrir-modal-add-admin"]');
        await addButton.trigger("click");

        const input = wrapper.find('input#usuarioTitulo');
        await input.setValue("333");

        vi.mocked(administradorService.adicionarAdministrador).mockResolvedValue({} as any);

        await wrapper.find('button.confirm').trigger("click");
        await flushPromises();

        expect(administradorService.adicionarAdministrador).toHaveBeenCalledWith("333");
    });

    it("deve remover administrador", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        const removeBtn = wrapper.findAll('button').find(b => b.text().includes('Remover'));
        await removeBtn?.trigger("click");

        vi.mocked(administradorService.removerAdministrador).mockResolvedValue({} as any);

        await wrapper.find('button.confirm').trigger("click");
        await flushPromises();

        expect(administradorService.removerAdministrador).toHaveBeenCalledWith("111");
    });

    it("deve gerenciar estados de modais e fluxos de erro na administração de usuários", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;

        // Atualização de v-model nos modais
        const modals = wrapper.findAllComponents({name: 'ModalConfirmacao'});
        for (const modal of modals) {
            await modal.vm.$emit('update:modelValue', true);
        }
        expect(vm.mostrarModalAdicionarAdmin).toBe(true);
        expect(vm.mostrarModalRemoverAdmin).toBe(true);
        
        // Disparo de evento shown do modal
        const modalAdd = wrapper.findComponent({name: 'ModalConfirmacao'});
        await modalAdd.vm.$emit('shown');

        // Tentativa de adicionar administrador com título inválido
        vm.novoAdminTitulo = "";
        await vm.adicionarAdmin();
        
        // Tratamento de erro na adição de administrador
        vm.novoAdminTitulo = "123";
        vi.mocked(administradorService.adicionarAdministrador).mockRejectedValue(new Error("Erro add"));
        await vm.adicionarAdmin();

        // Tentativa de remover administrador sem seleção
        vm.adminParaRemover = null;
        await vm.removerAdmin();

        // Tratamento de erro na remoção de administrador
        vm.adminParaRemover = {tituloEleitoral: "111", nome: "Admin"};
        vi.mocked(administradorService.removerAdministrador).mockRejectedValue(new Error("Erro rem"));
        await vm.removerAdmin();
    });
});
