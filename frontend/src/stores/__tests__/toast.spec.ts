import {beforeEach, describe, expect, it} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useToastStore} from '../toast';

describe('Toast store', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
    });

    it('deve inicializar com toastPendente null', () => {
        const store = useToastStore();
        expect(store.toastPendente).toBeNull();
    });

    it('deve setar uma mensagem pending', () => {
        const store = useToastStore();
        store.setPending('Mensagem de teste');
        expect(store.toastPendente).toEqual({mensagem: 'Mensagem de teste', variante: 'success'});
    });

    it('deve consumir a mensagem pending e retornar o valor', () => {
        const store = useToastStore();
        store.setPending('Mensagem de teste');

        const consumed = store.consumePending();

        expect(consumed).toEqual({mensagem: 'Mensagem de teste', variante: 'success'});
        expect(store.toastPendente).toBeNull();
    });

    it('deve retornar null se nao houver mensagem pendente ao consumir', () => {
        const store = useToastStore();
        const consumed = store.consumePending();

        expect(consumed).toBeNull();
    });
});
