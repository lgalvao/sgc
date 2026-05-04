import {describe, expect, it} from "vitest";
import {mount} from "@vue/test-utils";
import MapaModaisRoot from "../MapaModaisRoot.vue";

describe("MapaModaisRoot.vue", () => {
    const defaultProps = {
        modoSomenteLeitura: false,
        atividades: [],
        codigoSubprocesso: 123,
        mostrarModalCriarNovaCompetencia: true,
        competenciaSendoEditada: null,
        loadingCompetencia: false,
        fieldErrors: {},
        mostrarModalDisponibilizar: true,
        loadingDisponibilizacao: false,
        notificacaoDisponibilizacao: "",
        mostrarModalExcluirCompetencia: true,
        loadingExclusao: false,
        competenciaParaExcluir: null,
        carregandoFluxoMapa: false,
        homologacao: false,
        mostrarModalAceitar: true,
        mostrarModalValidar: true,
        mostrarModalDevolucao: true,
        mensagemErroDevolucao: "",
        observacaoDevolucao: "",
        mostrarModalSugestoes: true,
        loadingSugestoesEnvio: false,
        mensagemErroSugestoes: "",
        sugestoes: "",
        mostrarModalVerSugestoes: true,
        podeApresentarSugestoes: true,
        sugestoesVisualizacao: "",
        mostrarModalImpacto: true,
        loadingImpacto: false,
        mostrarModalHistorico: true,
        historicoAnalise: []
    };

    it("deve renderizar todos os modais quando as props de visibilidade estão ativas", () => {
        const wrapper = mount(MapaModaisRoot, {
            props: defaultProps,
            global: {
                stubs: {
                    CompetenciaEdicaoModal: true,
                    MapaDisponibilizacaoModal: true,
                    CompetenciaExclusaoModal: true,
                    MapaAceitacaoModal: true,
                    MapaValidacaoModal: true,
                    MapaDevolucaoModal: true,
                    MapaSugestoesEnvioModal: true,
                    MapaSugestoesVisualizacaoModal: true,
                    ImpactoMapaModal: true,
                    HistoricoAnaliseModal: true
                }
            }
        });

        // Verifica se os componentes stubs foram renderizados
        expect(wrapper.findComponent({name: 'CompetenciaEdicaoModal'}).exists()).toBe(true);
        expect(wrapper.findComponent({name: 'MapaDisponibilizacaoModal'}).exists()).toBe(true);
        expect(wrapper.findComponent({name: 'CompetenciaExclusaoModal'}).exists()).toBe(true);
        expect(wrapper.findComponent({name: 'MapaAceitacaoModal'}).exists()).toBe(true);
        expect(wrapper.findComponent({name: 'MapaValidacaoModal'}).exists()).toBe(true);
        expect(wrapper.findComponent({name: 'MapaDevolucaoModal'}).exists()).toBe(true);
        expect(wrapper.findComponent({name: 'MapaSugestoesEnvioModal'}).exists()).toBe(true);
        expect(wrapper.findComponent({name: 'MapaSugestoesVisualizacaoModal'}).exists()).toBe(true);
    });

    it("não deve renderizar modais de edição em modo somente leitura", () => {
        const wrapper = mount(MapaModaisRoot, {
            props: {...defaultProps, modoSomenteLeitura: true},
            global: {
                stubs: {
                    CompetenciaEdicaoModal: true,
                    MapaDisponibilizacaoModal: true,
                    CompetenciaExclusaoModal: true,
                    MapaAceitacaoModal: true,
                    MapaValidacaoModal: true,
                    MapaDevolucaoModal: true,
                    MapaSugestoesEnvioModal: true,
                    MapaSugestoesVisualizacaoModal: true,
                    ImpactoMapaModal: true,
                    HistoricoAnaliseModal: true
                }
            }
        });

        expect(wrapper.findComponent({name: 'CompetenciaEdicaoModal'}).exists()).toBe(false);
        expect(wrapper.findComponent({name: 'MapaDisponibilizacaoModal'}).exists()).toBe(false);
        expect(wrapper.findComponent({name: 'CompetenciaExclusaoModal'}).exists()).toBe(false);

        // Modais de workflow ainda devem aparecer
        expect(wrapper.findComponent({name: 'MapaAceitacaoModal'}).exists()).toBe(true);
    });
});
