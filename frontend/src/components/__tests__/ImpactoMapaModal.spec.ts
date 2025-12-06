import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import ImpactoMapaModal from "@/components/ImpactoMapaModal.vue";
import {TipoImpactoCompetencia} from "@/types/impacto";

vi.mock("@/services/mapaService", () => ({
  verificarImpactos: vi.fn(),
}));

describe("ImpactoMapaModal.vue", () => {
  let wrapper: any;

  function createWrapper(impacto: any) {
    const wrapper = mount(ImpactoMapaModal, {
      props: {
        mostrar: true,
        idProcesso: 1,
        siglaUnidade: "TESTE",
      },
      global: {
        plugins: [
          createTestingPinia({
            stubActions: false,
            initialState: {
              mapas: {
                impactoMapa: impacto,
              },
              processos: {
                processoDetalhe: {
                  unidades: [{ sigla: "TESTE", codSubprocesso: 123 }],
                },
              },
            },
          }),
        ],
      },
    });

    return { wrapper };
  }

  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    wrapper?.unmount();
  });

  it("deve mostrar mensagem de nenhum impacto", async () => {
    const impacto = {
      temImpactos: false,
      atividadesInseridas: [],
      atividadesRemovidas: [],
      atividadesAlteradas: [],
      competenciasImpactadas: [],
    };
    const { wrapper: w } = createWrapper(impacto);
    wrapper = w;
    await flushPromises();

    expect(wrapper.text()).toContain("Nenhum impacto detectado no mapa.");
  });

  it("deve mostrar atividades inseridas", async () => {
    const impacto = {
      temImpactos: true,
      atividadesInseridas: [{ codigo: 1, descricao: "Nova Atividade" }],
      atividadesRemovidas: [],
      atividadesAlteradas: [],
      competenciasImpactadas: [],
    };
    const { wrapper: w } = createWrapper(impacto);
    wrapper = w;
    await flushPromises();

    expect(wrapper.text()).toContain("Atividades Inseridas");
    expect(wrapper.text()).toContain("Nova Atividade");
  });

  it("deve mostrar competências impactadas", async () => {
    const impacto = {
      temImpactos: true,
      atividadesInseridas: [],
      atividadesRemovidas: [],
      atividadesAlteradas: [],
      competenciasImpactadas: [
        {
          codigo: 1,
          descricao: "Competência Impactada",
          atividadesAfetadas: ["Atividade Removida"],
          tipoImpacto: TipoImpactoCompetencia.ATIVIDADE_REMOVIDA,
        },
      ],
    };
    const { wrapper: w } = createWrapper(impacto);
    wrapper = w;
    await flushPromises();

    expect(wrapper.text()).toContain("Competências Impactadas");
    expect(wrapper.text()).toContain("Competência Impactada");
  });
});
