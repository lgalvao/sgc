import { mount } from "@vue/test-utils";
import { describe, it, expect } from "vitest";
import AtividadeItem from "@/components/AtividadeItem.vue";
import { Atividade } from "@/types/tipos";

describe("AtividadeItem.vue", () => {
  const atividadeMock: Atividade = {
    codigo: 1,
    descricao: "Atividade Teste",
    conhecimentos: [],
  };

  it("deve renderizar os botões de ação com a classe d-flex para ficarem lado a lado", async () => {
    const wrapper = mount(AtividadeItem, {
      props: {
        atividade: atividadeMock,
        podeEditar: true,
      },
      global: {
        stubs: {
           BButton: { template: '<button class="btn"><slot /></button>' },
           BCard: { template: '<div class="card"><slot /></div>' },
           BCardBody: { template: '<div class="card-body"><slot /></div>' },
           BFormInput: true,
           BForm: true,
           BCol: true,
        }
      }
    });

    // Encontra o container dos botões de ação (editar/remover atividade)
    const botoesContainer = wrapper.find(".botoes-acao-atividade");
    
    expect(botoesContainer.exists()).toBe(true);
    expect(botoesContainer.classes()).toContain("d-flex");
    expect(botoesContainer.classes()).toContain("position-absolute");
  });
});
