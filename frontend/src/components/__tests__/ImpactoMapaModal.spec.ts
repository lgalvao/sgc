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
            atividadesInseridas: [{codigo: 1, descricao: "Nova atividade", conhecimentos: ["Conhecimento A"]}],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: [],
        };

        const wrapper = createWrapper({impacto});
        await flushPromises();

        expect(wrapper.text()).toContain("Atividades inseridas");
        expect(wrapper.text()).toContain("Nova atividade");
        expect(wrapper.text()).toContain("Conhecimento A");
    });

    it("deve mostrar competências impactadas e suas mensagens de alteração", async () => {
        const impacto: any = {
            temImpactos: true,
            atividadesInseridas: [],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: [
                {
                    codigo: 1,
                    descricao: "Competência A",
                    atividadesAfetadas: ["Atividade removida: Ativ 1"],
                    tiposImpacto: [TipoImpactoCompetencia.ATIVIDADE_REMOVIDA],
                },
                {
                    codigo: 2,
                    descricao: "Competência B",
                    atividadesAfetadas: ["Atividade alterada: Ativ 2", "  Descrição alterada para Ativ 2 Novo"],
                    tiposImpacto: [TipoImpactoCompetencia.ATIVIDADE_ALTERADA],
                }
            ],
        };

        const wrapper = createWrapper({impacto});
        await flushPromises();

        expect(wrapper.text()).toContain("Competências impactadas");
        expect(wrapper.text()).toContain("Competência A");
        expect(wrapper.text()).toContain("Atividade removida: Ativ 1");

        expect(wrapper.text()).toContain("Competência B");
        expect(wrapper.text()).toContain("Atividade alterada: Ativ 2");
        expect(wrapper.text()).toContain("Descrição alterada para Ativ 2 Novo");
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
