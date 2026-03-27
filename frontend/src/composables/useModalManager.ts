import {ref, type Ref} from 'vue';
import {logger} from '@/utils';

/**
 * Estado de uma modal individual
 */
interface EstadoModal {
    aberto: boolean;
    dados?: any;
}

/**
 * Gerenciador de modals
 */
interface GerenciadorModals {
    modals: Record<string, Ref<EstadoModal>>;
    abrir: (nome: string, dados?: any) => void;
    fechar: (nome: string) => void;
    alternar: (nome: string) => void;
    estaAberto: (nome: string) => boolean;
    obterDados: (nome: string) => any;
    fecharTodos: () => void;
}

/**
 * Composable para gerenciar estado de múltiplas modals
 *
 * Simplifica o gerenciamento de múltiplas modals em um componente,
 * eliminando a necessidade de criar refs individuais para cada modal.
 *
 * @example
 * ```ts
 * const { modals, abrir, fechar, estaAberto } = useGerenciadorModals([
 *     'confirmarExclusao',
 *     'editarItem',
 *     'verDetalhes'
 * ]);
 *
 * // Abrir modal com dados
 * abrir('editarItem', { id: 123, nome: 'Teste' });
 *
 * // Verificar se está aberta
 * if (estaAberto('confirmarExclusao')) { ... }
 *
 * // Fechar modal
 * fechar('editarItem');
 * ```
 */
export function useGerenciadorModals(nomesModals: string[]): GerenciadorModals {
    // Cria refs para cada modal
    const modals: Record<string, Ref<EstadoModal>> = {};
    nomesModals.forEach(nome => {
        modals[nome] = ref<EstadoModal>({aberto: false});
    });

    /**
     * Abre uma modal, opcionalmente com dados
     */
    const abrir = (nome: string, dados?: any) => {
        if (!modals[nome]) {
            logger.warn(`Modal "${nome}" não foi registrada`);
            return;
        }
        modals[nome].value = {aberto: true, dados};
    };

    /**
     * Fecha uma modal e limpa seus dados
     */
    const fechar = (nome: string) => {
        if (!modals[nome]) {
            logger.warn(`Modal "${nome}" não foi registrada`);
            return;
        }
        modals[nome].value = {aberto: false, dados: undefined};
    };

    /**
     * Alterna o estado de uma modal
     */
    const alternar = (nome: string) => {
        if (!modals[nome]) {
            logger.warn(`Modal "${nome}" não foi registrada`);
            return;
        }
        const estadoAtual = modals[nome].value;
        modals[nome].value = {
            aberto: !estadoAtual.aberto,
            dados: estadoAtual.aberto ? undefined : estadoAtual.dados
        };
    };

    /**
     * Verifica se uma modal está aberta
     */
    const estaAberto = (nome: string): boolean => {
        return modals[nome]?.value.aberto ?? false;
    };

    /**
     * Obtém os dados de uma modal
     */
    const obterDados = (nome: string): any => {
        return modals[nome]?.value.dados;
    };

    /**
     * Fecha todas as modals
     */
    const fecharTodos = () => {
        Object.keys(modals).forEach(nome => fechar(nome));
    };

    return {
        modals,
        abrir,
        fechar,
        alternar,
        estaAberto,
        obterDados,
        fecharTodos
    };
}

// Aliases para compatibilidade legada durante a transição
/** @deprecated Use useGerenciadorModals */
export const useModalManager = useGerenciadorModals;
