import {defineStore} from "pinia";

export const usePainelStore = defineStore("painel", {
    getters: {
        alertas: () => [
            {
                data: "2025-07-01",
                unidade: "COSIS",
                descricao: "Prazo para COSIS alterado para 10/07/2025",
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
                situacao: "Em andamento",
                badge: "bg-warning text-dark",
            },
            {
                nome: "Processo 2023/2", situacao: "Finalizado", badge: "bg-success"
            },
        ],
        situacaoCadastro: () => "Em andamento",
        descricaoMapa: () => "Visualize o mapa de competências da sua unidade (atual e anteriores)."
    },
});
