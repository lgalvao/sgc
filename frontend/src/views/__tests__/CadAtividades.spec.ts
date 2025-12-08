import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {BFormInput} from "bootstrap-vue-next";
import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {computed} from "vue";
import ImportarAtividadesModal from "@/components/ImportarAtividadesModal.vue";
import * as usePerfilModule from "@/composables/usePerfil";
import * as analiseService from "@/services/analiseService";
// Import services to mock/spy
import * as atividadeService from "@/services/atividadeService";
import * as cadastroService from "@/services/cadastroService";
import * as mapaService from "@/services/mapaService";
import * as processoService from "@/services/processoService";
import * as unidadesService from "@/services/unidadesService";
import {useAnalisesStore} from "@/stores/analises";
import {useAtividadesStore} from "@/stores/atividades";
import {useProcessosStore} from "@/stores/processos";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useFeedbackStore} from "@/stores/feedback"; // Import feedback store
import {Perfil, SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import CadAtividades from "@/views/CadAtividades.vue";

const pushMock = vi.fn();

vi.mock("vue-router", () => ({
  useRouter: () => ({
    push: pushMock,
  }),
  createRouter: () => ({
    beforeEach: vi.fn(),
    afterEach: vi.fn(),
  }),
  createWebHistory: () => ({}),
}));

// Mock usePerfil
vi.mock("@/composables/usePerfil", () => ({
  usePerfil: vi.fn(),
}));

// Mock services
vi.mock("@/services/atividadeService", () => ({
  criarAtividade: vi.fn(),
  excluirAtividade: vi.fn(),
  criarConhecimento: vi.fn(),
  excluirConhecimento: vi.fn(),
  atualizarAtividade: vi.fn(),
  atualizarConhecimento: vi.fn(),
}));

vi.mock("@/services/mapaService", () => ({
  obterMapaVisualizacao: vi.fn(),
}));

vi.mock("@/services/cadastroService", () => ({
  disponibilizarCadastro: vi.fn(),
  disponibilizarRevisaoCadastro: vi.fn(),
}));

vi.mock("@/services/subprocessoService", () => ({
  importarAtividades: vi.fn(),
  adicionarCompetencia: vi.fn(),
  atualizarCompetencia: vi.fn(),
  removerCompetencia: vi.fn(),
}));

vi.mock("@/services/processoService", () => ({
  obterDetalhesProcesso: vi.fn(),
}));

vi.mock("@/services/unidadesService", () => ({
  buscarUnidadePorSigla: vi.fn(),
}));

vi.mock("@/services/analiseService", () => ({
  listarAnalisesCadastro: vi.fn(),
}));

const mockAtividades = [
  {
    codigo: 1,
    descricao: "Atividade 1",
    conhecimentos: [
      { id: 101, descricao: "Conhecimento 1.1" },
      { id: 102, descricao: "Conhecimento 1.2" },
    ],
  },
  {
    codigo: 2,
    descricao: "Atividade 2",
    conhecimentos: [],
  },
];

// Helper to generate map structure
const mockMapaVisualizacao = (atividades = []) => ({
  subprocessoCodigo: 123,
  competencias: [
    {
      codigo: 10,
      descricao: "Competencia Geral",
      atividades: atividades,
    },
  ],
});

describe("CadAtividades.vue", () => {
  let wrapper: any;

  function createWrapper(isRevisao = false, customState = {}) {
    // Setup usePerfil mock per test
    vi.mocked(usePerfilModule.usePerfil).mockReturnValue({
      perfilSelecionado: computed(() => Perfil.CHEFE),
      servidorLogado: computed(() => null),
      unidadeSelecionada: computed(() => null),
    } as any);

    const wrapper = mount(CadAtividades, {
      props: {
        codProcesso: 1,
        sigla: "TESTE",
      },
      global: {
        plugins: [
          createTestingPinia({
            stubActions: false, // Allow store actions to run and call mocked services
            initialState: {
              processos: {
                processoDetalhe: {
                  codigo: 1,
                  tipo: isRevisao
                    ? TipoProcesso.REVISAO
                    : TipoProcesso.MAPEAMENTO,
                  unidades: [
                    {
                      codUnidade: 123,
                      codSubprocesso: 123,
                      mapaCodigo: 456,
                      sigla: "TESTE",
                      situacaoSubprocesso: isRevisao
                        ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
                        : SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO,
                    },
                  ],
                },
              },
              unidades: {
                unidade: {
                  codigo: 1,
                  nome: "Unidade de Teste",
                  sigla: "TESTE",
                },
              },
              atividades: {
                atividadesPorSubprocesso: new Map(),
              },
              analises: {
                analisesPorSubprocesso: new Map(),
              },
              ...customState,
            },
          }),
        ],
        stubs: {
          ImportarAtividadesModal: true,
          BModal: {
            name: "BModal",
            template: `
                   <div v-if="modelValue" class="b-modal-stub" :aria-label="title">
                     <div class="stub-title">{{ title }}</div>
                     <slot />
                     <slot name="footer" />
                   </div>
                `,
            props: ["modelValue", "title"],
            emits: ["update:modelValue"],
          },
        },
      },
      attachTo: document.body,
    });

    const atividadesStore = useAtividadesStore();
    const processosStore = useProcessosStore();
    const subprocessosStore = useSubprocessosStore();
    const analisesStore = useAnalisesStore();
    const feedbackStore = useFeedbackStore(); // Get feedback store

    return {
      wrapper,
      atividadesStore,
      processosStore,
      subprocessosStore,
      analisesStore,
      feedbackStore,
    };
  }

  beforeEach(async () => {
    vi.clearAllMocks();
    window.confirm = vi.fn(() => true);

    // Default mocks
    vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue({
      codigo: 1,
      tipo: TipoProcesso.MAPEAMENTO,
      unidades: [
        {
          codUnidade: 123,
          codSubprocesso: 123,
          mapaCodigo: 456,
          sigla: "TESTE",
          situacaoSubprocesso: SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO,
        },
      ],
    } as any);
    vi.mocked(unidadesService.buscarUnidadePorSigla).mockResolvedValue({
      codigo: 1,
      sigla: "TESTE",
      nome: "Teste",
    } as any);
    vi.mocked(mapaService.obterMapaVisualizacao).mockResolvedValue(
      mockMapaVisualizacao([]) as any,
    );
    vi.mocked(analiseService.listarAnalisesCadastro).mockResolvedValue([]);
  });

  afterEach(() => {
    wrapper?.unmount();
  });

  it("deve carregar atividades no mount", async () => {
    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    expect(mapaService.obterMapaVisualizacao).toHaveBeenCalledWith(123);
  });

  it("deve adicionar uma atividade", async () => {
    vi.mocked(mapaService.obterMapaVisualizacao).mockResolvedValue(
      mockMapaVisualizacao([...mockAtividades] as any) as any,
    );
    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    const inputWrapper = wrapper.findComponent(BFormInput);
    const nativeInput = inputWrapper.find("input");
    await nativeInput.setValue("Nova Atividade");

    vi.mocked(atividadeService.criarAtividade).mockResolvedValue({
      codigo: 99,
      descricao: "Nova Atividade",
      conhecimentos: [],
    } as any);

    await wrapper
      .find('[data-testid="form-nova-atividade"]')
      .trigger("submit.prevent");

    expect(atividadeService.criarAtividade).toHaveBeenCalledWith(
      { descricao: "Nova Atividade" },
      456,
    );
  });

  it("deve remover uma atividade", async () => {
    // Setup mock BEFORE wrapper creation (or mock the fetch response)
    vi.mocked(mapaService.obterMapaVisualizacao).mockResolvedValue(
      mockMapaVisualizacao([...mockAtividades] as any) as any,
    );

    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    vi.mocked(atividadeService.excluirAtividade).mockResolvedValue();

    await wrapper
      .find('[data-testid="btn-remover-atividade"]')
      .trigger("click");
    expect(window.confirm).toHaveBeenCalled();
    expect(atividadeService.excluirAtividade).toHaveBeenCalledWith(1);
  });

  it("deve adicionar um conhecimento", async () => {
    vi.mocked(mapaService.obterMapaVisualizacao).mockResolvedValue(
      mockMapaVisualizacao([...mockAtividades] as any) as any,
    );

    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    const form = wrapper.find('[data-testid="form-novo-conhecimento"]');
    const inputWrapper = form.findComponent(BFormInput);
    const nativeInput = inputWrapper.find("input");
    await nativeInput.setValue("Novo Conhecimento");

    vi.mocked(atividadeService.criarConhecimento).mockResolvedValue({
      id: 99,
      descricao: "Novo Conhecimento",
    } as any);

    await form.trigger("submit.prevent");

    expect(atividadeService.criarConhecimento).toHaveBeenCalledWith(1, {
      descricao: "Novo Conhecimento",
    });
  });

  it("deve remover um conhecimento", async () => {
    vi.mocked(mapaService.obterMapaVisualizacao).mockResolvedValue(
      mockMapaVisualizacao([...mockAtividades] as any) as any,
    );

    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    vi.mocked(atividadeService.excluirConhecimento).mockResolvedValue();

    await wrapper
      .find('[data-testid="btn-remover-conhecimento"]')
      .trigger("click");
    expect(window.confirm).toHaveBeenCalled();
    expect(atividadeService.excluirConhecimento).toHaveBeenCalledWith(1, 101);
  });

  it("deve disponibilizar o cadastro", async () => {
    const atividadesComConhecimento = mockAtividades.filter(
      (a) => a.conhecimentos.length > 0,
    );
    vi.mocked(mapaService.obterMapaVisualizacao).mockResolvedValue(
      mockMapaVisualizacao([...atividadesComConhecimento] as any) as any,
    );

    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    vi.mocked(cadastroService.disponibilizarCadastro).mockResolvedValue();
    // Update process status for re-fetch
    vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue({
      codigo: 1,
      tipo: TipoProcesso.MAPEAMENTO,
      unidades: [
        {
          codUnidade: 123,
          sigla: "TESTE",
          situacaoSubprocesso:
            SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO,
        },
      ],
    } as any);

    await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");

    const confirmBtn = wrapper.find(
      '[data-testid="btn-confirmar-disponibilizacao"]',
    );
    await confirmBtn.trigger("click");
    await flushPromises();

    expect(cadastroService.disponibilizarCadastro).toHaveBeenCalledWith(123);
    expect(pushMock).toHaveBeenCalledWith("/painel");
  });

  it("deve abrir modal de importar atividades", async () => {
    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    await wrapper.find('[title="Importar"]').trigger("click");

    const modal = wrapper.findComponent(ImportarAtividadesModal);
    expect(modal.props("mostrar")).toBe(true);
  });

  it("deve permitir edição inline de atividade", async () => {
    vi.mocked(mapaService.obterMapaVisualizacao).mockResolvedValue(
      mockMapaVisualizacao([...mockAtividades] as any) as any,
    );

    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    await wrapper.find('[data-testid="btn-editar-atividade"]').trigger("click");

    expect(
      wrapper.find('[data-testid="inp-editar-atividade"]').exists(),
    ).toBe(true);

    await wrapper
      .find('[data-testid="inp-editar-atividade"]')
      .setValue("Atividade Editada");

    vi.mocked(atividadeService.atualizarAtividade).mockResolvedValue({
      codigo: 1,
      descricao: "Atividade Editada",
    } as any);

    await wrapper
      .find('[data-testid="btn-salvar-edicao-atividade"]')
      .trigger("click");

    expect(atividadeService.atualizarAtividade).toHaveBeenCalledWith(
      1,
      expect.objectContaining({ descricao: "Atividade Editada" }),
    );
  });

  it("deve permitir edição inline de conhecimento", async () => {
    vi.mocked(mapaService.obterMapaVisualizacao).mockResolvedValue(
      mockMapaVisualizacao([...mockAtividades] as any) as any,
    );

    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    await wrapper
      .find('[data-testid="btn-editar-conhecimento"]')
      .trigger("click");

    expect(
      wrapper.find('[data-testid="inp-editar-conhecimento"]').exists(),
    ).toBe(true);

    await wrapper
      .find('[data-testid="inp-editar-conhecimento"]')
      .setValue("Conhecimento Editado");

    vi.mocked(atividadeService.atualizarConhecimento).mockResolvedValue({
      id: 101,
      descricao: "Conhecimento Editado",
    } as any);

    await wrapper
      .find('[data-testid="btn-salvar-edicao-conhecimento"]')
      .trigger("click");

    expect(atividadeService.atualizarConhecimento).toHaveBeenCalledWith(
      1,
      101,
      expect.objectContaining({ descricao: "Conhecimento Editado" }),
    );
  });

  it("deve tratar disponibilizacao de revisao", async () => {
    vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue({
      codigo: 1,
      tipo: TipoProcesso.REVISAO,
      unidades: [
        {
          codUnidade: 123,
          codSubprocesso: 123,
          mapaCodigo: 456,
          sigla: "TESTE",
          situacaoSubprocesso:
            SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
        },
      ],
    } as any);
    const atividadesComConhecimento = mockAtividades.filter(
      (a) => a.conhecimentos.length > 0,
    );
    vi.mocked(mapaService.obterMapaVisualizacao).mockResolvedValue(
      mockMapaVisualizacao([...atividadesComConhecimento] as any) as any,
    );

    const { wrapper: w } = createWrapper(true); // isRevisao = true
    wrapper = w;
    await flushPromises();

    vi.mocked(
      cadastroService.disponibilizarRevisaoCadastro,
    ).mockResolvedValue();

    await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");

    const confirmBtn = wrapper.find(
      '[data-testid="btn-confirmar-disponibilizacao"]',
    );
    await confirmBtn.trigger("click");
    await flushPromises();

    expect(cadastroService.disponibilizarRevisaoCadastro).toHaveBeenCalledWith(
      123,
    );
  });

  it("deve abrir modal de historico de analise se houver analises", async () => {
    vi.mocked(analiseService.listarAnalisesCadastro).mockResolvedValue([
      {
        codigo: 1,
        dataHora: "2023-10-10T10:00:00",
        unidadeSigla: "TESTE",
        resultado: "REJEITADO",
        observacoes: "Obs",
      },
    ] as any);

    const { wrapper: w } = createWrapper();
    wrapper = w;
    await flushPromises();

    const buttons = wrapper.findAll("button");
    const btn = buttons.find((b: any) =>
      b.text().includes("Histórico de análise"),
    );
    expect(btn.exists()).toBe(true);

    await btn.trigger("click");
    await flushPromises();

    expect(wrapper.text()).toContain("Data/Hora");
    expect(wrapper.text()).toContain("REJEITADO");
  });

  it("deve exibir alerta de atividades sem conhecimento ao disponibilizar", async () => {
    vi.mocked(mapaService.obterMapaVisualizacao).mockResolvedValue(
      mockMapaVisualizacao([...mockAtividades] as any) as any,
    );

    const { wrapper: w, feedbackStore } = createWrapper();
    wrapper = w;
    await flushPromises();

    const showSpy = vi.spyOn(feedbackStore, "show");

    await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");
    await flushPromises();

    expect(showSpy).toHaveBeenCalledWith(
      "Atividades Incompletas",
      expect.stringContaining("As seguintes atividades não têm conhecimentos associados"),
      "warning"
    );
    expect(showSpy.mock.calls[0][1]).toContain("Atividade 2");
  });
});
