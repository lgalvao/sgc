import {ref, type Ref, watch} from 'vue';

type ArmazenamentoWeb = Pick<Storage, 'getItem' | 'setItem' | 'removeItem'>;

const toAny = (val: unknown) => {
    if (typeof val === 'string') {
        try {
            return JSON.parse(val);
        } catch {
            return val;
        }
    }
    return val;
};

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
            return toAny(JSON.parse(item));
        } catch {
            return toAny(item);
        }
    };

    const valorArmazenado = toAny(ref(lerValor()));

    watch(valorArmazenado, (novoValor) => {
        if (novoValor === undefined || Object.is(novoValor, null)) {
            armazenamento.removeItem(chave);
        } else {
            armazenamento.setItem(chave, JSON.stringify(novoValor));
        }
    }, {deep: true, flush: 'sync'});

    return valorArmazenado;
}
