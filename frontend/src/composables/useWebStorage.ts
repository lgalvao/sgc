import {ref, type Ref, watch} from 'vue';

type ArmazenamentoWeb = Pick<Storage, 'getItem' | 'setItem' | 'removeItem'>;

export function useWebStorage<T>(
    armazenamento: ArmazenamentoWeb,
    chave: string,
    valorPadrao: T,
): Ref<T> {
    const lerValor = (): T => {
        const item = armazenamento.getItem(chave);
        if (!item) {
            return valorPadrao;
        }

        try {
            return JSON.parse(item) as T;
        } catch {
            const valorCru: unknown = item;
            return valorCru as T;
        }
    };

    const valorArmazenado = ref(lerValor()) as Ref<T>;

    watch(valorArmazenado, (novoValor) => {
        if (novoValor === undefined || Object.is(novoValor, null)) {
            armazenamento.removeItem(chave);
        } else {
            armazenamento.setItem(chave, JSON.stringify(novoValor));
        }
    }, {deep: true, flush: 'sync'});

    return valorArmazenado;
}
