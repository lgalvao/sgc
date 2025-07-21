import { defineStore } from "pinia";
import atividadesConhecimentosSESEL from "../mocks/atividades_conhecimentos_SESEL.json";

export const useAtividadesConhecimentosStore = defineStore(
  "atividadesConhecimentos",
  {
    state: () => ({
      // Estrutura: { [siglaUnidade]: [atividades] }
      atividadesPorUnidade: {
        SESEL: atividadesConhecimentosSESEL.map((a, idx) => ({
          id: idx + 1,
          descricao: a.descricao,
          conhecimentos: a.conhecimentos.map((desc, cidx) => ({
            id: cidx + 1,
            descricao: desc,
          })),
          novoConhecimento: "",
        })),
      },
    }),
    actions: {
      setAtividades(unidade, atividades) {
        this.atividadesPorUnidade[unidade] = atividades;
      },
      adicionarAtividade(unidade, atividade) {
        if (!this.atividadesPorUnidade[unidade])
          this.atividadesPorUnidade[unidade] = [];
        this.atividadesPorUnidade[unidade].unshift(atividade);
      },
      removerAtividade(unidade, idx) {
        if (this.atividadesPorUnidade[unidade])
          this.atividadesPorUnidade[unidade].splice(idx, 1);
      },
      adicionarConhecimento(unidade, idxAtividade, conhecimento) {
        this.atividadesPorUnidade[unidade][idxAtividade].conhecimentos.push(
          conhecimento
        );
      },
      removerConhecimento(unidade, idxAtividade, idxConhecimento) {
        this.atividadesPorUnidade[unidade][idxAtividade].conhecimentos.splice(
          idxConhecimento,
          1
        );
      },
    },
  }
);
