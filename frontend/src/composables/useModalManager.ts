import {ref, type Ref} from 'vue';
import {logger} from '@/utils';

/**
 * Estado de uma modal individual
 */
interface ModalState {
    isOpen: boolean;
    data?: any;
}

/**
 * Gerenciador de modals
 */
interface ModalManager {
    modals: Record<string, Ref<ModalState>>;
    open: (name: string, data?: any) => void;
    close: (name: string) => void;
    toggle: (name: string) => void;
    isOpen: (name: string) => boolean;
    getData: (name: string) => any;
    closeAll: () => void;
}

/**
 * Composable para gerenciar estado de múltiplas modals
 * 
 * Simplifica o gerenciamento de múltiplas modals em um componente,
 * eliminando a necessidade de criar refs individuais para cada modal.
 * 
 * @param modalNames - Lista de nomes das modals a gerenciar
 * @returns Gerenciador de modals
 * 
 * @example
 * ```ts
 * const { modals, open, close, isOpen } = useModalManager([
 *     'confirmDelete',
 *     'editItem',
 *     'viewDetails'
 * ]);
 * 
 * // Abrir modal com dados
 * open('editItem', { id: 123, name: 'Test' });
 * 
 * // Verificar se está aberta
 * if (isOpen('confirmDelete')) { ... }
 * 
 * // Fechar modal
 * close('editItem');
 * ```
 */
export function useModalManager(modalNames: string[]): ModalManager {
    // Cria refs para cada modal
    const modals: Record<string, Ref<ModalState>> = {};
    modalNames.forEach(name => {
        modals[name] = ref<ModalState>({ isOpen: false });
    });

    /**
     * Abre uma modal, opcionalmente com dados
     */
    const open = (name: string, data?: any) => {
        if (!modals[name]) {
            logger.warn(`Modal "${name}" não foi registrada`);
            return;
        }
        modals[name].value = { isOpen: true, data };
    };

    /**
     * Fecha uma modal e limpa seus dados
     */
    const close = (name: string) => {
        if (!modals[name]) {
            logger.warn(`Modal "${name}" não foi registrada`);
            return;
        }
        modals[name].value = { isOpen: false, data: undefined };
    };

    /**
     * Alterna o estado de uma modal
     */
    const toggle = (name: string) => {
        if (!modals[name]) {
            logger.warn(`Modal "${name}" não foi registrada`);
            return;
        }
        const currentState = modals[name].value;
        modals[name].value = { 
            isOpen: !currentState.isOpen,
            data: currentState.isOpen ? undefined : currentState.data
        };
    };

    /**
     * Verifica se uma modal está aberta
     */
    const isOpen = (name: string): boolean => {
        return modals[name]?.value.isOpen ?? false;
    };

    /**
     * Obtém os dados de uma modal
     */
    const getData = (name: string): any => {
        return modals[name]?.value.data;
    };

    /**
     * Fecha todas as modals
     */
    const closeAll = () => {
        Object.keys(modals).forEach(name => close(name));
    };

    return {
        modals,
        open,
        close,
        toggle,
        isOpen,
        getData,
        closeAll
    };
}
