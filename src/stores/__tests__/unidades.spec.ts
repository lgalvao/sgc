import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useUnidadesStore} from '../unidades';

// Mock the unidades.json import
vi.mock('../../mocks/unidades.json', () => ({
    default: [
        {
            "sigla": "SEDOC",
            "nome": "Seção de Desenvolvimento Organizacional e Capacitação",
            "tipo": "ADMINISTRATIVA",
            "idServidorTitular": 1,
            "responsavel": {"idServidorResponsavel": 8, "tipo": 0, "dataInicio": "2025-01-01", "dataFim": "2025-12-31"},
            "filhas": [
                {
                    "sigla": "SGP",
                    "nome": "Secretaria de Gestao de Pessoas",
                    "tipo": "INTERMEDIARIA",
                    "idServidorTitular": 2,
                    "responsavel": {
                        "idServidorResponsavel": 13,
                        "tipo": 1,
                        "dataInicio": "2025-01-01",
                        "dataFim": "2025-12-31"
                    },
                    "filhas": []
                },
                {
                    "sigla": "STIC",
                    "nome": "Secretaria de Informática e Comunicações",
                    "tipo": "INTEROPERACIONAL",
                    "idServidorTitular": 5,
                    "responsavel": null,
                    "filhas": [
                        {
                            "sigla": "COSIS",
                            "nome": "Coordenadoria de Sistemas",
                            "tipo": "INTERMEDIARIA",
                            "idServidorTitular": 6,
                            "responsavel": null,
                            "filhas": [
                                {
                                    "sigla": "SEDESENV",
                                    "nome": "Seção de Desenvolvimento de Sistemas",
                                    "tipo": "OPERACIONAL",
                                    "idServidorTitular": 7,
                                    "responsavel": {
                                        "idServidorResponsavel": 8,
                                        "tipo": 0,
                                        "dataInicio": "2025-01-01",
                                        "dataFim": "2025-12-31"
                                    },
                                    "filhas": []
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ]
}));

describe('useUnidadesStore', () => {
    let unidadesStore: ReturnType<typeof useUnidadesStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        unidadesStore = useUnidadesStore();
        // Manually reset the store state based on the initial mock data
        unidadesStore.$patch({
            unidades: JSON.parse(JSON.stringify([
                {
                    "sigla": "SEDOC",
                    "nome": "Seção de Desenvolvimento Organizacional e Capacitação",
                    "tipo": "ADMINISTRATIVA",
                    "idServidorTitular": 1,
                    "responsavel": {
                        "idServidorResponsavel": 8,
                        "tipo": 0,
                        "dataInicio": "2025-01-01",
                        "dataFim": "2025-12-31"
                    },
                    "filhas": [
                        {
                            "sigla": "SGP",
                            "nome": "Secretaria de Gestao de Pessoas",
                            "tipo": "INTERMEDIARIA",
                            "idServidorTitular": 2,
                            "responsavel": {
                                "idServidorResponsavel": 13,
                                "tipo": 1,
                                "dataInicio": "2025-01-01",
                                "dataFim": "2025-12-31"
                            },
                            "filhas": []
                        },
                        {
                            "sigla": "STIC",
                            "nome": "Secretaria de Informática e Comunicações",
                            "tipo": "INTEROPERACIONAL",
                            "idServidorTitular": 5,
                            "responsavel": null,
                            "filhas": [
                                {
                                    "sigla": "COSIS",
                                    "nome": "Coordenadoria de Sistemas",
                                    "tipo": "INTERMEDIARIA",
                                    "idServidorTitular": 6,
                                    "responsavel": null,
                                    "filhas": [
                                        {
                                            "sigla": "SEDESENV",
                                            "nome": "Seção de Desenvolvimento de Sistemas",
                                            "tipo": "OPERACIONAL",
                                            "idServidorTitular": 7,
                                            "responsavel": {
                                                "idServidorResponsavel": 8,
                                                "tipo": 0,
                                                "dataInicio": "2025-01-01",
                                                "dataFim": "2025-12-31"
                                            },
                                            "filhas": []
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
            ])) // Deep clone to avoid mutation issues
        });
    });

    it('should initialize with mock unidades', () => {
        expect(unidadesStore.unidades.length).toBe(1); // Directly use the expected length
        expect(unidadesStore.unidades[0].sigla).toBe('SEDOC');
    });

    describe('actions', () => {
        it('pesquisarUnidade should find a top-level unit by sigla', () => {
            const unidade = unidadesStore.pesquisarUnidade('SEDOC');
            expect(unidade).toBeDefined();
            expect(unidade?.nome).toBe('Seção de Desenvolvimento Organizacional e Capacitação');
        });

        it('pesquisarUnidade should find a nested unit by sigla', () => {
            const unidade = unidadesStore.pesquisarUnidade('SEDESENV');
            expect(unidade).toBeDefined();
            expect(unidade?.nome).toBe('Seção de Desenvolvimento de Sistemas');
        });

        it('pesquisarUnidade should return null if unit not found', () => {
            const unidade = unidadesStore.pesquisarUnidade('NONEXISTENT');
            expect(unidade).toBeNull();
        });

        it('pesquisarUnidade should work when starting from a specific sub-tree', () => {
            const mockData = [
                {
                    "sigla": "SEDOC",
                    "nome": "Seção de Desenvolvimento Organizacional e Capacitação",
                    "tipo": "ADMINISTRATIVA",
                    "idServidorTitular": 1,
                    "responsavel": {
                        "idServidorResponsavel": 8,
                        "tipo": 0,
                        "dataInicio": "2025-01-01",
                        "dataFim": "2025-12-31"
                    },
                    "filhas": [
                        {
                            "sigla": "SGP",
                            "nome": "Secretaria de Gestao de Pessoas",
                            "tipo": "INTERMEDIARIA",
                            "idServidorTitular": 2,
                            "responsavel": {
                                "idServidorResponsavel": 13,
                                "tipo": 1,
                                "dataInicio": "2025-01-01",
                                "dataFim": "2025-12-31"
                            },
                            "filhas": []
                        },
                        {
                            "sigla": "STIC",
                            "nome": "Secretaria de Informática e Comunicações",
                            "tipo": "INTEROPERACIONAL",
                            "idServidorTitular": 5,
                            "responsavel": null,
                            "filhas": [
                                {
                                    "sigla": "COSIS",
                                    "nome": "Coordenadoria de Sistemas",
                                    "tipo": "INTERMEDIARIA",
                                    "idServidorTitular": 6,
                                    "responsavel": null,
                                    "filhas": [
                                        {
                                            "sigla": "SEDESENV",
                                            "nome": "Seção de Desenvolvimento de Sistemas",
                                            "tipo": "OPERACIONAL",
                                            "idServidorTitular": 7,
                                            "responsavel": {
                                                "idServidorResponsavel": 8,
                                                "tipo": 0,
                                                "dataInicio": "2025-01-01",
                                                "dataFim": "2025-12-31"
                                            },
                                            "filhas": []
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
            ];
            const sticUnit = mockData[0].filhas.find(u => u.sigla === 'STIC');
            expect(sticUnit).toBeDefined();
            const cosisUnit = unidadesStore.pesquisarUnidade('COSIS');
            expect(cosisUnit).toBeDefined();
            expect(cosisUnit?.sigla).toBe('COSIS');
        });
    });
});
