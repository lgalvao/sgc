import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {mapSituacaoProcesso, mapTipoProcesso, useProcessosStore} from '../processos'; // Importar as funções diretamente
import {Processo, SituacaoProcesso, Subprocesso, TipoProcesso} from '@/types/tipos';

// Mock the JSON imports
vi.mock('../../mocks/processos.json', () => ({
    default: [
        {
            "id": 1,
            "descricao": "Mapeamento de competências - 2025",
            "tipo": "Mapeamento",
            "dataLimite": "2025-06-30",
            "dataFinalizacao": "2025-10-15",
            "situacao": "Finalizado"
        },
        {
            "id": 2,
            "descricao": "Revisão de mapeamento STIC/COINF - 2025",
            "tipo": "Revisão",
            "dataLimite": "2025-07-15",
            "situacao": "Em andamento"
        }
    ]
}));
vi.mock('../../mocks/subprocessos.json', () => ({
    default: [
        {
            "id": 1,
            "idProcesso": 1,
            "unidade": "SESEL",
            "dataLimiteEtapa1": "2025-06-10",
            "dataLimiteEtapa2": "2025-06-20",
            "dataFimEtapa1": "2025-06-05",
            "dataFimEtapa2": "2025-06-15",
            "situacao": "Concluído",
            "unidadeAtual": "SESEL",
            "unidadeAnterior": null,
            "movimentacoes": [],
            "analises": []
        },
        {
            "id": 2,
            "idProcesso": 1,
            "unidade": "COSIS",
            "dataLimiteEtapa1": "2025-06-10",
            "dataLimiteEtapa2": "2025-06-20",
            "dataFimEtapa1": null,
            "dataFimEtapa2": null,
            "situacao": "Em andamento",
            "unidadeAtual": "COSIS",
            "unidadeAnterior": "SESEL",
            "movimentacoes": [],
            "analises": []
        },
        {
            "id": 3,
            "idProcesso": 2,
            "unidade": "SEDESENV",
            "dataLimiteEtapa1": "2025-07-01",
            "dataLimiteEtapa2": "2025-07-10",
            "dataFimEtapa1": null,
            "dataFimEtapa2": null,
            "situacao": "Em andamento",
            "unidadeAtual": "SEDESENV",
            "unidadeAnterior": null,
            "movimentacoes": [],
            "analises": []
        }
    ]
}));

