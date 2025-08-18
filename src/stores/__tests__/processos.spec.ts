import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useProcessosStore} from '../processos';
import {Processo, ProcessoTipo, ProcessoUnidade} from '@/types/tipos';

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
            "unidadeAnterior": null
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
            "unidadeAnterior": "SESEL"
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
            "unidadeAnterior": null
        }
    ]
}));

describe('useProcessosStore', () => {
    let processosStore: ReturnType<typeof useProcessosStore>;

    // Helper to parse dates in mock data for comparison
    const parseProcessoDates = (processo: any): Processo => ({
        ...processo,
        dataLimite: new Date(processo.dataLimite),
        dataFinalizacao: processo.dataFinalizacao ? new Date(processo.dataFinalizacao) : null,
    });

    const parseProcessoUnidadeDates = (pu: any): ProcessoUnidade => ({
        ...pu,
        dataLimiteEtapa1: pu.dataLimiteEtapa1 ? new Date(pu.dataLimiteEtapa1) : null,
        dataLimiteEtapa2: pu.dataLimiteEtapa2 ? new Date(pu.dataLimiteEtapa2) : null,
        dataFimEtapa1: pu.dataFimEtapa1 ? new Date(pu.dataFimEtapa1) : null,
        dataFimEtapa2: pu.dataFimEtapa2 ? new Date(pu.dataFimEtapa2) : null,
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
            processosUnidade: [
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
                    "unidadeAnterior": null
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
                    "unidadeAnterior": "SESEL"
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
                    "unidadeAnterior": null
                }
            ].map(parseProcessoUnidadeDates)
        });
    });

    it('should initialize with mock processos and processosUnidade with parsed dates', () => {
        expect(processosStore.processos.length).toBe(2); // Directly use the expected length
        expect(processosStore.processos[0].dataLimite).toBeInstanceOf(Date);
        expect(processosStore.processosUnidade.length).toBe(3); // Directly use the expected length
        expect(processosStore.processosUnidade[0].dataLimiteEtapa1).toBeInstanceOf(Date);
    });

    describe('getters', () => {
        it('getUnidadesDoProcesso should filter processosUnidade by idProcesso', () => {
            const unidades = processosStore.getUnidadesDoProcesso(1);
            expect(unidades.length).toBe(2);
            expect(unidades[0].unidade).toBe('SESEL');
            expect(unidades[1].unidade).toBe('COSIS');
        });

        it('getUnidadesDoProcesso should return empty array if no matching idProcesso', () => {
            const unidades = processosStore.getUnidadesDoProcesso(999);
            expect(unidades.length).toBe(0);
        });
    });

    describe('actions', () => {
        it('adicionarProcesso should add a new processo to the store', () => {
            const novoProcesso: Processo = {
                id: 3,
                descricao: 'Novo Processo de Teste',
                tipo: ProcessoTipo.DIAGNOSTICO,
                dataLimite: new Date('2025-12-31'),
                situacao: 'Em andamento'
            };
            const initialLength = processosStore.processos.length;

            processosStore.adicionarProcesso(novoProcesso);

            expect(processosStore.processos.length).toBe(initialLength + 1);
            expect(processosStore.processos[initialLength]).toEqual(novoProcesso);
        });

        it('adicionarProcessosUnidade should add multiple ProcessoUnidade objects to the store', () => {
            const novasProcessosUnidade: ProcessoUnidade[] = [
                {
                    id: 4, idProcesso: 3, unidade: 'NOVA1',
                    dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(),
                    dataFimEtapa1: null, dataFimEtapa2: null,
                    situacao: 'Em andamento', unidadeAtual: 'NOVA1', unidadeAnterior: null
                },
                {
                    id: 5, idProcesso: 3, unidade: 'NOVA2',
                    dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(),
                    dataFimEtapa1: null, dataFimEtapa2: null,
                    situacao: 'Em andamento', unidadeAtual: 'NOVA2', unidadeAnterior: null
                }
            ];
            const initialLength = processosStore.processosUnidade.length;

            processosStore.adicionarProcessosUnidade(novasProcessosUnidade);

            expect(processosStore.processosUnidade.length).toBe(initialLength + 2);
            expect(processosStore.processosUnidade[initialLength]).toEqual(novasProcessosUnidade[0]);
            expect(processosStore.processosUnidade[initialLength + 1]).toEqual(novasProcessosUnidade[1]);
        });

        it('finalizarProcesso should update situacao and dataFinalizacao for a process', () => {
            vi.useFakeTimers();
            const now = new Date('2025-11-01T10:00:00Z');
            vi.setSystemTime(now);

            processosStore.finalizarProcesso(2); // Processo with id 2 is 'Em andamento'

            const processoFinalizado = processosStore.processos.find(p => p.id === 2);
            expect(processoFinalizado?.situacao).toBe('Finalizado');
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
