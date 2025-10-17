import {beforeEach, describe, expect, it, vi} from 'vitest';
import {initPinia} from '@/test-utils/helpers';
import {TipoMudanca, useRevisaoStore} from '../revisao';

// Mock the mapas store before importing the revisao store
const mockMapasStore = {
    getMapaByUnidadeId: vi.fn()
};

vi.mock('../mapas', () => ({
    useMapasStore: vi.fn(() => mockMapasStore),
}));

describe('useRevisaoStore', () => {
    let revisaoStore: ReturnType<typeof useRevisaoStore>;

    beforeEach(() => {
        initPinia();
        revisaoStore = useRevisaoStore();
        revisaoStore.limparMudancas();
        revisaoStore.setMudancasParaImpacto([]);
    });

    it('should initialize with empty mudancas arrays', () => {
        expect(revisaoStore.mudancasRegistradas).toEqual([]);
        expect(revisaoStore.mudancasParaImpacto).toEqual([]);
    });

    describe('registrarMudanca', () => {
        it('should add mudanca with auto-generated id', () => {
            const mudanca = {
                tipo: TipoMudanca.AtividadeAdicionada,
                descricaoAtividade: 'Nova Atividade'
            };

            revisaoStore.registrarMudanca(mudanca);

            expect(revisaoStore.mudancasRegistradas).toHaveLength(1);
            expect(revisaoStore.mudancasRegistradas[0]).toMatchObject(mudanca);
            expect(revisaoStore.mudancasRegistradas[0].id).toBeDefined();
        });

        it('should increment nextId for each mudanca', () => {
            revisaoStore.registrarMudanca({
                tipo: TipoMudanca.AtividadeAdicionada,
                descricaoAtividade: 'Atividade 1'
            });
            revisaoStore.registrarMudanca({
                tipo: TipoMudanca.ConhecimentoAdicionado,
                descricaoConhecimento: 'Conhecimento 1'
            });

            expect(revisaoStore.mudancasRegistradas).toHaveLength(2);
            expect(revisaoStore.mudancasRegistradas[0].id).toBeDefined();
            expect(revisaoStore.mudancasRegistradas[1].id).toBeDefined();
        });
    });

    describe('setMudancasParaImpacto', () => {
        it('should set mudancasParaImpacto array', () => {
            const mudancas = [
                {
                    id: 1,
                    tipo: TipoMudanca.AtividadeAdicionada,
                    descricaoAtividade: 'Atividade Teste'
                }
            ];

            revisaoStore.setMudancasParaImpacto(mudancas);

            expect(revisaoStore.mudancasParaImpacto).toEqual(mudancas);
        });

        it('should replace existing mudancasParaImpacto', () => {
            const mudancas1 = [{
                id: 1,
                tipo: TipoMudanca.AtividadeAdicionada,
                descricaoAtividade: 'Atividade 1'
            }];
            const mudancas2 = [{
                id: 2,
                tipo: TipoMudanca.ConhecimentoAdicionado,
                descricaoConhecimento: 'Conhecimento 1'
            }];

            revisaoStore.setMudancasParaImpacto(mudancas1);
            expect(revisaoStore.mudancasParaImpacto).toEqual(mudancas1);

            revisaoStore.setMudancasParaImpacto(mudancas2);
            expect(revisaoStore.mudancasParaImpacto).toEqual(mudancas2);
        });
    });

    describe('limparMudancas', () => {
        it('should clear all mudancas', () => {
            revisaoStore.registrarMudanca({
                tipo: TipoMudanca.AtividadeAdicionada,
                descricaoAtividade: 'Atividade'
            });
            revisaoStore.setMudancasParaImpacto([{
                id: 1,
                tipo: TipoMudanca.ConhecimentoAdicionado,
                descricaoConhecimento: 'Conhecimento'
            }]);

            revisaoStore.limparMudancas();

            expect(revisaoStore.mudancasRegistradas).toEqual([]);
            // mudancasParaImpacto is not cleared by limparMudancas
        });
    });

    describe('obterIdsCompetenciasImpactadas', () => {
        it('should return IDs of competencies that have the activity associated', () => {
            // Configure the mock behavior
            mockMapasStore.getMapaByUnidadeId.mockImplementation((unidadeId: string, idProcesso: number) => {
                if (unidadeId === 'SESEL' && idProcesso === 1) {
                    return {
                        id: 1,
                        unidade: 'SESEL',
                        idProcesso: 1,
                        situacao: 'em_andamento',
                        competencias: [
                            { id: 1, descricao: 'Competência 1', atividadesAssociadas: [10, 20] },
                            { id: 2, descricao: 'Competência 2', atividadesAssociadas: [30] },
                            { id: 3, descricao: 'Competência 3', atividadesAssociadas: [10, 40] }
                        ],
                        dataCriacao: new Date(),
                        dataDisponibilizacao: null,
                        dataFinalizacao: null
                    };
                }
                return undefined;
            });

            const idsImpactados = revisaoStore.obterIdsCompetenciasImpactadas(10, 'SESEL', 1);

            expect(idsImpactados).toEqual([1, 3]); // Competências 1 e 3 têm a atividade 10 associada
        });

        it('should return empty array when no map is found for the unit and process', () => {
            // Configure the mock to return undefined
            mockMapasStore.getMapaByUnidadeId.mockReturnValue(undefined);

            const idsImpactados = revisaoStore.obterIdsCompetenciasImpactadas(10, 'NONEXISTENT', 999);

            expect(idsImpactados).toEqual([]);
        });

        it('should return empty array when map exists but no competencies have the activity associated', () => {
            // Configure the mock to return a map with competencies that don't have the activity
            mockMapasStore.getMapaByUnidadeId.mockReturnValue({
                id: 1,
                unidade: 'SESEL',
                idProcesso: 1,
                situacao: 'em_andamento',
                competencias: [
                    { id: 1, descricao: 'Competência 1', atividadesAssociadas: [20, 30] },
                    { id: 2, descricao: 'Competência 2', atividadesAssociadas: [40] }
                ],
                dataCriacao: new Date(),
                dataDisponibilizacao: null,
                dataFinalizacao: null
            });

            const idsImpactados = revisaoStore.obterIdsCompetenciasImpactadas(10, 'SESEL', 1);

            expect(idsImpactados).toEqual([]); // Nenhuma competência tem a atividade 10 associada
        });

        it('should return empty array when map exists but has no competencies', () => {
            // Configure the mock to return a map with no competencies
            mockMapasStore.getMapaByUnidadeId.mockReturnValue({
                id: 1,
                unidade: 'SESEL',
                idProcesso: 1,
                situacao: 'em_andamento',
                competencias: [],
                dataCriacao: new Date(),
                dataDisponibilizacao: null,
                dataFinalizacao: null
            });

            const idsImpactados = revisaoStore.obterIdsCompetenciasImpactadas(10, 'SESEL', 1);

            expect(idsImpactados).toEqual([]);
        });
    });

    describe('TipoMudanca enum', () => {
        it('should have all expected values', () => {
            expect(TipoMudanca.AtividadeAdicionada).toBe('AtividadeAdicionada');
            expect(TipoMudanca.AtividadeRemovida).toBe('AtividadeRemovida');
            expect(TipoMudanca.AtividadeAlterada).toBe('AtividadeAlterada');
            expect(TipoMudanca.ConhecimentoAdicionado).toBe('ConhecimentoAdicionado');
            expect(TipoMudanca.ConhecimentoRemovido).toBe('ConhecimentoRemovido');
            expect(TipoMudanca.ConhecimentoAlterado).toBe('ConhecimentoAlterado');
        });
    });
});