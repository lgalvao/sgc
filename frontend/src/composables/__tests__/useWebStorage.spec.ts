import {nextTick} from 'vue';
import {beforeEach, describe, expect, it} from 'vitest';
import {removerDoArmazenamento, removerMultiplosDoArmazenamento, useWebStorage} from '@/composables/useWebStorage';

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

describe('helpers de remoção do useWebStorage', () => {
    it('deve remover uma chave', () => {
        localStorage.setItem('chave-unica', '1');

        removerDoArmazenamento(localStorage, 'chave-unica');

        expect(localStorage.getItem('chave-unica')).toBeNull();
    });

    it('deve remover múltiplas chaves', () => {
        localStorage.setItem('chave-1', '1');
        localStorage.setItem('chave-2', '2');

        removerMultiplosDoArmazenamento(localStorage, ['chave-1', 'chave-2']);

        expect(localStorage.getItem('chave-1')).toBeNull();
        expect(localStorage.getItem('chave-2')).toBeNull();
    });
});
