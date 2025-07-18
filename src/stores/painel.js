import { defineStore } from "pinia";
import { useProcessosStore } from "./processos";

export const usePainelStore = defineStore("painel", {
  getters: {
    totalProcessos: () => useProcessosStore().processos.length,
    emAndamento: () =>
      useProcessosStore().processos.filter((p) => p.situacao === "Em andamento")
        .length,
    finalizados: () =>
      useProcessosStore().processos.filter((p) => p.situacao === "Finalizado")
        .length,
    pendenciasPorUnidade: () => [
      { unidade: "COSIS", status: "Aguardando", badge: "bg-warning text-dark" },
      { unidade: "SESEL", status: "Finalizado", badge: "bg-success" },
      { unidade: "SEDESENV", status: "Devolvido", badge: "bg-danger" },
    ],
    alertas: () => [
      {
        data: "2025-07-01",
        unidade: "COSIS",
        descricao: "Prazo para COSIS alterado para: 10/07/2025",
      },
      {
        data: "2025-07-02",
        unidade: "SEDESENV",
        descricao: "Devolvido para ajustes",
      },
    ],
    processosSubordinadas: () => [
      {
        nome: "Processo 2024/1",
        status: "Em andamento",
        badge: "bg-warning text-dark",
      },
      { nome: "Processo 2023/2", status: "Finalizado", badge: "bg-success" },
    ],
    situacaoCadastro: () => "Em andamento",
    descricaoMapa: () =>
      "Visualize o mapa de competências da sua unidade (atual e anteriores).",
  },
});
