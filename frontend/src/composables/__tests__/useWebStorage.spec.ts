import {nextTick} from 'vue';
import {beforeEach, describe, expect, it} from 'vitest';
import {useWebStorage} from '@/composables/useWebStorage';

describe('useWebStorage', () => {
    beforeEach(() => {
        localStorage.clear();
    });

    it('deve persistir no armazenamento informado', async () => {
        const valor = useWebStorage(localStorage, 'chave-web-storage', 'valor-inicial');

        valor.value = 'valor-atualizado';
        await nextTick();

        expect(localStorage.getItem('chave-web-storage')).toBe(JSON.stringify('valor-atualizado'));
    });

    it('deve remover quando o valor for nulo', async () => {
        localStorage.setItem('chave-web-storage', JSON.stringify('existente'));
        const valor = useWebStorage<string | null>(localStorage, 'chave-web-storage', 'valor-inicial');

        valor.value = null;
        await nextTick();

        expect(localStorage.getItem('chave-web-storage')).toBeNull();
    });
});
