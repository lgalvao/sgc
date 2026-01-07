import {beforeEach, describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import HistoricoView from "@/views/HistoricoView.vue";
import {createTestingPinia} from "@pinia/testing";
import {useRouter} from "vue-router";
import {apiClient} from "@/axios-setup";

// Mock router
vi.mock("vue-router", () => ({
    useRouter: vi.fn(),
    createRouter: vi.fn(),
    createWebHistory: vi.fn(),
}));

// Mock API client
vi.mock("@/axios-setup", () => ({
    apiClient: {
        get: vi.fn(),
    },
}));

// Setup components mocks to avoid rendering issues
// BContainer, BRow, BCol, BCard, BButton are from bootstrap-vue-next
// We should stub them or let mount handle them if they are simple.
// Given the errors were "Cannot call vm on an empty VueWrapper", it usually means
// we tried to find a component that wasn't rendered or wasn't found.
// The previous test code was trying to find 'TabelaProcessos', but the View code uses a manual table inside BCard.
// So the test was completely out of sync with the implementation.

describe("HistoricoView.vue", () => {
    let routerPushMock: any;
    const mockProcessos = [
        {
            codigo: 1,
            descricao: "Proc B",
            tipo: "MAPEAMENTO",
            dataFinalizacao: "2023-01-02T10:00:00",
        },
        {
            codigo: 2,
            descricao: "Proc A",
            tipo: "REVISAO",
            dataFinalizacao: "2023-01-01T10:00:00",
        }
    ];

    beforeEach(() => {
        vi.clearAllMocks();
        routerPushMock = vi.fn();
        (useRouter as any).mockReturnValue({
            push: routerPushMock,
        });
        (apiClient.get as any).mockResolvedValue({ data: mockProcessos });
    });

    const mountOptions = () => ({
        global: {
            plugins: [createTestingPinia({ createSpy: vi.fn })],
            stubs: {
                // Stub bootstrap components if necessary, but shallowMount or mount should work.
                // Since we are using mount, and these are external libs, they might fail if not set up.
                // For now, let's assume they work or we stub them if needed.
                BContainer: { template: '<div><slot /></div>' },
                BRow: { template: '<div><slot /></div>' },
                BCol: { template: '<div><slot /></div>' },
                BCard: { template: '<div><slot /></div>' },
                BButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' }
            }
        },
    });

    it("deve carregar processos finalizados ao montar", async () => {
        mount(HistoricoView, mountOptions());
        expect(apiClient.get).toHaveBeenCalledWith('/processos/finalizados');
    });

    it("deve renderizar tabela com processos", async () => {
        const wrapper = mount(HistoricoView, mountOptions());

        // Wait for async mount
        await new Promise(resolve => setTimeout(resolve, 0));

        // The view does not use TabelaProcessos component, it renders a table manually.
        const rows = wrapper.findAll('tbody tr');
        // If loaded, we expect rows for processes.
        // Note: wrapper updates might need await flushPromises from test-utils,
        // but setTimeout often works for simple promise resolution.

        // We have 2 processes mocked.
        expect(rows.length).toBe(2);
        expect(rows[0].text()).toContain("Proc B");
        expect(rows[1].text()).toContain("Proc A");
    });

    it("deve exibir mensagem se não houver processos", async () => {
        (apiClient.get as any).mockResolvedValue({ data: [] });
        const wrapper = mount(HistoricoView, mountOptions());

        await new Promise(resolve => setTimeout(resolve, 0));

        const text = wrapper.text();
        expect(text).toContain("Nenhum processo finalizado encontrado");
    });

    it("deve navegar para detalhes ao clicar", async () => {
        const wrapper = mount(HistoricoView, mountOptions());
        await new Promise(resolve => setTimeout(resolve, 0));

        const buttons = wrapper.findAll('button');
        // First row button
        await buttons[0].trigger('click');

        expect(routerPushMock).toHaveBeenCalledWith('/processos/1');
    });
});
