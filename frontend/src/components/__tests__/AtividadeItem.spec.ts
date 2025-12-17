import { mount } from "@vue/test-utils";
import { describe, it, expect } from "vitest";
import AtividadeItem from "@/components/AtividadeItem.vue";
import { Atividade } from "@/types/tipos";

describe("AtividadeItem.vue", () => {
  const atividadeMock: Atividade = {
    codigo: 1,
    descricao: "Atividade Teste",
    conhecimentos: [
        { id: 10, descricao: "Conhecimento 1" }
    ],
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

    const botoesContainer = wrapper.find(".botoes-acao-atividade");
    
    expect(botoesContainer.exists()).toBe(true);
    expect(botoesContainer.classes()).toContain("d-flex");
    expect(botoesContainer.classes()).toContain("position-absolute");
  });

  it("deve ter aria-labels descritivos nos botões de ação", async () => {
    const wrapper = mount(AtividadeItem, {
      props: {
        atividade: atividadeMock,
        podeEditar: true,
      },
      global: {
        stubs: {
           // Repassa todos os atributos, incluindo aria-label e data-testid
           BButton: {
             template: '<button class="btn" v-bind="$attrs"><slot /></button>'
           },
           BCard: { template: '<div class="card"><slot /></div>' },
           BCardBody: { template: '<div class="card-body"><slot /></div>' },
           BFormInput: true,
           BForm: true,
           BCol: true,
        }
      }
    });

    const btnEditar = wrapper.find('[data-testid="btn-editar-atividade"]');
    expect(btnEditar.attributes("aria-label")).toBe("Editar atividade: Atividade Teste");

    const btnRemover = wrapper.find('[data-testid="btn-remover-atividade"]');
    expect(btnRemover.attributes("aria-label")).toBe("Remover atividade: Atividade Teste");

    // Testar botões de conhecimento
    const btnEditarConhecimento = wrapper.find('[data-testid="btn-editar-conhecimento"]');
    expect(btnEditarConhecimento.attributes("aria-label")).toBe("Editar conhecimento: Conhecimento 1");

    const btnRemoverConhecimento = wrapper.find('[data-testid="btn-remover-conhecimento"]');
    expect(btnRemoverConhecimento.attributes("aria-label")).toBe("Remover conhecimento: Conhecimento 1");
  });
});
