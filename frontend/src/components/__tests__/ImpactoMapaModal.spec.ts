import {flushPromises, mount} from "@vue/test-utils";
import {describe, expect, it} from "vitest";
import ImpactoMapaModal from "@/components/mapa/ImpactoMapaModal.vue";
import {TipoImpactoCompetencia} from "@/types/tipos";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

describe("ImpactoMapaModal.vue", () => {
    const context = setupComponentTest();

    function createWrapper(propsOverrides = {}) {
        context.wrapper = mount(ImpactoMapaModal, {
            ...getCommonMountOptions(),
            props: {
                mostrar: true,
                impacto: null,
                loading: false,
                ...propsOverrides
            },
        });
        return context.wrapper;
    }

    it("deve mostrar mensagem de nenhum impacto", async () => {
        const impacto: any = {
            temImpactos: false,
            atividadesInseridas: [],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: [],
        };

        const wrapper = createWrapper({impacto});
        await flushPromises();

        expect(wrapper.text()).toContain("Nenhum impacto no mapa da unidade.");
    });

    it("deve mostrar atividades inseridas", async () => {
        const impacto: any = {
            temImpactos: true,
            atividadesInseridas: [{codigo: 1, descricao: "Nova Atividade", competenciasVinculadas: ["Comp A"]}],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: [],
        };

        const wrapper = createWrapper({impacto});
        await flushPromises();

        expect(wrapper.text()).toContain("Atividades Inseridas");
        expect(wrapper.text()).toContain("Nova Atividade");
        expect(wrapper.text()).toContain("Vinculada a: Comp A");
    });

    it("deve mostrar atividades removidas", async () => {
        const impacto: any = {
            temImpactos: true,
            atividadesInseridas: [],
            atividadesRemovidas: [{codigo: 2, descricao: "Atividade Removida"}],
            atividadesAlteradas: [],
            competenciasImpactadas: [],
        };

        const wrapper = createWrapper({impacto});
        await flushPromises();

        expect(wrapper.text()).toContain("Atividades Removidas");
        expect(wrapper.text()).toContain("Atividade Removida");
        const item = wrapper.find('[data-testid="lista-atividades-removidas"] li strong');
        expect(item.classes()).toContain("text-decoration-line-through");
    });

    it("deve mostrar atividades alteradas", async () => {
        const impacto: any = {
            temImpactos: true,
            atividadesInseridas: [],
            atividadesRemovidas: [],
            atividadesAlteradas: [{
                codigo: 3,
                descricao: "Atividade Nova Desc",
                descricaoAnterior: "Atividade Velha Desc"
            }],
            competenciasImpactadas: [],
        };

        const wrapper = createWrapper({impacto});
        await flushPromises();

        expect(wrapper.text()).toContain("Atividades Alteradas");
        expect(wrapper.text()).toContain("Atividade Nova Desc");
        expect(wrapper.text()).toContain("Anterior: Atividade Velha Desc");
    });

    it("deve mostrar competências impactadas e formatar tipos de impacto corretamente", async () => {
        const impacto: any = {
            temImpactos: true,
            atividadesInseridas: [],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: [
                {
                    codigo: 1,
                    descricao: "Competência A",
                    atividadesAfetadas: ["Ativ 1"],
                    tiposImpacto: [TipoImpactoCompetencia.ATIVIDADE_REMOVIDA],
                },
                {
                    codigo: 2,
                    descricao: "Competência B",
                    atividadesAfetadas: ["Ativ 2"],
                    tiposImpacto: [TipoImpactoCompetencia.ATIVIDADE_ALTERADA],
                },
                {
                    codigo: 3,
                    descricao: "Competência C",
                    atividadesAfetadas: ["Ativ 3", "Ativ 4"],
                    tiposImpacto: [TipoImpactoCompetencia.ATIVIDADE_REMOVIDA, TipoImpactoCompetencia.ATIVIDADE_ALTERADA],
                },
            ],
        };

        const wrapper = createWrapper({impacto});
        await flushPromises();

        expect(wrapper.text()).toContain("Competências Impactadas");
        expect(wrapper.text()).toContain("Competência A");
        expect(wrapper.text()).toContain("Atividade Removida"); // Formatação do tipo

        expect(wrapper.text()).toContain("Competência B");
        expect(wrapper.text()).toContain("Atividade Alterada"); // Formatação do tipo

        expect(wrapper.text()).toContain("Competência C");
        // Competência C tem múltiplos tipos
        expect(wrapper.text()).toContain("Atividade Removida, Atividade Alterada");
    });

    it("deve emitir evento 'fechar' ao clicar no botão", async () => {
        const impacto: any = {
            temImpactos: false,
            atividadesInseridas: [],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: []
        };
        const wrapper = createWrapper({impacto});
        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-fechar-impacto"]');
        await btn.trigger("click");

        expect(wrapper.emitted().fechar).toBeTruthy();
    });

    it("deve exibir estado de carregamento via prop", async () => {
        const wrapper = createWrapper({loading: true, impacto: null});
        expect(wrapper.text()).toContain("Verificando impactos...");
        expect(wrapper.find('.spinner-border').exists()).toBe(true);
    });
});
