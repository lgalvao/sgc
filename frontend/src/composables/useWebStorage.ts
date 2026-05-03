import {ref, type Ref, watch} from 'vue';

type ArmazenamentoWeb = Pick<Storage, 'getItem' | 'setItem' | 'removeItem'>;

export function useWebStorage<T>(
    armazenamento: ArmazenamentoWeb,
    chave: string,
    valorPadrao: T,
): Ref<T> {
    const lerValor = (): T => {
        const item = armazenamento.getItem(chave);
        if (item === null) {
            return valorPadrao;
        }

        try {
            return JSON.parse(item) as T;
        } catch {
            return item as unknown as T;
        }
    };

    const valorArmazenado = ref(lerValor()) as Ref<T>;

    watch(valorArmazenado, (novoValor) => {
        if (novoValor === null || novoValor === undefined) {
            armazenamento.removeItem(chave);
            return;
        }

        armazenamento.setItem(chave, JSON.stringify(novoValor));
    }, {deep: true, flush: 'sync'});

    return valorArmazenado;
}
