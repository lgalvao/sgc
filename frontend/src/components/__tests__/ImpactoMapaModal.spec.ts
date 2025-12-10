import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {afterEach, describe, expect, it, vi} from "vitest";
import ImpactoMapaModal from "@/components/ImpactoMapaModal.vue";
import {TipoImpactoCompetencia} from "@/types/impacto";
import {useMapasStore} from "@/stores/mapas";

describe("ImpactoMapaModal.vue", () => {
    let wrapper: any;

    function createWrapper(propsOverrides = {}, initialState = {}) {
        return mount(ImpactoMapaModal, {
            props: {
                mostrar: true,
                idProcesso: 1,
                siglaUnidade: "TESTE",
                ...propsOverrides
            },
            global: {
                plugins: [
                    createTestingPinia({
                        stubActions: false,
                        initialState: {
                            mapas: {
                                impactoMapa: null,
                            },
                            processos: {
                                processoDetalhe: {
                                    unidades: [{sigla: "TESTE", codSubprocesso: 123}],
                                },
                            },
                            ...initialState
                        },
                    }),
                ],
            },
        });
    }

    afterEach(() => {
        wrapper?.unmount();
        vi.clearAllMocks();
    });

    it("deve mostrar mensagem de nenhum impacto", async () => {
        const impacto = {
            temImpactos: false,
            atividadesInseridas: [],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: [],
        };

        wrapper = createWrapper({}, {mapas: {impactoMapa: impacto}});
        await flushPromises();

        expect(wrapper.text()).toContain("Nenhum impacto detectado no mapa.");
    });

    it("deve mostrar atividades inseridas", async () => {
        const impacto = {
            temImpactos: true,
            atividadesInseridas: [{codigo: 1, descricao: "Nova Atividade", competenciasVinculadas: ["Comp A"]}],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: [],
        };

        wrapper = createWrapper({}, {mapas: {impactoMapa: impacto}});
        await flushPromises();

        expect(wrapper.text()).toContain("Atividades Inseridas");
        expect(wrapper.text()).toContain("Nova Atividade");
        expect(wrapper.text()).toContain("Vinculada a: Comp A");
    });

    it("deve mostrar atividades removidas", async () => {
        const impacto = {
            temImpactos: true,
            atividadesInseridas: [],
            atividadesRemovidas: [{codigo: 2, descricao: "Atividade Removida"}],
            atividadesAlteradas: [],
            competenciasImpactadas: [],
        };

        wrapper = createWrapper({}, {mapas: {impactoMapa: impacto}});
        await flushPromises();

        expect(wrapper.text()).toContain("Atividades Removidas");
        expect(wrapper.text()).toContain("Atividade Removida");
        // Verifica se tem a classe text-decoration-line-through
        const item = wrapper.find('[data-testid="lista-atividades-removidas"] li strong');
        expect(item.classes()).toContain("text-decoration-line-through");
    });

    it("deve mostrar atividades alteradas", async () => {
        const impacto = {
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

        wrapper = createWrapper({}, {mapas: {impactoMapa: impacto}});
        await flushPromises();

        expect(wrapper.text()).toContain("Atividades Alteradas");
        expect(wrapper.text()).toContain("Atividade Nova Desc");
        expect(wrapper.text()).toContain("Anterior: Atividade Velha Desc");
    });

    it("deve mostrar competências impactadas e formatar tipos de impacto corretamente", async () => {
        const impacto = {
            temImpactos: true,
            atividadesInseridas: [],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: [
                {
                    codigo: 1,
                    descricao: "Competência A",
                    atividadesAfetadas: ["Ativ 1"],
                    tipoImpacto: TipoImpactoCompetencia.ATIVIDADE_REMOVIDA,
                },
                {
                    codigo: 2,
                    descricao: "Competência B",
                    atividadesAfetadas: ["Ativ 2"],
                    tipoImpacto: TipoImpactoCompetencia.ATIVIDADE_ALTERADA,
                },
                {
                    codigo: 3,
                    descricao: "Competência C",
                    atividadesAfetadas: ["Ativ 3"],
                    tipoImpacto: TipoImpactoCompetencia.IMPACTO_GENERICO,
                },
            ],
        };

        wrapper = createWrapper({}, {mapas: {impactoMapa: impacto}});
        await flushPromises();

        expect(wrapper.text()).toContain("Competências Impactadas");
        expect(wrapper.text()).toContain("Competência A");
        expect(wrapper.text()).toContain("Atividade Removida"); // Formatação do tipo

        expect(wrapper.text()).toContain("Competência B");
        expect(wrapper.text()).toContain("Atividade Alterada"); // Formatação do tipo

        expect(wrapper.text()).toContain("Competência C");
        expect(wrapper.text()).toContain("Alteração no Mapa"); // Formatação do tipo
    });

    it("deve carregar dados quando 'mostrar' mudar para true", async () => {
        // Inicia com mostrar: false
        wrapper = mount(ImpactoMapaModal, {
            props: {
                mostrar: false,
                idProcesso: 1,
                siglaUnidade: "TESTE",
            },
            global: {
                plugins: [
                    createTestingPinia({
                        stubActions: false,
                        initialState: {
                            processos: {
                                processoDetalhe: {
                                    unidades: [{sigla: "TESTE", codSubprocesso: 123}],
                                },
                            },
                        }
                    }),
                ],
            },
        });

        const mapasStore = useMapasStore();
        mapasStore.buscarImpactoMapa = vi.fn();

        await wrapper.setProps({mostrar: true});
        await flushPromises();

        expect(mapasStore.buscarImpactoMapa).toHaveBeenCalledWith(123);
    });

    it("não deve buscar dados se subprocesso não encontrado", async () => {
        // Inicia com mostrar: false e unidade sem subprocesso
        wrapper = mount(ImpactoMapaModal, {
            props: {
                mostrar: false,
                idProcesso: 1,
                siglaUnidade: "OUTRA", // Unidade que não existe no store
            },
            global: {
                plugins: [
                    createTestingPinia({
                        stubActions: false,
                        initialState: {
                            processos: {
                                processoDetalhe: {
                                    unidades: [{sigla: "TESTE", codSubprocesso: 123}],
                                },
                            },
                        }
                    }),
                ],
            },
        });

        const mapasStore = useMapasStore();
        mapasStore.buscarImpactoMapa = vi.fn();

        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {
        });

        await wrapper.setProps({mostrar: true});
        await flushPromises();

        expect(mapasStore.buscarImpactoMapa).not.toHaveBeenCalled();
        expect(consoleSpy).toHaveBeenCalledWith("Subprocesso não encontrado para unidade", "OUTRA");
        consoleSpy.mockRestore();
    });

    it("deve emitir evento 'fechar' ao clicar no botão", async () => {
        const impacto = {
            temImpactos: false,
            atividadesInseridas: [],
            atividadesRemovidas: [],
            atividadesAlteradas: [],
            competenciasImpactadas: []
        };
        wrapper = createWrapper({}, {mapas: {impactoMapa: impacto}});
        await flushPromises();

        const btn = wrapper.find('[data-testid="btn-fechar-impacto"]');
        await btn.trigger("click");

        expect(wrapper.emitted().fechar).toBeTruthy();
    });

    it("deve exibir estado de carregamento", async () => {
        wrapper = createWrapper();
        // Forçamos o estado de carregamento simulando a chamada
        const mapasStore = useMapasStore();

        mapasStore.buscarImpactoMapa = vi.fn((_: number) => new Promise<void>(() => {
        })); // Promise que nunca resolve

        // Recriar para pegar a transição de props
        wrapper = mount(ImpactoMapaModal, {
            props: {mostrar: false, idProcesso: 1, siglaUnidade: "TESTE"},
            global: {
                plugins: [createTestingPinia({
                    initialState: {
                        processos: {processoDetalhe: {unidades: [{sigla: "TESTE", codSubprocesso: 123}]}}
                    }
                })]
            }
        });

        await wrapper.setProps({mostrar: true});
        // Não damos flushPromises para manter o loading state

        expect(wrapper.text()).toContain("Verificando impactos...");
        expect(wrapper.find('.spinner-border').exists()).toBe(true);
    });
});