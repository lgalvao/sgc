import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import RelatorioUnidadesSemMapasVigentesView from "@/views/RelatorioUnidadesSemMapasVigentesView.vue";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";
import * as unidadeService from "@/services/unidadeService";
import {TEXTOS_RELATORIOS} from "@/constants/textos-relatorios";

const mocks = vi.hoisted(() => ({
    notify: vi.fn(),
    downloadRelatorioUnidadesSemMapasVigentesPdf: vi.fn(),
}));

vi.mock("@/services/unidadeService", async (importOriginal) => {
    const actual = await importOriginal<typeof import("@/services/unidadeService")>();
    return {
        ...actual,
        buscarTodasUnidades: vi.fn(),
        buscarCodigosUnidadesSemMapaVigente: vi.fn(),
    };
});

vi.mock("@/composables/useNotification", () => ({
    useNotification: () => ({notify: mocks.notify}),
}));

vi.mock("@/services/relatoriosService", () => ({
    relatoriosService: {
        downloadRelatorioUnidadesSemMapasVigentesPdf: mocks.downloadRelatorioUnidadesSemMapasVigentesPdf,
    },
}));

describe("RelatorioUnidadesSemMapasVigentesView.vue", () => {
    const ctx = setupComponentTest();

    const stubs = {
        LayoutPadrao: {template: "<div><slot /></div>"},
        PageHeader: {template: "<div><slot name='actions' /></div>"},
        BCard: {template: "<div><slot /></div>"},
        BSpinner: {template: "<span>loading...</span>"},
        BButton: {
            props: ["disabled"],
            emits: ["click"],
            template: "<button :disabled='disabled' @click='$emit(\"click\")'><slot /></button>",
        },
        CarregamentoPagina: {template: "<div data-testid='pagina-carregando' />"},
        EmptyState: {template: "<div data-testid='empty-state' />"},
        UnidadesSemMapaArvore: {
            props: ["unidades"],
            template: "<div data-testid='arvore'>{{ unidades.map((u) => u.sigla).join(',') }}</div>",
        },
    };

    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValue([
            {
                codigo: 900,
                sigla: "ADMIN",
                nome: "Administração",
                filhas: [
                    {
                        codigo: 2,
                        sigla: "SA",
                        nome: "Secretaria A",
                        filhas: [],
                    },
                    {
                        codigo: 3,
                        sigla: "SB",
                        nome: "Secretaria B",
                        filhas: [
                            {
                                codigo: 4,
                                sigla: "COA",
                                nome: "Coordenadoria A",
                                filhas: [],
                            },
                        ],
                    },
                ],
            },
            {
                codigo: 901,
                sigla: "ORFA",
                nome: "Unidade sem superior ativo",
                filhas: [],
            },
        ] as any);
        vi.mocked(unidadeService.buscarCodigosUnidadesSemMapaVigente).mockResolvedValue([2, 4, 901, 999]);
        mocks.downloadRelatorioUnidadesSemMapasVigentesPdf.mockResolvedValue(undefined);
    });

    it("deve exibir a árvore das unidades sem mapa vigente", async () => {
        ctx.wrapper = mount(RelatorioUnidadesSemMapasVigentesView, getCommonMountOptions({}, stubs));

        await ctx.wrapper.find("[data-testid='btn-visualizar-unidades-sem-mapa']").trigger("click");
        await flushPromises();

        expect(unidadeService.buscarTodasUnidades).toHaveBeenCalledTimes(1);
        expect(unidadeService.buscarCodigosUnidadesSemMapaVigente).toHaveBeenCalledTimes(1);
        expect(ctx.wrapper.text()).not.toContain("ADMIN");
        expect(ctx.wrapper.text()).not.toContain("ORFA");
        expect(ctx.wrapper.text()).toContain("SA");
        expect(ctx.wrapper.find("[data-testid='arvore']").text()).toContain("COA");
    });

    it("deve exibir empty state quando não houver unidades sem mapa vigente", async () => {
        vi.mocked(unidadeService.buscarCodigosUnidadesSemMapaVigente).mockResolvedValue([]);
        ctx.wrapper = mount(RelatorioUnidadesSemMapasVigentesView, getCommonMountOptions({}, stubs));

        await ctx.wrapper.find("[data-testid='btn-visualizar-unidades-sem-mapa']").trigger("click");
        await flushPromises();

        expect(ctx.wrapper.find("[data-testid='empty-state']").exists()).toBe(true);
    });

    it("deve notificar erro ao falhar a carga do relatório", async () => {
        vi.mocked(unidadeService.buscarTodasUnidades).mockRejectedValue(new Error("falha"));
        ctx.wrapper = mount(RelatorioUnidadesSemMapasVigentesView, getCommonMountOptions({}, stubs));

        await ctx.wrapper.find("[data-testid='btn-visualizar-unidades-sem-mapa']").trigger("click");
        await flushPromises();

        expect(mocks.notify).toHaveBeenCalledWith(TEXTOS_RELATORIOS.ERRO_BUSCA, "danger");
    });

    it("deve disparar exportação de PDF", async () => {
        ctx.wrapper = mount(RelatorioUnidadesSemMapasVigentesView, getCommonMountOptions({}, stubs));

        await ctx.wrapper.find("[data-testid='btn-pdf-unidades-sem-mapa']").trigger("click");
        await flushPromises();

        expect(mocks.downloadRelatorioUnidadesSemMapasVigentesPdf).toHaveBeenCalledTimes(1);
    });
});
