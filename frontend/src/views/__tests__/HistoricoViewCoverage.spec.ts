import { mount, flushPromises } from "@vue/test-utils";
import { describe, it, expect, vi } from "vitest";
import { createTestingPinia } from "@pinia/testing";
import HistoricoView from "@/views/HistoricoView.vue";
import { useProcessosStore } from "@/stores/processos";
import { createRouter, createMemoryHistory } from "vue-router";
import { TipoProcesso, SituacaoProcesso } from "@/types/tipos";

describe("HistoricoView Coverage", () => {
  it("renders correctly and rows are accessible", async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/', component: { template: '<div>Home</div>' } },
        { path: '/processo/:id', component: { template: '<div>Processo</div>' } }
      ],
    });

    // We mock push but we need the router to be functional for initial render
    const pushSpy = vi.spyOn(router, "push");

    const wrapper = mount(HistoricoView, {
      global: {
        plugins: [
          router,
          createTestingPinia({
            createSpy: vi.fn,
            stubActions: true,
            initialState: {
              "processos": {
                processosFinalizados: [
                  {
                    codigo: 1,
                    descricao: "Processo Teste",
                    tipo: TipoProcesso.MAPEAMENTO,
                    tipoLabel: "Mapeamento",
                    dataFinalizacaoFormatada: "01/01/2023",
                    situacao: SituacaoProcesso.FINALIZADO,
                  },
                ],
              },
            },
          }),
        ],
      },
    });

    await flushPromises();

    const store = useProcessosStore();
    expect(store.processosFinalizados).toHaveLength(1);

    // Find the row
    const row = wrapper.find("tbody tr.cursor-pointer");
    expect(row.exists()).toBe(true);
    expect(row.text()).toContain("Processo Teste");

    // Check accessibility attributes (expecting failure)
    expect(row.attributes("tabindex")).toBe("0");

    // Simulate keyboard interaction
    await row.trigger("keydown.enter");
    expect(pushSpy).toHaveBeenCalledWith("/processo/1");

    await row.trigger("keydown.space");
    expect(pushSpy).toHaveBeenCalledWith("/processo/1");
  });
});
