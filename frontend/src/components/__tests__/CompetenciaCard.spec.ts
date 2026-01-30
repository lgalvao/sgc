import {mount} from "@vue/test-utils";
import {describe, expect, it} from "vitest";
import CompetenciaCard from "@/components/CompetenciaCard.vue";
import type {Atividade, Competencia} from "@/types/tipos";

// Stubs para componentes do Bootstrap Vue Next
const stubs = {
  BCard: {
    template: '<div class="card"><slot /></div>',
  },
  BCardBody: {
    template: '<div class="card-body"><slot /></div>',
  },
  BCardHeader: {
    template: '<div class="card-header"><slot /></div>',
  },
  BButton: {
    props: ['title', 'variant'],
    template: '<button :title="title" :class="variant" @click="$emit(\'click\')"><slot /></button>',
  },
};

describe("CompetenciaCard.vue", () => {
  const mockAtividades: Atividade[] = [
    {codigo: 101, descricao: "Atividade 1", conhecimentos: []},
    {codigo: 102, descricao: "Atividade 2", conhecimentos: [{descricao: "Java", codigo: 1}]},
  ];

  const mockCompetencia: Competencia = {
    codigo: 10,
    descricao: "Competencia Teste",
    atividades: [],
    atividadesAssociadas: [101, 102],
  };

  it("deve renderizar descrição da competência", () => {
    const wrapper = mount(CompetenciaCard, {
      props: {
        competencia: mockCompetencia,
        atividades: mockAtividades,
        podeEditar: true,
      },
      global: {stubs, directives: {"b-tooltip": {}}},
    });

    expect(wrapper.text()).toContain("Competencia Teste");
  });

  it("deve renderizar atividades associadas", () => {
    const wrapper = mount(CompetenciaCard, {
      props: {
        competencia: mockCompetencia,
        atividades: mockAtividades,
        podeEditar: true,
      },
      global: {stubs, directives: {"b-tooltip": {}}},
    });

    expect(wrapper.text()).toContain("Atividade 1");
    expect(wrapper.text()).toContain("Atividade 2");
  });

  it("deve emitir evento editar ao clicar no botão editar", async () => {
    const wrapper = mount(CompetenciaCard, {
      props: {
        competencia: mockCompetencia,
        atividades: mockAtividades,
        podeEditar: true,
      },
      global: {stubs, directives: {"b-tooltip": {}}},
    });

    await wrapper.find('[data-testid="btn-editar-competencia"]').trigger("click");
    expect(wrapper.emitted("editar")).toBeTruthy();
    expect(wrapper.emitted("editar")?.[0]).toEqual([mockCompetencia]);
  });

  it("deve emitir evento excluir ao clicar no botão excluir", async () => {
    const wrapper = mount(CompetenciaCard, {
      props: {
        competencia: mockCompetencia,
        atividades: mockAtividades,
        podeEditar: true,
      },
      global: {stubs, directives: {"b-tooltip": {}}},
    });

    await wrapper.find('[data-testid="btn-excluir-competencia"]').trigger("click");
    expect(wrapper.emitted("excluir")).toBeTruthy();
    expect(wrapper.emitted("excluir")?.[0]).toEqual([mockCompetencia.codigo]);
  });

  it("deve emitir evento removerAtividade ao clicar na lixeira da atividade", async () => {
    const wrapper = mount(CompetenciaCard, {
      props: {
        competencia: mockCompetencia,
        atividades: mockAtividades,
        podeEditar: true,
      },
      global: {stubs, directives: {"b-tooltip": {}}},
    });

    // Encontra os botões de remover atividade (existem 2)
    const botoes = wrapper.findAll('[data-testid="btn-remover-atividade-associada"]');
    await botoes[0].trigger("click"); // Clica no primeiro (Atividade 101)

    expect(wrapper.emitted("removerAtividade")).toBeTruthy();
    expect(wrapper.emitted("removerAtividade")?.[0]).toEqual([mockCompetencia.codigo, 101]);
  });

  it("deve mostrar badge de conhecimentos se atividade tiver conhecimentos", () => {
    const wrapper = mount(CompetenciaCard, {
      props: {
        competencia: mockCompetencia,
        atividades: mockAtividades,
      },
      global: {stubs, directives: {"b-tooltip": {}}},
    });

    // Atividade 2 (index 1) tem conhecimentos
    const badges = wrapper.findAll('[data-testid="cad-mapa__txt-badge-conhecimentos-1"]');
    expect(badges.length).toBe(1); // Só Atividade 2 tem
    expect(badges[0].text()).toBe("1");
  });
});
