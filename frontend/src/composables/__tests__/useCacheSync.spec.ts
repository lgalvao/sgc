import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {useCacheSync} from '../useCacheSync';
import {useUnidadeStore} from '@/stores/unidade';
import {useOrganizacaoStore} from '@/stores/organizacao';
import {usePainelStore} from '@/stores/painel';
import {useSubprocessoStore} from '@/stores/subprocesso';
import {useMapasStore} from '@/stores/mapas';
import {logger} from '@/utils';
import {criarPiniaDeTeste} from '@/test-utils/storeTestHelpers';

const invalidateQueriesMock = vi.fn();

vi.mock('@pinia/colada', async (importOriginal) => {
    const actual = await importOriginal<typeof import('@pinia/colada')>();
    return {
        ...actual,
        useQueryCache: () => ({
            ...actual.useQueryCache(),
            invalidateQueries: invalidateQueriesMock,
        }),
    };
});

// Global to hold the last created instance
let lastInstance: EventSourceMock | null = null;
const instanciasCriadas: EventSourceMock[] = [];
const fechamentosPendentes: Array<() => void> = [];

// Mock EventSource
class EventSourceMock {
    static readonly CONNECTING = 0;
    static readonly OPEN = 1;
    static readonly CLOSED = 2;

    public onmessage: ((event: any) => void) | null = null;
    public onerror: ((event?: any) => void) | null = null;
    public close = vi.fn();
    public readyState = EventSourceMock.OPEN;
    private listeners: Record<string, ((event: any) => void)[]> = {};

    constructor(public url: string) {
        // eslint-disable-next-line @typescript-eslint/no-this-alias
        const self = this;
        lastInstance = self;
        instanciasCriadas.push(self);
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
    let subprocessoStore: any;
    let mapasStore: any;

    beforeEach(() => {
        criarPiniaDeTeste();
        unidadeStore = useUnidadeStore();
        organizacaoStore = useOrganizacaoStore();
        painelStore = usePainelStore();
        subprocessoStore = useSubprocessoStore();
        mapasStore = useMapasStore();
        lastInstance = null;
        instanciasCriadas.length = 0;
        vi.clearAllMocks();
        vi.spyOn(unidadeStore, 'invalidar');
        vi.spyOn(organizacaoStore, 'invalidar');
        vi.spyOn(painelStore, 'invalidar');
        vi.spyOn(subprocessoStore, 'invalidar');
        vi.spyOn(mapasStore, 'invalidar');
        vi.spyOn(logger, 'warn').mockImplementation(() => logger);
        invalidateQueriesMock.mockReset();
    });

    afterEach(() => {
        while (fechamentosPendentes.length > 0) {
            fechamentosPendentes.pop()?.();
        }
    });

    it('deve conectar ao EventSource na URL correta', () => {
        const closeSync = useCacheSync();
        fechamentosPendentes.push(closeSync);
        expect(lastInstance?.url).toBe('/api/eventos');
    });

    it('deve invalidar apenas os caches organizacionais quando receber o evento org-cache-refreshed', () => {
        fechamentosPendentes.push(useCacheSync());

        lastInstance?.emit('org-cache-refreshed', {});

        expect(unidadeStore.invalidar).toHaveBeenCalled();
        expect(organizacaoStore.invalidar).toHaveBeenCalled();
        expect(painelStore.invalidar).toHaveBeenCalled();
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ['painel']});
        expect(subprocessoStore.invalidar).not.toHaveBeenCalled();
        expect(mapasStore.invalidar).not.toHaveBeenCalled();
    });

    it('deve preservar caches críticos e limpar marcações locais do painel ao invalidar por SSE organizacional', () => {
        subprocessoStore.contextoEdicao = {detalhes: {codigo: 200, situacao: 'MAPA'}} as any;
        mapasStore.sincronizarMapa(200, {
            codigo: 1,
            subprocessoCodigo: 200,
            observacoes: 'Mapa vivo',
            competencias: [],
            atividades: [],
            situacao: 'EM_ANDAMENTO',
        } as any);
        painelStore.registrarLeitura([2]);

        fechamentosPendentes.push(useCacheSync());
        lastInstance?.emit('org-cache-refreshed', {});

        expect(painelStore.isMarcadoComoLido(2)).toBe(false);
        expect(subprocessoStore.contextoEdicao).toEqual(expect.objectContaining({
            detalhes: expect.objectContaining({codigo: 200}),
        }));
        expect(mapasStore.obterMapa(200)).toEqual(expect.objectContaining({
            subprocessoCodigo: 200,
            observacoes: 'Mapa vivo',
        }));
        expect(mapasStore.mapaDisponivel(200)).toBe(true);
    });

    it('não deve fechar a conexão em caso de erro transitório durante reconexão', () => {
        fechamentosPendentes.push(useCacheSync());
        if (lastInstance) {
            lastInstance.readyState = EventSourceMock.CONNECTING;
        }

        if (lastInstance?.onerror) {
            lastInstance.onerror();
        }

        expect(lastInstance?.close).not.toHaveBeenCalled();
        expect(logger.warn).not.toHaveBeenCalled();
    });

    it('não deve logar warning quando a conexão SSE já estiver fechada', () => {
        fechamentosPendentes.push(useCacheSync());
        if (lastInstance) {
            lastInstance.readyState = EventSourceMock.CLOSED;
        }

        if (lastInstance?.onerror) {
            lastInstance.onerror();
        }

        expect(lastInstance?.close).not.toHaveBeenCalled();
        expect(logger.warn).not.toHaveBeenCalled();
    });

    it('não deve logar warning ao encerrar a conexão manualmente', () => {
        const closeSync = useCacheSync();
        fechamentosPendentes.push(closeSync);
        closeSync();

        if (lastInstance?.onerror) {
            lastInstance.onerror();
        }

        expect(logger.warn).not.toHaveBeenCalled();
    });

    it('deve fechar a conexao ao chamar a funcao de retorno', () => {
        const closeSync = useCacheSync();
        fechamentosPendentes.push(closeSync);

        closeSync();

        expect(lastInstance?.close).toHaveBeenCalled();
    });

    it('deve fechar a conexao ao receber pagehide', () => {
        fechamentosPendentes.push(useCacheSync());

        window.dispatchEvent(new Event('pagehide'));

        expect(lastInstance?.close).toHaveBeenCalled();
    });

    it('deve reabrir a conexao ao receber pageshow apos pagehide', () => {
        fechamentosPendentes.push(useCacheSync());
        const primeiraInstancia = lastInstance;

        window.dispatchEvent(new Event('pagehide'));
        window.dispatchEvent(new Event('pageshow'));

        expect(instanciasCriadas).toHaveLength(2);
        expect(lastInstance).not.toBe(primeiraInstancia);
    });
});
