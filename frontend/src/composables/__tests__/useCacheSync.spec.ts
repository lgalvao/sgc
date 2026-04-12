import {beforeEach, describe, expect, it, vi} from 'vitest';
import {useCacheSync} from '../useCacheSync';
import {useUnidadeStore} from '@/stores/unidade';
import {useOrganizacaoStore} from '@/stores/organizacao';
import {createTestingPinia} from '@pinia/testing';
import {setActivePinia} from 'pinia';

// Global to hold the last created instance
let lastInstance: EventSourceMock | null = null;

// Mock EventSource
class EventSourceMock {
  public onmessage: ((event: any) => void) | null = null;
  public onerror: ((event?: any) => void) | null = null;
  public close = vi.fn();
  private listeners: Record<string, ((event: any) => void)[]> = {};

  constructor(public url: string) {
    // eslint-disable-next-line @typescript-eslint/no-this-alias
    const self = this;
    lastInstance = self;
  }

  addEventListener(event: string, callback: (event: any) => void) {
    if (!this.listeners[event]) {
      this.listeners[event] = [];
    }
    this.listeners[event].push(callback);

    if (event === 'error') {
        this.onerror = (e?: any) => callback(e);
    }
  }

  emit(event: string, data: any) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(cb => cb({data}));
    }
  }
}

vi.stubGlobal('EventSource', EventSourceMock);

describe('useCacheSync', () => {
  let unidadeStore: any;
  let organizacaoStore: any;

  beforeEach(() => {
    setActivePinia(createTestingPinia({
      createSpy: vi.fn,
    }));
    unidadeStore = useUnidadeStore();
    organizacaoStore = useOrganizacaoStore();
    lastInstance = null;
    vi.clearAllMocks();
  });

  it('deve conectar ao EventSource na URL correta', () => {
    const closeSync = useCacheSync();
    expect(lastInstance?.url).toBe('/api/eventos');
    closeSync();
  });

  it('deve invalidar os caches quando receber o evento org-cache-refreshed', () => {
    useCacheSync();

    lastInstance?.emit('org-cache-refreshed', {});

    expect(unidadeStore.invalidarCache).toHaveBeenCalled();
    expect(organizacaoStore.$reset).toHaveBeenCalled();
  });

  it('deve fechar a conexao em caso de erro', () => {
    useCacheSync();

    if (lastInstance?.onerror) {
      lastInstance.onerror();
    }

    expect(lastInstance?.close).toHaveBeenCalled();
  });

  it('deve fechar a conexao ao chamar a funcao de retorno', () => {
    const closeSync = useCacheSync();

    closeSync();

    expect(lastInstance?.close).toHaveBeenCalled();
  });
});
