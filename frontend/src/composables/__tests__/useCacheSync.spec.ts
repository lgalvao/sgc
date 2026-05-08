import {beforeEach, describe, expect, it, vi} from 'vitest';
import {useCacheSync} from '../useCacheSync';
import {useUnidadeStore} from '@/stores/unidade';
import {useOrganizacaoStore} from '@/stores/organizacao';
import {usePainelStore} from '@/stores/painel';
import {useProcessoStore} from '@/stores/processo';
import {useSubprocessoStore} from '@/stores/subprocesso';
import {useMapasStore} from '@/stores/mapas';
import {createPinia, setActivePinia} from 'pinia';

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
    let painelStore: any;
    let processoStore: any;
    let subprocessoStore: any;
    let mapasStore: any;

    beforeEach(() => {
        setActivePinia(createPinia());
        unidadeStore = useUnidadeStore();
        organizacaoStore = useOrganizacaoStore();
        painelStore = usePainelStore();
        processoStore = useProcessoStore();
        subprocessoStore = useSubprocessoStore();
        mapasStore = useMapasStore();
        lastInstance = null;
        vi.clearAllMocks();
        vi.spyOn(unidadeStore, 'invalidarCache');
        vi.spyOn(organizacaoStore, 'invalidar');
        vi.spyOn(painelStore, 'invalidar');
        vi.spyOn(processoStore, 'invalidar');
        vi.spyOn(subprocessoStore, 'invalidar');
        vi.spyOn(mapasStore, 'invalidar');
    });

    it('deve conectar ao EventSource na URL correta', () => {
        const closeSync = useCacheSync();
        expect(lastInstance?.url).toBe('/api/eventos');
        closeSync();
    });

    it('deve invalidar apenas os caches organizacionais quando receber o evento org-cache-refreshed', () => {
        useCacheSync();

        lastInstance?.emit('org-cache-refreshed', {});

        expect(unidadeStore.invalidarCache).toHaveBeenCalled();
        expect(organizacaoStore.invalidar).toHaveBeenCalled();
        expect(painelStore.invalidar).toHaveBeenCalled();
        expect(processoStore.invalidar).not.toHaveBeenCalled();
        expect(subprocessoStore.invalidar).not.toHaveBeenCalled();
        expect(mapasStore.invalidar).not.toHaveBeenCalled();
    });

    it('não deve fechar a conexão em caso de erro transitório', () => {
        useCacheSync();

        if (lastInstance?.onerror) {
            lastInstance.onerror();
        }

        expect(lastInstance?.close).not.toHaveBeenCalled();
    });

    it('deve fechar a conexao ao chamar a funcao de retorno', () => {
        const closeSync = useCacheSync();

        closeSync();

        expect(lastInstance?.close).toHaveBeenCalled();
    });
});
