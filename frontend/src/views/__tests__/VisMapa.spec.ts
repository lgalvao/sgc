import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount} from '@vue/test-utils';
import {createMemoryHistory, createRouter} from 'vue-router';
import {createTestingPinia} from '@pinia/testing';
import VisMapa from '@/views/processo/MapaVisualizacaoView.vue';
import AceitarMapaModal from "@/components/mapa/AceitarMapaModal.vue";
import {useProcessosStore} from "@/stores/processos";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useFeedbackStore} from "@/stores/feedback";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import {setupComponentTest} from "@/test-utils/componentTestHelpers";
import * as useAcessoModule from '@/composables/useAcesso';

// Mocks for services
vi.mock("@/services/unidadeService", () => ({
    buscarUnidadePorSigla: vi.fn().mockResolvedValue({ sigla: 'TEST', nome: 'Unidade' }),
}));
vi.mock("@/services/processoService", () => ({
    obterDetalhesProcesso: vi.fn().mockResolvedValue({ 
        codigo: 1,
        unidades: [
            {
                sigla: 'TEST',
                codUnidade: 10,
                codSubprocesso: 10,
                situacaoSubprocesso: 'MAPEAMENTO_MAPA_DISPONIBILIZADO',
            }
        ]
    }),
}));
vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoDetalhe: vi.fn().mockResolvedValue({ 
    }),
    obterMapaVisualizacao: vi.fn().mockResolvedValue({ competencias: [] }),
    listarAnalisesCadastro: vi.fn().mockResolvedValue([]),
    listarAnalisesValidacao: vi.fn().mockResolvedValue([]),
}));

const router = createRouter({
    history: createMemoryHistory(),
    routes: [
        { path: "/", component: { template: "<div>Home</div>" } },
        {
            path: "/processo/:codProcesso/:siglaUnidade/vis-mapa",
            name: "SubprocessoVisMapa",
            component: VisMapa,
        },
        {
            path: "/painel",
            name: "Painel",
            component: { template: "<div>Painel</div>" },
        },
        {
            path: "/processo/:codProcesso/:siglaUnidade",
            name: "Subprocesso",
            component: { template: "<div>Subprocesso</div>" },
        },
    ],
});

