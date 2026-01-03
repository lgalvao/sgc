import { setActivePinia, createPinia } from 'pinia';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useSubprocessosStore } from '../subprocessos';
import { useProcessosStore } from '../processos';
// import { useNotificacoesStore } from '../notificacoes'; // Module does not exist

// Mock dependencies
vi.mock('../processos');
// Mock feedback store instead of notificacoes if that is what was intended
vi.mock('../feedback', () => ({
    useFeedbackStore: vi.fn(() => ({
        show: vi.fn()
    }))
}));

// Mock API Client
vi.mock('@/axios-setup', () => ({
    apiClient: {
        post: vi.fn().mockResolvedValue({ data: {} }),
        get: vi.fn().mockResolvedValue({ data: {} })
    }
}));

describe('Subprocessos Store', () => {
    let store: ReturnType<typeof useSubprocessosStore>;
    let processosStore: any;

    beforeEach(() => {
        setActivePinia(createPinia());
        store = useSubprocessosStore();

        // Setup mocks
        processosStore = {
            fetchProcessoDetalhe: vi.fn(),
            buscarProcessoDetalhe: vi.fn(), // Add the method actually used
            processoSelecionado: { codigo: 1 },
            processoDetalhe: { codigo: 1 } // Add the property actually used
        };
        (useProcessosStore as any).mockReturnValue(processosStore);
    });

    it('alterarDataLimiteSubprocesso deve delegar para processosStore', async () => {
        const id = 123;
        const dados = { novaData: '2024-12-31', motivo: 'Teste' };

        // Mock apiClient.post inside the action
        const { apiClient } = await import('@/axios-setup');

        await store.alterarDataLimiteSubprocesso(id, dados);

        expect(apiClient.post).toHaveBeenCalledWith(`/subprocessos/${id}/data-limite`, {
             novaDataLimite: dados.novaData
        });
    });
});
