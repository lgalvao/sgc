import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {nextTick} from "vue";
import {Perfil, SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import VisAtividades from "@/views/VisAtividades.vue";
import {usePerfilStore} from "@/stores/perfil";

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

vi.mock("@/services/mapaService", () => ({
  obterMapaVisualizacao: vi.fn(),
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

describe("VisAtividades.vue", () => {
  let wrapper: any;

  function createWrapper(perfil: Perfil, situacao: SituacaoSubprocesso) {
    const wrapper = mount(VisAtividades, {
      props: {
        codProcesso: 1,
        sigla: "TESTE",
      },
      global: {
        plugins: [
          createTestingPinia({
            stubActions: true,
            initialState: {
              processos: {
                processoDetalhe: {
                  codigo: 1,
                  tipo: TipoProcesso.REVISAO,
                  unidades: [
                    {
                      codUnidade: 123,
                      sigla: "TESTE",
                      situacaoSubprocesso: situacao,
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
            },
          }),
        ],
      },
    });

    const perfilStore = usePerfilStore();
    perfilStore.perfilSelecionado = perfil;

    return { wrapper };
  }

  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    wrapper?.unmount();
  });

  it('deve mostrar o botão "Impacto no mapa" para GESTOR em CADASTRO_DISPONIBILIZADO', async () => {
    const { wrapper: w } = createWrapper(
      Perfil.GESTOR,
      SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO,
    );
    wrapper = w;
    await flushPromises();
    await nextTick();

    expect(wrapper.find('[data-testid="impactos-mapa-button"]').exists()).toBe(true);
  });

  it('deve mostrar o botão "Impacto no mapa" para ADMIN em CADASTRO_DISPONIBILIZADO', async () => {
    const { wrapper: w } = createWrapper(
      Perfil.ADMIN,
      SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO,
    );
    wrapper = w;
    await flushPromises();

    expect(wrapper.find('[data-testid="impactos-mapa-button"]').exists()).toBe(true);
  });

  it('não deve mostrar o botão "Impacto no mapa" para GESTOR em outra situação', async () => {
    const { wrapper: w } = createWrapper(
      Perfil.GESTOR,
      SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
    );
    wrapper = w;
    await flushPromises();

    expect(wrapper.find('[data-testid="impactos-mapa-button"]').exists()).toBe(false);
  });
});
