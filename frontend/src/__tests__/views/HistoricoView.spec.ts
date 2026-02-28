import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import HistoricoView from "@/views/HistoricoView.vue";
import {createTestingPinia} from "@pinia/testing";
import {setupComponentTest} from "@/test-utils/componentTestHelpers";

// Mock router
const {mockPush} = vi.hoisted(() => {
    return {
        mockPush: vi.fn(),
    }
});

vi.mock("vue-router", () => ({
    useRouter: () => ({push: mockPush}),
    createRouter: vi.fn(() => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
        push: mockPush,
        replace: vi.fn(),
        resolve: vi.fn(),
        currentRoute: {value: {}},
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

// Setup components mocks to avoid rendering issues
// BContainer, BRow, BCol, BCard, BButton are from bootstrap-vue-next
// Given the errors were "Cannot call vm on an empty VueWrapper", it usually means
// we tried to find a component that wasn't rendered or wasn't found.
// The previous test code was trying to find 'TabelaProcessos', but the View code uses a manual table inside BCard.
// So the test was completely out of sync with the implementation.

describe("HistoricoView.vue", () => {
    const context = setupComponentTest();
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
    });

    const mountOptions = () => {
        return {
            global: {
                plugins: [
                    createTestingPinia({
                        createSpy: vi.fn,
                        initialState: {
                            processos: {
                                processosFinalizados: mockProcessos,
                            }
                        }
                    }),
                ],
                stubs: {
                    RouterLink: true,
                    RouterView: true,
                },
            },
        };
    };

    it("deve carregar processos finalizados ao montar", async () => {
        context.wrapper = mount(HistoricoView, mountOptions());
        await flushPromises();

        // The component uses the store, so the data should be rendered from initial state
        expect(context.wrapper.findAll('tbody tr').length).toBe(2);
    });

    it("deve renderizar tabela com processos", async () => {
        context.wrapper = mount(HistoricoView, mountOptions());
        await flushPromises();

        const rows = context.wrapper.findAll('tbody tr');
        // We have 2 processes mocked in initial state.
        expect(rows.length).toBe(2);
        expect(rows[0].text()).toContain("Proc B");
        expect(rows[1].text()).toContain("Proc A");
    });

    it("deve exibir mensagem se não houver processos", async () => {
        const emptyOptions = {
            global: {
                plugins: [
                    createTestingPinia({
                        createSpy: vi.fn,
                        initialState: {
                            processos: {
                                processosFinalizados: [],
                            }
                        }
                    }),
                ],
                stubs: {
                    RouterLink: true,
                    RouterView: true,
                },
            },
        };

        context.wrapper = mount(HistoricoView, emptyOptions);
        await flushPromises();

        const text = context.wrapper.text();
        expect(text).toContain("Nenhum processo finalizado");
    });

    it("deve navegar para detalhes ao clicar", async () => {
        context.wrapper = mount(HistoricoView, mountOptions());
        await flushPromises();

        const rows = context.wrapper.findAll('tbody tr');
        expect(rows.length).toBeGreaterThan(0);
        await rows[0].trigger('click');

        expect(mockPush).toHaveBeenCalledWith('/processo/1');
    });
});
