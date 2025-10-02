import {createPinia, setActivePinia} from 'pinia';
import {vi} from 'vitest';
import type {Atividade} from '@/types/tipos';

/**
 * Dados de mock reutilizáveis para atividades
 */
export function getMockAtividadesData(): Atividade[] {
    return [
        {
            id: 1,
            descricao: "Manutenção de sistemas administrativos criados pela unidade",
            idSubprocesso: 3,
            conhecimentos: [{id: 1, descricao: "Criação de testes de integração em Cypress"}]
        },
        {
            id: 2,
            descricao: "Especificação de sistemas administrativos",
            idSubprocesso: 3,
            conhecimentos: [{id: 6, descricao: "Modelagem de dados"}]
        },
        {
            id: 3,
            descricao: "Implantação de sistemas externos",
            idSubprocesso: 1,
            conhecimentos: [{id: 40, descricao: "Conhecimento em configuração de APIs de terceiros"}]
        }
    ];
}

/**
 * Prepara uma instância de Pinia ativa e retorna a store de atividades "fresca".
 * Usa importActual para garantir que a store real seja importada após mocks terem sido aplicados.
 */
export async function prepareFreshAtividadesStore() {
    setActivePinia(createPinia());
    const {useAtividadesStore: useAtividadesStoreActual} = (await vi.importActual('../stores/atividades')) as {
        useAtividadesStore: () => any;
    };
    const store = useAtividadesStoreActual();
    const initialAtividades = getMockAtividadesData();
    store.atividades = initialAtividades.map((a: Atividade) => ({
        ...a,
        conhecimentos: a.conhecimentos.map((c: any) => ({...c}))
    }));
    store.nextId = Math.max(...initialAtividades.flatMap((a: Atividade) => [a.id, ...a.conhecimentos.map((c: any) => c.id)])) + 1;
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

