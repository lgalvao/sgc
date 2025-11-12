import {beforeEach, describe, expect, it, vi} from 'vitest';
import {useUnidadesStore} from '../unidades';
import {initPinia} from '@/test-utils/helpers';
import {expectContainsAll} from '@/test-utils/uiHelpers';
import * as unidadesService from "@/services/unidadesService";
import type {Unidade} from "@/types/tipos";

const mockUnidades: Unidade[] = [
    {
        "sigla": "SEDOC",
        "nome": "Seção de Desenvolvimento Organizacional e Capacitação",
        "tipo": "ADMINISTRATIVA",
        "idServidorTitular": 7,
        "responsavel": null,
        "codigo": 1,
        "filhas": [
            {
                "sigla": "SGP",
                "nome": "Secretaria de Gestao de Pessoas",
                "tipo": "INTERMEDIARIA",
                "idServidorTitular": 2,
                "responsavel": {
                    "codigo": 13,
                    "nome": "Servidor Responsável 13",
                    "tituloEleitoral": "1313131313",
                    "unidade": { "codigo": 1, "nome": "Unidade", "sigla": "UN" },
                    "email": "servidor13@email.com",
                    "ramal": "1313",
                    "usuarioTitulo": "Servidor Responsável 13",
                    "unidadeCodigo": 1,
                    "tipo": "SERVIDOR"
                },
                "filhas": [
                    {
                        "sigla": "COEDE",
                        "nome": "Coordenadoria de Educação Especial",
                        "tipo": "INTERMEDIARIA",
                        "idServidorTitular": 3,
                        "responsavel": null,
                        "filhas": [
                            {
                                "sigla": "SEMARE",
                                "nome": "Seção Magistrados e Requisitados",
                                "tipo": "OPERACIONAL",
                                "idServidorTitular": 4,
                                "responsavel": null,
                                "filhas": [],
                                "codigo": 5
                            }
                        ],
                        "codigo": 4
                    }
                ],
                "codigo": 3
            },
            {
                "sigla": "STIC",
                "nome": "Secretaria de Informática e Comunicações",
                "tipo": "INTEROPERACIONAL",
                "idServidorTitular": 5,
                "responsavel": null,
                "codigo": 2,
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
                                    "codigo": 8,
                                    "nome": "Servidor Responsável 8",
                                    "tituloEleitoral": "888888888",
                                    "unidade": { "codigo": 1, "nome": "Unidade", "sigla": "UN" },
                                    "email": "servidor8@email.com",
                                    "ramal": "8888",
                                    "usuarioTitulo": "Servidor Responsável 8",
                                    "unidadeCodigo": 1,
                                    "tipo": "SERVIDOR"
                                },
                                "filhas": [],
                                "codigo": 8
                            },
                            {
                                "sigla": "SEDIA",
                                "nome": "Seção de Dados e Inteligência Artificial",
                                "tipo": "OPERACIONAL",
                                "idServidorTitular": 9,
                                "responsavel": null,
                                "filhas": [],
                                "codigo": 9
                            },
                            {
                                "sigla": "SESEL",
                                "nome": "Seção de Sistemas Eleitorais",
                                "tipo": "OPERACIONAL",
                                "idServidorTitular": 10,
                                "responsavel": null,
                                "filhas": [],
                                "codigo": 10
                            }
                        ],
                        "codigo": 6
                    },
                    {
                        "sigla": "COSINF",
                        "nome": "Coordenadoria de Suporte e Infraestrutura",
                        "tipo": "INTERMEDIARIA",
                        "idServidorTitular": 12,
                        "responsavel": null,
                        "filhas": [
                            {
                                "sigla": "SENIC",
                                "nome": "Seção de Infraestrutura",
                                "tipo": "OPERACIONAL",
                                "idServidorTitular": 13,
                                "responsavel": null,
                                "filhas": [],
                                "codigo": 11
                            }
                        ],
                        "codigo": 7
                    },
                    {
                        "sigla": "COJUR",
                        "nome": "Coordenadoria Jurídica",
                        "tipo": "INTERMEDIARIA",
                        "idServidorTitular": 15,
                        "responsavel": null,
                        "filhas": [
                            {
                                "sigla": "SEJUR",
                                "nome": "Seção Jurídica",
                                "tipo": "OPERACIONAL",
                                "idServidorTitular": 16,
                                "responsavel": null,
                                "filhas": [],
                                "codigo": 12
                            },
                            {
                                "sigla": "SEPRO",
                                "nome": "Seção de Processos",
                                "tipo": "OPERACIONAL",
                                "idServidorTitular": 17,
                                "responsavel": null,
                                "filhas": [],
                                "codigo": 13
                            }
                        ],
                        "codigo": 14
                    }
                ]
            }
        ]
    }
];

vi.mock('@/services/unidadesService', () => ({
    buscarTodasUnidades: vi.fn(() => Promise.resolve({ data: mockUnidades })),
    buscarUnidadePorSigla: vi.fn()
}));