describe("VisMapa.vue", () => {
    const context = setupComponentTest();

    beforeEach(async () => {
        vi.clearAllMocks();
        await router.push("/processo/1/TEST/vis-mapa");
        await router.isReady();
    });

    const mountComponent = (initialState: any = {}, siglaUnidade = "TEST", accessOverrides: any = {}) => {
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeValidarMapa: { value: true },
            podeApresentarSugestoes: { value: true },
            podeAceitarMapa: { value: true },
            podeDevolverMapa: { value: true },
            podeHomologarMapa: { value: false },
            podeVerPagina: { value: true },
            podeVisualizarMapa: { value: true },
            ...accessOverrides
        } as any);

        context.wrapper = mount(VisMapa, {
            global: {
                plugins: [
                    createTestingPinia({
                        createSpy: vi.fn,
                        stubActions: false,
                        initialState: {
                            mapas: {
                                mapaVisualizacao: {
                                    codigo: 1,
                                    descricao: "Mapa Test",
                                    competencias: [
                                        {
                                            codigo: 1,
                                            descricao: "Competencia 1",
                                            atividades: [
                                                {
                                                    codigo: 1,
                                                    descricao: "Atividade 1",
                                                    conhecimentos: [
                                                        { codigo: 1, descricao: "Conhecimento 1" },
                                                    ],
                                                },
                                            ],
                                        },
                                    ],
                                    sugestoes: "Sugestoes do mapa",
                                },
                                ...initialState["mapas"],
                            },
                            unidades: {
                                unidades: [
                                    { sigla: "TEST", nome: "Unidade de Teste", filhas: [] },
                                ],
                                unidade: { sigla: "TEST", nome: "Unidade de Teste", filhas: [] },
                                ...initialState["unidades"],
                            },
                            processos: {
                                processoDetalhe: {
                                    unidades: [
                                        {
                                            sigla: siglaUnidade,
                                            codUnidade: 10,
                                            codSubprocesso: 10,
                                            situacaoSubprocesso:
                                                SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO,
                                        },
                                    ],
                                },
                                ...initialState["processos"],
                            },
                            perfil: {
                                perfilSelecionado: "CHEFE",
                                ...initialState["perfil"],
                            },
                            analises: {
                                analisesPorSubprocesso: new Map(),
                                ...initialState["analises"],
                            },
                            subprocessos: {
                                subprocessoDetalhe: {
                                    codigo: 10,
                                    codSubprocesso: 10,
                                },
                                ...initialState["subprocessos"],
                            },
                        },
                    }),
                    router,
                ],
                stubs: {
                    AceitarMapaModal: true,
                    BModal: {
                        props: ["modelValue", "title"],
                        template: `
                    <div v-if="modelValue" class="custom-modal-stub" :data-title="title">
                        <div class="modal-title">{{ title }}</div>
                        <slot />
                        <div class="modal-footer">
                            <slot name="footer" />
                        </div>
                    </div>
                `,
                    },
                },
            },
        });

        const feedbackStore = useFeedbackStore();
        return { wrapper: context.wrapper, feedbackStore };
    };

    it("renders correctly with data from store", async () => {
        const { wrapper } = mountComponent();
        await wrapper.vm.$nextTick();

        expect(wrapper.find('[data-testid="vis-mapa__txt-competencia-descricao"]').text()).toBe(
            "Competencia 1",
        );
        expect(wrapper.find(".atividade-associada-descricao").text()).toBe(
            "Atividade 1",
        );
        expect(wrapper.find('[data-testid="txt-conhecimento-item"]').text()).toBe(
            "Conhecimento 1",
        );
    });

    it("resolves nested unit from store", async () => {
        await router.push("/processo/1/CHILD/vis-mapa");
        const { wrapper } = mountComponent(
            {
                unidades: {
                    unidades: [
                        {
                            sigla: "PARENT",
                            nome: "Parent Unit",
                            filhas: [{ sigla: "CHILD", nome: "Child Unit", filhas: [] }],
                        },
                    ],
                    unidade: { sigla: "CHILD", nome: "Child Unit", filhas: [] },
                },
                processos: {
                    processoDetalhe: {
                        unidades: [
                            {
                                sigla: "CHILD",
                                codUnidade: 11,
                                situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO,
                            },
                        ],
                    },
                },
            },
            "CHILD",
        );
        await wrapper.vm.$nextTick();

        expect(wrapper.find('[data-testid="txt-header-unidade"]').text()).toContain(
            "CHILD - Child Unit",
        );
    });

    it("shows buttons for CHEFE when MAPEAMENTO_CONCLUIDO", async () => {
        const { wrapper } = mountComponent({
            perfil: { perfilSelecionado: "CHEFE" },
            processos: {
                processoDetalhe: {
                    unidades: [
                        {
                            sigla: "TEST",
                            codUnidade: 10,
                            situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO,
                        },
                    ],
                },
            },
        });
        await wrapper.vm.$nextTick();

        expect(
            wrapper.find('[data-testid="btn-mapa-sugestoes"]').exists(),
        ).toBe(true);
        expect(wrapper.find('[data-testid="btn-mapa-validar"]').exists()).toBe(true);
    });

    it("shows buttons for GESTOR when MAPA_VALIDADO", async () => {
        const { wrapper } = mountComponent({
            perfil: { perfilSelecionado: "GESTOR" },
            processos: {
                processoDetalhe: {
                    unidades: [
                        {
                            sigla: "TEST",
                            codUnidade: 10,
                            codSubprocesso: 10,
                            situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                        },
                    ],
                },
            },
        });
        await wrapper.vm.$nextTick();

        expect(wrapper.find('[data-testid="btn-mapa-devolver"]').exists()).toBe(
            true,
        );
        expect(
            wrapper.find('[data-testid="btn-mapa-homologar-aceite"]').exists(),
        ).toBe(true);
    });

    it("opens validar modal and confirms", async () => {
        const { wrapper, feedbackStore } = mountComponent({}, "TEST", { podeValidarMapa: { value: true } });
        const store = useProcessosStore();

        await wrapper.find('[data-testid="btn-mapa-validar"]').trigger("click");
        await wrapper.vm.$nextTick();

        const confirmBtn = wrapper.find('[data-testid="btn-validar-mapa-confirmar"]');
        expect(confirmBtn.exists()).toBe(true);

        await confirmBtn.trigger("click");
        await flushPromises();

        expect(store.validarMapa).toHaveBeenCalledWith(10);
        expect(feedbackStore.show).toHaveBeenCalled();
    });

    it("opens sugestoes modal and confirms", async () => {
        const { wrapper, feedbackStore } = mountComponent({}, "TEST", { podeValidarMapa: { value: true } });
        const store = useProcessosStore();

        await wrapper
            .find('[data-testid="btn-mapa-sugestoes"]')
            .trigger("click");
        await wrapper.vm.$nextTick();

        const textarea = wrapper.find('[data-testid="inp-sugestoes-mapa-texto"]');
        await textarea.setValue("Minhas sugestões");

        const confirmBtn = wrapper.find(
            '[data-testid="btn-sugestoes-mapa-confirmar"]',
        );
        await confirmBtn.trigger("click");
        await flushPromises();

        expect(store.apresentarSugestoes).toHaveBeenCalledWith(10, {
            sugestoes: "Minhas sugestões",
        });
        expect(feedbackStore.show).toHaveBeenCalled();
    });

    it("opens devolucao modal and confirms (GESTOR)", async () => {
        const { wrapper, feedbackStore } = mountComponent({
            perfil: { perfilSelecionado: "GESTOR" },
            processos: {
                processoDetalhe: {
                    unidades: [
                        {
                            sigla: "TEST",
                            codUnidade: 10,
                            codSubprocesso: 10,
                            situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                        },
                    ],
                },
            },
        });
        const store = useSubprocessosStore();
        vi.spyOn(feedbackStore, "show");

        await wrapper.find('[data-testid="btn-mapa-devolver"]').trigger("click");
        await wrapper.vm.$nextTick();

        const textarea = wrapper.find(
            '[data-testid="inp-devolucao-mapa-obs"]',
        );
        await textarea.setValue("Ajustar X");

        const confirmBtn = wrapper.find(
            '[data-testid="btn-devolucao-mapa-confirmar"]',
        );
        await confirmBtn.trigger("click");

        expect(store.devolverRevisaoCadastro).toHaveBeenCalledWith(10, {
            observacoes: "Ajustar X",
        });
        expect(feedbackStore.show).not.toHaveBeenCalled();
    });

    it("opens aceitar modal and confirms (GESTOR)", async () => {
        const { wrapper } = mountComponent({
            perfil: { perfilSelecionado: "GESTOR" },
            processos: {
                processoDetalhe: {
                    unidades: [
                        {
                            sigla: "TEST",
                            codUnidade: 10,
                            codSubprocesso: 10,
                            situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                        },
                    ],
                },
            },
        });
        const store = useProcessosStore();

        await wrapper
            .find('[data-testid="btn-mapa-homologar-aceite"]')
            .trigger("click");
        await wrapper.vm.$nextTick();

        const modal = wrapper.findComponent(AceitarMapaModal);
        expect(modal.props("mostrarModal")).toBe(true);

        modal.vm.$emit("confirmar-aceitacao", "Obs aceite");

        expect(store.aceitarValidacao).toHaveBeenCalledWith(10);
    });

    it("confirms homologacao (ADMIN)", async () => {
        const { wrapper } = mountComponent({
            perfil: { perfilSelecionado: "ADMIN" },
            processos: {
                processoDetalhe: {
                    tipo: TipoProcesso.REVISAO,
                    unidades: [
                        {
                            sigla: "TEST",
                            codUnidade: 10,
                            codSubprocesso: 10,
                            situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                        },
                    ],
                },
            },
        }, "TEST", { podeHomologarMapa: { value: true } });
        const store = useSubprocessosStore();

        await wrapper
            .find('[data-testid="btn-mapa-homologar-aceite"]')
            .trigger("click");
        await wrapper.vm.$nextTick();

        const modal = wrapper.findComponent(AceitarMapaModal);
        modal.vm.$emit("confirmar-aceitacao", "Obs homolog");

        expect(store.homologarRevisaoCadastro).toHaveBeenCalledWith(10, {
            observacoes: "Obs homolog",
        });
    });

    it("shows historico de analise", async () => {
        const { wrapper } = mountComponent({
            perfil: { perfilSelecionado: "GESTOR" }, // GESTOR tem podeAnalisar
        });

        await wrapper.vm.$nextTick();
        await flushPromises();

        // Para GESTOR, o botão é btn-mapa-historico-gestor
        const btn = wrapper.find('[data-testid="btn-mapa-historico-gestor"]');
        expect(btn.exists()).toBe(true);

        // Verificar que o modal pode ser aberto (teste básico)
        await btn.trigger("click");
        await flushPromises();

        // O modal abre mesmo sem análises (mostra mensagem "Nenhuma análise")
        expect(wrapper.vm.mostrarModalHistorico).toBe(true);
    });

    it("view suggestions (GESTOR)", async () => {
        const { wrapper } = mountComponent({
            perfil: { perfilSelecionado: "GESTOR" },
            processos: {
                processoDetalhe: {
                    unidades: [
                        {
                            sigla: "TEST",
                            codUnidade: 10,
                            codSubprocesso: 10,
                            situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES,
                        },
                    ],
                },
            },
        });
        await wrapper.vm.$nextTick();

        const btn = wrapper.find('[data-testid="btn-mapa-ver-sugestoes"]');
        expect(btn.exists()).toBe(true);

        await btn.trigger("click");
        await wrapper.vm.$nextTick();

        expect(wrapper.text()).toContain("Sugestões registradas");
    });

    it("does not show content if unit not found", async () => {
        const { wrapper } = mountComponent({
            unidades: {
                unidades: [], // empty list
                unidade: null
            }
        });
        await wrapper.vm.$nextTick();
        expect(wrapper.text()).toContain("Unidade não encontrada");
    });

    it("shows empty state if no map", async () => {
        const { wrapper } = mountComponent({
            mapas: {
                mapaVisualizacao: { competencias: [] } // empty
            }
        });
        await wrapper.vm.$nextTick();
        expect(wrapper.text()).toContain("Nenhuma competência cadastrada");
    });

    it("confirms homologacao (ADMIN) for Mapeamento", async () => {
        const { wrapper } = mountComponent({
            perfil: { perfilSelecionado: "ADMIN" },
            processos: {
                processoDetalhe: {
                    tipo: TipoProcesso.MAPEAMENTO,
                    unidades: [
                        {
                            sigla: "TEST",
                            codUnidade: 10,
                            codSubprocesso: 10,
                            situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                        },
                    ],
                },
            },
        });
        const store = useProcessosStore();

        await wrapper
            .find('[data-testid="btn-mapa-homologar-aceite"]')
            .trigger("click");
        await wrapper.vm.$nextTick();

        const modal = wrapper.findComponent(AceitarMapaModal);
        modal.vm.$emit("confirmar-aceitacao", "Obs homolog");

        expect(store.homologarValidacao).toHaveBeenCalledWith(10);
    });

    it("handles error in confirmarAceitacao", async () => {
        const { wrapper, feedbackStore } = mountComponent({
            perfil: { perfilSelecionado: "GESTOR" },
            processos: {
                processoDetalhe: {
                    unidades: [
                        {
                            sigla: "TEST",
                            codUnidade: 10,
                            codSubprocesso: 10,
                            situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                        },
                    ],
                },
            },
        });
        const store = useProcessosStore();
        (store.aceitarValidacao as any).mockRejectedValue(new Error("Fail"));
        vi.spyOn(feedbackStore, "show");

        await wrapper
            .find('[data-testid="btn-mapa-homologar-aceite"]')
            .trigger("click");
        await wrapper.vm.$nextTick();

        const modal = wrapper.findComponent(AceitarMapaModal);
        modal.vm.$emit("confirmar-aceitacao", "Obs");
        await flushPromises();

        expect(feedbackStore.show).toHaveBeenCalledWith("Erro", "Erro ao realizar a operação.", "danger");
    });

    it("handles error in confirmarDevolucao", async () => {
        const { wrapper, feedbackStore } = mountComponent({
            perfil: { perfilSelecionado: "GESTOR" },
            processos: {
                processoDetalhe: {
                    unidades: [
                        {
                            sigla: "TEST",
                            codUnidade: 10,
                            codSubprocesso: 10,
                            situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                        },
                    ],
                },
            },
        });
        const store = useSubprocessosStore();
        (store.devolverRevisaoCadastro as any).mockRejectedValue(new Error("Fail"));
        vi.spyOn(feedbackStore, "show");

        await wrapper.find('[data-testid="btn-mapa-devolver"]').trigger("click");
        await wrapper.vm.$nextTick();

        await wrapper.find('[data-testid="btn-devolucao-mapa-confirmar"]').trigger("click");
        await flushPromises();

        expect(feedbackStore.show).toHaveBeenCalledWith("Erro", "Erro ao devolver.", "danger");
    });

    it("handles error in confirmarValidacao", async () => {
        const { wrapper, feedbackStore } = mountComponent({}, "TEST", { podeValidarMapa: { value: true } });
        const store = useProcessosStore();
        (store.validarMapa as any).mockRejectedValue(new Error("Fail"));
        vi.spyOn(feedbackStore, "show");

        await wrapper.find('[data-testid="btn-mapa-validar"]').trigger("click");
        await wrapper.vm.$nextTick();
        await wrapper.find('[data-testid="btn-validar-mapa-confirmar"]').trigger("click");
        await flushPromises();

        expect(feedbackStore.show).toHaveBeenCalledWith("Erro ao validar mapa", expect.any(String), "danger");
    });

    it("handles error in confirmarSugestoes", async () => {
        const { wrapper, feedbackStore } = mountComponent({
            perfil: { perfilSelecionado: "CHEFE" }
        }, "TEST", { podeValidarMapa: { value: true } });
        const store = useProcessosStore();
        (store.apresentarSugestoes as any).mockRejectedValue(new Error("Fail"));
        vi.spyOn(feedbackStore, "show");
        await wrapper.find('[data-testid="btn-mapa-sugestoes"]').trigger("click");
        await wrapper.vm.$nextTick();
        await wrapper.find('[data-testid="btn-sugestoes-mapa-confirmar"]').trigger("click");
        await flushPromises();

        expect(feedbackStore.show).toHaveBeenCalledWith("Erro ao apresentar sugestões", expect.any(String), "danger");
    });

    it("closes ver sugestoes modal", async () => {
        const { wrapper } = mountComponent({
            perfil: { perfilSelecionado: "GESTOR" },
        }, "TEST", { podeAnalisar: { value: true }, podeApresentarSugestoes: { value: true }, podeAceitarMapa: { value: true } });
        await flushPromises();

        await wrapper.find('[data-testid="btn-mapa-ver-sugestoes"]').trigger("click");
        expect(wrapper.vm.mostrarModalVerSugestoes).toBe(true);

        await wrapper.find('[data-testid="btn-ver-sugestoes-mapa-fechar"]').trigger("click");
        expect(wrapper.vm.mostrarModalVerSugestoes).toBe(false);
    });

    it("closes historico modal", async () => {
        const { wrapper } = mountComponent({
            perfil: { perfilSelecionado: "GESTOR" }
        }, "TEST", { podeAceitarMapa: { value: true } });
        await flushPromises();

        await wrapper.find('[data-testid="btn-mapa-historico-gestor"]').trigger("click");
        await flushPromises();
        expect(wrapper.vm.mostrarModalHistorico).toBe(true);

        const modal = wrapper.findComponent({ name: 'HistoricoAnaliseModal' });
        await modal.vm.$emit('fechar');
        expect(wrapper.vm.mostrarModalHistorico).toBe(false);
    });
    it("triggers focus on shown in sugestoes modal", async () => {
        const { wrapper } = mountComponent({}, "TEST", { podeValidarMapa: { value: true } });
        await wrapper.vm.$nextTick();
        await wrapper.find('[data-testid="btn-mapa-sugestoes"]').trigger("click");
        
        const modal = wrapper.findAllComponents({ name: 'ModalConfirmacao' }).find(c => c.props('titulo') === 'Apresentar Sugestões');
        await modal?.vm.$emit('shown');
    });

    it("triggers focus on shown in devolucao modal", async () => {
        const { wrapper } = mountComponent({
            perfil: { perfilSelecionado: "GESTOR" }
        }, "TEST", { podeDevolverMapa: { value: true } });
        await flushPromises();
        await wrapper.vm.$nextTick();

        await wrapper.find('[data-testid="btn-mapa-devolver"]').trigger("click");
        
        const modal = wrapper.findAllComponents({ name: 'ModalConfirmacao' }).find(c => c.props('titulo') === 'Devolução');
        await modal?.vm.$emit('shown');
    });
});
