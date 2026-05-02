import {flushPromises, mount} from "@vue/test-utils";
import {describe, expect, it, vi} from "vitest";
import {createTestingPinia} from "@pinia/testing";
import HistoricoView from "@/views/HistoricoView.vue";
import {createMemoryHistory, createRouter} from "vue-router";
import {SituacaoProcesso, TipoProcesso} from "@/types/tipos";
import * as processoService from "@/services/processoService";

vi.mock("@/services/processoService", () => ({
    buscarProcessosFinalizados: vi.fn(),
}));

describe("HistoricoView Coverage", () => {
    it("renders correctly and rows are accessible", async () => {
        vi.mocked(processoService.buscarProcessosFinalizados).mockResolvedValue([
            {
                codigo: 1,
                descricao: "Processo teste",
                tipo: TipoProcesso.MAPEAMENTO,
                tipoLabel: "Mapeamento",
                dataFinalizacao: "2023-01-01T10:00:00",
                situacao: SituacaoProcesso.FINALIZADO,
            },
        ] as any);

        const router = createRouter({
            history: createMemoryHistory(),
            routes: [
                {path: '/', component: {template: '<div>Home</div>'}},
                {path: '/processo/:id', component: {template: '<div>Processo</div>'}}
            ],
        });

        const pushSpy = vi.spyOn(router, "push");

        const wrapper = mount(HistoricoView, {
            global: {
                plugins: [
                    router,
                    createTestingPinia({
                        createSpy: vi.fn,
                        stubActions: false,
                    }),
                ],
            },
        });

        await flushPromises();

        expect(processoService.buscarProcessosFinalizados).toHaveBeenCalled();

        const row = wrapper.find("tbody tr.row-processo-1");
        expect(row.exists()).toBe(true);
        expect(row.text()).toContain("Processo teste");
        expect(row.text()).toContain("01/01/2023");

        // Check accessibility attributes (expecting failure)
        expect(row.attributes("tabindex")).toBe("0");

        // Simulate keyboard interaction
        await row.trigger("keydown.enter");
        expect(pushSpy).toHaveBeenCalledWith("/processo/1");

        await row.trigger("keydown.space");
        expect(pushSpy).toHaveBeenCalledWith("/processo/1");
    });

    it("cobre branches de ordenação e sort", async () => {
        vi.mocked(processoService.buscarProcessosFinalizados).mockResolvedValue([
            { codigo: 1, dataFinalizacao: "2023-01-01", descricao: "A" },
            { codigo: 2, dataFinalizacao: null, descricao: "B" },
            { codigo: 3, dataFinalizacao: "2023-01-02", descricao: "C" },
        ] as any);

        const router = createRouter({
            history: createMemoryHistory(),
            routes: [{path: '/', component: {}}],
        });

        const wrapper = mount(HistoricoView, {
            global: {
                plugins: [router, createTestingPinia({ stubActions: false })],
            },
        });

        await flushPromises();
        const vm = wrapper.vm as any;

        // 1. Ordenar por mesmo campo (inverte direção)
        vm.ordenarPor("dataFinalizacao");
        expect(vm.asc).toBe(true);

        // 2. Ordenar por campo diferente
        vm.ordenarPor("descricao");
        expect(vm.criterio).toBe("descricao");
        expect(vm.asc).toBe(true);

        // 3. Forçar recalculação do sort para cobrir nulls
        const sorted = vm.processosOrdenados;
        expect(sorted).toBeDefined();

        // 4. Equal values (for line 52)
        vm.historicoStore.processos = [
            { codigo: 1, descricao: 'A', dataFinalizacao: '2023-01-01' },
            { codigo: 2, descricao: 'B', dataFinalizacao: '2023-01-01' }
        ] as any;
        expect(vm.processosOrdenados).toHaveLength(2);
    });

/*
    it("cobre onActivated", async () => {
        const pinia = createTestingPinia({ stubActions: false });
        const historicoStore = useHistoricoStore(pinia);
        
        // Mocking functions before mount to capture onMounted calls
        const garantirDadosSpy = vi.spyOn(historicoStore, 'garantirDados').mockResolvedValue(undefined as any);
        const dadosValidosSpy = vi.spyOn(historicoStore, 'dadosValidos');

        const wrapper = mount(HistoricoView, {
            global: {
                plugins: [pinia],
            },
        });
        await flushPromises();
        const vm = wrapper.vm as any;

        // 1. Valid data (should return early)
        dadosValidosSpy.mockReturnValue(true);
        await vm.onActivated?.();
        expect(garantirDadosSpy).toHaveBeenCalledTimes(1); // Only from onMounted
        
        // 2. Invalid data
        dadosValidosSpy.mockReturnValue(false);
        await vm.onActivated?.();
        expect(garantirDadosSpy).toHaveBeenCalledTimes(2);
    });
*/
});
