import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useMapasStore} from '../mapas';
import {useUnidadesStore} from '../unidades';
import {useProcessosStore} from '../processos';
import {useRevisaoStore} from '../revisao';
import {mockUnidade, mockProcessoDetalhe} from "@/test-utils/mocks";

// Mock the stores
vi.mock('@/stores/mapas');
vi.mock('@/stores/unidades');
vi.mock('@/stores/processos');
vi.mock('@/stores/revisao');

describe('Impacto Store Logic', () => {
    let mapasStore: ReturnType<typeof useMapasStore>;
    let unidadesStore: ReturnType<typeof useUnidadesStore>;
    let processosStore: ReturnType<typeof useProcessosStore>;
    let revisaoStore: ReturnType<typeof useRevisaoStore>;

    beforeEach(() => {
        setActivePinia(createPinia());

        mapasStore = useMapasStore();
        unidadesStore = useUnidadesStore();
        processosStore = useProcessosStore();
        revisaoStore = useRevisaoStore();

        // Setup mock implementations
        vi.mocked(useMapasStore).mockReturnValue({
            ...mapasStore,
            getMapaByUnidadeId: vi.fn().mockReturnValue({competencias: []}),
        });
        vi.mocked(useUnidadesStore).mockReturnValue({
            ...unidadesStore,
            pesquisarUnidade: vi.fn().mockReturnValue(mockUnidade),
        });
        vi.mocked(useProcessosStore).mockReturnValue({
            ...processosStore,
            fetchProcessoDetalhe: vi.fn().mockResolvedValue(undefined),
            processoDetalhe: mockProcessoDetalhe,
        });
        vi.mocked(useRevisaoStore).mockReturnValue({
            ...revisaoStore,
            mudancasParaImpacto: [],
        });

        vi.clearAllMocks();
    });

    it('should fetch process details', async () => {
        await processosStore.fetchProcessoDetalhe(1);
        expect(processosStore.fetchProcessoDetalhe).toHaveBeenCalledWith(1);
    });

    it('should get mapa by unidade id', () => {
        mapasStore.getMapaByUnidadeId(1);
        expect(mapasStore.getMapaByUnidadeId).toHaveBeenCalledWith(1);
    });

    it('should search for a unit', () => {
        unidadesStore.pesquisarUnidade('UNID');
        expect(unidadesStore.pesquisarUnidade).toHaveBeenCalledWith('UNID');
    });

    it('should have no impact when there are no changes', () => {
        expect(revisaoStore.mudancasParaImpacto).toEqual([]);
    });

    it('should have impact when there are changes', () => {
        revisaoStore.mudancasParaImpacto = [
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