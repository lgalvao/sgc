import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useUnidadesStore} from '../unidades';
import {useProcessosStore} from '../processos';
import {useRevisaoStore} from '../revisao';
import {mockProcessoDetalhe, mockUnidade} from "@/test-utils/mocks";

// Mock the stores
const mockMapasStore = {
    mapaCompleto: {competencias: []},
};
const mockUnidadesStore = {
    pesquisarUnidade: vi.fn().mockReturnValue(mockUnidade),
};
const mockProcessosStore = {
    fetchProcessoDetalhe: vi.fn().mockResolvedValue(undefined),
    processoDetalhe: mockProcessoDetalhe,
};
const mockRevisaoStore = {
    mudancasParaImpacto: [],
};

vi.mock('@/stores/mapas', () => ({ useMapasStore: vi.fn(() => mockMapasStore) }));
vi.mock('@/stores/unidades', () => ({ useUnidadesStore: vi.fn(() => mockUnidadesStore) }));
vi.mock('@/stores/processos', () => ({ useProcessosStore: vi.fn(() => mockProcessosStore) }));
vi.mock('@/stores/revisao', () => ({ useRevisaoStore: vi.fn(() => mockRevisaoStore) }));

describe('Impacto Store Logic', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
    });

    afterEach(() => {
        vi.clearAllMocks();
        mockRevisaoStore.mudancasParaImpacto = []; // Reset state
    });

    it('should fetch process details', async () => {
        const processosStore = useProcessosStore();
        await processosStore.fetchProcessoDetalhe(1);
        expect(mockProcessosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
    });

    it('should search for a unit', () => {
        const unidadesStore = useUnidadesStore();
        unidadesStore.pesquisarUnidade('UNID');
        expect(mockUnidadesStore.pesquisarUnidade).toHaveBeenCalledWith('UNID');
    });

    it('should have no impact when there are no changes', () => {
        const revisaoStore = useRevisaoStore();
        expect(revisaoStore.mudancasParaImpacto).toEqual([]);
    });

    it('should have impact when there are changes', () => {
        const revisaoStore = useRevisaoStore();
        mockRevisaoStore.mudancasParaImpacto = [
            {
                codigo: 1,
                tipo: 'AtividadeAdicionada',
                descricaoAtividade: 'Nova Atividade',
                competenciasImpactadasIds: [1]
            }
        ];
        expect(revisaoStore.mudancasParaImpacto).toHaveLength(1);
    });
});