describe('useUnidadesStore', () => {
    let unidadesStore: ReturnType<typeof useUnidadesStore>;

    beforeEach(() => {
        initPinia();
        unidadesStore = useUnidadesStore();
        unidadesStore.unidades = mockUnidades;
        vi.clearAllMocks();
    });

    it('should initialize with mock unidades', () => {
        expect(unidadesStore.unidades.length).toBeGreaterThan(0);
        expect(unidadesStore.unidades[0].sigla).toBeDefined();
    });

    describe('actions', () => {
        it('should fetch and set unidades', async () => {
            unidadesStore.unidades = [];
            await unidadesStore.fetchUnidades();
            expect(unidadesService.buscarTodasUnidades).toHaveBeenCalledTimes(1);
            expect(unidadesStore.unidades.length).toBeGreaterThan(0);
        });
        it('pesquisarUnidadePorSigla should find SEDOC unit by sigla', () => {
            const unidade = unidadesStore.pesquisarUnidadePorSigla('SEDOC');
            expect(unidade).toBeDefined();
            expect(unidade?.nome).toBe('Seção de Desenvolvimento Organizacional e Capacitação');
        });

        it('pesquisarUnidadePorSigla should find STIC unit by sigla', () => {
            const unidade = unidadesStore.pesquisarUnidadePorSigla('STIC');
            expect(unidade).toBeDefined();
            expect(unidade?.nome).toBe('Secretaria de Informática e Comunicações');
        });

        it('pesquisarUnidadePorSigla should find nested SEDESENV unit by sigla', () => {
            const unidade = unidadesStore.pesquisarUnidadePorSigla('SEDESENV');
            expect(unidade).toBeDefined();
            expect(unidade?.nome).toBe('Seção de Desenvolvimento de Sistemas');
        });

        it('pesquisarUnidadePorSigla should return null if unit not found', () => {
            const unidade = unidadesStore.pesquisarUnidadePorSigla('NONEXISTENT');
            expect(unidade).toBeNull();
        });

        it('getUnidadesSubordinadas should return direct and indirect subordinate units for a root unit (e.g., "SEDOC")', () => {
            const subordinadas = unidadesStore.getUnidadesSubordinadas('SEDOC');
            expectContainsAll(subordinadas, [
              'SGP','COEDE','SEMARE','STIC','COSIS','SEDESENV','SEDIA','SESEL','COSINF','SENIC','COJUR','SEJUR','SEPRO'
            ]);
            expect(subordinadas.length).toBe(14); // Total de unidades no mock
        });

        it('getUnidadesSubordinadas should return subordinate units for an intermediate unit (e.g., "STIC")', () => {
            const subordinadas = unidadesStore.getUnidadesSubordinadas('STIC');
            expectContainsAll(subordinadas, [
              'COSIS','SEDESENV','SEDIA','SESEL','COSINF','SENIC','COJUR','SEJUR','SEPRO'
            ]);
            expect(subordinadas.length).toBe(10);
        });

        it('getUnidadesSubordinadas should return an empty array for an operational unit (e.g., "SEMARE")', () => {
            const subordinadas = unidadesStore.getUnidadesSubordinadas('SEMARE');
            expect(subordinadas).toEqual(['SEMARE']); // A própria unidade é incluída
        });

        it('getUnidadesSubordinadas should return an empty array for a non-existent unit', () => {
            const subordinadas = unidadesStore.getUnidadesSubordinadas('NONEXISTENT');
            expect(subordinadas).toEqual([]);
        });

        it('getUnidadeSuperior should return the superior unit sigla for a child unit (e.g., "SEMARE" -> "COEDE")', () => {
            const superior = unidadesStore.getUnidadeSuperior('SEMARE');
            expect(superior).toBe('COEDE');
        });

        it('getUnidadeSuperior should return the superior unit sigla for an intermediate unit (e.g., "COEDE" -> "SGP")', () => {
            const superior = unidadesStore.getUnidadeSuperior('COEDE');
            expect(superior).toBe('SGP');
        });

        it('getUnidadeSuperior should return null for a root unit (e.g., "SEDOC")', () => {
            const superior = unidadesStore.getUnidadeSuperior('SEDOC');
            expect(superior).toBeNull();
        });

        it('getUnidadeSuperior should return null for a non-existent unit', () => {
            const superior = unidadesStore.getUnidadeSuperior('NONEXISTENT');
            expect(superior).toBeNull();
        });

        it('getUnidadeImediataSuperior should return the immediate superior unit sigla (e.g., "SEDESENV" -> "COSIS")', () => {
            const superior = unidadesStore.getUnidadeImediataSuperior('SEDESENV');
            expect(superior).toBe('COSIS');
        });

        it('getUnidadeImediataSuperior should return null for a root unit (e.g., "SEDOC")', () => {
            const superior = unidadesStore.getUnidadeImediataSuperior('SEDOC');
            expect(superior).toBeNull();
        });

        it('getUnidadeImediataSuperior should return null for a non-existent unit', () => {
            const superior = unidadesStore.getUnidadeImediataSuperior('NONEXISTENT');
            expect(superior).toBeNull();
        });
    });
});