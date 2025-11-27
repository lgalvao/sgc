import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {computed, defineComponent} from "vue";
import * as usePerfilModule from "@/composables/usePerfil";
// Import services to mock
import * as mapaService from "@/services/mapaService";
import * as subprocessoService from "@/services/subprocessoService";
import * as unidadesService from "@/services/unidadesService";
import {useAtividadesStore} from "@/stores/atividades";
import {useMapasStore} from "@/stores/mapas";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useUnidadesStore} from "@/stores/unidades";
import {Perfil} from "@/types/tipos";
import CadMapa from "@/views/CadMapa.vue";

const { pushMock } = vi.hoisted(() => {
  return { pushMock: vi.fn() };
});

vi.mock("vue-router", () => ({
  useRoute: () => ({
    params: {
      codProcesso: "1",
      siglaUnidade: "TESTE",
    },
  }),
  useRouter: () => ({
    push: pushMock,
    currentRoute: { value: { path: "/" } },
  }),
  createRouter: () => ({
    push: pushMock,
    afterEach: vi.fn(),
    beforeEach: vi.fn(),
  }),
  createWebHistory: vi.fn(),
  createMemoryHistory: vi.fn(),
}));

vi.mock("@/composables/usePerfil", () => ({
  usePerfil: vi.fn(),
}));

vi.mock("@/services/mapaService", () => ({
  obterMapaCompleto: vi.fn(),
  obterMapaVisualizacao: vi.fn(),
  disponibilizarMapa: vi.fn(),
}));

vi.mock("@/services/subprocessoService", () => ({
  buscarSubprocessoPorProcessoEUnidade: vi.fn(),
  fetchSubprocessoDetalhe: vi.fn(),
  adicionarCompetencia: vi.fn(),
  atualizarCompetencia: vi.fn(),
  removerCompetencia: vi.fn(),
}));

vi.mock("@/services/unidadesService", () => ({
  buscarUnidadePorSigla: vi.fn(),
}));

// Mock Bootstrap components
// Using defineComponent for Checkbox to handle v-model properly if needed,
// but simple stubs are usually enough if we rely on props.
const BFormCheckbox = defineComponent({
  name: "BFormCheckbox",
  props: {
    modelValue: { type: [Boolean, Array], default: false },
    value: { type: [String, Number, Boolean, Object], default: null },
  },
  emits: ["update:modelValue"],
  setup(props, { emit }) {
    const isChecked = computed(() => {
      if (Array.isArray(props.modelValue)) {
        return props.modelValue.includes(props.value);
      }
      return props.modelValue;
    });
    const handleChange = (e: any) => {
      let newValue = props.modelValue;
      if (Array.isArray(props.modelValue)) {
        if (e.target.checked) {
          newValue = [...props.modelValue, props.value];
        } else {
          newValue = props.modelValue.filter((v: any) => v !== props.value);
        }
      } else {
        newValue = e.target.checked;
      }
      emit("update:modelValue", newValue);
    };
    return { isChecked, handleChange };
  },
  template: `
    <div class="form-check">
      <input type="checkbox" class="form-check-input" :checked="isChecked" @change="handleChange" data-testid="atividade-checkbox" />
      <label class="form-check-label"><slot /></label>
    </div>
  `,
});

// Other simple stubs
const BModalStub = {
  name: "BModal",
  props: ["modelValue", "title"],
  template: `
        <div v-if="modelValue" class="modal-stub" :aria-label="title" data-testid="modal-container">
            <div class="modal-title">{{ title }}</div>
            <slot />
            <div class="modal-footer">
                <slot name="footer" />
            </div>
        </div>
    `,
  emits: ["update:modelValue", "ok", "hidden"],
};

