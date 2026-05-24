import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import CadastroView from "../CadastroView.vue";
import * as useCadastroAtividadesMutacoesModule from "@/composables/useCadastroAtividadesMutacoes";
import * as useCadastroDisponibilizacaoModule from "../cadastroDisponibilizacao";
import * as useCadastroAnaliseFluxoModule from "../cadastroAnaliseFluxo";
import * as useFluxoSubprocessoModule from "@/composables/useFluxoSubprocesso";
import {createMemoryHistory, createRouter} from "vue-router";
import * as useNotificationModule from "@/composables/useNotification";
import * as useCadastroOrquestracaoModule from "@/composables/useCadastroOrquestracao";

vi.mock("@/composables/useCadastroAtividadesMutacoes");
vi.mock("../cadastroDisponibilizacao");
vi.mock("../cadastroAnaliseFluxo");
vi.mock("@/composables/useFluxoSubprocesso");
vi.mock("@/composables/useNotification");
vi.mock("@/composables/useCadastroOrquestracao");

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{path: "/", component: {}}]
});

const mockMutacoes = {
  erroNovaAtividade: ref(""),
  adicionarAtividade: vi.fn(),
  dadosRemocao: ref(null),
  loadingRemocao: ref(false),
  mostrarModalConfirmacaoRemocao: ref(false),
  removerAtividade: vi.fn(),
  confirmarRemocao: vi.fn(),
  salvarEdicaoAtividade: vi.fn(),
  adicionarConhecimento: vi.fn(),
  removerConhecimento: vi.fn(),
  salvarEdicaoConhecimento: vi.fn()
};

const mockDisponibilizacao = {
  erroGlobal: ref(""),
  erroTick: ref(0),
  errosValidacao: ref([]),
  loadingValidacao: ref(false),
  loadingDisponibilizacao: ref(false),
  limparErrosValidacao: vi.fn(),
  disponibilizarCadastro: vi.fn(),
  confirmarDisponibilizacao: vi.fn(),
  obterErroParaAtividade: vi.fn()
};

const mockAnaliseFluxo = {
  historicoAnalises: ref([]),
  loadingAnaliseCadastro: ref(false),
  loadingDevolucaoAnalise: ref(false),
  observacaoValidacao: ref(""),
  observacaoDevolucao: ref(""),
  abrirModalHistorico: vi.fn(),
  abrirModalValidarAnalise: vi.fn(),
  abrirModalDevolverAnalise: vi.fn(),
  confirmarValidacaoAnalise: vi.fn(),
  confirmarDevolucaoAnalise: vi.fn()
};

const mockFluxo = {
  ultimoErro: ref(null),
  carregandoInicial: ref(false),
  subprocesso: ref({codigo: 123}),
  unidade: ref({sigla: "U", nome: "Unidade"}),
  permissoes: ref({}),
  situacaoAtual: ref(""),
  atividades: ref([]),
  houveAlteracaoCadastro: ref(false),
  podeEditarCadastro: ref(true)
};

const mockOrquestracao = {
    carregandoInicial: ref(false),
    codigoSubprocesso: ref(123),
    atividadesSnapshotInicial: ref(""),
    unidade: ref({sigla: "U", nome: "Unidade"}),
    codMapa: ref(100),
    carregarContextoInicial: vi.fn(),
    processarRespostaLocal: vi.fn()
};

const mockNotify = vi.fn();
const mockClear = vi.fn();

describe("CadastroView Coverage", () => {
  it("handleImportAtividades exibe aviso quando resultado tem aviso", async () => {
      vi.mocked(useFluxoSubprocessoModule.useFluxoSubprocesso).mockReturnValue(mockFluxo as any);
      vi.mocked(useCadastroAtividadesMutacoesModule.useCadastroAtividadesMutacoes).mockReturnValue(mockMutacoes as any);
      vi.mocked(useCadastroDisponibilizacaoModule.useCadastroDisponibilizacao).mockReturnValue(mockDisponibilizacao as any);
      vi.mocked(useCadastroAnaliseFluxoModule.useCadastroAnaliseFluxo).mockReturnValue(mockAnaliseFluxo as any);
      vi.mocked(useCadastroOrquestracaoModule.useCadastroOrquestracao).mockReturnValue(mockOrquestracao as any);
      vi.mocked(useNotificationModule.useNotification).mockReturnValue({
          notify: mockNotify,
          clear: mockClear,
          notificacao: ref(null)
      } as any);

      const wrapper = mount(CadastroView, {
          props: {codProcesso: 1, sigla: "U"},
          global: {
              plugins: [createTestingPinia(), router],
              stubs: {
                  LayoutPadrao: {template: "<div><slot/></div>"},
                  CadastroAcoesHeader: true,
                  CadAtividadeForm: true,
                  EmptyState: true,
                  CadastroFluxoModais: true,
                  AtividadeItem: true
              }
          }
      });
      
      await flushPromises();
      const resultadoComAviso = { 
          aviso: "Duplicatas",
          subprocesso: { codigo: 123 }
      } as any;
      await (wrapper.vm as any).handleImportAtividades(resultadoComAviso);
      expect(mockClear).toHaveBeenCalled();
      expect(mockNotify).toHaveBeenCalledWith(expect.any(String), 'warning');
  });
});