describe('useProcessosStore', () => {
    let processosStore: ReturnType<typeof useProcessosStore>;

    // Helper to parse dates in mock data for comparison
    const parseProcessoDates = (processo: {
        id: number,
        descricao: string,
        tipo: string,
        dataLimite: string,
        dataFinalizacao?: string | null,
        situacao: string
    }): Processo => ({
        ...processo,
        tipo: processo.tipo === 'Mapeamento' ? TipoProcesso.MAPEAMENTO :
            processo.tipo === 'Revisão' ? TipoProcesso.REVISAO : TipoProcesso.DIAGNOSTICO,
        situacao: processo.situacao === 'Finalizado' ? SituacaoProcesso.FINALIZADO :
            processo.situacao === 'Em andamento' ? SituacaoProcesso.EM_ANDAMENTO : SituacaoProcesso.CRIADO,
        dataLimite: new Date(processo.dataLimite),
        dataFinalizacao: processo.dataFinalizacao ? new Date(processo.dataFinalizacao) : null,
    });

    const parsesubprocessoDates = (pu: {
        id: number,
        idProcesso: number,
        unidade: string,
        dataLimiteEtapa1?: string | null,
        dataLimiteEtapa2?: string | null,
        dataFimEtapa1?: string | null,
        dataFimEtapa2?: string | null,
        situacao: string,
        unidadeAtual: string,
        unidadeAnterior: string | null
    }): Subprocesso => ({
        ...pu,
        situacao: pu.situacao as Subprocesso['situacao'],
        dataLimiteEtapa1: pu.dataLimiteEtapa1 ? new Date(pu.dataLimiteEtapa1) : new Date(),
        dataLimiteEtapa2: pu.dataLimiteEtapa2 ? new Date(pu.dataLimiteEtapa2) : null,
        dataFimEtapa1: pu.dataFimEtapa1 ? new Date(pu.dataFimEtapa1) : null,
        dataFimEtapa2: pu.dataFimEtapa2 ? new Date(pu.dataFimEtapa2) : null,
        movimentacoes: [],
        analises: [],
    });

    beforeEach(() => {
        setActivePinia(createPinia());
        processosStore = useProcessosStore();
        // Manually reset the store state based on the initial mock data, parsing dates
        processosStore.$patch({
            processos: [
                {
                    "id": 1,
                    "descricao": "Mapeamento de competências - 2025",
                    "tipo": "Mapeamento",
                    "dataLimite": "2025-06-30",
                    "dataFinalizacao": "2025-10-15",
                    "situacao": "Finalizado"
                },
                {
                    "id": 2,
                    "descricao": "Revisão de mapeamento STIC/COINF - 2025",
                    "tipo": "Revisão",
                    "dataLimite": "2025-07-15",
                    "situacao": "Em andamento"
                }
            ].map(parseProcessoDates),
            subprocessos: [
                {
                    "id": 1,
                    "idProcesso": 1,
                    "unidade": "SESEL",
                    "dataLimiteEtapa1": "2025-06-10",
                    "dataLimiteEtapa2": "2025-06-20",
                    "dataFimEtapa1": "2025-06-05",
                    "dataFimEtapa2": "2025-06-15",
                    "situacao": "Concluído",
                    "unidadeAtual": "SESEL",
                    "unidadeAnterior": null,
                    "analises": []
                },
                {
                    "id": 2,
                    "idProcesso": 1,
                    "unidade": "COSIS",
                    "dataLimiteEtapa1": "2025-06-10",
                    "dataLimiteEtapa2": "2025-06-20",
                    "dataFimEtapa1": null,
                    "dataFimEtapa2": null,
                    "situacao": "Em andamento",
                    "unidadeAtual": "COSIS",
                    "unidadeAnterior": "SESEL",
                    "analises": []
                },
                {
                    "id": 3,
                    "idProcesso": 2,
                    "unidade": "SEDESENV",
                    "dataLimiteEtapa1": "2025-07-01",
                    "dataLimiteEtapa2": "2025-07-10",
                    "dataFimEtapa1": null,
                    "dataFimEtapa2": null,
                    "situacao": "Em andamento",
                    "unidadeAtual": "SEDESENV",
                    "unidadeAnterior": null,
                    "analises": []
                }
            ].map(parsesubprocessoDates)
        });
    });

    it('should initialize with mock processos and subprocessos with parsed dates', () => {
        expect(processosStore.processos.length).toBe(2); // Directly use the expected length
        expect(processosStore.processos[0].dataLimite).toBeInstanceOf(Date);
        expect(processosStore.subprocessos.length).toBe(3); // Directly use the expected length
        expect(processosStore.subprocessos[0].dataLimiteEtapa1).toBeInstanceOf(Date);
        expect(processosStore.subprocessos[0].dataLimiteEtapa1).not.toBeNull();
    });

    // Novos testes para as funções de mapeamento
    describe('Funções de Mapeamento', () => {
        it('mapTipoProcesso should return correct TipoProcesso for Mapeamento', () => {
            expect(mapTipoProcesso('Mapeamento')).toBe(TipoProcesso.MAPEAMENTO);
        });

        it('mapTipoProcesso should return correct TipoProcesso for Revisão', () => {
            expect(mapTipoProcesso('Revisão')).toBe(TipoProcesso.REVISAO);
        });

        it('mapTipoProcesso should return correct TipoProcesso for Diagnóstico', () => {
            expect(mapTipoProcesso('Diagnóstico')).toBe(TipoProcesso.DIAGNOSTICO);
        });

        it('mapTipoProcesso should return default TipoProcesso for unknown type', () => {
            expect(mapTipoProcesso('Unknown')).toBe(TipoProcesso.MAPEAMENTO);
        });

        it('mapSituacaoProcesso should return correct SituacaoProcesso for Criado', () => {
            expect(mapSituacaoProcesso('Criado')).toBe(SituacaoProcesso.CRIADO);
        });

        it('mapSituacaoProcesso should return correct SituacaoProcesso for Em andamento', () => {
            expect(mapSituacaoProcesso('Em andamento')).toBe(SituacaoProcesso.EM_ANDAMENTO);
        });

        it('mapSituacaoProcesso should return correct SituacaoProcesso for Finalizado', () => {
            expect(mapSituacaoProcesso('Finalizado')).toBe(SituacaoProcesso.FINALIZADO);
        });

        it('mapSituacaoProcesso should return default SituacaoProcesso for unknown situation', () => {
            expect(mapSituacaoProcesso('Unknown')).toBe(SituacaoProcesso.CRIADO);
        });
    });

    describe('getters', () => {
        it('getUnidadesDoProcesso should filter subprocessos by idProcesso', () => {
            const unidades = processosStore.getUnidadesDoProcesso(1);
            expect(unidades.length).toBe(2);
            expect(unidades[0].unidade).toBe('SESEL');
            expect(unidades[1].unidade).toBe('COSIS');
        });

        it('getUnidadesDoProcesso should return empty array if no matching idProcesso', () => {
            const unidades = processosStore.getUnidadesDoProcesso(999);
            expect(unidades.length).toBe(0);
        });

        // Novos testes para getSubprocessosElegiveisAceiteBloco
        it('getSubprocessosElegiveisAceiteBloco should filter by idProcesso and unidadeAtual', () => {
            const elegiveis = processosStore.getSubprocessosElegiveisAceiteBloco(1, 'SESEL');
            expect(elegiveis.length).toBe(0); // Nenhum mock está com essa situação
        });

        it('getSubprocessosElegiveisAceiteBloco should return empty array if no matching subprocessos', () => {
            const elegiveis = processosStore.getSubprocessosElegiveisAceiteBloco(999, 'NONEXISTENT');
            expect(elegiveis.length).toBe(0);
        });

        // Novos testes para getSubprocessosElegiveisHomologacaoBloco
        it('getSubprocessosElegiveisHomologacaoBloco should filter by idProcesso', () => {
            const elegiveis = processosStore.getSubprocessosElegiveisHomologacaoBloco(1);
            expect(elegiveis.length).toBe(0); // Nenhum mock está com essa situação
        });

        it('getSubprocessosElegiveisHomologacaoBloco should return empty array if no matching subprocessos', () => {
            const elegiveis = processosStore.getSubprocessosElegiveisHomologacaoBloco(999);
            expect(elegiveis.length).toBe(0);
        });

        // Novos testes para getMovementsForSubprocesso
        it('getMovementsForSubprocesso should return movements for existing subprocesso', () => {
            // Adicionar uma movimentação de teste
            processosStore.addMovement({
                idSubprocesso: 1,
                unidadeOrigem: 'A',
                unidadeDestino: 'B',
                descricao: 'Teste'
            });
            const movements = processosStore.getMovementsForSubprocesso(1);
            expect(movements.length).toBe(1);
            expect(movements[0].descricao).toBe('Teste');
        });

        it('getMovementsForSubprocesso should return empty array for non-existent subprocesso', () => {
            const movements = processosStore.getMovementsForSubprocesso(999);
            expect(movements.length).toBe(0);
        });

        it('getMovementsForSubprocesso should return empty array if no movements exist for subprocesso', () => {
            // Garantir que não há movimentações para o subprocesso 2
            const movements = processosStore.getMovementsForSubprocesso(2);
            expect(movements.length).toBe(0);
        });
    });

    describe('actions', () => {
        it('adicionarProcesso should add a new processo to the store', () => {
            const novoProcesso: Processo = {
                id: 3,
                descricao: 'Novo Processo de Teste',
                tipo: TipoProcesso.DIAGNOSTICO,
                dataLimite: new Date('2025-12-31'),
                situacao: SituacaoProcesso.EM_ANDAMENTO,
                dataFinalizacao: null
            };
            const initialLength = processosStore.processos.length;

            processosStore.adicionarProcesso(novoProcesso);

            expect(processosStore.processos.length).toBe(initialLength + 1);
            expect(processosStore.processos[initialLength]).toEqual(novoProcesso);
        });

        it('adicionarsubprocessos should add multiple subprocesso objects to the store', () => {
            const novassubprocessos: Subprocesso[] = [
                {
                    id: 4, idProcesso: 3, unidade: 'NOVA1',
                    dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(),
                    dataFimEtapa1: null, dataFimEtapa2: null,
                    situacao: 'Em andamento', unidadeAtual: 'NOVA1', unidadeAnterior: null,
                    movimentacoes: [],
                    analises: []
                },
                {
                    id: 5, idProcesso: 3, unidade: 'NOVA2',
                    dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(),
                    dataFimEtapa1: null, dataFimEtapa2: null,
                    situacao: 'Em andamento', unidadeAtual: 'NOVA2', unidadeAnterior: null,
                    movimentacoes: [],
                    analises: []
                }
            ];
            const initialLength = processosStore.subprocessos.length;

            processosStore.adicionarsubprocessos(novassubprocessos);

            expect(processosStore.subprocessos.length).toBe(initialLength + 2);
            expect(processosStore.subprocessos[initialLength]).toEqual(novassubprocessos[0]);
            expect(processosStore.subprocessos[initialLength + 1]).toEqual(novassubprocessos[1]);
        });

        it('finalizarProcesso should update situacao and dataFinalizacao for a process', () => {
            vi.useFakeTimers();
            const now = new Date('2025-11-01T10:00:00Z');
            vi.setSystemTime(now);

            processosStore.finalizarProcesso(2); // Processo with id 2 is 'Em andamento'

            const processoFinalizado = processosStore.processos.find((p: Processo) => p.id === 2);
            expect(processoFinalizado?.situacao).toBe(SituacaoProcesso.FINALIZADO);
            expect(processoFinalizado?.dataFinalizacao).toEqual(now);

            vi.useRealTimers();
        });

        it('finalizarProcesso should not change state if process not found', () => {
            const initialProcessos = [...processosStore.processos]; // Clone to compare
            processosStore.finalizarProcesso(999);
            expect(processosStore.processos).toEqual(initialProcessos);
        });
    });
});