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
                "responsavel": null,
                "filhas": [],
                "codigo": 3
            },
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
        ]
    }
];

vi.mock('@/services/unidadesService', () => ({
    buscarTodasUnidades: vi.fn(() => Promise.resolve(mockUnidades)),
    buscarUnidadePorSigla: vi.fn(),
    buscarUnidadePorCodigo: vi.fn(),
    buscarArvoreComElegibilidade: vi.fn(() => Promise.resolve(mockUnidades))
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
            await unidadesStore.fetchUnidadesParaProcesso('MAPEAMENTO');
            expect(unidadesService.buscarArvoreComElegibilidade).toHaveBeenCalledTimes(1);
            expect(unidadesStore.unidades.length).toBeGreaterThan(0);
        });

        it('should handle error in fetchUnidadesParaProcesso', async () => {
            vi.mocked(unidadesService.buscarArvoreComElegibilidade).mockRejectedValueOnce(new Error('API Error'));
            await unidadesStore.fetchUnidadesParaProcesso('MAPEAMENTO');
            expect(unidadesStore.error).toContain('Falha ao carregar unidades');
        });

        it('fetchUnidade should set unidade state', async () => {
            const mockUnit = { sigla: 'TEST', nome: 'Test Unit' };
            vi.mocked(unidadesService.buscarUnidadePorSigla).mockResolvedValue(mockUnit as any);

            await unidadesStore.fetchUnidade('TEST');

            expect(unidadesService.buscarUnidadePorSigla).toHaveBeenCalledWith('TEST');
            expect(unidadesStore.unidade).toEqual(mockUnit);
        });

        it('fetchUnidade should handle error', async () => {
            vi.mocked(unidadesService.buscarUnidadePorSigla).mockRejectedValue(new Error('Fail'));
            await unidadesStore.fetchUnidade('TEST');
            expect(unidadesStore.error).toContain('Falha ao carregar unidade');
        });

        it('fetchUnidadePorCodigo should set unidade state', async () => {
            const mockUnit = { codigo: 123, nome: 'Test Unit' };
            vi.mocked(unidadesService.buscarUnidadePorCodigo).mockResolvedValue(mockUnit as any);

            await unidadesStore.fetchUnidadePorCodigo(123);

            expect(unidadesService.buscarUnidadePorCodigo).toHaveBeenCalledWith(123);
            expect(unidadesStore.unidade).toEqual(mockUnit);
        });

        it('fetchUnidadePorCodigo should handle error', async () => {
             vi.mocked(unidadesService.buscarUnidadePorCodigo).mockRejectedValue(new Error('Fail'));
             await unidadesStore.fetchUnidadePorCodigo(123);
             expect(unidadesStore.error).toContain('Falha ao carregar unidade');
        });

        it('pesquisarUnidadePorSigla should find SEDOC unit by sigla', () => {
            const unidade = unidadesStore.pesquisarUnidadePorSigla('SEDOC');
            expect(unidade).toBeDefined();
            expect(unidade?.nome).toBe('Seção de Desenvolvimento Organizacional e Capacitação');
        });

        it('pesquisarUnidadePorCodigo should find unit by code', () => {
            const unidade = unidadesStore.pesquisarUnidadePorCodigo(3); // SGP
            expect(unidade).toBeDefined();
            expect(unidade?.sigla).toBe('SGP');
        });

        it('pesquisarUnidadePorCodigo should find nested unit', () => {
            const unidade = unidadesStore.pesquisarUnidadePorCodigo(5); // SEMARE
            expect(unidade).toBeDefined();
            expect(unidade?.sigla).toBe('SEMARE');
        });

        it('pesquisarUnidadePorSigla should return null if unit not found', () => {
            const unidade = unidadesStore.pesquisarUnidadePorSigla('NONEXISTENT');
            expect(unidade).toBeNull();
        });

        it('getUnidadesSubordinadas should return direct and indirect subordinate units', () => {
            const subordinadas = unidadesStore.getUnidadesSubordinadas('SEDOC');
            // Based on trimmed mockUnidades in this file
            expect(subordinadas).toContain('SGP');
            expect(subordinadas).toContain('COEDE');
            expect(subordinadas).toContain('SEMARE');
        });

        it('getUnidadeSuperior should return the superior unit sigla', () => {
            const superior = unidadesStore.getUnidadeSuperior('SEMARE');
            expect(superior).toBe('COEDE');
        });
    });
});
