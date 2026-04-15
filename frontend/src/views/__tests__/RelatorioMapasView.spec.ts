import {describe, expect, it, vi, beforeEach} from "vitest";
import {mount} from "@vue/test-utils";
import RelatorioMapasView from "@/views/RelatorioMapasView.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import * as painelService from "@/services/painelService";
import {useRelatoriosStore} from "@/stores/relatorios";
import {usePerfilStore} from "@/stores/perfil";

vi.mock("@/services/painelService", () => ({
  listarProcessos: vi.fn(),
}));

describe("RelatorioMapasView.vue", () => {
  const ctx = setupComponentTest();

  const stubs = {
    LayoutPadrao: { template: "<div><slot /></div>" },
    PageHeader: { template: "<div><slot name='actions' /></div>" },
    BCard: { template: "<div><slot /></div>" },
    BRow: { template: "<div><slot /></div>" },
    BCol: { template: "<div><slot /></div>" },
    BFormGroup: { template: "<div><label><slot /></label></div>" },
    BFormSelect: {
      props: ["modelValue", "options"],
      template: "<select :value='modelValue' @change='$emit(\"update:modelValue\", Number($event.target.value) || null)'><option v-for='o in options' :key='o.value' :value='o.value'>{{o.text}}</option></select>"
    },
    BButton: {
        props: ["disabled"],
        template: "<button :disabled='disabled' @click='$emit(\"click\")'><slot /></button>"
    },
    BSpinner: { template: "<span>loading...</span>" },
    EmptyState: { template: "<div>Empty State</div>" },
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(painelService.listarProcessos).mockResolvedValue({
      content: [{ codigo: 1, descricao: "Processo 1" }],
      totalElements: 1,
    } as any);
  });

  it("deve carregar processos ao montar", async () => {
    ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
    await ctx.wrapper.vm.$nextTick();

    expect(painelService.listarProcessos).toHaveBeenCalled();
  });

  it("deve mostrar empty state quando nenhum processo está selecionado", async () => {
    ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));

    expect(ctx.wrapper.text()).toContain("Empty State");
  });

  it("deve chamar exportarMapasPdf ao clicar no botão gerar", async () => {
    ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
    await ctx.wrapper.vm.$nextTick();

    const relatoriosStore = useRelatoriosStore();
    const exportarSpy = vi.spyOn(relatoriosStore, "exportarMapasPdf").mockResolvedValue(undefined as any);

    const btn = ctx.wrapper.find("[data-testid='btn-gerar-mapas']");
    expect((btn.element as HTMLButtonElement).disabled).toBe(true);

    const vm = ctx.wrapper.vm as any;
    vm.processoIdSelecionado = 1;
    await ctx.wrapper.vm.$nextTick();

    expect((btn.element as HTMLButtonElement).disabled).toBe(false);

    await btn.trigger("click");

    expect(exportarSpy).toHaveBeenCalledWith(1, undefined);
  });

  it("deve incluir unidadeId na exportação se selecionada", async () => {
    const perfilStore = usePerfilStore();
    perfilStore.unidadeSelecionada = 99;
    perfilStore.unidadeSelecionadaSigla = "SIGLA";

    ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
    await ctx.wrapper.vm.$nextTick();

    const relatoriosStore = useRelatoriosStore();
    const exportarSpy = vi.spyOn(relatoriosStore, "exportarMapasPdf").mockResolvedValue(undefined as any);

    const vm = ctx.wrapper.vm as any;
    vm.processoIdSelecionado = 1;
    vm.unidadeIdSelecionada = 99;
    await ctx.wrapper.vm.$nextTick();

    await ctx.wrapper.find("[data-testid='btn-gerar-mapas']").trigger("click");

    expect(exportarSpy).toHaveBeenCalledWith(1, 99);
  });
});
