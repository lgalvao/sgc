import {beforeEach, describe, expect, it, vi} from 'vitest';
import {nextTick} from 'vue';
import {useLocalStorage} from '@/composables/useLocalStorage';

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
        const defaultVal = {a: 1};
        const val = useLocalStorage('test-key', defaultVal);

        val.value.a = 2;
        await nextTick();
        expect(localStorage.getItem('test-key')).toBe(JSON.stringify({a: 2}));
    });
});
