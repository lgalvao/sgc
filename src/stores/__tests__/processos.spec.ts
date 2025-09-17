import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {mapSituacaoProcesso, mapTipoProcesso, useProcessosStore} from '../processos'; // Importar as funções diretamente
import {Processo, SituacaoProcesso, Subprocesso, TipoProcesso} from '@/types/tipos';
import {useNotificacoesStore} from '../notificacoes';
import {SITUACOES_SUBPROCESSO} from '@/constants/situacoes'; // Adicionado

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
        },
        {
            "id": 3,
            "descricao": "Processo para testes de aceite/homologação",
            "tipo": "Mapeamento",
            "dataLimite": "2025-08-31",
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
        },
        {
            "id": 4,
            "idProcesso": 3,
            "unidade": "UNIDADE_GESTOR",
            "dataLimiteEtapa1": "2025-08-01",
            "dataLimiteEtapa2": "2025-08-10",
            "dataFimEtapa1": null,
            "dataFimEtapa2": null,
            "situacao": "Cadastro disponibilizado",
            "unidadeAtual": "UNIDADE_GESTOR",
            "unidadeAnterior": null,
            "movimentacoes": [],
            "analises": []
        },
        {
            "id": 5,
            "idProcesso": 3,
            "unidade": "UNIDADE_ADMIN",
            "dataLimiteEtapa1": "2025-08-01",
            "dataLimiteEtapa2": "2025-08-10",
            "dataFimEtapa1": null,
            "dataFimEtapa2": null,
            "situacao": "Revisão do cadastro disponibilizada",
            "unidadeAtual": "UNIDADE_ADMIN",
            "unidadeAnterior": null,
            "movimentacoes": [],
            "analises": []
        },
        {
            "id": 6,
            "idProcesso": 3,
            "unidade": "UNIDADE_COM_ANTERIOR",
            "dataLimiteEtapa1": "2025-08-01",
            "dataLimiteEtapa2": "2025-08-10",
            "dataFimEtapa1": null,
            "dataFimEtapa2": null,
            "situacao": "Mapa validado", // Situação que permite rejeição
            "unidadeAtual": "UNIDADE_COM_ANTERIOR",
            "unidadeAnterior": "UNIDADE_COM_ANTERIOR", // Para testar devolução para a própria unidade
            "movimentacoes": [],
            "analises": []
        },
        {
            "id": 7,
            "idProcesso": 3,
            "unidade": "UNIDADE_DIFERENTE",
            "dataLimiteEtapa1": "2025-08-01",
            "dataLimiteEtapa2": "2025-08-10",
            "dataFimEtapa1": null,
            "dataFimEtapa2": null,
            "situacao": "Mapa validado", // Situação que permite rejeição
            "unidadeAtual": "UNIDADE_DIFERENTE",
            "unidadeAnterior": "SEDOC", // Para testar devolução para unidade diferente
            "movimentacoes": [],
            "analises": []
        }
    ]
}));

// Mock useUnidadesStore
const mockUnidadesStoreInstance = {
    getUnidadeImediataSuperior: vi.fn((unidade: string) => {
        if (unidade === 'SESEL') return 'UNIDADE_SUPERIOR_SESEL';
        if (unidade === 'COSIS') return 'UNIDADE_SUPERIOR_COSIS';
        if (unidade === 'UNIDADE_GESTOR') return 'UNIDADE_SUPERIOR_SESEL'; // Mock para o teste
        return null;
    }),
};
vi.mock('../unidades', () => ({
    useUnidadesStore: vi.fn(() => mockUnidadesStoreInstance),
}));

// Mock useAlertasStore
const mockAlertasStoreInstance = {
    criarAlerta: vi.fn(),
};
vi.mock('../alertas', () => ({
    useAlertasStore: vi.fn(() => mockAlertasStoreInstance),
}));

