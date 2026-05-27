import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import {ref} from "vue";
import RelatorioMapasView from "@/views/RelatorioMapasView.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import {useRelatoriosStore} from "@/stores/relatorios";
import {Perfil} from "@/types/tipos";
import * as relatoriosQueryModule from "@/composables/useRelatorioMapasQuery";

const mockUnidadesDisponiveis = ref<any[]>([]);
const mockIsLoading = ref(false);
const mockIsPending = ref(false);

vi.mock("@/composables/useRelatorioMapasQuery", () => ({
    useRelatorioUnidadesComMapaQuery: () => ({
        data: mockUnidadesDisponiveis,
        isLoading: mockIsLoading,
        isPending: mockIsPending,
        refetch: vi.fn(),
    })
}));

describe("RelatorioMapasView.vue", () => {
    const ctx = setupComponentTest();

    const ArvoreUnidadesStub = {
        props: ["modelValue", "unidades"],
        template: "<div data-testid='arvore-unidades-stub' @click='$emit(\"update:modelValue\", [10])'>Árvore</div>"
    };

    const stubs = {
        LayoutPadrao: {template: "<div><slot /></div>"},
        PageHeader: {template: "<div><slot name='actions' /></div>"},
        BAlert: {template: "<div class='alert-stub'><slot /></div>"},
        EmptyState: {
            props: ["title", "description", "icon"],
            template: "<div data-testid='empty-state-stub'><h1>{{ title }}</h1><p>{{ description }}</p></div>"
        },
        BCard: {template: "<div v-bind='$attrs'><slot /></div>"},
        BCardBody: {template: "<div><slot /></div>"},
        BCardTitle: {template: "<div><slot /></div>"},
        BFormGroup: {template: "<div><label><slot /></label></div>"},
        BButton: {
            props: ["disabled"],
            template: "<button :disabled='disabled' @click='$emit(\"click\")'><slot /></button>"
        },
        BSpinner: {template: "<span>loading...</span>"},
        ArvoreUnidades: ArvoreUnidadesStub,
    };

    beforeEach(() => {
        vi.clearAllMocks();
        mockIsLoading.value = false;
        mockIsPending.value = false;
        mockUnidadesDisponiveis.value = [
            {
                codigo: 999,
                sigla: "ADMIN",
                nome: "Administração",
                isElegivel: false,
                filhas: [
                    {
                        codigo: 1,
                        sigla: "SA",
                        nome: "Secretaria A",
                        isElegivel: false,
                        filhas: [
                            {
                                codigo: 10,
                                sigla: "COSIS",
                                nome: "Coordenadoria de Sistemas",
                                isElegivel: true,
                                filhas: []
                            }
                        ]
                    }
                ]
            }
        ];
    });

    it("deve carregar query de unidades com mapa vigente ao montar", async () => {
        const querySpy = vi.spyOn(relatoriosQueryModule, "useRelatorioUnidadesComMapaQuery");
        ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
        await flushPromises();

        expect(querySpy).toHaveBeenCalled();
    });

    it("deve mostrar ramos disponíveis retornados pela query", async () => {
        ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
        await flushPromises();

        const vm = ctx.wrapper.vm as any;

        expect(vm.unidadesDisponiveis[0].sigla).toBe("ADMIN");
        expect(vm.unidadesDisponiveis[0].isElegivel).toBe(false);
        expect(vm.unidadesDisponiveis[0].filhas?.[0].sigla).toBe("SA");
        expect(vm.unidadesDisponiveis[0].filhas?.[0].filhas?.[0].sigla).toBe("COSIS");
        expect(vm.unidadesDisponiveis[0].filhas?.[0].filhas?.[0].isElegivel).toBe(true);
    });

    it("deve enviar as unidades selecionadas ao gerar html", async () => {
        ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
        await flushPromises();

        const relatoriosStore = useRelatoriosStore((ctx.wrapper.vm).$pinia);
        const buscarSpy = vi.spyOn(relatoriosStore, "buscarRelatorioMapas").mockResolvedValue(undefined as any);

        await ctx.wrapper.find("[data-testid='arvore-unidades-stub']").trigger("click");
        await flushPromises();
        await ctx.wrapper.find("[data-testid='btn-gerar-html-mapas']").trigger("click");

        expect(buscarSpy).toHaveBeenCalledWith([10]);
    });

    it("deve enviar as unidades selecionadas ao exportar pdf", async () => {
        ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
        await flushPromises();

        const relatoriosStore = useRelatoriosStore((ctx.wrapper.vm).$pinia);
        const exportarSpy = vi.spyOn(relatoriosStore, "exportarMapasPdf").mockResolvedValue(undefined as any);

        await ctx.wrapper.find("[data-testid='arvore-unidades-stub']").trigger("click");
        await flushPromises();
        await ctx.wrapper.find("[data-testid='btn-gerar-mapas']").trigger("click");

        expect(exportarSpy).toHaveBeenCalledWith([10]);
    });

    it("deve desabilitar os botões enquanto não houver unidades selecionadas", async () => {
        ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
        await flushPromises();

        const btnGerarHtml = ctx.wrapper.find("[data-testid='btn-gerar-html-mapas']");
        const btnPdf = ctx.wrapper.find("[data-testid='btn-gerar-mapas']");

        expect(btnGerarHtml.attributes("disabled")).toBeDefined();
        expect(btnPdf.attributes("disabled")).toBeDefined();

        await ctx.wrapper.find("[data-testid='arvore-unidades-stub']").trigger("click");
        await flushPromises();

        expect(btnGerarHtml.attributes("disabled")).toBeUndefined();
        expect(btnPdf.attributes("disabled")).toBeUndefined();
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
        await flushPromises();

        expect(ctx.wrapper.find("[data-testid='card-mapa-vigente']").exists()).toBe(true);
        expect(ctx.wrapper.text()).toContain("COORD");
        expect(ctx.wrapper.text()).toContain("Coordenadoria");
        expect(ctx.wrapper.text()).toContain("Competência 1");
        expect(ctx.wrapper.text()).toContain("Atividade 1");
        expect(ctx.wrapper.text()).toContain("Conhecimento 1");
    });

    it("deve encerrar o carregamento mesmo quando gerar relatório falhar", async () => {
        ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
        await flushPromises();

        const relatoriosStore = useRelatoriosStore((ctx.wrapper.vm).$pinia);
        vi.spyOn(relatoriosStore, "buscarRelatorioMapas").mockRejectedValue(new Error("403"));

        await ctx.wrapper.find("[data-testid='arvore-unidades-stub']").trigger("click");
        await flushPromises();

        await (ctx.wrapper.vm as any).gerarRelatorio();

        expect((ctx.wrapper.vm as any).carregando).toBe(false);
    });

    it("deve mostrar alerta para ADMIN quando não houver mapas vigentes", async () => {
        mockUnidadesDisponiveis.value = [];

        ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({
            perfil: {
                perfilSelecionado: Perfil.ADMIN
            }
        }, stubs));
        await flushPromises();

        expect(ctx.wrapper.find("[data-testid='empty-state-stub']").exists()).toBe(true);
        expect(ctx.wrapper.text()).toContain("Não há mapas vigentes.");
        expect(ctx.wrapper.find("[data-testid='container-arvore-unidades-mapas']").exists()).toBe(false);
    });

    it("deve mostrar alerta específico para GESTOR quando não houver mapas vigentes na subárvore", async () => {
        mockUnidadesDisponiveis.value = [];

        ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({
            perfil: {
                perfilSelecionado: Perfil.GESTOR,
                unidadeSelecionada: 1
            }
        }, stubs));
        await flushPromises();

        expect(ctx.wrapper.find("[data-testid='empty-state-stub']").exists()).toBe(true);
        expect(ctx.wrapper.text()).toContain("Não há mapas vigentes para sua unidade ou unidades subordinadas.");
        expect(ctx.wrapper.find("[data-testid='container-arvore-unidades-mapas']").exists()).toBe(false);
    });
});