describe("CadMapa.vue", () => {
  let wrapper: any;

  const mockAtividades = [
    { codigo: 101, descricao: "Atividade 1", conhecimentos: [] },
    {
      codigo: 102,
      descricao: "Atividade 2",
      conhecimentos: [{ descricao: "Java" }],
    },
  ];

  const mockCompetencias = [
    { codigo: 10, descricao: "Competencia A", atividadesAssociadas: [101] },
  ];

  const mockMapaCompleto = {
    codigo: 1,
    subprocessoCodigo: 123,
    competencias: [...mockCompetencias],
    situacao: "EM_ANDAMENTO",
  };



  function createWrapper(customState = {}) {
    vi.mocked(usePerfilModule.usePerfil).mockReturnValue({
      perfilSelecionado: { value: Perfil.CHEFE },
      servidorLogado: { value: null },
      unidadeSelecionada: { value: null },
      getPerfisDoServidor: vi.fn(),
    } as any);

    const wrapper = mount(CadMapa, {
      global: {
        plugins: [
          createTestingPinia({
            stubActions: false,
            initialState: {
              perfil: {
                perfilSelecionado: Perfil.CHEFE,
                unidadeSelecionada: 1,
                perfisUnidades: [
                  {
                    perfil: Perfil.CHEFE,
                    unidade: { codigo: 1, sigla: "TESTE" },
                  },
                ],
              },
              ...customState,
            },
          }),
        ],
        stubs: {
          ImpactoMapaModal: true,
          BModal: BModalStub,
          BButton: {
            name: "BButton",
            template: '<button type="button"><slot /></button>',
          },
          BContainer: { name: "BContainer", template: "<div><slot /></div>" },
          BCard: {
            name: "BCard",
            template: '<div class="card"><slot /></div>',
          },
          BCardBody: {
            name: "BCardBody",
            template: '<div class="card-body"><slot /></div>',
          },
          BFormInput: {
            name: "BFormInput",
            props: ["modelValue"],
            template:
              '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            emits: ["update:modelValue"],
          },
          BFormTextarea: {
            name: "BFormTextarea",
            props: ["modelValue"],
            template:
              '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            emits: ["update:modelValue"],
          },
          BFormCheckbox: BFormCheckbox,
          BAlert: {
            name: "BAlert",
            template: '<div role="alert"><slot /></div>',
          },
        },
        directives: {
          "b-tooltip": {},
        },
      },
      attachTo: document.body,
    });

    const mapasStore = useMapasStore();
    const atividadesStore = useAtividadesStore();
    const subprocessosStore = useSubprocessosStore();
    const unidadesStore = useUnidadesStore();

    return {
      wrapper,
      mapasStore,
      atividadesStore,
      subprocessosStore,
      unidadesStore,
    };
  }

  beforeEach(() => {
    vi.clearAllMocks();

    // Default mocks
    vi.mocked(unidadesService.buscarUnidadePorSigla).mockResolvedValue({
      codigo: 1,
      sigla: "TESTE",
      nome: "Teste",
    } as any);
    vi.mocked(
      subprocessoService.buscarSubprocessoPorProcessoEUnidade,
    ).mockResolvedValue({ codigo: 123 } as any);
    vi.mocked(subprocessoService.fetchSubprocessoDetalhe).mockResolvedValue({
      permissoes: { podeVisualizarImpacto: true },
    } as any);
    vi.mocked(mapaService.obterMapaCompleto).mockResolvedValue(
      mockMapaCompleto as any,
    );
    vi.mocked(mapaService.obterMapaVisualizacao).mockResolvedValue({
      competencias: [{ atividades: mockAtividades }],
    } as any);
  });

  afterEach(() => {
    wrapper?.unmount();
  });

  it("deve carregar dados no mount", async () => {
    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    expect(
      subprocessoService.buscarSubprocessoPorProcessoEUnidade,
    ).toHaveBeenCalledWith(1, "TESTE");
    expect(mapaService.obterMapaCompleto).toHaveBeenCalledWith(123);
    expect(mapaService.obterMapaVisualizacao).toHaveBeenCalledWith(123); // via atividadesStore

    expect(wrapper.text()).toContain("TESTE - Teste");

    expect(wrapper.text()).toContain("Competencia A");
  });

  it("deve abrir modal e criar nova competencia", async () => {
    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    await wrapper
      .find('[data-testid="btn-abrir-criar-competencia"]')
      .trigger("click");
    expect(
      wrapper.find('[data-testid="criar-competencia-modal"]').exists(),
    ).toBe(true);

    const textarea = wrapper.find('[data-testid="input-nova-competencia"]');
    await textarea.setValue("Nova Competencia Teste");

    // Checkbox interaction
    // Need to check if activities are rendered in modal.
    // Modal uses `atividades` computed property which comes from store.
    // `mockMapaVisualizacao` provided activities.

    // Select first activity (ID 101) using the checkbox stub
    const inputs = wrapper.findAll('input[type="checkbox"]');
    if (inputs.length > 0) {
      await inputs[0].setValue(true);
    }

    vi.mocked(subprocessoService.adicionarCompetencia).mockResolvedValue({
      ...mockMapaCompleto,
    } as any);

    await wrapper
      .find('[data-testid="btn-criar-competencia"]')
      .trigger("click");

    expect(subprocessoService.adicionarCompetencia).toHaveBeenCalledWith(
      123,
      expect.objectContaining({
        descricao: "Nova Competencia Teste",
        atividadesAssociadas: [101],
      }),
    );
  });

  it("deve editar uma competencia existente", async () => {
    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    await wrapper
      .find('[data-testid="btn-editar-competencia"]')
      .trigger("click");
    expect(
      wrapper.find('[data-testid="criar-competencia-modal"]').exists(),
    ).toBe(true);

    const textarea = wrapper.find('[data-testid="input-nova-competencia"]');
    await textarea.setValue("Competencia A Editada");

    vi.mocked(subprocessoService.atualizarCompetencia).mockResolvedValue({
      ...mockMapaCompleto,
    } as any);

    await wrapper
      .find('[data-testid="btn-criar-competencia"]')
      .trigger("click");

    expect(subprocessoService.atualizarCompetencia).toHaveBeenCalledWith(
      123,
      expect.objectContaining({
        codigo: 10,
        descricao: "Competencia A Editada",
      }),
    );
  });

  it("deve excluir uma competencia", async () => {
    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    await wrapper
      .find('[data-testid="btn-excluir-competencia"]')
      .trigger("click");

    // Find the modal by data-testid
    const deleteModal = wrapper.findComponent(
      '[data-testid="excluir-competencia-modal"]',
    );

    expect(deleteModal.exists()).toBe(true);
    // The modal should be visible now
    expect(deleteModal.props("modelValue")).toBe(true);

    vi.mocked(subprocessoService.removerCompetencia).mockResolvedValue({
      ...mockMapaCompleto,
    } as any);

    await deleteModal.vm.$emit("ok");

    expect(subprocessoService.removerCompetencia).toHaveBeenCalledWith(123, 10);
  });

  it("deve remover atividade associada", async () => {
    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    const removeBtn = wrapper.find(".botao-acao-inline");
    await removeBtn.trigger("click");

    vi.mocked(subprocessoService.atualizarCompetencia).mockResolvedValue({
      ...mockMapaCompleto,
    } as any);

    expect(subprocessoService.atualizarCompetencia).toHaveBeenCalledWith(
      123,
      expect.objectContaining({
        codigo: 10,
        atividadesAssociadas: [],
      }),
    );
  });

  it("deve abrir modal de disponibilizar e enviar", async () => {
    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    await wrapper
      .find('[data-testid="btn-disponibilizar-page"]')
      .trigger("click");

    const modal = wrapper.find('[data-testid="disponibilizar-modal"]');
    expect(modal.exists()).toBe(true);

    await wrapper
      .find('[data-testid="input-data-limite"]')
      .setValue("2023-12-31");
    await wrapper
      .find('[data-testid="input-observacoes-disponibilizacao"]')
      .setValue("Obs");

    vi.mocked(mapaService.disponibilizarMapa).mockResolvedValue();

    await wrapper.find('[data-testid="btn-disponibilizar"]').trigger("click");

    expect(mapaService.disponibilizarMapa).toHaveBeenCalledWith(123, {
      dataLimite: "2023-12-31",
      observacoes: "Obs",
    });
  });

  it("deve abrir modal de impacto", async () => {
    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    await wrapper.find('[data-testid="impactos-mapa-button"]').trigger("click");

    const impactoModal = wrapper.findComponent({ name: "ImpactoMapaModal" });
    expect(impactoModal.props("mostrar")).toBe(true);
  });

  it('deve mostrar o botão "Impacto no mapa" se tiver permissão', async () => {
    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    expect(wrapper.find('[data-testid="impactos-mapa-button"]').exists()).toBe(true);
  });

  it('não deve mostrar o botão "Impacto no mapa" se não tiver permissão', async () => {
    vi.mocked(subprocessoService.fetchSubprocessoDetalhe).mockResolvedValue({
      permissoes: { podeVisualizarImpacto: false },
    } as any);

    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    expect(wrapper.find('[data-testid="impactos-mapa-button"]').exists()).toBe(false);
  });
});
