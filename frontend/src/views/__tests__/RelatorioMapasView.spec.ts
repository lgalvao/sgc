import {beforeEach, describe, expect, it, vi} from "vitest";
import {mount} from "@vue/test-utils";
import RelatorioMapasView from "@/views/RelatorioMapasView.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import * as painelService from "@/services/painelService";
import * as processoService from "@/services/processoService";
import {useRelatoriosStore} from "@/stores/relatorios";

vi.mock("@/services/painelService", () => ({
  listarProcessos: vi.fn(),
}));

vi.mock("@/services/processoService", () => ({
  obterDetalhesProcesso: vi.fn(),
}));

describe("RelatorioMapasView.vue", () => {
  const ctx = setupComponentTest();

  const stubs = {
    LayoutPadrao: { template: "<div><slot /></div>" },
    PageHeader: { template: "<div><slot name='actions' /></div>" },
    BCard: { template: "<div v-bind='$attrs'><slot /></div>" },
    BCardBody: { template: "<div><slot /></div>" },
    BCardTitle: { template: "<div><slot /></div>" },
    BRow: { template: "<div><slot /></div>" },
    BCol: { template: "<div><slot /></div>" },
    BFormGroup: { template: "<div><label><slot /></label></div>" },
    BFormInvalidFeedback: { template: "<div><slot /></div>" },
    BFormSelect: {
      props: ["modelValue", "options", "disabled"],
      template: "<select :value='modelValue' :disabled='disabled' @change='$emit(\"update:modelValue\", Number($event.target.value) || null)'><option v-for='o in options' :key='o.value' :value='o.value'>{{o.text}}</option></select>"
    },
    BButton: {
        props: ["disabled"],
        template: "<button :disabled='disabled' @click='$emit(\"click\")'><slot /></button>"
    },
    BSpinner: { template: "<span>loading...</span>" },
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(painelService.listarProcessos).mockResolvedValue({
      content: [{ codigo: 1, descricao: "Processo 1" }],
      totalElements: 1,
    } as any);
    vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue({
      unidades: [
        {
          codUnidade: 10,
          sigla: "SEC",
          nome: "Secretaria",
          codSubprocesso: 0,
          dataLimite: "",
          situacaoSubprocesso: "NAO_INICIADO",
          filhos: [
            {
              codUnidade: 11,
              sigla: "COORD",
              nome: "Coordenadoria",
              codSubprocesso: 111,
              dataLimite: "",
              situacaoSubprocesso: "MAPEAMENTO_MAPA_HOMOLOGADO",
              filhos: []
            }
          ]
        }
      ]
    } as any);
  });

  it("deve carregar processos ao montar", async () => {
    ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
    await ctx.wrapper.vm.$nextTick();

    expect(painelService.listarProcessos).toHaveBeenCalled();
  });

  it("não deve renderizar empty state na tela de mapas", async () => {
    ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));

    expect(ctx.wrapper.find("[data-testid='empty-state-mapas']").exists()).toBe(false);
  });

  it("deve chamar exportarMapasPdf ao clicar no botão gerar", async () => {
    ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
    await ctx.wrapper.vm.$nextTick();

    const relatoriosStore = useRelatoriosStore((ctx.wrapper.vm as any).$pinia);
    const exportarSpy = vi.spyOn(relatoriosStore, "exportarMapasPdf").mockResolvedValue(undefined as any);

    const vm = ctx.wrapper.vm;
    vm.processoIdSelecionado = 1;
    await ctx.wrapper.vm.$nextTick();

    const btn = ctx.wrapper.find("[data-testid='btn-gerar-mapas']");
    await btn.trigger("click");

    expect(exportarSpy).toHaveBeenCalledWith(1, undefined);
  });

  it("deve chamar buscarRelatorioMapas ao clicar no botão gerar html", async () => {
    ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
    await ctx.wrapper.vm.$nextTick();

    const relatoriosStore = useRelatoriosStore((ctx.wrapper.vm as any).$pinia);
    const buscarSpy = vi.spyOn(relatoriosStore, "buscarRelatorioMapas").mockResolvedValue(undefined as any);

    const vm = ctx.wrapper.vm as unknown as {
      processoIdSelecionado: number | null;
    };
    vm.processoIdSelecionado = 1;
    await ctx.wrapper.vm.$nextTick();

    await ctx.wrapper.find("[data-testid='btn-gerar-html-mapas']").trigger("click");

    expect(buscarSpy).toHaveBeenCalledWith(1, undefined);
  });

  it("deve incluir unidadeId na exportação se selecionada", async () => {
    ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
    await ctx.wrapper.vm.$nextTick();

    const relatoriosStore = useRelatoriosStore((ctx.wrapper.vm as any).$pinia);
    const exportarSpy = vi.spyOn(relatoriosStore, "exportarMapasPdf").mockResolvedValue(undefined as any);

    const vm = ctx.wrapper.vm;
    vm.processoIdSelecionado = 1;
    await ctx.wrapper.vm.$nextTick();
    vm.unidadeIdSelecionada = 11;
    await ctx.wrapper.vm.$nextTick();

    await ctx.wrapper.find("[data-testid='btn-gerar-mapas']").trigger("click");

    expect(exportarSpy).toHaveBeenCalledWith(1, 11);
  });

  it("não deve exibir a unidade virtual ADMIN nas opções", async () => {
    vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue({
      unidades: [
        {
          codUnidade: 1,
          sigla: "ADMIN",
          nome: "Administração",
          codSubprocesso: 0,
          dataLimite: "",
          situacaoSubprocesso: "NAO_INICIADO",
          filhos: [
            {
              codUnidade: 2,
              sigla: "SEC",
              nome: "Secretaria",
              codSubprocesso: 200,
              dataLimite: "",
              situacaoSubprocesso: "MAPEAMENTO_MAPA_HOMOLOGADO",
              filhos: []
            }
          ]
        }
      ]
    } as any);

    ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
    await ctx.wrapper.vm.$nextTick();
    const vm = ctx.wrapper.vm as unknown as {
      processoIdSelecionado: number | null;
      opcoesUnidades: Array<{ value: number | null; text: string }>;
    };
    vm.processoIdSelecionado = 1;
    await ctx.wrapper.vm.$nextTick();
    await ctx.wrapper.vm.$nextTick();

    expect(vm.opcoesUnidades).toEqual([
      { value: null, text: "Todas" },
      { value: 2, text: "SEC" }
    ]);
  });

  it("deve carregar unidades participantes ao selecionar um processo", async () => {
    ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
    await ctx.wrapper.vm.$nextTick();

    const vm = ctx.wrapper.vm as unknown as {
      processoIdSelecionado: number | null;
      opcoesUnidades: Array<{ value: number | null; text: string }>;
    };

    vm.processoIdSelecionado = 1;
    await ctx.wrapper.vm.$nextTick();
    await ctx.wrapper.vm.$nextTick();

    expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
    expect(vm.opcoesUnidades).toEqual([
      { value: null, text: "Todas" },
      { value: 11, text: "COORD" },
      { value: 10, text: "SEC" }
    ]);
  });

  it("deve deixar o select de unidade desabilitado até escolher um processo", async () => {
    ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
    await ctx.wrapper.vm.$nextTick();

    const selectUnidade = ctx.wrapper.find("[data-testid='select-unidade-mapas']");
    expect(selectUnidade.attributes("disabled")).toBeDefined();

    const vm = ctx.wrapper.vm as unknown as {
      processoIdSelecionado: number | null;
      opcoesUnidades: Array<{ value: number | null; text: string }>;
    };
    expect(vm.opcoesUnidades).toEqual([
      { value: null, text: "(Selecione um processo)" }
    ]);

    vm.processoIdSelecionado = 1;
    await ctx.wrapper.vm.$nextTick();

    expect(selectUnidade.attributes("disabled")).toBeUndefined();
  });

  it("deve renderizar o relatório html na própria página", async () => {
    ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({
      relatorios: {
        relatorioMapas: [
          {
            codigoUnidade: 11,
            siglaUnidade: "COORD",
            nomeUnidade: "Coordenadoria",
            totalCompetencias: 1,
            competencias: [
              {
                codigo: 21,
                descricao: "Competência 1",
                atividades: [
                  {
                    codigo: 31,
                    descricao: "Atividade 1",
                    conhecimentos: [
                      {
                        codigo: 41,
                        descricao: "Conhecimento 1"
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    }, stubs));
    await ctx.wrapper.vm.$nextTick();

    expect(ctx.wrapper.find("[data-testid='card-relatorio-mapas']").exists()).toBe(true);
    expect(ctx.wrapper.text()).toContain("COORD - Coordenadoria");
    expect(ctx.wrapper.text()).toContain("Competência 1");
    expect(ctx.wrapper.text()).toContain("Atividade 1");
    expect(ctx.wrapper.text()).toContain("Conhecimento 1");
  });
});
