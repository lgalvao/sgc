import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import UnidadeView from "../UnidadeView.vue";
import {useUnidadeStore} from "@/stores/unidade";
import {relatoriosService} from "@/services/relatoriosService";
import {createMemoryHistory, createRouter} from "vue-router";

vi.mock("@/services/relatoriosService", () => ({
  relatoriosService: {
    downloadRelatorioMapaVigenteUnidadePdf: vi.fn(),
    downloadRelatorioMapaVigenteUnidadeCsv: vi.fn()
  }
}));

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{path: "/", component: {}}]
});

describe("UnidadeView Coverage", () => {
  it("exportarMapaVigentePdf lida com erro", async () => {
    const pinia = createTestingPinia({ stubActions: false });
    const unidadeStore = useUnidadeStore();
    unidadeStore.cacheUnidades.set(1, { codigo: 1, sigla: "U", nome: "Unidade", titular: {} } as any);
    unidadeStore.cacheMapasVigentes.set(1, { codigo: 100 } as any);

    vi.mocked(relatoriosService.downloadRelatorioMapaVigenteUnidadePdf).mockRejectedValue(new Error("Erro PDF"));

    const wrapper = mount(UnidadeView, {
      props: { codUnidade: 1 },
      global: {
        plugins: [pinia, router],
        stubs: {
          LayoutPadrao: { template: "<div><slot/></div>" },
          PageHeader: { template: "<div><slot name='actions'/></div>" },
          BCard: true,
          BCardBody: true,
          UnidadeContatoInfo: true,
          EmptyState: true,
          TreeTable: true,
          BDropdown: true,
          BDropdownItemButton: true
        }
      }
    });

    await flushPromises();
    (wrapper.vm as any).mapaVigente = { codigo: 100 };
    
    await (wrapper.vm as any).exportarMapaVigentePdf();
    expect(relatoriosService.downloadRelatorioMapaVigenteUnidadePdf).toHaveBeenCalledWith(1);
    expect((wrapper.vm as any).loadingExportacaoPdf).toBe(false);
  });

  it("calcula descricaoResponsabilidade corretamente", async () => {
      const pinia = createTestingPinia();
      const wrapper = mount(UnidadeView, {
          props: { codUnidade: 1 },
          global: { plugins: [pinia, router], stubs: { LayoutPadrao: true } }
      });

      const vm = wrapper.vm as any;
      vm.unidade = { tipoResponsabilidade: "SUBSTITUTO", dataFimResponsabilidade: "2026-12-31" };
      expect(vm.descricaoResponsabilidade).toContain("Substituição");
      expect(vm.descricaoResponsabilidade).toContain("31/12/2026");

      vm.unidade = { tipoResponsabilidade: "ATRIBUICAO_TEMPORARIA", dataFimResponsabilidade: null };
      expect(vm.descricaoResponsabilidade).toBe("Atrib. temporária");
  });

  it("calcula titularExibivel e labelContatoPrincipal", async () => {
      const pinia = createTestingPinia();
      const wrapper = mount(UnidadeView, {
          props: { codUnidade: 1 },
          global: { plugins: [pinia, router], stubs: { LayoutPadrao: true } }
      });

      const vm = wrapper.vm as any;
      vm.unidade = { 
          titular: { tituloEleitoral: "123" },
          responsavel: { tituloEleitoral: "456" }
      };
      
      expect(vm.responsavelEhTitular).toBe(false);
      expect(vm.titularExibivel).toBe(true);
      expect(vm.labelContatoPrincipal).toContain("Responsável");
      
      vm.unidade.responsavel.tituloEleitoral = "123";
      expect(vm.responsavelEhTitular).toBe(true);
      expect(vm.titularExibivel).toBe(false);
      expect(vm.labelContatoPrincipal).toContain("Titular");
  });
});
