import {createTestingPinia} from "@pinia/testing";
import {mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {type Mapa, type SubprocessoPermissoes, TipoProcesso, type Unidade,} from "@/types/tipos";
import SubprocessoCards from "../SubprocessoCards.vue";

const pushMock = vi.fn();

vi.mock("vue-router", () => ({
  useRouter: () => ({
    push: pushMock,
    currentRoute: {
      value: {
        params: {
            codProcesso: "1",
            siglaUnidade: "TEST",
        },
      },
    },
  }),
}));

describe("SubprocessoCards.vue", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const defaultPermissoes: SubprocessoPermissoes = {
    podeVerPagina: true,
    podeEditarMapa: true,
    podeVisualizarMapa: true,
    podeDisponibilizarCadastro: true,
    podeDevolverCadastro: true,
    podeAceitarCadastro: true,
    podeVisualizarDiagnostico: true,
    podeAlterarDataLimite: true,
    podeVisualizarImpacto: true,
  };

  const createWrapper = (propsOverride: any = {}) => {
    return mount(SubprocessoCards, {
      props: {
        permissoes: defaultPermissoes,
        ...propsOverride,
      },
      global: {
          plugins: [createTestingPinia({stubActions: false})],
      },
    });
  };

  const mockMapa: Mapa = {
    codigo: 1,
      descricao: "mapa de teste",
      unidade: {sigla: "UNID_TESTE"} as Unidade,
      situacao: "em_andamento",
    codProcesso: 1,
    competencias: [],
    dataCriacao: new Date().toISOString(),
    dataDisponibilizacao: null,
    dataFinalizacao: null,
  };

    describe("Lógica de Navegação", () => {
        it("deve navegar para SubprocessoCadastro ao clicar no card de atividades (edição)", async () => {
      const wrapper = createWrapper({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null,
          situacao: "Mapa disponibilizado",
          permissoes: {...defaultPermissoes, podeEditarMapa: true},
      });

      const card = wrapper.find('[data-testid="atividades-card"]');
            await card.trigger("click");

      expect(pushMock).toHaveBeenCalledWith({
          name: "SubprocessoCadastro",
          params: {
              codProcesso: 1,
              siglaUnidade: "TEST",
          },
      });
    });

        it("deve navegar para SubprocessoVisCadastro ao clicar no card de atividades (visualização)", async () => {
      const wrapper = createWrapper({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: null,
          situacao: "Mapa disponibilizado",
          permissoes: {
              ...defaultPermissoes,
              podeEditarMapa: false,
              podeVisualizarMapa: true,
          },
      });

      const card = wrapper.find('[data-testid="atividades-card-vis"]');
            await card.trigger("click");

      expect(pushMock).toHaveBeenCalledWith({
          name: "SubprocessoVisCadastro",
          params: {
              codProcesso: 1,
              siglaUnidade: "TEST",
          },
      });
    });

        it("deve navegar para SubprocessoMapa ao clicar no card de mapa", async () => {
      const wrapper = createWrapper({
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: mockMapa,
          situacao: "Mapa disponibilizado",
          permissoes: {...defaultPermissoes, podeVisualizarMapa: true},
      });

      const card = wrapper.find('[data-testid="mapa-card"]');
            await card.trigger("click");

      expect(pushMock).toHaveBeenCalledWith({
          name: "SubprocessoMapa",
          params: {
              codProcesso: 1,
              siglaUnidade: "TEST",
          },
      });
    });

        it("deve navegar para DiagnosticoEquipe ao clicar no card de diagnostico", async () => {
            const wrapper = createWrapper({
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                mapa: null,
                situacao: "Em andamento",
                permissoes: {...defaultPermissoes, podeVisualizarDiagnostico: true},
            });

            const card = wrapper.find('[data-testid="diagnostico-card"]');
            await card.trigger("click");

            expect(pushMock).toHaveBeenCalledWith({
                name: "DiagnosticoEquipe",
                params: {
                    codProcesso: 1,
                    siglaUnidade: "TEST",
                },
            });
    });

        it("deve navegar para OcupacoesCriticas ao clicar no card de ocupações", async () => {
            const wrapper = createWrapper({
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                mapa: null,
                situacao: "Em andamento",
            });

            const card = wrapper.find('[data-testid="ocupacoes-card"]');
            await card.trigger("click");

            expect(pushMock).toHaveBeenCalledWith({
                name: "OcupacoesCriticas",
                params: {
                    codProcesso: 1,
                    siglaUnidade: "TEST",
                },
            });
    });
  });

    describe("Lógica de Renderização", () => {
        it("não deve renderizar card de mapa se não puder visualizar", () => {
            const wrapper = createWrapper({
                tipoProcesso: TipoProcesso.MAPEAMENTO,
                mapa: mockMapa,
                situacao: "Mapa disponibilizado",
                permissoes: {
                    ...defaultPermissoes,
                    podeVisualizarMapa: false,
                    podeEditarMapa: false,
                },
            });

            expect(wrapper.find('[data-testid="mapa-card"]').exists()).toBe(false);
        });
  });
});
