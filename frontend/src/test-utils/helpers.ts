import {createPinia, setActivePinia} from "pinia";
import {vi} from "vitest";
import type {Atividade} from "@/types/tipos";

/**
 * Dados de mock reutilizáveis para atividades
 */
export function getMockAtividadesData(): Atividade[] {
    return [
        {
            codigo: 1,
            descricao: "Manutenção de sistemas administrativos criados pela unidade",
            conhecimentos: [
                {codigo: 1, descricao: "Criação de testes de integração em Cypress"},
            ],
        },
        {
            codigo: 2,
            descricao: "Especificação de sistemas administrativos",
            conhecimentos: [{codigo: 6, descricao: "Modelagem de dados"}],
        },
        {
            codigo: 3,
            descricao: "Implantação de sistemas externos",
            conhecimentos: [
                {
                    codigo: 40,
                    descricao: "Conhecimento em configuração de APIs de terceiros",
                },
            ],
        },
    ];
}

/**
 * Inicializa e ativa uma instância de Pinia retornando-a.
 * Uso: const pinia = initPinia();
 */
export function initPinia() {
    const pinia = createPinia();
    setActivePinia(pinia);
    return pinia;
}
