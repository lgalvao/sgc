import {mount} from "@vue/test-utils";
import {createPinia, setActivePinia} from "pinia";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import ImpactoMapaModal from "../ImpactoMapaModal.vue";

const mocks = vi.hoisted(() => {
    const mockImpacto = {
        temImpactos: true,
        totalAtividadesInseridas: 1,
        totalAtividadesRemovidas: 1,
        totalAtividadesAlteradas: 0,
        totalCompetenciasImpactadas: 1,
        atividadesInseridas: [
            {
                codigo: 203,
                descricao: "Nova Atividade",
                tipoImpacto: "INSERIDA",
                competenciasVinculadas: [],
            },
        ],
        atividadesRemovidas: [
            {codigo: 204, descricao: "Atividade Removida", tipoImpacto: "REMOVIDA"},
        ],
        atividadesAlteradas: [],
        competenciasImpactadas: [
            {
                codigo: 101,
                descricao: "Competência A",
                atividadesAfetadas: ["Atividade Removida"],
                tipoImpacto: "ATIVIDADE_REMOVIDA",
            },
        ],
    };
    return {
        mockImpacto,
        fetchImpactoMapa: vi.fn(),
    };
});

// Mock stores
vi.mock("@/stores/mapas", () => ({
  useMapasStore: () => ({
    impactoMapa: ref(mocks.mockImpacto),
    fetchImpactoMapa: mocks.fetchImpactoMapa,
  }),
}));

vi.mock("@/stores/processos", () => ({
  useProcessosStore: () => ({
    processoDetalhe: {
        unidades: [{sigla: "UT", codSubprocesso: 100}],
    },
    fetchProcessoDetalhe: vi.fn(),
  }),
}));

describe("ImpactoMapaModal", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    mocks.fetchImpactoMapa.mockClear();
  });

    it("não deve buscar impactos quando mostrar for falso", () => {
    mount(ImpactoMapaModal, {
        props: {mostrar: false, idProcesso: 1, siglaUnidade: "UT"},
      global: {
          stubs: {BModal: true},
      },
    });
    expect(mocks.fetchImpactoMapa).not.toHaveBeenCalled();
  });

    it("deve buscar impactos ao abrir", async () => {
    const wrapper = mount(ImpactoMapaModal, {
        props: {mostrar: false, idProcesso: 1, siglaUnidade: "UT"},
      global: {
          stubs: {BModal: true},
      },
    });

    await wrapper.setProps({ mostrar: true });
    expect(mocks.fetchImpactoMapa).toHaveBeenCalledWith(100);
  });

    it("deve renderizar a seção de atividades inseridas corretamente", () => {
    const wrapper = mount(ImpactoMapaModal, {
        props: {mostrar: true, idProcesso: 1, siglaUnidade: "UT"},
      global: {
          stubs: {BModal: {template: "<div><slot/></div>"}},
      },
    });

        expect(wrapper.text()).toContain("Atividades Inseridas");
        expect(wrapper.text()).toContain("Nova Atividade");
  });

    it("deve renderizar a seção de atividades removidas corretamente", () => {
        const wrapper = mount(ImpactoMapaModal, {
            props: {mostrar: true, idProcesso: 1, siglaUnidade: "UT"},
      global: {
          stubs: {BModal: {template: "<div><slot/></div>"}},
      },
    });

        expect(wrapper.text()).toContain("Atividades Removidas");
        expect(wrapper.text()).toContain("Atividade Removida");
  });

    it("deve renderizar a seção de competências impactadas corretamente", () => {
    const wrapper = mount(ImpactoMapaModal, {
        props: {mostrar: true, idProcesso: 1, siglaUnidade: "UT"},
      global: {
          stubs: {BModal: {template: "<div><slot/></div>"}},
      },
    });

        expect(wrapper.text()).toContain("Competências Impactadas");
        expect(wrapper.text()).toContain("Competência A");
        expect(wrapper.text()).toContain("Atividade Removida");
  });
});
