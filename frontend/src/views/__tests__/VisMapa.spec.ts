import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";

import {beforeEach, describe, expect, it, vi} from "vitest";
import {createMemoryHistory, createRouter} from "vue-router";
import AceitarMapaModal from "@/components/AceitarMapaModal.vue";
import {useProcessosStore} from "@/stores/processos";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useFeedbackStore} from "@/stores/feedback"; // Import feedback store
import {SituacaoSubprocesso} from "@/types/tipos";
import VisMapa from "../VisMapa.vue";

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
  beforeEach(async () => {
    vi.clearAllMocks();
    await router.push("/processo/1/TEST/vis-mapa");
    await router.isReady();
  });

  const mountComponent = (initialState = {}, siglaUnidade = "TEST") => {
    // If testing nested units, we might need to push a different route or just rely on the fact that component reads param.
    // Since router is global in this test file setup, we should push before mount if sigla changes.

    const wrapper = mount(VisMapa, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
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
                        SituacaoSubprocesso.MAPA_DISPONIBILIZADO,
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
    return { wrapper, feedbackStore };
  };

  it("renders correctly with data from store", async () => {
    const { wrapper } = mountComponent();
    await wrapper.vm.$nextTick();

    expect(wrapper.find('[data-testid="competencia-descricao"]').text()).toBe(
      "Competencia 1",
    );
    expect(wrapper.find(".atividade-associada-descricao").text()).toBe(
      "Atividade 1",
    );
    expect(wrapper.find('[data-testid="conhecimento-item"]').text()).toBe(
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
        },
        processos: {
          processoDetalhe: {
            unidades: [
              {
                sigla: "CHILD",
                codUnidade: 11,
                situacaoSubprocesso: SituacaoSubprocesso.MAPEAMENTO_CONCLUIDO,
              },
            ],
          },
        },
      },
      "CHILD",
    );
    await wrapper.vm.$nextTick();

    expect(wrapper.find('[data-testid="unidade-info"]').text()).toContain(
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
              situacaoSubprocesso: SituacaoSubprocesso.MAPA_DISPONIBILIZADO,
            },
          ],
        },
      },
    });
    await wrapper.vm.$nextTick();

    expect(
      wrapper.find('[data-testid="apresentar-sugestoes-btn"]').exists(),
    ).toBe(true);
    expect(wrapper.find('[data-testid="validar-btn"]').exists()).toBe(true);
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
              situacaoSubprocesso: SituacaoSubprocesso.MAPA_VALIDADO,
            },
          ],
        },
      },
    });
    await wrapper.vm.$nextTick();

    expect(wrapper.find('[data-testid="devolver-ajustes-btn"]').exists()).toBe(
      true,
    );
    expect(
      wrapper.find('[data-testid="btn-registrar-aceite-homologar"]').exists(),
    ).toBe(true);
  });

  it("opens validar modal and confirms", async () => {
    const { wrapper, feedbackStore } = mountComponent();
    const store = useProcessosStore();
    vi.spyOn(feedbackStore, "show");

    await wrapper.find('[data-testid="validar-btn"]').trigger("click");
    await wrapper.vm.$nextTick();

    const confirmBtn = wrapper.find('[data-testid="modal-validar-confirmar"]');
    expect(confirmBtn.exists()).toBe(true);

    await confirmBtn.trigger("click");

    expect(store.validarMapa).toHaveBeenCalledWith(10);
    expect(feedbackStore.show).toHaveBeenCalled();
  });

  it("opens sugestoes modal and confirms", async () => {
    const { wrapper, feedbackStore } = mountComponent();
    const store = useProcessosStore();
    vi.spyOn(feedbackStore, "show");

    await wrapper
      .find('[data-testid="apresentar-sugestoes-btn"]')
      .trigger("click");
    await wrapper.vm.$nextTick();

    const textarea = wrapper.find('[data-testid="sugestoes-textarea"]');
    await textarea.setValue("Minhas sugestões");

    const confirmBtn = wrapper.find(
      '[data-testid="modal-apresentar-sugestoes-confirmar"]',
    );
    await confirmBtn.trigger("click");

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
              situacaoSubprocesso: SituacaoSubprocesso.MAPA_VALIDADO,
            },
          ],
        },
      },
    });
    const store = useSubprocessosStore();
    vi.spyOn(feedbackStore, "show");

    await wrapper.find('[data-testid="devolver-ajustes-btn"]').trigger("click");
    await wrapper.vm.$nextTick();

    const textarea = wrapper.find(
      '[data-testid="observacao-devolucao-textarea"]',
    );
    await textarea.setValue("Ajustar X");

    const confirmBtn = wrapper.find(
      '[data-testid="modal-devolucao-confirmar"]',
    );
    await confirmBtn.trigger("click");

    expect(store.devolverRevisaoCadastro).toHaveBeenCalledWith(10, {
      observacoes: "Ajustar X",
    });
    expect(feedbackStore.show).not.toHaveBeenCalled(); // Should not show error on success (and success toast is not called in this path in component, only navigation)
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
              situacaoSubprocesso: SituacaoSubprocesso.MAPA_VALIDADO,
            },
          ],
        },
      },
    });
    const store = useSubprocessosStore();

    await wrapper
      .find('[data-testid="btn-registrar-aceite-homologar"]')
      .trigger("click");
    await wrapper.vm.$nextTick();

    const modal = wrapper.findComponent(AceitarMapaModal);
    expect(modal.props("mostrarModal")).toBe(true);

    await modal.vm.$emit("confirmar-aceitacao", "Obs aceite");

    expect(store.aceitarRevisaoCadastro).toHaveBeenCalledWith(10, {
      observacoes: "Obs aceite",
    });
  });

  it("confirms homologacao (ADMIN)", async () => {
    const { wrapper } = mountComponent({
      perfil: { perfilSelecionado: "ADMIN" },
      processos: {
        processoDetalhe: {
          unidades: [
            {
              sigla: "TEST",
              codUnidade: 10,
              codSubprocesso: 10,
              situacaoSubprocesso: SituacaoSubprocesso.MAPA_VALIDADO,
            },
          ],
        },
      },
    });
    const store = useSubprocessosStore();

    await wrapper
      .find('[data-testid="btn-registrar-aceite-homologar"]')
      .trigger("click");
    await wrapper.vm.$nextTick();

    const modal = wrapper.findComponent(AceitarMapaModal);
    await modal.vm.$emit("confirmar-aceitacao", "Obs homolog");

    expect(store.homologarRevisaoCadastro).toHaveBeenCalledWith(10, {
      observacoes: "Obs homolog",
    });
  });

  it("shows historico de analise", async () => {
    const analisesData = [
      {
        codigo: 1,
        dataHora: "2023-01-01T12:00:00",
        unidadeSigla: "UNIT",
        resultado: "APROVADO",
        observacoes: "Bom",
      },
    ];

    const { wrapper } = mountComponent({
      analises: {
        analisesPorSubprocesso: new Map([[10, analisesData]]),
      },
    });

    await wrapper.vm.$nextTick();
    await flushPromises();

    const btn = wrapper.find('[data-testid="historico-analise-btn"]');
    expect(btn.exists()).toBe(true);

    await btn.trigger("click");
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain("Data/Hora");
    expect(wrapper.text()).toContain("APROVADO");
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
              situacaoSubprocesso: SituacaoSubprocesso.AGUARDANDO_AJUSTES_MAPA,
            },
          ],
        },
      },
    });
    await wrapper.vm.$nextTick();

    const btn = wrapper.find('[data-testid="ver-sugestoes-btn"]');
    expect(btn.exists()).toBe(true);

    await btn.trigger("click");
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain("Sugestões registradas");
  });
});