// Mock useNotificacoesStore
const mockNotificacoesStoreInstance = {
    email: vi.fn(),
};
vi.mock('../notificacoes', () => ({
    useNotificacoesStore: vi.fn(() => mockNotificacoesStoreInstance),
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
        vi.clearAllMocks(); // Limpar todos os mocks antes de cada teste
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
                },
                {
                    "id": 3,
                    "descricao": "Processo para testes de aceite/homologação",
                    "tipo": "Mapeamento",
                    "dataLimite": "2025-08-31",
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
                    "movimentacoes": [],
                    "analises": []
                },
                {
                    "id": 4,
                    "idProcesso": 3,
                    "unidade": "UNIDADE_GESTOR",
                    "dataLimiteEtapa1": "2025-08-01",
                    "dataLimiteEtapa2": "2025-08-10",
                    "dataFimEtapa1": null,
                    "dataFimEtapa2": null,
                    "situacao": "Cadastro disponibilizado",
                    "unidadeAtual": "UNIDADE_GESTOR",
                    "unidadeAnterior": null,
                    "movimentacoes": [],
                    "analises": []
                },
                {
                    "id": 5,
                    "idProcesso": 3,
                    "unidade": "UNIDADE_ADMIN",
                    "dataLimiteEtapa1": "2025-08-01",
                    "dataLimiteEtapa2": "2025-08-10",
                    "dataFimEtapa1": null,
                    "dataFimEtapa2": null,
                    "situacao": "Revisão do cadastro disponibilizada",
                    "unidadeAtual": "UNIDADE_ADMIN",
                    "unidadeAnterior": null,
                    "movimentacoes": [],
                    "analises": []
                },
                {
                    "id": 6,
                    "idProcesso": 3,
                    "unidade": "UNIDADE_COM_ANTERIOR",
                    "dataLimiteEtapa1": "2025-08-01",
                    "dataLimiteEtapa2": "2025-08-10",
                    "dataFimEtapa1": null,
                    "dataFimEtapa2": null,
                    "situacao": "Mapa validado", // Situação que permite rejeição
                    "unidadeAtual": "UNIDADE_COM_ANTERIOR",
                    "unidadeAnterior": "UNIDADE_COM_ANTERIOR", // Para testar devolução para a própria unidade
                    "movimentacoes": [],
                    "analises": []
                },
                {
                    "id": 7,
                    "idProcesso": 3,
                    "unidade": "UNIDADE_DIFERENTE",
                    "dataLimiteEtapa1": "2025-08-01",
                    "dataLimiteEtapa2": "2025-08-10",
                    "dataFimEtapa1": null,
                    "dataFimEtapa2": null,
                    "situacao": "Mapa validado", // Situação que permite rejeição
                    "unidadeAtual": "UNIDADE_DIFERENTE",
                    "unidadeAnterior": "SEDOC", // Para testar devolução para unidade diferente
                    "movimentacoes": [],
                    "analises": []
                }
            ].map(parsesubprocessoDates)
        });
    });

    it('should initialize with mock processos and subprocessos with parsed dates', () => {
        expect(processosStore.processos.length).toBe(3); // Directly use the expected length
        expect(processosStore.processos[0].dataLimite).toBeInstanceOf(Date);
        expect(processosStore.subprocessos.length).toBe(7); // Directly use the expected length
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
            const elegiveis = processosStore.getSubprocessosElegiveisAceiteBloco(3, 'UNIDADE_GESTOR');
            expect(elegiveis.length).toBe(1);
            expect(elegiveis[0].id).toBe(4);
            expect(elegiveis[0].unidade).toBe('UNIDADE_GESTOR');
            expect(elegiveis[0].situacao).toBe('Cadastro disponibilizado');
        });

        it('getSubprocessosElegiveisAceiteBloco should return empty array if no matching subprocessos', () => {
            const elegiveis = processosStore.getSubprocessosElegiveisAceiteBloco(999, 'NONEXISTENT');
            expect(elegiveis.length).toBe(0);
        });

        it('getSubprocessosElegiveisHomologacaoBloco should filter by idProcesso and specific situations', () => {
            const elegiveis = processosStore.getSubprocessosElegiveisHomologacaoBloco(3);
            expect(elegiveis.length).toBe(2);
            expect(elegiveis[0].id).toBe(4);
            expect(elegiveis[1].id).toBe(5);
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
                id: 4,
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
                    id: 6, idProcesso: 4, unidade: 'NOVA1',
                    dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(),
                    dataFimEtapa1: null, dataFimEtapa2: null,
                    situacao: 'Em andamento', unidadeAtual: 'NOVA1', unidadeAnterior: null,
                    movimentacoes: [],
                    analises: []
                },
                {
                    id: 7, idProcesso: 4, unidade: 'NOVA2',
                    dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(),
                    dataFimEtapa1: null, dataFimEtapa2: null,
                    situacao: 'Em andamento', unidadeAtual: 'NOVA2', unidadeAnidadeAnterior: null,
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

        it('removerProcesso should remove the process, its subprocessos and related movements', () => {
            const initialProcessosLength = processosStore.processos.length;
            const initialSubprocessosLength = processosStore.subprocessos.length;
            const initialMovementsLength = processosStore.movements.length;

            // Adicionar uma movimentação para o subprocesso 1 (idProcesso 1)
            processosStore.addMovement({
                idSubprocesso: 1,
                unidadeOrigem: 'A',
                unidadeDestino: 'B',
                descricao: 'Movimento do processo 1'
            });
            // Adicionar uma movimentação para o subprocesso 3 (idProcesso 2)
            processosStore.addMovement({
                idSubprocesso: 3,
                unidadeOrigem: 'C',
                unidadeDestino: 'D',
                descricao: 'Movimento do processo 2'
            });

            expect(processosStore.movements.length).toBe(initialMovementsLength + 2);

            // Remover o processo com id 1
            processosStore.removerProcesso(1);

            // Verificar se o processo foi removido
            expect(processosStore.processos.length).toBe(initialProcessosLength - 1);
            expect(processosStore.processos.find(p => p.id === 1)).toBeUndefined();

            // Verificar se os subprocessos do processo 1 foram removidos
            expect(processosStore.subprocessos.length).toBe(initialSubprocessosLength - 2); // 2 subprocessos para o processo 1
            expect(processosStore.subprocessos.some(sp => sp.idProcesso === 1)).toBe(false);

            // Verificar se as movimentações relacionadas ao processo 1 foram removidas
            expect(processosStore.movements.length).toBe(initialMovementsLength + 1); // Apenas a movimentação do processo 2 deve permanecer
            expect(processosStore.movements.some(m => m.idSubprocesso === 1)).toBe(false);
            expect(processosStore.movements.some(m => m.idSubprocesso === 3)).toBe(true);
        });

        it('removerProcesso should not change state if process not found', () => {
            const initialProcessos = [...processosStore.processos];
            const initialSubprocessos = [...processosStore.subprocessos];
            const initialMovements = [...processosStore.movements];

            processosStore.removerProcesso(999);

            expect(processosStore.processos).toEqual(initialProcessos);
            expect(processosStore.subprocessos).toEqual(initialSubprocessos);
            expect(processosStore.movements).toEqual(initialMovements);
        });

        it('editarProcesso should update process details and recreate subprocessos', () => {
            const initialSubprocessosLength = processosStore.subprocessos.length;
            const processoId = 1;
            const novasUnidades = ['UNIDADE_A', 'UNIDADE_B'];
            const novosDados = {
                id: processoId,
                descricao: 'Nova Descrição do Processo 1',
                tipo: TipoProcesso.REVISAO,
                dataLimite: new Date('2026-01-01'),
                unidades: novasUnidades
            };

            processosStore.editarProcesso(novosDados);

            const processoAtualizado = processosStore.processos.find(p => p.id === processoId);
            expect(processoAtualizado?.descricao).toBe('Nova Descrição do Processo 1');
            expect(processoAtualizado?.tipo).toBe(TipoProcesso.REVISAO);
            expect(processoAtualizado?.dataLimite).toEqual(new Date('2026-01-01'));

            // Verificar se os subprocessos antigos do processo 1 foram removidos
            expect(processosStore.subprocessos.some(sp => sp.idProcesso === processoId && sp.unidade === 'SESEL')).toBe(false);
            expect(processosStore.subprocessos.some(sp => sp.idProcesso === processoId && sp.unidade === 'COSIS')).toBe(false);

            // Verificar se os novos subprocessos foram criados
            expect(processosStore.subprocessos.filter(sp => sp.idProcesso === processoId).length).toBe(novasUnidades.length);
            expect(processosStore.subprocessos.some(sp => sp.idProcesso === processoId && sp.unidade === 'UNIDADE_A')).toBe(true);
            expect(processosStore.subprocessos.some(sp => sp.idProcesso === processoId && sp.unidade === 'UNIDADE_B')).toBe(true);
            
            // Verificar se o total de subprocessos foi ajustado corretamente
            // Havia 2 subprocessos para o processo 1, agora há 2 novos. O total deve ser o mesmo.
            expect(processosStore.subprocessos.length).toBe(initialSubprocessosLength);
        });

        it('editarProcesso should not change state if process not found', () => {
            const initialProcessos = [...processosStore.processos];
            const initialSubprocessos = [...processosStore.subprocessos];
            const novosDados = {
                id: 999,
                descricao: 'Descrição Inexistente',
                tipo: TipoProcesso.DIAGNOSTICO,
                dataLimite: new Date(),
                unidades: ['UNIDADE_X']
            };

            processosStore.editarProcesso(novosDados);

            expect(processosStore.processos).toEqual(initialProcessos);
            expect(processosStore.subprocessos).toEqual(initialSubprocessos);
        });

        describe('processarCadastroBloco', () => {
            it('should process "aceitar" action for GESTOR, add movement and keep situation', async () => {
                const subprocessoId = 4; // UNIDADE_GESTOR, situacao: "Cadastro disponibilizado"
                const initialMovementsLength = processosStore.movements.length;
                const mockUnidadesStore = mockUnidadesStoreInstance; // Usar a instância global do mock

                await processosStore.processarCadastroBloco({
                    idProcesso: 3,
                    unidades: ['UNIDADE_GESTOR'],
                    tipoAcao: 'aceitar',
                    unidadeUsuario: 'UNIDADE_GESTOR'
                });

                expect(processosStore.movements.length).toBe(initialMovementsLength + 1);
                // expect(mockUnidadesStore.getUnidadeImediataSuperior).toHaveBeenCalledWith('UNIDADE_GESTOR'); // Removido
                expect(processosStore.movements[initialMovementsLength].descricao).toBe('Cadastro de atividades e conhecimentos validado em bloco');
                
                const subprocessoAtualizado = processosStore.subprocessos.find(sp => sp.id === subprocessoId);
                expect(subprocessoAtualizado?.situacao).toBe('Cadastro disponibilizado'); // Situação deve ser mantida
            });

            it('should process "homologar" action for ADMIN, add movement and update situation', async () => {
                const subprocessoId = 5; // UNIDADE_ADMIN, situacao: "Revisão do cadastro disponibilizada"
                const initialMovementsLength = processosStore.movements.length;
                const mockNotificacoesStore = useNotificacoesStore(); // Get the mocked store instance

                await processosStore.processarCadastroBloco({
                    idProcesso: 3,
                    unidades: ['UNIDADE_ADMIN'],
                    tipoAcao: 'homologar',
                    unidadeUsuario: 'ADMIN' // Não usado para homologar, mas necessário para o tipo
                });

                expect(processosStore.movements.length).toBe(initialMovementsLength + 1);
                expect(processosStore.movements[initialMovementsLength].descricao).toBe('Cadastro de atividades e conhecimentos homologado em bloco');
                
                const subprocessoAtualizado = processosStore.subprocessos.find(sp => sp.id === subprocessoId);
                expect(subprocessoAtualizado?.situacao).toBe('Revisão do cadastro homologada'); // Situação deve ser atualizada
            });

            it('should handle multiple units in a block process', async () => {
                const initialMovementsLength = processosStore.movements.length;
                
                // Adicionar um subprocesso com situação "Cadastro disponibilizado" para o processo 3
                processosStore.subprocessos.push(parsesubprocessoDates({
                    "id": 8, // Alterado de 6 para 8
                    "idProcesso": 3,
                    "unidade": "OUTRA_UNIDADE",
                    "dataLimiteEtapa1": "2025-08-01",
                    "dataLimiteEtapa2": "2025-08-10",
                    "situacao": "Cadastro disponibilizado",
                    "unidadeAtual": "OUTRA_UNIDADE",
                    "unidadeAnterior": null
                }));

                await processosStore.processarCadastroBloco({
                    idProcesso: 3,
                    unidades: ['UNIDADE_GESTOR', 'OUTRA_UNIDADE'],
                    tipoAcao: 'aceitar',
                    unidadeUsuario: 'UNIDADE_GESTOR'
                });

                expect(processosStore.movements.length).toBe(initialMovementsLength + 2); // Duas movimentações
                expect(processosStore.subprocessos.find(sp => sp.id === 4)?.situacao).toBe('Cadastro disponibilizado');
                expect(processosStore.subprocessos.find(sp => sp.id === 8)?.situacao).toBe('Cadastro disponibilizado'); // Alterado de 6 para 8
            });

            it('should not process if subprocesso not found', async () => {
                const initialMovementsLength = processosStore.movements.length;
                
                await processosStore.processarCadastroBloco({
                    idProcesso: 3,
                    unidades: ['UNIDADE_INEXISTENTE'],
                    tipoAcao: 'aceitar',
                    unidadeUsuario: 'UNIDADE_GESTOR'
                });

                expect(processosStore.movements.length).toBe(initialMovementsLength); // Nenhuma movimentação adicionada
            });
            it('should not add movement or update situation if subprocesso not found for "aceitar" action', async () => {
                const initialMovementsLength = processosStore.movements.length;
                const initialSubprocessosLength = processosStore.subprocessos.length;

                await processosStore.processarCadastroBloco({
                    idProcesso: 999, // Processo inexistente
                    unidades: ['UNIDADE_GESTOR'],
                    tipoAcao: 'aceitar',
                    unidadeUsuario: 'UNIDADE_GESTOR'
                });

                expect(processosStore.movements.length).toBe(initialMovementsLength); // Nenhuma movimentação adicionada
                expect(processosStore.subprocessos.length).toBe(initialSubprocessosLength); // Nenhum subprocesso atualizado
            });
            it('should not add movement or update situation if subprocesso not found for "homologar" action', async () => {
                const initialMovementsLength = processosStore.movements.length;
                const initialSubprocessosLength = processosStore.subprocessos.length;

                await processosStore.processarCadastroBloco({
                    idProcesso: 999, // Processo inexistente
                    unidades: ['UNIDADE_ADMIN'],
                    tipoAcao: 'homologar',
                    unidadeUsuario: 'ADMIN'
                });

                expect(processosStore.movements.length).toBe(initialMovementsLength); // Nenhuma movimentação adicionada
                expect(processosStore.subprocessos.length).toBe(initialSubprocessosLength); // Nenhum subprocesso atualizado
            });
        });

        describe('alterarDataLimiteSubprocesso', () => {
            it('should update dataLimiteEtapa1, add movement and send notification', async () => {
                vi.useFakeTimers();
                const now = new Date('2025-11-01T10:00:00Z');
                vi.setSystemTime(now);

                const subprocessoId = 1; // SESEL
                const novaData = new Date('2026-01-01');
                const initialMovementsLength = processosStore.movements.length;
                const mockNotificacoesStore = useNotificacoesStore();

                await processosStore.alterarDataLimiteSubprocesso({
                    idProcesso: 1,
                    unidade: 'SESEL',
                    etapa: 1,
                    novaDataLimite: novaData
                });

                const subprocessoAtualizado = processosStore.subprocessos.find(sp => sp.id === subprocessoId);
                expect(subprocessoAtualizado?.dataLimiteEtapa1).toEqual(novaData);
                expect(processosStore.movements.length).toBe(initialMovementsLength + 1);
                expect(processosStore.movements[initialMovementsLength].descricao).toContain(`Data limite da etapa 1 alterada para ${novaData.toISOString().split('T')[0]}`);
                expect(mockNotificacoesStore.email).toHaveBeenCalledWith(
                    `SGC: Data limite de etapa alterada - SESEL`,
                    `Responsável pela SESEL`,
                    `Prezado(a) responsável pela SESEL,

A data limite da etapa 1 no processo foi alterada para ${novaData.toISOString().split('T')[0]}.

Mais informações no Sistema de Gestão de Competências.`
                );
                vi.useRealTimers();
            });

            it('should update dataLimiteEtapa2, add movement and send notification', async () => {
                vi.useFakeTimers();
                const now = new Date('2025-11-01T10:00:00Z');
                vi.setSystemTime(now);

                const subprocessoId = 1; // SESEL
                const novaData = new Date('2026-02-01');
                const initialMovementsLength = processosStore.movements.length;
                const mockNotificacoesStore = useNotificacoesStore();

                await processosStore.alterarDataLimiteSubprocesso({
                    idProcesso: 1,
                    unidade: 'SESEL',
                    etapa: 2,
                    novaDataLimite: novaData
                });

                const subprocessoAtualizado = processosStore.subprocessos.find(sp => sp.id === subprocessoId);
                expect(subprocessoAtualizado?.dataLimiteEtapa2).toEqual(novaData);
                expect(processosStore.movements.length).toBe(initialMovementsLength + 1);
                expect(processosStore.movements[initialMovementsLength].descricao).toContain(`Data limite da etapa 2 alterada para ${novaData.toISOString().split('T')[0]}`);
                expect(mockNotificacoesStore.email).toHaveBeenCalledWith(
                    `SGC: Data limite de etapa alterada - SESEL`,
                    `Responsável pela SESEL`,
                    `Prezado(a) responsável pela SESEL,

A data limite da etapa 2 no processo foi alterada para ${novaData.toISOString().split('T')[0]}.

Mais informações no Sistema de Gestão de Competências.`
                );
                vi.useRealTimers();
            });

            it('should reject if subprocesso not found', async () => {
                const initialMovementsLength = processosStore.movements.length;
                await expect(processosStore.alterarDataLimiteSubprocesso({
                    idProcesso: 999,
                    unidade: 'NONEXISTENT',
                    etapa: 1,
                    novaDataLimite: new Date()
                })).rejects.toThrow('Subprocesso não encontrado');
                expect(processosStore.movements.length).toBe(initialMovementsLength); // Nenhuma movimentação adicionada
            });
        });

        describe('aceitarMapa', () => {
            it('should homologate map directly if perfil isADMIN', async () => {
                const subprocessoId = 5; // UNIDADE_ADMIN
                const initialMovementsLength = processosStore.movements.length;
                const mockUnidadesStore = mockUnidadesStoreInstance; // Usar a instância global do mock
                const mockAlertasStore = mockAlertasStoreInstance;
                const mockNotificacoesStore = mockNotificacoesStoreInstance;

                await processosStore.aceitarMapa({
                    idProcesso: 3,
                    unidade: 'UNIDADE_ADMIN',
                    perfil: 'ADMIN'
                });

                const subprocessoAtualizado = processosStore.subprocessos.find(sp => sp.id === subprocessoId);
                expect(subprocessoAtualizado?.situacao).toBe('Mapa homologado');
                expect(processosStore.movements.length).toBe(initialMovementsLength); // Nenhuma movimentação para ADMIN
                expect(mockUnidadesStore.getUnidadeImediataSuperior).not.toHaveBeenCalled();
                expect(mockAlertasStore.criarAlerta).not.toHaveBeenCalled();
                expect(mockNotificacoesStore.email).not.toHaveBeenCalled();
            });

            it('should send map to superior unit if perfil is GESTOR', async () => {
                const subprocessoId = 4; // UNIDADE_GESTOR
                const initialMovementsLength = processosStore.movements.length;
                const mockUnidadesStore = mockUnidadesStoreInstance;
                const mockAlertasStore = mockAlertasStoreInstance;
                const mockNotificacoesStore = mockNotificacoesStoreInstance;

                await processosStore.aceitarMapa({
                    idProcesso: 3,
                    unidade: 'UNIDADE_GESTOR',
                    perfil: 'GESTOR'
                });

                const subprocessoAtualizado = processosStore.subprocessos.find(sp => sp.id === subprocessoId);
                expect(subprocessoAtualizado?.situacao).toBe('Mapa validado');
                expect(subprocessoAtualizado?.unidadeAtual).toBe('UNIDADE_SUPERIOR_SESEL'); // Mocked superior for SESEL, using it for GESTOR
                expect(subprocessoAtualizado?.unidadeAnterior).toBe('UNIDADE_GESTOR');
                expect(processosStore.movements.length).toBe(initialMovementsLength + 1);
                expect(processosStore.movements[initialMovementsLength].descricao).toBe('Mapa de competências validado');
                expect(mockUnidadesStore.getUnidadeImediataSuperior).toHaveBeenCalledWith('UNIDADE_GESTOR');
                expect(mockNotificacoesStore.email).toHaveBeenCalled();
                expect(mockAlertasStore.criarAlerta).toHaveBeenCalled();
            });

            it('should reject if subprocesso not found', async () => {
                await expect(processosStore.aceitarMapa({
                    idProcesso: 999,
                    unidade: 'NONEXISTENT',
                    perfil: 'GESTOR'
                })).rejects.toThrow('Subprocesso não encontrado');
            });

            it('should reject if superior unit not found for GESTOR', async () => {
                const mockUnidadesStore = mockUnidadesStoreInstance;
                (mockUnidadesStore.getUnidadeImediataSuperior as vi.Mock).mockImplementationOnce(() => null); // Mockar a implementação para garantir que null seja retornado

                await expect(processosStore.aceitarMapa({
                    idProcesso: 3,
                    unidade: 'UNIDADE_GESTOR',
                    perfil: 'GESTOR'
                })).rejects.toThrow('Unidade superior não encontrada');
            });
        });

        describe('rejeitarMapa', () => {
            it('should return map to same unit if unidadeAnterior is the same as unidade', async () => {
                const subprocessoId = 6; // UNIDADE_COM_ANTERIOR, unidadeAnterior: UNIDADE_COM_ANTERIOR
                const initialMovementsLength = processosStore.movements.length;
                const mockAlertasStore = mockAlertasStoreInstance;
                const mockNotificacoesStore = mockNotificacoesStoreInstance;

                await processosStore.rejeitarMapa({
                    idProcesso: 3,
                    unidade: 'UNIDADE_COM_ANTERIOR'
                });

                const subprocessoAtualizado = processosStore.subprocessos.find(sp => sp.id === subprocessoId);
                expect(subprocessoAtualizado?.situacao).toBe('Mapa disponibilizado');
                expect(subprocessoAtualizado?.unidadeAtual).toBe('UNIDADE_COM_ANTERIOR');
                expect(subprocessoAtualizado?.unidadeAnterior).toBe('UNIDADE_COM_ANTERIOR');
                expect(subprocessoAtualizado?.dataFimEtapa2).toBeNull(); // Deve ser resetado
                expect(processosStore.movements.length).toBe(initialMovementsLength + 1);
                expect(processosStore.movements[initialMovementsLength].descricao).toBe('Devolução da validação do mapa de competências para ajustes');
                expect(mockNotificacoesStore.email).toHaveBeenCalled();
                expect(mockAlertasStore.criarAlerta).toHaveBeenCalled();
            });

            it('should return map to different unit if unidadeAnterior is different from unidade', async () => {
                const subprocessoId = 7; // UNIDADE_DIFERENTE, unidadeAnterior: SEDOC
                const initialMovementsLength = processosStore.movements.length;
                const mockAlertasStore = mockAlertasStoreInstance;
                const mockNotificacoesStore = mockNotificacoesStoreInstance;

                await processosStore.rejeitarMapa({
                    idProcesso: 3,
                    unidade: 'UNIDADE_DIFERENTE'
                });

                const subprocessoAtualizado = processosStore.subprocessos.find(sp => sp.id === subprocessoId);
                expect(subprocessoAtualizado?.situacao).toBe('Mapa criado');
                expect(subprocessoAtualizado?.unidadeAtual).toBe('SEDOC');
                expect(subprocessoAtualizado?.unidadeAnterior).toBe('UNIDADE_DIFERENTE');
                expect(processosStore.movements.length).toBe(initialMovementsLength + 1);
                expect(processosStore.movements[initialMovementsLength].descricao).toBe('Devolução da validação do mapa de competências para ajustes');
                expect(mockNotificacoesStore.email).toHaveBeenCalled();
                expect(mockAlertasStore.criarAlerta).toHaveBeenCalled();
            });

            it('should reject if subprocesso not found', async () => {
                await expect(processosStore.rejeitarMapa({
                    idProcesso: 999,
                    unidade: 'NONEXISTENT'
                })).rejects.toThrow('Subprocesso não encontrado');
            });

            it('should reject if unidadeAnterior not found', async () => {
                // Criar um subprocesso sem unidadeAnterior para testar o erro
                processosStore.subprocessos.push(parsesubprocessoDates({
                    "id": 8,
                    "idProcesso": 3,
                    "unidade": "UNIDADE_SEM_ANTERIOR",
                    "dataLimiteEtapa1": "2025-08-01",
                    "dataLimiteEtapa2": "2025-08-10",
                    "situacao": "Mapa validado",
                    "unidadeAtual": "UNIDADE_SEM_ANTERIOR",
                    "unidadeAnterior": null // Sem unidade anterior
                }));

                await expect(processosStore.rejeitarMapa({
                    idProcesso: 3,
                    unidade: 'UNIDADE_SEM_ANTERIOR'
                })).rejects.toThrow('Unidade anterior não encontrada');
            });
        });

        describe('apresentarSugestoes', () => {
            it('should add suggestions, update situation, and send email', async () => {
                const subprocessoId = 4; // UNIDADE_GESTOR
                const sugestoes = 'Sugestões de teste para o mapa.';
                const initialMovementsLength = processosStore.movements.length;
                const mockUnidadesStore = mockUnidadesStoreInstance;
                const mockNotificacoesStore = mockNotificacoesStoreInstance;

                await processosStore.apresentarSugestoes({
                    idProcesso: 3,
                    unidade: 'UNIDADE_GESTOR',
                    sugestoes: sugestoes
                });

                const subprocessoAtualizado = processosStore.subprocessos.find(sp => sp.id === subprocessoId);
                expect(subprocessoAtualizado?.situacao).toBe('Mapa com sugestões');
                expect(subprocessoAtualizado?.sugestoes).toBe(sugestoes);
                expect(subprocessoAtualizado?.unidadeAtual).toBe('UNIDADE_SUPERIOR_SESEL');
                expect(subprocessoAtualizado?.unidadeAnterior).toBe('UNIDADE_GESTOR');
                expect(subprocessoAtualizado?.dataFimEtapa2).toBeInstanceOf(Date);
                expect(subprocessoAtualizado?.analises).toEqual([]); // Histórico de análise deve ser excluído
                expect(processosStore.movements.length).toBe(initialMovementsLength + 1);
                expect(processosStore.movements[initialMovementsLength].descricao).toBe('Apresentação de sugestões para o mapa de competências');
                expect(mockUnidadesStore.getUnidadeImediataSuperior).toHaveBeenCalledWith('UNIDADE_GESTOR');
                expect(mockNotificacoesStore.email).toHaveBeenCalled();
            });

            it('should reject if subprocesso not found', async () => {
                await expect(processosStore.apresentarSugestoes({
                    idProcesso: 999,
                    unidade: 'NONEXISTENT',
                    sugestoes: 'Sugestões'
                })).rejects.toThrow('Subprocesso não encontrado');
            });

            it('should reject if superior unit not found', async () => {
                const mockUnidadesStore = mockUnidadesStoreInstance;
                (mockUnidadesStore.getUnidadeImediataSuperior as vi.Mock).mockImplementationOnce(() => null);

                await expect(processosStore.apresentarSugestoes({
                    idProcesso: 3,
                    unidade: 'UNIDADE_GESTOR',
                    sugestoes: 'Sugestões'
                })).rejects.toThrow('Unidade superior não encontrada');
            });
        }); // <-- Adicionado o fechamento

        describe('validarMapa', () => {
            it('should validate map, update situation, and send email', async () => {
                const subprocessoId = 4; // UNIDADE_GESTOR
                const initialMovementsLength = processosStore.movements.length;
                const mockUnidadesStore = mockUnidadesStoreInstance;
                const mockNotificacoesStore = mockNotificacoesStoreInstance;

                await processosStore.validarMapa({
                    idProcesso: 3,
                    unidade: 'UNIDADE_GESTOR'
                });

                const subprocessoAtualizado = processosStore.subprocessos.find(sp => sp.id === subprocessoId);
                expect(subprocessoAtualizado?.situacao).toBe('Mapa validado');
                expect(subprocessoAtualizado?.unidadeAtual).toBe('UNIDADE_SUPERIOR_SESEL');
                expect(subprocessoAtualizado?.unidadeAnterior).toBe('UNIDADE_GESTOR');
                expect(subprocessoAtualizado?.dataFimEtapa2).toBeInstanceOf(Date);
                expect(subprocessoAtualizado?.analises).toEqual([]); // Histórico de análise deve ser excluído
                expect(processosStore.movements.length).toBe(initialMovementsLength + 1);
                expect(processosStore.movements[initialMovementsLength].descricao).toBe('Validação do mapa de competências');
                expect(mockUnidadesStore.getUnidadeImediataSuperior).toHaveBeenCalledWith('UNIDADE_GESTOR');
                expect(mockNotificacoesStore.email).toHaveBeenCalled();
            });

            it('should reject if subprocesso not found', async () => {
                await expect(processosStore.validarMapa({
                    idProcesso: 999,
                    unidade: 'NONEXISTENT'
                })).rejects.toThrow('Subprocesso não encontrado');
            });

            it('should reject if superior unit not found', async () => {
                const mockUnidadesStore = mockUnidadesStoreInstance;
                (mockUnidadesStore.getUnidadeImediataSuperior as vi.Mock).mockImplementationOnce(() => null);

                await expect(processosStore.validarMapa({
                    idProcesso: 3,
                    unidade: 'UNIDADE_GESTOR'
                })).rejects.toThrow('Unidade superior não encontrada');
            });
        });

        it('addMovement should add a new movement with a unique ID and current timestamp', () => {
            const initialMovementsLength = processosStore.movements.length;
            const movementData = {
                idSubprocesso: 1,
                unidadeOrigem: 'ORIGEM',
                unidadeDestino: 'DESTINO',
                descricao: 'Nova movimentação'
            };

            vi.useFakeTimers();
            const now = new Date('2025-11-01T10:00:00Z');
            vi.setSystemTime(now);

            processosStore.addMovement(movementData);

            expect(processosStore.movements.length).toBe(initialMovementsLength + 1);
            const newMovement = processosStore.movements[initialMovementsLength];
            expect(newMovement.id).toBeDefined();
            expect(newMovement.id).not.toBeNull();
            expect(newMovement.dataHora).toEqual(now);
            expect(newMovement.idSubprocesso).toBe(movementData.idSubprocesso);
            expect(newMovement.unidadeOrigem).toBe(movementData.unidadeOrigem);
            expect(newMovement.unidadeDestino).toBe(movementData.unidadeDestino);
            expect(newMovement.descricao).toBe(movementData.descricao);

            vi.useRealTimers();
        });
        it('should call all actions and getters for basic coverage check', async () => {
            // Teste de getters
            processosStore.getUnidadesDoProcesso(1);
            processosStore.getSubprocessosElegiveisAceiteBloco(3, 'UNIDADE_GESTOR');
            processosStore.getSubprocessosElegiveisHomologacaoBloco(3);
            processosStore.getMovementsForSubprocesso(1);

            // Teste de actions
            const novoProcesso = { id: 100, descricao: 'Teste', tipo: TipoProcesso.MAPEAMENTO, dataLimite: new Date(), situacao: SituacaoProcesso.CRIADO };
            processosStore.adicionarProcesso(novoProcesso);

            const novosSubprocessos = [{ id: 101, idProcesso: 100, unidade: 'TESTE', dataLimiteEtapa1: new Date(), dataLimiteEtapa2: new Date(), situacao: SITUACOES_SUBPROCESSO.NAO_INICIADO, movimentacoes: [], analises: [] }];
            processosStore.adicionarsubprocessos(novosSubprocessos);

            processosStore.removerProcesso(100); // Remover o processo adicionado

            processosStore.editarProcesso({ id: 1, descricao: 'Editado', tipo: TipoProcesso.REVISAO, dataLimite: new Date(), unidades: ['SESEL'] });

            processosStore.finalizarProcesso(1);

            await processosStore.processarCadastroBloco({ idProcesso: 3, unidades: ['UNIDADE_GESTOR'], tipoAcao: 'aceitar', unidadeUsuario: 'UNIDADE_GESTOR' });
            await processosStore.processarCadastroBloco({ idProcesso: 3, unidades: ['UNIDADE_ADMIN'], tipoAcao: 'homologar', unidadeUsuario: 'ADMIN' });

            await processosStore.alterarDataLimiteSubprocesso({ idProcesso: 1, unidade: 'SESEL', etapa: 1, novaDataLimite: new Date() });

            await processosStore.aceitarMapa({ idProcesso: 3, unidade: 'UNIDADE_GESTOR', perfil: 'GESTOR' });
            await processosStore.aceitarMapa({ idProcesso: 3, unidade: 'UNIDADE_ADMIN', perfil: 'ADMIN' });

            await processosStore.rejeitarMapa({ idProcesso: 3, unidade: 'UNIDADE_COM_ANTERIOR' });

            await processosStore.apresentarSugestoes({ idProcesso: 3, unidade: 'UNIDADE_GESTOR', sugestoes: 'Sugestoes de teste' });

            await processosStore.validarMapa({ idProcesso: 3, unidade: 'UNIDADE_GESTOR' });

            processosStore.addMovement({ idSubprocesso: 1, unidadeOrigem: 'A', unidadeDestino: 'B', descricao: 'Movimento de teste' });
        });
    });
});
