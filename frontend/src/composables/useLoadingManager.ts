import {computed, ref, type Ref} from 'vue';
import {logger} from '@/utils';

/**
 * Gerenciador de múltiplos estados de carregamento
 */
interface GerenciadorCarregamento {
    estados: Record<string, Ref<boolean>>;
    iniciar: (nome: string) => void;
    parar: (nome: string) => void;
    estaCarregando: (nome: string) => boolean;
    qualquerCarregando: Ref<boolean>;
    pararTodos: () => void;
    comCarregamento: <T>(nome: string, fn: () => Promise<T>) => Promise<T>;
}

/**
 * Composable para gerenciar múltiplos estados de carregamento
 *
 * Simplifica o gerenciamento de estados de carregamento em componentes
 * com múltiplas operações assíncronas.
 *
 *
 * @example
 * ```ts
 * const carregamento = useGerenciadorCarregamento(['buscar', 'salvar', 'excluir']);
 *
 * // Iniciar carregamento
 * carregamento.iniciar('buscar');
 *
 * // Verificar estado
 * if (carregamento.estaCarregando('buscar')) { ... }
 *
 * // Parar carregamento
 * carregamento.parar('buscar');
 *
 * // Usar wrapper para async
 * await carregamento.comCarregamento('salvar', async () => {
 *     await salvarDados();
 * });
 *
 * // Verificar se qualquer carregamento está ativo
 * if (carregamento.qualquerCarregando.value) { ... }
 * ```
 */
export function useGerenciadorCarregamento(nomes: string[]): GerenciadorCarregamento {
    // Cria refs para cada estado de carregamento
    const estados: Record<string, Ref<boolean>> = {};
    nomes.forEach(nome => {
        estados[nome] = ref(false);
    });

    /**
     * Inicia um estado de carregamento
     */
    const iniciar = (nome: string) => {
        if (!estados[nome]) {
            logger.warn(`Estado de carregamento "${nome}" não foi registrado`);
            return;
        }
        estados[nome].value = true;
    };

    /**
     * Para um estado de carregamento
     */
    const parar = (nome: string) => {
        if (!estados[nome]) {
            logger.warn(`Estado de carregamento "${nome}" não foi registrado`);
            return;
        }
        estados[nome].value = false;
    };

    /**
     * Verifica se um estado está carregando
     */
    const estaCarregando = (nome: string): boolean => {
        return estados[nome]?.value ?? false;
    };

    /**
     * Computed que retorna true se qualquer estado estiver carregando
     */
    const qualquerCarregando = computed(() => {
        return Object.values(estados).some(estado => estado.value);
    });

    /**
     * Para todos os estados de carregamento
     */
    const pararTodos = () => {
        Object.keys(estados).forEach(nome => parar(nome));
    };

    /**
     * Wrapper para executar função assíncrona com carregamento automático
     */
    const comCarregamento = async <T>(nome: string, fn: () => Promise<T>): Promise<T> => {
        try {
            iniciar(nome);
            return await fn();
        } finally {
            parar(nome);
        }
    };

    return {
        estados,
        iniciar,
        parar,
        estaCarregando,
        qualquerCarregando,
        pararTodos,
        comCarregamento
    };
}

/**
 * Versão simplificada para um único estado de carregamento
 *
 *
 * @example
 * ```ts
 * const carregamento = useCarregamentoSimples();
 *
 * carregamento.iniciar();
 * if (carregamento.estaCarregando.value) { ... }
 * carregamento.parar();
 *
 * // Ou usar wrapper
 * await carregamento.comCarregamento(async () => {
 *     await buscarDados();
 * });
 * ```
 */
export function useCarregamentoSimples(valorInicial = false) {
    const estaCarregando = ref(valorInicial);

    const iniciar = () => {
        estaCarregando.value = true;
    };

    const parar = () => {
        estaCarregando.value = false;
    };

    const alternar = () => {
        estaCarregando.value = !estaCarregando.value;
    };

    const comCarregamento = async <T>(fn: () => Promise<T>): Promise<T> => {
        try {
            iniciar();
            return await fn();
        } finally {
            parar();
        }
    };

    return {
        estaCarregando,
        iniciar,
        parar,
        alternar,
        comCarregamento
    };
}

// Aliases para compatibilidade legada durante a transição
/** @deprecated Use useGerenciadorCarregamento */
export const useLoadingManager = useGerenciadorCarregamento;
/** @deprecated Use useCarregamentoSimples */
export const useSingleLoading = useCarregamentoSimples;
