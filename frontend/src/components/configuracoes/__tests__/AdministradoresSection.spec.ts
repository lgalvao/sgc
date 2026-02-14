import {describe, expect, it, vi, beforeEach} from "vitest";
import {mount, flushPromises} from "@vue/test-utils";
import AdministradoresSection from "../AdministradoresSection.vue";
import * as administradorService from "@/services/administradorService";
import {createTestingPinia} from "@pinia/testing";
import {useNotificacoesStore} from "@/stores/feedback";

vi.mock("@/services/administradorService", () => ({
    listarAdministradores: vi.fn(),
    adicionarAdministrador: vi.fn(),
    removerAdministrador: vi.fn()
}));

describe("AdministradoresSection.vue", () => {
    const mockAdmins = [
        { nome: "Admin 1", tituloEleitoral: "111", matricula: "M1", unidadeSigla: "U1", unidadeCodigo: 1 },
        { nome: "Admin 2", tituloEleitoral: "222", matricula: "M2", unidadeSigla: "U2", unidadeCodigo: 2 }
    ];

    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(administradorService.listarAdministradores).mockResolvedValue(mockAdmins);
    });

    const createWrapper = () => {
        return mount(AdministradoresSection, {
            global: {
                plugins: [createTestingPinia({ createSpy: vi.fn })],
                stubs: {
                    BButton: { template: '<button><slot /></button>' },
                    BAlert: { template: '<div><slot /></div>' },
                    EmptyState: true,
                    ModalConfirmacao: {
                        props: ['modelValue', 'loading'],
                        template: '<div v-if="modelValue"><slot /><button class="confirm" @click="$emit(\'confirmar\')">OK</button></div>',
                        emits: ['confirmar']
                    },
                    LoadingButton: {
                        props: ['loading', 'text'],
                        template: '<button @click="$emit(\'click\')">{{ text }}<slot /></button>',
                        emits: ['click']
                    }
                }
            }
        });
    };

    it("deve carregar e exibir administradores", async () => {
        const wrapper = createWrapper();
        await flushPromises();

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

    it("deve adicionar administrador", async () => {
        const wrapper = createWrapper();
        const notificacoes = useNotificacoesStore();
        await flushPromises();

        const addButton = wrapper.findAll('button').find(b => b.text().includes('Adicionar administrador'));
        await addButton?.trigger("click"); // Abrir modal
        
        const input = wrapper.find('input#usuarioTitulo');
        await input.setValue("333");

        vi.mocked(administradorService.adicionarAdministrador).mockResolvedValue({} as any);
        
        await wrapper.find('button.confirm').trigger("click");
        await flushPromises();

        expect(administradorService.adicionarAdministrador).toHaveBeenCalledWith("333");
        expect(notificacoes.show).toHaveBeenCalledWith("Sucesso", expect.anything(), "success");
    });

    it("deve remover administrador", async () => {
        const wrapper = createWrapper();
        const notificacoes = useNotificacoesStore();
        await flushPromises();

        const removeBtns = wrapper.findAll('button').filter(b => b.text().includes('Remover'));
        await removeBtns[0].trigger("click"); // Confirmar remoção
        
        vi.mocked(administradorService.removerAdministrador).mockResolvedValue({} as any);

        await wrapper.find('button.confirm').trigger("click");
        await flushPromises();

        expect(administradorService.removerAdministrador).toHaveBeenCalledWith("111");
        expect(notificacoes.show).toHaveBeenCalledWith("Sucesso", expect.anything(), "success");
    });

    it("deve tratar erro ao adicionar", async () => {
        const wrapper = createWrapper();
        const notificacoes = useNotificacoesStore();
        await flushPromises();

        // Trigger confirm without setting value
        await (wrapper.vm as any).adicionarAdmin();
        expect(notificacoes.show).toHaveBeenCalledWith("Erro", expect.stringContaining("válido"), "warning");

        (wrapper.vm as any).novoAdminTitulo = "333";

        vi.mocked(administradorService.adicionarAdministrador).mockRejectedValue(new Error("Erro API"));
        
        await (wrapper.vm as any).adicionarAdmin();
        await flushPromises();

        expect(notificacoes.show).toHaveBeenCalledWith("Erro", "Erro API", "danger");
    });

    it("deve tratar erro ao remover", async () => {
        const wrapper = createWrapper();
        const notificacoes = useNotificacoesStore();
        await flushPromises();

        const node = { nome: "Admin 1", tituloEleitoral: "111" };
        (wrapper.vm as any).adminParaRemover = node;
        
        vi.mocked(administradorService.removerAdministrador).mockRejectedValue(new Error("Erro Remover"));

        await (wrapper.vm as any).removerAdmin();
        await flushPromises();

        expect(notificacoes.show).toHaveBeenCalledWith("Erro", "Erro Remover", "danger");
    });

    it("removerAdmin não faz nada se não houver admin selecionado", async () => {
        const wrapper = createWrapper();
        await (wrapper.vm as any).removerAdmin();
        expect(administradorService.removerAdministrador).not.toHaveBeenCalled();
    });
});
