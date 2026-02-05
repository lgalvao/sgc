import {beforeEach, describe, expect, it, vi} from 'vitest';
import {nextTick} from 'vue';
import {
    removeFromLocalStorage,
    removeMultipleFromLocalStorage,
    useLocalStorage,
    useLocalStorageMultiple
} from '@/composables/useLocalStorage';

describe('useLocalStorage', () => {
    beforeEach(() => {
        localStorage.clear();
        vi.clearAllMocks();
    });

    it('deve retornar o valor padrão se o localStorage estiver vazio', () => {
        const val = useLocalStorage('test-key', 'default');
        expect(val.value).toBe('default');
    });

    it('deve retornar o valor do localStorage se existir', () => {
        localStorage.setItem('test-key', JSON.stringify('stored'));
        const val = useLocalStorage('test-key', 'default');
        expect(val.value).toBe('stored');
    });

    it('deve lidar com JSON inválido no localStorage', () => {
        localStorage.setItem('test-key', 'not-json');
        const val = useLocalStorage('test-key', 'default');
        expect(val.value).toBe('not-json');
    });

    it('deve atualizar o localStorage quando o valor mudar', async () => {
        const val = useLocalStorage('test-key', 'default');
        val.value = 'new-value';

        await nextTick();
        expect(localStorage.getItem('test-key')).toBe(JSON.stringify('new-value'));
    });

    it('deve remover do localStorage se o valor for null ou undefined', async () => {
        localStorage.setItem('test-key', JSON.stringify('stored'));
        const val = useLocalStorage<string | null>('test-key', 'default');

        val.value = null;
        await nextTick();
        expect(localStorage.getItem('test-key')).toBeNull();
    });

    it('deve funcionar com objetos complexos', async () => {
        const defaultVal = { a: 1 };
        const val = useLocalStorage('test-key', defaultVal);

        val.value.a = 2;
        await nextTick();
        expect(localStorage.getItem('test-key')).toBe(JSON.stringify({ a: 2 }));
    });
});

describe('useLocalStorageMultiple', () => {
    it('deve gerenciar múltiplas chaves', async () => {
        const items = useLocalStorageMultiple({
            k1: 'v1',
            k2: 10
        });

        expect(items.k1.value).toBe('v1');
        expect(items.k2.value).toBe(10);

        items.k1.value = 'v2';
        await nextTick();
        expect(localStorage.getItem('k1')).toBe(JSON.stringify('v2'));
    });
});

describe('removeFromLocalStorage utilities', () => {
    it('removeFromLocalStorage deve remover uma chave', () => {
        localStorage.setItem('key1', 'val');
        removeFromLocalStorage('key1');
        expect(localStorage.getItem('key1')).toBeNull();
    });

    it('removeMultipleFromLocalStorage deve remover múltiplas chaves', () => {
        localStorage.setItem('key1', 'val1');
        localStorage.setItem('key2', 'val2');
        removeMultipleFromLocalStorage(['key1', 'key2']);
        expect(localStorage.getItem('key1')).toBeNull();
        expect(localStorage.getItem('key2')).toBeNull();
    });
});
