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
                dataFinalizacaoFormatada: "01/01/2023",
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
                        stubActions: true,
                    }),
                ],
            },
        });

        await flushPromises();

        expect(processoService.buscarProcessosFinalizados).toHaveBeenCalled();

        const row = wrapper.find("tbody tr.row-processo-1");
        expect(row.exists()).toBe(true);
        expect(row.text()).toContain("Processo teste");

        // Check accessibility attributes (expecting failure)
        expect(row.attributes("tabindex")).toBe("0");

        // Simulate keyboard interaction
        await row.trigger("keydown.enter");
        expect(pushSpy).toHaveBeenCalledWith("/processo/1");

        await row.trigger("keydown.space");
        expect(pushSpy).toHaveBeenCalledWith("/processo/1");
    });
});
