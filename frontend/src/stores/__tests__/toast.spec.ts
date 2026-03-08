import {beforeEach, describe, expect, it} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useToastStore} from '../toast';

describe('Toast Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('deve inicializar com pendingToast null', () => {
    const store = useToastStore();
    expect(store.pendingToast).toBeNull();
  });

  it('deve setar uma mensagem pending', () => {
    const store = useToastStore();
    store.setPending('Mensagem de teste');
    expect(store.pendingToast).toEqual({ body: 'Mensagem de teste' });
  });

  it('deve consumir a mensagem pending e retornar o valor', () => {
    const store = useToastStore();
    store.setPending('Mensagem de teste');
    
    const consumed = store.consumePending();
    
    expect(consumed).toEqual({ body: 'Mensagem de teste' });
    expect(store.pendingToast).toBeNull();
  });

  it('deve retornar null se nao houver mensagem pendente ao consumir', () => {
    const store = useToastStore();
    const consumed = store.consumePending();
    
    expect(consumed).toBeNull();
  });
});
