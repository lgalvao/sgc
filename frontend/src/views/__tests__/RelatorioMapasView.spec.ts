import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import RelatorioMapasView from "@/views/RelatorioMapasView.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import * as unidadeService from "@/services/unidadeService";
import {useRelatoriosStore} from "@/stores/relatorios";
import {Perfil} from "@/types/tipos";

vi.mock("@/services/unidadeService", async (importOriginal) => {
    const actual = await importOriginal<typeof import("@/services/unidadeService")>();
    return {
        ...actual,
        buscarTodasUnidades: vi.fn(),
        buscarCodigosUnidadesComMapaVigente: vi.fn(),
    };
});

describe("RelatorioMapasView.vue", () => {
    const ctx = setupComponentTest();

    const ArvoreUnidadesStub = {
        props: ["modelValue", "unidades"],
        template: "<div data-testid='arvore-unidades-stub' @click='$emit(\"update:modelValue\", [10])'>Árvore</div>"
    };

    const stubs = {
        LayoutPadrao: {template: "<div><slot /></div>"},
        PageHeader: {template: "<div><slot name='actions' /></div>"},
        BAlert: {template: "<div data-testid='alert-sem-mapas'><slot /></div>"},
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
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValue([
            {
                codigo: 999,
                sigla: "ADMIN",
                nome: "Administração",
                filhas: [
                    {
                        codigo: 1,
                        sigla: "SA",
                        nome: "Secretaria A",
                        filhas: [
                            {
                                codigo: 10,
                                sigla: "COSIS",
                                nome: "Coordenadoria de Sistemas",
                                filhas: []
                            },
                            {
                                codigo: 11,
                                sigla: "COAUD",
                                nome: "Coordenadoria de Auditoria",
                                filhas: []
                            }
                        ]
                    }
                ]
            }
        ] as any);
        vi.mocked(unidadeService.buscarCodigosUnidadesComMapaVigente).mockResolvedValue([10]);
    });

    it("deve carregar unidades e codigos com mapa vigente ao montar", async () => {
        ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
        await flushPromises();

        expect(unidadeService.buscarTodasUnidades).toHaveBeenCalled();
        expect(unidadeService.buscarCodigosUnidadesComMapaVigente).toHaveBeenCalled();
    });

    it("deve mostrar apenas ramos que levam a unidades com mapa vigente", async () => {
        ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({}, stubs));
        await flushPromises();

        const vm = ctx.wrapper.vm as unknown as {
            unidadesDisponiveis: Array<{
                sigla: string;
                isElegivel?: boolean;
                filhas?: Array<{
                    sigla: string;
                    isElegivel?: boolean;
                    filhas?: Array<{ sigla: string; isElegivel?: boolean }>;
                }>;
            }>;
        };

        expect(vm.unidadesDisponiveis[0].sigla).toBe("ADMIN");
        expect(vm.unidadesDisponiveis[0].isElegivel).toBe(false);
        expect(vm.unidadesDisponiveis[0].filhas?.[0].sigla).toBe("SA");
        expect(vm.unidadesDisponiveis[0].filhas?.[0].isElegivel).toBe(false);
        expect(vm.unidadesDisponiveis[0].filhas?.[0].filhas).toEqual([
            expect.objectContaining({sigla: "COSIS", isElegivel: true}),
        ]);
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

    it("deve restringir a árvore à unidade ativa e subordinadas quando perfil for GESTOR", async () => {
        ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({
            perfil: {
                perfilSelecionado: Perfil.GESTOR,
                unidadeSelecionada: 1
            }
        }, stubs));
        await flushPromises();

        const vm = ctx.wrapper.vm as unknown as {
            unidadesDisponiveis: Array<{ sigla: string; filhas?: Array<{ sigla: string }> }>;
        };

        expect(vm.unidadesDisponiveis).toHaveLength(1);
        expect(vm.unidadesDisponiveis[0].sigla).toBe("SA");
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
        vi.mocked(unidadeService.buscarCodigosUnidadesComMapaVigente).mockResolvedValue([]);

        ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({
            perfil: {
                perfilSelecionado: Perfil.ADMIN
            }
        }, stubs));
        await flushPromises();

        expect(ctx.wrapper.find("[data-testid='alert-sem-mapas']").exists()).toBe(true);
        expect(ctx.wrapper.text()).toContain("Não há mapas vigentes.");
        expect(ctx.wrapper.find("[data-testid='container-arvore-unidades-mapas']").exists()).toBe(false);
    });

    it("deve mostrar alerta específico para GESTOR quando não houver mapas vigentes na subárvore", async () => {
        vi.mocked(unidadeService.buscarCodigosUnidadesComMapaVigente).mockResolvedValue([]);

        ctx.wrapper = mount(RelatorioMapasView, getCommonMountOptions({
            perfil: {
                perfilSelecionado: Perfil.GESTOR,
                unidadeSelecionada: 1
            }
        }, stubs));
        await flushPromises();

        expect(ctx.wrapper.find("[data-testid='alert-sem-mapas']").exists()).toBe(true);
        expect(ctx.wrapper.text()).toContain("Não há mapas vigentes para sua unidade ou unidades subordinadas.");
        expect(ctx.wrapper.find("[data-testid='container-arvore-unidades-mapas']").exists()).toBe(false);
    });
});
