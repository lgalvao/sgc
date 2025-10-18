import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useProcessosStore} from '@/stores/processos';
import {useAtividadesStore} from '@/stores/atividades';
import {mockProcessoDetalhe, mockProcessosPainel} from "@/test-utils/mocks";

// Mock das stores
vi.mock('@/stores/processos', () => ({
    useProcessosStore: vi.fn(() => ({
        processosPainel: [],
        processoDetalhe: null,
        fetchProcessosPainel: vi.fn(),
        fetchProcessoDetalhe: vi.fn(),
    })),
}));

vi.mock('@/stores/atividades', () => ({
    useAtividadesStore: vi.fn(() => ({
        atividadesPorSubprocesso: new Map(),
        getAtividadesPorSubprocesso: vi.fn().mockReturnValue([]),
        fetchAtividadesPorSubprocesso: vi.fn(),
        importarAtividades: vi.fn(),
    })),
}));

describe('ImportarAtividadesModal Store Logic', () => {
    let processosStore: ReturnType<typeof useProcessosStore>;
    let atividadesStore: ReturnType<typeof useAtividadesStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        processosStore = useProcessosStore();
        atividadesStore = useAtividadesStore();
        vi.clearAllMocks();

        // Setup default mocks
        vi.mocked(processosStore.fetchProcessosPainel).mockResolvedValue(undefined);
        vi.mocked(processosStore.fetchProcessoDetalhe).mockResolvedValue(undefined);
        vi.mocked(atividadesStore.fetchAtividadesPorSubprocesso).mockResolvedValue(undefined);
        processosStore.processosPainel = mockProcessosPainel;
    });

    it('should fetch available processes', async () => {
        await processosStore.fetchProcessosPainel();
        expect(processosStore.fetchProcessosPainel).toHaveBeenCalled();
    });

    it('should fetch process details', async () => {
        processosStore.processoDetalhe = mockProcessoDetalhe;
        await processosStore.fetchProcessoDetalhe(1);
        expect(processosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
    });

    it('should fetch activities', async () => {
        processosStore.processoDetalhe = mockProcessoDetalhe;
        await atividadesStore.fetchAtividadesPorSubprocesso(101);
        expect(atividadesStore.fetchAtividadesPorSubprocesso).toHaveBeenCalledWith(101);
    });

    it('should import activities', async () => {
        await atividadesStore.importarAtividades(1, 101);
        expect(atividadesStore.importarAtividades).toHaveBeenCalledWith(1, 101);
    });
});