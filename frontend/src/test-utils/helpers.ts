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
 * Prepara uma instância de Pinia ativa e retorna a store de atividades "fresca".
 * Usa importActual para garantir que a store real seja importada após mocks terem sido aplicados.
 */
export async function prepareFreshAtividadesStore() {
    setActivePinia(createPinia());
    const {useAtividadesStore: useAtividadesStoreActual} =
        (await vi.importActual("../stores/atividades")) as {
            useAtividadesStore: () => any;
        };
    const store = useAtividadesStoreActual();
    const initialAtividades = getMockAtividadesData();

    // Populate the Map with a dummy subprocess ID (e.g., 1)
    const mockAtividades = initialAtividades.map((a: Atividade) => ({
        ...a,
        conhecimentos: a.conhecimentos.map((c: any) => ({...c})),
    }));
    store.atividadesPorSubprocesso.set(1, mockAtividades);

    return store;
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
