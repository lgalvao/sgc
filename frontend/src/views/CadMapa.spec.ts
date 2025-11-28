import {createTestingPinia} from "@pinia/testing";
import {mount} from "@vue/test-utils";
import {BButton, BFormCheckbox, BFormInput, BFormTextarea, BModal, vBTooltip,} from "bootstrap-vue-next";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {useRoute} from "vue-router";
import {useMapasStore} from "@/stores/mapas";
import CadMapa from "./CadMapa.vue";
import {useAtividadesStore} from "@/stores/atividades";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useUnidadesStore} from "@/stores/unidades";

vi.mock("vue-router", () => ({
    useRoute: vi.fn(),
    useRouter: vi.fn(() => ({
        push: vi.fn(),
    })),
    createRouter: vi.fn(() => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
    })),
    createWebHistory: vi.fn(),
}));

describe("CadMapa.vue", () => {
    beforeEach(() => {
        vi.mocked(useRoute).mockReturnValue({
            params: {
                codProcesso: "1",
                siglaUnidade: "TEST",
            },
        } as any);
    });

    const globalComponents = {
        global: {
            plugins: [createTestingPinia()],
            components: {
                BFormTextarea,
                BFormCheckbox,
                BFormInput,
                BModal,
                BButton,
            },
            directives: {
                "b-tooltip": vBTooltip,
            },
        },
    };

    it("renders the component", () => {
        const wrapper = mount(CadMapa, globalComponents);
        expect(wrapper.exists()).toBe(true);
    });

    it("opens the create competency modal when the button is clicked", async () => {
        const wrapper = mount(CadMapa, globalComponents);

        const unidadesStore = useUnidadesStore();
        unidadesStore.unidade = {
            sigla: "TEST",
            nome: "Test Unit",
            codigo: 1,
            filhas: [],
        } as any;

        const subprocessosStore = useSubprocessosStore();
        subprocessosStore.buscarSubprocessoPorProcessoEUnidade = vi
            .fn()
            .mockResolvedValue(10);
        subprocessosStore.buscarSubprocessoDetalhe = vi.fn().mockResolvedValue({});

        const atividadesStore = useAtividadesStore();
        atividadesStore.buscarAtividadesParaSubprocesso = vi
            .fn()
            .mockResolvedValue({});

        await wrapper.vm.$nextTick();
        // Wait for async onMounted
        await new Promise((resolve) => setTimeout(resolve, 10));
        await wrapper.vm.$nextTick();

        await wrapper
            .find('[data-testid="btn-abrir-criar-competencia"]')
            .trigger("click");

        expect(
            wrapper.find('[data-testid="criar-competencia-modal"]').exists(),
        ).toBe(true);
    });

    it("calls the disponibilizarMapa action when the disponibilizar button is clicked", async () => {
        const wrapper = mount(CadMapa, {
            global: {
                plugins: [
                    createTestingPinia({
                        initialState: {
                            mapas: {
                                mapaCompleto: {
                                    competencias: [
                                        {
                                            codigo: 1,
                                            descricao: "Competency 1",
                                            atividadesAssociadas: [1],
                                        },
                                    ],
                                },
                            },
                        },
                    }),
                ],
                components: {
                    BFormTextarea,
                    BFormCheckbox,
                    BFormInput,
                    BModal,
                    BButton,
                },
                directives: {
                    "b-tooltip": vBTooltip,
                },
            },
        });

        const unidadesStore = useUnidadesStore();
        unidadesStore.unidade = {
            sigla: "TEST",
            nome: "Test Unit",
            codigo: 1,
            filhas: [],
        } as any;

        const mapasStore = useMapasStore();
        const subprocessosStore = useSubprocessosStore();
        subprocessosStore.buscarSubprocessoPorProcessoEUnidade = vi
            .fn()
            .mockResolvedValue(10);
        subprocessosStore.buscarSubprocessoDetalhe = vi.fn().mockResolvedValue({});

        const atividadesStore = useAtividadesStore();
        atividadesStore.buscarAtividadesParaSubprocesso = vi
            .fn()
            .mockResolvedValue({});

        await wrapper.vm.$nextTick();
        await new Promise((resolve) => setTimeout(resolve, 10));
        await wrapper.vm.$nextTick();

        await wrapper
            .find('[data-testid="btn-disponibilizar-page"]')
            .trigger("click");
        await wrapper
            .find('[data-testid="input-data-limite"]')
            .setValue("2025-12-31");
        await wrapper.find('[data-testid="btn-disponibilizar"]').trigger("click");

        expect(mapasStore.disponibilizarMapa).toHaveBeenCalled();
    });
});
